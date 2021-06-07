/**
 * Jin - a chess client for internet chess servers. More information is available at
 * http://www.jinchess.com/. Copyright (C) 2005 Alexander Maryanovsky. All rights reserved.
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
package free.jin.console.prefs;

import free.jin.console.ConsoleManager;
import free.jin.ui.TabbedPreferencesPanel;

/** The main preferences panel for the console plugin. */
public abstract class ConsolePrefsPanel extends TabbedPreferencesPanel {

  /** Creates a new <code>ConsolePrefsPanel</code> */
  public ConsolePrefsPanel(ConsoleManager consoleManager) {
    addPanel(createTextPrefsPanel(consoleManager), "textDisplayTab");
    addPanel(createCustomConsolesPrefsPanel(consoleManager), "customConsolesTab");
    if (consoleManager.getConn().getTextEncoding() != null)
      addPanel(new EncodingPrefsPanel(consoleManager), "encodingTab");
  }

  /**
   * Creates the <code>TextPrefsPanel</code> to be used in this console prefs panel. The default
   * implementation returns a plain <code>TextPrefsPanel</code>, but may be overridden by
   * server-specific classes to return a server-specific text prefs panel.
   */
  protected TextPrefsPanel createTextPrefsPanel(ConsoleManager cm) {
    return new TextPrefsPanel(cm);
  }

  /** Creates the <code>CustomConsolesPrefsPanel</code> to be used in this console prefs panel. */
  protected abstract CustomConsolesPrefsPanel createCustomConsolesPrefsPanel(ConsoleManager cm);
}
