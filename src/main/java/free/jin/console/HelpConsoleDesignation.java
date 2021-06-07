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

import free.jin.Connection;
import free.jin.I18n;

/** A base implementation of a console designation for the server's interactive helping system. */
public abstract class HelpConsoleDesignation extends AbstractConsoleDesignation {

  /**
   * Creates a new <code>HelpConsoleDesignation</code>.
   *
   * @param connection The connection to the server.
   * @param countUnseenMessages Whether to count the number of unseen messages and display it in the
   *     designation's name.
   * @param encoding The encoding to use for encoding/decoding messages.
   * @param isConsoleCloseable Whether the console should be closeable.
   */
  public HelpConsoleDesignation(
      Connection connection, boolean countUnseenMessages, String encoding, boolean isCloseable) {
    super(
        connection,
        countUnseenMessages,
        I18n.get(HelpConsoleDesignation.class).getString("name"),
        encoding,
        isCloseable);

    addCommandType(new AskHelpQuestion());
  }

  /** Adds the instructions text to the console. */
  @Override
  public void setConsole(Console console) {
    super.setConsole(console);

    console.addToOutput(
        I18n.get(getClass(), HelpConsoleDesignation.class).getString("instructions"), "info");
  }

  /** Joins the help forum. */
  @Override
  protected void joinForums() {
    connection.joinHelpForum();
  }

  /** A <code>CommandType</code> which asks a help question. */
  private class AskHelpQuestion extends AbstractCommandType {

    /** Creates a new <code>AskHelpQuestion</code> command type. */
    public AskHelpQuestion() {
      super(I18n.get(AskHelpQuestion.class).getString("name"));
    }

    /** Asks the specified help question. */
    @Override
    protected void send(String userText) {
      connection.sendHelpQuestion(encode(userText), getTag());
    }

    /** Echoes the question to the console. */
    @Override
    protected void echo(String userText) {
      Console console = getConsole();
      console.addToOutput(
          connection.getUser().getName() + ": " + userText, console.getUserTextType());
    }
  }
}
