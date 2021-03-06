package com.coolweather.app.activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.coolweather.app.R;
import com.coolweather.app.db.CoolWeatherDB;
import com.coolweather.app.model.City;
import com.coolweather.app.model.County;
import com.coolweather.app.model.Province;
import com.coolweather.app.util.HttpCallbackListener;
import com.coolweather.app.util.HttpUtil;
import com.coolweather.app.util.Utility;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by joke on 2017/4/13.
 */
public class ChooseAreaActivity extends Activity {

    public static final int LEVEL_PROVINCE = 0;
    public static final int LEVEL_CITY = 1;
    public static final int LEVEL_COUNTY = 2;

    private ProgressDialog progressDialog;
    private TextView tv_title;
    private ListView lv_view;
    private ArrayAdapter<String> arrayAdapter;
    private CoolWeatherDB coolWeatherDB;
    private List<String> dataList = new ArrayList<String>();    //ListView的数据列表

    private List<Province> provinceList;
    private List<City> cityList;
    private List<County> countyList;

    private Province selectedProvince;
    private City selectedCity;

    private int currentLevel;   //当前选中级别

    private boolean isFromWeatherActivity;  //是否从weatherActivity中跳转过来

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.choose_area);
        isFromWeatherActivity = getIntent().getBooleanExtra("From_Weather_Activity",false);
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        // 已经选择了城市且不是从WeatherActivity跳转过来，才会直接跳转到 WeatherActivity
        if (sharedPreferences.getBoolean("city_selected",false) && isFromWeatherActivity){
            Intent intent = new Intent(this,WeatherActivity.class);
            startActivity(intent);
            finish();
            return;
        }
        lv_view = (ListView) findViewById(R.id.id_lv_view);
        tv_title = (TextView) findViewById(R.id.id_tv_title);

        arrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1,dataList);
        lv_view.setAdapter(arrayAdapter);

        coolWeatherDB = CoolWeatherDB.getInstant(this);
        lv_view.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (currentLevel == LEVEL_PROVINCE) {
                    selectedProvince = provinceList.get(position);
                    queryCities();
                } else if (currentLevel == LEVEL_CITY) {
                    selectedCity = cityList.get(position);
                    queryCounties();
                }else if (currentLevel == LEVEL_COUNTY){
                    String countyCode = countyList.get(position).getCountyCode();
                    Intent intent = new Intent(ChooseAreaActivity.this,WeatherActivity.class);
                    intent.putExtra("county_code",countyCode);
                    startActivity(intent);
                    finish();
                }
            }
        });
        queryProvinces();
    }

    /**
     * 查询全国所有的省，优先在数据库中查询，若没有再去网络服务器上查。
     */
    private void queryProvinces() {
        provinceList = coolWeatherDB.loadProvince();
        if (provinceList.size() > 0 ){
            dataList.clear();
            for (Province province : provinceList){
                dataList.add(province.getProvinceName());
            }
            arrayAdapter.notifyDataSetChanged();
            lv_view.setSelection(0);
            tv_title.setText("中国");
            currentLevel = LEVEL_PROVINCE;
        }else {
            queryFromServer(null, "province");
        }
    }

    /**
     * 查询省内所有的市，优先在数据库中查询，若没有再去网络服务器上查。
     */
    private void queryCities() {
        cityList = coolWeatherDB.loadCity(selectedProvince.getId());
        Log.d("CDB", "loadCity: >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>><<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<");
        Log.d("CDB", "loadCity: >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>><<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<");
        if (cityList.size() > 0 ){
            dataList.clear();
            for(City c : cityList){
                dataList.add(c.getCityName());
            }
            arrayAdapter.notifyDataSetChanged();
            lv_view.setSelection(0);
            tv_title.setText(selectedProvince.getProvinceName());
            currentLevel = LEVEL_CITY;
        }else {
            queryFromServer(selectedProvince.getProvinceCode(), "city");
        }
    }

    /**
     *查询市内所有城市，优先在数据库中查询，若没有再去网络服务器上查。
     */
    private void queryCounties() {
        countyList = coolWeatherDB.loadCounty(selectedCity.getId());
        if (countyList.size() > 0){
            dataList.clear();
            for (County c : countyList){
                dataList.add(c.getCountyName());
            }
            arrayAdapter.notifyDataSetChanged();
            lv_view.setSelection(0);
            tv_title.setText(selectedCity.getCityName());
            currentLevel = LEVEL_COUNTY;
        }else {
            queryFromServer(selectedCity.getCityCode(), "county");
        }
    }

    /**
     * 根据传入的代号和类型从服务器上查询省市县数据
     * @param code 省市县的代号
     * @param type 查询类型
     */
    public void queryFromServer(final String code, final String type) {
        String address;
        if (!TextUtils.isEmpty(code)){
            address = "http://www.weather.com.cn/data/list3/city" + code + ".xml";
        }else {
            address = "http://www.weather.com.cn/data/list3/city.xml";
        }
//        /**
//         * 心知天气API
//         */
//        if (!TextUtils.isEmpty(code)){
//            address = "http://apis.baidu.com/thinkpage/weather_api/suggestion" +  "？location=daqing&language=zh-Hans&unit=c&start=0&days=3";
//        }else {
//            address = "http://www.weather.com.cn/data/list3/city.xml";
//        }
        showProgressDialog();
        HttpUtil.sendHttpRequest(address, new HttpCallbackListener() {
            @Override
            public void onFinish(String response) {
                boolean result = false;
                if ("province".equals(type)){
                    result = Utility.handleProvinceResponse(coolWeatherDB,response);
                }else if ("city".equals(type)){
                    result = Utility.handleCityResponse(coolWeatherDB,response,selectedProvince.getId());
                }else if ("county".equals(type)){
                    result = Utility.handleCountyResponse(coolWeatherDB,response,selectedCity.getId());
                }
                if (result){
                    //通过runOnUIThread方法回到主线程处理逻辑
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {

                            closeProgressDialog();

                            if ("province".equals(type)){
                                queryProvinces();
                            }else if ("city".equals(type)){
                                queryCities();
                            }else if ("county".equals(type)){
                                queryCounties();
                            }
                        }
                    });
                }
            }

            @Override
            public void onError(Exception e) {
                //通过runOnUIThread方法回到主线程处理逻辑
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        closeProgressDialog();
                        Toast.makeText(ChooseAreaActivity.this, "Fail to load.", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    /**
     * 显示加载对话窗
     */
    private void showProgressDialog() {
        if (progressDialog == null){
            progressDialog = new ProgressDialog(this);
            progressDialog.setMessage("Loading...");
            progressDialog.setCanceledOnTouchOutside(false);
        }
        progressDialog.show();
    }

    /**
     * 关闭进度对话窗
     */
    private void closeProgressDialog() {
        if (progressDialog != null){
            progressDialog.dismiss();
        }
    }

    /**
     * 捕获back键，判断是返回市列表、省列表、还是直接退出。
     */
    public void onBackPressed(){
        if (currentLevel == LEVEL_COUNTY){
            queryCities();
        }else if (currentLevel == LEVEL_CITY){
            queryProvinces();
        }else {
            if (isFromWeatherActivity){
                Intent intent = new Intent(this,WeatherActivity.class);
                startActivity(intent);
            }
            finish();
        }
    }
}
