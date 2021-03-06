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

package org.apache.myfaces.tobago.internal.taglib.component;

import org.apache.myfaces.tobago.apt.annotation.Tag;
import org.apache.myfaces.tobago.apt.annotation.TagAttribute;
import org.apache.myfaces.tobago.apt.annotation.UIComponentTag;
import org.apache.myfaces.tobago.apt.annotation.UIComponentTagAttribute;
import org.apache.myfaces.tobago.component.RendererTypes;
import org.apache.myfaces.tobago.internal.taglib.declaration.HasAction;
import org.apache.myfaces.tobago.internal.taglib.declaration.HasActionListener;
import org.apache.myfaces.tobago.internal.taglib.declaration.HasConfirmation;
import org.apache.myfaces.tobago.internal.taglib.declaration.HasFragment;
import org.apache.myfaces.tobago.internal.taglib.declaration.HasIdBindingAndRendered;
import org.apache.myfaces.tobago.internal.taglib.declaration.HasLink;
import org.apache.myfaces.tobago.internal.taglib.declaration.HasOutcome;
import org.apache.myfaces.tobago.internal.taglib.declaration.HasTarget;
import org.apache.myfaces.tobago.internal.taglib.declaration.IsDisabledBySecurity;
import org.apache.myfaces.tobago.internal.taglib.declaration.IsImmediateCommand;
import org.apache.myfaces.tobago.internal.taglib.declaration.IsOmit;
import org.apache.myfaces.tobago.internal.taglib.declaration.IsTransition;

import javax.el.ValueExpression;
import javax.faces.component.UICommand;

/**
 * Add an event behavior to the component.
 */
@Tag(name = "event")
@UIComponentTag(uiComponent = "org.apache.myfaces.tobago.component.UIEvent",
    uiComponentBaseClass = "org.apache.myfaces.tobago.internal.component.AbstractUIEvent",
    uiComponentFacesClass = "javax.faces.component.UICommand",
    componentFamily = UICommand.COMPONENT_FAMILY,
    rendererType = RendererTypes.EVENT,
    interfaces = {
        // As long as no behavior event names are defined, ClientBehaviorHolder must be implemented for Majorra.
        "javax.faces.component.behavior.ClientBehaviorHolder"
    },
    faceletHandler = "org.apache.myfaces.tobago.facelets.EventHandler")
public interface EventTagDeclaration
    extends HasIdBindingAndRendered, HasAction, HasActionListener, IsImmediateCommand, HasConfirmation,
    HasLink, HasOutcome, HasFragment, IsTransition, HasTarget, IsDisabledBySecurity, IsOmit {

  /**
   * The name of the event as an instance of {@link org.apache.myfaces.tobago.component.ClientBehaviors}
   */
  @TagAttribute
  @UIComponentTagAttribute(type = "org.apache.myfaces.tobago.component.ClientBehaviors")
  void setEvent(final ValueExpression event);

}
