/**
 * The utillib library.
 * More information is available at http://www.jinchess.com/.
 * Copyright (C) 2007 Alexander Maryanovsky.
 * All rights reserved.
 *
 * The utillib library is free software; you can redistribute
 * it and/or modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 2 of the
 * License, or (at your option) any later version.
 *
 * The utillib library is distributed in the hope that it will
 * be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser
 * General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with utillib library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package free.util.swing.tabbedpane;

import java.awt.Component;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import javax.swing.Icon;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;


/**
 * Encapsulates a single tab.
 */

public class Tab{
  
  
  
  /**
   * The component displayed in the tab.
   */
  
  private final Component component;
  
  
  
  /**
   * The title of the tab; may be <code>null</code>.
   */
  
  private String title;
  
  
  
  /**
   * The icon of the tab; may be <code>null</code>.
   */
  
  private Icon icon;
  
  
  
  /**
   * Whether the tab is closeable.
   */
  
  private boolean isCloseable;
  
  
  
  /**
   * Our tab close approver.
   */
  
  private TabCloseApprover tabCloseApprover = null;
  
  
  
  /**
   * Our change listeners.
   */
  
  private final List changeListeners = new LinkedList();
  
  
  
  /**
   * Creates a new <code>Tab</code> with the specified arguments.
   * 
   * @param component The component displayed in the tab.
   * @param title The title of the tab.
   * @param icon The icon of the tab.
   * @param isCloseable Whether the tab should have UI to allow the user to
   * close it.
   */
  
  public Tab(Component component, String title, Icon icon, boolean isCloseable){
    if (component == null)
      throw new IllegalArgumentException("component may not be null");
    
    this.component = component;
    this.title = title;
    this.icon = icon;
    this.isCloseable = isCloseable;
  }
  
  
  
  /**
   * Adds a change listener to this tab.
   */
  
  public void addChangeListener(ChangeListener listener){
    changeListeners.add(listener);
  }
  
  
  
  /**
   * Removes a change listener from this tab.
   */
  
  public void removeChangeListener(ChangeListener listener){
    changeListeners.remove(listener);
  }
  
  
  
  /**
   * Fires a state change event.
   */
  
  protected void fireStateChanged(){
    ChangeEvent evt = new ChangeEvent(this);
    for (Iterator i = changeListeners.iterator(); i.hasNext();){
      ChangeListener listener = (ChangeListener)i.next();
      listener.stateChanged(evt);
    }
  }
  
  
  
  /**
   * Returns the component displayed in the tab.
   */
  
  public Component getComponent(){
    return component;
  }
  
  
  
  /**
   * Returns the tab's title; may be <code>null</code>.
   */
  
  public String getTitle(){
    return title;
  }
  
  
  
  /**
   * Sets the title of the tab.
   */
  
  public void setTitle(String title){
    this.title = title;
    fireStateChanged();
  }
  
  
  
  /**
   * Returns the tab's icon; may be <code>null</code>. 
   */
  
  public Icon getIcon(){
    return icon;
  }
  
  
  
  /**
   * Sets the icon of this tab.
   */
  
  public void setIcon(Icon icon){
    this.icon = icon;
    fireStateChanged();
  }
  
  
  
  /**
   * Returns whether the tab should display UI to allow the user to close it.
   */
  
  public boolean isCloseable(){
    return isCloseable;
  }
  
  
  
  /**
   * Sets the closeable state of this tab.
   */
  
  public void setCloseable(boolean isCloseable){
    this.isCloseable = isCloseable;
    fireStateChanged();
  }
  
  
  
  /**
   * Returns the current approver of tab close actions; <code>null</code> if
   * none.
   */
  
  public TabCloseApprover getTabCloseApprover(){
    return tabCloseApprover;
  }
  
  
  
  /**
   * Sets the approver of tab close actions. The approver is consulted when the
   * user attempts to close the tab. If the approver disapproves, the
   * action is canceled and the tab isn't closed. A <code>null</code> approver
   * is not consulted and thus the close action always goes forward. The default
   * value is <code>null</code>. In addition to this approver, the tabbed pane's
   * approver is also consulted.
   */
  
  public void setTabCloseApprover(TabCloseApprover tabCloseApprover){
    this.tabCloseApprover = tabCloseApprover;
  }
  
  
  
}
