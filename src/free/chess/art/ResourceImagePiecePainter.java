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

package free.chess.art;

import java.awt.*;
import java.io.IOException;
import java.io.InputStream;
import java.util.StringTokenizer;
import java.util.Hashtable;
import free.chess.ImagePiecePainter;
import free.chess.PiecePainter;
import free.chess.ChessPiece;
import free.util.IOUtilities;
import free.util.PairKey;


/**
 * An extension of ImagePiecePainter which loads the images by using the
 * Class.getResource() method, allowing you to package piece sets
 * along with your application's jar file.
 */

public class ResourceImagePiecePainter extends ImagePiecePainter{



  /**
   * A Hashtable mapping PairKeys of classes and PairKeys of relative pathnames
   * and extensions (new PairKey(myClass, new PairKey(relPath, extension))) to
   * arrays of length 2 (and type Object []) in which the first element is a
   * Dimension object specifying the preferred piece size and the second element
   * is a Hashtable of preloaded images.
   */

  private static final Hashtable cache = new Hashtable();





  /**
   * <P>Creates a new ResourceImagePiecePainter with images loaded by the given
   * class' class loader from the given relative path (relative to the package
   * name of that class) and of the given extension. The
   * ResourceImagePiecePainter will try to read a file named "sizes.txt" from
   * the relative path. That file should contain two lines. The first line
   * must contain a single integer specifying the preferred size. The 2nd line
   * should contain a list of sizes of all the available piece images, separated
   * by spaces. It will then look for directories with names corresponding to 
   * the sizes and in those directories for resources named
   * "<color char><piece char>.<extension>" where <color char> is either
   * 'w' or 'b' (for black or white), <piece char> is one of 'k', 'q', 'r', 'b'
   * 'n' or 'p'. The images for all twelve pieces must be present there and they
   * must have the correct size.
   * <P>For example, <code>new ResourceImagePiecePainter(com.mycompany.Myclass.class, "marble", "gif")</code>
   * will create a ResourceImagePiecePainter which will load "sizes.txt" from
   * com/mycompany/marble/sizes.txt and will then proceed loading images from
   * (for example) com/mycompany/marble/32/wb.gif, com/mycompany/marble/32/br.gif,
   * com/mycompany/marble/64/wq.gif etc.
   * <P>The implementation caches all the loaded data statically, so you needn't
   * be worried about creating more than one instance of this class with the
   * same data.
   */

  public ResourceImagePiecePainter(Class c, String relPath, String extension) throws IOException{
    super(getPrefSize(c, relPath, extension), getImages(c, relPath, extension));
  }




  /**
   * Returns the preferred size for the piece set and returns a Dimension object
   * specifying it.
   */

  private static synchronized Dimension getPrefSize(Class c, String relPath, String extension) throws IOException{
    PairKey pk = new PairKey(c, new PairKey(relPath, extension));
    Object [] cached = (Object [])cache.get(pk);
    if (cached != null)
      return (Dimension)cached[0];
    else{
      loadData(c, relPath, extension);
      cached = (Object [])cache.get(pk);
      return (Dimension)cached[0];
    }
  } 




  /**
   * Returns a Hashtable of the images.
   */

  private static synchronized Hashtable getImages(Class c, String relPath, String extension) throws IOException{
    PairKey pk = new PairKey(c, new PairKey(relPath, extension));
    Object [] cached = (Object [])cache.get(pk);
    if (cached != null)
      return (Hashtable)cached[1];
    else{
      loadData(c, relPath, extension);
      cached = (Object [])cache.get(pk);
      return (Hashtable)cached[1];
    }
  }





  /**
   * Loads and caches the actual data.
   */

  private static void loadData(Class c, String relPath, String extension) throws IOException{
    Toolkit toolkit = Toolkit.getDefaultToolkit();
    MediaTracker tracker = new MediaTracker(new Canvas());
    InputStream sizesIn = c.getResourceAsStream(relPath+"/sizes.txt");
    StringTokenizer lines = new StringTokenizer(IOUtilities.loadText(sizesIn), "\n\r");
    int sizeInt = Integer.parseInt(lines.nextToken());
    Dimension prefSize = new Dimension(sizeInt, sizeInt);
    StringTokenizer sizes = new StringTokenizer(lines.nextToken(), " ");
    Hashtable pieceImages = new Hashtable(sizes.countTokens()*5/4);
    while (sizes.hasMoreTokens()){
      Hashtable pieces = new Hashtable(15);
      Integer size = new Integer(sizes.nextToken());

      Image image;

      String dirName = relPath+"/"+size;

      pieces.put(ChessPiece.WHITE_KING, image = toolkit.getImage(c.getResource(dirName+"/wk.gif")));
      tracker.addImage(image, 0);

      pieces.put(ChessPiece.BLACK_KING, image = toolkit.getImage(c.getResource(dirName+"/bk.gif")));
      tracker.addImage(image, 0);

      pieces.put(ChessPiece.WHITE_QUEEN, image = toolkit.getImage(c.getResource(dirName+"/wq.gif")));
      tracker.addImage(image, 0);

      pieces.put(ChessPiece.BLACK_QUEEN, image = toolkit.getImage(c.getResource(dirName+"/bq.gif")));
      tracker.addImage(image, 0);

      pieces.put(ChessPiece.WHITE_ROOK, image = toolkit.getImage(c.getResource(dirName+"/wr.gif")));
      tracker.addImage(image, 0);

      pieces.put(ChessPiece.BLACK_ROOK, image = toolkit.getImage(c.getResource(dirName+"/br.gif")));
      tracker.addImage(image, 0);

      pieces.put(ChessPiece.WHITE_BISHOP, image = toolkit.getImage(c.getResource(dirName+"/wb.gif")));
      tracker.addImage(image, 0);

      pieces.put(ChessPiece.BLACK_BISHOP, image = toolkit.getImage(c.getResource(dirName+"/bb.gif")));
      tracker.addImage(image, 0);

      pieces.put(ChessPiece.WHITE_KNIGHT, image = toolkit.getImage(c.getResource(dirName+"/wn.gif")));
      tracker.addImage(image, 0);

      pieces.put(ChessPiece.BLACK_KNIGHT, image = toolkit.getImage(c.getResource(dirName+"/bn.gif")));
      tracker.addImage(image, 0);

      pieces.put(ChessPiece.WHITE_PAWN, image = toolkit.getImage(c.getResource(dirName+"/wp.gif")));
      tracker.addImage(image, 0);

      pieces.put(ChessPiece.BLACK_PAWN, image = toolkit.getImage(c.getResource(dirName+"/bp.gif")));
      tracker.addImage(image, 0);

      pieceImages.put(size, pieces);

      try{
        tracker.waitForAll();
      } catch (InterruptedException e){
          e.printStackTrace();
        }
    }

    cache.put(new PairKey(c, new PairKey(relPath, extension)), new Object[]{prefSize, pieceImages});
  }


}