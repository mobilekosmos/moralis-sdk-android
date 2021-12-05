<p align="center">
    <a href="https://moralis.io">
    <img width="132" height="101" src="https://moralis.io/wp-content/uploads/2021/01/logo.png" class="attachment-full size-full" alt="Moralis Build Serverless web3 apps" loading="lazy" /></a>
</p>

<h2 align="center">Moralis SDK for Android (Unofficial)</h2>

<p align="center">
    An unofficial library that gives you access to the powerful Moralis Server backend from your native Android app. <a href="https://admin.moralis.io">Create Server Here</a>
</p>

<br>

For more information on Moralis and its features, see [the website](https://moralis.io), [the JavaScript guide](https://docs.moralis.io), [the Cloud Code guide](https://docs.moralis.io/moralis-server/cloud-code) or [Web3 Reference](https://docs.moralis.io/moralis-server/web3-sdk/intro).

**This library is not related to the Moralis team and thus not maintained by them.**

## Getting Started

1. The easiest way to integrate the Moralis SDK into your Android project is through adding the dependency to your buid.gradle file:
      ```implementation 'com.github.mobilekosmos:moralis-sdk-android:<Tag>'```
where '< Tag >' is the latest released version on Github, please check.
You can also use "master-SNAPSHOT" but this is not recommended, as the master is not approved for production.

2. Add following line to your manifest.xml in the application tag:
```android:networkSecurityConfig="@xml/network_config"```
This is currently needed by the WalletConnect integration, but hopefully we can get rid of it in the final version.
    
3. Extend your application class from "MoralisApplication" and Call ```Moralis.start(APP_ID, SERVER_URL, applicationContext)```, like this:
```
    const val APP_ID = "xyz"
    const val SERVER_URL = "https://asdcyx.grandmoralis.com:2053/server"
    class App: MoralisApplication() {
        override fun onCreate() {
            super.onCreate()
            Moralis.start(APP_ID, SERVER_URL, this)
        }
    }
 ``` 
This is needed because the Moralis SDK must be aware of Android's activity lifecycle to be able to work.
    
4. Extend your authenticating Activity from Moralis.MoralisAuthenticationCallback
5. Call ```Moralis.onStart(this)``` and ```Moralis.onDestroy(this)``` in the respectives lifecycle callbacks.
    
Check the MainActivity class in the Sample to see a working example.

## Disclaimer
This is a proof of concept, not ready for production yet, work in progress.

⭐️ Star me: if you want this project to progress please star it, every star counts!

Find the [TODO list here](https://github.com/mobilekosmos/moralis-sdk-android/issues/1).

## Sample
The sample includes a first draft of a working app that uses the native SDK showcasing a few functions.

[<img src="https://img.youtube.com/vi/QYApykZJjko/maxresdefault.jpg" width="50%">](https://youtu.be/QYApykZJjko)
[<img src="https://img.youtube.com/vi/UXMPpFu81Zc/maxresdefault.jpg" width="50%">](https://youtu.be/UXMPpFu81Zc)
[<img src="https://img.youtube.com/vi/4VDuw0DGszQ/maxresdefault.jpg" width="50%">](https://youtu.be/4VDuw0DGszQ)
