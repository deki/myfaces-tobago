package org.apache.myfaces.tobago.taglib.component;

/*
 * Copyright 2002-2005 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/*
  * Created 14.09.2004 at 12:03:45.
  * $Id$
  */

import org.apache.myfaces.tobago.apt.annotation.BodyContent;
import org.apache.myfaces.tobago.apt.annotation.Tag;
import org.apache.myfaces.tobago.apt.annotation.UIComponentTag;
import org.apache.myfaces.tobago.component.UIMenuSeparator;
import org.apache.myfaces.tobago.taglib.decl.HasBinding;
import org.apache.myfaces.tobago.taglib.decl.IsRendered;

import javax.faces.component.UIComponent;



public class MenuSeparatorTag extends TobagoTag
    implements MenuSeparatorTagDeclaration {

  //public static final String MENU_TYPE = "menuSeparator";

  protected void setProperties(UIComponent component) {
    super.setProperties(component);
    component.setRendererType(null);
    //ComponentUtil.setStringProperty(component, ATTR_COMMAND_TYPE, MENU_TYPE);
  }

  public String getComponentType() {
    return UIMenuSeparator.COMPONENT_TYPE;
  }
}
