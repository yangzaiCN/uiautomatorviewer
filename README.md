
##### 使用方式：
执行gradle jar命令(或gradlew jar)，将build目录下编译出来的uiautomatorviewer.jar替换你本地的${ANDROID_HOME}/tools/lib下的uiautomatorviewer.jar即可
或者使用跟目录下已编译好的jar

##### 背景：
在做UI自动化时必不可少需要使用到uiautomatorviewer这个工具，但是有时候我们进行元素定位的时候希望使用xpath定位，而这个 工具自身并没提供，为了方便自动生成xpath。在网上找到的方法感觉不是很完整，于是打算自己亲自对该工具进行二次开发。
- 开发环境：
    - ide:intellij idea
    - 编译环境：gradle
    - 语言环境：java
    - 还要必不可少的android sdk
    
##### 下载源码
- 下载[uiautomatorviewer源代码](https://android.googlesource.com/platform/frameworks/testing/+/aecdc4a/uiautomator/utils/uiautomatorviewer/)
下载完成后查看Android.mk，这里面有我们依赖的jar
```
LOCAL_JAVA_LIBRARIES := \
    swt \
    org.eclipse.jface_3.6.2.M20110210-1200 \
    org.eclipse.core.commands_3.6.0.I20100512-1500 \
    org.eclipse.equinox.common_3.6.0.v20100503
```
这些jar可以到你本地的${ANDROID_HOME}/tools/lib中找到

##### 构建：
- 添加gradle构建文件：
    - 1.在源码跟目录添加settings.gradle,build.gradle
settings.gradle:

```
rootProject.name = 'myuiautomatorview'
```
build.gradle:
```
group 'yangzaiCN'
version '1.0-SNAPSHOT'
def clientName = "uiautomatorviewer.jar"
apply plugin: 'java'

sourceCompatibility = 1.8

repositories {
    mavenCentral()
}

jar {
    manifest {
        attributes 'Implementation-Title': 'yangzaiCN uiautomator',
                'Implementation-Version': version,
                'Main-Class': 'com.android.uiautomator.UiAutomatorViewer',
                'Class-Path':'org-eclipse-jface-3.6.2.jar ddmlib.jar org-eclipse-core-c\
        ommands-3.6.0.jar org-eclipse-equinox-common-3.6.0.jar osgi-4.0.0.jar\
        common.jar kxml2-2.3.0.jar annotations.jar guava-17.0.jar'
    }
    archiveName = clientName
}

dependencies {
    compile fileTree(dir: 'libs', include: '*.jar')
}
```
主意：Class-Path中的jar要与你本地sdk中${ANDROID_HOME}/tools/lib目录下的jar版本对应。
![1517371277(1).png](http://upload-images.jianshu.io/upload_images/1909684-29d217dd4ec29a00.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)


- 2.根目录添加libs文件夹，把刚才找到的几个jar文件拷贝进来
- 3.使用ide导入gradle工程，编译完成后运行com.android.uiautomatorviewer.UiAutomatorViewer即可

##### 问题：
如果在dump资源的时候出现崩溃弹窗，如：

![1517297490(1).png](http://upload-images.jianshu.io/upload_images/1909684-14659be87b1a7d51.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

这些异常时使用adb命令时抛出的，具体原因后续研究，解决的方式也很简单，你可以先执行下dump或screencap命令。对应的类是
com.android.uiautomator.actions.ScreenshotAction

```
adb shell /system/bin/uiautomator dump /sdcard/uidump.xml
或
adb shell screencap -p /sdcard/screenshot.png
```    
![1517297801(1).png](http://upload-images.jianshu.io/upload_images/1909684-0cc3d75623af585d.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)
