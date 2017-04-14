package com.coolweather.app.util;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.StringDef;
import android.text.TextUtils;

import com.coolweather.app.db.CoolWeatherDB;
import com.coolweather.app.model.City;
import com.coolweather.app.model.County;
import com.coolweather.app.model.Province;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.prefs.PreferencesFactory;

/**
 * Created by joke on 2017/4/13.
 *
 * 省市县数据都是“代号|城市，代号|城市”这种格式，本类即是将其分开并保存至数据库
 *
 */
public class Utility {
    /**
     * 解析和处理服务器返回的省级数据
     *
     * @param coolWeatherDB
     * @param response
     * @return
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
     *
     * @param coolWeatherDB
     * @param response
     * @param provinceId
     * @return
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
     *
     * @param coolWeatherDB
     * @param response
     * @param cityId
     * @return
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

    /**
     * 解析服务器返回的JSON数据，并将解析出的数据存储到本地。
     *
     * @param context
     * @param response
     */
    public synchronized static void handleWeatherResponse(Context context,String response){
        try {
            JSONObject jsonObject = new JSONObject(response);
            JSONObject weatherIndo = jsonObject.getJSONObject("weatherinfo");
            String cityName = weatherIndo.getString("city");
            String weatherCode = weatherIndo.getString("cityid");
            String temp1 = weatherIndo.getString("temp1");
            String temp2 = weatherIndo.getString("temp2");
            String weatherDesp = weatherIndo.getString("weather");
            String publishTime = weatherIndo.getString("ptime");
            saveWeatherInfo(context, cityName, weatherCode, temp1, temp2, weatherDesp, publishTime);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * 将天气信息存储到SharePreferences文件中
     *
     * @param context
     * @param cityName
     * @param weatherCode
     * @param temp1
     * @param temp2
     * @param weatherDesp
     * @param publishTime
     */
    private static void saveWeatherInfo(Context context, String cityName, String weatherCode, String temp1, String temp2, String weatherDesp, String publishTime) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy年M月d日", Locale.CHINA);
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
        editor.putBoolean("city_selected", true);
        editor.putString("city_name", cityName);
        editor.putString("weather_code", weatherCode);
        editor.putString("temp1", temp1);
        editor.putString("temp2", temp2);
        editor.putString("weather_desp", weatherDesp);
        editor.putString("publish_time", publishTime);
        editor.putString("current_date",sdf .format(new Date()));
        editor.commit();
    }
}