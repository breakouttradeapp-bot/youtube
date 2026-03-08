# Retrofit
-keepattributes Signature
-keepattributes *Annotation*
-keep class retrofit2.** { *; }
-keepclasseswithmembers class * {
    @retrofit2.http.* <methods>;
}
# Gson
-keep class com.google.gson.** { *; }
-keep class com.aitube.seogenerator.models.** { *; }
# OkHttp
-dontwarn okhttp3.**
-keep class okhttp3.** { *; }
