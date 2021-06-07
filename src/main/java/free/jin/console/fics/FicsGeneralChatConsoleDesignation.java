/**
 * Jin - a chess client for internet chess servers. More information is available at
 * http://www.jinchess.com/. Copyright (C) 2008 Alexander Maryanovsky. All rights reserved.
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
package free.jin.console.fics;

import free.jin.Connection;
import free.jin.console.ics.IcsGeneralChatConsoleDesignation;
import free.jin.event.ChatEvent;

/**
 * A FICS-specific general chat console. This displays shouts.
 *
 * @author Maryanovsky Alexander
 */
public class FicsGeneralChatConsoleDesignation extends IcsGeneralChatConsoleDesignation {

  /** Creates a new <code>FicsGeneralChatConsoleDesignation</code>. */
  public FicsGeneralChatConsoleDesignation(
      Connection connection, String encoding, boolean isConsoleCloseable) {
    super(connection, encoding, isConsoleCloseable);
  }

  /** Returns whether the specified chat event is a shout. */
  @Override
  protected boolean isStandardChatMessage(ChatEvent evt) {
    return "shout".equals(evt.getType());
  }

  /** Sends the specified text as a shout. */
  @Override
  protected void sendStandardChatMessage(String userText) {
    sendCommand("shout " + userText);
  }
}
