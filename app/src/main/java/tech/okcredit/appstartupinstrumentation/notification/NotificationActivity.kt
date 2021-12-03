package tech.okcredit.appstartupinstrumentation.notification

import android.app.Activity
import android.os.Bundle
import tech.okcredit.appstartupinstrumentation.R

class NotificationActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }
}
