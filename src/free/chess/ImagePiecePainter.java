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
import java.awt.image.FilteredImageSource;
import java.awt.image.ImageFilter;
import java.awt.image.RGBImageFilter;
import java.io.IOException;
import java.net.URL;
import java.util.*;

import free.util.IOUtilities;
import free.util.ImageUtilities;
import free.util.TextUtilities;


/**
 * An implementation of <code>PiecePainter</code> which paints images.
 */

public final class ImagePiecePainter implements ResourcePiecePainter{


  
  /**
   * The painter we delegate to while loading images.
   */
  
  private static final PiecePainter whileLoadingDelegate = new DefaultPiecePainter();
  
  
  
  /**
   * The ImageFilter we use to create shaded images.
   */

  private static final ImageFilter SHADING_FILTER = new ShadingFilter();



  /**
   * True if piece images are to be loaded asynchronously, and in the meanwhile,
   * the delegate should be used.
   */
  
  private static volatile boolean asyncImageLoad = false;
  
  
  
  /**
   * An array whose indices specify the size of the images and whose values
   * are maps mapping <code>Piece</code>s to either <code>Image</code>s or
   * <code>URL</code>s, if the image isn't loaded yet.
   */

  private Map [] pieceImages;



  /**
   * Same as pieceImages only for shaded images.
   */

  private Map [] shadedPieceImages;
  
  
  
  /**
   * Maps the square sizes for which images are currently being loaded to the
   * <code>ImageDataReceiver</code>s waiting on the data.
   */
  
  private final Map imageDataReceivers = new HashMap(2);


  
  /**
   * A no-arg constructor, so that this piece painter can be used as a
   * <code>ResourcePiecePainter</code>.
   */
   
  public ImagePiecePainter(){
     
  }
  
  

  /**
   * Creates a new <code>ImagePiecePainter</code> with the specified piece
   * images. The given <code>Map</code> should map <code>Integer</code>s
   * specifying the size of the piece images to <code>Maps</code>s which in
   * turn map <code>Piece</code>s to piece <code>Image</code>s.
   */

  public ImagePiecePainter(Map pieceImages){

    // Find the largest size
    int maxSize = 0;
    Iterator sizes = pieceImages.keySet().iterator();
    while (sizes.hasNext()){
      int size = ((Integer)sizes.next()).intValue();
      if (size <= 0)
        throw new IllegalArgumentException("Image sizes must be positive");

      if (size > maxSize)
        maxSize = size;
    }

    if (maxSize == 0)
      throw new IllegalArgumentException("No sizes in the hashtable");

    this.pieceImages = new Map[maxSize + 1];
    this.shadedPieceImages = new Map[maxSize + 1];

    // Fill the array
    sizes = pieceImages.keySet().iterator();
    while (sizes.hasNext()){
      Integer size = (Integer)sizes.next();
      int sizeInt = size.intValue();

      Map images = (Map)pieceImages.get(size);
      int imagesCount = images.size();
      this.pieceImages[sizeInt] = new HashMap(imagesCount);
      this.shadedPieceImages[sizeInt] = new HashMap(imagesCount);

      Iterator pieces = images.keySet().iterator();
      while (pieces.hasNext()){
        Object key = pieces.next();
        Image image = (Image)images.get(key);
        Image shadedImage = shadeImage(image);

        this.pieceImages[sizeInt].put(key, image);
        this.shadedPieceImages[sizeInt].put(key, shadedImage);
      }
    }
  }
  
  
  
  /**
   * Sets whether piece images are to be loaded asynchronously and a simpler
   * piece painter delegate used while they load. Asynchronous loading is off
   * by default.
   */
  
  public static void setAsyncImageLoad(boolean asyncImageLoad){
    ImagePiecePainter.asyncImageLoad = asyncImageLoad;
  }
  
  
  
  /**
   * Creates a shaded version of the specified image.
   */
  
  private static Image shadeImage(Image image){
    return Toolkit.getDefaultToolkit().createImage(new FilteredImageSource(image.getSource(), SHADING_FILTER));
  }
  
  
  
  /**
   * Since <code>ImagePiecePainter</code>s are immutable, simply returns
   * <code>this</code>.
   */
  
  public PiecePainter freshInstance(){
    return this;
  }
  
  
  
