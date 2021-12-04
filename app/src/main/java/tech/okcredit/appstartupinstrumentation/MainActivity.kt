package tech.okcredit.appstartupinstrumentation

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        findViewById<TextView>(R.id.hello_world).text = this.hashCode().toString()

        findViewById<View>(R.id.hello_world).setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
        }
    }
}
