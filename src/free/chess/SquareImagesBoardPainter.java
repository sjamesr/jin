/**
 * The chess framework library.
 * More information is available at http://www.jinchess.com/.
 * Copyright (C) 2006 Alexander Maryanovsky.
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
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Toolkit;
import java.io.IOException;
import java.net.URL;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Properties;
import java.util.Set;

import free.util.IOUtilities;
import free.util.ImageUtilities;



/**
 * A <code>BoardPainter</code> which paints the board with two images - one for
 * light squares an one for dark squares.
 */

public class SquareImagesBoardPainter implements ResourceBoardPainter{
  
  
  
  /**
   * The constant for specifying image scaling mode.
   * In this mode, the square images are simply scaled to fit the square.
   * This means that all squares will look the same. For this option, you should
   * provide relatively small images, around 60x60.
   */
  
  public static final int SCALE_MODE = 0;
  
  
  
  /**
   * The constant for specifying image slicing mode.
   * If the square images are larger than the squares, each square will
   * be drawn using a contain a different, unscaled portion of the image.
   * If the square images are smaller than the squares, they will be tiled
   * inside each square.
   * For this option you should provide either large images - 200x200 to 300x300,
   * or small, tileable images.
   */
  
  public static final int SLICE_MODE = 1;
  
  
  
  /**
   * A delegate board painter we use while the board image is being loaded.
   */
  
  private static final BoardPainter whileLoadingDelegate = new DefaultBoardPainter();
  
  
  
  /**
   * True if the board image is to be loaded asynchronously, and in the meanwhile,
   * the delegate should be used.
   */
  
  private static volatile boolean asyncImageLoad = false;
  
  
  
  /**
   * The light square image. When <code>SquareImagesBoardPainter</code> is used
   * as a <code>ResourceBoardPainter</code>, this remains <code>null</code>
   * until the image data is fully loaded.
   */
  
  private Image lightImage = null;
  
  
  
  /**
   * The dark square image. When <code>SquareImagesBoardPainter</code> is used
   * as a <code>ResourceBoardPainter</code>, this remains <code>null</code>
   * until the image data is fully loaded.
   */
  
  private Image darkImage = null;
  
  
  
  /**
   * The mode of image usage, either {@link #SCALE_MODE} or {@link #SLICE_MODE}. 
   */
  
  private int mode;
  
  
  
  /**
   * A version of the light square image, scaled according to the last paint
   * request.
   */
  
  private Image scaledLightImage = null;
  
  
  
  /**
   * A version of the dark square image, scaled according to the last paint
   * request.
   */
  
  private Image scaledDarkImage = null;
  
  
  
  /**
   * When <code>SquareImagesBoardPainter</code> is used as a
   * <code>ResourceBoardPainter</code>, this specified the URL of the light
   * square image. Otherwise, it remains null.
   */
  
  private URL lightImageUrl = null;
  
  
  
  /**
   * When <code>SquareImagesBoardPainter</code> is used as a
   * <code>ResourceBoardPainter</code>, this specified the URL of the dark
   * square image. Otherwise, it remains null.
   */
  
  private URL darkImageUrl = null;
  
  
  
  /**
   * The <code>ImageDataReceiver</code>, if any, currently waiting on board
   * image data to load.
   */
  
  private ImageDataReceiver imageDataReceiver = null;
  
  
  
  /**
   * A no-arg constructor so that this <code>SquareImagesBoardPainter</code> can
   * be used as a <code>ResourceBoardPainter</code>.
   */
   
  public SquareImagesBoardPainter(){
     
  }



  /**
   * <p>Creates a new <code>SquareImagesBoardPainter</code> which paints light
   * squares with <code>lightImage</code> and dark squares with
   * <code>darkImage</code>. <code>mode</code> is either {@link #SCALE_MODE} or
   * {@link #SLICE_MODE}.
   */

  public SquareImagesBoardPainter(Image lightImage, Image darkImage, int mode){
    switch (mode){
      case SCALE_MODE:
      case SLICE_MODE:
        break;
      default:
        throw new IllegalArgumentException("Unknown image usage mode: " + mode);
    }
    
    this.lightImage = lightImage;
    this.darkImage = darkImage;
    this.mode = mode;
  }
  
  
  
  /**
   * Sets whether the square images are to be loaded asynchronously and a simpler
   * board painter delegate used while they load. Asynchronous loading is off
   * by default.
   */
  
  public static void setAsyncImageLoad(boolean asyncImageLoad){
    SquareImagesBoardPainter.asyncImageLoad = asyncImageLoad;
  }
  
  
  
  /**
   * Since <code>SquareImagesBoardPainter</code>s are immutable, simply returns
   * <code>this</code>.
   */
  
  public BoardPainter freshInstance(){
    return this;
  }
  
  
  
  /**
   * Loads the painter from the specified URL. The file structure at the URL is
   * described below.
   * <p>A properties file named "definition" must be located at the URL.
   * That file should contain the following properties:
   * <ul>
   *   <li><code>ext</code>: Specifies the extension (type) of the
   *       image(s) - gif, png, etc. If this is omitted, "gif" is assumed.
   *   <li><code>imageUseMode</code>: Specifies the way the images are used.
   *       Possible values are "scale" and "slice". If omitted, defaults to
   *       "slice".
   * </ul>
   * Two images named <code>light.[ext]</code> and <code>dark.[ext]</code> must
   * be located at the URL, where "ext" is the value of the <code>ext</code>
   * property. 
   */
  
