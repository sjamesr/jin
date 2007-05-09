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

import java.util.ArrayList;
import java.util.List;



/**
 * The default implementation of a <code>TabbedPaneModel</code>.
 */

public class DefaultTabbedPaneModel extends AbstractTabbedPaneModel{
  
  
  
  /**
   * The list of tabs.
   */
  
  private final List tabs = new ArrayList();
  
  
  
  /**
   * The currently selected tab; <code>-1</code> if none.
   */
  
  private int selectedTabIndex = -1;
  
  
  
  /**
   * {@inheritDoc}
   */
  
  public int getTabCount(){
    return tabs.size();
  }
  
  
  
  /**
   * {@inheritDoc}
   */
  
  public Tab getTab(int index){
    return (Tab)tabs.get(index);
  }
  
  
  
  /**
   * {@inheritDoc}
   */
  
  public void addTab(Tab tab, int index){
    tabs.add(index, tab);
    fireTabAdded(index);
  }
  
  
  
  /**
   * {@inheritDoc}
   */
  
  public void removeTab(int index){
    int selected = getSelectedIndex();
    setSelectedIndex(-1); // Deselect it
    
    tabs.remove(index);
    fireTabRemoved(index);
    
    // Select again
    if (selected > index)
      selected = selected - 1;
    setSelectedIndex(Math.min(selected, getTabCount() - 1));
  }
  
  
  
  /**
   * {@inheritDoc}
   */
  
  public int getSelectedIndex(){
    return selectedTabIndex;
  }
  
  
  
  /**
   * {@inheritDoc}
   */
  
  public void setSelectedIndex(int index){
    if (selectedTabIndex != -1)
      fireTabDeselected(selectedTabIndex);
    selectedTabIndex = index;
    if (selectedTabIndex != -1)
      fireTabSelected(selectedTabIndex);
  }
  
  
  
}
