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
import free.chess.Piece;
import free.chess.ChessPiece;
                                                

/**            
 * An abstract class offering a convenient partial implementation for vector
 * based piece sets.
 */

public abstract class VectorPiecePainter extends AbstractColoredPiecePainter{



  /**
   * The size of the cached pieces.
   */

  private Dimension cachedPieceSize = new Dimension(-1, -1);




  /**
   * The cached piece polygons.
   */

  private Polygon kingPolygon, queenPolygon, rookPolygon, bishopPolygon, knightPolygon, pawnPolygon;




  /**
   * Creates a new <code>VectorPiecePainter</code>.
   */
   
  public VectorPiecePainter(){
    super(Color.white, Color.black);
  }



  /**
   * Creates a new <code>VectorPiecePainter</code> which will draw white and
   * black pieces using the given colors.
   *
   * @param whiteColor The color for the white pieces.
   * @param blackColor The color for the black pieces.
   */

  public VectorPiecePainter(Color whiteColor, Color blackColor){
    super(whiteColor, blackColor);
  }



  /**
   * Creates a new <code>VectorPiecePainter</code> which will produce white and
   * black pieces with the given colors and the given outline colors.
   *
   * @param whiteColor The color for the white pieces.
   * @param blackColor The color for the black pieces.
   * @param whiteOutline The color for the outline of white pieces.
   * @param blackOutline The color for the outline of black pieces.
   */

  public VectorPiecePainter(Color whiteColor, Color blackColor,
                            Color whiteOutline, Color blackOutline){
    super(whiteColor, blackColor, whiteOutline, blackOutline);
  }




  
  /**
   * Creates and returns a Polygon of a king when fit into the given dimensions.
   * This polygon will be used to draw king pieces.
   */

  protected abstract Polygon createKingPolygon(int width, int height);




  /**
   * Creates and returns a Polygon of a queen when fit into the given dimensions.
   * This polygon will be used to draw queen pieces.
   */

  protected abstract Polygon createQueenPolygon(int width, int height);




  /**
   * Creates and returns a Polygon of a rook when fit into the given dimensions.
   * This polygon will be used to draw rook pieces.
   */

  protected abstract Polygon createRookPolygon(int width, int height);




  /**
   * Creates and returns a Polygon of a bishop when fit into the given dimensions.
   * This polygon will be used to draw bishop pieces.
   */

  protected abstract Polygon createBishopPolygon(int width, int height);




  /**
   * Creates and returns a Polygon of a knight when fit into the given dimensions.
   * This polygon will be used to draw knight pieces.
   */

  protected abstract Polygon createKnightPolygon(int width, int height);




  /**
   * Creates and returns a Polygon of a pawn when fit into the given dimensions.
   * This polygon will be used to draw pawn pieces.
   */

  protected abstract Polygon createPawnPolygon(int width, int height);




  /**
   * Clears the caching of piece polygons.
   */

  private void clearPieceCache(){
    kingPolygon = null;
    queenPolygon = null;
    rookPolygon = null;
    bishopPolygon = null;
    knightPolygon = null;
    pawnPolygon = null;
  }



  /**
   * Draws the given piece at the given coordinates with the given size on
   * the given Graphics.
   */

  public final void paintPiece(Piece piece, Graphics g, Component component, Rectangle rect,
      boolean isShaded){

    Color pieceColor = getPieceColor(piece, isShaded);
    Color outlineColor = getOutlineColor(piece, isShaded);

    int x = rect.x;
    int y = rect.y;
    int width = rect.width;
    int height = rect.height;

    g.translate(x,y);

    if (!(piece instanceof ChessPiece))
      return;

    ChessPiece cPiece = (ChessPiece)piece;

    if ((cachedPieceSize.width != width) || (cachedPieceSize.height != height)){
      clearPieceCache();
      cachedPieceSize.width = width;
      cachedPieceSize.height = height;
    }
    
    if (cPiece.isKing())
      drawKingImage(g, width, height, pieceColor, outlineColor);
    else if (cPiece.isQueen())
      drawQueenImage(g, width, height, pieceColor, outlineColor);
    else if (cPiece.isRook())
      drawRookImage(g, width, height, pieceColor, outlineColor);
    else if (cPiece.isBishop())
      drawBishopImage(g, width, height, pieceColor, outlineColor);
    else if (cPiece.isKnight())
      drawKnightImage(g, width, height, pieceColor, outlineColor);
    else if (cPiece.isPawn())
      drawPawnImage(g, width, height, pieceColor, outlineColor);

    g.translate(-x, -y);
  }



  /**
   * Paints an image of a king of the given size with the given color and
   * outline color using the given Graphics.
   */

  protected void drawKingImage(Graphics g, int width, int height,
      Color pieceColor, Color outlineColor){
    
    if (kingPolygon == null)
      kingPolygon = createKingPolygon(width, height);

    g.setColor(pieceColor);
    g.fillPolygon(kingPolygon);

    g.setColor(outlineColor);
    g.drawPolygon(kingPolygon);
  }






  /**
   * Paints an image of a queen of the given size with the given color and
   * outline color using the given Graphics.
   */

  protected void drawQueenImage(Graphics g, int width, int height,
      Color pieceColor, Color outlineColor){
        
    if (queenPolygon == null)
      queenPolygon = createQueenPolygon(width, height);

    g.setColor(pieceColor);
    g.fillPolygon(queenPolygon);

    g.setColor(outlineColor);
    g.drawPolygon(queenPolygon);
  }




  /**
   * Paints an image of a rook of the given size with the given color and
   * outline color using the given Graphics.
   */

  protected void drawRookImage(Graphics g, int width, int height,
      Color pieceColor, Color outlineColor){
        
    if (rookPolygon == null)
      rookPolygon = createRookPolygon(width, height);

    g.setColor(pieceColor);
    g.fillPolygon(rookPolygon);

    g.setColor(outlineColor);
    g.drawPolygon(rookPolygon);
  }






  /**
   * Paints an image of a bishop of the given size with the given color and
   * outline color using the given Graphics.
   */

  protected void drawBishopImage(Graphics g, int width, int height,
      Color pieceColor, Color outlineColor){
    
    if (bishopPolygon == null)
      bishopPolygon = createBishopPolygon(width, height);

    g.setColor(pieceColor);
    g.fillPolygon(bishopPolygon);

    g.setColor(outlineColor);
    g.drawPolygon(bishopPolygon);
  }






  /**
   * Paints an image of a knight of the given size with the given color and
   * outline color using the given Graphics.
   */

  protected void drawKnightImage(Graphics g, int width, int height,
      Color pieceColor, Color outlineColor){
        
    if (knightPolygon == null)
      knightPolygon = createKnightPolygon(width, height);

    g.setColor(pieceColor);
    g.fillPolygon(knightPolygon);

    g.setColor(outlineColor);
    g.drawPolygon(knightPolygon);
  }




  /**
   * Paints an image of a pawn of the given size with the given color and
   * outline color using the given Graphics.
   */

  protected void drawPawnImage(Graphics g, int width, int height,
      Color pieceColor, Color outlineColor){
    
    if (pawnPolygon == null)
      pawnPolygon = createPawnPolygon(width, height);

    g.setColor(pieceColor);
    g.fillPolygon(pawnPolygon);

    g.setColor(outlineColor);
    g.drawPolygon(pawnPolygon);
  }    



}
