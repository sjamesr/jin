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



/**
 * The model of a tabbed pane.
 */

public interface TabbedPaneModel{
  
  
  
  /**
   * Returns the number of tabs.
   */
  
  int getTabCount();
  
  
  
  /**
   * Returns the tab at the specified index.
   */
  
  Tab getTab(int index);
  
  
  
  /**
   * Returns the index of the specified tab; <code>-1</code> if no such tab
   * exists in the model.
   */
  
  int indexOfTab(Tab tab);
  
  
  
  /**
   * Returns the index of the tab holding the specified component;
   * <code>-1</code> if no such tab exists in the model.
   */
  
  int indexOfComponent(Component component);
  
  
  
  /**
   * Adds a tab at the specified index.
   */
  
  void addTab(Tab tab, int index);
  
  
  
  /**
   * Appends a tab.
   */
  
  void addTab(Tab tab);
  
  
  
  /**
   * Removes the tab at the specified index.
   */
  
  void removeTab(int index);
  
  
  
  /**
   * Removes all tabs.
   */
  
  void clearTabs();
  
  
  
  /**
   * Returns the index of the currently selected tab, or <code>-1</code> if
   * there are no selected tabs.
   */
  
  int getSelectedIndex();
  
  
  
  /**
   * Returns the selected tab; <code>null</code> if there are no selected tabs.
   */
  
  Tab getSelectedTab();
  
  
  
  /**
   * Selects the tab with the specified index. Use <code>-1</code> to deselect
   * all tabs.
   */
  
  void setSelectedIndex(int index);
  
  
  
  /**
   * Adds a <code>TabbedPaneListener</code>.
   */
  
  void addTabbedPaneListener(TabbedPaneListener listener);
  
  
  
  /**
   * Removes a <code>TabbedPaneListener</code>.
   */
  
  void removeTabbedPaneListener(TabbedPaneListener listener);
  
  
  
}
