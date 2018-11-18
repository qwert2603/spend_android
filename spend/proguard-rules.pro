# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in /home/alex/Android/Sdk/tools/proguard/proguard-android.txt
# You can edit the include path and order by changing the proguardFiles
# directive in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Add any project specific keep options here:

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
-renamesourcefileattribute SourceFile


# andrlib
-keepclasseswithmembernames class com.hannesdorfmann.mosby3.FragmentMviDelegateImpl { *; }


# andrlib_generator
-dontwarn com.qwert2603.andrlib.generator.**


# retrofit & okhttp
-dontwarn okio.**
-dontwarn okhttp3.**
-dontwarn javax.annotation.**
-dontwarn retrofit2.**
-dontwarn retrofit2.Platform$Java8
-dontnote retrofit2.Platform
-keepattributes Signature
-keepattributes Exceptions


-keep class com.qwert2603.spenddemo.model.rest.entity.** { *; }


-dontwarn com.qwert2603.spenddemo.dialogs.**