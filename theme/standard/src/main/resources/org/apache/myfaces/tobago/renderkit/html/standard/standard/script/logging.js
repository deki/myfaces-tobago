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

var LOG = new Object();
Object.extend(LOG, {
  IdBase: "TbgLog",
  messages: new Array(),
  appenders: new Array(),
  maximumSeverity: 0,
  SEVERITY_ID_POSTFIX: "clientSeverity",
  DEBUG: 1,
  INFO:  2,
  WARN:  3,
  ERROR: 4,
  NONE: 100,

  show: function() {
    for (var i = 0 ; i < this.appenders.length; i++) {
      var appender = this.appenders[i];
      if (appender.show) {
        appender.show();
      }
    }
  },

  getMaximumSeverity: function() {
    return this.maximumSeverity;
  },

  addAppender: function(appender) {
    this.appenders.push(appender);
  },

  addMessage: function(msg) {
    if (this.maximumSeverity < msg.type) {
      this.maximumSeverity = msg.type;
    }
    this.messages.push(msg);
    for (var i = 0 ; i < this.appenders.length; i++) {
      var appender = this.appenders[i];
      if (appender.append
          && typeof(msg.type) == "number"
          && appender.logFor(msg.type)) {
        appender.append(msg);
      }
    }
  },

  debug: function(text) {
    this.addMessage(new LOG.LogMessage(LOG.DEBUG, text));
  },

  info  : function(text) {
    this.addMessage(new LOG.LogMessage(LOG.INFO, text));
  },

  warn: function(text) {
    this.addMessage(new LOG.LogMessage(LOG.WARN, text));
  },

  error: function(text) {
    this.show();
    this.addMessage(new LOG.LogMessage(LOG.ERROR, text));
  },

  bindOnWindow: function() {
    window.onerror = this.windowError.bind(this);
  },

  windowError: function(msg, url, line){
    var message = "Error in (" + (url || window.location) + ") on line "+ line +" with message (" + msg + ")";
    this.error(message);
  },

  listScriptFiles: function() {
    var children = document.getElementsByTagName('head')[0].childNodes;
    for (var i = 0; i < children.length; i++) {
      var child = children[i];
      if (child.tagName.toUpperCase() == "SCRIPT"){
        this.debug("script.src=" + child.src);
      }
    }
  },

  listThemeConfig : function() {
    for (var name in Tobago.Config) {
      if (typeof Tobago.Config[name] == 'object' && name != "fallbackNames") {
        for (var key in Tobago.Config[name]) {
          LOG.debug(name + "." + key + " = " + Tobago.Config[name][key]);
        }
      }
    }
  },

  debugAjaxComponents: function() {
     for (var name in Tobago.ajaxComponents) {
       var component = Tobago.ajaxComponents[name];
       if (typeof component == 'string') {
         LOG.debug("AjaxComponentId = " + name + " ContainerId = " + component);
       } else if ((typeof component == 'object') && component.tagName) {
         LOG.debug("AjaxComponentId = " + name + " ContainerType = " + component.tagName);
       } else if ((typeof component == 'object') && (typeof component.reloadWithAction == "function")) {
         LOG.debug("AjaxComponentId = " + name + " TobagoAjaxReloadable object");
       } else {
         LOG.debug("AjaxComponentId = " + name + " Unknown object");
       }
    }
  }
});

//LOG.bindOnWindow();


