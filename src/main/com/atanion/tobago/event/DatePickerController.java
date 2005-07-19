package com.atanion.tobago.event;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.atanion.tobago.TobagoConstants;
import com.atanion.tobago.config.ThemeConfig;
import com.atanion.tobago.component.ComponentUtil;
import com.atanion.util.logging.LogUtils;

import javax.faces.el.MethodBinding;
import javax.faces.el.EvaluationException;
import javax.faces.el.MethodNotFoundException;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.faces.component.UIComponent;
import javax.servlet.ServletRequest;
import java.util.StringTokenizer;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: weber
 * Date: Jul 18, 2005
 * Time: 3:24:46 PM
 * To change this template use File | Settings | File Templates.
 */
public class DatePickerController extends MethodBinding {

  private static final Log LOG = LogFactory.getLog(DatePickerController.class);
  public static final String OPEN_POPUP = "openPopup";
  public static final String CLOSE_POPUP = "closePopup";

  private static DatePickerController instance;

  private DatePickerController() {
  }

  public static synchronized DatePickerController getInstance() {
    if (instance == null) {
      instance = new DatePickerController();
    }
    return instance;
  }

  public Object invoke(FacesContext facesContext, Object[] objects)
      throws EvaluationException, MethodNotFoundException {

    if (objects[0] instanceof ActionEvent) {
      javax.faces.component.UICommand command =
          (javax.faces.component.UICommand) ((ActionEvent) objects[0]).getSource();
      final String commandId = command.getClientId(facesContext);
      LOG.info("commandId = " + commandId);
      if (LOG.isDebugEnabled()) {
        LOG.debug("commandId = " + commandId);
      }
      if (commandId.endsWith(OPEN_POPUP)) {
        LogUtils.logParameter((ServletRequest) facesContext.getExternalContext().getRequest());


        final UIComponent popup
            = command.getFacet(TobagoConstants.FACET_PICKER_POPUP);
        if (popup != null) {
          popup.setRendered(true);
          final String dimensionParameter
              = commandId.substring(0, commandId.length() - OPEN_POPUP.length()) + "Dimension";
          LOG.info("dimensionParameter = " + dimensionParameter);
          final String dimension = (String) facesContext.getExternalContext().getRequestParameterMap().get(dimensionParameter);
          LOG.info("Dimension = " + dimension);

          int width = -1;
          int height = -1;
          int left = -1;
          int top = -1;
          StringTokenizer st = new StringTokenizer(dimension, "x:");
          width = nextDimensionToken(st, 800);
          height = nextDimensionToken(st, 600);
          left = nextDimensionToken(st, -1);
          top = nextDimensionToken(st, -1);
          int popupWidth = ComponentUtil.getIntAttribute(popup, TobagoConstants.ATTR_WIDTH, -1);
          int popupHeight = ComponentUtil.getIntAttribute(popup, TobagoConstants.ATTR_HEIGHT, -1);
          int popupLeft = ComponentUtil.getIntAttribute(popup, TobagoConstants.ATTR_LEFT, -1);
          int popupTop = ComponentUtil.getIntAttribute(popup, TobagoConstants.ATTR_TOP, -1);

          final Map<String, String>  attributes = popup.getAttributes();
          if (popupWidth == -1) {
            popupWidth = 300;
            attributes.put(TobagoConstants.ATTR_WIDTH, String.valueOf(popupWidth));
          }
          if (popupHeight == -1) {
            popupHeight = 300;
            attributes.put(TobagoConstants.ATTR_HEIGHT, String.valueOf(popupHeight));
          }

          if (popupLeft == -1) {
            if (left != -1) {
              int popupRight = left + (popupWidth / 2);
              popupLeft = popupRight < width ? popupRight - popupWidth : width - popupWidth;
            } else {
              popupLeft = (width - popupWidth) / 2;
            }
            attributes.put(TobagoConstants.ATTR_LEFT,
                (popupLeft > 0 ? String.valueOf(popupLeft) : "0"));
          }

          if (popupTop == -1) {
            if (top != -1) {
              final int fixedHeight = ThemeConfig.getValue(facesContext, command, "fixedHeight");
              int popupBottom = top + popupHeight + fixedHeight;
              popupTop = popupBottom < height ? top + fixedHeight : height - popupHeight;
            } else {
              popupTop = (height - popupHeight) / 2;
            }
            attributes.put(TobagoConstants.ATTR_TOP,
                (popupTop > 0 ? String.valueOf(popupTop) : "0"));
          }
        }
      } else if (commandId.endsWith(CLOSE_POPUP)) {
        command.getParent().getParent().setRendered(false);
      }

    }
    return null;
  }

  private int nextDimensionToken(StringTokenizer st, int defaultValue) {
    if (st.hasMoreTokens()) {
      try {
        defaultValue = Integer.parseInt(st.nextToken());
      } catch (NumberFormatException e) {
        LOG.error("Catched: " + e.getMessage(), e);
      }
    }
    return defaultValue;
  }

  public Class getType(FacesContext facesContext) throws MethodNotFoundException {
    return String.class;
  }
}
