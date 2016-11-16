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

package org.apache.myfaces.tobago.internal.renderkit;

import org.apache.myfaces.tobago.component.ClientBehaviors;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Map of commands to be send to the user agent.
 * It contains the command which shall be executed by click or other events.
 *
 * @since 2.0.0
 */
public class CommandMap {

  private Command click;
  private Map<ClientBehaviors, Command> other;

  /**
   * Creates an empty command map, which may hold different command triggered by different keys.
   */
  public CommandMap() {
  }

  /**
   * Creates a command map, which hold the given command triggered by "click".
   */
  public CommandMap(final Command click) {
    this.click = click;
  }

  public void setClick(final Command click) {
    this.click = click;
  }

  public Command getClick() {
    return click;
  }

  public void addCommand(final ClientBehaviors name, final Command command) {
    if (name == ClientBehaviors.click) {
      setClick(command);
    } else {
      if (other == null) {
        other = new HashMap<ClientBehaviors, Command>();
      }

      other.put(name, command);
    }
  }

  public Map<ClientBehaviors, Command> getOther() {
    if (other != null) {
      return Collections.unmodifiableMap(other);
    } else {
      return null;
    }
  }

  public void merge(final CommandMap commandMap) {
    final Command click = commandMap.getClick();
    if (click != null) {
      setClick(click);
    } else {
      for (Map.Entry<ClientBehaviors, Command> entry : commandMap.getOther().entrySet()) {
        addCommand(entry.getKey(), entry.getValue());
      }
    }
  }

  public boolean isEmpty() {
    return click == null && other == null;
  }
}