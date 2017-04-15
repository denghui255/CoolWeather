package com.coolweather.app.activity;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.coolweather.app.R;
import com.coolweather.app.util.HttpCallbackListener;
import com.coolweather.app.util.HttpUtil;
import com.coolweather.app.util.Utility;

import java.io.BufferedReader;

/**
 * Created by joke on 2017/4/14.
 */
public class WeatherActivity extends Activity implements View.OnClickListener {

    private LinearLayout ll_weatherInfo;
    private TextView tv_cityName;
    private TextView tv_publish;    //发布时间
    private TextView tv_weatherDesp;
    private TextView tv_temp1;
    private TextView tv_temp2;
    private TextView tv_currentData;    //当前时间

    private Button bt_changeCity;
    private Button bt_updateWeather;

    @Override
    public void onCreate(Bundle savedInstanceState, PersistableBundle persistentState) {
        super.onCreate(savedInstanceState, persistentState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.weather_layout);

        ll_weatherInfo = (LinearLayout) findViewById(R.id.id_ll_weatherInfo);
        tv_cityName = (TextView) findViewById(R.id.id_tv_cityName);
        tv_publish = (TextView) findViewById(R.id.id_tv_publish);
        tv_weatherDesp = (TextView) findViewById(R.id.id_tv_weatherDesp);
        tv_temp1 = (TextView) findViewById(R.id.id_tv_temp1);
        tv_temp2 = (TextView) findViewById(R.id.id_tv_temp2);
        tv_currentData = (TextView) findViewById(R.id.id_tv_currentData);
        bt_changeCity = (Button) findViewById(R.id.id_bt_changeCity);
        bt_updateWeather = (Button) findViewById(R.id.id_bt_updateWeather);

        String countyCode = getIntent().getStringExtra("county_code");
        if ( !TextUtils.isEmpty(countyCode)){
            //有县级代号时，去查询天气
            tv_publish.setText("Syncing...");
            ll_weatherInfo.setVisibility(View.INVISIBLE);
            tv_cityName.setVisibility(View.INVISIBLE);
            queryWeatherCode(countyCode);
        }else {
            //没有代号就直接显示本地天气
            showLocalWeather();
        }

        bt_changeCity.setOnClickListener(this);
        bt_updateWeather.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.id_bt_changeCity:
                Intent intent = new Intent(this,ChooseAreaActivity.class);
                intent.putExtra("From_Weather_Activity",true);
                startActivity(intent);
                finish();
                break;
            case R.id.id_bt_updateWeather:
                tv_publish.setText("Sync...");
                SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
                String weatherCode = sp.getString("weather_code","");
                if (TextUtils.isEmpty(weatherCode)){
                    queryWeatherInfo(weatherCode);
                }
                break;
            default:
                break;
        }
    }

    /**
     * 查询县级代号所对应的天气代号
     * @param countyCode
     */
    private void queryWeatherCode(String countyCode) {
        String address = "http://www.weather.com.cn/data/list3/city" + countyCode + ".xml";
        queryFromServer(address, "countyCode");
    }

    /**
     * 查询天气代号所对应的天气
     * @param weatherCode
     */
    private void queryWeatherInfo(String weatherCode) {
        String address = "http://www.weather.com.cn/data/cityinfo/" + weatherCode + ".html";
        queryFromServer(address, "weatherCode");
    }

    /**
     * 根据传入的地址和类型去向服务器查询天气代号或者天气信息。
     * @param address
     * @param type
     */
    private void queryFromServer(String address, final String type) {
        HttpUtil.sendHttpRequest(address, new HttpCallbackListener() {
            @Override
            public void onFinish(String response) {
                if ("countyCode".equals(type)){
                    if ( ! TextUtils.isEmpty(response)){
                        // 从服务器返回的数据中解析出天气代号
                        String[] array = response.split("\\|");
                        if (array != null && array.length == 2) {
                            String weatherCode = array[1];
                            queryWeatherInfo(weatherCode);
                        }
                    }else if ("weatherCode".equals(type)){
                        // 处理服务器返回的天气信息
                        Utility.handleWeatherResponse(WeatherActivity.this,response);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                showLocalWeather();
                            }
                        });
                    }
                }
            }

            @Override
            public void onError(Exception e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        tv_publish.setText("Failed to sync.");
                    }
                });
            }
        });
    }

    /**
     * 显示当地天气。
     * 从SharedPreferences文件中读取存储的天气信息，并显示到界面上。
     */
    private void showLocalWeather() {
            SharedPreferences prefs = PreferenceManager. getDefaultSharedPreferences(this);
            tv_cityName.setText( prefs.getString("city_name", ""));
            tv_temp1.setText(prefs.getString("temp1", ""));
            tv_temp2.setText(prefs.getString("temp2", ""));
            tv_weatherDesp.setText(prefs.getString("weather_desp", ""));
            tv_publish.setText("今天" + prefs.getString("publish_time", "") + "发布");
            tv_currentData.setText(prefs.getString("current_date", ""));
            ll_weatherInfo.setVisibility(View.VISIBLE);
            tv_cityName.setVisibility(View.VISIBLE);
        }
}
