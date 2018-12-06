/**
 * Jin - a chess client for internet chess servers. More information is available at
 * http://www.jinchess.com/. Copyright (C) 2007 Alexander Maryanovsky. All rights reserved.
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
package free.jin.console.fics;

import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.regex.Pattern;

import free.jin.Game;
import free.jin.I18n;
import free.jin.ServerUser;
import free.jin.console.Console;
import free.jin.console.ConsoleDesignation;
import free.jin.console.PersonalChatConsoleDesignation;
import free.jin.console.ics.IcsConsoleManager;
import free.jin.console.ics.IcsCustomConsoleDesignation;
import free.jin.event.ChatEvent;
import free.jin.ui.PreferencesPanel;

/**
 * An extension of the default ConsoleManager for the freechess.org server.
 */
public class FreechessConsoleManager extends IcsConsoleManager {

  /**
   * {@inheritDoc}
   */
  @Override
  protected String getDefaultTextForChat(ChatEvent evt, String encoding) {
    String type = evt.getType();
    ServerUser sender = evt.getSender();
    String title = evt.getSenderTitle();
    String rating = evt.getSenderRating() == -1 ? "----" : String.valueOf(evt.getSenderRating());
    String message = decode(evt.getMessage(), encoding);
    Object forum = evt.getForum();

    if (evt.getCategory() == ChatEvent.GAME_CHAT_CATEGORY) forum = ((Game) forum).getID();

    Object[] args =
        new Object[] {String.valueOf(sender), title, rating, String.valueOf(forum), message};

    I18n i18n = I18n.get(FreechessConsoleManager.class);
    return i18n.getFormattedString(type + ".displayPattern", args);
  }

  /**
   * Returns a FICS-specific system console designation.
   */
  @Override
  protected ConsoleDesignation createSystemConsoleDesignation() {
    return new FreechessSystemConsoleDesignation(getConn(), getEncoding());
  }

  /**
   * Returns a FICS-specific help console designation.
   */
  @Override
  protected ConsoleDesignation createHelpConsoleDesignation(boolean isCloseable) {
    return new FreechessHelpConsoleDesignation(getConn(), getEncoding(), isCloseable);
  }

  /**
   * Returns a FICS-specific general chat console designation.
   */
  @Override
  protected ConsoleDesignation createGeneralChatConsoleDesignation(boolean isCloseable) {
    return new FicsGeneralChatConsoleDesignation(getConn(), getEncoding(), isCloseable);
  }

  /**
   * Returns a FICS-specific personal chat console designation.
   */
  @Override
  protected ConsoleDesignation createPersonalChatConsoleDesignation(
      ServerUser user, boolean isCloseable) {
    return new PersonalChatConsoleDesignation(getConn(), user, getEncoding(), isCloseable);
  }

  /**
   * Creates an ICC-specific game chat console designation.
   */
  @Override
  protected ConsoleDesignation createGameConsoleDesignation(Game game) {
    return new FreechessGameConsoleDesignation(getConn(), game, getEncoding());
  }

  /**
   * Creates a <code>FreechessConsole</code> with the specified designation.
   */
  @Override
  protected Console createConsole(ConsoleDesignation designation) {
    return new FreechessConsole(this, designation);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected IcsCustomConsoleDesignation loadCustomConsoleDesignation(
      String prefsPrefix,
      String title,
      String encoding,
      List channels,
      Pattern messageRegex,
      boolean includeShouts,
      boolean includeCShouts) {
    return new FreechessCustomConsoleDesignation(
        getConn(), title, encoding, false, channels, messageRegex, includeShouts, includeCShouts);
  }

  /**
   * Returns the set of FICS channels.
   */
  @Override
  protected SortedMap createChannels() {
    SortedMap channels = new TreeMap();

    for (int i = 0; i < 256; i++) channels.put(new Integer(i), new FicsChannel(i));

    return channels;
  }

  /**
   * Return a PreferencesPanel for changing the console manager's settings.
   */
  @Override
  public PreferencesPanel getPreferencesUI() {
    return new FreechessConsolePrefsPanel(this);
  }
}
