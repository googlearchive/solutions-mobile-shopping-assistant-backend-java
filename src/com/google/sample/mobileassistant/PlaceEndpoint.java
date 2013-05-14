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
package com.google.sample.mobileassistant;

import com.google.api.server.spi.ServiceException;
import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiMethod;
import com.google.api.server.spi.config.ApiNamespace;
import com.google.api.server.spi.response.BadRequestException;
import com.google.appengine.api.datastore.GeoPt;
import com.google.appengine.api.users.User;

import java.util.List;

import javax.inject.Named;
import javax.persistence.EntityManager;

/**
 * Exposes REST API over Place resources.
 */
@Api(name = "shoppingassistant", resource = "places", version = "v1", namespace = @ApiNamespace(
    ownerDomain = ApiKeysAndIds.API_OWNER, ownerName = ApiKeysAndIds.API_OWNER,
    packagePath = ApiKeysAndIds.API_PACKAGE_PATH), clientIds = {
    ApiKeysAndIds.ANDROID_CLIENT_ID, ApiKeysAndIds.IOS_CLIENT_ID, ApiKeysAndIds.WEB_CLIENT_ID},
    audiences = {ApiKeysAndIds.AUDIENCE_ID})
public class PlaceEndpoint {
  /**
   * This method lists nearby places. It uses HTTP GET method.
   *
   * @return List of nearby places.
   */
  @ApiMethod(httpMethod = "GET", path = "place")
  public List<PlaceInfo> list(@Named("longitude") String longitudeString,
      @Named("latitude") String latitudeString, @Named("distanceInKm") long distanceInKm,
      @Named("count") int count, User user) throws ServiceException {

    float latitude;
    float longitude;
    GeoPt location;

    try {
      latitude = (float) Double.parseDouble(latitudeString);
    } catch (Exception e) {
      throw new BadRequestException("Invalid value of 'latitude' argument");
    }

    try {
      longitude = (float) Double.parseDouble(longitudeString);
    } catch (Exception e) {
      throw new BadRequestException("Invalid value of 'longitude' argument");
    }

    try {
      location = new GeoPt(latitude, longitude);
    } catch (Exception e) {
      throw new BadRequestException("Invalid pair of 'latitude' and 'longitude' arguments");
    }

    // limit the result set to up to 100 places within up to 200 km
    if (count > 100) {
      count = 100;
    } else if (count <= 0) {
      throw new BadRequestException("Invalid value of 'count' argument");
    }

    if (distanceInKm > 200) {
      distanceInKm = 200;
    } else if (distanceInKm < 0) {
      throw new BadRequestException("Invalid value of 'distanceInKm' argument");
    }

    List<PlaceInfo> places = PlacesHelper.getPlaces(location, 1000 * distanceInKm, count);

    return places;
  }

  /**
   * This method gets the entity having primary key id. It uses HTTP GET method.
   *
   * @param id the primary key of the java bean.
   * @return The entity with primary key id.
   */
  public Place get(@Named("id") Long id, User user) throws ServiceException {
    EndpointUtil.throwIfNotAdmin(user);

    EntityManager mgr = getEntityManager();
    Place place = null;
    try {
      place = mgr.find(Place.class, id);
    } finally {
      mgr.close();
    }
    return place;
  }

  /**
   * This inserts the entity into App Engine datastore. It uses HTTP POST method.
   *
   * @param place the entity to be inserted.
   * @return The inserted entity.
   */
  public Place insert(Place place, User user) throws ServiceException {
    EndpointUtil.throwIfNotAdmin(user);

    EntityManager mgr = getEntityManager();
    try {
      mgr.persist(place);
    } finally {
      mgr.close();
    }
    return place;
  }

  /**
   * This method is used for updating a entity. It uses HTTP PUT method.
   *
   * @param place the entity to be updated.
   * @return The updated entity.
   */
  public Place update(Place place, User user) throws ServiceException {
    EndpointUtil.throwIfNotAdmin(user);

    EntityManager mgr = getEntityManager();
    try {
      mgr.persist(place);
    } finally {
      mgr.close();
    }
    return place;
  }

  /**
   * This method removes the entity with primary key id. It uses HTTP DELETE method.
   *
   * @param id the primary key of the entity to be deleted.
   */
  public void remove(@Named("id") Long id, User user) throws ServiceException {
    EndpointUtil.throwIfNotAdmin(user);

    EntityManager mgr = getEntityManager();
    Place place = null;
    try {
      place = mgr.find(Place.class, id);
      mgr.remove(place);
    } finally {
      mgr.close();
    }
  }

  private static EntityManager getEntityManager() {
    return EMF.get().createEntityManager();
  }
}
