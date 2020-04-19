package app.yu.weather;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.media.Image;
import android.nfc.Tag;
import android.nfc.tech.TagTechnology;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import org.w3c.dom.Text;

import java.io.IOException;

import app.yu.weather.gson.Forecast;
import app.yu.weather.gson.Weather;
import app.yu.weather.service.AutoUpdateService;
import app.yu.weather.util.HttpUtil;
import app.yu.weather.util.Utility;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class WeatherActivity extends AppCompatActivity {



  private ScrollView weatherLayout;
  private TextView titleCity;
  private TextView titleUpdateTime;
  private TextView degreText;
  private TextView weatherInfoText;
  private LinearLayout forecastLayout;
  private TextView aqiText;
  private TextView pm25Text;
  private TextView comfortText;
  private TextView carWashText;
  private TextView sportText;
  private ImageView bingPicImage;
  public SwipeRefreshLayout swipeRefresh;
  private Button navButton;
  public DrawerLayout drawerLayout;


  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    // 将背景图和状态栏融合到一起
    if (Build.VERSION.SDK_INT >= 21) {
      // 拿到当前activity的decorView
      View decorView = getWindow().getDecorView();
      // 将activity的布局显示在状态栏之上
      decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
      // 将状态栏设置为透明色
      getWindow().setStatusBarColor(Color.TRANSPARENT);
    }

    setContentView(R.layout.activity_weather);

    // 初始化控件
    drawerLayout = findViewById(R.id.drawer_layout);
    navButton = findViewById(R.id.nav_button);
    swipeRefresh = findViewById(R.id.swipe_refresh);
    swipeRefresh.setColorSchemeResources(R.color.colorPrimary);;
    bingPicImage = findViewById(R.id.bing_pic_img);
    weatherLayout = findViewById(R.id.weather_layout);
    titleCity = findViewById(R.id.title_city);
    titleUpdateTime = findViewById(R.id.title_update_time);
    degreText = findViewById(R.id.degree_text);
    weatherInfoText = findViewById(R.id.weather_info_text);
    forecastLayout = findViewById(R.id.forecast_layout);
    aqiText = findViewById(R.id.aqi_text);
    pm25Text = findViewById(R.id.pm25_text);
    comfortText = findViewById(R.id.comfort_text);
    carWashText = findViewById(R.id.car_wash_text);
    sportText = findViewById(R.id.sport_text);
    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
    String weatherString = prefs.getString("weather", null);

    final String weatherId;
    if (weatherString != null) {
      // 有缓存解析天气数据
      Weather weather = Utility.handleWeatherResponse(weatherString);
      weatherId = weather.basic.weatherId;
      showWeatherInfo(weather);
    } else {
      // 无缓存去服务器查询天气
      String weatherid = getIntent().getStringExtra("weather_id");
      weatherId = weatherid;
      weatherLayout.setVisibility(View.INVISIBLE);
      requestWeather(weatherid);
    }

    // 先从缓存中加载，否则网络获取
    String bingPic = prefs.getString("bing_pic", null);
    if (bingPic != null) {
      Glide.with(this).load(bingPic).into(bingPicImage);
    } else {
      loadBingPic();
    }

    // 刷新
    swipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
      @Override
      public void onRefresh() {
        requestWeather(weatherId);
      }
    });

    // DrawerLayout 中第一个子控件用于作为主屏幕中显示的内容
    // 第二个子控件用于作为滑动菜单中显示的内容
    navButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        drawerLayout.openDrawer(GravityCompat.START);
      }
    });
  }

  // 加载bing的每日一图
  private void loadBingPic() {
    String bingURL = "http://guolin.tech/api/bing_pic";
    HttpUtil.sendOkHttpRequest(bingURL, new Callback() {
      @Override
      public void onFailure(Call call, IOException e) {
        e.printStackTrace();
      }

      @Override
      public void onResponse(Call call, Response response) throws IOException {
        final String bingPic = response.body().string();
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(WeatherActivity.this).edit();
        editor.putString("bing_pic", bingPic);
        editor.apply();
        runOnUiThread(new Runnable() {
          @Override
          public void run() {
            Glide.with(WeatherActivity.this).load(bingPic).into(bingPicImage);
          }
        });
      }
    });
  }

  // 根据天气id请求天气信息
  public void requestWeather(final String weatherid) {
    String weatherurl="http://guolin.tech/api/weather?cityid="+ weatherid + "&key=bc0418b57b2d4918819d3974ac1285d9";
    HttpUtil.sendOkHttpRequest(weatherurl, new Callback() {
      @Override
      public void onFailure(Call call, IOException e) {
        runOnUiThread(new Runnable() {
          @Override
          public void run() {
            Toast.makeText(WeatherActivity.this, "获取天气信息失败", Toast.LENGTH_SHORT).show();
            swipeRefresh.setRefreshing(false);
          }
        });
      }

      @Override
      public void onResponse(Call call, Response response) throws IOException {
        final String responseText = response.body().string();
        final Weather weather = Utility.handleWeatherResponse(responseText);
        runOnUiThread(new Runnable() {
          @Override
          public void run() {
            if (weather != null && "ok".equals(weather.status)) {
              SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(WeatherActivity.this).edit();
              editor.putString("weather", responseText);
              editor.apply();
              showWeatherInfo(weather);
            } else {
              Toast.makeText(WeatherActivity.this, "获取天气信息失败", Toast.LENGTH_SHORT).show();
            }
            swipeRefresh.setRefreshing(false);
          }
        });
      }
    });

    loadBingPic();
  }


  // 处理并展示 Weather 实体类中的数据
  private void showWeatherInfo(Weather weather) {
    if (weather != null && "ok".equals(weather.status)) {
      String cityName = weather.basic.cityName;
      String updateTime = weather.basic.update.updateTime.split(" ")[1];
      String degree = weather.now.temperature + "℃";
      String weatherInfo = weather.now.more.info;
      titleCity.setText(cityName);
      titleUpdateTime.setText(updateTime);
      degreText.setText(degree);
      weatherInfoText.setText(weatherInfo);
      forecastLayout.removeAllViews();

      for (Forecast forecast : weather.forecastList) {
        View view = LayoutInflater.from(this).inflate(R.layout.forecast_item, forecastLayout, false);
        TextView dateText = view.findViewById(R.id.date_text);
        TextView infoText = view.findViewById(R.id.info_text);
        TextView maxText = view.findViewById(R.id.max_text);
        TextView minText = view.findViewById(R.id.min_text);

        dateText.setText(forecast.date);
        infoText.setText(forecast.more.info);
        maxText.setText(forecast.temperature.max);
        minText.setText(forecast.temperature.min);
        forecastLayout.addView(view);
      }

      if (weather.aqi != null) {
        aqiText.setText(weather.aqi.city.aqi);
        pm25Text.setText(weather.aqi.city.pm25);
      }

      String comfort = "舒适度：" + weather.suggestion.comfort.info;
      String carWash = "洗车指数：" + weather.suggestion.carWash.info;
      String sport = "运动指数：" + weather.suggestion.sport.info;
      comfortText.setText(comfort);
      carWashText.setText(carWash);
      sportText.setText(sport);

      weatherLayout.setVisibility(View.VISIBLE);

      // 启动服务
      Intent intent = new Intent(this, AutoUpdateService.class);
      startService(intent);
    } else {
      Toast.makeText(WeatherActivity.this, "获取天气信息失败", Toast.LENGTH_SHORT).show();
    }
  }
}
