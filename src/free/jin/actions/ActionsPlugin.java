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
import java.awt.event.HierarchyEvent;
import java.awt.event.HierarchyListener;

import javax.swing.AbstractButton;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.ListModel;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;

import free.jin.action.JinAction;
import free.jin.plugin.Plugin;
import free.jin.plugin.PluginUIContainer;
import free.jin.ui.UIProvider;
import free.util.models.BooleanListener;
import free.util.models.UnmodifiableBooleanModel;


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
      content.add(new ActionButton(action, new JButton()).getButton());
    }
    
    content.setBorder(new EmptyBorder(10, 10, 10, 10));
    
    buttonContainer.getContentPane().removeAll();
    buttonContainer.getContentPane().setLayout(new BorderLayout());
    buttonContainer.getContentPane().add(content, BorderLayout.CENTER);
    buttonContainer.sizeToFit();
  }
  
  
  
  /**
   * A wrapper of an <code>AbstractButton</code> which makes the button
   * represent and activate <code>JinAction</code>.
   */
  
  private static class ActionButton implements BooleanListener{
    
    
    
    /**
     * The action we represent.
     */
    
    private final JinAction action;
    
    
    
    /**
     * The button we wrap.
     */
    
    private final AbstractButton button;
    
    
    
    /**
     * Creates a new <code>ActionButton</code> to represent the specified
     * action using the specified button.
     */
    
    public ActionButton(JinAction action, AbstractButton button){
      this.action = action;
      this.button = button;
      
      button.addHierarchyListener(new HierarchyListener(){
        public void hierarchyChanged(HierarchyEvent e){
          if ((e.getChangeFlags() & HierarchyEvent.DISPLAYABILITY_CHANGED) != 0){
            if (ActionButton.this.button.isDisplayable())
              madeDisplayable();
            else
              madeUndisplayable();
          }
        }
      });
    }
    
    
    
    /**
     * Returns the button.
     */
    
    public AbstractButton getButton(){
      return button;
    }
    
    
    
    /**
     * Invoked when the button is made displayable.
     */
    
    private void madeDisplayable(){
      button.setText(action.getName());
      
      button.setEnabled(action.isEnabled());
      action.getEnabledModel().addListener(this);
      
      button.addActionListener(action);
    }
    
    
    
    /**
     * Invoked when the button is made undisplayable.
     */
    
    private void madeUndisplayable(){
      action.getEnabledModel().removeListener(this);
      button.removeActionListener(action);
    }
    
    
    
    /**
     * Sets the button's enabled state to match the action's enabled state.
     */
    
    public void modelChanged(UnmodifiableBooleanModel model){
      button.setEnabled(model.isOn());
    }

    
    
  }
  
  
  
}