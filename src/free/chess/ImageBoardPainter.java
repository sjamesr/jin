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

import java.awt.*;
import java.awt.image.ImageObserver;


/**
 * An implementation of BoardPainter which uses an Image, or two images (one for
 * light squares and one for dark) to draw the board.
 */

public class ImageBoardPainter implements BoardPainter{


  /**
   * The image of the entire board. This may be null if we're using two square
   * images.
   */

  private final Image boardImage;



  /**
   * The image scaled to the size given in the last call to scaleHint.
   */

  private Image scaled = null;




  /**
   * The image of the light square. This may be null if we're using one big
   * image for the entire board.
   */

  private final Image lightImage;




  /**
   * The image of the dark square. This may be null if we're using one big
   * image for the entire board.
   */

  private final Image darkImage;




  /**
   * The scaled version of lightImage.
   */

  private Image scaledLight = null;




  /**
   * The scaled version of darkImage.
   */

  private Image scaledDark = null;




  /**
   * Creates a new ImageBoardPainter which paints the board with the given Image.
   */

  public ImageBoardPainter(Image boardImage){
    this.boardImage = boardImage;
    this.lightImage = null;
    this.darkImage = null;
  }




  /**
   * Creates a new ImageBoardPainter which paints light squares by using the
   * given light image and dark squares by using the given light image.
   */

  public ImageBoardPainter(Image lightImage, Image darkImage){
    MediaTracker tracker = new MediaTracker(new Canvas());
    this.lightImage = lightImage;
    this.darkImage = darkImage;
    tracker.addImage(lightImage, 0);
    tracker.addImage(darkImage, 0);
    try{
      tracker.waitForAll();
    } catch (InterruptedException e){}
    System.out.println("width="+lightImage.getWidth(null)+" height="+lightImage.getWidth(null));
    this.boardImage = null;
  }




  /**
   * Returns the Image this ImageBoardPainter uses to draw the board, or null
   * if it uses two images (one for light squares and one for dark ones)
   * instead.
   */

  public Image getImage(){
    return boardImage;
  }




  /**
   * Returns the image used for drawing light squares, or null if one big image
   * is used for drawing the whole board.
   */

  public Image getLightImage(){
    return lightImage;
  }




  /**
   * Returns the image used for drawing dark squares, or null if one big image
   * is used for drawing the whole board.
   */

  public Image getDarkImage(){
    return darkImage;
  }




  /**
   * Starts scaling the board image.
   */

  public void scaleHint(int width, int height){
    if (boardImage == null){
      if ((scaledLight != null) && (scaledLight.getWidth(null) == width/8) && (scaledLight.getHeight(null) == height/8))
        return;

      scaledLight = lightImage.getScaledInstance(width/8, height/8, Image.SCALE_FAST);
      scaledDark = darkImage.getScaledInstance(width/8, height/8, Image.SCALE_FAST);
      MediaTracker tracker = new MediaTracker(new Canvas());
      tracker.addImage(scaledLight, 0);
      tracker.addImage(scaledDark, 0);
      try{
        tracker.waitForAll();
      } catch (InterruptedException e){}
    }
    else{
      if ((scaled != null) && (scaled.getWidth(null) == width) && (scaled.getHeight(null) == height))
        return;

      scaled = boardImage.getScaledInstance(width, height, Image.SCALE_FAST);
      MediaTracker tracker = new MediaTracker(new Canvas());
      tracker.addImage(scaled, 0);
      try{
        tracker.waitForAll();
      } catch (InterruptedException e){}
    } 
  }




  /**
   * Returns the size of the unscaled image.
   */

  public Dimension getPreferredBoardSize(){
    if (boardImage == null){
      int width = lightImage.getWidth(null) * 8;
      int height = lightImage.getHeight(null) * 8;
      return new Dimension(width, height);
    }
    return new Dimension(boardImage.getWidth(null), boardImage.getHeight(null)); 
  }



  /**
   * Paints the board at the given location on the given Graphics scaled to
   * the given size.
   */

  public void paintBoard(Graphics g, ImageObserver observer, int x, int y, int width, int height){
    scaleHint(width, height);

    if (boardImage == null){
      Rectangle clipRect = g.getClipRect();
      Rectangle drawnRect = new Rectangle(x, y, width/8, height/8);
      for (int file = 0; file < 8; file++, drawnRect.x += width/8){
        drawnRect.y = y;
        for (int rank = 7; rank >= 0; rank--, drawnRect.y += height/8){
          if (!drawnRect.intersects(clipRect))
            continue;
          
          if ((file+rank) % 2 == 0)
            g.drawImage(scaledDark, drawnRect.x, drawnRect.y, observer);
          else
            g.drawImage(scaledLight, drawnRect.x, drawnRect.y, observer);
        }
      }
        
    }
    else{
      g.drawImage(scaled, x, y, observer);
    }
  } 


}
