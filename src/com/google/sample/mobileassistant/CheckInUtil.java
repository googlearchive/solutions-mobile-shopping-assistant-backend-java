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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

import javax.persistence.EntityManager;
import javax.persistence.Query;

/**
 * CheckIn Utility class.
 */
public class CheckInUtil {
  private static final Logger log = Logger.getLogger(CheckInUtil.class.getName());

  /**
   * Retrieves the list of checkins done by a given user at a given place since a specific date.
   *
   * @param userEmail The email address of the user.
   * @param placeId The id of the place.
   * @param dateFrom The start date for matching checkins.
   * @return List of all matching checkin entities.
   */
  @SuppressWarnings({"cast", "unchecked"})
  static List<CheckIn> getCheckinsForUser(String userEmail, String placeId, Date dateFrom) {

    EntityManager mgr = getEntityManager();
    List<CheckIn> result = new ArrayList<CheckIn>();
    try {
      log.info("list checkins for user = " + userEmail + " checked into place = " + placeId
          + "after " + dateFrom);
      Query query = mgr.createQuery("select from CheckIn as CheckIn where userEmail =:userEmail "
          + "and placeId = :placeId and checkinDate > :checkindate")
          .setParameter("userEmail", userEmail).setParameter("placeId", placeId)
          .setParameter("checkindate", dateFrom);
      for (CheckIn checkIn : (List<CheckIn>) query.getResultList()) {
        log.info(checkIn.getCheckinDate().toString());
        result.add(checkIn);
      }
    } finally {
      mgr.close();
    }
    return result;
  }

  private static EntityManager getEntityManager() {
    return EMF.get().createEntityManager();
  }
}
