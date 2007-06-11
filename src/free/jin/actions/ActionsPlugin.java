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
import java.util.*;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;

import free.jin.action.JinAction;
import free.jin.plugin.Plugin;
import free.jin.plugin.PluginUIAdapter;
import free.jin.plugin.PluginUIContainer;
import free.jin.plugin.PluginUIEvent;
import free.jin.ui.UIProvider;
import free.util.TableLayout;
import free.util.TextUtilities;
import free.util.swing.IconButton;


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
   * The order of actions - a list of their IDs.
   */
  
  private List actionsOrder;
  
  
  
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
    String actionsOrderPref = getPrefs().getString("order", null);
    if (actionsOrderPref != null)
      actionsOrder = Arrays.asList(TextUtilities.getTokens(actionsOrderPref, " "));
    else
      actionsOrder = null;
    
    buttonContainer = createButtonContainer();
    buttonContainer.addPluginUIListener(new PluginUIAdapter(){
      public void pluginUIShown(PluginUIEvent evt){
        buttonContainer.pack();
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
   * Sorts the specified actions according to our "order" property and returns
   * the resulting list.
   */
  
  private List sort(ListModel model){
    List list = new ArrayList(model.getSize());
    for (int i = 0; i < model.getSize(); i++)
      list.add(model.getElementAt(i));

    if (actionsOrder == null)
      return list;
    
    Collections.sort(list, new Comparator(){
      public int compare(Object arg0, Object arg1){
        JinAction action1 = (JinAction)arg0;
        JinAction action2 = (JinAction)arg1;
        
        String id1 = action1.getId();
        String id2 = action2.getId();
        
        int index1 = actionsOrder.indexOf(id1);
        int index2 = actionsOrder.indexOf(id2);
        
        if (index1 == -1)
          return (index2 == -1) ? 0 : 1;
        else if (index2 == -1)
          return (index1 == -1) ? 0 : -1;
        else
          return index1 - index2;
      }
    });
    
    return list;
  }
  
  
  
  /**
   * Adds the action buttons into the button container. 
   */
   
  private void updateActionButtons(){
    ListModel actions = getActions();
    List sortedActions = sort(actions);
    
    JComponent content = new JPanel(new TableLayout(1, 17, 17));
    for (Iterator i = sortedActions.iterator(); i.hasNext();){
      JinAction action = (JinAction)i.next();
      
      JButton button = new IconButton(action);
      button.setAlignmentX(JComponent.CENTER_ALIGNMENT);
      button.setHorizontalTextPosition(SwingConstants.CENTER);
      button.setVerticalTextPosition(SwingConstants.BOTTOM);
      
      content.add(button);
    }
    
    content.setBorder(new EmptyBorder(10, 10, 10, 10));
    content.setAlignmentY(JComponent.TOP_ALIGNMENT);
    
    buttonContainer.getContentPane().removeAll();
    buttonContainer.getContentPane().setLayout(new BorderLayout());
    buttonContainer.getContentPane().add(content, BorderLayout.CENTER);
    
    if (buttonContainer.isVisible())
      buttonContainer.pack();
  }
  
  
  
}