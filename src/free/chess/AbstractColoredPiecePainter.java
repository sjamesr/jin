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

import java.awt.*;


/**
 * A skeleton implementation of <code>ColoredPiecePainter</code>. 
 */

public abstract class AbstractColoredPiecePainter implements ColoredPiecePainter{


  
  /**
   * The color of the white pieces.
   */

  private Color whiteColor;



  /**
   * The color of the black pieces.
   */

  private Color blackColor;



  /**
   * The color of the outline of the white pieces.
   */

  private Color whiteOutline;



  /**
   * The color of the outline of the black pieces.
   */

  private Color blackOutline;


  
  /**
   * Creates a new <code>AbstractColoredPiecePainter</code>. White pieces will
   * be drawn with white black pieces with black.
   */

  public AbstractColoredPiecePainter(){
    this(Color.white,Color.black);
  }




  /**
   * Creates a new <code>AbstractColoredPiecePainter</code> which will draw
   * white and black pieces using the given colors. The outline of the pieces
   * will have an RGB value reverse to the RGB values of the given colors (if
   * the color for the white pieces is for example R=255 G=128 B=0 then the
   * outline of the white pieces will be R=0 G=127 B=255).
   *
   * @param whiteColor The color for the white pieces.
   * @param blackColor The color for the black pieces.
   */

  public AbstractColoredPiecePainter(Color whiteColor, Color blackColor){
    this(whiteColor, blackColor, getReversed(whiteColor), getReversed(blackColor));
  }




  /**
   * Creates a new <code>AbstractColoredPiecePainter</code> which will produce
   * white and black pieces with the given colors and the given outline colors.
   *
   * @param whiteColor The color for the white pieces.
   * @param blackColor The color for the black pieces.
   * @param whiteOutline The color for the outline of white pieces.
   * @param blackOutline The color for the outline of black pieces.
   */

  public AbstractColoredPiecePainter(Color whiteColor, Color blackColor,
      Color whiteOutline, Color blackOutline){
    
    if (whiteColor == null)
      throw new IllegalArgumentException("Null white color");
    if (blackColor == null)
      throw new IllegalArgumentException("Null black color");
    if (whiteOutline == null)
      throw new IllegalArgumentException("Null white outline color");
    if (blackOutline == null)
      throw new IllegalArgumentException("Null black outline color");
    
    this.whiteColor = whiteColor;
    this.blackColor = blackColor;
    this.whiteOutline = whiteOutline;
    this.blackOutline = blackOutline;
  }

  
  
  /**
   * Returns the color with the opposite RGB values from the given color.
   *
   * @param color The color to reverse.
   */

  private static Color getReversed(Color color){
    return new Color(255 - color.getRed(), 255 - color.getGreen(), 255 - color.getBlue());
  }
  


  /**
   * Retrieves the color with which white pieces are drawn.
   *
   * @return The color for the white pieces.
   */

  public Color getWhiteColor(){
    return whiteColor;
  }



  /**
   * Sets the color with which white pieces are drawn.
   */

  public void setWhiteColor(Color color){
    if (color == null)
      throw new IllegalArgumentException("Null color");
    
    whiteColor = color;
  }



  /**
   * Retrieves the color with which black pieces are drawn.
   *
   * @return The color for the black pieces.
   */

  public Color getBlackColor(){
    return blackColor;
  }



  /**
   * Sets the color with which black pieces are drawn.
   */

  public void setBlackColor(Color color){
    if (color == null)
      throw new IllegalArgumentException("Null color");
    
    blackColor = color;
  }



  /**
   * Retrieves the color with which the outline of white pieces is drawn.
   *
   * @return The color for the outline of white pieces.
   */

  public Color getWhiteOutline(){
    return whiteOutline;
  }



  /**
   * Sets the color with which the outline of white pieces is drawn.
   */

  public void setWhiteOutline(Color color){
    if (color == null)
      throw new IllegalArgumentException("Null color");
    
    whiteOutline = color;
  }



  /**
   * Retrieves the color with which the outline of black pieces is drawn.
   *
   * @return The color for the outline of black pieces.
   */

  public Color getBlackOutline(){
    return blackOutline;
  }


  
  /**
   * Sets the color with which the outline of black pieces is drawn.
   */

  public void setBlackOutline(Color color){
    if (color == null)
      throw new IllegalArgumentException("Null color");
    
    blackOutline = color;
  }
  
  
  
  /**
   * Returns with which the specified piece should be drawn.
   */
   
  public Color getPieceColor(Piece piece, boolean isShaded){
    if (piece == null)
      return null;
    
    Color color = piece.isWhite() ? getWhiteColor() : getBlackColor();
    return isShaded ? getShaded(color) : color; 
  }
  
  
  
  /**
   * Returns the color with which the outline of the specified piece should be
   * drawn.
   */
   
  public Color getOutlineColor(Piece piece, boolean isShaded){
    if (piece == null)
      return null;
    
    Color color = piece.isWhite() ? getWhiteOutline() : getBlackOutline();
    return isShaded ? getShaded(color) : color;
  }
  
  
  
  /**
   * Returns a shaded version of the specified color.
   */

  protected Color getShaded(Color color){
    int r = (color.getRed() + 128*2)/3;
    int g = (color.getGreen() + 128*2)/3;
    int b = (color.getBlue() + 128*2)/3;

    return new Color(r, g, b);
  }
  
  


}
