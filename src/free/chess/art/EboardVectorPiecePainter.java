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

/*
  This class is based on artwork created by Felipe Paulo Guazzi Bergo and
  licensed by him under the GPL license. I received explicit permission from
  him to license my code that is based on his artwork under the MIT license.
  Here is his contact information:
    eboard - chess client
    http://eboard.sourceforge.net
    Copyright (C) 2000-2001 Felipe Paulo Guazzi Bergo
    bergo@seul.org
*/


package free.chess.art;

import java.awt.*;
import free.chess.PiecePainter;
import free.chess.Piece;
import free.chess.ChessPiece;
import java.awt.image.ImageObserver;


/**
 * An implementation of PiecePainter which draws the vector pieces used in the
 * "eboard" program. The artwork belongs solely to the author(s) of eboard.
 * For information see <A HREF="http://eboard.sourceforge.net/">the eboard
 * website</A>
 */

public class EboardVectorPiecePainter implements PiecePainter{


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
   * Creates a new EboardVectorPiecePainter. White pieces will have the
   * white color and Black pieces the black color.
   */

  public EboardVectorPiecePainter(){
    this(Color.white,Color.black);
  }




  /**
   * Creates a new EboardVectorPiecePainter which will produce
   * White and Black pieces with the given colors. The borders of the
   * pieces will have the opposite RGB values from the RGB values of
   * the given colors (if the color for the White pieces is for example
   * R=255 G=128 B=0 then the border of the White pieces will be
   * R=0 G=127 B=255).
   *
   * @param whitePiecesColor The color for the White pieces.
   * @param blackPiecesColor The color for the Black pieces.
   */

  public EboardVectorPiecePainter(Color whitePiecesColor, Color blackPiecesColor){
    this(whitePiecesColor,blackPiecesColor,getReversed(whitePiecesColor),getReversed(blackPiecesColor));
  }






  /**
   * Creates a new EboardVectorPiecePainter which will produce
   * White and Black pieces with the given colors and the given
   * border colors.
   *
   * @param whitePiecesColor The color for the White pieces.
   * @param blackPiecesColor The color for the Black pieces.
   * @param whiteBorderColor The color for the border of White pieces.
   * @param blackBorderColor The color for the border of Black pieces.
   */

