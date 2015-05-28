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

import org.apache.myfaces.tobago.component.Attributes;
import org.apache.myfaces.tobago.component.ComponentTypes;
import org.apache.myfaces.tobago.component.Facets;
import org.apache.myfaces.tobago.component.RendererTypes;
import org.apache.myfaces.tobago.component.SupportsMarkup;
import org.apache.myfaces.tobago.component.UIColumnSelector;
import org.apache.myfaces.tobago.component.UICommand;
import org.apache.myfaces.tobago.component.UILink;
import org.apache.myfaces.tobago.component.UIMenu;
import org.apache.myfaces.tobago.component.UIMenuCommand;
import org.apache.myfaces.tobago.component.UIOut;
import org.apache.myfaces.tobago.component.UIReload;
import org.apache.myfaces.tobago.component.UISheet;
import org.apache.myfaces.tobago.component.UIToolBar;
import org.apache.myfaces.tobago.context.ClientProperties;
import org.apache.myfaces.tobago.context.Markup;
import org.apache.myfaces.tobago.context.ResourceManagerUtils;
import org.apache.myfaces.tobago.event.PageAction;
import org.apache.myfaces.tobago.internal.component.AbstractUIColumn;
import org.apache.myfaces.tobago.internal.component.AbstractUIColumnNode;
import org.apache.myfaces.tobago.internal.component.AbstractUIData;
import org.apache.myfaces.tobago.internal.component.AbstractUIMenu;
import org.apache.myfaces.tobago.internal.component.AbstractUIOut;
import org.apache.myfaces.tobago.internal.component.AbstractUISheet;
import org.apache.myfaces.tobago.internal.layout.Cell;
import org.apache.myfaces.tobago.internal.layout.Grid;
import org.apache.myfaces.tobago.internal.layout.OriginCell;
import org.apache.myfaces.tobago.internal.util.StringUtils;
import org.apache.myfaces.tobago.layout.Display;
import org.apache.myfaces.tobago.layout.LayoutBase;
import org.apache.myfaces.tobago.layout.Measure;
import org.apache.myfaces.tobago.layout.TextAlign;
import org.apache.myfaces.tobago.model.ExpandedState;
import org.apache.myfaces.tobago.model.SheetState;
import org.apache.myfaces.tobago.model.TreePath;
import org.apache.myfaces.tobago.renderkit.LayoutComponentRendererBase;
import org.apache.myfaces.tobago.renderkit.css.Classes;
import org.apache.myfaces.tobago.renderkit.css.Style;
import org.apache.myfaces.tobago.renderkit.html.BootstrapClass;
import org.apache.myfaces.tobago.renderkit.html.Command;
import org.apache.myfaces.tobago.renderkit.html.CommandMap;
import org.apache.myfaces.tobago.renderkit.html.DataAttributes;
import org.apache.myfaces.tobago.renderkit.html.HtmlAttributes;
import org.apache.myfaces.tobago.renderkit.html.HtmlElements;
import org.apache.myfaces.tobago.renderkit.html.HtmlInputTypes;
import org.apache.myfaces.tobago.renderkit.html.JsonUtils;
import org.apache.myfaces.tobago.renderkit.html.util.HtmlRendererUtils;
import org.apache.myfaces.tobago.renderkit.util.EncodeUtils;
import org.apache.myfaces.tobago.renderkit.util.RenderUtils;
import org.apache.myfaces.tobago.util.ComponentUtils;
import org.apache.myfaces.tobago.util.CreateComponentUtils;
import org.apache.myfaces.tobago.util.FacetUtils;
import org.apache.myfaces.tobago.webapp.TobagoResponseWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.el.ValueExpression;
import javax.faces.application.Application;
import javax.faces.component.UIColumn;
import javax.faces.component.UIComponent;
import javax.faces.component.UINamingContainer;
import javax.faces.context.FacesContext;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class SheetRenderer extends LayoutComponentRendererBase {

  private static final Logger LOG = LoggerFactory.getLogger(SheetRenderer.class);

  public static final String WIDTHS_POSTFIX = ComponentUtils.SUB_SEPARATOR + "widths";
  public static final String SELECTED_POSTFIX = ComponentUtils.SUB_SEPARATOR + "selected";

  private static final Integer HEIGHT_0 = 0;
  private static final String DOTS = "...";

  @Override
  public void prepareRender(final FacesContext facesContext, final UIComponent component) throws IOException {
    super.prepareRender(facesContext, component);
    ensureHeader(facesContext, (UISheet) component);
  }

  private void ensureHeader(final FacesContext facesContext, final UISheet sheet) {
    UIComponent header = sheet.getHeader();
    if (header == null) {
      header = CreateComponentUtils.createComponent(facesContext, ComponentTypes.PANEL, null, "_header");
      header.setTransient(true);
      final List<AbstractUIColumn> columns = sheet.getAllColumns();
      int i = 0;
      for (final AbstractUIColumn column : columns) {
        final AbstractUIOut out = (AbstractUIOut) CreateComponentUtils.createComponent(
            facesContext, ComponentTypes.OUT, RendererTypes.OUT, "_col" + i);
        out.setTransient(true);
//        out.setValue(column.getLabel());
        ValueExpression valueExpression = column.getValueExpression(Attributes.LABEL);
        if (valueExpression != null) {
          out.setValueExpression(Attributes.VALUE, valueExpression);
        } else {
          out.setValue(column.getAttributes().get(Attributes.LABEL));
        }
        valueExpression = column.getValueExpression(Attributes.RENDERED);
        if (valueExpression != null) {
          out.setValueExpression(Attributes.RENDERED, valueExpression);
        } else {
          out.setRendered((Boolean) column.getAttributes().get(Attributes.RENDERED));
        }
        header.getChildren().add(out);
        i++;
      }
      sheet.setHeader(header);
    }
  }

  @Override
  public void encodeEnd(final FacesContext facesContext, final UIComponent uiComponent) throws IOException {

    final UISheet sheet = (UISheet) uiComponent;

    final Style style = new Style(facesContext, sheet);

    final String sheetId = sheet.getClientId(facesContext);

    final TobagoResponseWriter writer = HtmlRendererUtils.getTobagoResponseWriter(facesContext);

    // Outer sheet div
    writer.startElement(HtmlElements.DIV, sheet);
    writer.writeIdAttribute(sheetId);
    HtmlRendererUtils.writeDataAttributes(facesContext, writer, sheet);
    writer.writeClassAttribute(Classes.create(sheet));
    writer.writeStyleAttribute(style);
    final UIComponent facetReload = sheet.getFacet(Facets.RELOAD);
    if (facetReload != null && facetReload instanceof UIReload && facetReload.isRendered()) {
      final UIReload update = (UIReload) facetReload;
      writer.writeAttribute(DataAttributes.RELOAD, update.getFrequency());
    }
    final String[] clientIds = ComponentUtils.evaluateClientIds(facesContext, sheet, sheet.getRenderedPartially());
    if (clientIds.length > 0) {
      writer.writeAttribute(DataAttributes.PARTIAL_IDS, JsonUtils.encode(clientIds), true);
    }
    writer.writeAttribute(DataAttributes.SELECTION_MODE, sheet.getSelectable(), false);
    writer.writeAttribute(DataAttributes.FIRST, Integer.toString(sheet.getFirst()), false);

    final boolean rowAction = HtmlRendererUtils.renderSheetCommands(sheet, facesContext, writer);

    renderSheet(facesContext, sheet, rowAction, style);

    writer.endElement(HtmlElements.DIV);
  }

  private void renderSheet(
      final FacesContext facesContext, final UISheet sheet, final boolean hasClickAction, final Style style)
      throws IOException {
    final TobagoResponseWriter writer = HtmlRendererUtils.getTobagoResponseWriter(facesContext);
    final String sheetId = sheet.getClientId(facesContext);

    final String selectable = sheet.getSelectable();

    final Application application = facesContext.getApplication();
    final SheetState state = sheet.getSheetState(facesContext);
    final List<Integer> columnWidths = sheet.getWidthList();

    final List<Integer> selectedRows = getSelectedRows(sheet, state);
    final List<AbstractUIColumn> renderedColumnList = sheet.getRenderedColumns();

    writer.startElement(HtmlElements.INPUT, null);
    writer.writeIdAttribute(sheetId + WIDTHS_POSTFIX);
    writer.writeNameAttribute(sheetId + WIDTHS_POSTFIX);
    writer.writeAttribute(HtmlAttributes.TYPE, HtmlInputTypes.HIDDEN, false);
    writer.writeAttribute(HtmlAttributes.VALUE, StringUtils.joinWithSurroundingSeparator(columnWidths), false);
    writer.endElement(HtmlElements.INPUT);

    RenderUtils.writeScrollPosition(facesContext, writer, sheet, sheet.getScrollPosition());

    if (!UISheet.NONE.equals(selectable)) {
      writer.startElement(HtmlElements.INPUT, null);
      writer.writeIdAttribute(sheetId + SELECTED_POSTFIX);
      writer.writeNameAttribute(sheetId + SELECTED_POSTFIX);
      writer.writeAttribute(HtmlAttributes.TYPE, HtmlInputTypes.HIDDEN, false);
      writer.writeAttribute(
          HtmlAttributes.VALUE, StringUtils.joinWithSurroundingSeparator(selectedRows), true);
      writer.endElement(HtmlElements.INPUT);
    }

    writer.startElement(HtmlElements.INPUT, null);
    writer.writeAttribute(HtmlAttributes.TYPE, HtmlInputTypes.HIDDEN, false);
    final String hiddenId = sheetId + UINamingContainer.getSeparatorChar(facesContext)
        + PageAction.TO_PAGE.getToken() + ComponentUtils.SUB_SEPARATOR + "value";
    writer.writeAttribute(HtmlAttributes.NAME, hiddenId, false);
    writer.writeIdAttribute(hiddenId);
    writer.endElement(HtmlElements.INPUT);

    ExpandedState expandedState = null;
    StringBuilder expandedValue = null;
    if (sheet.isTreeModel()) {
      expandedState = sheet.getExpandedState();
      expandedValue = new StringBuilder(",");
    }

    final boolean showHeader = sheet.isShowHeader();

// BEGIN RENDER BODY CONTENT

    if (showHeader) {
      renderColumnHeaders(facesContext, sheet, writer, renderedColumnList);
    }

    writer.startElement(HtmlElements.DIV, null);
    writer.writeIdAttribute(sheetId + ComponentUtils.SUB_SEPARATOR + "data_div");
    writer.writeClassAttribute(Classes.create(sheet, "body"));
      
    writer.startElement(HtmlElements.TABLE, null);
    writer.writeAttribute(HtmlAttributes.CELLSPACING, "0", false);
    writer.writeAttribute(HtmlAttributes.CELLPADDING, "0", false);
    writer.writeAttribute(HtmlAttributes.SUMMARY, "", false);
    writer.writeClassAttribute(Classes.create(sheet, "bodyTable").getStringValue()
        + " " + BootstrapClass.TABLE.getName()
        + " " + BootstrapClass.TABLE_BORDERED.getName()
        + " " + BootstrapClass.TABLE_STRIPED.getName()
        + " " + BootstrapClass.TABLE_HOVER.getName()); // XXX

    writeColgroup(writer, columnWidths);

    // Print the Content

    if (LOG.isDebugEnabled()) {
      LOG.debug("first = " + sheet.getFirst() + "   rows = " + sheet.getRows());
    }

    final String var = sheet.getVar();

    boolean odd = false;
    boolean emptySheet = true;
    // rows = 0 means: show all
    final int last = sheet.isRowsUnlimited() ? Integer.MAX_VALUE : sheet.getFirst() + sheet.getRows();
    for (int rowIndex = sheet.getFirst(); rowIndex < last; rowIndex++) {
      sheet.setRowIndex(rowIndex);
      if (!sheet.isRowAvailable()) {
        break;
      }

      final Object rowRendered = sheet.getAttributes().get("rowRendered");
      if (rowRendered instanceof Boolean && !((Boolean) rowRendered)) {
        continue;
      }

      emptySheet = false;
      odd = !odd;

      if (LOG.isDebugEnabled()) {
        LOG.debug("var       " + var);
        LOG.debug("list      " + sheet.getValue());
      }

      if (sheet.isTreeModel()) {
        final TreePath path = sheet.getPath();
        if (sheet.isFolder() && expandedState.isExpanded(path)) {
          expandedValue.append(rowIndex);
          expandedValue.append(",");
        }
      }

      writer.startElement(HtmlElements.TR, null);
      if (rowRendered instanceof Boolean) {
        // if rowRendered attribute is set we need the rowIndex on the client
        writer.writeAttribute(DataAttributes.ROW_INDEX, rowIndex);
      }
      Markup rowMarkup = odd ? Markup.ODD : Markup.EVEN;
      final boolean selected = selectedRows.contains(rowIndex);
      if (selected) {
        rowMarkup = rowMarkup.add(Markup.SELECTED);
      }
      final String[] rowMarkups = (String[]) sheet.getAttributes().get("rowMarkup");
      if (rowMarkups != null) {
        rowMarkup = rowMarkup.add(Markup.valueOf(rowMarkups));
      }
      writer.writeClassAttribute(Classes.create(sheet, "row", rowMarkup));
      if (!sheet.isRowVisible()) {
        final Style rowStyle = new Style();
        rowStyle.setDisplay(Display.NONE);
        writer.writeStyleAttribute(rowStyle);
      }
      final String parentId = sheet.getRowParentClientId();
      if (parentId != null) {
        writer.writeAttribute(DataAttributes.TREE_PARENT, parentId, false);
      }

      int columnIndex = -1;
      for (final UIColumn column : renderedColumnList) {
        columnIndex++;

        writer.startElement(HtmlElements.TD, column);

        Markup markup = column instanceof SupportsMarkup ? ((SupportsMarkup) column).getMarkup() : Markup.NULL;
        if (markup == null) {
          markup = Markup.NULL;
        }
        if (columnIndex == 0) {
          markup = markup.add(Markup.FIRST);
        }
        if (hasClickAction) {
          markup = markup.add(Markup.CLICKABLE);
        }
        if (isPure(column)) {
          markup = markup.add(Markup.PURE);
        }
        final TextAlign align = TextAlign.parse((String) column.getAttributes().get(Attributes.ALIGN));
        if (align == TextAlign.RIGHT) {
          markup = markup.add(Markup.RIGHT);
        } else if (align == TextAlign.CENTER) {
          markup = markup.add(Markup.CENTER);
        } else if (align == TextAlign.JUSTIFY) {
          markup = markup.add(Markup.JUSTIFY);
        }
        writer.writeClassAttribute(Classes.create(sheet, "cell", markup));

        if (column instanceof UIColumnSelector) {
          final boolean disabled = ComponentUtils.getBooleanAttribute(column, Attributes.DISABLED);
          writer.startElement(HtmlElements.INPUT, null);
          writer.writeAttribute(HtmlAttributes.TYPE, HtmlInputTypes.CHECKBOX, false);
          writer.writeAttribute(HtmlAttributes.CHECKED, selected);
          writer.writeAttribute(HtmlAttributes.DISABLED, disabled);
          writer.writeIdAttribute(sheetId + "_data_row_selector_" + rowIndex);
          writer.writeClassAttribute(Classes.create(sheet, "columnSelector"));
          writer.endElement(HtmlElements.INPUT);
        } else if (column instanceof AbstractUIColumnNode) {
          EncodeUtils.prepareRendererAll(facesContext, column);
          RenderUtils.encode(facesContext, column);
        } else {
          final List<UIComponent> children = sheet.getRenderedChildrenOf(column);
          for (final UIComponent grandKid : children) {
            // set height to 0 to prevent use of layoutheight from parent
            grandKid.getAttributes().put(Attributes.LAYOUT_HEIGHT, HEIGHT_0);
            // XXX hotfix
            if (grandKid instanceof LayoutBase) {
              final LayoutBase base = (LayoutBase) grandKid;
              if (base.getLeft() != null) {
                base.setLeft(null);
              }
              if (base.getTop() != null) {
                base.setTop(null);
              }
            }
            EncodeUtils.prepareRendererAll(facesContext, grandKid);
            RenderUtils.encode(facesContext, grandKid);
          }
        }

        writer.endElement(HtmlElements.TD);
      }

      writer.startElement(HtmlElements.TD, null);
      writer.writeClassAttribute(Classes.create(sheet, "cell", Markup.FILLER));
//      writer.write("&nbsp;");
      writer.startElement(HtmlElements.DIV, null);
      writer.endElement(HtmlElements.DIV);
      writer.endElement(HtmlElements.TD);

      writer.endElement(HtmlElements.TR);
    }

    sheet.setRowIndex(-1);

    if (emptySheet && showHeader) {
      writer.startElement(HtmlElements.TR, null);
      int columnIndex = -1;
      for (final UIColumn ignored : renderedColumnList) {
        columnIndex++;
        writer.startElement(HtmlElements.TD, null);
        writer.startElement(HtmlElements.DIV, null);
        if (columnWidths != null) {
          final Integer divWidth = columnWidths.get(columnIndex);
          final Style divStyle = new Style();
          divStyle.setWidth(Measure.valueOf(divWidth));
          writer.writeStyleAttribute(divStyle);
        }
        writer.endElement(HtmlElements.DIV);
        writer.endElement(HtmlElements.TD);
      }
      writer.startElement(HtmlElements.TD, null);
      writer.writeClassAttribute(Classes.create(sheet, "cell", Markup.FILLER));
//      writer.write("&nbsp;");
      writer.startElement(HtmlElements.DIV, null);
      writer.endElement(HtmlElements.DIV);
      writer.endElement(HtmlElements.TD);
      writer.endElement(HtmlElements.TR);
    }

    writer.endElement(HtmlElements.TABLE);
    writer.endElement(HtmlElements.DIV);

// END RENDER BODY CONTENT

    if (sheet.isPagingVisible()) {
      final Style footerStyle = new Style();
      footerStyle.setWidth(sheet.getCurrentWidth());
      writer.startElement(HtmlElements.DIV, sheet);
      writer.writeClassAttribute(Classes.create(sheet, "footer"));
      writer.writeStyleAttribute(footerStyle);

      // show row range
      final Markup showRowRange = markupForLeftCenterRight(sheet.getShowRowRange());
      if (showRowRange != Markup.NULL) {
        UICommand pagerCommand = (UICommand) sheet.getFacet(Facets.PAGER_ROW);
        if (pagerCommand == null) {
          pagerCommand = createPagingCommand(application, PageAction.TO_ROW, false);
          sheet.getFacets().put(Facets.PAGER_ROW, pagerCommand);
        }
        final String pagerCommandId = pagerCommand.getClientId(facesContext);

        writer.startElement(HtmlElements.SPAN, null);
        writer.writeClassAttribute(Classes.create(sheet, "pagingOuter", showRowRange));
        writer.writeAttribute(HtmlAttributes.TITLE,
            ResourceManagerUtils.getPropertyNotNull(facesContext, "tobago", "sheetPagingInfoRowPagingTip"), true);
        writer.flush(); // is needed in some cases, e. g. TOBAGO-1094
        writer.write(createSheetPagingInfoRow(sheet, facesContext, pagerCommandId));
        writer.endElement(HtmlElements.SPAN);
      }

      // show direct links
      final Markup showDirectLinks = markupForLeftCenterRight(sheet.getShowDirectLinks());
      if (showDirectLinks != Markup.NULL) {
        writer.startElement(HtmlElements.UL, null);
        final String pagingOuter = Classes.create(sheet, "pagingOuter", showDirectLinks).getStringValue();
        writer.writeClassAttribute(pagingOuter  + " "
            + BootstrapClass.PAGINATION.getName() + " tobago-sheet-pagingLinks");
        String areaId = "pagingLinks";
        writer.writeIdAttribute(sheetId + ComponentUtils.SUB_SEPARATOR + areaId);
        if (sheet.isShowDirectLinksArrows()) {
          final boolean atBeginning = sheet.isAtBeginning();
          link(facesContext, application, areaId, atBeginning, PageAction.FIRST, sheet);
          link(facesContext, application, areaId, atBeginning, PageAction.PREV, sheet);
        }
        writeDirectPagingLinks(writer, facesContext, application, sheet);
        if (sheet.isShowDirectLinksArrows()) {
          final boolean atEnd = sheet.isAtEnd();
          link(facesContext, application, areaId, atEnd, PageAction.NEXT, sheet);
          link(facesContext, application, areaId, atEnd || !sheet.hasRowCount(), PageAction.LAST, sheet);
        }
        writer.endElement(HtmlElements.UL);
      }

      // show page range
      final Markup showPageRange = markupForLeftCenterRight(sheet.getShowPageRange());
      if (showPageRange != Markup.NULL) {
        UICommand pagerCommand = (UICommand) sheet.getFacet(Facets.PAGER_PAGE);
        if (pagerCommand == null) {
          pagerCommand = createPagingCommand(application, PageAction.TO_PAGE, false);
          sheet.getFacets().put(Facets.PAGER_PAGE, pagerCommand);
        }
        final String pagerCommandId = pagerCommand.getClientId(facesContext);

        writer.startElement(HtmlElements.SPAN, null);
        final Classes pagingOuter = Classes.create(sheet, "pagingOuter", showPageRange);
        writer.writeClassAttribute(pagingOuter.getStringValue() + " tobago-sheet-pagingPages");
        String areaId = "pagingPages";
        writer.writeIdAttribute(sheetId + ComponentUtils.SUB_SEPARATOR + "pagingPages");
        writer.writeText("");

        if (sheet.isShowPageRangeArrows()) {
          final boolean atBeginning = sheet.isAtBeginning();
          link(facesContext, application, areaId, atBeginning, PageAction.FIRST, sheet);
          link(facesContext, application, areaId, atBeginning, PageAction.PREV, sheet);
        }
        writer.startElement(HtmlElements.SPAN, null);
        writer.writeClassAttribute(Classes.create(sheet, "pagingText"));
        writer.writeAttribute(HtmlAttributes.TITLE,
                ResourceManagerUtils.getPropertyNotNull(facesContext, "tobago", "sheetPagingInfoPagePagingTip"), true);
        writer.flush(); // is needed in some cases, e. g. TOBAGO-1094
        if (sheet.getRowCount() != 0) {
          final Locale locale = facesContext.getViewRoot().getLocale();
          final int first = sheet.getCurrentPage() + 1;
          final boolean unknown = !sheet.hasRowCount();
          final int pages = unknown ? -1 : sheet.getPages();
          final String key;
          if (unknown) {
            if (first == pages) {
              key = "sheetPagingInfoUndefinedSinglePage";
            } else {
              key = "sheetPagingInfoUndefinedPages";
            }
          } else {
            if (first == pages) {
              key = "sheetPagingInfoSinglePage";
            } else {
              key = "sheetPagingInfoPages";
            }
          }
          final Object[] args = {
              first,
              pages == -1 ? "?" : pages,
              unknown ? "" : sheet.getRowCount()
          };
          final MessageFormat detail1 = new MessageFormat(
              ResourceManagerUtils.getPropertyNotNull(facesContext, "tobago", key + "1"), locale);
          final MessageFormat detail2 = new MessageFormat(
              ResourceManagerUtils.getPropertyNotNull(facesContext, "tobago", key + "2"), locale);
          writer.write(detail1.format(args));
          writer.startElement(HtmlElements.SPAN, null);
          writer.writeClassAttribute("tobago-sheet-pagingOutput"); // todo
          writer.writeText(Integer.toString(first));
          writer.endElement(HtmlElements.SPAN);
          writer.startElement(HtmlElements.INPUT, null);
          writer.writeAttribute(HtmlAttributes.TYPE, HtmlInputTypes.TEXT, false);
          writer.writeClassAttribute("tobago-sheet-pagingInput"); // todo
          writer.writeAttribute(HtmlAttributes.VALUE, first);
          if (!unknown) {
            writer.writeAttribute(HtmlAttributes.MAXLENGTH, Integer.toString(pages).length());
          }
          writer.endElement(HtmlElements.INPUT);
          writer.write(detail2.format(args));
        } else {
          writer.write(ResourceManagerUtils.getPropertyNotNull(facesContext, "tobago", "sheetPagingInfoEmptyPage"));
        }
        writer.endElement(HtmlElements.SPAN);
        if (sheet.isShowPageRangeArrows()) {
          final boolean atEnd = sheet.isAtEnd();
          link(facesContext, application, areaId, atEnd, PageAction.NEXT, sheet);
          link(facesContext, application, areaId, atEnd || !sheet.hasRowCount(), PageAction.LAST, sheet);
        }
        writer.endElement(HtmlElements.SPAN);
      }

      writer.endElement(HtmlElements.DIV);
    }

    if (sheet.isTreeModel()) {
      writer.startElement(HtmlElements.INPUT, sheet);
      writer.writeAttribute(HtmlAttributes.TYPE, HtmlInputTypes.HIDDEN, false);
      final String expandedId = sheetId + ComponentUtils.SUB_SEPARATOR + AbstractUIData.SUFFIX_EXPANDED;
      writer.writeNameAttribute(expandedId);
      writer.writeIdAttribute(expandedId);
      writer.writeClassAttribute(Classes.create(sheet, AbstractUIData.SUFFIX_EXPANDED));
      writer.writeAttribute(HtmlAttributes.VALUE, expandedValue.toString(), false);
      writer.endElement(HtmlElements.INPUT);
    }
  }

  private void writeColgroup(final TobagoResponseWriter writer, final List<Integer> columnWidths) throws IOException {
    if (columnWidths != null) {
      writer.startElement(HtmlElements.COLGROUP, null);
      for (final Integer columnWidth : columnWidths) {
        writeCol(writer, columnWidth);
      }
      writeCol(writer, 0); // extra entry for resizing...
      // TODO: replace 0 later
      // TODO: the value should be added to the list
      writer.endElement(HtmlElements.COLGROUP);
    }
  }

  private void writeCol(final TobagoResponseWriter writer, final Integer columnWidth) throws IOException {
    writer.startElement(HtmlElements.COL, null);
    if (columnWidth != null) {
      writer.writeAttribute(HtmlAttributes.WIDTH, columnWidth);
    }
    writer.endElement(HtmlElements.COL);
  }

  /**
   * Differ between simple content and complex content.
   * Decide if the content of a cell needs usually the whole possible space or
   * is the character of the content like flowing text.
   * In the second case, the style usually sets a padding.<br/>
   * Pure is needed for &lt;tc:panel>,  &lt;tc:in>, etc.<br/>
   * Pure is not needed for  &lt;tc:out> and &lt;tc:link>
   */
  private boolean isPure(final UIColumn column) {
    for (final UIComponent child : column.getChildren()) {
      if (!(child instanceof UIOut) && !(child instanceof UILink)) {
        return true;
      }
    }
    return false;
  }

  private String createSheetPagingInfoRow(
      final UISheet sheet, final FacesContext facesContext, final String pagerCommandId) {
    final String sheetPagingInfo;
    if (sheet.getRowCount() != 0) {
      final Locale locale = facesContext.getViewRoot().getLocale();
      final int first = sheet.getFirst() + 1;
      final int last = sheet.hasRowCount()
          ? sheet.getLastRowIndexOfCurrentPage()
          : -1;
      final boolean unknown = !sheet.hasRowCount();
      final String key; // plural
      if (unknown) {
        if (first == last) {
          key = "sheetPagingInfoUndefinedSingleRow";
        } else {
          key = "sheetPagingInfoUndefinedRows";
        }
      } else {
        if (first == last) {
          key = "sheetPagingInfoSingleRow";
        } else {
          key = "sheetPagingInfoRows";
        }
      }
      final String message = ResourceManagerUtils.getPropertyNotNull(facesContext, "tobago", key);
      final MessageFormat detail = new MessageFormat(message, locale);
      final Object[] args = {
          first,
          last == -1 ? "?" : last,
          unknown ? "" : sheet.getRowCount(),
          pagerCommandId + ComponentUtils.SUB_SEPARATOR + "text"
      };
      sheetPagingInfo = detail.format(args);
    } else {
      sheetPagingInfo = ResourceManagerUtils.getPropertyNotNull(facesContext, "tobago", "sheetPagingInfoEmptyRow");
    }
    return sheetPagingInfo;
  }

  @Override
  public void decode(final FacesContext facesContext, final UIComponent component) {
    super.decode(facesContext, component);

    final UISheet sheet = (UISheet) component;

    String key = sheet.getClientId(facesContext) + WIDTHS_POSTFIX;

    final Map requestParameterMap = facesContext.getExternalContext().getRequestParameterMap();
    if (requestParameterMap.containsKey(key)) {
      final String widths = (String) requestParameterMap.get(key);
      if (widths.trim().length() > 0) {
        sheet.getAttributes().put(Attributes.WIDTH_LIST_STRING, widths);
      }
    }

    key = sheet.getClientId(facesContext) + SELECTED_POSTFIX;
    if (requestParameterMap.containsKey(key)) {
      final String selected = (String) requestParameterMap.get(key);
      if (LOG.isDebugEnabled()) {
        LOG.debug("selected = " + selected);
      }
      List<Integer> selectedRows;
      try {
        selectedRows = StringUtils.parseIntegerList(selected);
      } catch (final NumberFormatException e) {
        LOG.warn(selected, e);
        selectedRows = Collections.emptyList();
      }

      sheet.getAttributes().put(Attributes.SELECTED_LIST_STRING, selectedRows);
    }

    RenderUtils.decodeScrollPosition(facesContext, sheet);
    RenderUtils.decodedStateOfTreeData(facesContext, sheet);
  }

  private Markup markupForLeftCenterRight(final String name) {
    if ("left".equals(name)) {
      return Markup.LEFT;
    }
    if ("center".equals(name)) {
      return Markup.CENTER;
    }
    if ("right".equals(name)) {
      return Markup.RIGHT;
    }
    return Markup.NULL;
  }

  private String checkPagingAttribute(final String name) {
    if (isNotNone(name)) {
      return name;
    } else {
      if (!"none".equals(name)) {
        LOG.warn("Illegal value in sheets paging attribute: '" + name + "'");
      }
      return "none";
    }
  }

  private boolean isNotNone(final String value) {
    // todo: use enum type instead of string
    return "left".equals(value) || "center".equals(value) || "right".equals(value);
  }

  @Override
  public boolean getRendersChildren() {
    return true;
  }

  private List<Integer> getSelectedRows(final UISheet data, final SheetState state) {
    List<Integer> selected = (List<Integer>) data.getAttributes().get(Attributes.SELECTED_LIST_STRING);
    if (selected == null && state != null) {
      selected = state.getSelectedRows();
    }
    if (selected == null) {
      selected = Collections.emptyList();
    }
    return selected;
  }

  private void link(final FacesContext facesContext, final Application application, String areaId,
                    final boolean disabled, final PageAction command, final UISheet data)
      throws IOException {
    final UICommand link = createPagingCommand(application, command, disabled);

    data.getFacets().put(command.getToken(), link);


    final String tip = ResourceManagerUtils.getPropertyNotNull(facesContext, "tobago",
        "sheet" + command.getToken());
    final String image = ResourceManagerUtils.getImage(facesContext,
        "image/sheet" + command.getToken() + (disabled ? "Disabled" : ""));

    final TobagoResponseWriter writer = HtmlRendererUtils.getTobagoResponseWriter(facesContext);
    writer.startElement(HtmlElements.IMG, null);
    writer.writeIdAttribute(data.getClientId(facesContext)
        + ComponentUtils.SUB_SEPARATOR + areaId + ComponentUtils.SUB_SEPARATOR + "pagingArrows"
        + ComponentUtils.SUB_SEPARATOR + command.getToken());
    final Classes pagerClasses = Classes.create(data, "footerPagerButton", disabled ? Markup.DISABLED : null);
    writer.writeClassAttribute(pagerClasses);
    writer.writeAttribute(HtmlAttributes.SRC, image, false);
    writer.writeAttribute(HtmlAttributes.TITLE, tip, true);
    writer.writeAttribute(HtmlAttributes.ALT, "", false);
    writer.writeAttribute(DataAttributes.DISABLED, disabled);
    writer.endElement(HtmlElements.IMG);
  }

  // TODO sheet.getColumnLayout() may return the wrong number of column...
  // TODO
  // TODO

  private void renderColumnHeaders(
      final FacesContext facesContext, final UISheet sheet, final TobagoResponseWriter writer,
      final List<AbstractUIColumn> renderedColumnList)
      throws IOException {

    final Grid grid = sheet.getHeaderGrid();
    if (grid == null) {
      LOG.warn("Can't render column headers, because grid == null. One reason can be, the you use nested sheets. "
          + "The inner sheet ensureHeader() will be called outside the iterating over the rows. "
          + "Nesting sheet is currently not supported.");
      return;
    }
    final List<Integer> columnWidths = sheet.getWidthList();

    if (LOG.isDebugEnabled()) {
      LOG.debug("*****************************************************");
      LOG.debug("" + grid);
      LOG.debug("*****************************************************");
    }

    writer.startElement(HtmlElements.DIV, sheet);
    writer.writeClassAttribute(Classes.create(sheet, "headerDiv"));
    writer.startElement(HtmlElements.TABLE, sheet);
    writer.writeAttribute(HtmlAttributes.CELLSPACING, "0", false);
    writer.writeAttribute(HtmlAttributes.CELLPADDING, "0", false);
    writer.writeAttribute(HtmlAttributes.SUMMARY, "", false);
    writer.writeClassAttribute(
        Classes.create(sheet, "headerTable").getStringValue()
            + " " + BootstrapClass.TABLE.getName()
            + " " + BootstrapClass.TABLE_BORDERED.getName());

    writeColgroup(writer, columnWidths);

    writer.startElement(HtmlElements.TBODY, sheet);
    for (int i = 0; i < grid.getRowCount(); i++) {
      writer.startElement(HtmlElements.TR, null);
      for (int j = 0; j < grid.getColumnCount(); j++) {
        final Cell cell = grid.getCell(j, i);
        if (cell instanceof OriginCell) {
//          writer.startElement(HtmlElements.TD, null);
          writer.startElement(HtmlElements.TH, null);
          if (cell.getColumnSpan() > 1) {
            writer.writeAttribute(HtmlAttributes.COLSPAN, cell.getColumnSpan());
          }
          if (cell.getRowSpan() > 1) {
            writer.writeAttribute(HtmlAttributes.ROWSPAN, cell.getRowSpan());
          }

          final UIComponent cellComponent = (UIComponent) cell.getComponent();
          final boolean pure = !(cellComponent instanceof UIOut);

          writer.startElement(HtmlElements.DIV, null);
          writer.writeClassAttribute(Classes.create(sheet, "headerCell"));
          writer.startElement(HtmlElements.SPAN, null);
          final Style headerStyle = new Style();
          Measure headerHeight = Measure.valueOf(20).multiply(cell.getRowSpan());
          if (!pure) {
            headerHeight = headerHeight.subtract(6); // XXX todo
          }
          headerStyle.setHeight(headerHeight);
          writer.writeStyleAttribute(headerStyle);
          final AbstractUIColumn column = renderedColumnList.get(j);
          String sorterImage = null;
          Markup markup = Markup.NULL;
          String tip = (String) column.getAttributes().get(Attributes.TIP);
          // sorter icons should only displayed when there is only 1 column and not input
          if (cell.getColumnSpan() == 1 && cellComponent instanceof UIOut) {
            final boolean sortable = ComponentUtils.getBooleanAttribute(column, Attributes.SORTABLE);
            if (sortable) {
              UICommand sortCommand = (UICommand) column.getFacet(Facets.SORTER);
              if (sortCommand == null) {
                final String columnId = column.getClientId(facesContext);
                final String sorterId = columnId.substring(columnId.lastIndexOf(":") + 1) + "_" + UISheet.SORTER_ID;
                sortCommand = (UICommand) CreateComponentUtils.createComponent(
                    facesContext, UICommand.COMPONENT_TYPE, RendererTypes.LINK, sorterId);
                column.getFacets().put(Facets.SORTER, sortCommand);
              }
              String[] clientIds = ComponentUtils.evaluateClientIds(facesContext, sheet, sheet.getRenderedPartially());
              if (clientIds.length == 0) {
                clientIds = new String[]{sheet.getClientId(facesContext)};
              }
              final CommandMap map = new CommandMap();
              final Command click = new Command(
                  sortCommand.getClientId(facesContext), null, null, null, clientIds, null, null, null, null, null);
              map.setClick(click);
              writer.writeAttribute(DataAttributes.COMMANDS, JsonUtils.encode(map), true);

              if (tip == null) {
                tip = "";
              } else {
                tip += " - ";
              }
              tip += ResourceManagerUtils.getPropertyNotNull(facesContext, "tobago", "sheetTipSorting");

              markup = markup.add(Markup.SORTABLE);

              final SheetState sheetState = sheet.getSheetState(facesContext);
              if (column.getId().equals(sheetState.getSortedColumnId())) {
                final String sortTitle;
                if (sheetState.isAscending()) {
                  sorterImage = "glyphicon-chevron-up";
                  sortTitle = ResourceManagerUtils.getPropertyNotNull(facesContext, "tobago", "sheetAscending");
                  markup = markup.add(Markup.ASCENDING);
                } else {
                  sorterImage = "glyphicon-chevron-down";
                  sortTitle = ResourceManagerUtils.getPropertyNotNull(facesContext, "tobago", "sheetDescending");
                  markup = markup.add(Markup.DESCENDING);
                }
                if (sortTitle != null) {
                  tip += " - " + sortTitle;
                }
              }
            }
          }

          if (j == 0) {
            markup = markup.add(Markup.FIRST);
          }
          if (pure) {
            markup = markup.add(Markup.PURE);
          }
          writer.writeClassAttribute(Classes.create(sheet, "header", markup));
          writer.writeAttribute(HtmlAttributes.TITLE, tip, true);

          if (column instanceof UIColumnSelector) {
            renderColumnSelectorHeader(facesContext, writer, sheet);
          } else {
            RenderUtils.encode(facesContext, cellComponent);

            final AbstractUIMenu dropDownMenu = FacetUtils.getDropDownMenu(column);
            // render sub menu popup button
            if (dropDownMenu != null && dropDownMenu.isRendered()) {

              writer.startElement(HtmlElements.SPAN, column);
              writer.writeClassAttribute(Classes.create(column, "menu"));

              writer.startElement(HtmlElements.IMG, column);
              final String menuImage
                  = ResourceManagerUtils.getImage(facesContext, "image/sheetSelectorMenu");
              writer.writeAttribute(HtmlAttributes.TITLE, "", false);
              writer.writeAttribute(HtmlAttributes.SRC, menuImage, false);
              writer.endElement(HtmlElements.IMG);
              ToolBarRendererBase.renderDropDownMenu(facesContext, writer, dropDownMenu);

              writer.endElement(HtmlElements.SPAN);
            }

          }

          if (sorterImage != null) {
            writer.startElement(HtmlElements.SPAN, null);
            writer.writeClassAttribute(BootstrapClass.GLYPHICON, BootstrapClass.glyphicon(sorterImage));
            writer.endElement(HtmlElements.SPAN);
          }

          writer.endElement(HtmlElements.SPAN);
          if (renderedColumnList.get(j).isResizable()) {
            encodeResizing(writer, sheet, j + cell.getColumnSpan() - 1);
          }
          writer.endElement(HtmlElements.DIV);

//          writer.endElement(HtmlElements.TD);
          writer.endElement(HtmlElements.TH);
        }
      }
      // add a filler column
//      writer.startElement(HtmlElements.TD, null);
      writer.startElement(HtmlElements.TH, null);
      writer.startElement(HtmlElements.DIV, null);
      // todo: is the filler class needed here?
      writer.writeClassAttribute(Classes.create(sheet, "headerCell", Markup.FILLER));
      writer.startElement(HtmlElements.SPAN, null);
      writer.writeClassAttribute(Classes.create(sheet, "header"));
      final Style headerStyle = new Style();
      headerStyle.setHeight(Measure.valueOf(14)); // XXX todo
      writer.writeStyleAttribute(headerStyle);
      writer.endElement(HtmlElements.SPAN);
      writer.endElement(HtmlElements.DIV);
//      writer.endElement(HtmlElements.TD);
      writer.endElement(HtmlElements.TH);

      writer.endElement(HtmlElements.TR);
    }
    writer.endElement(HtmlElements.TBODY);
    writer.endElement(HtmlElements.TABLE);
    writer.endElement(HtmlElements.DIV);
  }

  private void encodeResizing(final TobagoResponseWriter writer, final AbstractUISheet sheet, final int columnIndex)
      throws IOException {
    writer.startElement(HtmlElements.SPAN, null);
    writer.writeClassAttribute(Classes.create(sheet, "headerResize"));
    writer.writeAttribute(DataAttributes.COLUMN_INDEX, Integer.toString(columnIndex), false);
    writer.write("&nbsp;&nbsp;"); // is needed for IE
    writer.endElement(HtmlElements.SPAN);
  }

  protected void renderColumnSelectorHeader(
      final FacesContext facesContext, final TobagoResponseWriter writer, final UISheet sheet)
      throws IOException {

    final UIToolBar toolBar = createToolBar(facesContext, sheet);
    writer.startElement(HtmlElements.DIV, null);
    writer.writeClassAttribute(Classes.create(sheet, "toolBar"));

    if (UISheet.MULTI.equals(sheet.getSelectable())) {
      EncodeUtils.prepareRendererAll(facesContext, toolBar);
      RenderUtils.encode(facesContext, toolBar);
    }

    writer.endElement(HtmlElements.DIV);
  }

  private UIToolBar createToolBar(final FacesContext facesContext, final UISheet sheet) {
    final Application application = facesContext.getApplication();
    final UICommand dropDown = (UICommand) CreateComponentUtils.createComponent(
        facesContext, UICommand.COMPONENT_TYPE, null, "dropDown");
    final UIMenu menu = (UIMenu) CreateComponentUtils.createComponent(
        facesContext, UIMenu.COMPONENT_TYPE, RendererTypes.MENU, "menu");
    FacetUtils.setDropDownMenu(dropDown, menu);
    final String sheetId = sheet.getClientId(facesContext);

    createMenuItem(facesContext, menu, "sheetMenuSelect", Markup.SHEET_SELECT_ALL, sheetId);
    createMenuItem(facesContext, menu, "sheetMenuUnselect", Markup.SHEET_DESELECT_ALL, sheetId);
    createMenuItem(facesContext, menu, "sheetMenuToggleselect", Markup.SHEET_TOGGLE_ALL, sheetId);

    final UIToolBar toolBar = (UIToolBar) application.createComponent(UIToolBar.COMPONENT_TYPE);
    toolBar.setId(facesContext.getViewRoot().createUniqueId());
    toolBar.setRendererType("TabGroupToolBar");
    toolBar.setTransient(true);
    toolBar.getChildren().add(dropDown);
    sheet.getFacets().put(Facets.TOOL_BAR, toolBar);
    return toolBar;
  }

  private void createMenuItem(
      final FacesContext facesContext, final UIMenu menu, final String label, final Markup markup,
      final String sheetId) {
    final String id = markup.toString();
    final UIMenuCommand menuItem = (UIMenuCommand) CreateComponentUtils.createComponent(
        facesContext, UIMenuCommand.COMPONENT_TYPE, RendererTypes.MENU_COMMAND, id);
    menuItem.setLabel(ResourceManagerUtils.getPropertyNotNull(facesContext, "tobago", label));
    menuItem.setMarkup(markup);
    menuItem.setOmit(true);
    ComponentUtils.putDataAttributeWithPrefix(menuItem, DataAttributes.SHEET_ID, sheetId);
    menu.getChildren().add(menuItem);
  }

  private void writeDirectPagingLinks(
      final TobagoResponseWriter writer, final FacesContext facesContext, final Application application,
      final UISheet sheet)
      throws IOException {
    UICommand pagerCommand = (UICommand) sheet.getFacet(Facets.PAGER_PAGE);
    if (pagerCommand == null) {
      pagerCommand = createPagingCommand(application, PageAction.TO_PAGE, false);
      sheet.getFacets().put(Facets.PAGER_PAGE, pagerCommand);
    }
    final String pagerCommandId = pagerCommand.getClientId(facesContext);
    int linkCount = ComponentUtils.getIntAttribute(sheet, Attributes.DIRECT_LINK_COUNT);
    linkCount--;  // current page needs no link
    final ArrayList<Integer> prevs = new ArrayList<Integer>(linkCount);
    int page = sheet.getCurrentPage() + 1;
    for (int i = 0; i < linkCount && page > 1; i++) {
      page--;
      if (page > 0) {
        prevs.add(0, page);
      }
    }

    final ArrayList<Integer> nexts = new ArrayList<Integer>(linkCount);
    page = sheet.getCurrentPage() + 1;
    final int pages = sheet.hasRowCount() || sheet.isRowsUnlimited() ? sheet.getPages() : Integer.MAX_VALUE;
    for (int i = 0; i < linkCount && page < pages; i++) {
      page++;
      if (page > 1) {
        nexts.add(page);
      }
    }

    if (prevs.size() > (linkCount / 2)
        && nexts.size() > (linkCount - (linkCount / 2))) {
      while (prevs.size() > (linkCount / 2)) {
        prevs.remove(0);
      }
      while (nexts.size() > (linkCount - (linkCount / 2))) {
        nexts.remove(nexts.size() - 1);
      }
    } else if (prevs.size() <= (linkCount / 2)) {
      while (prevs.size() + nexts.size() > linkCount) {
        nexts.remove(nexts.size() - 1);
      }
    } else {
      while (prevs.size() + nexts.size() > linkCount) {
        prevs.remove(0);
      }
    }

    final boolean bootstrap = ClientProperties.getInstance(facesContext).getTheme().getName().equals("bootstrap");
    String name;
    name = bootstrap ? "«" : "...";
    int skip = prevs.size() > 0 ? prevs.get(0) : 1;
    if (!sheet.isShowDirectLinksArrows() && skip > 1) {
      skip -= (linkCount - (linkCount / 2));
      skip--;
      name = "...";
      if (skip < 1) {
        skip = 1;
        if (prevs.get(0) == 2) {
          name = "1";
        }
      }
      writeLinkElement(writer, sheet, name, skip, PagingLinkType.NORMAL);
    } else {
      writeLinkElement(writer, sheet, name, null, PagingLinkType.DISABLED);
    }
    for (final Integer prev : prevs) {
      name = prev.toString();
      writeLinkElement(writer, sheet, name, prev, PagingLinkType.NORMAL);
    }
    name = Integer.toString(sheet.getCurrentPage() + 1);
    writeLinkElement(writer, sheet, name, sheet.getCurrentPage() + 1, PagingLinkType.CURRENT);

    for (final Integer next : nexts) {
      name = next.toString();
      writeLinkElement(writer, sheet, name, next, PagingLinkType.NORMAL);
    }

    // XXX hack bootstrap
    name = bootstrap ? "»" : "...";
    skip = nexts.size() > 0 ? nexts.get(nexts.size() - 1) : pages;
    if (!sheet.isShowDirectLinksArrows() && skip < pages) {
      skip += linkCount / 2;
      skip++;
      name = "...";
      if (skip > pages) {
        skip = pages;
        if ((nexts.get(nexts.size() - 1)) == skip - 1) {
          name = Integer.toString(skip);
        }
      }
      writeLinkElement(writer, sheet, name, skip, PagingLinkType.NORMAL);
    } else {
      writeLinkElement(writer, sheet, name, null, PagingLinkType.DISABLED);
    }
  }

  private enum PagingLinkType {
    NORMAL, CURRENT, DISABLED
  }

  private UICommand createPagingCommand(
      final Application application, final PageAction command, final boolean disabled) {
    final UICommand link;
    link = (UICommand) application.createComponent(UICommand.COMPONENT_TYPE);
    link.setRendererType(RendererTypes.SHEET_PAGE_COMMAND);
    link.setRendered(true);
    link.setId(command.getToken());
    link.getAttributes().put(Attributes.INLINE, Boolean.TRUE);
    link.getAttributes().put(Attributes.DISABLED, disabled);
    return link;
  }

  private void writeLinkElement(
      final TobagoResponseWriter writer, final UISheet sheet, final String name, final Integer toPage,
      final PagingLinkType pagingLinkType)
      throws IOException {
    String innerElement = pagingLinkType == PagingLinkType.NORMAL ? HtmlElements.A : HtmlElements.SPAN;

    writer.startElement(HtmlElements.LI, null);
    if (pagingLinkType == PagingLinkType.CURRENT) {
      writer.writeClassAttribute(BootstrapClass.ACTIVE);
    }
    if (pagingLinkType == PagingLinkType.DISABLED) {
      writer.writeClassAttribute(BootstrapClass.DISABLED);
    }
    writer.startElement(innerElement, null);
    writer.writeClassAttribute(Classes.create(sheet, "pagingLink"));
    if (pagingLinkType == PagingLinkType.NORMAL) {
      writer.writeAttribute(DataAttributes.TO_PAGE, toPage);
      writer.writeAttribute(HtmlAttributes.HREF, "#", false);
    }
    writer.flush();
    writer.write(name);
    writer.endElement(innerElement);
    writer.endElement(HtmlElements.LI);
  }

  private Measure getContentBorder(final FacesContext facesContext, final UISheet data) {
    return getBorderLeft(facesContext, data).add(getBorderRight(facesContext, data));
  }

  @Override
  public void encodeChildren(final FacesContext context, final UIComponent component) throws IOException {
    // DO Nothing
  }

  @Override
  public boolean getPrepareRendersChildren() {
    return true;
  }

  @Override
  public void prepareRendersChildren(final FacesContext facesContext, final UIComponent component) throws IOException {
    final UISheet sheet = (UISheet) component;
    for (final UIColumn column : sheet.getRenderedColumns()) {
      if (column instanceof AbstractUIColumnNode) {
        if (LOG.isDebugEnabled()) {
          LOG.debug("TODO: AbstractUIColumnNode are not prepared.");
        }
        // TBD: when the column should be prepared for rendering, I think we need to
        // TBD: iterate over each row to prepare it.
        // TBD: in the moment this method TreeNodeRendererBase.prepareRender() will not be called in sheets
      } else {
        EncodeUtils.prepareRendererAll(facesContext, column);
      }
    }
  }
}
