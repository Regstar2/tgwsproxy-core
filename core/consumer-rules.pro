-dontwarn java.awt.**
-dontwarn java.beans.**
-dontwarn javax.swing.**
-dontwarn com.sun.jna.**

-keep class com.sun.jna.** { *; }
-keep interface com.sun.jna.Library { *; }
-keep interface io.github.regstar2.tgwsproxy.core.TgWsNativeLibrary { *; }

-keepclassmembers class * extends com.sun.jna.Library {
    <methods>;
}
