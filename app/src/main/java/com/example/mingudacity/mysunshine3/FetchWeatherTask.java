package com.example.mingudacity.mysunshine3;

import android.os.AsyncTask;
import android.support.annotation.Nullable;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;

/**
 * Created by linna on 4/7/2016.
 */
class FetchWeatherTask extends AsyncTask<String, Void, String> {

    private ForecastFragment mForecastFragment;
    private final String LOG_TAG = FetchWeatherTask.class.getSimpleName();

    public FetchWeatherTask(ForecastFragment forecastFragment) {
        mForecastFragment = forecastFragment;
    }

    // http://api.openweathermap.org/data/2.5/forecast/daily?q=98102&mode=json&units=imperial&cnt=7&appid=482a4276611839ba6baa850ddb7ec08c
    @Nullable
    private String getWeatherForecast(String urlString) {
        Log.v(LOG_TAG, "urlString=" + urlString);
        // These two need to be declared outside the try/catch
        // so that they can be closed in the finally block.
        HttpURLConnection httpConnection = null;    // android API vs HTTPClient
        BufferedReader reader = null;
        // Will contain the raw JSON response as a string.
        String forecastJsonStr = null;

        try {
            // Construct the URL for the OpenWeatherMap query
            // Possible parameters are avaiable at OWM's forecast API page, at
            // http://openweathermap.org/API#forecast

            // Create the request to OpenWeatherMap, and open the connection
            httpConnection = (HttpURLConnection) new URL(urlString).openConnection();
            httpConnection.setRequestMethod("GET");
            httpConnection.connect();

            // Read the input stream into a String
            InputStream inputStream = httpConnection.getInputStream();
            if (inputStream != null) {
                StringBuilder buffer = new StringBuilder();
                reader = new BufferedReader(new InputStreamReader(inputStream));
                String line;
                while ((line = reader.readLine()) != null) {
                    // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                    // But it does make debugging a *lot* easier if you print out the completed
                    // buffer for debugging.
                    buffer.append(line).append("\n");
                }
                if (buffer.length() != 0) {
                    forecastJsonStr = buffer.toString();
                }
            }
        } catch (IOException e) {
            Log.e(LOG_TAG, "Error ", e);
            // If the code didn't successfully get the weather data, there's no point in attemping
            // to parse it.
        } finally {
            if (httpConnection != null) {
                httpConnection.disconnect();
            }
            if (reader != null) {
                try {
                    reader.close();
                } catch (final IOException e) {
                    Log.e("PlaceholderFragment", "Error closing stream", e);
                }
            }
        }
        return forecastJsonStr;
    }

    private String[] getWeatherDataFronJson(String forecastJsonStr, int daynum)
        throws JSONException {

        final String TAG_LIST = "list";
        final String TAG_TEMP = "temp";
        final String TAG_MAX = "max";
        final String TAG_MIN = "min";
        final String TAG_WEATHER = "weather";
        final String TAG_MAIN = "main";
        final String DATE_FORMAT = "EEE, MMM d";
        final String RET_FORMAT = "%s - %s - %.2f/%.2f";

        Log.v(LOG_TAG, forecastJsonStr);
        JSONArray jarray = new JSONObject(forecastJsonStr).getJSONArray(TAG_LIST);
        String[] ret = new String[jarray.length()];
        String main;
        SimpleDateFormat dateFormater = new SimpleDateFormat(DATE_FORMAT, Locale.getDefault());
        GregorianCalendar gcday = new GregorianCalendar(Locale.getDefault());
        gcday.setGregorianChange(new Date(Long.MAX_VALUE));
        for (int i = 0; i < jarray.length(); i++) {
            JSONObject dayObj = jarray.getJSONObject(i);
            JSONObject temperature = dayObj.getJSONObject(TAG_TEMP);
            double maxTemp = temperature.getDouble(TAG_MAX);
            double minTemp = temperature.getDouble(TAG_MIN);
            Log.v(LOG_TAG, "max="+maxTemp+",min="+minTemp);
            JSONArray jweather = dayObj.getJSONArray(TAG_WEATHER);
            JSONObject jobj0 = jweather.getJSONObject(0);
            main = jobj0.getString(TAG_MAIN);
            String dateStr = dateFormater.format(gcday.getTime());
            ret[i] = String.format(RET_FORMAT, dateStr, main, maxTemp, minTemp);
            gcday.add(Calendar.DAY_OF_MONTH, 1);
        }
        return ret;
     }
    @Override
    protected String doInBackground(String... params) {
        String paramInfo = String.format("#params=%d, [0]=$s", params.length, params[0]);
        Log.v(LOG_TAG, paramInfo);
        return getWeatherForecast(params[0]);
    }

    @Override
    protected void onPostExecute(String result) {
        try {
//            mForecastFragment.mforecastData = result;
            String[] results = getWeatherDataFronJson(result, 7);
            for (String str : results) {
                Log.v(LOG_TAG, str);
            }
        } catch (JSONException je) {
            Log.e(LOG_TAG, "Error", je);
        }
    }
}
