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
import java.io.IOException;
import java.util.Properties;
import java.util.Hashtable;
import free.util.IOUtilities;
import free.util.ImageUtilities;
import free.util.TextUtilities;


/**
 * An extension of ImagePiecePainter which loads the images by using the
 * Class.getResource() method, allowing you to package piece sets
 * along with your application's jar file.
 */

public class ResourceImagePiecePainter extends ImagePiecePainter{



  /**
   * A private constructor.
   */

  private ResourceImagePiecePainter(Hashtable pieces){
    super(pieces);
  }



  /**
   * <P>Creates a new <code>ResourceImagePiecePainter</code> with images loaded
   * by the given class' class loader from the given relative path (relative to
   * the package name of that class). The <code>ResourceImagePiecePainter</code>
   * will try to read a properties file named "definition" from the relative
   * path. That file should contain the following three properties:
   * <ul>
   *   <li><code>image.type</code>: Specifies the extension (type) of the
   *       images - gif, png etc. If this is not specified, "gif" is assumed.
   *   <li><code>size.pref</code>: Specifies the preferred size of the piece
   *       set, in pixels.
   *   <li><code>size.list</code>: A list of sizes of all the available piece
   *       images, separated by spaces.
   * </ul> 
   * The painter will then look for directories with names corresponding to the
   * sizes and in those directories for resources named
   * "<color char><piece char>.<extension>" where <color char> is either 'w' or
   * 'b' (for black or white), <piece char> is one of 'k', 'q', 'r', 'b', 'n'
   * or 'p'. The images for all twelve pieces must be present there and they
   * must have the correct size.
   * <P>For example,
   * <code>new ResourceImagePiecePainter(com.mycompany.Myclass.class, "marble")</code>
   * will create a ResourceImagePiecePainter which will load "definition" from
   * com/mycompany/marble/definition and will then proceed loading images from
   * (for example) com/mycompany/marble/32/wb.gif,
   * com/mycompany/marble/32/br.gif, com/mycompany/marble/64/wq.gif etc.
   */

  public static ImagePiecePainter getInstance(Class c, String relPath) throws IOException{
    return getInstance(c.getClassLoader(), TextUtilities.translateResource(c, relPath));        
  }
  
  

  /**
   * <P>Creates a new ResourceImagePiecePainter which loads piece images using
   * the specified ClassLoader's getResourceAsStream method. The layout and
   * names of the loaded files are specified in the documentation of the
   * <code>getInstance(Class, String)</code> method. The root of the layout tree
   * is the specified <code>path</code> string.
   */
  
  public static ImagePiecePainter getInstance(ClassLoader cl, String path) throws IOException{
    if (!(path.endsWith("/") || "".equals(path)))
      path = path + "/";
    String defFile = path + "definition";
    
    Properties def = IOUtilities.loadProperties(cl.getResourceAsStream(defFile));
    if (def == null)
      throw new IOException("Unable to load " + defFile + " with classloader: " + cl);
    
    String ext = def.getProperty("ext", "gif");
    int [] sizes = TextUtilities.parseIntList(def.getProperty("size.list"), " ");
    
    Toolkit toolkit = Toolkit.getDefaultToolkit();
    Hashtable pieceImages = new Hashtable(sizes.length*5/4);
    Image [] images = new Image[12*sizes.length];
    int imagesCount = 0;
    for (int i = 0; i < sizes.length; i++){
      Integer size = new Integer(sizes[i]);
      
      Hashtable pieces = new Hashtable(15);

      String dirName = path + size + "/";

      pieces.put(ChessPiece.WHITE_KING, images[imagesCount++] =
        toolkit.getImage(cl.getResource(dirName + "wk." + ext)));
      pieces.put(ChessPiece.BLACK_KING, images[imagesCount++] = 
        toolkit.getImage(cl.getResource(dirName + "bk." + ext)));
      pieces.put(ChessPiece.WHITE_QUEEN, images[imagesCount++] =
        toolkit.getImage(cl.getResource(dirName + "wq." + ext)));
      pieces.put(ChessPiece.BLACK_QUEEN, images[imagesCount++] = 
        toolkit.getImage(cl.getResource(dirName + "bq." + ext)));
      pieces.put(ChessPiece.WHITE_ROOK, images[imagesCount++] =
        toolkit.getImage(cl.getResource(dirName + "wr." + ext)));
      pieces.put(ChessPiece.BLACK_ROOK, images[imagesCount++] =
        toolkit.getImage(cl.getResource(dirName + "br." + ext)));
      pieces.put(ChessPiece.WHITE_BISHOP, images[imagesCount++] = 
        toolkit.getImage(cl.getResource(dirName + "wb." + ext)));
      pieces.put(ChessPiece.BLACK_BISHOP, images[imagesCount++] = 
        toolkit.getImage(cl.getResource(dirName + "bb." + ext)));
      pieces.put(ChessPiece.WHITE_KNIGHT, images[imagesCount++] = 
        toolkit.getImage(cl.getResource(dirName + "wn." + ext)));
      pieces.put(ChessPiece.BLACK_KNIGHT, images[imagesCount++] =
        toolkit.getImage(cl.getResource(dirName + "bn." + ext)));
      pieces.put(ChessPiece.WHITE_PAWN, images[imagesCount++] =
        toolkit.getImage(cl.getResource(dirName + "wp." + ext)));
      pieces.put(ChessPiece.BLACK_PAWN, images[imagesCount++] =
        toolkit.getImage(cl.getResource(dirName + "bp." + ext)));

      pieceImages.put(size, pieces);
    }

    return new ResourceImagePiecePainter(pieceImages);
  }


  
}
