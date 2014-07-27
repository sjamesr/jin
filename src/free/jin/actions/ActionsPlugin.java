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
import java.awt.Component;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.ListModel;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;

import free.jin.Preferences;
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
   * The order of actions - a list of their IDs; <code>null</code> if the order
   * doesn't matter.
   */
  
  private List actionsOrder;
  
  
  
  /**
   * The actions to include - a list of their IDs; <code>null</code> if to
   * include everything. 
   */
  
  private List actionsIncludeFilter;
  
  
  
  /**
   * Creates a new <code>ActionsPlugin</code>.
   */
   
  public ActionsPlugin(){
    
  }
   

  
  /**
   * Returns the id of this plugin - "actions".
   */
   
  @Override
  public String getId(){
    return "actions";
  }
  
  
  
  /**
   * Starts the plugin.
   */
   
  @Override
  public void start(){
    Preferences prefs = getPrefs();
    
    String actionsOrderPref = prefs.getString("order", null);
    if (actionsOrderPref != null)
      actionsOrder = Arrays.asList(TextUtilities.getTokens(actionsOrderPref, " "));
    else
      actionsOrder = null;
    
    String actionsIncludePref = prefs.getString("filter.include", null);
    if (actionsIncludePref != null)
      actionsIncludeFilter = Arrays.asList(TextUtilities.getTokens(actionsIncludePref, " "));
    else
      actionsIncludeFilter = null;
    
    buttonContainer = createButtonContainer();
    buttonContainer.addPluginUIListener(new PluginUIAdapter(){
      @Override
      public void pluginUIShown(PluginUIEvent evt){
        buttonContainer.pack();
      }
    });
    
    updateActionButtons();
    
    getActions().addListDataListener(new ListDataListener(){
      @Override
      public void contentsChanged(ListDataEvent e){
        updateActionButtons();
      }
      @Override
      public void intervalAdded(ListDataEvent e){
        updateActionButtons();
      }
      @Override
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
   * Creates a <code>List</code> from the specified <code>ListModel</code>.
   */
  
  private List makeList(ListModel model){
    List list = new ArrayList(model.getSize());
    for (int i = 0; i < model.getSize(); i++)
      list.add(model.getElementAt(i));
    return list;
  }
  
  
  
  /**
   * Filters the specified list of actions according to our filter setting and
   * returns the resulting list.
   */
  
  private List filter(List actions){
    if (actionsIncludeFilter == null)
      return actions;
    
    List filteredActions = new LinkedList();
    for (Iterator i = actions.iterator(); i.hasNext();){
      JinAction action = (JinAction)i.next();
      if (actionsIncludeFilter.contains(action.getId()))
        filteredActions.add(action);
    }
    
    return filteredActions;
  }
  
  
  
  /**
   * Sorts the specified actions according to our "order" setting and returns
   * the resulting list.
   */
  
  private List sort(List actions){
    if (actionsOrder == null)
      return actions;
    
    Collections.sort(actions, new Comparator(){
      @Override
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
    
    return actions;
  }
  
  
  
  /**
   * Adds the action buttons into the button container. 
   */
   
  private void updateActionButtons(){
    ListModel actionsListModel = getActions();
    List actionsList = sort(filter(makeList(actionsListModel)));
    
    JComponent content = new JPanel(new TableLayout(1, 17, 17));
    for (Iterator i = actionsList.iterator(); i.hasNext();){
      JinAction action = (JinAction)i.next();
      
      JButton button = new IconButton(action);
      button.setAlignmentX(Component.CENTER_ALIGNMENT);
      button.setHorizontalTextPosition(SwingConstants.CENTER);
      button.setVerticalTextPosition(SwingConstants.BOTTOM);
      
      content.add(button);
    }
    
    content.setBorder(new EmptyBorder(10, 10, 10, 10));
    content.setAlignmentY(Component.TOP_ALIGNMENT);
    
    buttonContainer.getContentPane().removeAll();
    buttonContainer.getContentPane().setLayout(new BorderLayout());
    buttonContainer.getContentPane().add(content, BorderLayout.CENTER);
    
    if (buttonContainer.isVisible())
      buttonContainer.pack();
  }
  
  
  
}