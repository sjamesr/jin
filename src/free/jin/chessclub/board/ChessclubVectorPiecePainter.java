/**
 * Jin - a chess client for internet chess servers.
 * More information is available at http://www.hightemplar.com/jin/.
 * Copyright (C) 2002 Alexander Maryanovsky.
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

package free.jin.chessclub.board;

import java.awt.*;
import free.chess.PiecePainter;
import free.chess.Piece;
import free.chess.ChessPiece;
import java.awt.image.ImageObserver;


/**
 * An implementation of PiecePainter which draws blitzin-like vector pieces.
 * Note that the vector pieces belong exlusively to chessclub.com - you may use
 * them only for products targeted specifically for chessclub.com. You may *not*
 * use them for any other purpose, commercial or otherwise.
 */

public class ChessclubVectorPiecePainter implements PiecePainter{


  /**
   * The color of the white pieces.
   */

  private final Color whitePiecesColor;



  /**
   * The color of the black pieces.
   */

  private final Color blackPiecesColor;



  /**
   * The color of the border of the white pieces.
   */

  private final Color whitePiecesBorderColor;



  /**
   * The color of the border of the black pieces.
   */

  private final Color blackPiecesBorderColor;



  /**
   * The size of the cached pieces.
   */

  private Dimension cachedPieceSize = new Dimension(-1, -1);




  /**
   * The cached piece polygons.
   */

  private Polygon kingPolygon, queenPolygon, rookPolygon, bishopPolygon, knightPolygon, pawnPolygon;




  /**
   * Creates a new ChessclubVectorPiecePainter. White pieces will have the
   * white color and Black pieces the black color.
   */

  public ChessclubVectorPiecePainter(){
    this(Color.white,Color.black);
  }




  /**
   * Creates a new ChessclubVectorPiecePainter which will produce
   * White and Black pieces with the given colors. The borders of the
   * pieces will have the opposite RGB values from the RGB values of
   * the given colors (if the color for the White pieces is for example
   * R=255 G=128 B=0 then the border of the White pieces will be
   * R=0 G=127 B=255).
   *
   * @param whitePiecesColor The color for the White pieces.
   * @param blackPiecesColor The color for the Black pieces.
   */

  public ChessclubVectorPiecePainter(Color whitePiecesColor, Color blackPiecesColor){
    this(whitePiecesColor,blackPiecesColor,getReversed(whitePiecesColor),getReversed(blackPiecesColor));
  }






  /**
   * Creates a new ChessclubVectorPiecePainter which will produce
   * White and Black pieces with the given colors and the given
   * border colors.
   *
   * @param whitePiecesColor The color for the White pieces.
   * @param blackPiecesColor The color for the Black pieces.
   * @param whiteBorderColor The color for the border of White pieces.
   * @param blackBorderColor The color for the border of Black pieces.
   */

  public ChessclubVectorPiecePainter(Color whitePiecesColor, Color blackPiecesColor,
                                      Color whiteBorderColor, Color blackBorderColor){
    this.whitePiecesColor = whitePiecesColor;
    this.blackPiecesColor = blackPiecesColor;
    this.whitePiecesBorderColor = whiteBorderColor;
    this.blackPiecesBorderColor = blackBorderColor;
  }





  /**
   * Returns the color with the opposite RGB values from the given color.
   *
   * @param color The color to reverse.
   */

  private static Color getReversed(Color color){
    return new Color(255-color.getRed(),255-color.getGreen(),255-color.getBlue());
  }




  /**
   * Retrieves the color with which White pieces are drawn.
   *
   * @return The color for the White pieces.
   */

  public Color getWhiteColor(){
    return whitePiecesColor;
  }





  /**
   * Retrieves the color with which Black pieces are drawn.
   *
   * @return The color for the Black pieces.
   */

  public Color getBlackColor(){
    return blackPiecesColor;
  }





  /**
   * Retrieves the color with which borders of White pieces are drawn.
   *
   * @return The color for the borders of White pieces.
   */

  public Color getWhiteBorderColor(){
    return whitePiecesBorderColor;
  }






  /**
   * Retrieves the color with which borders of Black pieces are drawn.
   *
   * @return The color for the borders of Black pieces.
   */

  public Color getBlackBorderColor(){
    return blackPiecesBorderColor;
  }




  /**
   * Does nothing.
   */

  public void scaleHint(int width, int height){

  }




  /**
   * Returns 37x37.
   */

  public Dimension getPreferredPieceSize(){
    return new Dimension(37,37);
  }




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

