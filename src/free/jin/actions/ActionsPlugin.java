/**
 * Jin - a chess client for internet chess servers.
 * More information is available at http://www.jinchess.com/.
 * Copyright (C) 2004 Alexander Maryanovsky.
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

package free.jin.actions;

import javax.swing.*;
import free.jin.plugin.*;
import javax.swing.border.EmptyBorder;
import java.awt.GridLayout;
import java.awt.BorderLayout;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import free.jin.action.JinAction;


/**
 * A plugin which displays the various available actions and lets the user run
 * them.
 */
 
public class ActionsPlugin extends Plugin{
  
  
  
  /**
   * The plugin's menu, listing all the actions.
   */
   
  private final JMenu menu = new JMenu("Actions");
  
  
  
  /**
   * The ui container for the action buttons.
   */
   
  private PluginUIContainer buttonContainer;
  
  
  
  /**
   * The radio button indicating the actions ui container is visible. 
   */
   
  private final JRadioButtonMenuItem shownButton; 
    
  

  /**
   * The radio button indicating the actions ui container is invisible. 
   */
   
  private final JRadioButtonMenuItem hiddenButton;
  
  
  
  /**
   * Creates a new <code>ActionsPlugin</code>.
   */
   
  public ActionsPlugin(){
    shownButton = new JRadioButtonMenuItem("Action Buttons Shown"); 
    hiddenButton = new JRadioButtonMenuItem("Action Buttons Hidden");
    
    shownButton.setMnemonic('S');
    hiddenButton.setMnemonic('H');
    
    ButtonGroup bGroup = new ButtonGroup();
    bGroup.add(shownButton);
    bGroup.add(hiddenButton);
    
    shownButton.setActionCommand("show");
    hiddenButton.setActionCommand("hide");

    ActionListener visibilityListener = new ActionListener(){
      public void actionPerformed(ActionEvent evt){
        String actionCommand = evt.getActionCommand();
        boolean isVisible = "show".equals(actionCommand);
        buttonContainer.setVisible(isVisible);
      }
    };

    shownButton.addActionListener(visibilityListener);
    hiddenButton.addActionListener(visibilityListener);
    
    menu.add(shownButton);
    menu.add(hiddenButton);
  }
   

  
  /**
   * Returns the name of this plugin - "Actions".
   */
  
  public String getName(){
    return "Actions";
  }
  
  
  
  /**
   * Returns the id of this plugin - "actions".
   */
   
  public String getId(){
    return "actions";
  }
  
  
  
  /**
   * Starts the plugin.
   */
   
  public void start(){
    buttonContainer = createButtonContainer();
    
    addActionButtons();
    addActionMenuItems();
    
    if (getPrefs().getBool("visible", true))
      buttonContainer.setVisible(true);
    else
      hiddenButton.setSelected(true);
      // In the "true" case, the button is made selected by the listener.
  }
  
  
  
  /**
   * Creates the ui container for the action buttons.
   */
   
  private PluginUIContainer createButtonContainer(){
    PluginUIContainer container = createContainer("");
    container.setTitle("Actions");
    container.setCloseOperation(PluginUIContainer.HIDE_ON_CLOSE);
    
    container.addPluginUIListener(new PluginUIAdapter(){
      public void pluginUIShown(PluginUIEvent evt){
        if (!shownButton.isSelected())
          shownButton.setSelected(true);
      }
      public void pluginUIHidden(PluginUIEvent evt){
        if (!hiddenButton.isSelected())
          hiddenButton.setSelected(true);
      }
    });
    
    return container;
  }
  
  
  
  /**
   * Adds the action buttons into the button container. 
   */
   
  private void addActionButtons(){
    ListModel actions = getActions();
    
    JComponent content = new JPanel(new GridLayout(actions.getSize(), 1, 5, 5));
    for (int i = 0; i < actions.getSize(); i++){
      JinAction action = (JinAction)actions.getElementAt(i);
      JButton button = new JButton(action.getName());
      button.addActionListener(action);
      content.add(button);
    }
    
    content.setBorder(new EmptyBorder(10, 10, 10, 10));
    
    buttonContainer.getContentPane().setLayout(new BorderLayout());
    buttonContainer.getContentPane().add(content, BorderLayout.CENTER);
  }
  
  
  
  /**
   * Adds all the actions to the plugin's menu.
   */
   
  private void addActionMenuItems(){
    menu.addSeparator();
    ListModel actions = getActions();
    for (int i = 0; i < actions.getSize(); i++){
      JinAction action = (JinAction)actions.getElementAt(i);
      JMenuItem menuItem = new JMenuItem(action.getName());
      menuItem.addActionListener(action);
      menu.add(menuItem);
    }
  }
  
  
  
  /**
   * Returns the menu for this plugin.
   */
   
  public JMenu getPluginMenu(){
    return menu;
  }
  
  
  
  /**
   * Saves the plugin's state into preferences.
   */
   
  public void saveState(){
    getPrefs().setBool("visible", buttonContainer.isVisible());     
  }
  
  
  
}