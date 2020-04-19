package app.yu.weather.service;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.os.SystemClock;
import android.preference.PreferenceManager;

import java.io.IOException;

import app.yu.weather.gson.Weather;
import app.yu.weather.util.HttpUtil;
import app.yu.weather.util.Utility;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class AutoUpdateService extends Service {
  public AutoUpdateService() {
  }

  @Override
  public IBinder onBind(Intent intent) {
    return null;
  }

  @Override
  public int onStartCommand(Intent intent, int flags, int startId) {
    updateWeather();
    updateBingPic();

    // 8小时候会执行onStartCommand
    AlarmManager manager = (AlarmManager) getSystemService(ALARM_SERVICE);
    int hour = 8 * 60 * 60 * 1000; // 8小时的毫秒数
    long triggerAtTime = SystemClock.elapsedRealtime() + hour;
    Intent i = new Intent(this, AutoUpdateService.class);
    PendingIntent pi = PendingIntent.getService(this, 0, i, 0);
    manager.cancel(pi);
    manager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, triggerAtTime, pi);

    return super.onStartCommand(intent, flags, startId);
  }

  private void updateWeather() {
    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
    String weatherString = prefs.getString("weather", null);
    if (weatherString != null) {
      // 有缓存的时候直接解析天气数据
      Weather weather = Utility.handleWeatherResponse(weatherString);
      final String weatherId = weather.basic.weatherId;
      String weatherurl="http://guolin.tech/api/weather?cityid="+ weatherId + "&key=bc0418b57b2d4918819d3974ac1285d9";
      HttpUtil.sendOkHttpRequest(weatherurl, new Callback() {
        @Override
        public void onFailure(Call call, IOException e) {
          e.printStackTrace();
        }

        @Override
        public void onResponse(Call call, Response response) throws IOException {
          String resp = response.body().string();
          Weather weather = Utility.handleWeatherResponse(resp);
          if (weather != null && "ok".equals(weather.status)) {
            SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(AutoUpdateService.this).edit();
            editor.putString("weather", resp);
            editor.apply();
          }
        }
      });
    }
  }

  private void updateBingPic() {
    String bingURL = "http://guolin.tech/api/bing_pic";
    HttpUtil.sendOkHttpRequest(bingURL, new Callback() {
      @Override
      public void onFailure(Call call, IOException e) {
        e.printStackTrace();
      }

      @Override
      public void onResponse(Call call, Response response) throws IOException {
        String bingPic = response.body().string();
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(AutoUpdateService.this).edit();
        editor.putString("bing_pic", bingPic);
        editor.apply();
      }
    });
  }
}
