package com.teskalabs.seacat.demo

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.teskalabs.seacat.SeaCat

import java.net.URL
import javax.net.ssl.HttpsURLConnection


class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        Thread({
            val url = URL("https://gw01.seacat.io/")
            val connection = url.openConnection() as HttpsURLConnection
            connection.setSSLSocketFactory((application as KeyoteDemoApp).seacat.sslContext.socketFactory)

            val istream = connection.getInputStream()
            val x = istream.readBytes()
            Log.i(SeaCat.TAG, ">>>" + x.toString())
        }).start()
    }
}
