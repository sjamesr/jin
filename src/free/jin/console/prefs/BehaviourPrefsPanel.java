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

import javax.swing.*;

import free.jin.BadChangesException;
import free.jin.GameListConnection;
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
   * The radio button specifying "embedded" game lists.
   */
  
  private final JRadioButton embeddedGameLists;
  
  
  
  /**
   * The radio button specifying "external" game lists.
   */
  
  private final JRadioButton externalGameLists;
  
  
  
  /**
   * The checkbox specifying copy-on-select policy.
   */
  
  private final JCheckBox copyOnSelect;
  
  
  
  /**
   * The radio button specifying that game lists not be displayed in any special
   * manner.
   */
  
  private final JRadioButton textGameLists;
  
  
  
  /**
   * Creates a new <code>BehaviourPrefsPanel</code>.
   */
  
  public BehaviourPrefsPanel(ConsoleManager consoleManager){
    this.consoleManager = consoleManager;
    
    I18n i18n = I18n.get(BehaviourPrefsPanel.class);
    
    if (consoleManager.getConn() instanceof GameListConnection){
      embeddedGameLists = i18n.createRadioButton("embeddedGameListsRadioButton");
      externalGameLists = i18n.createRadioButton("externalGameListsRadioButton");
      textGameLists = i18n.createRadioButton("textGameListsRadioButton");
      
      switch (consoleManager.getGameListsDisplayStyle()){
        case ConsoleManager.EMBEDDED_GAME_LISTS: embeddedGameLists.setSelected(true); break;
        case ConsoleManager.EXTERNAL_GAME_LISTS: externalGameLists.setSelected(true); break;
        case ConsoleManager.NO_GAME_LISTS: textGameLists.setSelected(true); break;
        default:
          throw new IllegalStateException();
      }
      
      embeddedGameLists.addActionListener(proxyActionListener);
      externalGameLists.addActionListener(proxyActionListener);
      textGameLists.addActionListener(proxyActionListener);
    }
    else{
      embeddedGameLists = null;
      externalGameLists = null;
      textGameLists = null;
    }
    
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

    if (embeddedGameLists != null){
      ButtonGroup bg = new ButtonGroup();
      bg.add(embeddedGameLists);
      bg.add(externalGameLists);
      bg.add(textGameLists);
      
      JPanel panel = new PreferredSizedPanel();
      panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
      panel.setBorder(BorderFactory.createCompoundBorder(
        i18n.createTitledBorder("gameListsDisplayPanel"),
        BorderFactory.createEmptyBorder(0, 5, 5, 5)));
          
      embeddedGameLists.setAlignmentX(JComponent.LEFT_ALIGNMENT);
      externalGameLists.setAlignmentX(JComponent.LEFT_ALIGNMENT);
      textGameLists.setAlignmentX(JComponent.LEFT_ALIGNMENT);
      
      panel.add(embeddedGameLists);
      panel.add(externalGameLists);
      panel.add(textGameLists);
      
      add(panel);
      add(Box.createVerticalStrut(10));
    }

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
    if (embeddedGameLists != null){
      if (embeddedGameLists.isSelected())
        consoleManager.setGameListsDisplayStyle(ConsoleManager.EMBEDDED_GAME_LISTS);
      else if (externalGameLists.isSelected())
        consoleManager.setGameListsDisplayStyle(ConsoleManager.EXTERNAL_GAME_LISTS);
      else
        consoleManager.setGameListsDisplayStyle(ConsoleManager.NO_GAME_LISTS);
    }
    
    consoleManager.setCopyOnSelect(copyOnSelect.isSelected());
  }
  
  

}
