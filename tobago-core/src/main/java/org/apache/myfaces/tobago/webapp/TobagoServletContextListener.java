package org.apache.myfaces.tobago.webapp;

/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import org.apache.myfaces.tobago.config.TobagoConfig;
import org.apache.myfaces.tobago.context.ResourceManagerFactory;
import org.apache.myfaces.tobago.internal.config.TobagoConfigBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

public class TobagoServletContextListener implements ServletContextListener {

  private static final Logger LOG = LoggerFactory.getLogger(TobagoServletContextListener.class);

  public void contextInitialized(ServletContextEvent event) {

    if (LOG.isInfoEnabled()) {
      LOG.info("*** contextInitialized ***");
    }

    ServletContext servletContext = event.getServletContext();

    if (servletContext.getAttribute(TobagoConfig.TOBAGO_CONFIG) != null) {
      LOG.warn("Tobago has been already initialized. Do nothing.");
      return;
    }

    TobagoConfigBuilder.init(servletContext);
  }

  public void contextDestroyed(ServletContextEvent event) {
    if (LOG.isInfoEnabled()) {
      LOG.info("*** contextDestroyed ***\n--- snip ---------"
          + "--------------------------------------------------------------");
    }

    ServletContext servletContext = event.getServletContext();

    servletContext.removeAttribute(TobagoConfig.TOBAGO_CONFIG);

    ResourceManagerFactory.release(servletContext);

    //LogFactory.releaseAll();
//    LogManager.shutdown();
  }

}
