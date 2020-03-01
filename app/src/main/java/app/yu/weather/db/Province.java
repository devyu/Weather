package app.yu.weather.db;

import org.litepal.crud.LitePalSupport;

/**
 * Created by jy on 2020/3/1.
 */

public class Province extends LitePalSupport {

  private int id;
  private String provinceName;
  private int provinceCode;

  public int getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
  }

  public String getProvinceName() {
    return provinceName;
  }

  public void setProvinceName(String provinceName) {
    this.provinceName = provinceName;
  }

  public int getProvinceCode() {
    return provinceCode;
  }

  public void setProvinceCode(int provinceCode) {
    this.provinceCode = provinceCode;
  }
}
