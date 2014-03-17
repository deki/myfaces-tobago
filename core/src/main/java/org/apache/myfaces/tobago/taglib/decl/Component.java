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

package org.apache.myfaces.tobago.taglib.decl;

import org.apache.myfaces.tobago.apt.annotation.TagAttribute;

import javax.servlet.jsp.JspException;

/**
 * Created: Apr 9, 2005 2:36:06 PM
 * User: bommel
 * $Id$
 */
public interface Component {

  @TagAttribute
  void setId(String s);

  /**
   * Flag indicating whether or not this component should be rendered
   * (during Render Response Phase), or processed on any subsequent form submit.
   *
   * @param s
   */

  @TagAttribute
  void setRendered(String s);

  /**
   * The value binding expression linking this component to a property
   * in a backing bean
   *
   * @param binding
   * @throws JspException
   */

  @TagAttribute
  void setBinding(String binding) throws JspException;
}