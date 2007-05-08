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

import javax.swing.Icon;


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
  
  private final String title;
  
  
  
  /**
   * The icon of the tab; may be <code>null</code>.
   */
  
  private final Icon icon;
  
  
  
  /**
   * Whether the tab is closeable.
   */
  
  private final boolean isCloseable;
  
  
  
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
   * Returns the tab's icon; may be <code>null</code>. 
   */
  
  public Icon getIcon(){
    return icon;
  }
  
  
  
  /**
   * Returns whether the tab should display UI to allow the user to close it.
   */
  
  public boolean isCloseable(){
    return isCloseable;
  }
  
  
  
}
