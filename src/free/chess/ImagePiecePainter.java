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
 * along with Foobar; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package free.chess;

import java.awt.Graphics;
import java.awt.Image;
import java.awt.Dimension;
import java.awt.image.ImageObserver;
import java.util.Hashtable;
import java.util.Enumeration;


/**
 * An implementation of PiecePainter which paints images.
 */

public class ImagePiecePainter implements PiecePainter{



  /**
   * An array whose indices specify the size of the images and whose values
   * are Hashtables mapping Pieces to Images.
   */

  private final Hashtable [] pieceImages;




  /**
   * The preferred size of the pieces.
   */

  private final Dimension prefPieceSize;




  /**
   * Creates a new ImagePiecePainter with the given preferred piece image size.
   * The given Hashtable should map Integer objects specifying the size of the
   * piece images to Hashtables which in turn map Piece objects to piece Images.
   * The constructor does not clone the given Hashtable, so it should not be
   * modified after creating the ImagePiecePainter.
   */

  public ImagePiecePainter(Dimension prefPieceSize, Hashtable pieceImages){

    // Find the largest size
    int maxSize = 0;
    Enumeration sizes = pieceImages.keys();
    while (sizes.hasMoreElements()){
      int size = ((Integer)sizes.nextElement()).intValue();
      if (size <= 0)
        throw new IllegalArgumentException("Image sizes must be positive");

      if (size > maxSize)
        maxSize = size;
    }

    if (maxSize == 0)
      throw new IllegalArgumentException("No sizes in the hashtable");

    this.pieceImages = new Hashtable[maxSize+1];

    // Fill the array
    sizes = pieceImages.keys();
    while (sizes.hasMoreElements()){
      Integer key = (Integer)sizes.nextElement();
      this.pieceImages[key.intValue()] = (Hashtable)pieceImages.get(key);
    }

    this.prefPieceSize = new Dimension(prefPieceSize.width, prefPieceSize.height);
  }




  /**
   * Returns the Image by which the given Piece is represented at the given
   * size.
   */

  public Image getPieceImage(int size, Piece piece){
    if (size <= 0)
      throw new IllegalArgumentException("Image size must be positive");

    if (size >= pieceImages.length)
      return (Image)(pieceImages[pieceImages.length-1].get(piece));

    if (pieceImages[size] != null)
      return (Image)(pieceImages[size].get(piece));

    for (int i = size; i > 0; i--)
      if (pieceImages[i] != null)
        return (Image)(pieceImages[i].get(piece));

    for (int i = size+1; i < pieceImages.length; i++)
      if (pieceImages[i] != null)
        return (Image)(pieceImages[i].get(piece));

    throw new Error("This can't happen");
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
    return new Dimension(prefPieceSize.width, prefPieceSize.height);
  }





  /**
   * Paints the image of the given piece at the given coordinates on the given
   * Graphics object scaled to the given size.
   */

  public void paintPiece(Piece piece, Graphics g, ImageObserver observer, int x, int y, int width, int height){
    int size = width > height ? height : width;
    Image pieceImage = getPieceImage(size, piece);
    int pieceWidth = pieceImage.getWidth(null);
    int pieceHeight = pieceImage.getHeight(null);
    g.drawImage(pieceImage, x+(width-pieceWidth)/2, y+(height-pieceHeight)/2, observer);
  }

}