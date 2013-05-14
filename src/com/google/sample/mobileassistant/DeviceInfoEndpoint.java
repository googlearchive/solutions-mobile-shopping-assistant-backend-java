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
import com.google.api.server.spi.config.ApiNamespace;
import com.google.appengine.api.users.User;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import javax.inject.Named;
import javax.persistence.EntityManager;
import javax.persistence.Query;

/**
 * Exposes REST API over DeviceInfo resources.
 */
@Api(name = "shoppingassistant", resource = "deviceinfos",
    version = "v1", namespace = @ApiNamespace(ownerDomain = ApiKeysAndIds.API_OWNER,
        ownerName = ApiKeysAndIds.API_OWNER, packagePath = ApiKeysAndIds.API_PACKAGE_PATH),
    clientIds = {
        ApiKeysAndIds.ANDROID_CLIENT_ID, ApiKeysAndIds.IOS_CLIENT_ID, ApiKeysAndIds.WEB_CLIENT_ID},
    audiences = {ApiKeysAndIds.AUDIENCE_ID})
public class DeviceInfoEndpoint {
  private static final Logger log = Logger.getLogger(DeviceInfoEndpoint.class.getName());

  /**
   * This method lists all the entities inserted in datastore. It uses HTTP GET method.
   *
   * @param user user object if the caller is authenticated. Set by Cloud Endpoints infrastructure.
   * @return List of all entities persisted.
   * @throws ServiceException if the caller is not authorized to call this method
   */
  @SuppressWarnings({"cast", "unchecked"})
  public List<DeviceInfo> list(User user) throws ServiceException {
    EndpointUtil.throwIfNotAdmin(user);

    EntityManager mgr = getEntityManager();
    List<DeviceInfo> result = new ArrayList<DeviceInfo>();
    try {
      Query query = mgr.createQuery("select from DeviceInfo as DeviceInfo");
      for (DeviceInfo deviceInfo : (List<DeviceInfo>) query.getResultList()) {
        result.add(deviceInfo);
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
   * @param user user object if the caller is authenticated. Set by Cloud Endpoints infrastructure.
   * @return The entity with primary key id.
   * @throws ServiceException if the caller is not authorized to call this method
   */
  public DeviceInfo get(@Named("id") String id, User user) throws ServiceException {
    EndpointUtil.throwIfNotAdmin(user);

    EntityManager mgr = getEntityManager();
    DeviceInfo deviceinfo = null;
    try {
      deviceinfo = mgr.find(DeviceInfo.class, id);
    } finally {
      mgr.close();
    }
    return deviceinfo;
  }

  /**
   * This inserts the entity into App Engine datastore. It uses HTTP POST method.
   *
   * @param deviceinfo the entity to be inserted.
   * @param user user object if the caller is authenticated. Set by Cloud Endpoints infrastructure.
   * @return The inserted entity.
   * @throws ServiceException if the caller is unauthenticated
   */
  public DeviceInfo insert(DeviceInfo deviceinfo, User user) throws ServiceException {
    EndpointUtil.throwIfNotAuthenticated(user);

    // TODO(user): associate user information (user id / email) with
    // deviceInfo

    log.info("Inserting device info");
    return insertDeviceInfo(deviceinfo);
  }

  /**
   * This method is used for updating a entity. It uses HTTP PUT method.
   *
   * @param deviceinfo the entity to be updated.
   * @param user user object if the caller is authenticated. Set by Cloud Endpoints infrastructure.
   * @return The updated entity.
   * @throws ServiceException if the caller is not authorized to call this method
   */
  public DeviceInfo update(DeviceInfo deviceinfo, User user) throws ServiceException {
    EndpointUtil.throwIfNotAdmin(user);

    EntityManager mgr = getEntityManager();
    try {
      mgr.persist(deviceinfo);
    } finally {
      mgr.close();
    }
    return deviceinfo;
  }

  /**
   * This method removes the entity with primary key id. It uses HTTP DELETE method.
   *
   * @param id the primary key of the entity to be deleted.
   * @param user user object if the caller is authenticated. Set by Cloud Endpoints infrastructure.
   * @throws ServiceException if the caller is not authorized to call this method
   */
  public void remove(@Named("id") String id, User user) throws ServiceException {
    EndpointUtil.throwIfNotAdmin(user);

    removeDeviceInfo(id);
  }

  void removeDeviceInfo(String id) {
    EntityManager mgr = getEntityManager();
    DeviceInfo deviceinfo = null;
    try {
      deviceinfo = mgr.find(DeviceInfo.class, id);
      mgr.remove(deviceinfo);
    } finally {
      mgr.close();
    }
  }

  DeviceInfo insertDeviceInfo(DeviceInfo deviceinfo) {
    EntityManager mgr = getEntityManager();
    try {
      mgr.persist(deviceinfo);
    } finally {
      mgr.close();
    }
    return deviceinfo;
  }

  private static EntityManager getEntityManager() {
    return EMF.get().createEntityManager();
  }
}
