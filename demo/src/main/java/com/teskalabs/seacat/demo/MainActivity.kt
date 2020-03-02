package com.teskalabs.seacat.demo

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.teskalabs.seacat.SeaCat
import kotlinx.android.synthetic.main.activity_main.*
import java.net.URL
import java.text.SimpleDateFormat
import javax.net.ssl.HttpsURLConnection


class MainActivity : AppCompatActivity() {

    val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            when (intent.action) {
                SeaCat.ACTION_IDENTITY_ENROLLED -> update()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }


    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return super.onCreateOptionsMenu(menu)
    }

    override fun onStart() {
        super.onStart()
        update()

        val intentFilter = IntentFilter()
        intentFilter.addCategory(SeaCat.CATEGORY_SEACAT)
        intentFilter.addAction(SeaCat.ACTION_IDENTITY_ENROLLED)
        SeaCat.broadcastManager.registerReceiver(receiver, intentFilter)
    }

    override fun onStop() {
        SeaCat.broadcastManager.unregisterReceiver(receiver)
        super.onStop()
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {

            R.id.action_revoke_identity -> {
                SeaCat.identity.revoke()
                Toast.makeText(applicationContext, "Identity has been revoked!", Toast.LENGTH_LONG).show()
                startActivity(Intent(this@MainActivity, SplashActivity::class.java))
                finish()
                return true
            }

            R.id.action_renew_identity -> {
                SeaCat.identity.renew()
            }
        }

        return super.onOptionsItemSelected(item)
    }

    fun update() {
        identityTV.text = "Identity: %s".format(SeaCat.identity.toString())

        val cert= SeaCat.identity.certificate
        if (cert != null) {
            val format = SimpleDateFormat("dd.MMM yyyy HH:mm:ss")

            sequenceTV.text = "Sequence: %s".format(cert.serialNumber.toString())
            validFromTV.text = "Valid from %s".format(format.format(cert.notBefore))
            validToTV.text = "Valid to %s".format(format.format(cert.notAfter))

        } else {
            sequenceTV.text = "Sequence -"
            validFromTV.text = "Valid from -"
            validToTV.text = "Valid to -"
        }

    }

    fun onRestCallClicked(view: android.view.View) {
        Thread({
            val url = URL("https://zscanner.seacat.io/medicalc/v3.1/departments")
            val connection = url.openConnection() as HttpsURLConnection
            connection.setSSLSocketFactory(SeaCat.sslContext.socketFactory)

            val istream = connection.getInputStream()
            val bytes = istream.readBytes()

            var result = ""
            for (b in bytes) {
                result += String.format("%02X", b)
            }
            Log.i(SeaCat.TAG, "Downloaded: " + result)
        }).start()
    }

}
