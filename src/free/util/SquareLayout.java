/**
 * The utillib library.
 * More information is available at http://www.jinchess.com/.
 * Copyright (C) 2002 Alexander Maryanovsky.
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

package free.util;

import java.awt.LayoutManager;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;


/**
 * A LayoutManager which lays out a single component making sure its height is
 * always the same as its width.
 */

public class SquareLayout implements LayoutManager{
  

  
  /**
   * Creates a new SquareLayout.
   */

  public SquareLayout(){

  }
  
  
  
  /**
   * Creates a new Container with SquareLayout which will contain the specified
   * component.
   */
   
  public static Container createSquareContainer(Component child){
    Container container = new Container();
    container.setLayout(new SquareLayout());
    container.add(child);
    
    return container;
  }



  /**
   * Adds the specified component with the specified name to the layout. 
   */

  public void addLayoutComponent(String name, Component component){
    
  }
  
  

  /**
   * Removes the specified component from the layout. 
   */

  public void removeLayoutComponent(Component component){
    
  }
  
  
  
  /**
   * Returns the sole child of the specified container, or <code>null</code> if
   * none. Throws an <code>IllegalStateException</code> if there is more than
   * one child.
   */
   
  private Component getChild(Container c){
    int childCount = c.getComponentCount();
    if (childCount > 1)
      throw new IllegalStateException("May not layout more than one component");
    else if (childCount == 0)
      return null;
    else
      return c.getComponent(childCount - 1);
  }



  /**
   * Lays out the container in the specified panel. 
   */

  public void layoutContainer(Container container){
    Component child = getChild(container);
    if (child == null)
      return;
    
    Dimension parentSize = container.getSize();
    int minSize = parentSize.width < parentSize.height ? parentSize.width : parentSize.height;
    child.setBounds(0, 0, minSize, minSize);
  }

  

  /**
   * Calculates the minimum size dimensions for the specified panel given the 
   * components in the specified parent container. 
   */

  public Dimension minimumLayoutSize(Container container){
    Component child = getChild(container);
    if (child == null)
      return new Dimension(0, 0);
    
    Dimension childMinSize = child.getMinimumSize();
    int maxSize = childMinSize.width > childMinSize.height ? childMinSize.width : childMinSize.height;
    return new Dimension(maxSize, maxSize);
  }




  /**
   * Calculates the preferred size dimensions for the specified panel given the
   * components in the specified parent container. 
   */

  public Dimension preferredLayoutSize(Container container){
    Component child = getChild(container);
    if (child == null)
      return new Dimension(0, 0);
    
    Dimension childPrefSize = child.getPreferredSize();
    int maxSize = childPrefSize.width > childPrefSize.height ? childPrefSize.width : childPrefSize.height;
    return new Dimension(maxSize, maxSize);
  }


  
}
