package com.coolweather.app.util;

import android.text.TextUtils;

import com.coolweather.app.db.CoolWeatherDB;
import com.coolweather.app.model.City;
import com.coolweather.app.model.County;
import com.coolweather.app.model.Province;

/**
 * Created by joke on 2017/4/13.
 *
 * 省市县数据都是“代号|城市，代号|城市”这种格式，本类即是将其分开并保存至数据库
 *
 */
public class Utility {
    /**
     * 解析和处理服务器返回的省级数据
     */
    public synchronized static boolean handleProvinceResponse(CoolWeatherDB coolWeatherDB, String response){
        if ( ! TextUtils.isEmpty(response)){
            String[] allProvinces = response.split(",");    //以“，”为分裂符分开个省份
            for (String p : allProvinces){
                String[] onrProvince = p.split("\\|");
                Province province = new Province();
                province.setProvinceCode(onrProvince[0]);
                province.setProvinceName(onrProvince[1]);
                coolWeatherDB.saveProvince(province);
            }
            return  true;
        }
        return false;
    }
    /**
     * 解析和处理服务器返回的市级数据
     */
    public synchronized static boolean handleCityResponse(CoolWeatherDB coolWeatherDB, String response,int provinceId){
        if ( ! TextUtils.isEmpty(response)){
            String[] allCitys = response.split(",");    //以“，”为分裂符分开个省份
            if (allCitys != null && allCitys.length > 0){
                for (String c : allCitys){
                    String[] onrCity = c.split("\\|");
                    City city = new City();
                    city.setCityCode(onrCity[0]);
                    city.setCityName(onrCity[1]);
                    city.setProvinceId(provinceId);
                    coolWeatherDB.saveCity(city);
                }
            }
            return  true;
        }
        return false;
    }
    /**
     * 解析和处理服务器返回的县级数据
     */
    public synchronized static boolean handleCountyResponse(CoolWeatherDB coolWeatherDB, String response,int cityId){
        if ( ! TextUtils.isEmpty(response)){
            String[] allCountys = response.split(",");    //以“，”为分裂符分开个省份
            if (allCountys != null && allCountys.length > 0){
                for (String c : allCountys){
                    String[] onrCounty = c.split("\\|");
                    County county = new County();
                    county.setCountyCode(onrCounty[0]);
                    county.setCountyName(onrCounty[1]);
                    county.setCityId(cityId);
                    coolWeatherDB.saveCounty(county);
                }
            }
            return  true;
        }
        return false;
    }
}
