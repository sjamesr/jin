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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.ListModel;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;

import free.jin.ConnectionManager;
import free.jin.I18n;
import free.jin.Jin;
import free.jin.Session;
import free.jin.SessionEvent;
import free.jin.SessionListener;
import free.jin.action.JinAction;
import free.util.models.BooleanListener;
import free.util.models.UnmodifiableBooleanModel;



/**
 * A menu allowing the user to use any available Actions.
 */

public class ActionsMenu extends JMenu implements SessionListener, ListDataListener{
  
  
  
  /**
   * Creates a new <code>ActionsMenu</code>.
   */
  
  public ActionsMenu(){
    I18n.get(ActionsMenu.class).initAbstractButton(this, "this");
  }
  
  
  
  /**
   * Initializes the menu, registering any needed listeners.
   */
  
  public void addNotify(){
    super.addNotify();
    
    ConnectionManager connManager = Jin.getInstance().getConnManager(); 
    connManager.addSessionListener(this);
    
    Session session  = connManager.getSession();
    if (session != null){
      ListModel actions = session.getPluginContext().getActions();
      actions.addListDataListener(this);
      
      updateActionMenuItems(actions);
    }
  }
  
  
  
  /**
   * Unregisters any listeners we've registered.
   */
  
  public void removeNotify(){
    super.removeNotify();
    
    ConnectionManager connManager = Jin.getInstance().getConnManager();
    connManager.removeSessionListener(this);
    
    Session session = connManager.getSession();
    if (session != null){
      ListModel actions = session.getPluginContext().getActions();
      actions.removeListDataListener(this);
      
      removeAll();
    }
  }
  
  
  
  /**
   * Sets the action menu items to match the specified list of actions.
   */
   
  private void updateActionMenuItems(ListModel actions){
    removeAll();
    for (int i = 0; i < actions.getSize(); i++){
      JinAction action = (JinAction)actions.getElementAt(i);
      JMenuItem menuItem = new ActionMenuItem(action);
      add(menuItem);
    }
  }
  
  
  
  
  /*
   * SessionListener implementation. Registers and unregisters us as session
   * listeners and updates the menu items accordingly.
   */
  
  public void sessionEstablished(SessionEvent evt){
    Session session = evt.getSession();

    ListModel actions = session.getPluginContext().getActions();
    actions.addListDataListener(this);
    
    updateActionMenuItems(actions);
  }
  
  public void sessionClosed(SessionEvent evt){
    Session session = evt.getSession();
    
    ListModel actions = session.getPluginContext().getActions();
    actions.removeListDataListener(this);
    
    removeAll();
  }
  
  
  
  
  
  /*
   * ListDataListener implementation. Synchronizes the menu items with
   * the list of actions
   */
  
  public void intervalAdded(ListDataEvent evt){
    updateActionMenuItems((ListModel)evt.getSource());
  }
  
  public void intervalRemoved(ListDataEvent evt){
    updateActionMenuItems((ListModel)evt.getSource());
  }
  
  public void contentsChanged(ListDataEvent evt){
    updateActionMenuItems((ListModel)evt.getSource());
  }
  
  
  
  /**
   * The menu item representing and activating the action.
   */
  
  private class ActionMenuItem extends JMenuItem implements ActionListener, BooleanListener{
    
    
    
    /**
     * The action.
     */
    
    private final JinAction action;
    
    
    
    /**
     * Creates a new <code>ActionMenuItem</code> with the specified action.
     */
    
    public ActionMenuItem(JinAction action){
      this.action = action;
      
      setText(action.getName());
      
      // We don't use the actions themselves (they implement <code>ActionListener</code>) because we want to pass
      // the menu and not the menu item as the actor to the action. That we do because the menu items
      // have the popup window as their toplevel parent (see what the actions do with the actor).
      addActionListener(this);
      
      setEnabled(action.isEnabled());
      action.getEnabledModel().addListener(this);
    }
    
    
    
    /**
     * Invokes the action.
     */
    
    public void actionPerformed(ActionEvent e){
      ActionMenuItem.this.action.go(ActionsMenu.this);
    }
    
    
    
    /**
     * Sets the button's enabled state to match the action's enabled state.
     */
    
    public void modelChanged(UnmodifiableBooleanModel model){
      setEnabled(model.isOn());
    }
    
    
    
    /**
     * Removes any listeners we've registered.
     */
    
    public void dispose(){
      removeActionListener(this);
      action.getEnabledModel().removeListener(this);
    }
    
    
    
  }
  


}
