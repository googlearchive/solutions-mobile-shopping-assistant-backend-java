package com.google.samplesolutions.mobileassistant;

import com.google.samplesolutions.mobileassistant.EMF;

import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiMethod;
import com.google.api.server.spi.config.ApiNamespace;
import com.google.api.server.spi.response.CollectionResponse;
import com.google.appengine.api.datastore.Cursor;
import com.google.appengine.datanucleus.query.JPACursorHelper;

import java.util.List;

import javax.annotation.Nullable;
import javax.inject.Named;
import javax.persistence.EntityExistsException;
import javax.persistence.EntityNotFoundException;
import javax.persistence.EntityManager;
import javax.persistence.Query;

@Api(name = "checkinendpoint", namespace = @ApiNamespace(ownerDomain = "google.com",
    ownerName = "google.com", packagePath = "samplesolutions.mobileassistant"))
public class CheckInEndpoint {

  /**
   * This method lists all the entities inserted in datastore.
   * It uses HTTP GET method and paging support.
   *
   * @return A CollectionResponse class containing the list of all entities
   * persisted and a cursor to the next page.
   */
  @SuppressWarnings({"unchecked", "unused"})
  @ApiMethod(name = "listCheckIn")
  public CollectionResponse<CheckIn> listCheckIn(
      @Nullable @Named("cursor") String cursorString, @Nullable @Named("limit") Integer limit) {

    EntityManager mgr = null;
    Cursor cursor = null;
    List<CheckIn> execute = null;

    try {
      mgr = getEntityManager();
      Query query = mgr.createQuery("select from CheckIn as CheckIn");
      if (cursorString != null && cursorString != "") {
        cursor = Cursor.fromWebSafeString(cursorString);
        query.setHint(JPACursorHelper.CURSOR_HINT, cursor);
      }

      if (limit != null) {
        query.setFirstResult(0);
        query.setMaxResults(limit);
      }

      execute = (List<CheckIn>) query.getResultList();
      cursor = JPACursorHelper.getCursor(execute);
      if (cursor != null) cursorString = cursor.toWebSafeString();

      // Tight loop for fetching all entities from datastore and accomodate
      // for lazy fetch.
      for (CheckIn obj : execute);
    } finally {
      mgr.close();
    }

    return CollectionResponse.<CheckIn>builder()
        .setItems(execute).setNextPageToken(cursorString).build();
  }

  /**
   * This method gets the entity having primary key id. It uses HTTP GET method.
   *
   * @param id the primary key of the java bean.
   * @return The entity with primary key id.
   */
  @ApiMethod(name = "getCheckIn")
  public CheckIn getCheckIn(@Named("id") Long id) {
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
   * This inserts a new entity into App Engine datastore. If the entity already
   * exists in the datastore, an exception is thrown.
   * It uses HTTP POST method.
   *
   * @param checkin the entity to be inserted.
   * @return The inserted entity.
   */
  @ApiMethod(name = "insertCheckIn")
  public CheckIn insertCheckIn(CheckIn checkin) {
    EntityManager mgr = getEntityManager();
    try {
     
      mgr.persist(checkin);
    } finally {
      mgr.close();
    }
    return checkin;
  }

  /**
   * This method is used for updating an existing entity. If the entity does not
   * exist in the datastore, an exception is thrown.
   * It uses HTTP PUT method.
   *
   * @param checkin the entity to be updated.
   * @return The updated entity.
   */
  @ApiMethod(name = "updateCheckIn")
  public CheckIn updateCheckIn(CheckIn checkin) {
    EntityManager mgr = getEntityManager();
    try {
      if (!containsCheckIn(checkin)) {
        throw new EntityNotFoundException("Object does not exist");
      }
      mgr.persist(checkin);
    } finally {
      mgr.close();
    }
    return checkin;
  }

  /**
   * This method removes the entity with primary key id.
   * It uses HTTP DELETE method.
   *
   * @param id the primary key of the entity to be deleted.
   * @return The deleted entity.
   */
  @ApiMethod(name = "removeCheckIn")
  public CheckIn removeCheckIn(@Named("id") Long id) {
    EntityManager mgr = getEntityManager();
    CheckIn checkin = null;
    try {
      checkin = mgr.find(CheckIn.class, id);
      mgr.remove(checkin);
    } finally {
      mgr.close();
    }
    return checkin;
  }

  private boolean containsCheckIn(CheckIn checkin) {
    EntityManager mgr = getEntityManager();
    boolean contains = true;
    try {
      CheckIn item = mgr.find(CheckIn.class, checkin.getKey());
      if (item == null) {
        contains = false;
      }
    } finally {
      mgr.close();
    }
    return contains;
  }

  private static EntityManager getEntityManager() {
    return EMF.get().createEntityManager();
  }

}
