/**
 * Jin - a chess client for internet chess servers.
 * More information is available at http://www.jinchess.com/.
 * Copyright (C) 2004 Alexander Maryanovsky.
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

package free.jin.board;

import free.chess.Square;
import java.awt.Color;


/**
 * Represents a circle on the board.
 */
 
public final class Circle{
  
  
  
  /**
   * The square of the circle.
   */
   
  private final Square square;
  
  
  
  /**
   * The color of the circle.
   */
   
  private final Color color;
  
  
  
  /**
   * Creates a new <code>Circle</code> at the specified square and with the
   * specified color.
   */
   
  public Circle(Square square, Color color){
    if ((square == null) || (color == null))
       throw new IllegalArgumentException("Null value not allowed");
    
    this.square = square;
    this.color = color;
  }
  
  
  
  /**
   * Returns the square of the circle.
   */
   
  public Square getSquare(){
    return square;
  }
  
  
  
  /**
   * Returns the color of the circle.
   */
   
  public Color getColor(){
    return color;
  }
  
  
  
  /**
   * Returns whether this circle is the same as the specified one. Two circles
   * are the same if they are at the same square and have the same color. 
   */
   
  public boolean equals(Object o){
    if (!(o instanceof Circle))
      return false;
    
    Circle c = (Circle)o;
    return square.equals(c.square) && color.equals(c.color);
  }
  
  
  
  /**
   * Returns the hashcode of this circle.
   */
   
  public int hashCode(){
    int result = 17;
    result = 37*result + square.hashCode();
    result = 37*result + color.hashCode();
    return result;
  }
   
  
   
  
}