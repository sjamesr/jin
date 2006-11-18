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
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.util.Properties;

import free.util.IOUtilities;
import free.util.ImageUtilities;


/**
 * An implementation of BoardPainter which uses an Image, or two images (one for
 * light squares and one for dark) to draw the board.
 */

public final class ImageBoardPainter implements ResourceBoardPainter{
  
  
  
  /**
   * The image of the entire board. This may be null if we're using two square
   * images.
   */

  private Image boardImage;



  /**
   * The image scaled to the size given in the last call to scaleImages.
   */

  private Image scaled = null;




  /**
   * The image of the light square. This may be null if we're using one big
   * image for the entire board.
   */

  private Image lightImage;




  /**
   * The image of the dark square. This may be null if we're using one big
   * image for the entire board.
   */

  private Image darkImage;




  /**
   * The scaled version of lightImage.
   */

  private Image scaledLight = null;




  /**
   * The scaled version of darkImage.
   */

  private Image scaledDark = null;




  /**
   * True if the light and dark images should be scaled, false if sliced.
   * Only relevant if boardImage is null.
   */

  private boolean isScaled;


  
  
  /**
   * A no-arg constructor so that this <code>ImageBoardPainter</code> can be
   * used as a <code>ResourceBoardPainter</code>.
   */
   
  public ImageBoardPainter(){
     
  }



  /**
   * Creates a new ImageBoardPainter which paints the board with the given Image.
   */

  public ImageBoardPainter(Image boardImage){
    this.boardImage = boardImage;
    this.lightImage = null;
    this.darkImage = null;
    isScaled = false;
  }




  /**
   * Creates a new ImageBoardPainter which paints light squares by using the
   * given light image and dark squares by using the given light image.
   * There are two options for using the images. One is having them scaled and
   * fitted into each square - this means that all squares will look the same.
   * For this option, you should provide relatively small images, around 60x60.
   * The second option is to have the images used by pieces - each square will
   * contain a different, unscaled portion of the large given image. For this
   * option you should provide a large image - 200x200 to 300x300 is a good
   * idea. If you don't, all the squares will look about the same. Since in the
   * case the images are too small, the implementation will tile them, the
   * second option can also be used if you have very small images with
   * repeating, tileable patterns. The <code>isScaled</code> argument specified
   * which option is chosen, <code>true</code> for scaling (first option) and
   * <code>false</code> for slicing/tiling.
   */

  public ImageBoardPainter(Image lightImage, Image darkImage, boolean isScaled){
    this.lightImage = lightImage;
    this.darkImage = darkImage;
    this.isScaled = isScaled;
    this.boardImage = null;
  }
  
  
  
  /**
   * Since <code>ImageBoardPainter</code>s are immutable, simply returns
   * <code>this</code>.
   */
  
  public BoardPainter freshInstance(){
    return this;
  }




  /**
   * Loads the board images from the specified URL. The structure at the
   * specified url is described below.
   * A properties file named "definition" must be located at the base URL.
   * That file should contain the following two properties:
   * <ul>
   *   <li><code>type</code>: The value is either "single" or "light-dark". This
   *       specifies whether there is a single image of the entire board or two
   *       pattern images for the light and dark squares. If this is omitted,
   *       "light-dark" is assumed.
   *   <li><code>image.type</code>: Specifies the extension (type) of the
   *       image(s) - gif, png, etc. If this is omitted, "gif" is assumed.
   * </ul>
   * If the value of <code>type</code> is "single", the image will be loaded
   * from a file named "board.gif" (assuming <code>image.type</code> is "gif").
   * If the value of <code>type</code> is "light-dark", the images will be
   * loaded from "light.gif" and "dark.gif". In this case, the definition file
   * must contain the boolean property <code>scaleSquares</code>. This property
   * specifies whether the square images specify an exact image of each square
   * or a general pattern to be used for the squares. In effect, if the value
   * of <code>scaleSquares</code> is "true", the light and dark square images
   * will be stretched to fill each square on the board. If it is "false", the
   * images will be sliced and tiled to fill each square. The default value is
   * "false". In the first case all the squares will look the same - in the 2nd
   * they may not look the same if the patterns are bigger than the squares. If
   * all your desired values are the default ones, you may omit the definition
   * file altogether.
   */
   
