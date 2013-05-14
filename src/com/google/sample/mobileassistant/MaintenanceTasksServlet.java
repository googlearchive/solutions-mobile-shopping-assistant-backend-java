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

import com.google.appengine.api.search.Document;
import com.google.appengine.api.search.GetRequest;
import com.google.appengine.api.search.GetResponse;
import com.google.appengine.api.search.Index;
import com.google.appengine.api.search.PutException;
import com.google.appengine.api.search.StatusCode;

import java.io.IOException;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * HttpServlet for handling maintenance tasks.
 */
public class MaintenanceTasksServlet extends HttpServlet {

  private static final long serialVersionUID = 1L;

  @Override
  public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    resp.setContentType("text/plain");
    if (!buildSearchIndexForPlaces()) {
      resp.getWriter().println("MaintenanceTasks failed. Try again by refreshing the page.");
      return;
    }
    resp.getWriter().println("MaintenanceTasks completed");
  }

  @SuppressWarnings({"cast", "unchecked"})
  private boolean buildSearchIndexForPlaces() {
    Index index = PlacesHelper.getIndex();

    removeAllDocumentsFromIndex();

    EntityManager mgr = getEntityManager();
    try {
      Query query = mgr.createQuery("select from Place as Place");
      for (Object obj : (List<Object>) query.getResultList()) {
        Place place = (Place) obj;
        Document placeAsDocument = PlacesHelper.buildDocument(
            place.getPlaceId(), place.getName(), place.getAddress(), place.getLocation());
        try {
          index.put(placeAsDocument);
        } catch (PutException e) {
          if (StatusCode.TRANSIENT_ERROR.equals(e.getOperationResult().getCode())) {
            return false;
          }
        }
      }
    } catch (Exception e) {
      return false;
    } finally {
      mgr.close();
    }

    return true;
  }

  private void removeAllDocumentsFromIndex() {
    Index index = PlacesHelper.getIndex();

    GetRequest request = GetRequest.newBuilder().setReturningIdsOnly(true).build();

    GetResponse<Document> response = index.getRange(request);
    for (Document document : response.getResults()) {
      index.delete(document.getId());
    }
  }

  private static EntityManager getEntityManager() {
    return EMF.get().createEntityManager();
  }
}
