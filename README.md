##高仿豌豆荚免Root自动安装（AccessibilityService）


- 对于那些由于视力、听力或其它身体原因导致不能方便使用 Android 智能手机的用户，
Android 提供了 Accessibility 功能和服务帮助这些用户更加简单地操作设备，包括文字转语音、触觉反馈、
手势操作、轨迹球和手柄操作。开发者可以搭建自己的 Accessibility 服务，这可以加强应用的可用性，
例如声音提示，物理反馈，和其他可选的操作模式。

- 随着Android系统版本的迭代，Accessibility功能也越来越强大，它能实时地获取当前操作应用的
窗口元素信息，并能够双向交互，既能获取用户的输入，也能对窗口元素进行操作，比如点击按钮。
更多的介绍见Android开发者官网的[Accessibility](https://developer.android.com/guide/topics/ui/accessibility/index.html)页面。

- 话不多说直接开始，首先使用Android Accessibility 需要三个步骤：

    1、申请权限
    2、注册服务
    3、配置 AccessibilityService Info

首先需要申请权限
```
<uses-permission android:name="android.permission.BIND_ACCESSIBILITY_SERVICE" />

```

注册服务

```
 <service
         android:name=".Your Accessibility Name"
         android:enabled="true"
         android:exported="true"
         android:label="Your Service Title"
         android:permission="android.permission.BIND_ACCESSIBILITY_SERVICE"
         android:process=":accessibility">
         <intent-filter>
             <action android:name="android.accessibilityservice.AccessibilityService" />
         </intent-filter>
         <meta-data
             android:name="android.accessibilityservice"
             android:resource="@xml/accessibility_config" />
 </service>

```
配置 AccessibilityService Info

```
<accessibility-service
    xmlns:android="http://schemas.android.com/apk/res/android"
    android:accessibilityEventTypes="typeViewScrolled|typeWindowContentChanged|typeWindowStateChanged"
    android:accessibilityFeedbackType="feedbackAllMask"
    android:accessibilityFlags="flagDefault|flagReportViewIds"
    android:canRetrieveWindowContent="true"
    android:description="@string/auto_service_des"
    android:packageNames="packagename1,packagename2" />
```
在这里需要注意的是packageNames是AccessibilityService所监听的应用的包名。可以监听多个，在自动安装
的时候箭筒不同的包名用于做适配。源码中会有所要适配的包名，包含了大部分安装程序的包名。

该程序实现自动装的原理分析：
首先我们可以用 getRootInActiveWindow()，和event.getSource()均可以得到AccessibilityNodeInfo的实例
，即为触发这次事件的UI节点。

重写AccessibilityService服务，实现onAccessibilityEvent方法，该方法是监听服务监听到界面变化会调用
因此，我们从该方法去做实现我们的自动安装功能。

那么我们如何找到UI元素呢？
1、findAccessibilityNodeInfosByText(String text) 该方法可以根据控件显示的文本得到控件。所注意的是
该方法的逻辑是包含（contains）而不是等于(equal)。
例如：参数我们传递 "安装" ，那么像，"是否安装？"，"安装"，都会得到，所以需要我们去处理。

最后我们使用模拟用户点击实现自动点击效果
```
nodeInfo.performAction(AccessibilityNodeInfo.ACTION_CLICK);
```
实现的基本流程就是这样，但是这只是刚刚开始，我们需要更严格的逻辑去处理，现在可以看下onAccessibilityEvent
方法我是怎么实现的，

```
private final Set<String> installViewSet = new HashSet<>(Arrays.asList(new String[]{"com.android.packageinstaller.PackageInstallerActivity",
            "com.android.packageinstaller.OppoPackageInstallerActivity","com.android.packageinstaller.InstallAppProgress",
            "com.lenovo.safecenter.install.InstallerActivity","com.lenovo.safecenter.defense.install.fragment.InstallInterceptActivity",
            "com.lenovo.safecenter.install.InstallProgress","com.lenovo.safecenter.install.InstallAppProgress",
            "com.lenovo.safecenter.defense.fragment.install.InstallInterceptActivity"}));

    private final Set<String> installPkgSet = new HashSet<>(Arrays.asList(new String[]{"com.samsung.android.packageinstaller",
            "com.android.packageinstaller", "com.google.android.packageinstaller", "com.lenovo.safecenter", "com.lenovo.security"
            , "com.xiaomi.gamecenter"}));

    private final Set<String> uninstallPkgSet = new HashSet<>(Arrays.asList(new String[]{"com.android.packageinstaller.UninstallAppProgress"
            , "android.app.AlertDialog"}));

    boolean isInstallOrUninstall = true;

    private List<String> nodeContents;
    private List<String> completeTexts;
    private List<String> installTexts;


private void doAccessibilityEvent(AccessibilityEvent event) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN) {

            String className = event.getClassName().toString();
            if (uninstallPkgSet.contains(className)) {
                isInstallOrUninstall = false;
            }

            if(installViewSet.contains(event.getClassName().toString())) {
                isInstallOrUninstall = true;
            }

            if (installViewSet.contains(event.getPackageName().toString())) {
                isInstallOrUninstall = true;
            }

            AccessibilityNodeInfo rootNodeInfo = getRootInActiveWindow();

            if (rootNodeInfo != null && isInstallOrUninstall) {
                String pkgName = (String) rootNodeInfo.getPackageName();

                if (installPkgSet.contains(pkgName)) {
                    for (int i = 0; i < nodeContents.size(); i++) {
                        List<AccessibilityNodeInfo> textNodeInfo = new ArrayList<>();
                        for (int k = 0; k < completeTexts.size(); k++) {
                            textNodeInfo.addAll(rootNodeInfo.findAccessibilityNodeInfosByText(completeTexts.get(k)));
                        }

                        if (textNodeInfo.size() > 0) {
                            for (int j = 0; j < textNodeInfo.size(); j++) {
                                String text = textNodeInfo.get(j).getText().toString();
                                if (completeTexts.contains(text)) {
                                    clickInstall(textNodeInfo.get(j));
                                }
                            }
                        }
                    }
                }
            }

            AccessibilityNodeInfo nodeInfo = event.getSource();

            if (nodeInfo != null && isInstallOrUninstall) {
                for (int i = 0; i < nodeContents.size(); i++) {
                    List<AccessibilityNodeInfo> textNodeInfo = nodeInfo.findAccessibilityNodeInfosByText(nodeContents.get(i));
                    List<AccessibilityNodeInfo> installNodeInfo = new ArrayList<>();
                    for (int k = 0; k < completeTexts.size(); k++) {
                        installNodeInfo.addAll(nodeInfo.findAccessibilityNodeInfosByText(installTexts.get(k)));
                    }

                    boolean isInstall = installNodeInfo.size() != 0;

                    if (textNodeInfo != null && textNodeInfo.size() > 0) {
                        for (int j = 0; j < textNodeInfo.size(); j++) {
                            String text = textNodeInfo.get(j).getText().toString();
                            if (nodeContents.contains(text) && isInstall) {
                                clickInstall(textNodeInfo.get(j));
                            }
                        }
                    }
                }
            }
        }
    }

```

在这里我是根据豌豆荚所兼容的android手机都进行了兼容处理，并对多语言进行处理。大家看到我对单个文字也进行了list话不理解的话，
想想多语言，估计就理解了。

目前测试国内及国外手机几乎都能实现自动装。

目前有一个AppInstall的管理类，实现了Root安装，及accessibility安装的管理。让使用起来更加方便。

[项目地址](https://github.com/alicemmm/accessibility)



