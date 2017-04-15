package com.coolweather.app.util;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by joke on 2017/4/13.
 */
public class HttpUtil {

    /**
     * 发送网络请求的时候调用该方法
    * @param address URL对象
    * @param listener  实例化的接口对象，回调方法需要实例化接口,网络反馈信息可通过该对象调用
    */
    public static void sendHttpRequest(final String address,final HttpCallbackListener listener){

        new Thread(new Runnable() {
            @Override
            public void run() {
                HttpURLConnection connection = null;
                try {
                    URL url = new URL(address);
                    connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("GET");
                    connection.setConnectTimeout(8000);
                    connection.setReadTimeout(8000);
                    connection.setDoInput(true);
                    connection.setDoOutput(true);
                    InputStream inputStream = connection.getInputStream();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                    StringBuilder response = new StringBuilder();
                    String line;
                    while((line = reader.readLine()) != null){
                        response.append(line);
                    }
                    if (listener != null){
                        //回调onFinish()方法
                        listener.onFinish(response.toString());
                    }
                } catch (IOException e) {
                    if (listener != null){
                        //回调onError()方法
                        listener.onError(e);
                    }
                }finally {
                    if (connection != null){
                        connection.disconnect();    //连接不为空就关闭连接
                    }
                }
            }
        }).start();

//        /**
//         * 心知天气API
//         */
//       new Thread(new Runnable() {
//            @Override
//            public void run() {
//                HttpURLConnection connection = null;
//                try {
//                    URL url = new URL(address);
//                    connection = (HttpURLConnection) url.openConnection();
//                    connection.setRequestMethod("GET");
//                    // 填入apikey到HTTP header
//                    connection.setRequestProperty("apikey", "5rfo8ug5elbnysc7");
//                    connection.connect();
//                    connection.setConnectTimeout(8000);
//                    connection.setReadTimeout(8000);
//                    connection.setDoInput(true);
//                    connection.setDoOutput(true);
//                    InputStream inputStream = connection.getInputStream();
//                    BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream,"UTF-8"));
//                    StringBuilder response = new StringBuilder();
//                    String line;
//                    while((line = reader.readLine()) != null){
//                        response.append(line);
//                    }
//                    reader.close();
//                    if (listener != null){
//                        //回调onFinish()方法
//                        listener.onFinish(response.toString());
//                    }
//                } catch (IOException e) {
//                    if (listener != null){
//                        //回调onError()方法
//                        listener.onError(e);
//                    }
//                }finally {
//                    if (connection != null){
//                        connection.disconnect();    //连接不为空就关闭连接
//                    }
//                }
//            }
//        }).start();
    }
}

//{

//    "results": [

//        {

//            "location": {

//                "id": "YB2BVPSH4JM5",

//                "name": "大庆",

//                "country": "CN",

//                "path": "大庆,大庆,黑龙江,中国",

//                "timezone": "Asia/Shanghai",

//                "timezone_offset": "+08:00"

//            },

//            "daily": [

//                {

//                    "date": "2016-12-30",

//                    "text_day": "晴",  白天天气

//                    "code_day": "0",

//                    "text_night": "晴",  夜间天气

//                    "code_night": "0",

//                    "high": "-8",   最高温度

//                    "low": "-18",   最低温度

//                    "precip": "",

//                    "wind_direction": "西",

//                    "wind_direction_degree": "270",

//                    "wind_speed": "15",

//                    "wind_scale": "3"   风力级别

//                },

//                {

//                    "date": "2016-12-31",

//                    "text_day": "晴",

//                    "code_day": "0",

//                    "text_night": "晴",

//                    "code_night": "0",

//                    "high": "-7",

//                    "low": "-18",

//                    "precip": "",

//                    "wind_direction": "西",

//                    "wind_direction_degree": "270",

//                    "wind_speed": "15",

//                    "wind_scale": "3"

//                },

//                {

//                    "date": "2017-01-01",

//                    "text_day": "晴",

//                    "code_day": "0",

//                    "text_night": "晴",

//                    "code_night": "0",

//                    "high": "-10",

//                    "low": "-17",

//                    "precip": "",

//                    "wind_direction": "南",

//                    "wind_direction_degree": "180",

//                    "wind_speed": "15",

//                    "wind_scale": "3"

//                }

//            ],

//            "last_update": "2016-12-30T08:00:00+08:00"

//        }

//    ]

//}