  public EboardVectorPiecePainter(Color whitePiecesColor, Color blackPiecesColor,
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
   * Returns 36x36.
   */

  public Dimension getPreferredPieceSize(){
    return new Dimension(36,36);
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

      kingPolygon.addPoint(50*width/108,8*height/108);
      kingPolygon.addPoint(51*width/108,13*height/108);
      kingPolygon.addPoint(46*width/108,12*height/108);
      kingPolygon.addPoint(46*width/108,17*height/108);
      kingPolygon.addPoint(51*width/108,16*height/108);
      kingPolygon.addPoint(51*width/108,23*height/108);
      kingPolygon.addPoint(49*width/108,26*height/108);
      kingPolygon.addPoint(46*width/108,29*height/108);
      kingPolygon.addPoint(44*width/108,37*height/108);
      kingPolygon.addPoint(43*width/108,41*height/108);
      kingPolygon.addPoint(37*width/108,37*height/108);
      kingPolygon.addPoint(31*width/108,35*height/108);
      kingPolygon.addPoint(27*width/108,35*height/108);
      kingPolygon.addPoint(23*width/108,35*height/108);
      kingPolygon.addPoint(19*width/108,36*height/108);
      kingPolygon.addPoint(15*width/108,38*height/108);
      kingPolygon.addPoint(11*width/108,41*height/108);
      kingPolygon.addPoint(9*width/108,45*height/108);
      kingPolygon.addPoint(7*width/108,49*height/108);
      kingPolygon.addPoint(7*width/108,53*height/108);
      kingPolygon.addPoint(8*width/108,56*height/108);
      kingPolygon.addPoint(9*width/108,59*height/108);
      kingPolygon.addPoint(11*width/108,61*height/108);
      kingPolygon.addPoint(12*width/108,64*height/108);
      kingPolygon.addPoint(18*width/108,70*height/108);
      kingPolygon.addPoint(21*width/108,71*height/108);
      kingPolygon.addPoint(22*width/108,74*height/108);
      kingPolygon.addPoint(22*width/108,82*height/108);
      kingPolygon.addPoint(23*width/108,85*height/108);
      kingPolygon.addPoint(23*width/108,93*height/108);
      kingPolygon.addPoint(38*width/108,98*height/108);
      kingPolygon.addPoint(41*width/108,98*height/108);
      kingPolygon.addPoint(45*width/108,99*height/108);
      kingPolygon.addPoint(57*width/108,99*height/108);
      kingPolygon.addPoint(61*width/108,98*height/108);
      kingPolygon.addPoint(64*width/108,98*height/108);
      kingPolygon.addPoint(68*width/108,97*height/108);
      kingPolygon.addPoint(83*width/108,92*height/108);
      kingPolygon.addPoint(83*width/108,80*height/108);
      kingPolygon.addPoint(84*width/108,76*height/108);
      kingPolygon.addPoint(84*width/108,72*height/108);
      kingPolygon.addPoint(87*width/108,70*height/108);
      kingPolygon.addPoint(95*width/108,62*height/108);
      kingPolygon.addPoint(97*width/108,60*height/108);
      kingPolygon.addPoint(98*width/108,56*height/108);
      kingPolygon.addPoint(99*width/108,52*height/108);
      kingPolygon.addPoint(98*width/108,48*height/108);
      kingPolygon.addPoint(96*width/108,44*height/108);
      kingPolygon.addPoint(93*width/108,40*height/108);
      kingPolygon.addPoint(89*width/108,37*height/108);
      kingPolygon.addPoint(85*width/108,36*height/108);
      kingPolygon.addPoint(81*width/108,35*height/108);
      kingPolygon.addPoint(77*width/108,35*height/108);
      kingPolygon.addPoint(73*width/108,36*height/108);
      kingPolygon.addPoint(67*width/108,39*height/108);
      kingPolygon.addPoint(62*width/108,41*height/108);
      kingPolygon.addPoint(61*width/108,37*height/108);
      kingPolygon.addPoint(59*width/108,29*height/108);
      kingPolygon.addPoint(56*width/108,26*height/108);
      kingPolygon.addPoint(54*width/108,23*height/108);
      kingPolygon.addPoint(54*width/108,16*height/108);
      kingPolygon.addPoint(59*width/108,17*height/108);
      kingPolygon.addPoint(59*width/108,12*height/108);
      kingPolygon.addPoint(54*width/108,13*height/108);
      kingPolygon.addPoint(55*width/108,8*height/108);
    }

    g.setColor(pieceColor);
    g.fillPolygon(kingPolygon);

    g.setColor(borderColor);
    g.drawPolygon(kingPolygon);

    g.setColor(borderColor);
    g.drawLine(44*width/108,40*height/108,53*width/108,58*height/108);
    g.drawLine(53*width/108,58*height/108,53*width/108,68*height/108);
    g.drawLine(62*width/108,40*height/108,53*width/108,58*height/108);
    g.drawLine(23*width/108,73*height/108,53*width/108,68*height/108);
    g.drawLine(53*width/108,68*height/108,83*width/108,73*height/108);
    g.drawLine(24*width/108,92*height/108,53*width/108,86*height/108);
    g.drawLine(53*width/108,86*height/108,82*width/108,92*height/108);
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

      queenPolygon.addPoint(51*width/108,7*height/108);
      queenPolygon.addPoint(48*width/108,10*height/108);
      queenPolygon.addPoint(47*width/108,14*height/108);
      queenPolygon.addPoint(49*width/108,16*height/108);
      queenPolygon.addPoint(51*width/108,18*height/108);
      queenPolygon.addPoint(43*width/108,54*height/108);
      queenPolygon.addPoint(31*width/108,22*height/108);
      queenPolygon.addPoint(34*width/108,18*height/108);
      queenPolygon.addPoint(34*width/108,14*height/108);
      queenPolygon.addPoint(30*width/108,11*height/108);
      queenPolygon.addPoint(26*width/108,12*height/108);
      queenPolygon.addPoint(23*width/108,15*height/108);
      queenPolygon.addPoint(24*width/108,18*height/108);
      queenPolygon.addPoint(25*width/108,21*height/108);
      queenPolygon.addPoint(28*width/108,22*height/108);
      queenPolygon.addPoint(27*width/108,57*height/108);
      queenPolygon.addPoint(12*width/108,33*height/108);
      queenPolygon.addPoint(13*width/108,29*height/108);
      queenPolygon.addPoint(14*width/108,25*height/108);
      queenPolygon.addPoint(11*width/108,21*height/108);
      queenPolygon.addPoint(7*width/108,20*height/108);
      queenPolygon.addPoint(3*width/108,22*height/108);
      queenPolygon.addPoint(2*width/108,26*height/108);
      queenPolygon.addPoint(3*width/108,29*height/108);
      queenPolygon.addPoint(5*width/108,31*height/108);
      queenPolygon.addPoint(8*width/108,32*height/108);
      queenPolygon.addPoint(10*width/108,34*height/108);
      queenPolygon.addPoint(15*width/108,65*height/108);
      queenPolygon.addPoint(21*width/108,71*height/108);
      queenPolygon.addPoint(25*width/108,83*height/108);
      queenPolygon.addPoint(22*width/108,88*height/108);
      queenPolygon.addPoint(19*width/108,95*height/108);
      queenPolygon.addPoint(28*width/108,98*height/108);
      queenPolygon.addPoint(32*width/108,98*height/108);
      queenPolygon.addPoint(35*width/108,99*height/108);
      queenPolygon.addPoint(39*width/108,99*height/108);
      queenPolygon.addPoint(42*width/108,100*height/108);
      queenPolygon.addPoint(62*width/108,100*height/108);
      queenPolygon.addPoint(65*width/108,99*height/108);
      queenPolygon.addPoint(69*width/108,99*height/108);
      queenPolygon.addPoint(73*width/108,98*height/108);
      queenPolygon.addPoint(79*width/108,98*height/108);
      queenPolygon.addPoint(83*width/108,97*height/108);
      queenPolygon.addPoint(87*width/108,95*height/108);
      queenPolygon.addPoint(84*width/108,88*height/108);
      queenPolygon.addPoint(81*width/108,83*height/108);
      queenPolygon.addPoint(83*width/108,71*height/108);
      queenPolygon.addPoint(89*width/108,65*height/108);
      queenPolygon.addPoint(96*width/108,33*height/108);
      queenPolygon.addPoint(98*width/108,32*height/108);
      queenPolygon.addPoint(101*width/108,31*height/108);
      queenPolygon.addPoint(103*width/108,29*height/108);
      queenPolygon.addPoint(104*width/108,25*height/108);
      queenPolygon.addPoint(101*width/108,21*height/108);
      queenPolygon.addPoint(97*width/108,20*height/108);
      queenPolygon.addPoint(93*width/108,22*height/108);
      queenPolygon.addPoint(92*width/108,26*height/108);
      queenPolygon.addPoint(93*width/108,29*height/108);
      queenPolygon.addPoint(94*width/108,32*height/108);
      queenPolygon.addPoint(78*width/108,56*height/108);
      queenPolygon.addPoint(78*width/108,23*height/108);
      queenPolygon.addPoint(80*width/108,22*height/108);
      queenPolygon.addPoint(82*width/108,18*height/108);
      queenPolygon.addPoint(82*width/108,14*height/108);
      queenPolygon.addPoint(78*width/108,11*height/108);
      queenPolygon.addPoint(74*width/108,12*height/108);
      queenPolygon.addPoint(71*width/108,15*height/108);
      queenPolygon.addPoint(72*width/108,18*height/108);
      queenPolygon.addPoint(73*width/108,21*height/108);
      queenPolygon.addPoint(75*width/108,23*height/108);
      queenPolygon.addPoint(62*width/108,54*height/108);
      queenPolygon.addPoint(54*width/108,18*height/108);
      queenPolygon.addPoint(57*width/108,17*height/108);
      queenPolygon.addPoint(59*width/108,13*height/108);
      queenPolygon.addPoint(57*width/108,9*height/108);
      queenPolygon.addPoint(53*width/108,7*height/108);
    }

