-keep class com.flipdesk.** { *; }
-keepattributes *Annotation*, InnerClasses
-dontwarn kotlinx.serialization.**

# kotlinx.serialization
-keepattributes Signature
-keepclassmembers class kotlinx.serialization.json.** { *; }
-keep,includedescriptorclasses class com.flipdesk.**$$serializer { *; }
-keepclassmembers class com.flipdesk.** {
    *** Companion;
}

# Hilt / Dagger / Room / WindowManager
-keep class dagger.hilt.** { *; }
-keep class androidx.room.** { *; }
-keep class androidx.window.** { *; }
