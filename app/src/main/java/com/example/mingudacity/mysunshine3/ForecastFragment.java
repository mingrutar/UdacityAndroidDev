package com.example.mingudacity.mysunshine3;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.Arrays;
import java.util.List;

enum OWPUnit {          //openweathermap units
    METRIC("metric"),
    IMPERIAL("imperial");

    private String name;
    OWPUnit(String name) {this.name = name;}
    @Override
    public String toString() {return name;}
}

/**
 * A placeholder fragment containing a simple view.
 */
public class ForecastFragment extends Fragment {
    static final String LOG_TAG = ForecastFragment.class.getSimpleName();

    static final String OWMUrlBase = "http://api.openweathermap.org/data/2.5/forecast/daily";
    static final String QUERY_PARAM = "q";
    static final String FORMAT_PARAM = "mode";
    static final String UNITS_PARAM = "units";
    static final String DAYS_PARAM = "cnt";
    static final String APPID_PARAM = "appid";
    static final String myApiKey = "482a4276611839ba6baa850ddb7ec08c";

    String mforecastData;
    ArrayAdapter<String> mAdapter;
    static String[] testData = new String[] {"Today - Sunny - 88/63",
            "Tomorrow - Foggy - 78/53",
            "Wed - Cloudy - 72/65",
            "Thurs - Rainy - 68/51",
            "Fri - Foggy - 70/49",
            "Sat - Sunny - 76/68"};

    @Override
    public void onCreate(Bundle savedInstance) {
        super.onCreate(savedInstance);
        setHasOptionsMenu(true);
    }
    public ArrayAdapter<String>  getAdapter() {
        return mAdapter;
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);
        Context context = getActivity();
        // if items is passed in ctor, the Arrays.asList is stored but it is immutable, so clear() fails
        ///List<String> items = new ArrayList <String>( Arrays.asList(testData));
        List<String> items = Arrays.asList(testData);
        mAdapter = new ArrayAdapter<>(context,
                R.layout.list_item_forecast,        //file name of list view layout (textView here)
                R.id.list_item_forecast_textview);   //textView id
        mAdapter.addAll(items);
        ListView listView = (ListView) rootView.findViewById(R.id.listview_forecast);
        listView.setAdapter(mAdapter);
        return rootView;
    }
    @Override
    public void onCreateOptionsMenu (Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.forecastfragment, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }
    @Override
    public boolean onOptionsItemSelected (MenuItem item) {
        int mid = item.getItemId();
        if (mid == R.id.action_refresh) {
//            String urlString = String.format("http://api.openweathermap.org/data/2.5/forecast/daily?q=98102&mode=json&units=metric&cnt=7&appid=%s", myApiKey);
            String urlString = buildUrl("98102", myApiKey, 7, OWPUnit.IMPERIAL);
            new FetchWeatherTask(this).execute(urlString);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    /*
    // http://api.openweathermap.org/data/2.5/forecast/daily?q=98102&mode=json&units=imperial&cnt=7&appid=482a4276611839ba6baa850ddb7ec08c
      String.format("http://api.openweathermap.org/data/2.5/forecast/daily?" +
                    "q=98102&mode=json&units=metric&cnt=7&appid=%s", apiKey);
      if use apache URIBuilder:
        URI buildUri=new URIBuilder(OWMUrlBase).addParameter(FORMAT_PARAM, "json")...build();
        add to build.gradle 'compile group: "org.apache.httpcomponents' , name: 'httpclient-android' , version: '4.3.5.1'"
    */
    private String buildUrl(String zip, String apiKey, int numDays, OWPUnit unit ) {
        Uri buildUri = Uri.parse(OWMUrlBase).buildUpon()
                .appendQueryParameter(QUERY_PARAM, zip)
                .appendQueryParameter(FORMAT_PARAM, "json")
                .appendQueryParameter(UNITS_PARAM, unit.toString())
                .appendQueryParameter(DAYS_PARAM, Integer.toString(numDays))
                .appendQueryParameter(APPID_PARAM, apiKey).build();
        return buildUri.toString();
    }

}
