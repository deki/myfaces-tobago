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

package org.apache.myfaces.tobago.renderkit.html.standard.standard.tag;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.myfaces.tobago.renderkit.RendererBase;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import java.io.IOException;

public class SubviewRenderer extends RendererBase {

    private static final Logger LOG = LoggerFactory.getLogger(SubviewRenderer.class);

  @Override
  public void encodeBegin(final FacesContext facesContext, final UIComponent component)
      throws IOException {
    if (LOG.isInfoEnabled()) {
      LOG.info("SSSSSSSSSSSSSSSSSSS Subview component = " + component.getClass().getName());
    }
    super.encodeBegin(facesContext, component);
  }

  @Override
  public void encodeEnd(final FacesContext facesContext,
                        final UIComponent component) throws IOException {

  }

}

