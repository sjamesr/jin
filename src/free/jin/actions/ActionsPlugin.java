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
import free.jin.plugin.Plugin;
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
    ListModel actions = getActions();
    for (int i = 0; i < actions.getSize(); i++){
      JinAction action = (JinAction)actions.getElementAt(i);
      menu.add(createMenuItemFor(action));
    }
  }
  
  
  
  /**
   * Creates and returns a <code>JMenuItem</code> for the specified
   * <code>JinAction</code>.
   */
   
  private JMenuItem createMenuItemFor(final JinAction action){
    JMenuItem menuItem = new JMenuItem(action.getName());
    menuItem.addActionListener(action);
    
    return menuItem;
  }
  
  
  
  /**
   * Returns the menu for this plugin.
   */
   
  public JMenu getPluginMenu(){
    return menu;
  }
  
  
  
}