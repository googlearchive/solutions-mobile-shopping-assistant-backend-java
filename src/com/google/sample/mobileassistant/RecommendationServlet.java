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

import java.io.IOException;
import java.util.Calendar;
import java.util.HashMap;
import java.util.IllegalFormatException;
import java.util.Random;
import java.util.TimeZone;
import java.util.logging.Logger;

import javax.persistence.EntityManager;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * HttpServlet for processing request to generate personalized recommendations 
 * and pushing info about them to user's devices.
 */
public class RecommendationServlet extends HttpServlet {
  private static final long serialVersionUID = 1L;
  
  // Two minutes for recommendation expiration is reasonable for demo purposes. 
  // For production it will more likely be several hours. 
  private static final int RECOMMENDATION_EXPIRATION_IN_MINUTES = 2; 
  
  private static final Logger log = Logger.getLogger(RecommendationServlet.class.getName());
  private static final Random random = new Random();

  @Override
  public void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    String placeId = req.getParameter("placeId");
    String userEmail = req.getParameter("userEmail");

    // Skip generating new recommendations if user checked into the same
    // place within the recommendation expiration time
    // In other words, if the number of checkins in this time window is
    // larger than 1 (one checkin is the "current" one)
    Calendar validityTimeWindow = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
    validityTimeWindow.add(Calendar.MINUTE, -RECOMMENDATION_EXPIRATION_IN_MINUTES);
    if (CheckInUtil.getCheckinsForUser(userEmail, placeId, validityTimeWindow.getTime()).size()
        > 1) {
      log.info("Skipping generating recommendations for user " + userEmail + " checked into place "
          + placeId);

      return;
    }

    log.info("Generating recommendations for user " + userEmail + " checked into place " + placeId);

    // This sample, instead of actually generating personalized recommendations, only pretends to 
    // crunch data for some time (by having a 15 seconds delay - long enough for demo :-) ), 
    // and then it inserts two recommendations based on a recommendation template 
    // with a specific key. The recommendation from the template is then customized using 
    // randomly generated prices and one of a few available product recommendation images.
    
    try {
      Thread.sleep(15 * 1000);
    } catch (InterruptedException e1) {
      resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
      return;
    }

    EntityManager mgr = getEntityManager();

    // Let Task Queue handle any exceptions through normal retry logic and error logging,
    // so the code only catches InvalidFormatException and allows the other exception pass through.
    
    String[] recommendationData = null;
    
    try {
      // Retrieve the recommendation template.
      Recommendation recommendationTemplate = mgr.find(Recommendation.class, "template1");

      if (recommendationTemplate == null) {
        log.warning(
            "No recommendation template found. Skipping generating personalized recommendations");
        return;
      }
      
      recommendationData = recommendationTemplate.getTitle().split(";");
      if (recommendationData.length != 2) {
        log.warning("Invalid format of the recommendation template. "
            + "The title property should have two parts separated by a semicolon. "
            + "Skipping generating personalized recommendations.");

        return;
      }

      for (int i = 0; i < 2; i++) {
        Recommendation r = new Recommendation();
        r.setTitle(recommendationData[0]);
        r.setDescription(String.format(
            recommendationTemplate.getDescription(), 110 + random.nextInt(90),
            80 + random.nextInt(20)));
        r.setImageUrl(String.format(recommendationTemplate.getImageUrl(), 3 + random.nextInt(6)));

        // set recommendation expiration
        Calendar expirationTime = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        expirationTime.add(Calendar.MINUTE, RECOMMENDATION_EXPIRATION_IN_MINUTES);
        r.setExpiration(expirationTime.getTime());

        mgr.persist(r);
      }
    } catch (IllegalFormatException e) {
      log.warning("IllegalFormatException caught. This indicates that the format of the "
          + "recommendation template is invalid. "
          + "Skipping generating personalized recommendations");
      return;
    } finally {
      mgr.close();
    }

    long numberOfItemsWithReducedPrices = 2;
    String firstItem = recommendationData[1];

    try {
      HashMap<String, String> payload = new HashMap<String, String>();
      payload.put("NotificationKind", "PriceCheckLowerPrices1");
      payload.put("ProductCount", Long.toString(numberOfItemsWithReducedPrices));
      payload.put("ProductName", firstItem);

      PushNotificationHelper.sendMessageToUserDevice(userEmail, payload);
    } catch (IOException e) {
      log.info("Exception when sending push notification for user " + userEmail
          + " checked into a place " + placeId);
    }
  }

  private static EntityManager getEntityManager() {
    return EMF.get().createEntityManager();
  }
}
