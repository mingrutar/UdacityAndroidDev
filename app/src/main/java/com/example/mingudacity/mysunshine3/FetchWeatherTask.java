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
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;

/**
 * Created by linna on 4/7/2016.
 */
class FetchWeatherTask extends AsyncTask<String, Void, String[]> {
    private ForecastFragment mForecastFragment;
    private final String LOG_TAG = FetchWeatherTask.class.getSimpleName();
    // string for rsponse json
    static final String TAG_LIST = "list";
    static final String TAG_TEMP = "temp";
    static final String TAG_MAX = "max";
    static final String TAG_MIN = "min";
    static final String TAG_WEATHER = "weather";
    static final String TAG_MAIN = "main";
    // data formatter
    static final String DATE_FORMAT = "EEE, MMM d";
    static final String RET_FORMAT = "%s - %s - %s";

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
//        Log.v(LOG_TAG, forecastJsonStr);
        JSONArray jarray = new JSONObject(forecastJsonStr).getJSONArray(TAG_LIST);
        String[] ret = new String[jarray.length()];
        SimpleDateFormat dateFormater = new SimpleDateFormat(DATE_FORMAT, Locale.getDefault());
        String unitType = mForecastFragment.getPrefUnitType();
        GregorianCalendar gcday = new GregorianCalendar(Locale.getDefault());
        gcday.setGregorianChange(new Date(Long.MAX_VALUE));
        for (int i = 0; i < jarray.length(); i++) {
            JSONObject dayObj = jarray.getJSONObject(i);
            JSONObject temperature = dayObj.getJSONObject(TAG_TEMP);
            double highTemp = temperature.getDouble(TAG_MAX);
            double lowTemp = temperature.getDouble(TAG_MIN);
            JSONArray jweather = dayObj.getJSONArray(TAG_WEATHER);
            JSONObject jobj0 = jweather.getJSONObject(0);
            String main = jobj0.getString(TAG_MAIN);
            String dateStr = dateFormater.format(gcday.getTime());
            String highLow = formatHighLows(highTemp, lowTemp, unitType);
            ret[i] = String.format(RET_FORMAT, dateStr, main, highLow);
            gcday.add(Calendar.DAY_OF_MONTH, 1);
        }
        return ret;
    }

    public String formatHighLows(double max, double min, String unitType) {
        if (unitType == mForecastFragment.getString(R.string.pref_unit_imperial)) {
            max = (max * 1.8) + 32;
            min = (min * 1.8) + 32;
        } else if (unitType != mForecastFragment.getString(R.string.pref_unit_metric)) {
            String message = "unknown unit" + unitType;
            Log.d(LOG_TAG, message);
            return message;
        }
        return String.format("%d/%d", Math.round(max), Math.round(min));
    }

    @Override
    protected String[] doInBackground(String... params) {
        String[] results = null;
        try {
            String ret = getWeatherForecast(params[0]);
            results  = getWeatherDataFronJson(ret, 7);
        } catch (JSONException je) {
            Log.e(LOG_TAG, "Error", je);
        }
        return results;
    }

    @Override
    protected void onPostExecute(String[] result) {
        if (result != null) {
            List<String> newData = Arrays.asList(result);
            mForecastFragment.updateAdapter(newData);
        }
    }
}
