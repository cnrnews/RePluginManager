### RePluginManager
针对RePlguin 加载外置插件的一套工具封装库

### 如何使用
在需要加载外置插件的地方调用如下代码即可
```
 ImoocPluginManager.getInstance()
            .loadPlugin(WebViewPluginConfig.PLUGIN_NAME)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(new Consumer<PluginInfo>() {
              @Override
              public void accept(PluginInfo pluginInfo) throws Exception {
                // 跳转H5页面
                gotoWebView();
              }
            }, new Consumer<Throwable>() {
              @Override
              public void accept(Throwable throwable) throws Exception {
                PluginInfo pluginInfo = ImoocPluginManager.getInstance().getPluginInfo(WebViewPluginConfig.PLUGIN_NAME);
                if (pluginInfo!=null){
                  gotoWebView();
                }
              }
            });
```
