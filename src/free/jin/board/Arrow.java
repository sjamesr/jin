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
 * Represents an arrow on the board.
 */
 
public final class Arrow{
  


  /**
   * The "from" square of the arrow.
   */
   
  private final Square from;
  
  
  
  /**
   * The "to" square of the arrow.
   */
   
  private final Square to;
  
  
  
  /**
   * The color of the arrow;
   */
   
  private final Color color;
  
  
  
  /**
   * Creates a new Arrow with the specified "to" and "from" squares color.
   */
   
  public Arrow(Square from, Square to, Color color){
    if ((from == null) || (to == null) || (color == null))
      throw new IllegalArgumentException("Null value not allowed");
    
    this.from = from;
    this.to = to;
    this.color = color;
  }
  
  
  
  /**
   * Returns the "from" square of the arrow.
   */
   
  public Square getFrom(){
    return from;
  }
  
  
  
  /**
   * Returns the "to" square of the arrow.
   */
   
  public Square getTo(){
    return to;
  }
  
  
  
  /**
   * Returns the color of the arrow.
   */
   
  public Color getColor(){
    return color;
  }
  
  
  
  /**
   * Returns whether this arrow is the same as the specified one. Two arrows are
   * the same if they have the same "from" and "to" squares and the same color.
   */
   
  public boolean equals(Object o){
    if (!(o instanceof Arrow))
      return false;
    
    Arrow a = (Arrow)o;
    return from.equals(a.from) && to.equals(a.to) && color.equals(a.color);
  }
  
  
  
  /**
   * Returns the hashcode of this arrow.
   */
   
  public int hashCode(){
    int result = 17;
    result = 37*result + from.hashCode();
    result = 37*result + to.hashCode();
    result = 37*result + color.hashCode();
    return result;
  }
  
 
   
}
