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

package org.apache.myfaces.tobago.example.demo;

import org.apache.commons.lang3.StringUtils;

import javax.faces.application.NavigationHandler;
import javax.faces.application.ViewHandler;
import javax.faces.component.UIViewRoot;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import java.io.IOException;
import java.util.Collections;

public class PrettyUrlForMenuNavigationHandler extends NavigationHandler {

  private NavigationHandler navigationHandler;

  public PrettyUrlForMenuNavigationHandler(final NavigationHandler navigationHandler) {
    this.navigationHandler = navigationHandler;
  }

  @Override
  public void handleNavigation(final FacesContext facesContext, final String fromAction, final String outcome) {

    if (StringUtils.startsWith(outcome, "/content/")) {
      final ViewHandler viewHandler = facesContext.getApplication().getViewHandler();
      final UIViewRoot viewRoot = viewHandler.createView(facesContext, outcome);
      facesContext.setViewRoot(viewRoot);
      final ExternalContext externalContext = facesContext.getExternalContext();
      try {
        externalContext.redirect(
            externalContext.encodeRedirectURL(externalContext.getRequestContextPath() + outcome,
            Collections.emptyMap()));
      } catch (final IOException e) {
        // not nice?
        facesContext.renderResponse();
      }
    } else {
      navigationHandler.handleNavigation(facesContext, fromAction, outcome);
    }
  }
}
