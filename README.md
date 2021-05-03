# android-startup-instrumentation
This is an instrumentation library for Android Startup which gives duration for each phase of App Startup.


### Gradle Setup

```gradle
repositories {
    maven { url 'https://jitpack.io' }
}

dependencies {
    implementation 'com.github.anjalsaneen:android-startup-instrumentation:1.0-alpha'
}
```

### Usage
Add AppStartUpTrace.start()(at the start of onCreate) and AppStartUpTrace.stop()(at the end of onCreate) inside Application onCreate method.

```
override fun onCreate() {
    AppStartUpTrace.start() //Should be before super.onCreate()
    
    super.onCreate()
        ...
        ...
        ...
        

    AppStartUpTrace.stop(this) { appStartUpMetrics->
        Log.v("StartUp Logs", appStartUpMetrics.toString())
    }
}
```

### Result

On AppStartUpTrace.stop() 2nd parameter it takes a lambda which returns Startup logs at the time of the first draw which has duration for each step during App StartUp.

```
AppStartUpTrace.stop(this) { appStartUpMetrics->
        Log.v("Total Time", appStartUpMetrics.totalTime.toString()) // Time Taken For Cold StartUp
        Log.v("Process Fork To CP", appStartUpMetrics.processForkToContentProvider.toString()) // Time Taken From Process start to initialising content provider
        Log.v("Content provider", appStartUpMetrics.contentProviderToAppStart.toString()) //Time Taken for initialising content providers
        Log.v("Application Create", appStartUpMetrics.contentProviderToAppStart.toString()) //Time Taken for running Application onCreate
        Log.v("First Draw", appStartUpMetrics.contentProviderToAppStart.toString()) //Time Taken from end of Application onCreate to drawing first frame
}
```

Note: it gives result only post Lollipop devices(21+)

### Acknowledgements

Thanks to [py - Pierre Yves Ricau](https://github.com/pyricau) for the detailed [article series](https://dev.to/pyricau/android-vitals-what-time-is-it-2oih) about cold startup. 

### License

TODO
