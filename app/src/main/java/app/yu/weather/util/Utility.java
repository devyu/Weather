package app.yu.weather.util;

import android.text.TextUtils;

import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import app.yu.weather.db.City;
import app.yu.weather.db.County;
import app.yu.weather.db.Province;
import app.yu.weather.gson.Weather;


/**
 * Created by jy on 2020/3/3.
 */



public class Utility {

  // 解析天气数据成 Weather 实体类
  public static Weather handleWeatherResponse(String res) {
    try {
      JSONObject jsonObject = new JSONObject(res);
      JSONArray jsonArray = jsonObject.getJSONArray("HeWeather");
      String weatherContent = jsonArray.getJSONObject(0).toString();
      // 将JSON转换成Weather对象
      return new Gson().fromJson(weatherContent, Weather.class);
    } catch (Exception e) {
      e.printStackTrace();
    }
    return null;
  }

  // 解析省级数据
  public static boolean handleProvinceResponce(String res) {
    if (!TextUtils.isEmpty(res)) {
      try {
        JSONArray allProvinces = new JSONArray(res);
        for (int i = 0; i < allProvinces.length(); i++) {
          JSONObject obj = allProvinces.getJSONObject(i);
          Province province = new Province();
          province.setProvinceName(obj.getString("name"));
          province.setProvinceCode(obj.getInt("id"));
          // 存入数据库
          province.save();
        }
        return true;
      } catch (JSONException e) {
        e.printStackTrace();
      }
    }

    return false;
  }

  // 解析市级数据
  public static boolean handleCityResponse(String res, int provinceId) {
    if (!TextUtils.isEmpty(res)) {
      try {
        JSONArray allProvinces = new JSONArray(res);
        for (int i = 0; i < allProvinces.length(); i++) {
          JSONObject obj = allProvinces.getJSONObject(i);
          City city = new City();
          city.setCityName(obj.getString("name"));
          city.setCityCode(obj.getInt("id"));
          city.setProvinceId(provinceId);
          city.save();
        }
        return true;
      } catch (JSONException e) {
        e.printStackTrace();
      }
    }

    return false;
  }

  // 设置县数据
  public static boolean handleCountyResponse(String res, int cityId) {
    if (!TextUtils.isEmpty(res)) {
      try {
        JSONArray allCounties = new JSONArray(res);
        for (int i = 0; i < allCounties.length(); i++) {
          JSONObject obj = allCounties.getJSONObject(i);
          County county = new County();
          county.setCountyName(obj.getString("name"));
          county.setWeatherId(obj.getString("weather_id"));
          county.setCityId(cityId);
          county.save();
        }
        return true;
      } catch (JSONException e) {
        e.printStackTrace();
      }
    }

    return false;
  }
}
