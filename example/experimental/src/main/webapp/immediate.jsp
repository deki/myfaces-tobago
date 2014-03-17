<%--
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
--%>
<%@ taglib uri="http://myfaces.apache.org/tobago/component" prefix="tc" %>
<%@ taglib uri="http://myfaces.apache.org/tobago/extension" prefix="tx" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<f:view>
  <tc:page id="page" width="300px" height="300px">
    <f:facet name="layout">
      <tc:gridLayout rows="fixed;*;*;fixed"/>
    </f:facet>

    <tc:in value="#{immediate.in}" required="true"/>
    <tc:selectOneListbox value="#{immediate.selectOne}">
      <tc:selectItems value="#{immediate.selectItems}"/>
    </tc:selectOneListbox>
    <tc:selectManyListbox value="#{immediate.selectMany}">
      <tc:selectItems value="#{immediate.selectItems}"/>
    </tc:selectManyListbox>

    <tc:panel>
      <f:facet name="layout">
        <tc:gridLayout columns="*;*"/>
      </f:facet>

      <tc:button label="immediate" immediate="true"/>
      <tc:button label="normal"/>
    </tc:panel>

  </tc:page>
</f:view>