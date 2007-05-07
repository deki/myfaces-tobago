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

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import static org.apache.myfaces.tobago.TobagoConstants.ATTR_STYLE_CLASS;
import org.apache.myfaces.tobago.util.HtmlWriterUtil;
import org.apache.myfaces.tobago.util.XmlUtils;
import org.apache.myfaces.tobago.renderkit.html.HtmlAttributes;
import org.apache.myfaces.tobago.renderkit.html.StyleClasses;
import org.apache.myfaces.tobago.renderkit.html.HtmlConstants;

import javax.faces.component.UIComponent;
import javax.faces.context.ResponseWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.Stack;

public class TobagoResponseWriter extends ResponseWriter {

  private static final Log LOG = LogFactory.getLog(TobagoResponseWriter.class);

  private static final Set<String> EMPTY_TAG = new HashSet<String>(Arrays.asList(
              HtmlConstants.BR,
              HtmlConstants.AREA,
              HtmlConstants.LINK,
              HtmlConstants.IMG,
              HtmlConstants.PARAM,
              HtmlConstants.HR,
              HtmlConstants.INPUT,
              HtmlConstants.COL,
              HtmlConstants.BASE,
              HtmlConstants.META));

  private Writer writer;

  private UIComponent component;

  private boolean startStillOpen;

  private String contentType;

  private String characterEncoding;

  private Stack<String> stack;

  /** use XML instead HMTL */
  private boolean xml;

  private boolean insideScriptOrStyle = false;

  private HtmlWriterUtil helper;

  public TobagoResponseWriter(final Writer writer, final String contentType,
                              final String characterEncoding) {
//    LOG.info("new TobagoResponseWriter!");
//    final StackTraceElement[] stackTrace = new Exception().getStackTrace();
//    for (int i = 1; i < stackTrace.length && i < 5; i++) {
//      LOG.info("  " + stackTrace[i].toString());
//    }
    this.writer = writer;
    this.stack = new Stack<String>();
    this.contentType = contentType;
    this.characterEncoding
        = characterEncoding != null ? characterEncoding : "UTF-8";
    if ("application/xhtml".equals(contentType)
        || "application/xml".equals(contentType)
        || "text/xml".equals(contentType)) {
      xml = true;
    }
    helper = new HtmlWriterUtil(writer, characterEncoding);
  }

  private String findValue(final Object value, final String property) {
    if (value != null) {
      return value instanceof String ? (String) value : value.toString();
    } else if (property != null) {
      if (component != null) {
        final Object object = component.getAttributes().get(property);
        if (object != null) {
          return object instanceof String ? (String) object : object.toString();
        } else {
          return null;
        }
      } else {
        final String trace = getCallingClassStackTraceElementString();
        LOG.error("Don't know what to do! "
            + "Property defined, but no component to get a value. "
            + trace.substring(trace.indexOf('(')));
        LOG.error("value = 'null'");
        LOG.error("property = '" + property + "'");
        return null;
      }
    } else {
      final String trace = getCallingClassStackTraceElementString();
      LOG.error("Don't know what to do! "
          + "No value and no property defined. "
          + trace.substring(trace.indexOf('(')));
      LOG.error("value = 'null'");
      LOG.error("property = 'null'");
      return null;
    }
  }

  public void write(final char[] cbuf, final int off, final int len)
      throws IOException {
    writer.write(cbuf, off, len);
  }

  @Override
  public void write(String string) throws IOException {
    closeOpenTag();
    writer.write(string);
  }

  @Override
  public void write(int i) throws IOException {
    closeOpenTag();
    writer.write(i);
  }

  @Override
  public void write(char[] chars) throws IOException {
    closeOpenTag();
    writer.write(chars);
  }

  @Override
  public void write(String string, int i, int i1) throws IOException {
    closeOpenTag();
    writer.write(string, i, i1);
  }

  public void close() throws IOException {
    closeOpenTag();
    writer.close();
  }

