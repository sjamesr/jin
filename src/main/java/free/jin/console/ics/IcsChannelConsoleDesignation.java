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
package free.jin.console.ics;

import free.jin.Connection;
import free.jin.I18n;
import free.jin.console.Channel;
import free.jin.console.ChatConsoleDesignation;
import free.jin.console.Console;
import free.jin.event.ChatEvent;

/**
 * A chat console designation which displays a single channel.
 */
public class IcsChannelConsoleDesignation extends ChatConsoleDesignation {

  /**
   * The channel we're displaying.
   */
  private final Channel channel;

  /**
   * Creates a new <code>IcsChannelConsoleDesignation</code> for the specified set of channels.
   *
   * @param connection The connection to the server.
   * @param channel The channel to display.
   * @param encoding The encoding to use for encoding/decoding messages.
   * @param isConsoleCloseable Whether the console should be closeable.
   */
  public IcsChannelConsoleDesignation(
      Connection connection, Channel channel, String encoding, boolean isConsoleCloseable) {
    super(connection, channel.getShortName(), encoding, isConsoleCloseable);

    this.channel = channel;

    addAccepted("channel-tell", channel.getId(), ANY_SENDER);
    addAccepted("channel-qtell", channel.getId(), ANY_SENDER);
    addAccepted("announcement", null, ANY_SENDER);

    String commandName =
        I18n.get(IcsChannelConsoleDesignation.class).getString("sendChannelTellCommandName");
    addCommandType(
        new AbstractCommandType(commandName) {
          @Override
          protected void send(String userText) {
            Channel channel = IcsChannelConsoleDesignation.this.channel;
            sendCommand("xtell " + channel.getId() + " " + userText);
          }
        });
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected void appendChat(ChatEvent evt) {
    if ("channel-tell".equals(evt.getType())) {
      String senderName = evt.getSender().getName();
      String senderTitle = evt.getSenderTitle();
      int senderRating = evt.getSenderRating();
      String message = decode(evt.getMessage());

      String text =
          senderName
              + (senderTitle == null ? "" : senderTitle)
              + (senderRating == -1 ? "" : "(" + senderRating + ")")
              + ": "
              + message;

      Console console = getConsole();
      console.addToOutput(text, console.textTypeForEvent(evt));
    } else super.appendChat(evt);
  }
}
