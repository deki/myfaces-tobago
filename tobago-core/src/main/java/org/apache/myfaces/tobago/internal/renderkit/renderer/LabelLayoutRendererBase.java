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

package org.apache.myfaces.tobago.internal.renderkit.renderer;

import org.apache.myfaces.tobago.component.Attributes;
import org.apache.myfaces.tobago.component.LabelLayout;
import org.apache.myfaces.tobago.component.SupportsAccessKey;
import org.apache.myfaces.tobago.component.SupportsLabelLayout;
import org.apache.myfaces.tobago.context.Markup;
import org.apache.myfaces.tobago.internal.component.AbstractUIStyle;
import org.apache.myfaces.tobago.internal.util.HtmlRendererUtils;
import org.apache.myfaces.tobago.internal.util.JsonUtils;
import org.apache.myfaces.tobago.internal.util.StringUtils;
import org.apache.myfaces.tobago.renderkit.LabelWithAccessKey;
import org.apache.myfaces.tobago.renderkit.css.BootstrapClass;
import org.apache.myfaces.tobago.renderkit.css.TobagoClass;
import org.apache.myfaces.tobago.renderkit.html.DataAttributes;
import org.apache.myfaces.tobago.renderkit.html.HtmlAttributes;
import org.apache.myfaces.tobago.renderkit.html.HtmlElements;
import org.apache.myfaces.tobago.util.ComponentUtils;
import org.apache.myfaces.tobago.webapp.TobagoResponseWriter;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import java.io.IOException;
import java.util.List;

/**
 * Manages the rendering of the <b>label</b> and the <b>field</b> together with different possibilities for
 * the position of the label (defined by {@link org.apache.myfaces.tobago.component.Attributes#labelLayout}
 */
public abstract class LabelLayoutRendererBase extends DecodingInputRendererBase {

  @Override
  public void encodeBegin(final FacesContext facesContext, final UIComponent component) throws IOException {

    encodeBeginSurroundingLabel(facesContext, component);

    switch (getType(component)) {
      case segmentLeft:
        if (LabelLayout.getSegment(facesContext) == LabelLayout.segmentRight) {
          encodeBeginMessageField(facesContext, component);
        }
        break;
      case segmentRight:
        if (LabelLayout.getSegment(facesContext) == LabelLayout.segmentLeft) {
          encodeBeginMessageField(facesContext, component);
        }
        break;
      default:
        encodeBeginMessageField(facesContext, component);
        break;
    }
  }

  @Override
  public void encodeEnd(final FacesContext facesContext, final UIComponent component) throws IOException {

    switch (getType(component)) {
      case segmentLeft:
        if (LabelLayout.getSegment(facesContext) == LabelLayout.segmentRight) {
          encodeEndMessageField(facesContext, component);
        }
        break;
      case segmentRight:
        if (LabelLayout.getSegment(facesContext) == LabelLayout.segmentLeft) {
          encodeEndMessageField(facesContext, component);
        }
        break;
      default:
        encodeEndMessageField(facesContext, component);
        break;
    }

    // render the styles here, because inside of <select> its not possible.
    if (component.getRendersChildren()) {
      final List<AbstractUIStyle> children = ComponentUtils.findDescendantList(component, AbstractUIStyle.class);
      for (final AbstractUIStyle child : children) {
        child.encodeAll(facesContext);
      }
    }

    encodeEndSurroundingLabel(facesContext, component);
  }

  @Override
  public void encodeChildren(final FacesContext context, final UIComponent component) throws IOException {
    if (component.getChildCount() > 0) {
      for (int i = 0, childCount = component.getChildCount(); i < childCount; i++) {
        final UIComponent child = component.getChildren().get(i);
        if (!child.isRendered()) {
          continue;
        }
        if (child instanceof AbstractUIStyle) {
          // will be rendered in {@link encodeEnd}
          continue;
        }

        child.encodeAll(context);
      }
    }
  }

  protected abstract void encodeBeginMessageField(FacesContext facesContext, UIComponent component) throws IOException;

  protected abstract void encodeEndMessageField(FacesContext facesContext, UIComponent component) throws IOException;

