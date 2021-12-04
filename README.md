# android-app-launch-tracking
This is an instrumentation library for tracking the launch response time of the android app. Launch Response Time is the time from when the system triggers App Launch to when the display has rendered the first frame of the window of the activity brought to the foreground.

### Gradle Setup

```gradle
repositories {
    maven { url 'https://jitpack.io' }
}

dependencies {
    implementation 'com.github.okcredit:android-cold-startup-instrumentation:2.0-alpha'
}
```

### Usage
Add `AppStartUpTracer.start()` (at the start of onCreate) and `AppStartUpTracer.onAppLaunchListener()` (at the end of onCreate) inside Application onCreate method.

```
override fun onCreate() {
    AppStartUpTracer.start() //Should be before super.onCreate()
    
    super.onCreate()
        ...
        ...
        ...
        

    AppStartUpTracer.onAppLaunchListener(this) { appStartUpMetrics->
        Log.v("<<<<App Launched", appStartUpMetrics.toString())
    }
}
```

### Result

On AppStartUpTracer.onAppLaunchListener() 2nd parameter takes a lambda which returns information regarding app launch. AppLaunchMetrics is a sealed class contain information regarding app launch. It can be either ColdStartUp or WarmAndHotStartup.

| ColdStartUpData  | Details       |
| -------------    | ------------- |
| startUpMetrics   | It Contains cold startup duration from Process start to the first draw and the time difference between multiple splits mentioned in [Phases of App Cold StartUp]() |
| appUpdateData     | It Contains information regarding app updates like app starts after the first install, an update, first install after clearing data or a crash. it also tracks reason for last app exit, first Install time, last updated time, last cold startup time, version details of all installed versions  |
| firstActivityName     | Name of first activity  |
| firstActivityReferrer     | Information about who launched the first activity. See details [here](https://developer.android.com/reference/android/app/Activity#getReferrer())  |
| firstActivityIntent     | Intent of first activity. we are exposing intent for tracking notification details on app launch |

<br/>
<br/>

| WarmAndHotStartUpData  | Details       |
| -------------    | ------------- |
| warmAndHotStartUpMetrics   | It Contains hot and warm startup metrics from activity resume/create to draw.  |
| activityState     | State of activity when user returns back to App.<br/> <br/> CREATED_NO_STATE(Warm Launch) - The activity was created with no state bundle and then resumed  <br/> CREATED_WITH_STATE(Warm Launch) - The activity was created with a state bundle and then resumed <br/> STARTED(Hot Launch) - The activity already created. it was started and then resumed when user launch the app <br/> RESUMED(Hot Launch) - The activity already created and started. it was then just resumed when user launch the app |
| importance     | The relative importance level that the system places on this process. See details [here](https://developer.android.com/reference/android/app/ActivityManager.RunningAppProcessInfo#importance)  |
| durationFromLastAppStop     | Duration from last app stop to launch  |
| resumeActivityName     | Name of launch activity |
| resumeActivityReferrer     | Information about who launched the first activity. See details [here] |
| resumeActivityIntent     | Intent of resumed activity |



Note: it gives result only post Lollipop devices(21+)

### App Cold Startup

A Cold Launch is what happens when the App Launch requires a Process Start. This library is tracking process start to first draw and duration between below phases

- **Process Fork to Content Provider** : Time Duration between App process forked from Zygote and First Initialization of content provider. Creating the app object and Launching the main thread will be happening here. developers have little influence on the improvement here.

- **Content Provider to App OnCreate()** : Time Duration between First Initialization of the content provider to Start of App.OnCreate(). it includes All time taken for Content providers in the app.

- **App OnCreate() Start to App OnCreate() End** : Time Duration between Start of App.OnCreate() to End of App.OnCreate(). it includes time taken for App.OnCreate()

- **App OnCreate() End to First Draw of the frame** : Time Duration between End of App.OnCreate() to First Draw of the frame. it includes time taken for Initial activity initialisation, inflating the first layout, onMeasure() and onDraw() of for initial layout.


<img width="1331" alt="Screenshot 2021-07-16 at 4 38 24 PM" src="https://user-images.githubusercontent.com/43947967/125938714-483d0f14-96be-4c3d-944f-941130912626.png">

### App Hot and Warm Startup

A Hot Launch is what happens when the process was alive and the activity that is being resumed needs to first be started, i.e. was previously stopped but not destroyed. A Warm Launch is what happens when the process was alive and the activity that is being resumed needs to first be created, i.e. it was previously destroyed or never created.


### Acknowledgements

- Thanks to [py - Pierre Yves Ricau](https://github.com/pyricau) for this detailed [article series](https://dev.to/pyricau/android-vitals-what-time-is-it-2oih) about cold startup.

- Thanks to [Square Tart](https://github.com/square/tart). it helps to get code snippet for this library.

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
