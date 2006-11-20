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
 * A <code>BoardPainter</code> which paints the entire board using a single,
 * scaled image.
 */

public class BoardImageBoardPainter implements ResourceBoardPainter{
  
  
  
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
   * The board image. When <code>BoardImageBoardPainter</code> is used as a
   * <code>ResourceBoardPainter</code>, this remains <code>null</code> until
   * the image data is fully loaded.
   */
  
  private Image boardImage = null;
  
  
  
  /**
   * The board image, scaled according to the last request to paint a board.
   */
  
  private Image scaledBoardImage = null;
  
  
  
  /**
   * When <code>BoardImageBoardPainter</code> is used as a
   * <code>ResourceBoardPainter</code>, this specified the URL of the board's
   * image. Otherwise, it remains null.
   */
  
  private URL boardImageUrl = null;
  
  
  
  /**
   * The <code>ImageDataReceiver</code>, if any, currently waiting on board
   * image data to load.
   */
  
  private ImageDataReceiver imageDataReceiver = null;
  
  
  
  /**
   * A no-arg constructor so that this <code>BoardImageBoardPainter</code> can
   * be used as a <code>ResourceBoardPainter</code>.
   */
   
  public BoardImageBoardPainter(){
     
  }



  /**
   * Creates a new <code>BoardImageBoardPainter</code> which paints the board
   * with the given image.
   */

  public BoardImageBoardPainter(Image boardImage){
    this.boardImage = boardImage;
  }
  
  
  
  /**
   * Sets whether the board image is to be loaded asynchronously and a simpler
   * board painter delegate used while it loads. Asynchronous loading is off
   * by default.
   */
  
  public static void setAsyncImageLoad(boolean asyncImageLoad){
    BoardImageBoardPainter.asyncImageLoad = asyncImageLoad;
  }
  
  
  
  /**
   * Since <code>BoardImageBoardPainter</code>s are immutable, simply returns
   * <code>this</code>.
   */
  
  public BoardPainter freshInstance(){
    return this;
  }
  
  
  
  /**
   * Loads the painter from the specified URL. The file structure at the URL is
   * described below.
   * <p>A properties file named "definition" must be located at the URL.
   * That file should contain the following property:
   * <ul>
   *   <li><code>ext</code>: Specifies the extension (type) of the
   *       image(s) - gif, png, etc. If this is omitted, "gif" is assumed.
   * </ul>
   * An image file named <code>board.ext</code> must be located at the URL,
   * where "ext" is the value of the <code>ext</code> property. 
   */
  
  public void load(URL url) throws IOException{
    if (boardImageUrl != null)
      throw new IllegalStateException("This BoardImageBoardPainter has already been loaded");
    
    URL defURL = new URL(url, "definition");

    Properties def = IOUtilities.loadProperties(defURL, true);
    if (def == null)
      def = new Properties();

    String ext = def.getProperty("ext", "gif");
    
    boardImageUrl = new URL(url, "board." + ext);
  }
  
  
  
  /**
   * If the board image is already loaded, prepares the scaled version for the
   * specified board size and returns <code>true</code>. Otherwise, starts
   * loading it. If asynchronous loading is enabled, the loading is done in a
   * background thread, otherwise, waits until the loading is done.
   * Returns whether the image is ready. 
   */
  
  protected synchronized boolean prepareBoardImage(int width, int height, Component target){
    if (boardImage == null){
      if (imageDataReceiver != null){ // Already being loaded
        imageDataReceiver.addComponentToRepaint(target);
        return false;
      }
      
      if (asyncImageLoad){
        imageDataReceiver = new ImageDataReceiver(target);
        IOUtilities.loadAsynchronously(new URL[]{boardImageUrl}, null, imageDataReceiver, true);
        return false;
      }
      else{
        imageDataReceiver = new ImageDataReceiver(null);
        IOUtilities.loadSynchronously(new URL[]{boardImageUrl}, null, imageDataReceiver, true);
        if (boardImage == null)
          return false;
        
        scaleBoardImage(width, height);
        return true;
      }
    }
    else{
      scaleBoardImage(width, height);
      return true;
    }
  }
  
  
  
  /**
   * Scales the board image to the specified size.
   */

  private void scaleBoardImage(int width, int height){
    if ((scaledBoardImage != null) &&
        (scaledBoardImage.getWidth(null) == width) &&
        (scaledBoardImage.getHeight(null) == height))
      return;
    
    scaledBoardImage = boardImage.getScaledInstance(width, height, Image.SCALE_SMOOTH);
    ImageUtilities.preload(scaledBoardImage);
  }
  
  
  
  /**
   * Paints the board at the given location on the given Graphics scaled to
   * the given size.
   */

  public void paintBoard(Graphics g, Component component, int x, int y, int width, int height){
    if (prepareBoardImage(width, height, component))
      g.drawImage(scaledBoardImage, x, y, component);
    else
      whileLoadingDelegate.paintBoard(g, component, x, y, width, height);
  }
  
  
  
  
  /**
   * The receiver of board image data. Responsible for creating the board image. 
   */
  
  private class ImageDataReceiver implements IOUtilities.DataReceiver{
    
    
    
    /**
     * The set of components to repaint once the image is loaded. 
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
     * Called when the image data has been loaded. Creates the board image.
     */
    
    public void dataRead(URL [] urls, Object id, byte [][] data, IOException [] exceptions){
      // If there are any exceptions, we simply quit - this will cause
      // the painter to keep using the delegate to paint the board.
      for (int i = 0; i < exceptions.length; i++)
        if (exceptions[i] != null)
          return;
      
      synchronized(BoardImageBoardPainter.this){
        boardImage = Toolkit.getDefaultToolkit().createImage(data[0]);
        ImageUtilities.preload(boardImage);
        
        imageDataReceiver = null;
        
        for (Iterator i = componentsToRepaint.iterator(); i.hasNext();){
          Component component = (Component)i.next();
          if (component != null)
            component.repaint();
        }
      }
    }
    
    
    
  }
  
  
  
}
