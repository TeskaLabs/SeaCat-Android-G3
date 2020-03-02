package com.teskalabs.seacat.demo

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import androidx.appcompat.app.AppCompatActivity
import com.teskalabs.seacat.SeaCat


class SplashActivity : AppCompatActivity() {

    val handler = Handler()
    lateinit var timerRunnable: Runnable

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        timerRunnable = Runnable {
            if (SeaCat.ready) {
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            } else {
                handler.postDelayed(timerRunnable, 500)
            }
        }

    }

    override fun onStart() {
        super.onStart()
        handler.post(timerRunnable)
    }


    override fun onBackPressed() {
        // Disable back button
    }
}
