/**
 * Jin - a chess client for internet chess servers. More information is available at
 * http://www.jinchess.com/. Copyright (C) 2008 Alexander Maryanovsky. All rights reserved.
 *
 * This program is free software; you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation; either version 2 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program; if
 * not, write to the Free Software Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA
 * 02111-1307, USA.
 */
package free.jin.console.icc;

import free.jin.Connection;
import free.jin.console.ics.IcsGeneralChatConsoleDesignation;
import free.jin.event.ChatEvent;

/**
 * The "general chat" console for the WCL server. This displays the Lobby channel (250).
 *
 * @author Maryanovsky Alexander
 */
public class WclGeneralChatConsoleDesignation extends IcsGeneralChatConsoleDesignation {

  /**
   * Channel 250 id.
   */
  private static final Integer LOBBY_ID = new Integer(250);

  /**
   * Creates a new <code>WclGeneralChatConsoleDesignation</code>.
   */
  public WclGeneralChatConsoleDesignation(
      Connection connection, String encoding, boolean isConsoleCloseable) {
    super(connection, encoding, isConsoleCloseable);

    addAccepted("channel-tell", LOBBY_ID, ANY_SENDER);
    addAccepted("channel-qtell", LOBBY_ID, ANY_SENDER);
    addAccepted("announcement", null, ANY_SENDER);
  }

  /**
   * Returns whether the specified chat event is a channel 250 tell.
   */
  @Override
  protected boolean isStandardChatMessage(ChatEvent evt) {
    return "channel-tell".equals(evt.getType()) && LOBBY_ID.equals(evt.getForum());
  }

  /**
   * Sends the specified text as a shout.
   */
  @Override
  protected void sendStandardChatMessage(String userText) {
    sendCommand("tell " + LOBBY_ID + " " + userText);
  }
}
