/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.myfaces.tobago.internal.context;

public final class ImageCacheKey {
  private final ClientPropertiesKey clientPropertiesKey;
  private final String name;
  private final int hashCode;

  public ImageCacheKey(ClientPropertiesKey clientPropertiesKey, String name) {
    this.name = name;
    this.clientPropertiesKey = clientPropertiesKey;
    hashCode = calcHashCode();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    ImageCacheKey that = (ImageCacheKey) o;

    return clientPropertiesKey.equals(that.clientPropertiesKey) && name.equals(that.name);
  }

  private int calcHashCode() {
    int result;
    result = clientPropertiesKey.hashCode();
    result = 31 * result + name.hashCode();
    return result;
  }

  @Override
  public int hashCode() {
    return hashCode;
  }
}