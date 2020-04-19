package app.yu.weather.gson;

import com.google.gson.annotations.SerializedName;

/**
 * Created by jy on 2020/4/12.
 */

public class Now {
  @SerializedName("tmp")
  public String temperature;

  @SerializedName("cond")
  public More more;

  public class More {
    @SerializedName("txt")
    public String info;
  }
}
