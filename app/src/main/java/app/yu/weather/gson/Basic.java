package app.yu.weather.gson;
import com.google.gson.annotations.SerializedName;

/**
 * Created by jy on 2020/4/12.
 */

/*
* base: {
*   city: "xxx",
*   id: 'CN000001',
*   update: {
*     "loc" : "2010-12-1"
*   }
* }
* */

public class Basic {
  @SerializedName("city")
  public String cityName;

  @SerializedName("id")
  public String weatherId;

  public Update update;

  public class Update {
    @SerializedName("loc")
    public String updateTime;
  }

}
