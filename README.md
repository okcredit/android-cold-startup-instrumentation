# android-cold-startup-instrumentation
This is an instrumentation library for Android Cold Startup which gives duration for each [phase](#phases-of-app-startup) of App Startup.


### Gradle Setup

```gradle
repositories {
    maven { url 'https://jitpack.io' }
}

dependencies {
    implementation 'com.github.okcredit:android-cold-startup-instrumentation:1.0'
}
```

### Usage
Add `AppStartUpTracer.start()` (at the start of onCreate) and `AppStartUpTracer.stop()` (at the end of onCreate) inside Application onCreate method.

```
override fun onCreate() {
    AppStartUpTracer.start() //Should be before super.onCreate()
    
    super.onCreate()
        ...
        ...
        ...
        

    AppStartUpTracer.stop(this) { appStartUpMetrics->
        Log.v("StartUp Logs", appStartUpMetrics.toString())
    }
}
```

### Result

On AppStartUpTracer.stop() 2nd parameter takes a lambda which returns Startup logs at the time of the first draw which has duration for each phase during App StartUp.

```
AppStartUpTracer.stop(this) { appStartUpMetrics->
        Log.v("Total Time", appStartUpMetrics.totalTime.toString()) // Time Taken For Cold StartUp
        Log.v("Process Fork To CP", appStartUpMetrics.processForkToContentProvider.toString()) // Time Taken From Process start to initialising content provider
        Log.v("Content provider", appStartUpMetrics.contentProviderToAppStart.toString()) //Time Taken for initialising content providers
        Log.v("Application Create", appStartUpMetrics.applicationOnCreateTime.toString()) //Time Taken for running Application onCreate
        Log.v("First Draw", appStartUpMetrics.appOnCreateEndToFirstDraw.toString()) //Time Taken from end of Application onCreate to drawing first frame
}
```

Note: it gives result only post Lollipop devices(21+)

### Phases of App Startup

- **Process Fork to Content Provider** : Time Duration between App process forked from Zygote and First Initialization of content provider. Creating the app object and Launching the main thread will be happening here. developers have little influence on the improvement here.

- **Content Provider to App OnCreate()** : Time Duration between First Initialization of the content provider to Start of App.OnCreate(). it includes All time taken for Content providers in the app.

- **App OnCreate() Start to App OnCreate() End** : Time Duration between Start of App.OnCreate() to End of App.OnCreate(). it includes time taken for App.OnCreate()

- **App OnCreate() End to First Draw of the frame** : Time Duration between End of App.OnCreate() to First Draw of the frame. it includes time taken for Initial activity initialisation, inflating the first layout, onMeasure() and onDraw() of for initial layout.


<img width="1331" alt="Screenshot 2021-07-16 at 4 38 24 PM" src="https://user-images.githubusercontent.com/43947967/125938714-483d0f14-96be-4c3d-944f-941130912626.png">


### Acknowledgements

Thanks to [py - Pierre Yves Ricau](https://github.com/pyricau) for this detailed [article series](https://dev.to/pyricau/android-vitals-what-time-is-it-2oih) about cold startup. it helps to get code snippet for this library.

### License

    Copyright 2021 OkCredit.

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
