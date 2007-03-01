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

import free.jin.action.JinAction;
import free.jin.plugin.Plugin;
import free.jin.plugin.PluginUIContainer;
import free.jin.ui.UIProvider;

import java.awt.BorderLayout;
import java.awt.GridLayout;

import javax.swing.*;
import javax.swing.border.EmptyBorder;


/**
 * A plugin which displays the various available actions and lets the user run
 * them.
 */
 
public class ActionsPlugin extends Plugin{
  
  
  
  /**
   * The ui container for the action buttons.
   */
   
  private PluginUIContainer buttonContainer;
  
  
  
  /**
   * Creates a new <code>ActionsPlugin</code>.
   */
   
  public ActionsPlugin(){
    
  }
   

  
  /**
   * Returns the name of this plugin.
   */
  
  public String getName(){
    return getI18n().getString("pluginName");
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
  }
  
  
  
  /**
   * Creates the ui container for the action buttons.
   */
   
  private PluginUIContainer createButtonContainer(){
    PluginUIContainer container = createContainer("", UIProvider.HIDEABLE_CONTAINER_MODE);
    container.setTitle(getI18n().getString("buttonPanelTitle"));
    container.setResizable(false);
    
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
  
  
  
}