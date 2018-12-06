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

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import free.jin.Preferences;
import free.jin.console.ConsoleManager;
import free.jin.console.CustomConsoleDesignation;

/**
 * A base class for <code>ConsoleManager</code> implementations ICS-based servers.
 *
 * @author Maryanovsky Alexander
 */
public abstract class IcsConsoleManager extends ConsoleManager {

  /**
   * {@inheritDoc}
   */
  @Override
  protected CustomConsoleDesignation loadCustomConsoleDesignation(
      String prefsPrefix, String title, String encoding, List channels, Pattern messageRegex) {
    Preferences prefs = getPrefs();

    boolean includeShouts = prefs.getBool(prefsPrefix + "includeShouts", false);
    boolean includeCShouts = prefs.getBool(prefsPrefix + "includeCShouts", false);

    return loadCustomConsoleDesignation(
        prefsPrefix, title, encoding, channels, messageRegex, includeShouts, includeCShouts);
  }

  /**
   * Loads a custom console designation from the preferences using the specified prefix and the
   * already retrieved data.
   */
  protected abstract IcsCustomConsoleDesignation loadCustomConsoleDesignation(
      String prefsPrefix,
      String title,
      String encoding,
      List channels,
      Pattern messageRegex,
      boolean includeShouts,
      boolean includeCShouts);

  /**
   * {@inheritDoc}
   */
  @Override
  public Object encodeConsoleChannelsPref(List channels) {
    int[] channelNumbers = new int[channels.size()];
    for (int i = 0; i < channels.size(); i++) {
      IcsChannel channel = (IcsChannel) channels.get(i);
      channelNumbers[i] = channel.getNumber();
    }
    return channelNumbers;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public List parseConsoleChannelsPref(Object channelsPrefsValue) {
    if (channelsPrefsValue == null) return Collections.EMPTY_LIST;

    Map allChannels = getChannels();
    List channels = new LinkedList();
    int[] channelNumbers = (int[]) channelsPrefsValue;
    for (int i = 0; i < channelNumbers.length; i++)
      channels.add(allChannels.get(new Integer(channelNumbers[i])));

    return channels;
  }
}
