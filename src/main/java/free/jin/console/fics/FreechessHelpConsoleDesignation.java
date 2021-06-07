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
package free.jin.console.fics;

import free.jin.Connection;
import free.jin.console.HelpConsoleDesignation;
import free.jin.event.ChatEvent;
import free.jin.event.JinEvent;

/** A console designation for FICS's help channel. */
public class FreechessHelpConsoleDesignation extends HelpConsoleDesignation {

  /**
   * Creates a new <code>FreechessHelpConsoleDesignation</code>.
   *
   * @param connection The connection to the server.
   * @param encoding The encoding to use for encoding/decoding messages.
   * @param isConsoleCloseable Whether the console should be closeable.
   */
  public FreechessHelpConsoleDesignation(
      Connection connection, String encoding, boolean isConsoleCloseable) {
    super(connection, false, encoding, isConsoleCloseable);
  }

  /** Returns whether the specified event is a help channel tell. */
  @Override
  protected boolean accept(JinEvent evt) {
    if (isTaggedByUs(evt)) return true;

    if (!(evt instanceof ChatEvent)) return false;

    ChatEvent chatEvent = (ChatEvent) evt;
    return "channel-tell".equals(chatEvent.getType())
        && new Integer(1).equals(chatEvent.getForum());
  }
}
