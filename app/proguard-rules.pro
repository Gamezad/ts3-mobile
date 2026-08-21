# ts3j and its dependencies use reflection for protocol and crypto classes.
-keep class com.github.manevolent.ts3j.** { *; }
-keep class org.bouncycastle.** { *; }
-keep class org.xbill.DNS.** { *; }
-dontwarn org.bouncycastle.**
-dontwarn org.xbill.DNS.**
