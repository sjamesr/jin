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
import free.jin.ServerUser;
import free.jin.event.ChatEvent;
import free.jin.event.JinEvent;

/** A <code>ConsoleDesignation</code> for person-to-person chat. */
public class PersonalChatConsoleDesignation extends AbstractConsoleDesignation {

  /** The user with whom we're chatting. */
  private final ServerUser conversationPartner;

  /**
   * Creates a new <code>PersonalChatConsoleDesignation</code>.
   *
   * @param connection The connection to the server.
   * @param conversationPartner The user we're talking to.
   * @param encoding The encoding to use for the conversation.
   * @param isConsoleCloseable Whether the console should be closeable.
   */
  public PersonalChatConsoleDesignation(
      Connection connection,
      ServerUser conversationPartner,
      String encoding,
      boolean isConsoleCloseable) {
    super(connection, true, conversationPartner.getName(), encoding, isConsoleCloseable);

    this.conversationPartner = conversationPartner;

    addCommandType(new SendPersonalTell());
  }

  /** Joins personal chat with out conversation partner. */
  @Override
  protected void joinForums() {
    connection.joinPersonalChat(conversationPartner);
  }

  /** Accepts only person-to-person chat events. */
  @Override
  protected boolean accept(JinEvent evt) {
    if (isTaggedByUs(evt)) return true;

    if (!(evt instanceof ChatEvent)) return false;

    ChatEvent chatEvent = (ChatEvent) evt;
    return (chatEvent.getCategory() == ChatEvent.PERSON_TO_PERSON_CHAT_CATEGORY)
        && chatEvent.getSender().equals(conversationPartner);
  }

  /**
   * Returns whether the specified object is a <code>PersonalChatConsoleDesignation</code> equals to
   * this one. Two <code>PersonalChatConsoleDesignation</code>s are equal if they have the same
   * conversation partner.
   */
  @Override
  public boolean equals(Object o) {
    if (!(o instanceof PersonalChatConsoleDesignation)) return false;

    PersonalChatConsoleDesignation designation = (PersonalChatConsoleDesignation) o;
    return designation.conversationPartner.equals(conversationPartner);
  }

  /** Returns the hash code of this <code>PersonalChatConsoleDesignation</code>. */
  @Override
  public int hashCode() {
    return conversationPartner.hashCode();
  }

  /** A command type which sends a personal tell to our conversation partner. */
  private class SendPersonalTell extends AbstractCommandType {

    /** Creates a new <code>SendPersonalTell</code> command type. */
    public SendPersonalTell() {
      super(
          I18n.get(SendPersonalTell.class)
              .getFormattedString("name", new Object[] {conversationPartner.getName()}));
    }

    /** Sends the specified personal tell to our conversation partner. */
    @Override
    protected void send(String message) {
      connection.sendPersonalTell(conversationPartner, encode(message), getTag());
    }

    /** Echoes the message to the console. */
    @Override
    protected void echo(String userText) {
      Console console = getConsole();
      console.addToOutput(
          connection.getUser().getName() + ": " + userText, console.getUserTextType());
    }
  }
}