  public void paintPiece(Piece piece, Graphics g, ImageObserver observer, int x, int y, int width, int height){
    g.translate(x,y);

    Color pieceColor = (piece.isWhite() ? getWhiteColor() : getBlackColor());
    Color borderColor = (piece.isWhite() ? getWhiteBorderColor() : getBlackBorderColor());

    if (!(piece instanceof ChessPiece))
      return;

    ChessPiece cPiece = (ChessPiece)piece;

    if (cPiece.isKing())
      drawKingImage(g, width, height, pieceColor, borderColor);
    else if (cPiece.isQueen())
      drawQueenImage(g, width, height, pieceColor, borderColor);
    else if (cPiece.isRook())
      drawRookImage(g, width, height, pieceColor, borderColor);
    else if (cPiece.isBishop())
      drawBishopImage(g, width, height, pieceColor, borderColor);
    else if (cPiece.isKnight())
      drawKnightImage(g, width, height, pieceColor, borderColor);
    else if (cPiece.isPawn())
      drawPawnImage(g, width, height, pieceColor, borderColor);

    g.translate(-x,-y);
  }






  /**
   * Paints an image of a king of the given size with the given color and border
   * color using the given Graphics.
   */

  protected void drawKingImage(Graphics g, int width, int height, Color pieceColor, Color borderColor){
    if ((cachedPieceSize.width!=width)||(cachedPieceSize.height!=height)||(kingPolygon==null)){
      if (kingPolygon!=null){
        cachedPieceSize.width = width;
        cachedPieceSize.height = height;
        clearPieceCache();
      }

      kingPolygon = new Polygon();
      kingPolygon.addPoint(12*width/37,34*height/37);
      kingPolygon.addPoint(25*width/37,34*height/37);
      kingPolygon.addPoint(27*width/37,33*height/37);
      kingPolygon.addPoint(29*width/37,23*height/37);
      kingPolygon.addPoint(32*width/37,22*height/37);
      kingPolygon.addPoint(34*width/37,20*height/37);
      kingPolygon.addPoint(35*width/37,17*height/37);
      kingPolygon.addPoint(35*width/37,16*height/37);
      kingPolygon.addPoint(34*width/37,13*height/37);
      kingPolygon.addPoint(32*width/37,11*height/37);
      kingPolygon.addPoint(28*width/37,10*height/37);
      kingPolygon.addPoint(24*width/37,12*height/37);
      kingPolygon.addPoint(23*width/37,13*height/37);
      kingPolygon.addPoint(22*width/37,13*height/37);
      kingPolygon.addPoint(22*width/37,11*height/37);
      kingPolygon.addPoint((int)(19.4*width/37),8*height/37);
      kingPolygon.addPoint((int)(19.4*width/37),(int)(6.4*height/37));
      kingPolygon.addPoint(21*width/37,(int)(6.4*height/37));
      kingPolygon.addPoint(21*width/37,(int)(4.6*height/37));
      kingPolygon.addPoint((int)(19.4*width/37),(int)(4.6*height/37));
      kingPolygon.addPoint((int)(19.4*width/37),3*height/37);
      kingPolygon.addPoint((int)(17.6*width/37),3*height/37);
      kingPolygon.addPoint((int)(17.6*width/37),(int)(4.6*height/37));
      kingPolygon.addPoint(16*width/37,(int)(4.6*height/37));
      kingPolygon.addPoint(16*width/37,(int)(6.4*height/37));
      kingPolygon.addPoint((int)(17.6*width/37),(int)(6.4*height/37));
      kingPolygon.addPoint((int)(17.6*width/37),8*height/37);
      kingPolygon.addPoint(15*width/37,11*height/37);
      kingPolygon.addPoint(15*width/37,13*height/37);
      kingPolygon.addPoint(14*width/37,13*height/37);
      kingPolygon.addPoint(13*width/37,12*height/37);
      kingPolygon.addPoint(9*width/37,10*height/37);
      kingPolygon.addPoint(5*width/37,11*height/37);
      kingPolygon.addPoint(3*width/37,13*height/37);
      kingPolygon.addPoint(2*width/37,16*height/37);
      kingPolygon.addPoint(2*width/37,17*height/37);
      kingPolygon.addPoint(3*width/37,20*height/37);
      kingPolygon.addPoint(5*width/37,22*height/37);
      kingPolygon.addPoint(8*width/37,23*height/37);
      kingPolygon.addPoint(10*width/37,33*height/37);
    }

    g.setColor(pieceColor);
    g.fillPolygon(kingPolygon);

    g.setColor(borderColor);
    g.drawPolygon(kingPolygon);

    g.setColor(borderColor);
    g.drawLine(10*width/37,31*height/37,18*width/37,30*height/37);
    g.drawLine(18*width/37,30*height/37,19*width/37,30*height/37);
    g.drawLine(19*width/37,30*height/37,27*width/37,31*height/37);
    g.drawLine(9*width/37,28*height/37,16*width/37,27*height/37);
    g.drawLine(16*width/37,27*height/37,21*width/37,27*height/37);
    g.drawLine(21*width/37,27*height/37,28*width/37,28*height/37);
    g.drawLine(20*width/37,22*height/37,28*width/37,22*height/37);
    g.drawLine(28*width/37,22*height/37,30*width/37,21*height/37);
    g.drawLine(30*width/37,21*height/37,31*width/37,20*height/37);
    g.drawLine(31*width/37,20*height/37,32*width/37,17*height/37);
    g.drawLine(32*width/37,17*height/37,32*width/37,16*height/37);
    g.drawLine(32*width/37,16*height/37,31*width/37,13*height/37);
    g.drawLine(31*width/37,13*height/37,30*width/37,12*height/37);
    g.drawLine(30*width/37,12*height/37,27*width/37,12*height/37);
    g.drawLine(27*width/37,12*height/37,20*width/37,19*height/37);
    g.drawLine(20*width/37,19*height/37,20*width/37,22*height/37);
    g.drawLine(17*width/37,22*height/37,9*width/37,22*height/37);
    g.drawLine(9*width/37,22*height/37,7*width/37,21*height/37);
    g.drawLine(7*width/37,21*height/37,6*width/37,20*height/37);
    g.drawLine(6*width/37,20*height/37,5*width/37,17*height/37);
    g.drawLine(5*width/37,17*height/37,5*width/37,16*height/37);
    g.drawLine(5*width/37,16*height/37,6*width/37,13*height/37);
    g.drawLine(6*width/37,13*height/37,7*width/37,12*height/37);
    g.drawLine(7*width/37,12*height/37,10*width/37,12*height/37);
    g.drawLine(10*width/37,12*height/37,17*width/37,19*height/37);
    g.drawLine(17*width/37,19*height/37,17*width/37,22*height/37);
    g.drawLine(19*width/37,11*height/37,20*width/37,14*height/37);
    g.drawLine(20*width/37,14*height/37,19*width/37,15*height/37);
    g.drawLine(19*width/37,15*height/37,18*width/37,15*height/37);
    g.drawLine(18*width/37,15*height/37,17*width/37,14*height/37);
    g.drawLine(17*width/37,14*height/37,18*width/37,11*height/37);
    g.drawLine(18*width/37,11*height/37,19*width/37,11*height/37);
  }






