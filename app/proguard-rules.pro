# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile
-dontoptimize
-dontpreverify

#极光push
-dontwarn cn.jpush.**
-keep class cn.jpush.** { *; }
-keep class * extends cn.jpush.android.helpers.JPushMessageReceiver { *; }

#讯飞语音
-keep class com.iflytek.**{*;}
-keepattributes Signature

#？？？
-dontwarn cn.jiguang.**
-keep class cn.jiguang.** { *; }


#高德定位
-keep class com.amap.api.location.**{*;}
-keep class com.amap.api.fence.**{*;}
-keep class com.autonavi.aps.amapapi.model.**{*;}

#高德搜索
-keep   class com.amap.api.services.**{*;}

#高德2D地图
-keep class com.amap.api.maps2d.**{*;}
-keep class com.amap.api.mapcore2d.**{*;}

#glide
-keep public class * implements com.bumptech.glide.module.GlideModule
-keep public class * extends com.bumptech.glide.module.AppGlideModule
-keep public enum com.bumptech.glide.load.ImageHeaderParser$** {
  **[] $VALUES;
  public *;
}

#ModPush
-keep class com.mob.**{*;}
-dontwarn com.mob.**
-keep class com.mob.**{*;}
-dontwarn com.mob.**

#huawei
-keep class com.huawei.**{*;}
-dontwarn com.huawei.**

#meizu
-keep class com.meizu.**{*;}
-dontwarn com.meizu.**

#xiaomi
-keep class com.xiaomi.**{*;}
-dontwarn com.xiaomi.**
-keep class org.apache.thrift.**{*;}
-keep class android.os.SystemProperties
-dontwarn android.os.SystemProperties

#FCM
-keep class com.google.**{*;}
-dontwarn com.google.**

#VIVO
-keep class com.vivo.push.**{*; }
-keep class com.vivo.vms.**{*; }
-dontwarn com.vivo.push.**

#OPPO
-keep class com.coloros.** {*;}
-dontwarn com.coloros.**


#mobPush
-keep class com.huawei.**{*;}
-keep class com.meizu.**{*;}
-keep class com.xiaomi.**{*;}
-keep class android.os.SystemProperties
-keep class com.coloros.** {*;}
-keep class com.google.** {*;}
-keep class org.apache.thrift.**{*;}

-dontwarn com.huawei.**
-dontwarn com.meizu.**
-dontwarn com.xiaomi.**
-dontwarn android.os.SystemProperties
-dontwarn com.coloros.**
-dontwarn com.google.**
-dontwarn org.apache.thrift.**

-dontwarn com.vivo.push.**
-keep class com.vivo.push.**{*; }
-keep class com.vivo.vms.**{*; }
-keep class com.huawei.**{*;}
-keep class com.meizu.**{*;}
-keep class com.xiaomi.**{*;}
-keep class android.os.SystemProperties
-keep class com.coloros.** {*;}
-keep class com.google.** {*;}
-keep class org.apache.thrift.**{*;}

-dontwarn com.huawei.**
-dontwarn com.meizu.**
-dontwarn com.xiaomi.**
-dontwarn android.os.SystemProperties
-dontwarn com.coloros.**
-dontwarn com.google.**
-dontwarn org.apache.thrift.**

-dontwarn com.vivo.push.**
-keep class com.vivo.push.**{*; }
-keep class com.vivo.vms.**{*; }