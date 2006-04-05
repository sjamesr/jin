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

package free.jin.ui;


import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JComponent;

import free.jin.BadChangesException;
import free.jin.I18n;
import free.jin.Jin;



/**
 * A panel for selecting Jin UI preferences. 
 */

public class UiPrefsPanel extends CompositePreferencesPanel{
  
  
  
  /**
   * Creates a new <code>UiPrefsPanel</code>.
   */
  
  public UiPrefsPanel(){
    I18n i18n = I18n.get(UiPrefsPanel.class);
    
    setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
    
    addPanel(new LookAndFeelPrefPanel(), i18n.getString("lookAndFeelPanel.name"), i18n.getString("lookAndFeelPanel.tooltip"));
    addPanel(new WindowingModePrefPanel(), i18n.getString("windowingModePanel.name"), i18n.getString("windowingModePanel.tooltip"));
  }
  
  
  
  /**
   * Adds the specified panel to the UI.
   */
  
  protected void addPanelToUi(PreferencesPanel panel, String name, String tooltip){
    panel.setBorder(BorderFactory.createCompoundBorder(
        BorderFactory.createTitledBorder(name),
        BorderFactory.createEmptyBorder(0, 5, 5, 5)));
    panel.setAlignmentY(JComponent.TOP_ALIGNMENT);
    add(panel);
  }
  
  
  
  /**
   * Overrides {@link CompositePreferencesPanel#applyChanges()} to ask the user
   * about restarting Jin to apply the new settings.
   */
  
  public void applyChanges() throws BadChangesException{
    super.applyChanges();
    
    Object result = I18n.get(UiPrefsPanel.class).question(OptionPanel.YES, "restartJinDialog", this,
      new Object[]{Jin.getInstance().getAppName()});
    
    if (result == OptionPanel.YES)
      Jin.getInstance().quit(false);
  }


}
