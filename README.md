# Mobile Shopping Assistant Java Backend

## Copyright
Copyright 2013 Google Inc. All Rights Reserved.

Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at

[http://www.apache.org/licenses/LICENSE-2.0](http://www.apache.org/licenses/LICENSE-2.0)

Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.

## Disclaimer
This sample application is not an official Google product.

## Supported Platform and Versions
This sample source code and project is designed to work with Eclipse. It was tested with Eclipse 3.8 and Google App Engine SDK 1.7.7.1.

## Overview
[Mobile Shopping Assistant Java Backend](https://github.com/GoogleCloudPlatform/solutions-mobile-shopping-assistant) demonstrates how to build a mobile backend that will power Android and iOS native applications and expose API using Google Cloud Endpoints.

### Prerequisite
1. Eclipse with [Google Plugin for Eclipse](https://developers.google.com/eclipse/docs/getting_started) version 3.2.3 or newer.

2. Google App Engine SDK version 1.7.7.1 or higher.


## Developer Guide using Eclipse

The repository does not include jars. Therefore when you open the project in Eclipse you will initially see errors. After following the steps below, all errors should be gone.

1. Follow [Android documentation](http://developer.android.com/google/gcm/gs.html) to install the helper libraries and to copy the gcm-server.jar into war/WEB-INF/lib directory.

2. If you see errors about App Engine not being configured correctly, go to Project Properties->Google->App Engine and unselect 'Use Google App Engine' and click OK. Then go to the same dialog again, select 'Use Google App Engine' and make sure that the selected version of SDK is 1.7.7.1 or newer.

3. Open war/WEB-INF/appengine-web.xml and enter your app id within application XML element.

4. Open MobileAssistant-AppEngine/src/com.google.sample.mobileassistant/ApiKeysAndIds.java and enter your *GCM_API_KEY*, *ANDROID_CLIENT_ID*, *IOS_CLIENT_ID*, *WEB_CLIENT_ID* and *AUDIENCE*. Follow [Android documentation](http://developer.android.com/google/gcm/gs.html) to obtain *GCM_API_KEY* (Note: In the 'Enabling the GCM Service' step choose 'Google Cloud Messaging for Android') and follow the [Cloud Endpoints documentation](https://developers.google.com/appengine/docs/java/endpoints/auth#creating-client-id) to obtain the 3 Client IDs. For Android Client ID the package name is 'com.google.sample.mobileassistant'. For iOS Client ID the bundle is 'com.google.sample.mobileassistant'.

5. Select MobileAssistant-AppEngine project in "Project Explorer" and from Google context menu select Generate Cloud Endpoint client library.

6. Select MobileAssistant-AppEngine project and from Google context menu select Deploy to App Engine.

7. Follow steps in README.md in MobileAssistant-Data directory to import sample data to the deployed backend.



Now you are ready to use Mobile Shopping Assistant Android client or iOS Client with the mobile backend that you deployed. Follow the steps in README.md for Android and iOS client.
