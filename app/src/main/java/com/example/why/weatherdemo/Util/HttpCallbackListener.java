package com.example.why.weatherdemo.Util;

/**
 * Created by why on 2017/3/11.
 */

public interface HttpCallbackListener
{
    void onFinish(String response);

    void onError(Exception e);
}
