/**
 * Jin - a chess client for internet chess servers.
 * More information is available at http://www.jinchess.com/.
 * Copyright (C) 2005 Alexander Maryanovsky.
 * All rights reserved.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */


package free.jin.console.prefs;

import java.awt.BorderLayout;

import javax.swing.BorderFactory;
import javax.swing.JTabbedPane;

import free.jin.I18n;
import free.jin.console.ConsoleManager;
import free.jin.ui.CompositePreferencesPanel;
import free.jin.ui.PreferencesPanel;



/**
 * The main preferences panel for the console plugin. 
 */

public class ConsolePrefsPanel extends CompositePreferencesPanel{
  
  
  
  /**
   * The tabbed pane.
   */
  
  private final JTabbedPane tabs = new JTabbedPane();

  

  
  /**
   * Creates a new <code>ConsolePrefsPanel</code>
   */
  
  public ConsolePrefsPanel(ConsoleManager consoleManager){
    I18n i18n = I18n.get(getClass(), ConsolePrefsPanel.class);
    
    addPanel(createTextPrefsPanel(consoleManager), i18n.getString("textDisplayTab.text"), i18n.getString("textDisplayTab.tooltip"));
    addPanel(new BehaviourPrefsPanel(consoleManager), i18n.getString("consoleBehaviourTab.text"), i18n.getString("consoleBehaviourTab.tooltip"));
    if (consoleManager.supportsMultipleEncodings())
      addPanel(new EncodingPrefsPanel(consoleManager), i18n.getString("encodingTab.text"), i18n.getString("encodingTab.tooltip"));
    
    setLayout(new BorderLayout());
    add(tabs, BorderLayout.CENTER);
  }
  
  
  
  /**
   * Adds the specified panel to a new tab.
   */
  
  protected void addPanelToUi(PreferencesPanel panel, String name, String tooltip){
    panel.setBorder(BorderFactory.createEmptyBorder(15, 10, 15, 10));
    tabs.addTab(name, null, panel, tooltip);
  }
  
  
  
  /**
   * Creates the <code>TextPrefsPanel</code> to be used in this console prefs
   * panel. This method returns a plain <code>TextPrefsPanel</code>, but allows
   * subclasses to return a subclass of it.
   */
  
  protected TextPrefsPanel createTextPrefsPanel(ConsoleManager cm){
    return new TextPrefsPanel(cm);
  }
  
  
  
}
