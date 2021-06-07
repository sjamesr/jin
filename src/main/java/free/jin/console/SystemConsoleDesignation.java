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
import free.jin.FriendsConnection;
import free.jin.I18n;
import free.jin.event.ChatEvent;
import free.jin.event.FriendsEvent;
import free.jin.event.FriendsListener;
import free.jin.event.JinEvent;

/**
 * A base implementation of the designation for a "System" console. This class is meant to be
 * subclassed by server-specific classes.
 */
public abstract class SystemConsoleDesignation extends AbstractConsoleDesignation
    implements FriendsListener {

  /**
   * Creates a new <code>SystemConsoleDesignation</code>.
   *
   * @param connection The connection to the server.
   * @param encoding The encoding to use for encoding/decoding messages.
   */
  public SystemConsoleDesignation(Connection connection, String encoding) {
    super(connection, I18n.get(SystemConsoleDesignation.class).getString("name"), encoding, false);

    addCommandType(new IssueCommand());
  }

  /**
   * Registers ourselves as <code>FriendsListener</code>, if the connection implements <code>
   * FriendsConnection</code>.
   */
  @Override
  protected void joinForums() {
    if (connection instanceof FriendsConnection)
      ((FriendsConnection) connection).getFriendsListenerManager().addFriendsListener(this);
  }

  /** The system console displays all events. */
  @Override
  protected boolean accept(JinEvent evt) {
    return true;
  }

  /**
   * Appends the text for the specified chat event to the console and notifies the console of any
   * personal tells received..
   */
  @Override
  protected void appendChat(ChatEvent evt) {
    super.appendChat(evt);

    if (isPersonalTell(evt)) getConsole().personalTellReceived(evt.getSender());
  }

  /** Returns whether the specified chat event is a personal tell to the user. */
  protected boolean isPersonalTell(ChatEvent evt) {
    return (evt.getCategory() == ChatEvent.PERSON_TO_PERSON_CHAT_CATEGORY);
  }

  /** Invoked when a friend connects. */
  @Override
  public void friendConnected(FriendsEvent evt) {
    I18n i18n = I18n.get(getClass(), SystemConsoleDesignation.class);
    Console console = getConsole();

    String text =
        i18n.getFormattedString("friendConnected", new Object[] {evt.getFriend().getName()});
    console.addToOutput(text, console.textTypeForEvent(evt));
  }

  /** Invoked when a friend disconnects. */
  @Override
  public void friendDisconnected(FriendsEvent evt) {
    I18n i18n = I18n.get(getClass(), SystemConsoleDesignation.class);
    Console console = getConsole();

    String text =
        i18n.getFormattedString("friendDisconnected", new Object[] {evt.getFriend().getName()});
    console.addToOutput(text, console.textTypeForEvent(evt));
  }

  @Override
  public void friendAdded(FriendsEvent evt) {}

  @Override
  public void friendRemoved(FriendsEvent evt) {}

  @Override
  public void friendStateChanged(FriendsEvent evt) {}

  /** A command type which sends the command typed by the user as-is to the server. */
  private class IssueCommand extends AbstractCommandType {

    /** Creates a new <code>IssueCommand</code>. */
    public IssueCommand() {
      super(I18n.get(IssueCommand.class).getString("name"));
    }

    /** Issues the specified command. */
    @Override
    protected void send(String command) {
      sendCommand(command);
    }

    /** Echoes the specified command to the console. */
    @Override
    protected void echo(String command) {
      echoCommand(command);
    }
  }
}
