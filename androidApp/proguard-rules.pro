# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in /opt/sdk/tools/proguard/proguard-android.txt
# You can edit the include path and order by changing the proguardFiles
# directive in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Add any project specific keep options here:

# Openfeedback
-keep class io.openfeedback.** { *; }

# CRASHLYTICS
-keepattributes SourceFile,LineNumberTable        # Keep file names and line numbers.
-keep public class * extends java.lang.Exception  # Optional: Keep custom exceptions.

# Should not be needed after Appolo GraphQL upgrades OKHTTP to version 4.11
-dontwarn org.bouncycastle.jsse.BCSSLParameters
-dontwarn org.bouncycastle.jsse.BCSSLSocket
-dontwarn org.bouncycastle.jsse.provider.BouncyCastleJsseProvider
-dontwarn org.conscrypt.Conscrypt$Version
-dontwarn org.conscrypt.Conscrypt
-dontwarn org.conscrypt.ConscryptHostnameVerifier
-dontwarn org.openjsse.javax.net.ssl.SSLParameters
-dontwarn org.openjsse.javax.net.ssl.SSLSocket
-dontwarn org.openjsse.net.ssl.OpenJSSE

# kotlinx-datetime 0.8.0 (02-04) removed the real kotlinx.datetime.Clock/Instant classes in
# favor of typealiases to kotlin.time.Clock/Instant, so their .class files no longer exist.
# io.openfeedback:openfeedback-viewmodel was compiled against kotlinx-datetime 0.6.x, where
# these were real classes, and its bytecode still references them directly (commitComment /
# timestampToInstant). Openfeedback is feature-flagged off in this app
# (OPEN_FEEDBACK_ENABLED = "false" in AppModule/BuildConfig), so these code paths are
# unreachable at runtime; R8 generated these exact three lines in missing_rules.txt.
-dontwarn kotlinx.datetime.Clock$System
-dontwarn kotlinx.datetime.Instant$Companion
-dontwarn kotlinx.datetime.Instant