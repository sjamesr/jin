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

package free.chess;

import java.awt.Graphics;
import java.awt.Image;
import java.awt.Dimension;
import java.awt.image.ImageObserver;
import java.util.Hashtable;


/**
 * An implementation of PiecePainter which paints images. Note that due to
 * JDK1.1 limitations, this implementation will not try to scale the pieces.
 */

public class ImagePiecePainter implements PiecePainter{


  /**
   * A Hashtable mapping Pieces to Images.
   */

  private final Hashtable pieceImages = new Hashtable();




  /**
   * The size of the pieces.
   */

  private final Dimension pieceSize;





  /**
   * Creates a new ImagePiecePainter with the given piece image size. Note that
   * you may set images of different size than this (and than each other) to
   * represent pieces. This size is used in the getPreferredPieceSize method,
   * so it should be something at least close to the size of the images.
   */

  public ImagePiecePainter(Dimension pieceSize){
    this.pieceSize = new Dimension(pieceSize.width, pieceSize.height);
  }



  /**
   * Sets the given Piece to be represented by the given Image.
   */

  public void setPieceImage(Piece piece, Image image){
    pieceImages.put(piece, image);
  }



  /**
   * Returns the Image by which the given Piece is represented.
   */

  public Image getPieceImage(Piece piece){
    return (Image)pieceImages.get(piece);
  }




  /**
   * Does nothing since this ImagePiecePainter does not attempt to scale the
   * images.
   */

  public void scaleHint(int width, int height){

  }




  /**
   * Returns the preferred size of the pieces.
   */

  public Dimension getPreferredPieceSize(){
    return pieceSize;
  }





  /**
   * Paints the image of the given piece at the given coordinates on the given
   * Graphics object scaled to the given size.
   */

  public void paintPiece(Piece piece, Graphics g, ImageObserver observer, int x, int y, int width, int height){
    Image pieceImage = (Image)pieceImages.get(piece);
    int pieceWidth = pieceImage.getWidth(null);
    int pieceHeight = pieceImage.getHeight(null);
    g.drawImage(pieceImage, x+(width-pieceWidth)/2, y+(height-pieceHeight)/2, observer);
  }

}