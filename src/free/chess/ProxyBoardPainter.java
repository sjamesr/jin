/**
 * The chess framework library.
 * More information is available at http://www.jinchess.com/.
 * Copyright (C) 2004 Alexander Maryanovsky.
 * All rights reserved.
 *
 * The chess framework library is free software; you can redistribute
 * it and/or modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 2 of the
 * License, or (at your option) any later version.
 *
 * The chess framework library is distributed in the hope that it will
 * be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser
 * General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with the chess framework library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package free.chess;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Component;


/**
 * A <code>BoardPainter</code> implementation which delegates the actual painter
 * to another <code>BoardPainter</code>, allowing board painters that can be
 * instantiated with the default constructor. Typically, you would want to
 * extend this class, create the real board painter in a class initializer and
 * simply pass it to superclass's (this class's) constructor whenever your
 * board painter is instantiated.
 */
 
public class ProxyBoardPainter implements BoardPainter{
  
  
  
  /**
   * The delegate board painter.
   */
   
  private final BoardPainter delegate;
  
  
  
  /**
   * Creates a new <code>ProxyBoardPainter</code> with the specified delegate
   * board painter.
   */
   
  public ProxyBoardPainter(BoardPainter delegate){
    this.delegate = delegate;   
  }
  
  

  /**
   * Delegates the action to the delegate BoardPainter.
   */

   public void paintBoard(Graphics g, Component component, int x, int y, int width, int height){
     delegate.paintBoard(g, component, x, y, width, height);
   }
  

   
}
