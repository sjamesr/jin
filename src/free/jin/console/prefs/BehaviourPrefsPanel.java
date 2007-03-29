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

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JPanel;

import free.jin.BadChangesException;
import free.jin.I18n;
import free.jin.console.ConsoleManager;
import free.jin.ui.PreferencesPanel;
import free.util.swing.PreferredSizedPanel;



/**
 * A preferences panel which allows the user to select how the console behaves
 * in certain cases.
 */

public final class BehaviourPrefsPanel extends PreferencesPanel{
  
  
  
  /**
   * The console manager whose preferences we're displaying/modifying.
   */
  
  private final ConsoleManager consoleManager;
  
  
  
  /**
   * The checkbox specifying copy-on-select policy.
   */
  
  private final JCheckBox copyOnSelect;
  
  
  
  /**
   * Creates a new <code>BehaviourPrefsPanel</code>.
   */
  
  public BehaviourPrefsPanel(ConsoleManager consoleManager){
    this.consoleManager = consoleManager;
    
    I18n i18n = I18n.get(BehaviourPrefsPanel.class);
    
    copyOnSelect = i18n.createCheckBox("copyOnSelectCheckBox");
    copyOnSelect.setSelected(consoleManager.isCopyOnSelect());
    copyOnSelect.addActionListener(proxyActionListener);
    
    createUI();
  }
  
  
  
  /**
   * Creates the UI of this panel.
   */
  
  private void createUI(){
    setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
    
    I18n i18n = I18n.get(BehaviourPrefsPanel.class);

    JPanel panel = new PreferredSizedPanel();
    panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
    panel.setBorder(BorderFactory.createCompoundBorder(
      i18n.createTitledBorder("textSelectionPanel"),
      BorderFactory.createEmptyBorder(0, 5, 5, 5)));
    copyOnSelect.setAlignmentX(JComponent.LEFT_ALIGNMENT);
    panel.add(copyOnSelect);
    
    add(panel);
  }
  
  
  
  /**
   * Applies any changes made by the user.
   */

  public void applyChanges() throws BadChangesException{
    consoleManager.setCopyOnSelect(copyOnSelect.isSelected());
  }
  
  

}
