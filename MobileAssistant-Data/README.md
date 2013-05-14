Mobile Shopping Assistant Data Import Files
===========================================


##Copyright
Copyright 2013 Google Inc. All Rights Reserved.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

[http://www.apache.org/licenses/LICENSE-2.0](http://www.apache.org/licenses/LICENSE-2.0)

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.


## Disclaimer
This sample application is not an official Google product.


## Overview
The files in this directory can be used to populate the data for the Mobile Shopping Assistant.
Without any data the sample is not really useful.

## Prerequisite
[Google App Engine SDK for Python](https://developers.google.com/appengine/downloads) installed and symlinks created.

## Usage
1. Edit all csv files and insert sample data using the format defined in these files.

2. The backend API returns the list of places within not more than 100 kms from a specified location. Client apps by default retrieve places that are within 50 km from the current location. So when entering locations in places.csv make sure to use locations (latitude and longitude) that are within the above range from your location.

3. Make sure to put valid URLs to images representing offers and product recommendations.
   Otherwise the app will display the default icon instead.

4. Import the files:

        appcfg.py upload_data --config_file bulkloader.yaml --url=http://<url_of_mobile_shopping_assistant_backed_app>/remote_api --filename places.csv --kind=Place -e admin_email_address
        appcfg.py upload_data --config_file bulkloader.yaml --url=http://<url_of_mobile_shopping_assistant_backed_app>/remote_api --filename recommendations.csv --kind=Recommendation -e admin_email_address
        appcfg.py upload_data --config_file bulkloader.yaml --url=http://<url_of_mobile_shopping_assistant_backed_app>/remote_api --filename offers.csv --kind=Offer -e admin_email_address

5. The application uses geo-proximity search to return the list of nearby places. This is done by using Google App Engine Search API. By default the application rebuilds the search index at 5 am New York time (it is configured in cron.xml). To avoid having to wait until the index is rebuilt on that schedule, you can request the index to be rebuilt on demand. To do that, open your browser and navigate to: http://<url_of_mobile_shopping_assistant_backed_app>/admin/buildsearchindex .
You need to be signed in as an administrator. When the index is rebuilt, the browser will display a message: "MaintenanceTasks completed".

