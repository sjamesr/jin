/**
 * Jin - a chess client for internet chess servers. More information is available at
 * http://www.jinchess.com/. Copyright (C) 2007 Alexander Maryanovsky. All rights reserved.
 *
 * <p>This program is free software; you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation; either version 2 of the
 * License, or (at your option) any later version.
 *
 * <p>This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * <p>You should have received a copy of the GNU General Public License along with this program; if
 * not, write to the Free Software Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA
 * 02111-1307, USA.
 */
package free.jin.console;

import free.jin.event.JinEvent;
import free.util.Named;
import java.beans.PropertyChangeListener;
import java.util.List;

/**
 * Defines the types of events a console is willing to display and the types of commands a user
 * might issue through that console.
 */
public interface ConsoleDesignation {

  /** Adds a property change listener. */
  void addPropertyChangeListener(PropertyChangeListener listener);

  /** Removes a property change listener. */
  void removePropertyChangeListener(PropertyChangeListener listener);

  /** Returns the name of this designation. */
  String getName();

  /**
   * This method is invoked to tell the designation what console it is responsible for. It is
   * invoked before any calls to {@link #receive(JinEvent)}.
   */
  void setConsole(Console console);

  /**
   * Receives the specified <code>JinEvent</code>, possibly displaying it in some manner in the
   * console. Returns whether the console "accepted" the event (usually means that it did indeed
   * display it in the console).
   */
  boolean receive(JinEvent evt);

  /** Returns the list of command types this console designation is able to issue. */
  List getCommandTypes();

  /** Returns whether the console is closeable. */
  boolean isConsoleCloseable();

  /** Defines a certain type of command which can be issued by the user. */
  public interface CommandType extends Named {

    /**
     * Invoked when a command is issued by the user with this command type.
     *
     * @param userText The text entered by the user.
     * @param doNotEcho Set if the command should not be echoed to the console.
     */
    public abstract void handleCommand(String userText, boolean doNotEcho);
  }
}
