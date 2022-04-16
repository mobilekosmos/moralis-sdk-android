[![CI](https://github.com/mobilekosmos/moralis-sdk-android/actions/workflows/main.yml/badge.svg?branch=master)](https://github.com/mobilekosmos/moralis-sdk-android/actions/workflows/main.yml)
[![Known Vulnerabilities](https://snyk.io/test/github/mobilekosmos/moralis-sdk-android/badge.svg)](https://snyk.io/test/github/mobilekosmos/moralis-sdk-android)
[![codecov](https://codecov.io/gh/mobilekosmos/moralis-sdk-android/branch/master/graph/badge.svg)](https://codecov.io/gh/mobilekosmos/moralis-sdk-android)
[![android min api](https://img.shields.io/badge/Android_API->=28-66c718.svg)](https://github.com/parse-community/parse-dashboard/releases)
[![](https://jitpack.io/v/mobilekosmos/moralis-sdk-android.svg)](https://jitpack.io/#mobilekosmos/moralis-sdk-android)
[![](https://jitpack.io/v/mobilekosmos/moralis-sdk-android/month.svg)](https://jitpack.io/#mobilekosmos/moralis-sdk-android)

<br>
<p>
    <a href="https://moralis.io">
    <img width="132" height="101" src="https://moralis.io/wp-content/uploads/2021/01/logo.png" class="attachment-full size-full" alt="Moralis Build Serverless web3 apps" loading="lazy" /></a>
</p>

<h2>Moralis Kotlin SDK for Android (Unofficial)</h2>

<p>
    An unofficial library that gives you access to the powerful Moralis Server backend from your native Android app.
</p>

For more information on Moralis and its features, see [the website](https://moralis.io) or [the documentation](https://docs.moralis.io).

**This library is not related to the Moralis team and thus not maintained by them.**

## Motivation
Kotlin is the Android native language by excellence and the only one that allows to achieve the best app quality possible, nor Flutter, Reactive Native, Ionic, etc. can provide comparable performance, memory and space consumption, availability of native APIs and overall stability. Furthermore Kotlin will provide cross plattform capabilities in the near future, dropping the need of JS for web, thus the odds of Kotlin being the language by excellence for native cross plattform development are very good.

## Getting Started

1. Add the SDK:
The easiest way to integrate the Moralis SDK into your Android project is through adding the dependency to your buid.gradle file:
      ```implementation 'com.github.mobilekosmos:moralis-sdk-android:<Tag>'```
where '< Tag >' is the latest released version on Github, please check.
You can also use "master-SNAPSHOT" but this is not recommended, as the master is not approved for production.

2. Initialize the SDK:
The recommended place to initialize the SDK is in your application class to make sure the SDK is always started when the app starts. For this extend your application class from "MoralisApplication" and Call ```Moralis.start(APP_ID, SERVER_URL, applicationContext)```, like this:
```
    const val APP_ID = "xyz"
    const val SERVER_URL = "https://asdcyx.grandmoralis.com:2053/server"
    class App: MoralisApplication() {
        override fun onCreate() {
            super.onCreate()
            Moralis.start(APP_ID, SERVER_URL)
        }
    }
 ``` 
    
3. Attach to the app's lifecycle:
Call ```Moralis.onStart(this)``` and ```Moralis.onDestroy(this)``` in the respectives lifecycle callbacks.

4. Attach to the Authentication events:
Extend your authenticating Activity from Moralis.MoralisAuthenticationCallback

5. Done, now you can call Moralis.authenticate() as per Moralis Docs or any Moralis function you want. Check the MainActivity class in the Sample to see a working example.

Note: the library automatically adds a line to the app's manifest:
```android:networkSecurityConfig="@xml/network_config"```
If your app is already setting this you must manually add the content of network_config.xml to your file. This is currently needed by the WalletConnect integration, but hopefully we can get rid of it in the final version.


## Disclaimer
This is a proof of concept, not ready for production yet, work in progress.

⭐️ Star me: if you want this project to progress please star it, every star counts!

Find the [TODO list here](https://github.com/mobilekosmos/moralis-sdk-android/issues/1).

## Sample
The sample includes a first draft of a working app that uses the native SDK showcasing a few functions.

Sample videos:

[<img src="https://img.youtube.com/vi/QYApykZJjko/maxresdefault.jpg" width="50%">](https://youtu.be/QYApykZJjko)
[<img src="https://img.youtube.com/vi/UXMPpFu81Zc/maxresdefault.jpg" width="50%">](https://youtu.be/UXMPpFu81Zc)
[<img src="https://img.youtube.com/vi/4VDuw0DGszQ/maxresdefault.jpg" width="50%">](https://youtu.be/4VDuw0DGszQ)