  /**
   * Loads the piece images from the specified URL. The structure at the
   * specified url is described below.
   * A properties file named "definition" must be located at the base URL.
   * That file should contain the following three properties:
   * <ul>
   *   <li><code>image.type</code>: Specifies the extension (type) of the
   *       images - gif, png etc. If this is not specified, "gif" is assumed.
   *   <li><code>size.pref</code>: Specifies the preferred size of the piece
   *       set, in pixels.
   *   <li><code>size.list</code>: A list of sizes of all the available piece
   *       images, in increasing order, separated by spaces.
   * </ul> 
   * Directories with names corresponding to the sizes must be present at the
   * base URL and in those directories resources named
   * <code>[color char][piece char].[extension]</code> where
   * <code>[color char]</code> is either 'w' or 'b' (for black or white),
   * <code>[piece char]</code> is one of 'k', 'q', 'r', 'b', 'n'
   * or 'p'. The images for all twelve pieces must be present there and they
   * must have the correct size.
   */
   
  public void load(URL baseUrl) throws IOException{
    if (pieceImages != null)
      throw new IllegalStateException("This ImagePiecePainter has already been loaded");
    
    URL defURL = new URL(baseUrl, "definition");
    Properties def = IOUtilities.loadProperties(defURL, true);
    if (def == null)
      throw new IOException("Unable to load " + defURL);
    
    String ext = def.getProperty("ext", "gif");
    int [] sizes = TextUtilities.parseIntList(def.getProperty("size.list"), " ");
    
    this.pieceImages = new HashMap[sizes[sizes.length - 1] + 1];
    this.shadedPieceImages = new HashMap[sizes[sizes.length - 1] + 1];
    
    Piece [] pieces = new Piece[]{
        ChessPiece.WHITE_KING, ChessPiece.BLACK_KING,
        ChessPiece.WHITE_QUEEN, ChessPiece.BLACK_QUEEN,
        ChessPiece.WHITE_ROOK, ChessPiece.BLACK_ROOK,
        ChessPiece.WHITE_BISHOP, ChessPiece.BLACK_BISHOP,
        ChessPiece.WHITE_KNIGHT, ChessPiece.BLACK_KNIGHT,
        ChessPiece.WHITE_PAWN, ChessPiece.BLACK_PAWN};
      
    String [] pieceNames = 
      new String[]{"wk", "bk", "wq", "bq", "wr", "br", "wb", "bb", "wn", "bn", "wp", "bp"};
    
    for (int i = 0; i < sizes.length; i++){
      int size = sizes[i];
      
      Map normal = new HashMap(15);
      Map shaded = new HashMap(15);

      for (int j = 0; j < pieces.length; j++){
        URL imageUrl = new URL(baseUrl, size + "/" + pieceNames[j] + "." + ext);
        normal.put(pieces[j], imageUrl);
        shaded.put(pieces[j], imageUrl);
      }

      this.pieceImages[size] = normal;
      this.shadedPieceImages[size] = shaded;
    }
  }
  
  
  
  /**
   * Returns the size of provided images which is the best fit for the
   * specified square size.
   */
  
  protected int bestFitImageSize(int squareSize){
    if (squareSize <= 0)
      throw new IllegalArgumentException("Image size must be positive");

    if (squareSize >= pieceImages.length)
      return pieceImages.length - 1;

    if (pieceImages[squareSize] != null)
      return squareSize;

    for (int i = squareSize; i > 0; i--)
      if (pieceImages[i] != null)
        return i;

    for (int i = squareSize+1; i < pieceImages.length; i++)
      if (pieceImages[i] != null)
        return i;

    throw new Error("This can't happen");
  }
  
  
  
  /**
   * If already loaded, returns the piece images at the specified size.
   * Otherwise, starts loading them (if async loading is enabled, in a
   * background thread, in the meanwhile, returning <code>null</code>),
   * and once done, repaints the specified component.
   */
  
  protected synchronized Map loadPieces(int squareSize, boolean shaded, Component target){
    int imageSize = bestFitImageSize(squareSize);
    Map images = (shaded ? shadedPieceImages : pieceImages)[imageSize];
    if (images.values().iterator().next() instanceof Image) // Already loaded
      return images;
    else{
      ImageDataReceiver receiver = (ImageDataReceiver)imageDataReceivers.get(new Integer(imageSize));
      if (receiver != null){ // We're already loading the images
        receiver.addComponentToRepaint(target);
        return null;
      }
      
      Set entrySet = images.entrySet();
      Piece [] pieces = new Piece[entrySet.size()];
      URL [] urls = new URL[entrySet.size()];
      Iterator entries = entrySet.iterator();
      for (int i = 0; i < pieces.length; i++){
        Map.Entry entry = (Map.Entry)entries.next();
        pieces[i] = (Piece)entry.getKey();
        urls[i] = (URL)entry.getValue();
      }
      
      Map normalImages = pieceImages[imageSize];
      Map shadedImages = shadedPieceImages[imageSize];
      receiver = new ImageDataReceiver(asyncImageLoad ? target : null, imageSize, normalImages, shadedImages);
      imageDataReceivers.put(new Integer(imageSize), receiver);
      
      if (asyncImageLoad){
        IOUtilities.loadAsynchronously(urls, pieces, receiver, true);
        return null;
      }
      else{
        IOUtilities.loadSynchronously(urls, pieces, receiver, true);
        return shaded ? shadedImages : normalImages;
      }
    }
  }