  public void flush() throws IOException {
    /*
    From the api:
    Flush any ouput buffered by the output method to the underlying Writer or OutputStream.
    This method will not flush the underlying Writer or OutputStream;
    it simply clears any values buffered by this ResponseWriter.
     */
    closeOpenTag();
  }

  public void writeText(final Object text, final String property)
      throws IOException {
    closeOpenTag();
    final String value = findValue(text, property);
    if (insideScriptOrStyle) {
      write(value);
    } else {
      if (xml) {
        write(XmlUtils.escape(value));
      } else {
        helper.writeText(value);
      }
    }
  }

  private  void closeOpenTag() throws IOException {
    if (startStillOpen) {
      writer.write("\n>");
      startStillOpen = false;
    }
  }

  public void writeText(final char[] text, final int offset, final int length)
      throws IOException {
    closeOpenTag();
    if (insideScriptOrStyle) {
      writer.write(text, offset, length);
    } else {
      if (xml) {
        writer.write(XmlUtils.escape(text.toString()).toCharArray(), offset, length);
// FIXME: not nice:     XmlUtils.escape(text.toString()).toCharArray()
      } else {
        helper.writeText(text, offset, length);
      }
    }
  }

  public void startDocument() throws IOException {
    // nothing to do
  }

  public void endDocument() throws IOException {
    // nothing to do
  }

  public String getContentType() {
    return contentType;
  }

  public String getCharacterEncoding() {
    return characterEncoding;
  }

  public void startElement(final String name) throws IOException {
    startElement(name, null);
  }
  public void startElement(final String name, final UIComponent currentComponent)
      throws IOException {
    this.component = currentComponent;
    stack.push(name);
//    closeStartIfNecessary();
    insideScriptOrStyle = isScriptOrStyle(name);
    if (LOG.isDebugEnabled()) {
      LOG.debug("start Element: " + name);
    }
    if (startStillOpen) {
      writer.write("\n>");
    }
    writer.write("<");
    writer.write(name);
    startStillOpen = true;
  }

  static boolean isScriptOrStyle(final String name) {
    try {
      switch (name.charAt(0)) {
        case 's' :
          switch (name.charAt(1)) {
            case 'c' :
              if (name.charAt(2) == 'r' && name.charAt(3) == 'i'
                  && name.charAt(4) == 'p' && name.charAt(5) == 't') {
                return true;
              }
            break;
            case 't' :
              if (name.charAt(2) == 'y' && name.charAt(3) == 'l'
                  && name.charAt(4) == 'e') {
                return true;
              }
            break;
            default:
              return false;
          }
          break;
        default:
          return false;
      }
    } catch (NullPointerException e) {
      // ignore
    } catch (StringIndexOutOfBoundsException e) {
      // ignore
    }
    return false;
  }

  public void endElement(final String name) throws IOException {
    if (LOG.isDebugEnabled()) {
      LOG.debug("end Element: " + name);
    }

    final String top = stack.pop();
    if (!top.equals(name)) {
      final String trace = getCallingClassStackTraceElementString();
      LOG.error("Element end with name='" + name + "' doesn't "
          + "match with top element on the stack='" + top + "' "
          + trace.substring(trace.indexOf('(')));
    }

    insideScriptOrStyle = false;

    if (EMPTY_TAG.contains(name)) {
      if (xml) {
        writer.write("\n/>");
      } else {
        writer.write("\n>");
      }
    } else {
      if (startStillOpen) {
        writer.write("\n>");
      }
      writer.write("</");
      writer.write(name);
//      writer.write("\n>"); // FIXME: this makes problems with Tidy
      writer.write(">");
    }
    startStillOpen = false;
  }

  public void writeComment(final Object obj) throws IOException {
    closeOpenTag();
    String comment = obj.toString();
    write("<!--");
    if (comment.indexOf("--") < 0) {
      write(comment);
    } else {
      String trace = getCallingClassStackTraceElementString();
      LOG.warn(
          "Comment must not contain the sequence '--', comment = '"
          + comment + "' " + trace.substring(trace.indexOf('(')));
      write(StringUtils.replace(comment, "--", "++"));
    }
    write("-->");
  }

