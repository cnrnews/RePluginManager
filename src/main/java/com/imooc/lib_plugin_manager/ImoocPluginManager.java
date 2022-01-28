package com.imooc.lib_plugin_manager;

import com.imooc.lib_network.okhttp.CommonOkHttpClient;
import com.imooc.lib_network.okhttp.listener.DisposeDataHandle;
import com.imooc.lib_network.okhttp.listener.DisposeDataListener;
import com.imooc.lib_network.okhttp.request.CommonRequest;
import com.imooc.lib_network.okhttp.utils.ResponseEntityToModule;
import com.imooc.lib_plugin_manager.api.MockData;
import com.imooc.lib_plugin_manager.api.RequestCenter;
import com.imooc.lib_plugin_manager.model.plugin.HomeConfigResponse;
import com.imooc.lib_plugin_manager.model.plugin.HomePluginConfigInfo;
import com.qihoo360.replugin.RePlugin;
import com.qihoo360.replugin.model.PluginInfo;

import java.io.File;
import java.util.concurrent.Callable;

import androidx.annotation.NonNull;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.ObservableSource;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.functions.Predicate;
import io.reactivex.schedulers.Schedulers;

/**
 * @Author lihl
 * @Date 2022/1/28 10:48
 * @Email 1601796593@qq.com
 *
 * 插件下载、更新、信息获取管理类
 */
public class ImoocPluginManager {
    private static ImoocPluginManager mInstance = null;
    public static ImoocPluginManager getInstance(){
        if (mInstance == null){
            synchronized (ImoocPluginManager.class){
                if (mInstance == null){
                    mInstance = new ImoocPluginManager();
                }
            }
        }
        return mInstance;
    }

    // 服务端插件配置信息
    private HomeConfigResponse mHomeConfigResponse;

    /**
     * 加载服务端插件信息
     */
    public void requestPluginConfigData(){
        RequestCenter.requestPluginConfigData(new DisposeDataListener() {
            @Override
            public void onSuccess(Object responseObj) {
                mHomeConfigResponse = (HomeConfigResponse) responseObj;
            }
            @Override
            public void onFailure(Object reasonObj) {
                // 使用模拟数据
                onSuccess(ResponseEntityToModule.parseJsonToModule(
                        MockData.HOME_CONFIG_DATA,
                        HomeConfigResponse.class
                ));
            }
        });
    }

    /**
     * 获取服务端插件信息
     * @param pluginName
     * @return
     */
    public Observable<HomePluginConfigInfo> getServerPluginInfo(@NonNull final String pluginName){
        return Observable.fromCallable(new Callable<HomePluginConfigInfo>() {
            @Override
            public HomePluginConfigInfo call() throws Exception {
                if (mHomeConfigResponse!=null && mHomeConfigResponse.data!=null){
                    for (HomePluginConfigInfo info : mHomeConfigResponse.data.list) {
                        if (info.mPluginName.equals(pluginName)){
                            return info;
                        }
                    }
                }
                return null;
            }
        });
    }

    /**
     * 获取宿主工程中的插件信息
     * @param pluginName
     * @return
     */
    public PluginInfo getPluginInfo(String pluginName){
        return RePlugin.getPluginInfo(pluginName);
    }

    /**
     * 获取可观察的插件信息
     * @param pluginName
     * @return
     */
    public Observable<PluginInfo> fetchPluginInfo(String pluginName){
        return Observable.just(RePlugin.getPluginInfo(pluginName));
    }
    /**
     * 下载插件
     * @param pluginUrl
     * @param savePath
     * @return
     */
    public Observable<File> downloadPlugin(@NonNull final String pluginUrl,
                                           @NonNull final String savePath){
        return Observable.create(new ObservableOnSubscribe<File>() {
            @Override
            public void subscribe(@io.reactivex.annotations.NonNull final ObservableEmitter<File> emitter) throws Exception {
                // 文件下载
                CommonOkHttpClient.downloadFile(
                        CommonRequest.createGetRequest(pluginUrl,null),
                        new DisposeDataHandle(new DisposeDataListener() {
                    @Override
                    public void onSuccess(Object responseObj) {
                        File file = (File) responseObj;
                        emitter.onNext(file);
                        emitter.onComplete();
                    }
                    @Override
                    public void onFailure(Object reasonObj) {
                        emitter.onError((Throwable) reasonObj);
                    }
                },savePath));
            }
        });
    }

    /**
     * 安装插件
     * @param filePath
     * @return
     */
    public Observable<PluginInfo> installPlugin(@NonNull final String filePath){
        return Observable.fromCallable(new Callable<PluginInfo>() {
            @Override
            public PluginInfo call() {
                return RePlugin.install(filePath);
            }
        });
    }

    /**
     * 安装插件
     * @param info
     * @return
     */
    public boolean preloadPlugin(@NonNull final PluginInfo info){
        return RePlugin.preload(info);
    }

    /**
     * 获取插件信息
     * @param pluginName
     * @return
     */
    public Observable<HomePluginConfigInfo> getDownloadPluginInfo(@NonNull String pluginName){
        return getServerPluginInfo(pluginName);
    }

    /**
     * 检查是否需要更新
     * @param pluginName
     * @return
     */
    public Observable<HomePluginConfigInfo> getUpdatePluginInfo(@NonNull String pluginName,
                                                                @NonNull final PluginInfo info){
        return getServerPluginInfo(pluginName).filter(new Predicate<HomePluginConfigInfo>() {
            @Override
            public boolean test(@io.reactivex.annotations.NonNull HomePluginConfigInfo homePluginConfigInfo) throws Exception {
                return info.getVersionValue() < homePluginConfigInfo.mPluginVersionCode;
            }
        });
    }

    /**
     * 对外提供插件加载信息
     * 获取远程插件信息，比较插件版本，判断是下载插件还是更新插件
     * @param pluginName
     * @return
     */
    public Observable<PluginInfo> loadPlugin(@NonNull final String pluginName){
        return fetchPluginInfo(pluginName)
                .flatMap(new Function<PluginInfo, ObservableSource<HomePluginConfigInfo>>() {
                    @Override
                    public ObservableSource<HomePluginConfigInfo> apply(PluginInfo pluginInfo) throws Exception {
                        return pluginInfo == null ? getDownloadPluginInfo(pluginName)
                                : getUpdatePluginInfo(pluginName,pluginInfo);
                    }
                }).flatMap(new Function<HomePluginConfigInfo, ObservableSource<File>>() {
                    @Override
                    public ObservableSource<File> apply(@io.reactivex.annotations.NonNull HomePluginConfigInfo homePluginConfigInfo){
                        // 下载插件
                        return downloadPlugin(
                                homePluginConfigInfo.mPluginUrl,
                                homePluginConfigInfo.mLocalPath
                        );
                    }
                }).observeOn(Schedulers.io()) // 在子线程操作
                .flatMap(new Function<File, ObservableSource<PluginInfo>>() {
                    @Override
                    public ObservableSource<PluginInfo> apply(@io.reactivex.annotations.NonNull File file) {
                        // 安装插件
                        return installPlugin(file.getAbsolutePath());
                    }
                }).doOnNext(new Consumer<PluginInfo>() {
                    @Override
                    public void accept(PluginInfo pluginInfo) throws Exception {
                        if (pluginInfo!=null){
                            // 预加载插件
                            preloadPlugin(pluginInfo);
                        }
                    }
                });
    }
}