    g.setColor(pieceColor);
    g.fillPolygon(queenPolygon);


    g.setColor(borderColor);
    g.drawPolygon(queenPolygon);

    g.setColor(borderColor);
    g.drawLine(19*width/108,95*height/108,53*width/108,88*height/108);
    g.drawLine(53*width/108,88*height/108,87*width/108,95*height/108);
    g.drawLine(25*width/108,83*height/108,53*width/108,76*height/108);
    g.drawLine(53*width/108,76*height/108,81*width/108,83*height/108);
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

      rookPolygon.addPoint(35*width/108,16*height/108);
      rookPolygon.addPoint(21*width/108,16*height/108);
      rookPolygon.addPoint(21*width/108,31*height/108);

      rookPolygon.addPoint(29*width/108,39*height/108);
      rookPolygon.addPoint(29*width/108,71*height/108);
      rookPolygon.addPoint(27*width/108,75*height/108);
      rookPolygon.addPoint(23*width/108,79*height/108);
      rookPolygon.addPoint(22*width/108,83*height/108);
      rookPolygon.addPoint(22*width/108,90*height/108);
      rookPolygon.addPoint(17*width/108,90*height/108);
      rookPolygon.addPoint(15*width/108,93*height/108);
      rookPolygon.addPoint(15*width/108,97*height/108);
      rookPolygon.addPoint(17*width/108,99*height/108);
      rookPolygon.addPoint(89*width/108,99*height/108);
      rookPolygon.addPoint(91*width/108,97*height/108);
      rookPolygon.addPoint(91*width/108,93*height/108);
      rookPolygon.addPoint(89*width/108,90*height/108);
      rookPolygon.addPoint(84*width/108,90*height/108);
      rookPolygon.addPoint(84*width/108,83*height/108);
      rookPolygon.addPoint(83*width/108,79*height/108);
      rookPolygon.addPoint(79*width/108,75*height/108);
      rookPolygon.addPoint(77*width/108,71*height/108);
      rookPolygon.addPoint(77*width/108,39*height/108);
      rookPolygon.addPoint(85*width/108,31*height/108);
      rookPolygon.addPoint(85*width/108,16*height/108);
      rookPolygon.addPoint(71*width/108,16*height/108);
      rookPolygon.addPoint(71*width/108,22*height/108);
      rookPolygon.addPoint(60*width/108,22*height/108);
      rookPolygon.addPoint(60*width/108,16*height/108);
      rookPolygon.addPoint(46*width/108,16*height/108);
      rookPolygon.addPoint(46*width/108,22*height/108);
      rookPolygon.addPoint(35*width/108,22*height/108);
    }

    g.setColor(pieceColor);
    g.fillPolygon(rookPolygon);

    g.setColor(borderColor);
    g.drawPolygon(rookPolygon);

    g.setColor(borderColor);
    g.drawLine(21*width/108,31*height/108,85*width/108,31*height/108);
    g.drawLine(23*width/108,79*height/108,83*width/108,79*height/108);
    g.drawLine(27*width/108,75*height/108,79*width/108,75*height/108);
    g.drawLine(29*width/108,39*height/108,77*width/108,39*height/108);
    g.drawLine(22*width/108,90*height/108,84*width/108,90*height/108);
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

      bishopPolygon.addPoint(53*width/108,7*height/108);
      bishopPolygon.addPoint(48*width/108,10*height/108);
      bishopPolygon.addPoint(47*width/108,14*height/108);
      bishopPolygon.addPoint(48*width/108,17*height/108);
      bishopPolygon.addPoint(50*width/108,19*height/108);
      bishopPolygon.addPoint(47*width/108,22*height/108);
      bishopPolygon.addPoint(43*width/108,24*height/108);
      bishopPolygon.addPoint(39*width/108,27*height/108);
      bishopPolygon.addPoint(35*width/108,30*height/108);
      bishopPolygon.addPoint(31*width/108,34*height/108);
      bishopPolygon.addPoint(29*width/108,38*height/108);
      bishopPolygon.addPoint(27*width/108,42*height/108);
      bishopPolygon.addPoint(27*width/108,46*height/108);
      bishopPolygon.addPoint(27*width/108,50*height/108);
      bishopPolygon.addPoint(28*width/108,53*height/108);
      bishopPolygon.addPoint(29*width/108,56*height/108);
      bishopPolygon.addPoint(31*width/108,58*height/108);
      bishopPolygon.addPoint(32*width/108,61*height/108);
      bishopPolygon.addPoint(34*width/108,63*height/108);
      bishopPolygon.addPoint(36*width/108,65*height/108);
      bishopPolygon.addPoint(35*width/108,69*height/108);
      bishopPolygon.addPoint(33*width/108,74*height/108);
      bishopPolygon.addPoint(32*width/108,77*height/108);
      bishopPolygon.addPoint(32*width/108,80*height/108);
      bishopPolygon.addPoint(35*width/108,81*height/108);
      bishopPolygon.addPoint(38*width/108,82*height/108);
      bishopPolygon.addPoint(42*width/108,82*height/108);
      bishopPolygon.addPoint(45*width/108,83*height/108);
      bishopPolygon.addPoint(48*width/108,83*height/108);
      bishopPolygon.addPoint(47*width/108,86*height/108);
      bishopPolygon.addPoint(40*width/108,88*height/108);
      bishopPolygon.addPoint(20*width/108,88*height/108);
      bishopPolygon.addPoint(12*width/108,90*height/108);
      bishopPolygon.addPoint(8*width/108,92*height/108);
      bishopPolygon.addPoint(8*width/108,94*height/108);
      bishopPolygon.addPoint(10*width/108,96*height/108);
      bishopPolygon.addPoint(11*width/108,99*height/108);
      bishopPolygon.addPoint(13*width/108,100*height/108);
      bishopPolygon.addPoint(15*width/108,100*height/108);
      bishopPolygon.addPoint(18*width/108,99*height/108);
      bishopPolygon.addPoint(22*width/108,98*height/108);
      bishopPolygon.addPoint(30*width/108,98*height/108);
      bishopPolygon.addPoint(33*width/108,99*height/108);
      bishopPolygon.addPoint(41*width/108,99*height/108);
      bishopPolygon.addPoint(44*width/108,98*height/108);
      bishopPolygon.addPoint(47*width/108,96*height/108);
      bishopPolygon.addPoint(49*width/108,96*height/108);
      bishopPolygon.addPoint(52*width/108,94*height/108);
      bishopPolygon.addPoint(54*width/108,94*height/108);
      bishopPolygon.addPoint(57*width/108,95*height/108);
      bishopPolygon.addPoint(59*width/108,97*height/108);
      bishopPolygon.addPoint(65*width/108,99*height/108);
      bishopPolygon.addPoint(73*width/108,99*height/108);
      bishopPolygon.addPoint(76*width/108,98*height/108);
      bishopPolygon.addPoint(88*width/108,98*height/108);
      bishopPolygon.addPoint(91*width/108,99*height/108);
      bishopPolygon.addPoint(93*width/108,101*height/108);
      bishopPolygon.addPoint(95*width/108,99*height/108);
      bishopPolygon.addPoint(97*width/108,95*height/108);
      bishopPolygon.addPoint(96*width/108,91*height/108);
      bishopPolygon.addPoint(92*width/108,89*height/108);
      bishopPolygon.addPoint(88*width/108,88*height/108);
      bishopPolygon.addPoint(68*width/108,88*height/108);
      bishopPolygon.addPoint(60*width/108,86*height/108);
      bishopPolygon.addPoint(59*width/108,83*height/108);
      bishopPolygon.addPoint(62*width/108,83*height/108);
      bishopPolygon.addPoint(66*width/108,82*height/108);
      bishopPolygon.addPoint(69*width/108,82*height/108);
      bishopPolygon.addPoint(73*width/108,81*height/108);
      bishopPolygon.addPoint(75*width/108,79*height/108);
      bishopPolygon.addPoint(73*width/108,74*height/108);
      bishopPolygon.addPoint(71*width/108,69*height/108);
      bishopPolygon.addPoint(69*width/108,65*height/108);
      bishopPolygon.addPoint(71*width/108,63*height/108);
      bishopPolygon.addPoint(73*width/108,61*height/108);
      bishopPolygon.addPoint(75*width/108,59*height/108);
      bishopPolygon.addPoint(77*width/108,55*height/108);
      bishopPolygon.addPoint(78*width/108,51*height/108);
      bishopPolygon.addPoint(79*width/108,47*height/108);
      bishopPolygon.addPoint(79*width/108,43*height/108);
      bishopPolygon.addPoint(77*width/108,39*height/108);
      bishopPolygon.addPoint(75*width/108,35*height/108);
      bishopPolygon.addPoint(71*width/108,31*height/108);
      bishopPolygon.addPoint(67*width/108,27*height/108);
      bishopPolygon.addPoint(63*width/108,24*height/108);
      bishopPolygon.addPoint(59*width/108,22*height/108);
      bishopPolygon.addPoint(56*width/108,19*height/108);
      bishopPolygon.addPoint(58*width/108,17*height/108);
      bishopPolygon.addPoint(59*width/108,13*height/108);
      bishopPolygon.addPoint(57*width/108,9*height/108);
    }

    g.setColor(pieceColor);
    g.fillPolygon(bishopPolygon);


    g.setColor(borderColor);
    g.drawPolygon(bishopPolygon);

    g.setColor(borderColor);
    g.drawLine(53*width/108,35*height/108,53*width/108,54*height/108);
    g.drawLine(44*width/108,44*height/108,62*width/108,44*height/108);
    g.drawLine(48*width/108,83*height/108,59*width/108,83*height/108);
    g.drawLine(36*width/108,65*height/108,53*width/108,63*height/108);
    g.drawLine(53*width/108,63*height/108,69*width/108,65*height/108);
    g.drawLine(33*width/108,74*height/108,53*width/108,72*height/108);
    g.drawLine(53*width/108,72*height/108,73*width/108,74*height/108);
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

      knightPolygon.addPoint(29*width/108,10*height/108);
      knightPolygon.addPoint(28*width/108,14*height/108);
      knightPolygon.addPoint(29*width/108,17*height/108);
      knightPolygon.addPoint(29*width/108,21*height/108);
      knightPolygon.addPoint(28*width/108,24*height/108);
      knightPolygon.addPoint(25*width/108,28*height/108);
      knightPolygon.addPoint(23*width/108,32*height/108);
      knightPolygon.addPoint(20*width/108,44*height/108);
      knightPolygon.addPoint(17*width/108,48*height/108);
      knightPolygon.addPoint(14*width/108,52*height/108);
      knightPolygon.addPoint(12*width/108,56*height/108);
      knightPolygon.addPoint(10*width/108,60*height/108);
      knightPolygon.addPoint(9*width/108,64*height/108);
      knightPolygon.addPoint(9*width/108,68*height/108);
      knightPolygon.addPoint(10*width/108,71*height/108);
      knightPolygon.addPoint(12*width/108,73*height/108);
      knightPolygon.addPoint(15*width/108,74*height/108);
      knightPolygon.addPoint(18*width/108,75*height/108);
      knightPolygon.addPoint(22*width/108,75*height/108);
      knightPolygon.addPoint(23*width/108,78*height/108);
      knightPolygon.addPoint(26*width/108,78*height/108);
      knightPolygon.addPoint(28*width/108,76*height/108);
      knightPolygon.addPoint(30*width/108,72*height/108);
      knightPolygon.addPoint(38*width/108,64*height/108);
      knightPolygon.addPoint(40*width/108,64*height/108);
      knightPolygon.addPoint(43*width/108,61*height/108);
      knightPolygon.addPoint(45*width/108,61*height/108);
      knightPolygon.addPoint(48*width/108,58*height/108);
      knightPolygon.addPoint(50*width/108,58*height/108);
      knightPolygon.addPoint(53*width/108,56*height/108);
      knightPolygon.addPoint(55*width/108,54*height/108);
      knightPolygon.addPoint(56*width/108,57*height/108);
      knightPolygon.addPoint(55*width/108,61*height/108);
      knightPolygon.addPoint(54*width/108,65*height/108);
      knightPolygon.addPoint(51*width/108,69*height/108);
      knightPolygon.addPoint(48*width/108,73*height/108);
      knightPolygon.addPoint(44*width/108,77*height/108);
      knightPolygon.addPoint(41*width/108,81*height/108);
      knightPolygon.addPoint(37*width/108,85*height/108);
      knightPolygon.addPoint(35*width/108,89*height/108);
      knightPolygon.addPoint(34*width/108,93*height/108);
      knightPolygon.addPoint(33*width/108,97*height/108);
      knightPolygon.addPoint(35*width/108,99*height/108);
      knightPolygon.addPoint(95*width/108,99*height/108);
      knightPolygon.addPoint(98*width/108,97*height/108);
      knightPolygon.addPoint(98*width/108,89*height/108);
      knightPolygon.addPoint(97*width/108,85*height/108);
      knightPolygon.addPoint(97*width/108,77*height/108);
      knightPolygon.addPoint(96*width/108,73*height/108);
      knightPolygon.addPoint(96*width/108,69*height/108);
      knightPolygon.addPoint(94*width/108,61*height/108);
      knightPolygon.addPoint(92*width/108,53*height/108);
      knightPolygon.addPoint(90*width/108,49*height/108);
      knightPolygon.addPoint(89*width/108,45*height/108);
      knightPolygon.addPoint(87*width/108,41*height/108);
      knightPolygon.addPoint(84*width/108,37*height/108);
      knightPolygon.addPoint(81*width/108,33*height/108);
      knightPolygon.addPoint(74*width/108,25*height/108);
      knightPolygon.addPoint(70*width/108,23*height/108);
      knightPolygon.addPoint(66*width/108,21*height/108);
      knightPolygon.addPoint(62*width/108,20*height/108);
      knightPolygon.addPoint(54*width/108,18*height/108);
      knightPolygon.addPoint(49*width/108,10*height/108);
      knightPolygon.addPoint(46*width/108,13*height/108);
      knightPolygon.addPoint(44*width/108,17*height/108);
      knightPolygon.addPoint(41*width/108,20*height/108);
      knightPolygon.addPoint(37*width/108,16*height/108);
    }

    g.setColor(pieceColor);
    g.fillPolygon(knightPolygon);

    g.setColor(borderColor);
    g.drawPolygon(knightPolygon);

    g.setColor(borderColor);
    g.drawLine(29*width/108,42*height/108,34*width/108,35*height/108);
    g.drawLine(34*width/108,35*height/108,38*width/108,34*height/108);
    g.drawLine(38*width/108,34*height/108,35*width/108,39*height/108);
    g.drawLine(34*width/108,39*height/108,29*width/108,42*height/108);
    g.drawLine(41*width/108,20*height/108,34*width/108,26*height/108);
    g.drawLine(22*width/108,75*height/108,25*width/108,68*height/108);
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

      pawnPolygon.addPoint(50*width/108,15*height/108);
      pawnPolygon.addPoint(46*width/108,17*height/108);
      pawnPolygon.addPoint(43*width/108,21*height/108);
      pawnPolygon.addPoint(42*width/108,25*height/108);
      pawnPolygon.addPoint(42*width/108,29*height/108);
      pawnPolygon.addPoint(45*width/108,33*height/108);
      pawnPolygon.addPoint(40*width/108,35*height/108);
      pawnPolygon.addPoint(36*width/108,39*height/108);
      pawnPolygon.addPoint(35*width/108,43*height/108);
      pawnPolygon.addPoint(34*width/108,47*height/108);
      pawnPolygon.addPoint(34*width/108,51*height/108);
      pawnPolygon.addPoint(35*width/108,54*height/108);
      pawnPolygon.addPoint(36*width/108,57*height/108);
      pawnPolygon.addPoint(38*width/108,59*height/108);
      pawnPolygon.addPoint(40*width/108,61*height/108);
      pawnPolygon.addPoint(34*width/108,65*height/108);
      pawnPolygon.addPoint(29*width/108,70*height/108);
      pawnPolygon.addPoint(27*width/108,74*height/108);
      pawnPolygon.addPoint(23*width/108,82*height/108);
      pawnPolygon.addPoint(22*width/108,86*height/108);
      pawnPolygon.addPoint(21*width/108,90*height/108);
      pawnPolygon.addPoint(21*width/108,94*height/108);
      pawnPolygon.addPoint(22*width/108,97*height/108);
      pawnPolygon.addPoint(25*width/108,98*height/108);
      pawnPolygon.addPoint(81*width/108,98*height/108);
      pawnPolygon.addPoint(85*width/108,97*height/108);
      pawnPolygon.addPoint(85*width/108,93*height/108);
      pawnPolygon.addPoint(84*width/108,85*height/108);
      pawnPolygon.addPoint(83*width/108,81*height/108);
      pawnPolygon.addPoint(80*width/108,74*height/108);
      pawnPolygon.addPoint(77*width/108,69*height/108);
      pawnPolygon.addPoint(72*width/108,64*height/108);
      pawnPolygon.addPoint(67*width/108,61*height/108);
      pawnPolygon.addPoint(69*width/108,59*height/108);
      pawnPolygon.addPoint(71*width/108,55*height/108);
      pawnPolygon.addPoint(72*width/108,51*height/108);
      pawnPolygon.addPoint(72*width/108,47*height/108);
      pawnPolygon.addPoint(71*width/108,43*height/108);
      pawnPolygon.addPoint(69*width/108,39*height/108);
      pawnPolygon.addPoint(65*width/108,35*height/108);
      pawnPolygon.addPoint(61*width/108,33*height/108);
      pawnPolygon.addPoint(64*width/108,29*height/108);
      pawnPolygon.addPoint(64*width/108,25*height/108);
      pawnPolygon.addPoint(62*width/108,21*height/108);
      pawnPolygon.addPoint(59*width/108,17*height/108);
      pawnPolygon.addPoint(55*width/108,15*height/108);
    }

    g.setColor(pieceColor);
    g.fillPolygon(pawnPolygon);

    g.setColor(borderColor);
    g.drawPolygon(pawnPolygon);
  }    
}