LOG.LogArea = Class.create();
LOG.LogArea.prototype = Object.extend(Draggable.prototype, {
  initialize: function() {
    var options = Object.extend({
      handle: false,
      starteffect: Prototype.emptyFunction,
      reverteffect: Prototype.emptyFunction,
      endeffect: Prototype.emptyFunction,
      zindex: 1000,
      revert: false,
      snap: false,   // false, or xy or [x,y] or function(x,y){ return [x,y] }

      // logging
      hide: false,
      severity: LOG.DEBUG
    }, arguments[0] || {});

    this.options = options;

    if (Tobago.element(LOG.IdBase)) {
      return;
    }

    this.element = document.createElement("DIV");
//    this.element.id = LOG.IdBase + "Element"
    this.setUpLogDiv(this.element, "30px", "2px", "400px", "500px", "1px solid red", "#aaaaaa");
    this.element.style.paddingTop = "25px";
    this.element.style.overflow = "hidden";
    this.element.style.zIndex = "9010";

    this.dragHandleTop = document.createElement("DIV");
    this.setUpHandleDiv(this.dragHandleTop, "0px", "0px", null, null, "100%", "25px", "2px outset gray", null, "move");
    this.element.appendChild(this.dragHandleTop);

    var tmpElement = document.createElement("SPAN");
    tmpElement.style.paddingLeft = "20px";
    tmpElement.innerHTML = "LoggingArea";
    this.dragHandleTop.appendChild(tmpElement);

    this.severitySelector = document.createElement("SELECT");
    var option = document.createElement("OPTION");
    option.value = LOG.DEBUG;
    option.innerHTML = "Debug";
    this.severitySelector.appendChild(option);
    option = document.createElement("OPTION");
    option.value = LOG.INFO;
    option.innerHTML = "Info";
    this.severitySelector.appendChild(option);
    option = document.createElement("OPTION");
    option.value = LOG.WARN;
    option.innerHTML = "Warn";
    this.severitySelector.appendChild(option);
    option = document.createElement("OPTION");
    option.value = LOG.ERROR;
    option.innerHTML = "Error";
    this.severitySelector.appendChild(option);
    option = document.createElement("OPTION");
    option.value = LOG.NONE;
    option.innerHTML = "None";
    this.severitySelector.appendChild(option);
    var sev = Tobago.element(Tobago.page.id + Tobago.SUB_COMPONENT_SEP + LOG.SEVERITY_ID_POSTFIX);
    if (sev) {
      this.severitySelector.value = sev.value;
    } else {
      this.severitySelector.value = LOG.INFO;
    }
    this.severitySelector.style.position = "absolute";
    this.severitySelector.style.right = "66px";
    this.dragHandleTop.appendChild(this.severitySelector);

    this.clearButton = this.createButtonElement();
    this.clearButton.style.right = "44px";
    this.clearButton.innerHTML = "C";
    this.clearButton.title = "Clear log area";
    this.dragHandleTop.appendChild(this.clearButton);

    this.oldButton = this.createButtonElement();
    this.oldButton.style.right = "22px";
    this.oldButton.innerHTML = "O";
    this.oldButton.title = "Get all Messages";
    this.dragHandleTop.appendChild(this.oldButton);

    this.hideButton = this.createButtonElement();
    this.hideButton.style.right = "0px";
    this.hideButton.innerHTML = "X";
    this.hideButton.title = "Hide log area";
    this.dragHandleTop.appendChild(this.hideButton);

    this.dragHandleLeft = document.createElement("DIV");
    this.setUpHandleDiv(this.dragHandleLeft, "0px", "0px", null, null, "1px", "100%", "2px outset gray", null, "move");
    this.element.appendChild(this.dragHandleLeft);

    this.dragHandleBottom = document.createElement("DIV");
    this.setUpHandleDiv(this.dragHandleBottom, null, "0px", "0px", null, "100%", "2px", "2px outset gray", null, "move");
    this.element.appendChild(this.dragHandleBottom);

    this.dragHandleRight = document.createElement("DIV");
    this.setUpHandleDiv(this.dragHandleRight, null, null, "0px", "0px", "1px", "100%", "2px outset gray", null, "move");
    this.element.appendChild(this.dragHandleRight);

//    this.resizeHandleNW = document.createElement("DIV");
//    this.setUpHandleDiv(this.resizeHandleNW, "0px", "0px", null, null, "5px", "5px", null, "#000000", "nw-resize");
//    this.element.appendChild(this.resizeHandleNW);
//
//    this.resizeHandleNE = document.createElement("DIV");
//    this.setUpHandleDiv(this.resizeHandleNE, "0px", null, null, "0px", "5px", "5px", null, "#000000", "ne-resize");
//    this.element.appendChild(this.resizeHandleNE);
//
//    this.resizeHandleSE = document.createElement("DIV");
//    this.setUpHandleDiv(this.resizeHandleSE, null, null, "0px", "0px", "5px", "5px", null, "#000000", "se-resize");
//    this.element.appendChild(this.resizeHandleSE);
//
//    this.resizeHandleSW = document.createElement("DIV");
//    this.setUpHandleDiv(this.resizeHandleSW, null, "0px", "0px", null, "5px", "5px", null, "#000000", "sw-resize");
//    this.element.appendChild(this.resizeHandleSW);

    this.scrollElement = document.createElement("DIV");
    this.scrollElement.style.overflow = "auto";
    this.scrollElement.style.width = "100%";
    this.scrollElement.style.height = "100%";
    this.scrollElement.style.background = "#ffffff";
    this.element.appendChild(this.scrollElement);

    this.logList = document.createElement("OL");
    this.logList.style.fontFamily = "Arial, sans-serif";
    this.logList.style.fontSize = "10pt";
    this.logList.id = "Log";
    this.scrollElement.appendChild(this.logList);

    this.handle       = options.handle ? Tobago.element(options.handle) : this.element;

    Element.makePositioned(this.element); // fix IE

    this.delta    = this.currentDelta();

    this.dragging     = false;

    this.eventMouseDown = this.initDrag.bindAsEventListener(this);

    this.eventChangeSeverity = this.changeSeverity.bindAsEventListener(this);
    this.eventClear     = this.clearList.bindAsEventListener(this);
    this.eventOld      = this.getOld.bindAsEventListener(this);
    this.eventHide      = this.hide.bindAsEventListener(this);
    this.eventStopEvent  = this.stopEvent.bindAsEventListener(this);


    Event.observe(this.dragHandleTop, "mousedown", this.eventMouseDown);
    Event.observe(this.dragHandleRight, "mousedown", this.eventMouseDown);
    Event.observe(this.dragHandleBottom, "mousedown", this.eventMouseDown);
    Event.observe(this.dragHandleLeft, "mousedown", this.eventMouseDown);

    Event.observe(this.severitySelector, "change", this.eventChangeSeverity);
    Event.observe(this.severitySelector, "click", this.eventStopEvent);
    Event.observe(this.severitySelector, "mousedown", this.eventStopEvent);
    Event.observe(this.clearButton, "click", this.eventClear);
    Event.observe(this.clearButton, "mousedown", this.eventStopEvent);
    Event.observe(this.oldButton, "click", this.eventOld);
    Event.observe(this.oldButton, "mousedown", this.eventStopEvent);
    Event.observe(this.hideButton, "click", this.eventHide);
    Event.observe(this.hideButton, "mousedown", this.eventStopEvent);

    Draggables.register(this);

    this.body = document.getElementsByTagName("body")[0];
    this.body.tbgLogArea = this;

    if (this.options.hide) {
//      this.element.style.display = 'none';
      this.hide();
    } else {
      this.show();  
    }
    this.body.appendChild(this.element);
    LOG.addAppender(this);
  },

  show: function() {
    this.element.style.display = '';
    this.hide = "show";
    this.setupHidden();
  },

  hide: function() {
    this.element.style.display = 'none';
    this.hide = "hide";
    this.setupHidden();
  },

  setUpLogDiv: function(element, top, right, width, height, border, background) {
    element.style.position = "absolute";
    element.style.MozBoxSizing = "border-box";
    if (right != null) element.style.right = right;
    if (top != null) element.style.top = top;
    if (height != null) element.style.height = height;
    if (width != null) element.style.width = width;
    if (border != null) element.style.border = border;
    if (background != null) element.style.background = background;
  },

  setUpHandleDiv: function(element, top, left, bottom, right, width, height, border, background, cursor) {
    this.setUpLogDiv(element, top, right, width, height, border, background);
    if (left != null) element.style.left = left;
    if (bottom != null) element.style.bottom = bottom;
    if (cursor != null) element.style.cursor = cursor;
  },

  createButtonElement: function() {
    var button = document.createElement("BUTTON");
    button.style.width = "20px";
    button.style.height = "20px";
    button.style.position = "absolute";
    button.style.top = "0px";
    button.style.paddingLeft = "0px";
    button.style.paddingRight = "0px";
    return button;
  },

  changeSeverity: function() {
    this.setupHidden();
  },

  setupHidden: function() {
    var hidden = Tobago.element(Tobago.page.id + Tobago.SUB_COMPONENT_SEP + "clientSeverity");
    if (hidden) {
      hidden.value = this.severitySelector.value + ";" + this.hide;
    }
  },


  clearList: function() {
    this.logList.innerHTML = "";
  },

  getOld: function() {
    for (var i = 0 ; i < LOG.messages.length; i++) {
      this.append(LOG.messages[i]);
    }
  },

  stopEvent: function(event) {
    Event.stop(event);
  },

  scrollToBottom: function() {
    this.scrollElement.scrollTop = this.scrollElement.scrollHeight;
  },

  append: function(message) {
    if (typeof(message.type) == "number" && !this.logFor(message.type)) {
      return;
    }

    var listElement = document.createElement("li");

    var logMessage;
    if (typeof(message) == "string") {
      logMessage = document.createTextNode(message);
    } else if (typeof(message.type) == "number") {
      var prefix = "";
      if (LOG.ERROR == message.type) {
        prefix = prefix + "Error: ";
      } else       if (LOG.WARN == message.type) {
        prefix = prefix + "Warn:  ";
      } else       if (LOG.INFO == message.type) {
        prefix = prefix + "Info:  ";
      } else       if (LOG.DEBUG == message.type) {
        prefix = prefix + "Debug: ";
      }
      logMessage = document.createTextNode(prefix + message.message);
    }
    listElement.appendChild(logMessage);
    this.logList.appendChild(listElement);

    this.scrollToBottom();
  },

  logFor: function(severity) {
    //this.append(severity + ":::" + typeof severity);
    if (this.severitySelector.value) {
    //    this.append("::::::" + this.severitySelector.value + "--" + typeof this.severitySelector.value
    //                + "--" + typeof (this.severitySelector.value - 0));
        return (severity >= (this.severitySelector.value - 0));
    }
    return (severity >= (this.options.severity -0));
  }
});

LOG.LogMessage = Class.create();
LOG.LogMessage.prototype = {
    initialize: function(type, message) {
      this.type = type;
      this.message = message;
      this.time = new Date();
    },

    displayOn: function(type) {
      return this.type <= type;
    }
}