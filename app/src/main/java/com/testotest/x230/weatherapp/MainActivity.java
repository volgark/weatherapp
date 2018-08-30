package com.testotest.x230.weatherapp;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.awt.font.TextAttribute;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.text.DecimalFormat;

public class MainActivity extends AppCompatActivity {

    private TextView tvLocation, tvTemperature, tvHumidity, tvWindSpeed, tvCloudiness;
    private Button btnRefresh;
    private ImageView ivIcon;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tvLocation = findViewById(R.id.location);
        tvTemperature = findViewById(R.id.temperature);
        tvHumidity = findViewById(R.id.humidity);
        tvWindSpeed = findViewById(R.id.wind_speed);
        tvCloudiness = findViewById(R.id.cloudiness);
        btnRefresh = findViewById(R.id.button_refresh);
        ivIcon = findViewById(R.id.icon);

        btnRefresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new WeatherDataRetrieval().execute();
            }
        });
    }

    private int getIcon(int code) {

        switch (code) {
            case 200:
            case 201:
            case 202:
            case 210:
            case 211:
            case 212:
            case 221:
            case 230:
            case 231:
            case 232:
                return R.drawable.ic_thunderstorm_large;
            case 300:
            case 301:
            case 302:
            case 310:
            case 311:
            case 312:
            case 313:
            case 314:
            case 321:
                return R.drawable.ic_drizzle_large;
            case 500:
            case 501:
            case 502:
            case 503:
            case 504:
            case 511:
            case 520:
            case 521:
            case 522:
            case 531:
                return R.drawable.ic_rain_large;
            case 600:
            case 601:
            case 602:
            case 611:
            case 612:
            case 615:
            case 616:
            case 620:
            case 621:
            case 622:
                return R.drawable.ic_snow_large;
            case 800:
                return R.drawable.ic_day_clear_large;
            case 801:
                return R.drawable.ic_day_few_clouds_large;
            case 802:
                return R.drawable.ic_scattered_clouds_large;
            case 803:
            case 804:
                return R.drawable.ic_broken_clouds_large;
            case 701:
            case 711:
            case 721:
            case 731:
            case 741:
            case 751:
            case 761:
            case 762:
                return R.drawable.ic_fog_large;
            case 781:
            case 900:
                return R.drawable.ic_tornado_large;
            case 905:
                return R.drawable.ic_windy_large;
            case 906:
                return R.drawable.ic_hail_large;
        }
        return code;
    }

    private class WeatherDataRetrieval extends AsyncTask<Void, Void, String> {
        private static final String WEATHER_SOURCE =
                "http://api.openweathermap.org/data/2.5/weather?APPID=82445b6c96b99bc3ffb78a4c0e17fca5&mode=json&id=1733047";

        /* invokes on UI thread befire the task is executed */
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        /* worker thread will pass information to ui thread below
         * cannot update UI thread or anything */
        @Override
        protected String doInBackground(Void... arg0) {

            NetworkInfo networkInfo = ((ConnectivityManager) MainActivity.this.getSystemService(Context.CONNECTIVITY_SERVICE)).getActiveNetworkInfo();

            if (networkInfo != null && networkInfo.isConnected()) {
                /* network-connected */

                try {
                    URL url = new URL(WEATHER_SOURCE);
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("GET");
                    conn.setConnectTimeout(15000);
                    conn.setReadTimeout(15000);
                    conn.connect();

                    int responseCode = conn.getResponseCode();
                    if (responseCode == HttpURLConnection.HTTP_OK) {
                        BufferedReader bufferedReader = new BufferedReader(
                                new InputStreamReader(conn.getInputStream()));
                        if (bufferedReader != null) {
                            String readLine;
                            StringBuffer stringBuffer = new StringBuffer();
                            while ((readLine = bufferedReader.readLine()) != null) {
                                stringBuffer.append(readLine);
                            }
                            return stringBuffer.toString();
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                /* no connection */
                return null;
            }
            return null;
        }

        /* ui thread */
        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            DecimalFormat df = new DecimalFormat(("#.00"));

            try {
                if (result != null) {
                    final JSONObject weatherJSON = new JSONObject(result);
                    tvLocation.setText(weatherJSON.getString("name") + "," + weatherJSON.getJSONObject("sys").getString("country"));
                    tvWindSpeed.setText(String.valueOf(weatherJSON.getJSONObject("wind").getDouble("speed")) + " mps");
                    tvCloudiness.setText(String.valueOf(weatherJSON.getJSONObject("clouds").getInt("all")) + " %");

                    final JSONObject mainJSON = weatherJSON.getJSONObject("main");
                    tvTemperature.setText(String.valueOf(df.format(mainJSON.getDouble("temp") - 273.15)));
                    tvHumidity.setText(String.valueOf(mainJSON.getInt("humidity")) + " %");

                    final JSONArray weatherJSONArray = weatherJSON.getJSONArray("weather");
                    try {
                        if (weatherJSONArray.length() > 0) {
                            int code = weatherJSONArray.getJSONObject(0).getInt("id");
                            ivIcon.setImageResource(getIcon(code));
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
}