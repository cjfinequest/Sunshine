package com.example.cjfin.sunshine.app;

import android.os.AsyncTask;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.text.format.Time;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * A placeholder fragment containing a simple view.
 */
public class ForecastFragment extends Fragment {
    private ArrayAdapter<String> Bill;
    private final String LOG_TAG = getClass().getSimpleName();

    public ForecastFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.forecastfragment, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_refresh) {
            FetchWeatherTask weatherTask = new FetchWeatherTask();
            weatherTask.execute("11950");
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {


        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        ArrayList<String> weatherlist = new ArrayList<String>();
        weatherlist.add("Today the weather is mild with a high of 65");
        weatherlist.add("Tomorrow cold high 30/low 20");
        weatherlist.add("Tuesday rainy high 50/low 40");
        weatherlist.add("Wednesday humid high 70/low 60");
        weatherlist.add("Thursday foggy high 83/low 70");
        weatherlist.add("Friday sunny high 80/low 70");
        weatherlist.add("Saturday clear skies high 63/low 60");

        Bill = new ArrayAdapter<String>(getActivity(),
                R.layout.list_item_forecast,
                R.id.list_item_forecast_textview, weatherlist);

        ListView listView = (ListView) rootView.findViewById(R.id.ListView_forecast);
        listView.setAdapter(Bill);
        return rootView;
    }

    class FetchWeatherTask extends AsyncTask<String, Void, List<String>> {
        @Override
        protected void onPostExecute(List<String> stringList) {
            super.onPostExecute(stringList);
            Log.v(LOG_TAG, "ON POST EXECUTE");
            if (stringList != null) {
                Bill.clear();
                for (String s : stringList) {
                    Bill.add(s);
                }
            }
        }
        private String getReadableDateString(long time) {
            // Because the API returns a unix timestamp (measured in seconds),
            // it must be converted to milliseconds in order to be converted to valid date.
            SimpleDateFormat shortenedDateFormat = new SimpleDateFormat("EEE MMM dd");
            return shortenedDateFormat.format(time);
        }

        /**

         */
        private String formatHighLows(double high, double low) {
            // For presentation, assume the user doesn't care about tenths of a degree.
            long roundedHigh = Math.round(high);
            long roundedLow = Math.round(low);

            String highLowStr = roundedHigh + "/" + roundedLow;
            return highLowStr;
        }

        /**
         * Take the String representing the complete forecast in JSON Format and
         * pull out the data we need to construct the Strings needed for the wireframes.
         * <p/>
         * Fortunately parsing is easy:  constructor takes the JSON string and converts it
         * into an Object hierarchy for us.
         */
        private String[] getWeatherDataFromJson(String forecastJsonStr, int numDays)
                throws JSONException {

            // These are the names of the JSON objects that need to be extracted.
            final String OWM_LIST = "list";
            final String OWM_WEATHER = "weather";
            final String OWM_TEMPERATURE = "temp";
            final String OWM_MAX = "max";
            final String OWM_MIN = "min";
            final String OWM_DESCRIPTION = "main";

            JSONObject forecastJson = new JSONObject(forecastJsonStr);
            JSONArray weatherArray = forecastJson.getJSONArray(OWM_LIST);


            Time dayTime = new Time();
            dayTime.setToNow();

            // we start at the day returned by local time. Otherwise this is a mess.
            int julianStartDay = Time.getJulianDay(System.currentTimeMillis(), dayTime.gmtoff);

            // now we work exclusively in UTC
            dayTime = new Time();

            String[] resultStrs = new String[numDays];
            for (int i = 0; i < weatherArray.length(); i++) {
                // For now, using the format "Day, description, hi/low"
                String day;
                String description;
                String highAndLow;

                // Get the JSON object representing the day
                JSONObject dayForecast = weatherArray.getJSONObject(i);

                // The date/time is returned as a long.  We need to convert that
                // into something human-readable, since most people won't read "1400356800" as
                // "this saturday".
                Log.d(LOG_TAG,"Getting date");
                long dateTime;
                // Cheating to convert this to UTC time, which is what we want anyhow
                dateTime = dayTime.setJulianDay(julianStartDay + i);
                day = getReadableDateString(dateTime);
                Log.d(LOG_TAG,"GOT DATE");

                // description is in a child array called "weather", which is 1 element long.
                JSONObject weatherObject = dayForecast.getJSONArray(OWM_WEATHER).getJSONObject(0);
                description = weatherObject.getString(OWM_DESCRIPTION);

                // Temperatures are in a child object called "temp".  Try not to name variables
                // "temp" when working with temperature.  It confuses everybody.
                JSONObject temperatureObject = dayForecast.getJSONObject(OWM_TEMPERATURE);
                double high = temperatureObject.getDouble(OWM_MAX);
                double low = temperatureObject.getDouble(OWM_MIN);

                highAndLow = formatHighLows(high, low);
                resultStrs[i] = day + " - " + description + " - " + highAndLow;
            }

            for (String s : resultStrs) {
                Log.v(LOG_TAG, "Forecast entry: " + s);
            }
            return resultStrs;

        }

    @Override
    protected List<String> doInBackground(String... params) {
        return getDataAsList("11950");//TODO implement list
//            return getDataAsList(params[0]);//TODO implement list
    }

    public String getData(String zip) {
        Log.i(LOG_TAG, "START GET DATA");
        StringBuilder sb = new StringBuilder();
        try {
            Log.i(LOG_TAG, "ATTEMP ESTABLISH CONNECTION");
            URL url;
            url = new URL("http://api.openweathermap.org/data/2.5/forecast/daily?q=11772&mode=json&units=metric&cnt=7");
            // Create the request to OpenWeatherMap, and open the connection
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            Log.i(LOG_TAG, "CONNECTION ESTABLISHED");

            // Read the input stream into a String
            InputStream inputStream = urlConnection.getInputStream();
            StringBuffer buffer = new StringBuffer();
            if (inputStream == null) {
                // Nothing to do.
                return null;
            }
            Log.i(LOG_TAG, "ABOUT TO READ");

            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
                Log.i(LOG_TAG, line);
            }
            Log.i(LOG_TAG, "READ");

        } catch (IOException e) {
            Log.e(LOG_TAG, e.toString());
        }
        return sb.toString();
    }

    public List<String> getDataAsList(String zip) {
        String data = getData(zip);
        Log.i(LOG_TAG, "START PARSE DATA");
        Log.i("zipp", data);

        List<String> list = new ArrayList<String>();
        try {
            JSONObject everything = new JSONObject(data);
            JSONArray jArray = everything.getJSONArray("list");
            for (int i = 0; i < jArray.length(); i++) {
                JSONObject json = jArray.getJSONObject(i);
                JSONObject temp = json.getJSONObject("temp");
                String max = temp.getString("max");
                String min = temp.getString("min");
                String weather = json.getJSONArray("weather").getJSONObject(0).getString("main");
                list.add(weather + " " + max + " " + min);
                //String main = weather.main(getString);
            }
        } catch (JSONException e) {
            Log.e(LOG_TAG, e.toString());
        }
        return list;
    }


}
}

