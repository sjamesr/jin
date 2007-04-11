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

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.Image;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.ListModel;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;

import free.jin.action.JinAction;
import free.jin.plugin.Plugin;
import free.jin.plugin.PluginUIAdapter;
import free.jin.plugin.PluginUIContainer;
import free.jin.plugin.PluginUIEvent;
import free.jin.ui.UIProvider;
import free.util.swing.SwingUtils;


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
    buttonContainer.addPluginUIListener(new PluginUIAdapter(){
      public void pluginUIShown(PluginUIEvent evt){
        buttonContainer.sizeToFit();
      }
    });
    
    updateActionButtons();
    
    getActions().addListDataListener(new ListDataListener(){
      public void contentsChanged(ListDataEvent e){
        updateActionButtons();
      }
      public void intervalAdded(ListDataEvent e){
        updateActionButtons();
      }
      public void intervalRemoved(ListDataEvent e){
        updateActionButtons();
      }
    });
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
   
  private void updateActionButtons(){
    ListModel actions = getActions();
    
    JComponent content = new JPanel(new GridLayout(actions.getSize(), 1, 5, 5));
    for (int i = 0; i < actions.getSize(); i++){
      JinAction action = (JinAction)actions.getElementAt(i);
      
      JButton button = new JButton(action);
      Image iconImage = (Image)action.getValue(JinAction.ICON_IMAGE);
      if (iconImage != null){
        SwingUtils.makeIconButton(button, iconImage);
        button.setHorizontalTextPosition(SwingConstants.CENTER);
        button.setVerticalTextPosition(SwingConstants.BOTTOM);
      }
      
      content.add(button);
    }
    
    content.setBorder(new EmptyBorder(10, 10, 10, 10));
    
    buttonContainer.getContentPane().removeAll();
    buttonContainer.getContentPane().setLayout(new BorderLayout());
    buttonContainer.getContentPane().add(content, BorderLayout.CENTER);
    
    if (buttonContainer.isVisible())
      buttonContainer.sizeToFit();
  }
  
  
  
}