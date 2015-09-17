package com.example.cjfin.sunshine.app;

import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;

/**
 * A placeholder fragment containing a simple view.
 */
public class MainActivityFragment extends Fragment {

    public MainActivityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        ArrayList <String> weatherlist = new ArrayList <String>();
        weatherlist.add("Today the weather is mild with a high of 65");
        weatherlist.add("Tomorrow cold high 30/low 20");
        weatherlist.add("Tuesday rainy high 50/low 40");
        weatherlist.add("Wednesday humid high 70/low 60");
        weatherlist.add("Thursday foggy high 83/low 70");
        weatherlist.add("Friday sunny high 80/low 70");
        weatherlist.add("Saturday clear skies high 63/low 60");

        ArrayAdapter <String> Bill = new ArrayAdapter<String>( getActivity(),
                R.layout.list_item_forecast,
                R.id.list_item_forecast_textview,weatherlist);

        ListView listView = (ListView)rootView.findViewById(R.id.ListView_forecast);
        listView.setAdapter(Bill);
        return rootView;
    }
}
