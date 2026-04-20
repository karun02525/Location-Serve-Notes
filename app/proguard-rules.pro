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
-dontwarn com.amazonaws.mobile.auth.facebook.FacebookButton
-dontwarn com.amazonaws.mobile.auth.facebook.FacebookSignInProvider
-dontwarn com.amazonaws.mobile.auth.google.GoogleButton
-dontwarn com.amazonaws.mobile.auth.google.GoogleSignInProvider
-dontwarn com.amazonaws.mobile.auth.ui.AuthUIConfiguration$Builder
-dontwarn com.amazonaws.mobile.auth.ui.AuthUIConfiguration
-dontwarn com.amazonaws.mobile.auth.ui.SignInUI$LoginBuilder
-dontwarn com.amazonaws.mobile.auth.ui.SignInUI
-dontwarn com.amazonaws.mobile.auth.userpools.CognitoUserPoolsSignInProvider
-dontwarn com.amazonaws.mobileconnectors.cognitoauth.Auth$Builder
-dontwarn com.amazonaws.mobileconnectors.cognitoauth.Auth
-dontwarn com.amazonaws.mobileconnectors.cognitoauth.AuthUserSession
-dontwarn com.amazonaws.mobileconnectors.cognitoauth.activities.CustomTabsManagerActivity
-dontwarn com.amazonaws.mobileconnectors.cognitoauth.exceptions.AuthClientException
-dontwarn com.amazonaws.mobileconnectors.cognitoauth.exceptions.AuthServiceException
-dontwarn com.amazonaws.mobileconnectors.cognitoauth.handlers.AuthHandler
-dontwarn com.amazonaws.mobileconnectors.cognitoauth.tokens.AccessToken
-dontwarn com.amazonaws.mobileconnectors.cognitoauth.tokens.IdToken
-dontwarn com.amazonaws.mobileconnectors.cognitoauth.tokens.RefreshToken
-dontwarn com.amazonaws.mobileconnectors.cognitoauth.util.Pkce