package com.teskalabs.seacat.demo

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_splash.*


class SplashActivity : AppCompatActivity() {

    val handler = Handler()
    lateinit var timerRunnable: Runnable

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        val identity = (application as KeyoteDemoApp).seacat.identity
        timerRunnable = Runnable {
            if (identity.certificate == null) {
                handler.postDelayed(timerRunnable, 500)
            } else {
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            }
        }

        enrollButton.setOnClickListener { comenceEnroll() }
    }

    override fun onStart() {
        super.onStart()
        handler.post(timerRunnable)
    }


    fun comenceEnroll() {
        val attributes = mapOf(
            "aaa" to "bbb",
            "ccc" to "ddd"
        )
        (application as KeyoteDemoApp).seacat.identity.enroll(attributes)
    }

    override fun onBackPressed() {
        // Disable back button
    }
}
