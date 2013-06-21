/* Copyright (c) 2012 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.google.samplesolutions.mobileassistant;

import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.samplesolutions.mobileassistant.checkinendpoint.Checkinendpoint;
import com.google.samplesolutions.mobileassistant.checkinendpoint.model.CheckIn;
import com.google.samplesolutions.mobileassistant.placeendpoint.Placeendpoint;
import com.google.samplesolutions.mobileassistant.placeendpoint.model.Place;
import com.google.samplesolutions.mobileassistant.placeendpoint.model.CollectionResponsePlace;

import android.app.Activity;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;

import android.content.Intent;

public class MainActivity extends Activity {

  private ListView placesList;
  private List<Place> places = null;
  

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    placesList = (ListView) findViewById(R.id.PlacesList);

    new CheckInTask().execute();

    // start retrieving the list of nearby places
    new ListOfPlacesAsyncRetriever().execute();
    
    placesList.setOnItemClickListener(placesListClickListener);
  }


  /**
   * AsyncTask for calling Mobile Assistant API for checking into a 
   * place (e.g., a store).
   */
  private class CheckInTask extends AsyncTask<Void, Void, Void> {

    /**
     * Calls appropriate CloudEndpoint to indicate that user checked into a place.
     *
     * @param params the place where the user is checking in.
     */
    @Override
    protected Void doInBackground(Void... params) {
      CheckIn checkin = new com.google.samplesolutions.mobileassistant.checkinendpoint.model.CheckIn();

      // Set the ID of the store where the user is.
      checkin.setPlaceId("StoreNo123");

      Checkinendpoint.Builder builder = new Checkinendpoint.Builder(
          AndroidHttp.newCompatibleTransport(), new JacksonFactory(), null);

      builder = CloudEndpointUtils.updateBuilder(builder);

      Checkinendpoint endpoint = builder.build();


      try {
        endpoint.insertCheckIn(checkin).execute();
      } catch (IOException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }

      return null;
    }
  }


  /**
   * AsyncTask for retrieving the list of places (e.g., stores) and updating the
   * corresponding results list.
   */
  private class ListOfPlacesAsyncRetriever extends AsyncTask<Void, Void, CollectionResponsePlace> {

    @Override
    protected CollectionResponsePlace doInBackground(Void... params) {


      Placeendpoint.Builder endpointBuilder = new Placeendpoint.Builder(
          AndroidHttp.newCompatibleTransport(), new JacksonFactory(), null);
     
      endpointBuilder = CloudEndpointUtils.updateBuilder(endpointBuilder);


      CollectionResponsePlace result;

      Placeendpoint endpoint = endpointBuilder.build();

      try {
        result = endpoint.listPlace().execute();
      } catch (IOException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
        result = null;
      }
      return result;
    }

    @Override
    @SuppressWarnings("null")
    protected void onPostExecute(CollectionResponsePlace result) {
      ListAdapter placesListAdapter = createPlaceListAdapter(result.getItems());
      placesList.setAdapter(placesListAdapter);

      places = result.getItems();
    }
    
    private ListAdapter createPlaceListAdapter(List<Place> places) {
      final double kilometersInAMile = 1.60934;
      List<Map<String, Object>> data = new ArrayList<Map<String, Object>>();
      for (Place place : places) {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("placeIcon", R.drawable.ic_launcher);
        map.put("placeName", place.getName());
        map.put("placeAddress", place.getAddress());
        String distance = "1.2";
        map.put("placeDistance", distance);
        data.add(map);
      }

      SimpleAdapter adapter = new SimpleAdapter(MainActivity.this, data, R.layout.place_item,
          new String[] {"placeIcon", "placeName", "placeAddress", "placeDistance"},
          new int[] {R.id.place_Icon, R.id.place_name, R.id.place_address, R.id.place_distance});

      return adapter;
    }
    
  }

  private OnItemClickListener placesListClickListener = new OnItemClickListener() {
    @Override
    public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
      Place selectedPlace = places.get((int) arg3);

      new CheckInTask().execute();
      
      PlaceDetailsActivity.currentPlace = selectedPlace;
      Intent i = new Intent(MainActivity.this, PlaceDetailsActivity.class);
      startActivity(i);
      }
  };  
  
  
}