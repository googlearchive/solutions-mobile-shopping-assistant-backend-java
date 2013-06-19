/*
 * Copyright (c) 2012 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package com.google.samplesolutions.mobileassistant;

import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.samplesolutions.mobileassistant.offerendpoint.Offerendpoint;
import com.google.samplesolutions.mobileassistant.offerendpoint.model.Offer;
import com.google.samplesolutions.mobileassistant.offerendpoint.model.CollectionResponseOffer;
import com.google.samplesolutions.mobileassistant.placeendpoint.model.Place;
import com.google.samplesolutions.mobileassistant.recommendationendpoint.Recommendationendpoint;
import com.google.samplesolutions.mobileassistant.recommendationendpoint.model.Recommendation;
import com.google.samplesolutions.mobileassistant.recommendationendpoint.model.CollectionResponseRecommendation;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Window;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Activity used when the user "selected" a place or checked into a place.
 */
public class PlaceDetailsActivity extends Activity {
  protected static Place currentPlace;

  ListView offersList;
  ListView recommendationsList;

  TextView placesNameLabel;
  TextView offersListLabel;
  TextView recommendationsListLabel;

  /**
   * Initializes the activity content, binds relevant widgets and starts asynchronously retrieving
   * offers and recommendations.
   */
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    setContentView(R.layout.activity_single_place);

    offersList = (ListView) findViewById(R.id.OffersList);
    recommendationsList = (ListView) findViewById(R.id.RecommendationsList);

    placesNameLabel = (TextView) findViewById(R.id.PlacesNameLabel);
    offersListLabel = (TextView) findViewById(R.id.OffersListLabel);
    recommendationsListLabel = (TextView) findViewById(R.id.RecommendationListLabel);

    placesNameLabel.setText(currentPlace.getName());

