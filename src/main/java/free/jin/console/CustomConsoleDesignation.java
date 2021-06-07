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
package free.jin.console;

import free.jin.Connection;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * A console designation for custom, user-created consoles.
 *
 * @author Maryanovsky Alexander
 */
public abstract class CustomConsoleDesignation extends ChatConsoleDesignation {

  /** The list of channels we're displaying. */
  protected final List channels;

  /** A regular expression we match messages with, and display them if they match. */
  protected final Pattern messageRegex;

  /**
   * Creates a new <code>CustomConsoleDesignation</code>.
   *
   * @param connection The connection to the server.
   * @param name The name of the console.
   * @param encoding The encoding.
   * @param isConsoleCloseable Whether the console is closeable.
   * @param channels The list of channels displayed in the console.
   * @param messageRegex A regular expression to match messages to and display them if they match.
   */
  public CustomConsoleDesignation(
      Connection connection,
      String name,
      String encoding,
      boolean isConsoleCloseable,
      List channels,
      Pattern messageRegex) {
    super(connection, name, encoding, isConsoleCloseable);

    this.channels = new LinkedList(channels);
    this.messageRegex = messageRegex;
  }
}
