/**
 * The chess framework library.
 * More information is available at http://www.jinchess.com/.
 * Copyright (C) 2002 Alexander Maryanovsky.
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

import java.awt.Color;


/**
 * A skeleton implementation of <code>ColoredBoardPainter</code>.
 */
 
public abstract class AbstractColoredBoardPainter implements ColoredBoardPainter{
  
  
  
  /**
   * The color of the light squares.
   */
   
  private Color lightColor;
  
  
  
  /**
   * The color of the dark squares.
   */
   
  private Color darkColor;
  
  
  
  /**
   * Creates a new <code>AbstractColoredBoardPainter</code> with the specified
   * colors for light and dark squares.
   */
   
  public AbstractColoredBoardPainter(Color lightColor, Color darkColor){
    if (lightColor == null)
      throw new IllegalArgumentException("Null light color");
    if (darkColor == null)
      throw new IllegalArgumentException("Null dark color");

    this.lightColor = lightColor;
    this.darkColor = darkColor;
  }
  
  
  
  /**
   * Creates a new <code>AbstractColoredBoardPainter</code> with default colors
   * for light and dark squares.
   */
   
  public AbstractColoredBoardPainter(){
    this(Color.white.darker(), Color.black.brighter());
  }
  
  
  
  /**
   * Returns the color with which light squares are drawn.
   */
   
  public Color getLightColor(){
    return lightColor;
  }
  
  
  
  /**
   * Sets the color with which light squares are drawn;
   */
   
  public void setLightColor(Color lightColor){
    if (lightColor == null)
      throw new IllegalArgumentException("Null color");
    
    this.lightColor = lightColor;
  }
  
  
  
  /**
   * Returns the color with which dark squares are drawn.
   */
   
  public Color getDarkColor(){
    return darkColor;
  }
  
  
  
  /**
   * Sets the color with which dark squares are drawn;
   */
   
  public void setDarkColor(Color darkColor){
    if (darkColor == null)
      throw new IllegalArgumentException("Null color");
    
    this.darkColor = darkColor;
  }
  
   
   
}