  /**
   * Paints an image of a queen of the given size with the given color and border
   * color using the given Graphics.
   */

  protected void drawQueenImage(Graphics g, int width, int height, Color pieceColor, Color borderColor){
    if ((cachedPieceSize.width!=width)||(cachedPieceSize.height!=height)||(queenPolygon==null)){
      if (queenPolygon!=null){
        cachedPieceSize.width = width;
        cachedPieceSize.height = height;
        clearPieceCache();
      }

      queenPolygon = new Polygon();

      queenPolygon.addPoint(8*width/37,34*height/37);
      queenPolygon.addPoint(29*width/37,34*height/37);
      queenPolygon.addPoint(29*width/37,27*height/37);
      queenPolygon.addPoint(32*width/37,13*height/37);
      queenPolygon.addPoint(33*width/37,11*height/37);
      queenPolygon.addPoint(35*width/37,9*height/37);
      queenPolygon.addPoint(35*width/37,7*height/37);
      queenPolygon.addPoint(34*width/37,6*height/37);
      queenPolygon.addPoint(31*width/37,6*height/37);
      queenPolygon.addPoint(30*width/37,7*height/37);
      queenPolygon.addPoint(30*width/37,10*height/37);
      queenPolygon.addPoint(31*width/37,11*height/37);
      queenPolygon.addPoint(26*width/37,19*height/37);
      queenPolygon.addPoint(24*width/37,8*height/37);
      queenPolygon.addPoint(26*width/37,6*height/37);
      queenPolygon.addPoint(26*width/37,4*height/37);
      queenPolygon.addPoint(25*width/37,3*height/37);
      queenPolygon.addPoint(22*width/37,3*height/37);
      queenPolygon.addPoint(21*width/37,4*height/37);
      queenPolygon.addPoint(21*width/37,7*height/37);
      queenPolygon.addPoint(22*width/37,8*height/37);
      queenPolygon.addPoint(19*width/37,17*height/37);
      queenPolygon.addPoint(18*width/37,17*height/37);
      queenPolygon.addPoint(15*width/37,8*height/37);
      queenPolygon.addPoint(16*width/37,7*height/37);
      queenPolygon.addPoint(16*width/37,4*height/37);
      queenPolygon.addPoint(15*width/37,3*height/37);
      queenPolygon.addPoint(12*width/37,3*height/37);
      queenPolygon.addPoint(11*width/37,4*height/37);
      queenPolygon.addPoint(11*width/37,6*height/37);
      queenPolygon.addPoint(13*width/37,8*height/37);
      queenPolygon.addPoint(11*width/37,19*height/37);
      queenPolygon.addPoint(6*width/37,11*height/37);
      queenPolygon.addPoint(7*width/37,10*height/37);
      queenPolygon.addPoint(7*width/37,7*height/37);
      queenPolygon.addPoint(6*width/37,6*height/37);
      queenPolygon.addPoint(3*width/37,6*height/37);
      queenPolygon.addPoint(2*width/37,7*height/37);
      queenPolygon.addPoint(2*width/37,9*height/37);
      queenPolygon.addPoint(4*width/37,11*height/37);
      queenPolygon.addPoint(5*width/37,13*height/37);
      queenPolygon.addPoint(8*width/37,27*height/37);

    }

    g.setColor(pieceColor);
    g.fillPolygon(queenPolygon);


    g.setColor(borderColor);
    g.drawPolygon(queenPolygon);

    g.setColor(borderColor);
    g.drawLine(29*width/37,31*height/37,24*width/37,30*height/37);
    g.drawLine(24*width/37,30*height/37,13*width/37,30*height/37);
    g.drawLine(13*width/37,30*height/37,8*width/37,31*height/37);
    g.drawLine(8*width/37,26*height/37,13*width/37,25*height/37);
    g.drawLine(13*width/37,25*height/37,24*width/37,25*height/37);
    g.drawLine(24*width/37,25*height/37,29*width/37,26*height/37);
  }







