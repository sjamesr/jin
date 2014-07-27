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

package free.util.swing;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.LayoutManager2;
import java.awt.Rectangle;

import free.util.UnsupportedOperationException;



/**
 * A layout manager which makes all the children span the entire area of the
 * parent.
 */

public class WrapLayout implements LayoutManager2{
  
  
  
  /**
   * Our shared instance.
   */
  
  private static final WrapLayout SHARED_INSTANCE = new WrapLayout();
  
  
  
  /**
   * Returns an instance of <code>WrapLayout</code>.
   */
  
  public static WrapLayout getInstance(){
    return SHARED_INSTANCE;
  }
  
  
  
  /**
   * Lays out the specified container.
   */
  
  @Override
  public void layoutContainer(Container parent){
    Dimension size = parent.getSize();
    Insets insets = parent.getInsets();
    
    Rectangle bounds = new Rectangle(insets.left, insets.top,
        size.width - insets.left - insets.right, size.height - insets.top - insets.bottom);

    int componentCount = parent.getComponentCount();
    for (int i = 0; i < componentCount; i++)
      parent.getComponent(i).setBounds(bounds);
  }
  
  
  
  /**
   * Appends the specified insets to the specified <code>Dimension</code> object
   * and returns it.
   */
  
  private static Dimension append(Dimension size, Insets insets){
    size.width += insets.left + insets.right;
    size.height += insets.top + insets.bottom;
    
    return size;
  }
  
  
  
  /**
   * Returns the minimum size of the specified container, when laid out by us.
   */
  
  @Override
  public Dimension minimumLayoutSize(Container parent){
    Dimension size = new Dimension(0, 0);
    
    int componentCount = parent.getComponentCount();
    for (int i = 0; i < componentCount; i++){
      Dimension minSize = parent.getComponent(i).getMinimumSize();
      size.width = Math.max(size.width, minSize.width);
      size.height = Math.max(size.height, minSize.height);
    }
    
    Insets insets = parent.getInsets();
    
    return append(size, insets);
  }
  
  
  
  /**
   * Returns the preferred size of the specified container, when laid out by us.
   */
  
  @Override
  public Dimension preferredLayoutSize(Container parent){
    Dimension size = new Dimension(0, 0);
    
    int componentCount = parent.getComponentCount();
    for (int i = 0; i < componentCount; i++){
      Dimension prefSize = parent.getComponent(i).getPreferredSize();
      size.width = Math.max(size.width, prefSize.width);
      size.height = Math.max(size.height, prefSize.height);
    }
    
    Insets insets = parent.getInsets();
    
    return append(size, insets);
  }
  
  
  
  /**
   * Returns the maximum size of the specified container, when laid out by us.
   */
  
  @Override
  public Dimension maximumLayoutSize(Container parent){
    Dimension size = new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE);
    
    int componentCount = parent.getComponentCount();
    for (int i = 0; i < componentCount; i++){
      Dimension maxSize = parent.getComponent(i).getMaximumSize();
      size.width = Math.min(size.width, maxSize.width);
      size.height = Math.min(size.height, maxSize.height);
    }
    
    Insets insets = parent.getInsets();
    
    return append(size, insets);
  }
  
  
  
  /**
   * Throws an exception.
   */
  
  @Override
  public void addLayoutComponent(String name, Component comp){
    throw new UnsupportedOperationException("deprecated addLayoutComponent(String, Component)");
  }
  
  
  
  /**
   * Adds the specified component to the layout.
   */
  
  @Override
  public void addLayoutComponent(Component comp, Object constraints){
    
  }
  
  
  
  /**
   * Removes the specified component from the layout.
   */
  
  @Override
  public void removeLayoutComponent(Component comp){
    
  }
  
  
  
  /**
   * Returns the x-axis layout alignment of the specified container.
   */
  
  @Override
  public float getLayoutAlignmentX(Container target){
    return Component.CENTER_ALIGNMENT;
  }
  
  
  
  /**
   * Returns the y-axis layout alignment of the specified container.
   */
  
  @Override
  public float getLayoutAlignmentY(Container target){
    return Component.CENTER_ALIGNMENT;
  }
  
  
  
  /**
   * Invalidates the layout, dropping any cached values.
   */
  
  @Override
  public void invalidateLayout(Container target){
    
  }
  
  
  
}
