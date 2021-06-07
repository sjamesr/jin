/**
 * Jin - a chess client for internet chess servers. More information is available at
 * http://www.jinchess.com/. Copyright (C) 2003 Alexander Maryanovsky. All rights reserved.
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
package free.jin.console.icc;

import free.jin.console.Console;
import free.jin.console.ConsoleTextPane;

/** An extension of <code>ConsoleTextPane</code> adding ICC specific functionality. */
public class ChessclubConsoleTextPane extends ConsoleTextPane {

  /**
   * Creates a new <code>ChessclubConsoleTextPane</code> with the specified <code>Console</code>.
   */
  public ChessclubConsoleTextPane(Console console) {
    super(console);
  }

  /**
   * Overrides </code>ConsoleTextPane.isWordChar(char)</code> to specify that a dash character is
   * also a word character. We do this because ICC handles may include a dash and we want them to be
   * easily selectable.
   */
  @Override
  protected boolean isWordChar(char c) {
    return super.isWordChar(c) || (c == '-');
  }
}