  protected void encodeBeginSurroundingLabel(final FacesContext facesContext, final UIComponent component)
      throws IOException {

    final TobagoResponseWriter writer = getResponseWriter(facesContext);
    String clientId = component.getClientId(facesContext);
    final Markup markup = (Markup) ComponentUtils.getAttribute(component, Attributes.markup);

    // possible values:
    // - none
    // - flexLeft (default)
    // - flexRight
    // - top
    // - segmentLeft (todo)
    // - segmentRight (todo)
    // - flowLeft (todo)
    // - flowRight (todo)
    // - skip
    final LabelLayout labelLayout = getType(component);
    final boolean flex;
    switch (labelLayout) {
      case skip:
        return;
      case flexLeft:
      case flexRight:
        flex = true;
        break;
      case segmentLeft:
        if (LabelLayout.getSegment(facesContext) == LabelLayout.segmentLeft) {
          clientId += ComponentUtils.SUB_SEPARATOR + "label";
        }
        flex = false;
        break;
      case segmentRight:
        if (LabelLayout.getSegment(facesContext) == LabelLayout.segmentRight) {
          clientId += ComponentUtils.SUB_SEPARATOR + "label";
        }
        flex = false;
        break;
      case none:
      case top:
      case flowLeft:
      case flowRight:
      default: // none, top, segmentLeft, segmentRight, flowLeft, flowRight
        flex = false;
    }

    writer.startElement(HtmlElements.DIV);
    writer.writeIdAttribute(clientId);
    writer.writeAttribute(DataAttributes.MARKUP, JsonUtils.encode(markup), false);

    writer.writeClassAttribute(
        flex ? TobagoClass.FLEX_LAYOUT : null,
        flex ? BootstrapClass.D_FLEX : null,
        TobagoClass.LABEL__CONTAINER,
        BootstrapClass.FORM_GROUP,
        ComponentUtils.getBooleanAttribute(component, Attributes.required) ? TobagoClass.REQUIRED : null);

    switch (labelLayout) {
      case none:
        break;
      case flexRight:
      case flowRight:
        break;
      case segmentLeft:
        if (LabelLayout.getSegment(facesContext) == LabelLayout.segmentLeft) {
          encodeLabel(facesContext, component, writer, labelLayout);
        }
        break;
      case segmentRight:
        if (LabelLayout.getSegment(facesContext) == LabelLayout.segmentRight) {
          encodeLabel(facesContext, component, writer, labelLayout);
        }
        break;
      default:
        encodeLabel(facesContext, component, writer, labelLayout);
    }
  }

  protected void encodeEndSurroundingLabel(final FacesContext facesContext, final UIComponent component)
      throws IOException {

    final TobagoResponseWriter writer = getResponseWriter(facesContext);
    final LabelLayout labelLayout = getType(component);

    switch (labelLayout) {
      case skip:
        return;
      case none:
        break;
      case flexRight:
      case flowRight:
        encodeLabel(facesContext, component, writer, labelLayout);
        break;
      default:
        // nothing to do
    }

    writer.endElement(HtmlElements.DIV);
  }

  protected void encodeLabel(final FacesContext facesContext, final UIComponent component,
      final TobagoResponseWriter writer, final LabelLayout labelLayout)
      throws IOException {
    // TBD: maybe use an interface for getLabel()
    final String label = ComponentUtils.getStringAttribute(component, Attributes.label);
    if (StringUtils.isNotBlank(label)) {
      writer.startElement(HtmlElements.LABEL);
      writer.writeAttribute(HtmlAttributes.FOR, getFieldId(facesContext, component), false);
      writer.writeClassAttribute(TobagoClass.LABEL, BootstrapClass.COL_FORM_LABEL);
      if (component instanceof SupportsAccessKey) {
        final LabelWithAccessKey labelWithAccessKey = new LabelWithAccessKey((SupportsAccessKey) component);
        HtmlRendererUtils.writeLabelWithAccessKey(writer, labelWithAccessKey);
      } else {
        writer.writeText(label);
      }
      writer.endElement(HtmlElements.LABEL);
    }
  }

  private LabelLayout getType(final UIComponent component) {
    return component instanceof SupportsLabelLayout
        ? ((SupportsLabelLayout) component).getLabelLayout()
        : LabelLayout.skip;
  }

  protected abstract String getFieldId(final FacesContext facesContext, final UIComponent component);
}
