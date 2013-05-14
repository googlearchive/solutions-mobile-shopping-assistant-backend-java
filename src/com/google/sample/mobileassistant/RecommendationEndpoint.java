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
import com.google.appengine.api.users.User;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.inject.Named;
import javax.persistence.EntityManager;
import javax.persistence.Query;

/**
 * Exposes REST API over Recommendation resources.
 */
@Api(name = "shoppingassistant", resource = "recommendations",
    version = "v1", namespace = @ApiNamespace(ownerDomain = ApiKeysAndIds.API_OWNER,
        ownerName = ApiKeysAndIds.API_OWNER, packagePath = ApiKeysAndIds.API_PACKAGE_PATH),
    clientIds = {
        ApiKeysAndIds.ANDROID_CLIENT_ID, ApiKeysAndIds.IOS_CLIENT_ID, ApiKeysAndIds.WEB_CLIENT_ID},
    audiences = {ApiKeysAndIds.AUDIENCE_ID})
public class RecommendationEndpoint {
  /**
   * This method lists all the entities inserted in datastore. It uses HTTP GET method.
   *
   * @return List of all entities persisted.
   */
  @SuppressWarnings({"cast", "unchecked"})
  @ApiMethod(httpMethod = "GET", path = "recommendation")
  public List<Recommendation> list(@Named("placeId") String placeId, User user) {
    // TODO(user): Retrieve only recommendations applicable to given place

    EntityManager mgr = getEntityManager();
    List<Recommendation> result = new ArrayList<Recommendation>();
    try {
      // Retrieve only recommendations that haven't expired
      Query query = mgr.createQuery(
          "select from Recommendation as Recommendation " + "where expiration > :expiration")
          .setParameter("expiration", new Date());

      for (Recommendation recommendation : (List<Recommendation>) query.getResultList()) {
        result.add(recommendation);
      }
    } finally {
      mgr.close();
    }
    return result;
  }

  /**
   * This method gets the entity having primary key id. It uses HTTP GET method.
   *
   * @param id the primary key of the java bean.
   * @return The entity with primary key id.
   */
  public Recommendation get(@Named("id") Long id, User user) throws ServiceException {
    EndpointUtil.throwIfNotAdmin(user);

    EntityManager mgr = getEntityManager();
    Recommendation recommendation = null;
    try {
      recommendation = mgr.find(Recommendation.class, id);
    } finally {
      mgr.close();
    }
    return recommendation;
  }

  /**
   * This inserts the entity into App Engine datastore. It uses HTTP POST method.
   *
   * @param recommendation the entity to be inserted.
   * @return The inserted entity.
   */
  public Recommendation insert(Recommendation recommendation, User user)
      throws ServiceException {
    EndpointUtil.throwIfNotAdmin(user);

    EntityManager mgr = getEntityManager();
    try {
      mgr.persist(recommendation);
    } finally {
      mgr.close();
    }
    return recommendation;
  }

  /**
   * This method is used for updating a entity. It uses HTTP PUT method.
   *
   * @param recommendation the entity to be updated.
   * @return The updated entity.
   */
  public Recommendation update(Recommendation recommendation, User user)
      throws ServiceException {
    EndpointUtil.throwIfNotAdmin(user);

    EntityManager mgr = getEntityManager();
    try {
      mgr.persist(recommendation);
    } finally {
      mgr.close();
    }
    return recommendation;
  }

  /**
   * This method removes the entity with primary key id. It uses HTTP DELETE method.
   *
   * @param id the primary key of the entity to be deleted.
   */
  public void remove(@Named("id") Long id, User user)
      throws ServiceException {
    EndpointUtil.throwIfNotAdmin(user);

    EntityManager mgr = getEntityManager();
    Recommendation recommendation = null;
    try {
      recommendation = mgr.find(Recommendation.class, id);
      mgr.remove(recommendation);
    } finally {
      mgr.close();
    }
  }

  private static EntityManager getEntityManager() {
    return EMF.get().createEntityManager();
  }
}