  public ResponseWriter cloneWithWriter(final Writer originalWriter) {
    return new TobagoResponseWriter(
        originalWriter, getContentType(), getCharacterEncoding());
  }

  public void writeAttribute(final String name, final boolean on) throws IOException {
    if (on) {
      // boolean attributes don't need escaped
      writeAttribute(name, name, false);
    }
  }

  public void writeComponentAttribute(final String name, final String property)
      throws IOException {
    writeAttribute(name, null, property, true);
  }

  public void writeAttribute(final String name, final Object value, final String property)
      throws IOException {
    writeAttribute(name, value, property, true);
  }

  public void writeAttribute(final String name, final Object value, final boolean escape)
      throws IOException {
      writeAttribute(name, value.toString(), escape);
  }

  public void writeAttribute(final String name, final String value, final boolean escape)
      throws IOException {
    if (!startStillOpen) {
      String trace = getCallingClassStackTraceElementString();
      String error = "Cannot write attribute when start-tag not open. "
          + "name = '" + name + "' "
          + "value = '" + value + "' "
          + trace.substring(trace.indexOf('('));
      LOG.error(error);
      throw new IllegalStateException(error);
    }

    if (value != null) {
      writer.write(' ');
      writer.write(name);
      writer.write("=\"");
      if (xml) {
        writer.write(XmlUtils.escape(value));
      } else {
        if (escape && HtmlWriterUtil.attributeValueMustEscaped(name)) {
          helper.writeAttributeValue(value);
        } else {
          writer.write(value);
        }
      }
      writer.write('\"');
    }
  }

  public void writeAttribute(final String name, final Object value,
                             final String property, final boolean escape)
      throws IOException {
    if (!startStillOpen) {
      final String trace = getCallingClassStackTraceElementString();
      final String error = "Cannot write attribute when start-tag not open. "
          + "name = '" + name + "'"
          + "value = '" + value + "'"
          + "property = '" + property + "' "
          + trace.substring(trace.indexOf('('));
      LOG.error(error);
      throw new IllegalStateException(error);
    }

    final String attribute = findValue(value, property);
    if (attribute != null) {
      writer.write(' ');
      writer.write(name);
      writer.write("=\"");
      if (xml) {
        writer.write(XmlUtils.escape(attribute));
      } else {
        if (escape && HtmlWriterUtil.attributeValueMustEscaped(name)) {
          helper.writeAttributeValue(attribute);
        } else {
          writer.write(attribute);
        }
      }
      writer.write('\"');
    }
  }

  private String getCallingClassStackTraceElementString() {
    final StackTraceElement[] stackTrace = new Exception().getStackTrace();
    int i = 1;
    while (stackTrace[i].getClassName().equals(this.getClass().getName())) {
      i++;
    }
    return stackTrace[i].toString();
  }

  public void writeIdAttribute(final String id) throws IOException {
    writeAttribute(HtmlAttributes.ID, id, false);
  }

  public void writeNameAttribute(final String name) throws IOException {
    writeAttribute(HtmlAttributes.NAME, name, false);
  }

  public void writeClassAttribute(final String cssClass) throws IOException {
    writeAttribute(HtmlAttributes.CLASS, cssClass, false);
  }

  public void writeClassAttribute(StyleClasses styleClasses) throws IOException {
    writeAttribute(HtmlAttributes.CLASS, styleClasses.toString(), false);
  }

  public void writeComponentClass() throws IOException {
    writeComponentAttribute(HtmlAttributes.CLASS, ATTR_STYLE_CLASS);
  }

  public void writeURIAttribute(final String s, final Object obj, final String s1)
      throws IOException {
    LOG.error("Not implemented yet!");
  }

}
