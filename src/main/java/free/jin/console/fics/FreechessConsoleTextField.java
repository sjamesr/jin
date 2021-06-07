/**
 * Jin - a chess client for internet chess servers. More information is available at
 * http://www.jinchess.com/. Copyright (C) 2002 Alexander Maryanovsky. All rights reserved.
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

import free.jin.ServerUser;
import free.jin.console.ConsoleTextField;

/** An extension of ConsoleTextField which implements some FICS specific features. */
public class FreechessConsoleTextField extends ConsoleTextField {

  /** Creates a new FreechessConsoleTextField which will be a part of the given FreechessConsole. */
  public FreechessConsoleTextField(FreechessConsole console) {
    super(console);
  }

  /**
   * Sets the textfield to be ready to send a tell to the given user. Sets the text in the textfield
   * to <code>"tell " + playerName + "! "</code> and puts the caret at the end of the text.
   */
  @Override
  protected void setTellPersonState(ServerUser user) {
    String text = "tell " + user.getName() + "! ";
    setText(text);
    setCaretPosition(text.length());
  }
}
