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

package org.apache.myfaces.tobago.taglib.component;

import org.apache.myfaces.tobago.component.ComponentUtil;
import static org.apache.myfaces.tobago.TobagoConstants.ATTR_VALUE;

import javax.faces.component.UISelectItems;
import javax.faces.component.UIComponent;

/*
 * User: bommel
 * Date: Feb 27, 2007
 * Time: 9:16:41 PM
 */
public class SelectItemsTag extends TobagoTag implements SelectItemsTagDeclaration {

  public String getComponentType() {
    return UISelectItems.COMPONENT_TYPE;
  }

  public String getRendererType() {
    return null;
  }

  private String value;

  @Override
  protected void setProperties(UIComponent component) {
    super.setProperties(component);
    ComponentUtil.setStringProperty(component, ATTR_VALUE, value);
  }

  @Override
  public void release() {
    super.release();
    this.value = null;
  }

  public String getValue() {
    return value;
  }

  public void setValue(String value) {
    this.value = value;
  }

}