  /**
   * Paints the image of the given piece at the given coordinates on the given
   * Graphics object scaled to the given size.
   */

  public void paintPiece(Piece piece, Graphics g, Component component, Rectangle rect,
      boolean shaded){

    int x = rect.x;
    int y = rect.y;
    int width = rect.width;
    int height = rect.height;

    int size = width > height ? height : width;
    
    Map pieces = loadPieces(size, shaded, component);
    if (pieces == null){
      whileLoadingDelegate.paintPiece(piece, g, component, rect, shaded);
      return;
    }
    
    Image pieceImage = (Image)pieces.get(piece);
    int pieceWidth = pieceImage.getWidth(null);
    int pieceHeight = pieceImage.getHeight(null);
    
    g.drawImage(pieceImage, x + (width - pieceWidth)/2, y + (height - pieceHeight)/2, component);
  }




  /**
   * The <code>ImageFilter</code> we use to create shaded piece images.
   */

  private static class ShadingFilter extends RGBImageFilter{

    public int filterRGB(int x, int y, int rgb){
      int alpha = (rgb >> 24) & 0xff;
      int r = (rgb >> 16) & 0xff;
      int g = (rgb >> 8) & 0xff;
      int b = rgb & 0xff;

      r = (r + 128*2)/3;
      g = (g + 128*2)/3;
      b = (b + 128*2)/3;

      return (alpha << 24) | (r << 16) | (g << 8) | b;
    }

  }
  
  
  
  /**
   * The receiver of loaded image data. Responsible for creating and correctly
   * mapping the piece images.
   */
  
  private class ImageDataReceiver implements IOUtilities.DataReceiver{
    
    
    
    /**
     * The map of pieces to normal images.
     */
    
    private final Map normalImages;
    
    
    
    /**
     * The map of pieces to shaded images.
     */
    
    private final Map shadedImages;
    
    
    
    /**
     * The size of the images we're loading. 
     */
    
    private final int imageSize;
    
    
    
    /**
     * The components to repaint when the loading is done.
     */
    
    private final Set componentsToRepaint = new HashSet(2);
    
    
    
    /**
     * Creates a new <code>ImageDataReceiver</code> with the specified maps to
     * fill and component to repaint when loading is done.
     */
    
    public ImageDataReceiver(Component componentToRepaint, int imageSize, Map normalImages, Map shadedImages){
      this.normalImages = normalImages;
      this.shadedImages = shadedImages;
      this.imageSize = imageSize;
      componentsToRepaint.add(componentToRepaint);
    }
    
    
    
    /**
     * Adds a component to the set of components to repaint once all the images
     * are loaded.
     */
    
    public void addComponentToRepaint(Component component){
      componentsToRepaint.add(component);
    }
    
    
    
    /**
     * Called when the image data has been loaded. Creates and maps the piece
     * images.
     */
    
    public void dataRead(URL [] urls, Object id, byte [][] data, IOException[] exceptions){
      // If there are any exceptions, we simply quit - this will cause
      // the painter to keep using the delegate to paint pieces.
      for (int i = 0; i < exceptions.length; i++)
        if (exceptions[i] != null)
          return;
      
      synchronized(ImagePiecePainter.this){
        Toolkit toolkit = Toolkit.getDefaultToolkit();
        Piece [] pieces = (Piece[])id;
        for (int i = 0; i < data.length; i++){
          Image normalImage = toolkit.createImage(data[i]);
          Image shadedImage = shadeImage(normalImage);
          
          ImageUtilities.preload(normalImage);
          ImageUtilities.preload(shadedImage);
          
          normalImages.put(pieces[i], normalImage);
          shadedImages.put(pieces[i], shadedImage);
        }
        
        imageDataReceivers.remove(new Integer(imageSize));
        
        for (Iterator i = componentsToRepaint.iterator(); i.hasNext();){
          Component component = (Component)i.next();
          if (component != null)
            component.repaint();
        }
      }
      
      
      
    }
    
    
    
    
  }
  
  
  
}
