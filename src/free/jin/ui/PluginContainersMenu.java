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

import free.jin.plugin.PluginUIContainer;
import free.jin.plugin.PluginUIEvent;
import free.jin.plugin.PluginUIListener;
import free.util.Utilities;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import javax.swing.*;



/**
 * A menu which allows the user to manage the various plugin containers.
 */

public class PluginContainersMenu extends JMenu implements PropertyChangeListener,
    PluginUIListener, ActionListener{
  
  
  
  /**
   * Maps <code>PluginUIContainers</code> to <code>JCheckBoxMenuItems</code>
   * which allow showing/hiding them.
   */
  
  private final Hashtable containersToVisCheckBoxes = new Hashtable();
  
  
  
  /**
   * Maps <code>JCheckBoxMenuItems</code> that allow showing/hiding plugin ui
   * containers to the said containers. 
   */
  
  private final Hashtable visCheckBoxesToContainers = new Hashtable();
  
  
  
  /**
   * Maps <code>PluginUIContainers</code> to <code>JRadioButtons</code> that
   * control which container is currently active.
   */
  
  private final Hashtable containersToActiveRadioButtons = new Hashtable();
  
  
  
  /**
   * Maps <code>JRadioButtons</code> that control which container is currently
   * active to the said containers.
   */
  
  private final Hashtable activeRadioButtonsToContainers = new Hashtable();
  
  
  
  /**
   * The list of all menu items (including the separator) in this menu.
   * We manage our own list because on every add/remove of an item we need to
   * rebuild the real menu. That we have to do because
   * JMenu.add(Component, int index) is completely broken under Swing 1.1.1. 
   */
  
  private final Vector items = new Vector();
  
  
  
  /**
   * The position of the separator between checkboxes for showing/hiding
   * containers and radio buttons for activating containers. -1 if none.
   */
  
  private int sepIndex = -1;
  
  
  /**
   * Creates a new <code>PluginContainersMenu</code>.
   */
  
  public PluginContainersMenu(String text, int mnemonic){
    this(Utilities.EMPTY_ENUM, text, mnemonic);
  }
  
  
  
  /**
   * Creates a new <code>PluginContainersMenu</code> with the specified list
   * of existing plugin ui containers.
   */
  
  public PluginContainersMenu(Enumeration existingContainers, String text, int mnemonic){
    super(text);
    setMnemonic(mnemonic);
    
    while (existingContainers.hasMoreElements())
      pluginContainerAdded((AbstractPluginUIContainer)existingContainers.nextElement());
  }
  
  
  
  /**
   * Synchronizes our own item list with what the menu thinks it has for
   * children.
   */
  
  private void syncMenus(){
    removeAll();
    
    for (int i = 0; i < items.size(); i++){
      Object item = items.elementAt(i);
      if (item instanceof JMenuItem)
        add((JMenuItem)item);
      else if (item instanceof JSeparator)
        addSeparator();
    }
  }
  
  
  
  /**
   * This method is invoked to notify us that a new plugin container has been
   * created.
   */
  
  public void pluginContainerAdded(AbstractPluginUIContainer pc){
    if (pc.getMode() == UIProvider.HIDEABLE_CONTAINER_MODE){
      JCheckBoxMenuItem item = 
        new JCheckBoxMenuItem("Show " + pc.getTitle(), pc.isVisible());
      
      containersToVisCheckBoxes.put(pc, item);
      visCheckBoxesToContainers.put(item, pc);
      
      item.addActionListener(this);
      
      addShowCheckBox(item);
    }
    
    JRadioButtonMenuItem item = new JRadioButtonMenuItem(pc.getTitle(), pc.isActive());
    
    containersToActiveRadioButtons.put(pc, item);
    activeRadioButtonsToContainers.put(item, pc);
    
    if (pc.isVisible())
      addActiveRadioButton(item);
    
    item.addActionListener(this);
    
    pc.addPluginUIListener(this);
    pc.addPropertyChangeListener(this);
  }
  
  
  
  /**
   * Adds the specified show/hide checkbox at the appropriate location.
   */
  
  private void addShowCheckBox(JCheckBoxMenuItem item){
    if (sepIndex != -1){ // separator already exists
      items.insertElementAt(item, sepIndex++);
    }
    else if (activeRadioButtonsToContainers.size() != 0){ // need to insert separator
      items.insertElementAt(item, 0);
      items.insertElementAt(new JSeparator(), sepIndex = 1);
    }
    else // separator not needed yet
      items.addElement(item);
    
    syncMenus();
  }
  
  
  
  /**
   * Removes the specified show/hide checkbox.
   */
  
  private void removeShowCheckBox(JCheckBoxMenuItem item){
    items.removeElement(item);
    
    if (sepIndex != -1)
      sepIndex--;
    
    if (sepIndex == 0){ // the separator is first
      items.removeElementAt(0);        // remove the separator
      sepIndex = -1;
    }
    
    syncMenus();
  }
  
  
  
  /**
   * Adds the specified activeness controlling radio button at the appropriate
   * location. 
   */
  
  private void addActiveRadioButton(JRadioButtonMenuItem item){
    if (sepIndex != -1) // separator already exists
      items.addElement(item);
    else if (visCheckBoxesToContainers.size() != 0){ // need to insert separator
      sepIndex = items.size();
      items.addElement(new JSeparator());
      items.addElement(item);
    }
    else // separator not needed yet
      items.addElement(item);
    
    syncMenus();
  }
  
  
  
  /**
   * Removes the specified activeness controlling radio button.
   */
  
  private void removeActiveRadioButton(JRadioButtonMenuItem item){
    items.removeElement(item);
    
    if ((sepIndex != -1) && (sepIndex == items.size() - 1)){  // the separator is last
      items.removeElementAt(sepIndex);                   // remove the separator
      sepIndex = -1;
    }
    
    syncMenus();
  }
  
  
  
  /**
   * This method is invoked when a property of one of the current plugin ui
   * containers changes.
   */
  
  public void propertyChange(PropertyChangeEvent evt){
    if ("title".equals(evt.getPropertyName())){
      JCheckBoxMenuItem checkBox = (JCheckBoxMenuItem)containersToVisCheckBoxes.get(evt.getSource());
      if (checkBox != null)
        checkBox.setText("Show " + (String)evt.getNewValue());
      
      JRadioButtonMenuItem radioButton = (JRadioButtonMenuItem)containersToActiveRadioButtons.get(evt.getSource());
      radioButton.setText((String)evt.getNewValue());
    }
    else if ("disposed".equals(evt.getPropertyName()))
      pluginContainerRemoved((AbstractPluginUIContainer)evt.getSource());
  }
  

  
  /**
   * This method is invoked when one of the current plugin ui containers is
   * shown.
   */
  
  public void pluginUIShown(PluginUIEvent evt){
    JCheckBoxMenuItem checkBox = (JCheckBoxMenuItem)containersToVisCheckBoxes.get(evt.getSource());
    if (checkBox != null)
      checkBox.setState(true);
    
    JRadioButtonMenuItem radioButton = (JRadioButtonMenuItem)containersToActiveRadioButtons.get(evt.getSource());
    addActiveRadioButton(radioButton);
  }
  
  
  
  /**
   * This method is invoked when one of the current plugin ui containers is
   * hidden.
   */
  
  public void pluginUIHidden(PluginUIEvent evt){
    JCheckBoxMenuItem item = (JCheckBoxMenuItem)containersToVisCheckBoxes.get(evt.getSource());
    if (item != null)
      item.setState(false);
    
    JRadioButtonMenuItem radioButton = (JRadioButtonMenuItem)containersToActiveRadioButtons.get(evt.getSource());
    removeActiveRadioButton(radioButton);
  }
  
  
  
  /**
   * This method is invoked when the plugin ui becomes "active".
   */

  public void pluginUIActivated(PluginUIEvent evt){
    JRadioButtonMenuItem radioButton = (JRadioButtonMenuItem)containersToActiveRadioButtons.get(evt.getSource());
    radioButton.setSelected(true);
  }



  /**
   * This method is invoked when the plugin ui becomes "inactive".
   */

  public void pluginUIDeactivated(PluginUIEvent evt){
    JRadioButtonMenuItem radioButton = (JRadioButtonMenuItem)containersToActiveRadioButtons.get(evt.getSource());
    radioButton.setSelected(false);
  }
  
  
  
  /**
   * This method is invoked when the user performs a ui closing operation on
   * one of the current plugin ui containers.
   */

  public void pluginUIClosing(PluginUIEvent evt){
    
  }
  
  
  
  /**
   * This method is invoked when one of the checkboxes or radio buttons is
   * clicked. 
   */
  public void actionPerformed(ActionEvent evt){
    Object source = evt.getSource();
    if (source instanceof JCheckBoxMenuItem){
      JCheckBoxMenuItem item = (JCheckBoxMenuItem)source;
      PluginUIContainer container = (PluginUIContainer)visCheckBoxesToContainers.get(item);
      
      container.setVisible(item.isSelected());
    }
    else if (source instanceof JRadioButtonMenuItem){
      JRadioButtonMenuItem item = (JRadioButtonMenuItem)source;
      PluginUIContainer container = (PluginUIContainer)activeRadioButtonsToContainers.get(item);
      
      container.setActive(item.isSelected());
    }
  }
  
    
  
  
  /**
   * This method is called when a plugin container has been disposed of.
   */
  
  private void pluginContainerRemoved(AbstractPluginUIContainer pc){
    // remove the visibility controlling menu item
    if (pc.getMode() == UIProvider.HIDEABLE_CONTAINER_MODE){
      JCheckBoxMenuItem item = (JCheckBoxMenuItem)containersToVisCheckBoxes.remove(pc);
      visCheckBoxesToContainers.remove(item);
      removeShowCheckBox(item);
    }
    
    JRadioButtonMenuItem item = (JRadioButtonMenuItem)containersToActiveRadioButtons.remove(pc);
    activeRadioButtonsToContainers.remove(item);
    removeActiveRadioButton(item);
  }
  
  

}
