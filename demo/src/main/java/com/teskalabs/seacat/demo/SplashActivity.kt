package com.teskalabs.seacat.demo

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import androidx.appcompat.app.AppCompatActivity
import com.teskalabs.seacat.SeaCat
import kotlinx.android.synthetic.main.activity_splash.*


class SplashActivity : AppCompatActivity() {

    val handler = Handler()
    lateinit var timerRunnable: Runnable

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        timerRunnable = Runnable {
            if (SeaCat.identity.certificate == null) {
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
            "ccc" to "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Vivamus mollis consequat pulvinar." // Long item in the CR attributes
        )
        SeaCat.identity.enroll(attributes)
    }

    override fun onBackPressed() {
        // Disable back button
    }
}
