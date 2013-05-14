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

import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.users.User;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

/**
 * UserAccount entity.
 */
@Entity
public class UserAccount {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Key key;

  private String firstName;

  public String getFirstName() {
    return firstName;
  }

  public void setFirstName(String firstName) {
    this.firstName = firstName;
  }

  private String lastName;

  public String getName() {
    return lastName;
  }

  public void setName(String lastName) {
    this.lastName = lastName;
  }

  private String email;

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  static boolean isAdmin(User user) {
    return false;
  }

}