  public void load(URL url) throws IOException{
    if (lightImageUrl != null)
      throw new IllegalStateException("This SquareImagesBoardPainter has already been loaded");
    
    URL defURL = new URL(url, "definition");

    Properties def = IOUtilities.loadProperties(defURL, true);
    if (def == null)
      def = new Properties();

    if (def.getProperty("scaleSquares") != null) // Backward compatibility
      mode = new Boolean(def.getProperty("scaleSquares", "false")).booleanValue() ? SCALE_MODE : SLICE_MODE;
    else{
      String modeString = def.getProperty("imageUseMode", "slice");
      mode = "slice".equals(modeString) ? SLICE_MODE : SCALE_MODE;
    }

    String ext = def.getProperty("ext", "gif");
    lightImageUrl = new URL(url, "light." + ext);
    darkImageUrl = new URL(url, "dark." + ext);
  }
  
  
  
  /**
   * If the square images are already loaded, prepares their scaled version
   * (if needed) for the specified board size and returns <code>true</code>.
   * Otherwise, starts loading them. If asynchronous loading is enabled, the
   * loading is done in a background thread, otherwise, waits until the loading
   * is done.
   * Returns whether the images are ready. 
   */
  
  protected synchronized boolean prepareSquareImages(int width, int height, Component target){
    if (lightImage == null){
      if (imageDataReceiver != null){ // Already being loaded
        imageDataReceiver.addComponentToRepaint(target);
        return false;
      }
      
      if (asyncImageLoad){
        imageDataReceiver = new ImageDataReceiver(target);
        IOUtilities.loadAsynchronously(new URL[]{lightImageUrl, darkImageUrl}, null, imageDataReceiver, true);
        return false;
      }
      else{
        imageDataReceiver = new ImageDataReceiver(null);
        IOUtilities.loadSynchronously(new URL[]{lightImageUrl, darkImageUrl}, null, imageDataReceiver, true);
        if ((lightImage == null) || (darkImage == null))
          return false;
        
        scaleSquareImages(width, height);
        return true;
      }
    }
    else{
      scaleSquareImages(width, height);
      return true;
    }
  }
  
  
  
  /**
   * Scales the square image to the specified board size.
   */

  private void scaleSquareImages(int width, int height){
    if (mode == SCALE_MODE){
      int squareWidth = width/8;
      int squareHeight = height/8;
      if ((scaledLightImage != null) &&
          (scaledLightImage.getWidth(null) == squareWidth) &&
          (scaledLightImage.getHeight(null) == squareHeight))
        return;
      
      scaledLightImage = lightImage.getScaledInstance(squareWidth, squareHeight, Image.SCALE_SMOOTH);
      scaledDarkImage = darkImage.getScaledInstance(squareWidth, squareHeight, Image.SCALE_SMOOTH);
      
      ImageUtilities.preload(scaledLightImage);
      ImageUtilities.preload(scaledDarkImage);
    }
    else if (mode == SLICE_MODE){
      
    }
  }
  
  
  
  /**
   * Paints the board at the given location on the given Graphics scaled to
   * the given size.
   */

  public void paintBoard(Graphics g, Component component, int x, int y, int width, int height){
    if (prepareSquareImages(width, height, component)){
      Rectangle clipRect = g.getClipBounds();
      Rectangle drawnRect = new Rectangle(x, y, width/8, height/8);
      
      if (mode == SCALE_MODE){
        for (int file = 0; file < 8; file++, drawnRect.x += width/8){
          drawnRect.y = y;
          for (int rank = 7; rank >= 0; rank--, drawnRect.y += height/8){
            if (!drawnRect.intersects(clipRect))
              continue;
            
            Image image = (file+rank) % 2 == 0 ? scaledDarkImage : scaledLightImage; 
            g.drawImage(image, drawnRect.x, drawnRect.y, component);
          }
        }
      }
      else if (mode == SLICE_MODE){
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
              image = darkImage;
              iwidth = dwidth;
              iheight = dheight;
            }
            else{
              image = lightImage;
              iwidth = lwidth;
              iheight = lheight;
            }
           
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
    else
      whileLoadingDelegate.paintBoard(g, component, x, y, width, height);
  }
  
  
  
  
  /**
   * The receiver of square image data. Responsible for creating the square images. 
   */
  
  private class ImageDataReceiver implements IOUtilities.DataReceiver{
    
    
    
    /**
     * The set of components to repaint once the images are loaded. 
     */
    
    private final Set componentsToRepaint = new HashSet(2);
    
    
    
    /**
     * Creates a new <code>ImageDataReceiver</code> with the specified component
     * to repaint once the image data is loaded.
     */
    
    public ImageDataReceiver(Component component){
      componentsToRepaint.add(component);
    }
    
    
    
    /**
     * Adds the specified component to the set of components to repaint once
     * loading the image is done.
     */
    
    public void addComponentToRepaint(Component component){
      componentsToRepaint.add(component);
    }
    
    
    
    /**
     * Called when the image data has been loaded. Creates the square images.
     */
    
    public void dataRead(URL [] urls, Object id, byte [][] data, IOException [] exceptions){
      // If there are any exceptions, we simply quit - this will cause
      // the painter to keep using the delegate to paint the board.
      for (int i = 0; i < exceptions.length; i++)
        if (exceptions[i] != null)
          return;
      
      lightImage = Toolkit.getDefaultToolkit().createImage(data[0]);
      darkImage = Toolkit.getDefaultToolkit().createImage(data[1]);
      
      ImageUtilities.preload(lightImage);
      ImageUtilities.preload(darkImage);
      
      for (Iterator i = componentsToRepaint.iterator(); i.hasNext();){
        Component component = (Component)i.next();
        if (component != null)
          component.repaint();
      }      
    }
    
    
    
  }
  
  
  
}
