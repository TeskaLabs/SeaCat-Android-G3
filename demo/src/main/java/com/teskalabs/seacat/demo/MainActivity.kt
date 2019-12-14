package com.teskalabs.seacat.demo

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity


class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

//        Thread({
//            val url = URL("https://gw01.seacat.io/")
//            val connection = url.openConnection() as HttpsURLConnection
//            connection.setSSLSocketFactory((application as KeyoteDemoApp).seacat.sslContext.socketFactory)
//
//            val istream = connection.getInputStream()
//            val x = istream.readBytes()
//            Log.i(SeaCat.TAG, ">>>" + x.toString())
//        }).start()
    }


    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {

            R.id.action_remove_identity -> {
                (application as KeyoteDemoApp).seacat.identity.remove()
                Toast.makeText(applicationContext, "Identity has been removed!", Toast.LENGTH_LONG).show()
                startActivity(Intent(this@MainActivity, SplashActivity::class.java))
                finish()
                return true
            }
        }

        return super.onOptionsItemSelected(item)
    }
}
