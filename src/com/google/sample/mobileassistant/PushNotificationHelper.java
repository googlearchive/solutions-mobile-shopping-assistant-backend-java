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

import com.google.android.gcm.server.*;
import com.google.android.gcm.server.Message.Builder;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Logger;

import javax.persistence.EntityManager;
import javax.persistence.Query;

/**
 * Helper class for sending push notification using Google Cloud Messaging.
 */
class PushNotificationHelper {
  private static final DeviceInfoEndpoint endpoint = new DeviceInfoEndpoint();
  private static final Logger log = Logger.getLogger(PushNotificationHelper.class.getName());

  /**
   * Sends a message to a device registered for a given user using GCM (Google Cloud Messaging).
   *
   * @param userId user to whom the message will be sent.
   * @param payload the key-value payload to be sent.
   * @throws IOException if sending the message failed.
   */
  @SuppressWarnings("unchecked")
  public static void sendMessageToUserDevice(String userId, HashMap<String, String> payload)
      throws IOException {
    Sender sender = new Sender(ApiKeysAndIds.GCM_API_KEY);

    // TODO(user): Send notification to the specific user
    EntityManager mgr = getEntityManager();
    List<DeviceInfo> devices = new ArrayList<DeviceInfo>();
    try {
      Query query = mgr.createQuery("select from DeviceInfo as DeviceInfo");
      for (Object obj : (List<Object>) query.getResultList()) {
        devices.add(((DeviceInfo) obj));
      }
    } finally {
      mgr.close();
    }

    for (DeviceInfo deviceInfo : devices) {
      doSendViaGcm(payload, sender, deviceInfo);
    }
  }

  /**
   * Sends the payload to the registered device using GCM (Google Cloud Messaging).
   *
   * @param payload the key-value payload to be sent to the device.
   * @param sender the Sender object to be used for sending the payload.
   * @param deviceInfo the registration id of the device.
   * @return Result the result of the send.
   * @throws IOException if sending the payload failed.
   */
  private static Result doSendViaGcm(
      HashMap<String, String> payload, Sender sender, DeviceInfo deviceInfo) throws IOException {
    Builder builder = new Message.Builder();

    for (String key : payload.keySet()) {
      builder.addData(key, payload.get(key));
    }

    Message msg = builder.build();
    Result result = doSendViaGcm(msg, sender, deviceInfo);

    return result;
  }

  /**
   * Sends the message to the registered device using GCM (Google Cloud Messaging).
   *
   * @param message the message to be sent in the GCM ping to the device.
   * @param sender the Sender object to be used for sending the payload,
   * @param deviceInfo the registration id of the device.
   * @return Result the result of the send.
   * @throws IOException if sending the message failed.
   */
  private static Result doSendViaGcm(Message message, Sender sender, DeviceInfo deviceInfo)
      throws IOException {
    log.info("Sending a push notification");

    Result result = sender.send(message, deviceInfo.getDeviceRegistrationID(), 5);

    if (result.getMessageId() != null) {
      log.info("Push notification sent");

      String canonicalRegId = result.getCanonicalRegistrationId();
      if (canonicalRegId != null) {
        endpoint.removeDeviceInfo(deviceInfo.getDeviceRegistrationID());
        deviceInfo.setDeviceRegistrationID(canonicalRegId);
        endpoint.insertDeviceInfo(deviceInfo);
      }
    } else {
      String error = result.getErrorCodeName();

      log.info("Push notification failed with error:" + error);

      if (error.equals(Constants.ERROR_NOT_REGISTERED)) {
        endpoint.removeDeviceInfo(deviceInfo.getDeviceRegistrationID());
      }
    }

    return result;
  }

  private static EntityManager getEntityManager() {
    return EMF.get().createEntityManager();
  }
}
