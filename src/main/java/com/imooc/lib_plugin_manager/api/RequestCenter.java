package com.imooc.lib_plugin_manager.api;

import com.imooc.lib_network.okhttp.CommonOkHttpClient;
import com.imooc.lib_network.okhttp.listener.DisposeDataHandle;
import com.imooc.lib_network.okhttp.listener.DisposeDataListener;
import com.imooc.lib_network.okhttp.request.CommonRequest;
import com.imooc.lib_network.okhttp.request.RequestParams;

/**
 * 请求中心
 */
public class RequestCenter {

  static class HttpConstants {
    private static final String ROOT_URL = "http://imooc.com/api";
    //private static final String ROOT_URL = "http://39.97.122.129";
  }

  //根据参数发送所有post请求
  public static void getRequest(String url, RequestParams params, DisposeDataListener listener,
      Class<?> clazz) {
    CommonOkHttpClient.get(CommonRequest.
        createGetRequest(url, params), new DisposeDataHandle(listener, clazz));
  }

  /**
   * 加载服务端插件信息
   * @param listener
   */
  public static void requestPluginConfigData(DisposeDataListener listener) {

  }
}
