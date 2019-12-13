package com.teskalabs.seacat.demo

import android.os.Bundle
import android.os.Handler
import androidx.appcompat.app.AppCompatActivity
import android.content.Intent
import kotlinx.android.synthetic.main.activity_splash.*


class SplashActivity : AppCompatActivity() {

    val handler = Handler()
    lateinit var stateChecker:Runnable


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        stateChecker = object : Runnable {
            override fun run() {
                if (!isEnrolled()) handler.postDelayed(this, 500)
            }
        }

        enrollButton.setOnClickListener { comenceEnroll() }
    }

    override fun onStart() {
        super.onStart()

        stateChecker.run();
    }

    override fun onStop() {
        handler.removeCallbacks(stateChecker);

        super.onStop()
    }


    private fun isEnrolled() : Boolean {
        if ((application as KeyoteDemoApp).seacat.identity.certificate == null) return false;

        startActivity(Intent(this@SplashActivity, MainActivity::class.java))
        finish()
        return true
    }


    fun comenceEnroll() {
        val attributes = mapOf(
            "aaa" to "bbb",
            "ccc" to "ddd"
        )
        (application as KeyoteDemoApp).seacat.identity.enroll(attributes)
    }

}
