/**
 * The utillib library.
 * More information is available at http://www.jinchess.com/.
 * Copyright (C) 2004 Alexander Maryanovsky.
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

import javax.swing.JPanel;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.LayoutManager;
import java.awt.BorderLayout;

/**
 * A small subclass of JPanel which simply overrides the
 * <code>getMaximumSize</code> and <code>getMinimumSize</code> to return the
 * value returned by <code>getPreferredSize</code>. This allows the panel to
 * always be sized at its preferred size.
 */
 
public class PreferredSizedPanel extends JPanel{
  
  
  
  /**
   * Creates a new <code>PreferredSizedPanel</code>.
   */
   
  public PreferredSizedPanel(){
     
  }
  
  
  
  /**
   * Creates a new <code>PreferredSizedPanel</code> with the specified layout
   * manager.
   */
   
  public PreferredSizedPanel(LayoutManager layoutManager){
    super(layoutManager);
  }
  
  
  
  /**
   * Creates a new <code>PreferredSizedPanel</code> with the specified double
   * buffering strategy.
   */
   
  public PreferredSizedPanel(boolean isDoubleBuffered){
    super(isDoubleBuffered);
  }
  
  
  
  /**
   * Creates a new <code>PreferredSizedPanel</code> with the specified layout
   * manager and double buffering strategy.
   */
   
  public PreferredSizedPanel(LayoutManager layoutManager, boolean isDoubleBuffered){
    super(layoutManager, isDoubleBuffered);
  }
  
  
  
  /**
   * Creates a <code>PreferredSizedPanel</code> which will hold the specified
   * component. This allows forcing any component to always be sized at its
   * preferred size.
   */
   
  public PreferredSizedPanel createWrapper(Component component){
    PreferredSizedPanel panel = new PreferredSizedPanel(new BorderLayout());
    panel.add(component, BorderLayout.CENTER);
    
    return panel;
  }
  
  
  
  /**
   * Overrides <code>getMinimumSize</code> to return the value returned by
   * <code>getPreferredSize</code>.
   */
   
  public Dimension getMinimumSize(){
    return getPreferredSize();
  }
  
  
  
  /**
   * Overrides <code>getMaximumSize</code> to return the value returned by
   * <code>getPreferredSize</code>.
   */
   
  public Dimension getMaximumSize(){
    return getPreferredSize();
  }
  
  
  
}
