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

import org.apache.myfaces.tobago.apt.annotation.BodyContentDescription;
import org.apache.myfaces.tobago.apt.annotation.Preliminary;
import org.apache.myfaces.tobago.apt.annotation.Tag;
import org.apache.myfaces.tobago.apt.annotation.TagAttribute;
import org.apache.myfaces.tobago.apt.annotation.UIComponentTag;
import org.apache.myfaces.tobago.apt.annotation.UIComponentTagAttribute;
import org.apache.myfaces.tobago.taglib.decl.HasActionListener;
import org.apache.myfaces.tobago.taglib.decl.HasIdBindingAndRendered;
import org.apache.myfaces.tobago.taglib.decl.HasIdReference;
import org.apache.myfaces.tobago.taglib.decl.HasNameReference;
import org.apache.myfaces.tobago.taglib.decl.HasState;
import org.apache.myfaces.tobago.taglib.decl.HasTreeNodeValue;
import org.apache.myfaces.tobago.taglib.decl.IsRequired;

/**
 * Created by IntelliJ IDEA.
 * User: bommel
 * Date: 11.02.2006
 * Time: 13:28:14
 * To change this template use File | Settings | File Templates.
 */
/**
 * Renders a tree view.
 */
@Tag(name = "tree")
@BodyContentDescription(anyTagOf = "<f:facet>* <f:actionListener>?")
@Preliminary(
    "Implement a var attribute for the tree like in the sheet (http://issues.apache.org/jira/browse/MYFACES-903)")
@UIComponentTag(
    uiComponent = "org.apache.myfaces.tobago.component.UITree",
    rendererType = "Tree")
public interface TreeTagDeclaration extends TobagoTagDeclaration,
    HasIdBindingAndRendered, HasTreeNodeValue, HasState,
    HasIdReference, HasActionListener, HasNameReference, IsRequired {

  /**
   * Flag indicating whether or not this component should be render selectable items.
   * Possible values are:
   * <ul>
   * <li><strong>multi</strong> : a multisection tree is rendered
   * <li><strong>single</strong> : a singlesection tree is rendered
   * <li><strong>multiLeafOnly</strong> : a multisection tree is rendered,
   * only Leaf's are selectable
   * <li><strong>singleLeafOnly</strong> : a singlesection tree is rendered,
   * only Leaf's are selectable
   * </ul>
   * For any other value or if this attribute is omited the items are not selectable.
   */
  @TagAttribute
  @UIComponentTagAttribute(defaultValue = "off",
      allowedValues = {"multi", "single", "multiLeafOnly", "singleLeafOnly", "off"})
  void setSelectable(String selectable);

  /**
   *
   */
  @TagAttribute
  @UIComponentTagAttribute(type = "java.lang.Boolean",
      defaultValue = "false")
  void setMutable(String mutable);

  /**
   *
   */
  @TagAttribute
  @UIComponentTagAttribute(type = "java.lang.Boolean",
      defaultValue = "false")
  void setShowRootJunction(String showRootJunction);

  /**
   *
   */
  @TagAttribute
  @UIComponentTagAttribute(type = "java.lang.Boolean",
      defaultValue = "false")
  void setShowIcons(String showIcons);

  /**
   *
   */
  @TagAttribute
  @UIComponentTagAttribute(type = "java.lang.Boolean",
      defaultValue = "false")
  void setShowJunctions(String showJunctions);

  /**
   *
   */
  @TagAttribute
  @UIComponentTagAttribute(type = "java.lang.Boolean",
      defaultValue = "false")
  void setShowRoot(String showRoot);

}
