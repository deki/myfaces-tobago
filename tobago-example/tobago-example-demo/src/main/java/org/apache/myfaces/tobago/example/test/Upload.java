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

package org.apache.myfaces.tobago.example.test;

import org.apache.myfaces.tobago.internal.util.PartUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.servlet.http.Part;
import java.io.Serializable;

public class Upload implements Serializable {

  private static final Logger LOG = LoggerFactory.getLogger(Upload.class);

  private Part part;

  public String upload() {
    if (part == null) {
      FacesContext.getCurrentInstance().addMessage(
          null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "No UploadItem found!", null));
      return null;
    }
    LOG.info("type=" + part.getContentType());
    LOG.info("size=" + part.getSize());
    final String name = PartUtils.getSubmittedFileName(part);
    LOG.info("name=" + name);
    FacesContext.getCurrentInstance().addMessage(
        null, new FacesMessage(FacesMessage.SEVERITY_INFO, "File was uploaded: " + name, null));

    return "/test/file/file.xhtml";
  }

  public Part getFile() {
    return part;
  }

  public void setFile(final Part file) {
    this.part = file;
  }
}
