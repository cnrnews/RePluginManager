package com.imooc.lib_plugin_manager.model.plugin;

import java.io.Serializable;

/**
 * @Author lihl
 * @Date 2022/1/28 9:26
 * @Email 1601796593@qq.com
 */
public class HomePluginConfigInfo implements Serializable {
    public String mPluginName;
    // 当前插件版本号
    public float mPluginVersionCode;
    // 下载地址
    public String mPluginUrl;
    // 本地要保存的地址
    public String mLocalPath;
}
