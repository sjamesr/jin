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
import java.awt.Dimension;
import java.awt.Image;
import java.awt.image.ImageObserver;


/**
 * An implementation of BoardPainter which uses an Image to draw the board.
 */

public class ImageBoardPainter implements BoardPainter{


  /**
   * The image.
   */

  private final Image boardImage;



  /**
   * The image scaled to the size given in the last call to scaleHint.
   */

  private Image scaledBoardImage = null;




  /**
   * Creates a new ImageBoardPainter which paints the board with the given Image.
   */

  public ImageBoardPainter(Image boardImage){
    this.boardImage = boardImage;
  }



  /**
   * Returns the Image this ImageBoardPainter uses to draw the board.
   */

  public Image getImage(){
    return boardImage;
  }



  /**
   * Starts scaling the board image.
   */

  public void scaleHint(int width, int height){
    // TODO: implement
  }




  /**
   * Returns the size of the unscaled image.
   */

  public Dimension getPreferredBoardSize(){
    return new Dimension(boardImage.getWidth(null), boardImage.getHeight(null)); 
  }



  /**
   * Paints the board at the given location on the given Graphics scaled to
   * the given size.
   */

  public void paintBoard(Graphics g, ImageObserver observer, int x, int y, int width, int height){
    g.drawImage(boardImage, x, y, width, height, observer);
  } 


}
