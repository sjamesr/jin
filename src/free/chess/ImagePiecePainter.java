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
import java.awt.image.*;
import java.util.Hashtable;
import java.util.Properties;
import java.util.Enumeration;
import java.net.URL;
import java.net.MalformedURLException;
import java.io.IOException;
import free.util.ImageUtilities;
import free.util.TextUtilities;
import free.util.IOUtilities;


/**
 * An implementation of PiecePainter which paints images.
 */

public class ImagePiecePainter implements ResourcePiecePainter{


  
  /**
   * The ImageFilter we use to create shaded images.
   */

  private static final ImageFilter SHADING_FILTER = new ShadingFilter();



  /**
   * An array whose indices specify the size of the images and whose values
   * are Hashtables mapping Pieces to Images.
   */

  private Hashtable [] pieceImages;



  /**
   * Same as pieceImages only for shaded images.
   */

  private Hashtable [] shadedPieceImages;


  
  /**
   * A no-arg constructor, so that this piece painter can be used as a
   * <code>ResourcePiecePainter</code>.
   */
   
  public ImagePiecePainter(){
     
  }
  
  

  /**
   * Creates a new ImagePiecePainter with the specified piece images.
   * The given Hashtable should map Integer objects specifying the size of the
   * piece images to Hashtables which in turn map Piece objects to piece Images.
   */

  public ImagePiecePainter(Hashtable pieceImages){

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

    this.pieceImages = new Hashtable[maxSize + 1];
    this.shadedPieceImages = new Hashtable[maxSize + 1];

    // Fill the array
    sizes = pieceImages.keys();
    while (sizes.hasMoreElements()){
      Integer size = (Integer)sizes.nextElement();
      int sizeInt = size.intValue();

      Hashtable images = (Hashtable)pieceImages.get(size);
      int imagesCount = images.size();
      this.pieceImages[sizeInt] = new Hashtable(imagesCount);
      this.shadedPieceImages[sizeInt] = new Hashtable(imagesCount);

      Enumeration pieces = images.keys();
      while (pieces.hasMoreElements()){
        Object key = pieces.nextElement();
        Image image = (Image)images.get(key);
        Image shadedImage = shadeImage(image);

        this.pieceImages[sizeInt].put(key, image);
        this.shadedPieceImages[sizeInt].put(key, shadedImage);
      }
    }
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
   
  public void load(URL url) throws IOException{
    URL defURL = new URL(url, "definition");
    Properties def = IOUtilities.loadProperties(defURL);
    if (def == null)
      throw new IOException("Unable to load " + defURL);
    
    String ext = def.getProperty("ext", "gif");
    int [] sizes = TextUtilities.parseIntList(def.getProperty("size.list"), " ");
    
    this.pieceImages = new Hashtable[sizes[sizes.length - 1] + 1];
    this.shadedPieceImages = new Hashtable[sizes[sizes.length - 1] + 1];
    
    Piece [] pieces = new Piece[]{ChessPiece.WHITE_KING, ChessPiece.BLACK_KING,
      ChessPiece.WHITE_QUEEN, ChessPiece.BLACK_QUEEN, ChessPiece.WHITE_ROOK,
      ChessPiece.BLACK_ROOK, ChessPiece.WHITE_BISHOP, ChessPiece.BLACK_BISHOP,
      ChessPiece.WHITE_KNIGHT, ChessPiece.BLACK_KNIGHT, ChessPiece.WHITE_PAWN,
      ChessPiece.BLACK_PAWN};
      
    String [] pieceNames = 
      new String[]{"wk", "bk", "wq", "bq", "wr", "br", "wb", "bb", "wn", "bn", "wp", "bp"};
    
    Toolkit toolkit = Toolkit.getDefaultToolkit();
    Hashtable pieceImages = new Hashtable(sizes.length*5/4);
    int imagesCount = 0;
    for (int i = 0; i < sizes.length; i++){
      int size = sizes[i];
      
      Hashtable normal = new Hashtable(15);
      Hashtable shaded = new Hashtable(15);

      for (int j = 0; j < pieces.length; j++)
        addImage(toolkit, url, pieces[j], size, pieceNames[j], ext, normal, shaded); 

      this.pieceImages[size] = normal;
      this.shadedPieceImages[size] = shaded;
    }
  }
  
  
  
  /**
   * Loads and maps the specified image and its shaded version in the specified
   * hashtables.
   */
   
   private void addImage(Toolkit toolkit, URL url, Piece piece, int size,
      String name, String ext, Hashtable normal, Hashtable shaded) throws MalformedURLException{
        
    Image normalImage = toolkit.getImage(new URL(url, size + "/" + name + "." + ext));
    Image shadedImage = shadeImage(normalImage);
    normal.put(piece, normalImage);
    shaded.put(piece, shadedImage);
   }




  /**
   * Returns a shaded version of the specified image.
   */

  protected Image shadeImage(Image image){
    Image shadedImage = Toolkit.getDefaultToolkit().createImage(
      new FilteredImageSource(image.getSource(), SHADING_FILTER));
    return shadedImage;
  }




  /**
   * Returns the Image by which the given Piece is represented at the given
   * size.
   */

  protected Image getPieceImage(int size, Piece piece, boolean shaded){
    Hashtable [] images = shaded ? shadedPieceImages : pieceImages;

    if (size <= 0)
      throw new IllegalArgumentException("Image size must be positive");

    if (size >= images.length)
      return (Image)(images[images.length-1].get(piece));

    if (images[size] != null)
      return (Image)(images[size].get(piece));

    for (int i = size; i > 0; i--)
      if (images[i] != null)
        return (Image)(images[i].get(piece));

    for (int i = size+1; i < images.length; i++)
      if (images[i] != null)
        return (Image)(images[i].get(piece));

    throw new Error("This can't happen");
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
    Image pieceImage = getPieceImage(size, piece, shaded);
    
    ImageUtilities.preload(pieceImage);
    
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


  
}