  /**
   * Paints an image of a rook of the given size with the given color and border
   * color using the given Graphics.
   */

  protected void drawRookImage(Graphics g, int width, int height, Color pieceColor, Color borderColor){
    if ((cachedPieceSize.width!=width)||(cachedPieceSize.height!=height)||(rookPolygon==null)){
      if (rookPolygon!=null){
        cachedPieceSize.width = width;
        cachedPieceSize.height = height;
        clearPieceCache();
      }

      rookPolygon = new Polygon();

      rookPolygon.addPoint(5*width/37,34*height/37);
      rookPolygon.addPoint(32*width/37,34*height/37);
      rookPolygon.addPoint(32*width/37,32*height/37);
      rookPolygon.addPoint(26*width/37,27*height/37);
      rookPolygon.addPoint(26*width/37,12*height/37);
      rookPolygon.addPoint(30*width/37,8*height/37);
      rookPolygon.addPoint(30*width/37,3*height/37);
      rookPolygon.addPoint(25*width/37,3*height/37);
      rookPolygon.addPoint(25*width/37,6*height/37);
      rookPolygon.addPoint(21*width/37,6*height/37);
      rookPolygon.addPoint(21*width/37,3*height/37);
      rookPolygon.addPoint(16*width/37,3*height/37);
      rookPolygon.addPoint(16*width/37,6*height/37);
      rookPolygon.addPoint(12*width/37,6*height/37);
      rookPolygon.addPoint(12*width/37,3*height/37);
      rookPolygon.addPoint(7*width/37,3*height/37);
      rookPolygon.addPoint(7*width/37,8*height/37);
      rookPolygon.addPoint(11*width/37,12*height/37);
      rookPolygon.addPoint(11*width/37,27*height/37);
      rookPolygon.addPoint(5*width/37,32*height/37);
    }

    g.setColor(pieceColor);
    g.fillPolygon(rookPolygon);

    g.setColor(borderColor);
    g.drawPolygon(rookPolygon);

    g.setColor(borderColor);
    g.drawLine(26*width/37,27*height/37,11*width/37,27*height/37);
    g.drawLine(26*width/37,12*height/37,11*width/37,12*height/37);
  }