  public void load(URL url) throws IOException{
    if ((boardImage != null) || (lightImage != null))
      throw new IllegalStateException("This ImageBoardPainter has already been loaded");
    
    URL defURL = new URL(url, "definition");

    Properties def = IOUtilities.loadProperties(defURL, true);
    if (def == null)
      def = new Properties();

    String type = def.getProperty("type", "light-dark");     
    String ext = def.getProperty("ext", "gif");
    
    Toolkit toolkit = Toolkit.getDefaultToolkit();    
    
    if ("single".equals(type)){
      Image boardImage = toolkit.getImage(new URL(url, "board." + ext));
      
      this.boardImage = boardImage;
    }
    else if ("light-dark".equals(type)){
      Image lightImage = toolkit.getImage(new URL(url, "light." + ext));
      Image darkImage = toolkit.getImage(new URL(url, "dark." + ext));

      boolean isScaled = def.getProperty("scaleSquares", "false").equalsIgnoreCase("true");
      
      this.lightImage = lightImage;
      this.darkImage = darkImage;
      this.isScaled = isScaled; 
    }
    else
      throw new IOException("Unrecognized type value: " + type); 
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
   * Returns <code>true</code> if the light and dark images are scaled, false
   * if they are sliced/tiled. This is only applicable if we're in the two-image
   * mode.
   */

  public boolean isScaled(){
    return isScaled;
  }
  
  
  
  /**
   * Preloads the images.
   */
  
  private void preloadImages(){
    if (boardImage == null){
      if ((lightImage instanceof BufferedImage) && (darkImage instanceof BufferedImage))
        return;
      
      ImageUtilities.preload(new Image[]{lightImage, darkImage}, new int[2]); 
      
      BufferedImage bufferedLightImage = 
        new BufferedImage(lightImage.getWidth(null), lightImage.getHeight(null), BufferedImage.TYPE_INT_RGB);
      bufferedLightImage.getGraphics().drawImage(lightImage, 0, 0, null);
      lightImage = bufferedLightImage;
      
      BufferedImage bufferedDarkImage = 
        new BufferedImage(darkImage.getWidth(null), darkImage.getHeight(null), BufferedImage.TYPE_INT_RGB);
      bufferedDarkImage.getGraphics().drawImage(darkImage, 0, 0, null);      
      darkImage = bufferedDarkImage;
    }
    else{
      if (boardImage instanceof BufferedImage)
        return;
      
      ImageUtilities.preload(boardImage);
      BufferedImage bufferedImage = 
        new BufferedImage(boardImage.getWidth(null), boardImage.getHeight(null), BufferedImage.TYPE_INT_RGB);
      bufferedImage.getGraphics().drawImage(boardImage, 0, 0, null);
      boardImage = bufferedImage;
    }
  }



  /**
   * Scales the board image.
   */

  private void scaleImages(int width, int height){
    if (boardImage == null){
      if ((scaledLight != null) && (scaledLight.getWidth(null) == width/8) && (scaledLight.getHeight(null) == height/8))
        return;

      if (isScaled){
        scaledLight = lightImage.getScaledInstance(width/8, height/8, Image.SCALE_SMOOTH);
        scaledDark = darkImage.getScaledInstance(width/8, height/8, Image.SCALE_SMOOTH);
      }
    }
    else{
      if ((scaled != null) && (scaled.getWidth(null) == width) && (scaled.getHeight(null) == height))
        return;

      scaled = boardImage.getScaledInstance(width, height, Image.SCALE_SMOOTH);
    } 
  }
  
  
  
  /**
   * Paints the board at the given location on the given Graphics scaled to
   * the given size.
   */

  public void paintBoard(Graphics g, Component component, int x, int y, int width, int height){
    // Preload if necessary
    preloadImages();
    
    scaleImages(width, height);

    if (boardImage == null){
      Rectangle clipRect = g.getClipBounds();
      Rectangle drawnRect = new Rectangle(x, y, width/8, height/8);
      int lwidth = lightImage.getWidth(null);
      int lheight = lightImage.getHeight(null);
      int dwidth = darkImage.getWidth(null);
      int dheight = darkImage.getHeight(null);
      for (int file = 0; file < 8; file++, drawnRect.x += width/8){
        drawnRect.y = y;
        for (int rank = 7; rank >= 0; rank--, drawnRect.y += height/8){
          if (!drawnRect.intersects(clipRect))
            continue;
          
          Image image;
          int iwidth, iheight;
          if ((file+rank) % 2 == 0){
            image = isScaled ? scaledDark : darkImage;
            iwidth = dwidth;
            iheight = dheight;
          }
          else{
            image = isScaled ? scaledLight : lightImage;
            iwidth = lwidth;
            iheight = lheight;
          }
         
          if (isScaled)
            g.drawImage(image, drawnRect.x, drawnRect.y, component);
          else{
            g.setClip(clipRect.intersection(drawnRect));
            int imgX = (file*iwidth) % drawnRect.width;
            int imgY = ((7-rank)*iheight) % drawnRect.height;
            for (int offx = drawnRect.x; offx - imgX < drawnRect.x+drawnRect.width; offx += iwidth)
              for (int offy = drawnRect.y; offy - imgY < drawnRect.y+drawnRect.height; offy += iheight)
                g.drawImage(image, offx - imgX, offy - imgY, component);
          }
        }
      }

      g.setClip(clipRect);
    }
    else{
      g.drawImage(scaled, x, y, component);
    }
  } 

  

}
