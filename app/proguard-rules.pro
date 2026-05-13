# Xposed/LSPosed
-keep class de.robv.android.xposed.** { *; }
-keep class com.darkk.compatcontrol.xposed.** { *; }

# Data models
-keep class com.darkk.compatcontrol.data.** { *; }

# Gson
-keepattributes Signature
-keepattributes *Annotation*
-keep class com.google.gson.** { *; }
-keep class * implements com.google.gson.TypeAdapterFactory
-keep class * implements com.google.gson.JsonSerializer
-keep class * implements com.google.gson.JsonDeserializer

# libsu
-keep class com.topjohnwu.superuser.** { *; }
