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

import free.jin.BadChangesException;
import free.jin.Jin;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.*;


/**
 * A prefs panel for selecting the current ui mode.
 */

public class WindowingModePrefPanel extends PreferencesPanel{
  
  
  
  /**
   * The MDI mode radio button.
   */
  
  private final JRadioButton mdiMode;
  
  
  
  /**
   * The SDI mode radio button.
   */
  
  private final JRadioButton sdiMode;
  
  
  
  /**
   * Creates a new <code>WindowingModePrefPanel</code>.
   */
  
  public WindowingModePrefPanel(){
    String pref = Jin.getInstance().getPrefs().getString("uiProvider.classname");
    mdiMode = new JRadioButton("Multiple Document Interface", MdiUiProvider.class.getName().equals(pref));
    sdiMode = new JRadioButton("Single Document Interface", SdiUiProvider.class.getName().equals(pref));
    
    ButtonGroup bg = new ButtonGroup();
    bg.add(mdiMode);
    bg.add(sdiMode);
    
    ActionListener changeListener = new ActionListener(){
      public void actionPerformed(ActionEvent evt){
        fireStateChanged();
      }
    };
    
    sdiMode.addActionListener(changeListener);
    mdiMode.addActionListener(changeListener);
    
    createUi();
  }
  
  
  
  /**
   * Creates the UI of this panel.
   */
  
  private void createUi(){
    mdiMode.setMnemonic('M');
    sdiMode.setMnemonic('S');
    
    mdiMode.setToolTipText("Jin windows are placed inside one outer window");
    sdiMode.setToolTipText("Jin windows are separate, OS native, windows");
    
    Container modesPanel = Box.createVerticalBox();
    modesPanel.add(mdiMode);
    modesPanel.add(sdiMode);
    
    Container restartPane = new JPanel(new GridLayout(2, 1, 5, 5));
    restartPane.add(new JLabel("You must restart Jin to apply"));
    restartPane.add(new JLabel("a different windowing mode"));

    setLayout(new BorderLayout(10, 10));
    add(modesPanel, BorderLayout.CENTER);
    add(restartPane, BorderLayout.SOUTH);
  }



  public void applyChanges() throws BadChangesException{
    Jin.getInstance().getPrefs().setString("uiProvider.classname", 
        mdiMode.isSelected() ? MdiUiProvider.class.getName() : SdiUiProvider.class.getName());
  }
  
  

}
