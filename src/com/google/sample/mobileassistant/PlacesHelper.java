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
package com.google.sample.mobileassistant;

import static com.google.appengine.api.utils.SystemProperty.Environment.Value.Development;
import static com.google.appengine.api.utils.SystemProperty.environment;

import com.google.appengine.api.datastore.GeoPt;
import com.google.appengine.api.search.Document;
import com.google.appengine.api.search.Field;
import com.google.appengine.api.search.GeoPoint;
import com.google.appengine.api.search.Index;
import com.google.appengine.api.search.IndexSpec;
import com.google.appengine.api.search.Query;
import com.google.appengine.api.search.QueryOptions;
import com.google.appengine.api.search.Results;
import com.google.appengine.api.search.ScoredDocument;
import com.google.appengine.api.search.SearchServiceFactory;
import com.google.appengine.api.search.SortExpression;
import com.google.appengine.api.search.SortOptions;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * Helper class for geo-proximity related management of Places.
 */
class PlacesHelper {
  private static final Logger log = Logger.getLogger(PlacesHelper.class.getName());
  private static final String INDEX_NAME = "Places";

  static Index getIndex() {
    IndexSpec indexSpec = IndexSpec.newBuilder().setName(INDEX_NAME).build();
    return SearchServiceFactory.getSearchService().getIndex(indexSpec);
  }

  static Document buildDocument(
      String placeId, String placeName, String placeAddress, GeoPt location) {
    GeoPoint geoPoint = new GeoPoint(location.getLatitude(), location.getLongitude());

    Document.Builder builder = Document.newBuilder()
        .addField(Field.newBuilder().setName("id").setText(placeId))
        .addField(Field.newBuilder().setName("name").setText(placeName))
        .addField(Field.newBuilder().setName("address").setText(placeAddress))
        .addField(Field.newBuilder().setName("place_location").setGeoPoint(geoPoint));

    // geo-location doesn't work under dev_server, so let's add another
    // field to use for retrieving documents
    if (environment.value() == Development) {
      builder.addField(Field.newBuilder().setName("value").setNumber(1));
    }

    Document place = builder.build();

    return place;
  }

  static List<PlaceInfo> getPlaces(GeoPt location, long distanceInMeters, int resultCount) {

    // TODO(user): Use memcache

    String geoPoint = "geopoint(" + location.getLatitude() + ", " + location.getLongitude() + ")";

    String query = "distance(place_location, " + geoPoint + ") < " + distanceInMeters;
    String locExpr = "distance(place_location, " + geoPoint + ")";

    SortExpression sortExpr = SortExpression.newBuilder()
        .setExpression(locExpr)
        .setDirection(SortExpression.SortDirection.ASCENDING)
        .setDefaultValueNumeric(distanceInMeters + 1)
        .build();
    Query searchQuery = Query.newBuilder().setOptions(QueryOptions.newBuilder()
        .setSortOptions(SortOptions.newBuilder().addSortExpression(sortExpr))).build(query);
    Results<ScoredDocument> results = getIndex().search(searchQuery);

    if (results.getNumberFound() == 0) {
      // geo-location doesn't work under dev_server
      if (environment.value() == Development) {
        // return all documents
        results = getIndex().search("value > 0");
      }
    }

    List<PlaceInfo> places = new ArrayList<PlaceInfo>();

    for (ScoredDocument document : results) {
      if (places.size() >= resultCount) {
        break;
      }

      GeoPoint p = document.getOnlyField("place_location").getGeoPoint();

      PlaceInfo place = new PlaceInfo();
      place.setplaceID(document.getOnlyField("id").getText());
      place.setName(document.getOnlyField("name").getText());
      place.setAddress(document.getOnlyField("address").getText());

      place.setLocation(new GeoPt((float) p.getLatitude(), (float) p.getLongitude()));

      // GeoPoints are not implemented on dev server and latitude and longitude are set to zero
      // But since those are doubles let's play safe
      // and use double comparison with epsilon set to 0.0001
      if (Math.abs(p.getLatitude()) <= 0.0001 && Math.abs(p.getLongitude()) <= 0.0001) {
        // set a fake distance of 5+ km
        place.setDistanceInKilometers(5 + places.size());
      } else {
        double distance = distanceInMeters / 1000;
        try {
          distance = getDistanceInKm(
              p.getLatitude(), p.getLongitude(), location.getLatitude(), location.getLongitude());
        } catch (Exception e) {
          log.warning("Exception when calculating a distance: " + e.getMessage());
        }

        place.setDistanceInKilometers(distance);
      }

      places.add(place);
    }

    return places;
  }

  static double getDistanceInKm(
      double latitude1, double longitude1, double latitude2, double longitude2) {
    final double earthRadius = 6378.1; // kilometers

    double lat1 = Math.toRadians(latitude1);
    double lat2 = Math.toRadians(latitude2);
    double long1 = Math.toRadians(longitude1);
    double long2 = Math.toRadians(longitude2);

    double dist = earthRadius * Math.acos(Math.sin(lat1) * Math.sin(lat2) + Math.cos(lat1)
        * Math.cos(lat2) * Math.cos(Math.abs(long1 - long2)));

    return dist;
  }
}
