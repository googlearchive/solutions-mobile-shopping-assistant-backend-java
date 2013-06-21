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

@Api(name = "offerendpoint", namespace = @ApiNamespace(ownerDomain = "google.com",
    ownerName = "google.com", packagePath = "samplesolutions.mobileassistant"))
public class OfferEndpoint {

  /**
   * This method lists all the entities inserted in datastore.
   * It uses HTTP GET method and paging support.
   *
   * @return A CollectionResponse class containing the list of all entities
   * persisted and a cursor to the next page.
   */
  @SuppressWarnings({"unchecked", "unused"})
  @ApiMethod(name = "listOffer")
  public CollectionResponse<Offer> listOffer(
      @Nullable @Named("cursor") String cursorString, @Nullable @Named("limit") Integer limit) {

    EntityManager mgr = null;
    Cursor cursor = null;
    List<Offer> execute = null;

    try {
      mgr = getEntityManager();
      Query query = mgr.createQuery("select from Offer as Offer");
      if (cursorString != null && cursorString != "") {
        cursor = Cursor.fromWebSafeString(cursorString);
        query.setHint(JPACursorHelper.CURSOR_HINT, cursor);
      }

      if (limit != null) {
        query.setFirstResult(0);
        query.setMaxResults(limit);
      }

      execute = (List<Offer>) query.getResultList();
      cursor = JPACursorHelper.getCursor(execute);
      if (cursor != null) cursorString = cursor.toWebSafeString();

      // Tight loop for fetching all entities from datastore and accomodate
      // for lazy fetch.
      for (Offer obj : execute);
    } finally {
      mgr.close();
    }

    return CollectionResponse.<Offer>builder()
        .setItems(execute).setNextPageToken(cursorString).build();
  }

  /**
   * This method gets the entity having primary key id. It uses HTTP GET method.
   *
   * @param id the primary key of the java bean.
   * @return The entity with primary key id.
   */
  @ApiMethod(name = "getOffer")
  public Offer getOffer(@Named("id") String id) {
    EntityManager mgr = getEntityManager();
    Offer offer = null;
    try {
      offer = mgr.find(Offer.class, id);
    } finally {
      mgr.close();
    }
    return offer;
  }

  /**
   * This inserts a new entity into App Engine datastore. If the entity already
   * exists in the datastore, an exception is thrown.
   * It uses HTTP POST method.
   *
   * @param offer the entity to be inserted.
   * @return The inserted entity.
   */
  @ApiMethod(name = "insertOffer")
  public Offer insertOffer(Offer offer) {
    EntityManager mgr = getEntityManager();
    try {
      if (containsOffer(offer)) {
        throw new EntityExistsException("Object already exists");
      }
      mgr.persist(offer);
    } finally {
      mgr.close();
    }
    return offer;
  }

  /**
   * This method is used for updating an existing entity. If the entity does not
   * exist in the datastore, an exception is thrown.
   * It uses HTTP PUT method.
   *
   * @param offer the entity to be updated.
   * @return The updated entity.
   */
  @ApiMethod(name = "updateOffer")
  public Offer updateOffer(Offer offer) {
    EntityManager mgr = getEntityManager();
    try {
      if (!containsOffer(offer)) {
        throw new EntityNotFoundException("Object does not exist");
      }
      mgr.persist(offer);
    } finally {
      mgr.close();
    }
    return offer;
  }

  /**
   * This method removes the entity with primary key id.
   * It uses HTTP DELETE method.
   *
   * @param id the primary key of the entity to be deleted.
   * @return The deleted entity.
   */
  @ApiMethod(name = "removeOffer")
  public Offer removeOffer(@Named("id") String id) {
    EntityManager mgr = getEntityManager();
    Offer offer = null;
    try {
      offer = mgr.find(Offer.class, id);
      mgr.remove(offer);
    } finally {
      mgr.close();
    }
    return offer;
  }

  private boolean containsOffer(Offer offer) {
    EntityManager mgr = getEntityManager();
    boolean contains = true;
    try {
      Offer item = mgr.find(Offer.class, offer.getOfferId());
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
