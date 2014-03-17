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

package org.apache.myfaces.tobago.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public final class Deprecation {

  // to prevent instantiation
  private Deprecation() {
  }

  /**
   * This Log object should help to detect the usage of deprecated code.
   * The main reason for this class is the lack of a "deprecated concept"
   * for tag libraries. Thought the designer of a Tobago page cannot see
   * in his IDE that a tag or attribute is deprecated.
   * <p>
   * The Tobago Java code will log into this Log object, with
   * <dl>
   *   <dt><code>error</code></dt>
   *   <dd>when the code is deprecated with a loss of function, or</dd>
   *   <dt><code>warn</code></dt>
   *   <dd>when the code is deprecated, but still works.</dd>
   * </dl>
   * <p>
   * This Log category can be switched off, in production environment without
   * affecting the normal logging category.
   */
  public static final Log LOG = LogFactory.getLog(Deprecation.class);

}