    retrieveOffers();
    retrieveRecommendations();
  }
  
  /**
   * Retrieves offers asynchronously and updates relevant widgets
   */
  void retrieveOffers() {
    new ListOfOffersAsyncRertriever().execute(currentPlace);
  }

  /**
   * Retrieves recommendations asynchronously and updates relevant widgets
   */
  void retrieveRecommendations() {
    new ListOfRecommendationsAsyncRetriever().execute(currentPlace);
  }

  /**
   * AsyncTask for retrieving the list of offers and updating the corresponding ListView and label.
   */
  private class ListOfOffersAsyncRertriever extends AsyncTask<Place, Void, CollectionResponseOffer> {
    private Logger log = Logger.getLogger(ListOfOffersAsyncRertriever.class.getName());

    /**
     * Updates UI to indicate that offers are being retrieved.
     */
    @Override
    protected void onPreExecute() {
      offersListLabel.setText(R.string.retrievingOffers);
    }

    /**
     * Updates UI to indicate that retrieval of the offers completed successfully or failed.
     */
    @Override
    protected void onPostExecute(CollectionResponseOffer result) {

      if (result == null || result.getItems() == null || result.getItems().size() < 1) {
        if (result == null) {
          offersListLabel.setText(R.string.failedToRetrieveOffers);
        } else {
          offersListLabel.setText(R.string.noOffers);
        }
        offersList.setAdapter(null);
        return;
      }

      offersListLabel.setText(R.string.offers);

      ListAdapter offersListAdapter = createOfferListAdapter(result.getItems());

      offersList.setAdapter(offersListAdapter);
    }

    /**
     * Creates ListAdapter populated with offer information.
     *
     * @param offers the list of offers used to populate the adapter.
     * @return an adapter populated with offer information.
     */
    private ListAdapter createOfferListAdapter(List<Offer> offers) {
      List<Map<String, Object>> data = new ArrayList<Map<String, Object>>();
      for (Offer offer : offers) {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("offerIcon", offer.getImageUrl());
        map.put("offerTitle", offer.getTitle());
        map.put("offerDetails", offer.getDescription());
        data.add(map);
      }

      SimpleAdapter adapter = new SimpleAdapter(PlaceDetailsActivity.this, data,
          R.layout.offer_item, new String[] {"offerIcon", "offerTitle", "offerDetails"},
          new int[] {R.id.offer_Image, R.id.offer_name, R.id.offer_description});
      adapter.setViewBinder(new ImageUrlViewBinder(R.id.offer_Image));
      return adapter;
    }

    /**
     * Retrieves the list of offers through appropriate CloudEndpoint.
     * @param params the place for which to retrieve offers.
     * @return collection of retrieved offers.
     */
    @Override
    protected CollectionResponseOffer doInBackground(Place... params) {
        Offerendpoint.Builder endpointBuilder = new Offerendpoint.Builder(AndroidHttp.newCompatibleTransport(), new JacksonFactory(), null);
        endpointBuilder = CloudEndpointUtils.updateBuilder(endpointBuilder);
        CollectionResponseOffer result;
        Offerendpoint endpoint = endpointBuilder.build();
        try {
          result = endpoint.listOffer().execute();
        } catch (IOException e){
          e.printStackTrace();
          result=null;
        }
        return result;
    }
  }

  /**
   * AsyncTask for retrieving the list of recommendations and updating the corresponding ListView
   * and label.
   */
  private class ListOfRecommendationsAsyncRetriever
      extends AsyncTask<Place, Void, CollectionResponseRecommendation> {
    private Logger log = Logger.getLogger(ListOfOffersAsyncRertriever.class.getName());

    /**
     * Updates UI to indicate that recommendations are being retrieved.
     */
    @Override
    protected void onPreExecute() {
      recommendationsListLabel.setText(R.string.retrievingRecommendations);
    }

    /**
     * Updates UI to indicate that retrieval of the offers completed successfully or failed.
     */
    @Override
    protected void onPostExecute(CollectionResponseRecommendation result) {
      if (result == null || result.getItems() == null || result.getItems().size() < 1) {
        if (result == null) {
          recommendationsListLabel.setText(R.string.failedToRetrieveRecommendations);
        } else {
          recommendationsListLabel.setText(R.string.noRecommendations);
        }
        recommendationsList.setAdapter(null);
        return;
      }

      recommendationsListLabel.setText(R.string.recommendations);

      ListAdapter recommendationsListAdapter = createRecommendationsListAdapter(result.getItems());
      recommendationsList.setAdapter(recommendationsListAdapter);
    }

    /**
     * Creates ListAdapter populated with recommendation information.
     *
     * @param recommendations the list of recommendations used to populate the adapter.
     * @return an adapter populated with recommendation information.
     */
    private ListAdapter createRecommendationsListAdapter(List<Recommendation> recommendations) {
      List<Map<String, Object>> data = new ArrayList<Map<String, Object>>();
      for (Recommendation recommendation : recommendations) {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("productImage", recommendation.getImageUrl());
        map.put("recommendationTitle", recommendation.getTitle());
        map.put("recommendationDetails", recommendation.getDescription());
        data.add(map);
      }

      SimpleAdapter adapter = new SimpleAdapter(PlaceDetailsActivity.this, data,
          R.layout.offer_item,
          new String[] {"productImage", "recommendationTitle", "recommendationDetails"},
          new int[] {R.id.offer_Image, R.id.offer_name, R.id.offer_description});
      adapter.setViewBinder(new ImageUrlViewBinder(R.id.offer_Image));
      return adapter;
    }

    /**
     * Retrieves the list of recommendations through appropriate CloudEndpoint.
     *
     * @param params the place for which to retrieve recommendations.
     * @return collection of retrieved recommendations.
     */
    @Override
    protected CollectionResponseRecommendation doInBackground(Place... params) {
      Recommendationendpoint.Builder endpointBuilder = new Recommendationendpoint.Builder(AndroidHttp.newCompatibleTransport(), new JacksonFactory(), null);
      endpointBuilder = CloudEndpointUtils.updateBuilder(endpointBuilder);
      CollectionResponseRecommendation result;
      Recommendationendpoint endpoint = endpointBuilder.build();
      try {
        result = endpoint.listRecommendation().execute();
      } catch (IOException e) {
        e.printStackTrace();
        result = null;
      }
      return result;
    }
  }
}

