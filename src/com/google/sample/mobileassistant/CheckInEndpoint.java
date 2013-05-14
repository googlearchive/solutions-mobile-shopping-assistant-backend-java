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

import static com.google.appengine.api.taskqueue.TaskOptions.Builder.withUrl;

import com.google.api.server.spi.ServiceException;
import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiNamespace;
import com.google.appengine.api.taskqueue.Queue;
import com.google.appengine.api.taskqueue.QueueFactory;
import com.google.appengine.api.users.User;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

import javax.inject.Named;
import javax.persistence.EntityManager;
import javax.persistence.Query;

/**
 * Exposes REST API over CheckIn resources.
 */
@Api(name = "shoppingassistant", resource = "checkins", version = "v1", namespace = @ApiNamespace(
    ownerDomain = ApiKeysAndIds.API_OWNER, ownerName = ApiKeysAndIds.API_OWNER,
    packagePath = ApiKeysAndIds.API_PACKAGE_PATH), clientIds = {
    ApiKeysAndIds.ANDROID_CLIENT_ID, ApiKeysAndIds.IOS_CLIENT_ID, ApiKeysAndIds.WEB_CLIENT_ID},
    audiences = {ApiKeysAndIds.AUDIENCE_ID})
public class CheckInEndpoint {
  private static final Logger log = Logger.getLogger(CheckInEndpoint.class.getName());

  /**
   * This method lists all the entities inserted in datastore. It uses HTTP GET method.
   *
   * @return List of all entities persisted.
   */
  @SuppressWarnings({"cast", "unchecked"})
  public List<CheckIn> list(User user) throws ServiceException {
    EndpointUtil.throwIfNotAdmin(user);

    EntityManager mgr = getEntityManager();
    List<CheckIn> result = new ArrayList<CheckIn>();
    try {
      Query query = mgr.createQuery("select from CheckIn as CheckIn");
      for (CheckIn checkIn : (List<CheckIn>) query.getResultList()) {
        result.add(checkIn);
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
  public CheckIn get(@Named("id") Long id, User user) throws ServiceException {
    EndpointUtil.throwIfNotAdmin(user);

    EntityManager mgr = getEntityManager();
    CheckIn checkin = null;
    try {
      checkin = mgr.find(CheckIn.class, id);
    } finally {
      mgr.close();
    }
    return checkin;
  }

  /**
   * This inserts the entity into App Engine datastore. It uses HTTP POST method.
   *
   * @param checkin the entity to be inserted.
   * @return The inserted entity.
   */
  public CheckIn insert(CheckIn checkin, User user) throws ServiceException {
    EndpointUtil.throwIfNotAuthenticated(user);

    checkin.setUserEmail(user.getEmail());
    checkin.setCheckinDate(new Date());

    // Do not use the key provided by the caller; use a generated key.
    checkin.clearKey();

    EntityManager mgr = getEntityManager();
    try {
      mgr.persist(checkin);
    } finally {
      mgr.close();
    }

    // generate personalized offers when user checks into a place and send them
    // to the user using push notification
    pushPersonalizedOffers(checkin.getPlaceId(), user);

    return checkin;
  }

  /**
   * This method is used for updating a entity. It uses HTTP PUT method.
   *
   * @param checkin the entity to be updated.
   * @return The updated entity.
   */
  public CheckIn update(CheckIn checkin, User user) throws ServiceException {
    EndpointUtil.throwIfNotAdmin(user);

    EntityManager mgr = getEntityManager();
    try {
      mgr.persist(checkin);
    } finally {
      mgr.close();
    }
    return checkin;
  }

  /**
   * This method removes the entity with primary key id. It uses HTTP DELETE method.
   *
   * @param id the primary key of the entity to be deleted.
   */
  public void remove(@Named("id") Long id, User user) throws ServiceException {
    EndpointUtil.throwIfNotAdmin(user);

    EntityManager mgr = getEntityManager();
    CheckIn checkin = null;
    try {
      checkin = mgr.find(CheckIn.class, id);
      mgr.remove(checkin);
    } finally {
      mgr.close();
    }
  }

  private static EntityManager getEntityManager() {
    return EMF.get().createEntityManager();
  }

  private void pushPersonalizedOffers(String placeId, User user) {
    // insert a task to a queue
    log.info("adding a task to recommendations-queue");
    Queue queue = QueueFactory.getQueue("recommendations-queue");

    try {
      String userEmail = user.getEmail();
      queue.add(withUrl("/tasks/recommendations")
          .param("userEmail", userEmail).param("placeId", placeId));
      log.info("task added");
    } catch (RuntimeException e) {
      log.severe(e.getMessage());
    }
  }
}
