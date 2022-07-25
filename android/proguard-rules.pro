-keepattributes InnerClasses

# Installreferrer
-keepclassmembers class com.android.installreferrer.api.** {
  *;
}

# JWT
-keep class io.jsonwebtoken.** { *; }
-keepnames class io.jsonwebtoken.* { *; }
-keepnames interface io.jsonwebtoken.* { *; }

-keep class org.bouncycastle.** { *; }
-keepnames class org.bouncycastle.** { *; }
-dontwarn org.bouncycastle.**