  /**
   * Paints an image of a bishop of the given size with the given color and border
   * color using the given Graphics.
   */

  protected void drawBishopImage(Graphics g, int width, int height, Color pieceColor, Color borderColor){
    if ((cachedPieceSize.width!=width)||(cachedPieceSize.height!=height)||(bishopPolygon==null)){
      if (bishopPolygon!=null){
        cachedPieceSize.width = width;
        cachedPieceSize.height = height;
        clearPieceCache();
      }

      bishopPolygon = new Polygon();

      bishopPolygon.addPoint(3*width/37,34*height/37);
      bishopPolygon.addPoint(34*width/37,34*height/37);
      bishopPolygon.addPoint(33*width/37,31*height/37);
      bishopPolygon.addPoint(24*width/37,31*height/37);
      bishopPolygon.addPoint(26*width/37,18*height/37);
      bishopPolygon.addPoint(26*width/37,15*height/37);
      bishopPolygon.addPoint(25*width/37,12*height/37);
      bishopPolygon.addPoint(20*width/37,7*height/37);
      bishopPolygon.addPoint(21*width/37,5*height/37);
      bishopPolygon.addPoint(20*width/37,3*height/37);
      bishopPolygon.addPoint(17*width/37,3*height/37);
      bishopPolygon.addPoint(16*width/37,5*height/37);
      bishopPolygon.addPoint(17*width/37,7*height/37);
      bishopPolygon.addPoint(12*width/37,12*height/37);
      bishopPolygon.addPoint(11*width/37,15*height/37);
      bishopPolygon.addPoint(11*width/37,18*height/37);
      bishopPolygon.addPoint(13*width/37,31*height/37);
      bishopPolygon.addPoint(4*width/37,31*height/37);
    }

    g.setColor(pieceColor);
    g.fillPolygon(bishopPolygon);


    g.setColor(borderColor);
    g.drawPolygon(bishopPolygon);

    g.setColor(borderColor);
    g.drawLine(24*width/37,31*height/37,22*width/37,29*height/37);
    g.drawLine(22*width/37,29*height/37,15*width/37,29*height/37);
    g.drawLine(15*width/37,29*height/37,13*width/37,31*height/37);
    g.drawLine(25*width/37,25*height/37,12*width/37,25*height/37);
    g.drawLine(18*width/37,19*height/37,19*width/37,19*height/37);
    g.drawLine(19*width/37,19*height/37,19*width/37,16*height/37);
    g.drawLine(19*width/37,16*height/37,21*width/37,16*height/37);
    g.drawLine(21*width/37,16*height/37,21*width/37,15*height/37);
    g.drawLine(21*width/37,15*height/37,19*width/37,15*height/37);
    g.drawLine(19*width/37,15*height/37,19*width/37,13*height/37);
    g.drawLine(19*width/37,13*height/37,18*width/37,13*height/37);
    g.drawLine(18*width/37,13*height/37,18*width/37,15*height/37);
    g.drawLine(18*width/37,15*height/37,16*width/37,15*height/37);
    g.drawLine(16*width/37,15*height/37,16*width/37,16*height/37);
    g.drawLine(16*width/37,16*height/37,18*width/37,16*height/37);
    g.drawLine(18*width/37,16*height/37,18*width/37,19*height/37);
  }






  /**
   * Paints an image of a knight of the given size with the given color and border
   * color using the given Graphics.
   */

