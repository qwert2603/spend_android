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
-keepattributes Signature, InnerClasses, EnclosingMethod
-keepattributes RuntimeVisibleAnnotations, RuntimeVisibleParameterAnnotations
-keepclassmembers,allowshrinking,allowobfuscation interface * {
    @retrofit2.http.* <methods>;
}
-dontwarn org.codehaus.mojo.animal_sniffer.IgnoreJRERequirement
-dontwarn javax.annotation.**
-dontwarn kotlin.Unit
-dontwarn retrofit2.KotlinExtensions
-if interface * { @retrofit2.http.* <methods>; }
-keep,allowobfuscation interface <1>


# Crashlytics
-keepattributes *Annotation*
-keep public class * extends java.lang.Exception


# firebase performance
-keep class com.google.firebase.** { *; }


-keep class com.qwert2603.spend.model.rest.entity.** { *; }
-keep class com.qwert2603.spend.model.entity.** { *; }
-keep class com.qwert2603.spend.model.local_db.results.** { *; }
-keep class com.qwert2603.spend.model.local_db.entity.** { *; }
-keep class com.qwert2603.spend.model.local_db.tables.** { *; }


-dontwarn com.qwert2603.spend.dialogs.**