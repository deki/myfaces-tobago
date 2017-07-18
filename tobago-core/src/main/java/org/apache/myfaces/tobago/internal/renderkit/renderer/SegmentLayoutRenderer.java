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

import org.apache.myfaces.tobago.component.LabelLayout;
import org.apache.myfaces.tobago.component.SupportsLabelLayout;
import org.apache.myfaces.tobago.component.UISegmentLayout;
import org.apache.myfaces.tobago.internal.component.AbstractUISegmentLayout;
import org.apache.myfaces.tobago.internal.util.RenderUtils;
import org.apache.myfaces.tobago.renderkit.RendererBase;
import org.apache.myfaces.tobago.renderkit.css.BootstrapClass;
import org.apache.myfaces.tobago.renderkit.css.TobagoClass;
import org.apache.myfaces.tobago.renderkit.html.HtmlElements;
import org.apache.myfaces.tobago.util.ComponentUtils;
import org.apache.myfaces.tobago.webapp.TobagoResponseWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import java.io.IOException;
import java.util.List;

/**
 * Renders the 12 columns grid layout.
 */
public class SegmentLayoutRenderer extends RendererBase {

  private static final Logger LOG = LoggerFactory.getLogger(SegmentLayoutRenderer.class);

  @Override
  public boolean getRendersChildren() {
    return true;
  }

  @Override
  public void encodeBegin(final FacesContext facesContext, final UIComponent component) throws IOException {
    final AbstractUISegmentLayout layout = (AbstractUISegmentLayout) component;
    final TobagoResponseWriter writer = getResponseWriter(facesContext);

    writer.startElement(HtmlElements.DIV);
//    writer.writeClassAttribute(BootstrapClass.FORM_HORIZONTAL, BootstrapClass.CONTAINER_FLUID);
    writer.writeClassAttribute(
        TobagoClass.SEGMENT_LAYOUT,
        BootstrapClass.ROW,
        TobagoClass.SEGMENT_LAYOUT.createMarkup(ComponentUtils.updateMarkup(layout, layout.getMarkup())));
//    writer.writeClassAttribute(Classes.create(layout), BootstrapClass.FORM_GROUP);
    writer.writeIdAttribute(layout.getClientId(facesContext));
  }

  @Override
  public void encodeChildren(final FacesContext facesContext, final UIComponent component) throws IOException {
    final TobagoResponseWriter writer = getResponseWriter(facesContext);
    final UISegmentLayout segmentLayout = (UISegmentLayout) component;

    if (!segmentLayout.isRendered()) {
      return;
    }

    final List<UIComponent> children = segmentLayout.getChildren();
    final BootstrapClass.Generator generator = new BootstrapClass.Generator(
        segmentLayout.getExtraSmall(),
        segmentLayout.getSmall(),
        segmentLayout.getMedium(),
        segmentLayout.getLarge());
    for (UIComponent child : children) {
      if (child.isRendered()) {
        encodeChild(facesContext, writer, generator, child);
      }
    }
  }

  private void encodeChild(
      final FacesContext facesContext, final TobagoResponseWriter writer,
      final BootstrapClass.Generator generator, final UIComponent child) throws IOException {

    if (child instanceof SupportsLabelLayout
        && LabelLayout.isSegment(((SupportsLabelLayout) child).getLabelLayout())) {

      // left part
      LabelLayout.setSegment(facesContext, LabelLayout.segmentLeft);
      encodeDiv(facesContext, writer, generator, child);
      generator.next();

      // right part
      LabelLayout.setSegment(facesContext, LabelLayout.segmentRight);
      encodeDiv(facesContext, writer, generator, child);
      generator.next();

      LabelLayout.removeSegment(facesContext);
    } else { // normal case
      encodeDiv(facesContext, writer, generator, child);
      generator.next();
    }
  }

  private void encodeDiv(
      FacesContext facesContext, TobagoResponseWriter writer, BootstrapClass.Generator generator, UIComponent child)
      throws IOException {
    writer.startElement(HtmlElements.DIV);
    writer.writeClassAttribute(null, null, generator.generate(child));
    RenderUtils.encode(facesContext, child);
    writer.endElement(HtmlElements.DIV);
  }

  @Override
  public void encodeEnd(final FacesContext facesContext, final UIComponent component) throws IOException {
    final TobagoResponseWriter writer = getResponseWriter(facesContext);
    writer.endElement(HtmlElements.DIV);
  }
}