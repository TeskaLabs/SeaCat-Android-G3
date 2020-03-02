package com.teskalabs.seacat.demojava;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.teskalabs.seacat.SeaCat;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.cert.X509Certificate;
import java.text.SimpleDateFormat;

import javax.net.ssl.HttpsURLConnection;

public class MainActivity extends AppCompatActivity {

    TextView identityTV;
    TextView sequenceTV;
    TextView validFromTV;
    TextView validToTV;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        identityTV = findViewById(R.id.identityTV);
        sequenceTV = findViewById(R.id.sequenceTV);
        validFromTV = findViewById(R.id.validFromTV);
        validToTV = findViewById(R.id.validToTV);
    }

    @Override
    protected void onStart() {
        super.onStart();
        update();


    }

    private void update() {
        SeaCat seacat = SeaCat.getInstance();

        identityTV.setText(String.format("Identity: %s", seacat.getIdentity().toString()));

        X509Certificate cert = seacat.getIdentity().getCertificate();

        if (cert != null) {
            SimpleDateFormat format = new SimpleDateFormat("dd.MMM yyyy HH:mm:ss");

            sequenceTV.setText("Sequence: %s".format(cert.getSerialNumber().toString()));
            validFromTV.setText("Valid from %s".format(format.format(cert.getNotBefore())));
            validToTV.setText("Valid to %s".format(format.format(cert.getNotAfter())));

        } else {
            sequenceTV.setText("Sequence -");
            validFromTV.setText("Valid from -");
            validToTV.setText("Valid to -");
        }

    }

    public void onRestCallClicked(View view) {
        final SeaCat seacat = SeaCat.getInstance();

        Thread thread = new Thread() {

            @Override
            public void run() {
                try {
                    URL url = new URL("https://zscanner.seacat.io/medicalc/v3.1/departments");

                    HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();

                    // Configure the HTTPS connection to use SeaCat SSL context
                    connection.setSSLSocketFactory(seacat.getSslContext().getSocketFactory());

                    ByteArrayOutputStream buffer = new ByteArrayOutputStream();
                    InputStream is = connection.getInputStream();
                    int nRead;
                    byte[] data = new byte[1024];
                    while ((nRead = is.read(data, 0, data.length)) != -1) {
                        buffer.write(data, 0, nRead);
                    }

                    buffer.flush();
                    byte[] bytes = buffer.toByteArray();

                    StringBuilder result = new StringBuilder();
                    for (byte b : bytes) {
                        result.append(String.format("%02X", b));
                    }
                    Log.i("MainActivity", "Downloaded: " + result);


                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
        thread.start();

    }
}