  protected void drawKnightImage(Graphics g, int width, int height, Color pieceColor, Color borderColor){
    if ((cachedPieceSize.width!=width)||(cachedPieceSize.height!=height)||(knightPolygon==null)){
      if (knightPolygon!=null){
        cachedPieceSize.width = width;
        cachedPieceSize.height = height;
        clearPieceCache();
      }

      knightPolygon = new Polygon();

      knightPolygon.addPoint(10*width/37,34*height/37);
      knightPolygon.addPoint(34*width/37,34*height/37);
      knightPolygon.addPoint(34*width/37,26*height/37);
      knightPolygon.addPoint(31*width/37,14*height/37);
      knightPolygon.addPoint(26*width/37,9*height/37);
      knightPolygon.addPoint(21*width/37,7*height/37);
      knightPolygon.addPoint(18*width/37,3*height/37);
      knightPolygon.addPoint(17*width/37,3*height/37);
      knightPolygon.addPoint(18*width/37,7*height/37);
      knightPolygon.addPoint(14*width/37,3*height/37);
      knightPolygon.addPoint(13*width/37,3*height/37);
      knightPolygon.addPoint(14*width/37,7*height/37);
      knightPolygon.addPoint(12*width/37,6*height/37);
      knightPolygon.addPoint(9*width/37,9*height/37);
      knightPolygon.addPoint(2*width/37,23*height/37);
      knightPolygon.addPoint(6*width/37,27*height/37);
      knightPolygon.addPoint(10*width/37,23*height/37);
      knightPolygon.addPoint((int)(7.4*width/37),27*height/37);
      knightPolygon.addPoint(8*width/37,27*height/37);
      knightPolygon.addPoint(17*width/37,20*height/37);
      knightPolygon.addPoint(18*width/37,22*height/37);
      knightPolygon.addPoint(10*width/37,31*height/37);
    }

    g.setColor(pieceColor);
    g.fillPolygon(knightPolygon);

    g.setColor(borderColor);
    g.drawPolygon(knightPolygon);

    g.setColor(borderColor);
    g.drawLine(12*width/37,11*height/37,11*width/37,11*height/37);
    g.drawLine(11*width/37,11*height/37,10*width/37,12*height/37);
    g.drawLine(10*width/37,12*height/37,9*width/37,14*height/37);
    g.drawLine(9*width/37,14*height/37,11*width/37,13*height/37);
    g.drawLine(11*width/37,13*height/37,12*width/37,12*height/37);
    g.drawLine(12*width/37,12*height/37,12*width/37,11*height/37);
    g.drawLine(21*width/37,10*height/37,27*width/37,15*height/37);
    g.drawLine(27*width/37,15*height/37,30*width/37,32*height/37);
    g.drawLine(30*width/37,32*height/37,32*width/37,32*height/37);
    g.drawLine(32*width/37,32*height/37,28*width/37,15*height/37);
    g.drawLine(28*width/37,15*height/37,22*width/37,10*height/37);
    g.drawLine(22*width/37,10*height/37,21*width/37,10*height/37);
    g.drawLine(5*width/37,20*height/37,4*width/37,23*height/37);
    g.drawLine(4*width/37,23*height/37,5*width/37,24*height/37);
    g.drawLine(5*width/37,24*height/37,6*width/37,21*height/37);
    g.drawLine(6*width/37,21*height/37,5*width/37,20*height/37);
  }




  /**
   * Paints an image of a pawn of the given size with the given color and border
   * color using the given Graphics.
   */

  protected void drawPawnImage(Graphics g, int width, int height, Color pieceColor, Color borderColor){
    if ((cachedPieceSize.width!=width)||(cachedPieceSize.height!=height)||(pawnPolygon==null)){
      if (pawnPolygon!=null){
        cachedPieceSize.width = width;
        cachedPieceSize.height = height;
        clearPieceCache();
      }

      pawnPolygon = new Polygon();

      pawnPolygon.addPoint(6*width/37,31*height/37);
      pawnPolygon.addPoint(31*width/37,31*height/37);
      pawnPolygon.addPoint(31*width/37,27*height/37);
      pawnPolygon.addPoint(28*width/37,23*height/37);
      pawnPolygon.addPoint(22*width/37,19*height/37);
      pawnPolygon.addPoint(25*width/37,17*height/37);
      pawnPolygon.addPoint(25*width/37,13*height/37);
      pawnPolygon.addPoint(23*width/37,11*height/37);
      pawnPolygon.addPoint(21*width/37,11*height/37);
      pawnPolygon.addPoint(23*width/37,9*height/37);
      pawnPolygon.addPoint(23*width/37,6*height/37);
      pawnPolygon.addPoint(21*width/37,4*height/37);
      pawnPolygon.addPoint(16*width/37,4*height/37);
      pawnPolygon.addPoint(14*width/37,6*height/37);
      pawnPolygon.addPoint(14*width/37,9*height/37);
      pawnPolygon.addPoint(16*width/37,11*height/37);
      pawnPolygon.addPoint(14*width/37,11*height/37);
      pawnPolygon.addPoint(12*width/37,13*height/37);
      pawnPolygon.addPoint(12*width/37,17*height/37);
      pawnPolygon.addPoint(15*width/37,19*height/37);
      pawnPolygon.addPoint(9*width/37,23*height/37);
      pawnPolygon.addPoint(6*width/37,27*height/37);
    }

    g.setColor(pieceColor);
    g.fillPolygon(pawnPolygon);

    g.setColor(borderColor);
    g.drawPolygon(pawnPolygon);
  }    
}
