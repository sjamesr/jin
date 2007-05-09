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

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;



/**
 * A partial (still abstract) implementation of a tabbed pane model which
 * handles some of the mundane tasks. 
 */

public abstract class AbstractTabbedPaneModel implements TabbedPaneModel{
  
  
  
  /**
   * The list of registered <code>TabbedPaneListener</code>s.
   */
  
  private final List listeners = new LinkedList();
  
  
  
  /**
   * {@inheritDoc}
   */
  
  public int indexOfTab(Tab tab){
    int tabCount = getTabCount();
    for (int i = 0; i < tabCount; i++)
      if (getTab(i).equals(tab))
        return i;
    return -1;
  }
  
  
  
  /**
   * {@inheritDoc}
   */
  
  public void addTabbedPaneListener(TabbedPaneListener listener){
    listeners.add(listener);
  }
  
  
  
  /**
   * {@inheritDoc} 
   */
  
  public void removeTabbedPaneListener(TabbedPaneListener listener){
    listeners.remove(listener);
  }
  
  
  
  /**
   * Fires a TAB_ADDED event.
   */
  
  protected void fireTabAdded(int tabIndex){
    TabbedPaneEvent evt = new TabbedPaneEvent(this, TabbedPaneEvent.TAB_ADDED, tabIndex);
    for (Iterator i = listeners.iterator(); i.hasNext();){
      TabbedPaneListener listener = (TabbedPaneListener)i.next();
      listener.tabAdded(evt);
    }
  }
  
  
  
  /**
   * Fires a TAB_REMOVED event.
   */
  
  protected void fireTabRemoved(int tabIndex){
    TabbedPaneEvent evt = new TabbedPaneEvent(this, TabbedPaneEvent.TAB_REMOVED, tabIndex);
    for (Iterator i = listeners.iterator(); i.hasNext();){
      TabbedPaneListener listener = (TabbedPaneListener)i.next();
      listener.tabRemoved(evt);
    }
  }
  
  
  
  /**
   * Fires a TAB_SELECTED event.
   */
  
  protected void fireTabSelected(int tabIndex){
    TabbedPaneEvent evt = new TabbedPaneEvent(this, TabbedPaneEvent.TAB_SELECTED, tabIndex);
    for (Iterator i = listeners.iterator(); i.hasNext();){
      TabbedPaneListener listener = (TabbedPaneListener)i.next();
      listener.tabSelected(evt);
    }
  }
  
  
  
  /**
   * Fires a TAB_DESELECTED event.
   */
  
  protected void fireTabDeselected(int tabIndex){
    TabbedPaneEvent evt = new TabbedPaneEvent(this, TabbedPaneEvent.TAB_DESELECTED, tabIndex);
    for (Iterator i = listeners.iterator(); i.hasNext();){
      TabbedPaneListener listener = (TabbedPaneListener)i.next();
      listener.tabDeselected(evt);
    }
  }
  
  
  
}
