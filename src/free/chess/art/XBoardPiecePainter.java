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

/**
 * An implementation of <code>PiecePainter</code> which draws the pieces used
 * by xboard/winboard.
 */

public class XBoardPiecePainter extends ImagePiecePainter{




  /**
   * The Hashtable of piece images. Maps Integer objects specifying the size
   * to Hashtables mapping Piece objects to Image objects.
   */

  private static final Hashtable pieceImages;




  /**
   * Loads the piece images.
   */

  static{
    try{
      Class myClass = XBoardPiecePainter.class;
      Toolkit toolkit = Toolkit.getDefaultToolkit();
      MediaTracker tracker = new MediaTracker(new Canvas());
      InputStream sizesIn = myClass.getResourceAsStream("xboard/sizes.txt");
      StringTokenizer sizes = new StringTokenizer(IOUtilities.loadText(sizesIn));
      pieceImages = new Hashtable(sizes.countTokens()*5/4);
      while (sizes.hasMoreTokens()){
        Hashtable pieces = new Hashtable(15);
        Integer size = new Integer(sizes.nextToken());

        InputStream in;
        byte [] imageData;
        Image image;

        imageData = IOUtilities.readToEnd(myClass.getResourceAsStream("xboard/"+size+"/wk.gif"));
        pieces.put(ChessPiece.WHITE_KING, image = toolkit.createImage(imageData));
        tracker.addImage(image, 0);

        imageData = IOUtilities.readToEnd(myClass.getResourceAsStream("xboard/"+size+"/bk.gif"));
        pieces.put(ChessPiece.BLACK_KING, image = toolkit.createImage(imageData));
        tracker.addImage(image, 0);

        imageData = IOUtilities.readToEnd(myClass.getResourceAsStream("xboard/"+size+"/wq.gif"));
        pieces.put(ChessPiece.WHITE_QUEEN, image = toolkit.createImage(imageData));
        tracker.addImage(image, 0);

        imageData = IOUtilities.readToEnd(myClass.getResourceAsStream("xboard/"+size+"/bq.gif"));
        pieces.put(ChessPiece.BLACK_QUEEN, image = toolkit.createImage(imageData));
        tracker.addImage(image, 0);

        imageData = IOUtilities.readToEnd(myClass.getResourceAsStream("xboard/"+size+"/wr.gif"));
        pieces.put(ChessPiece.WHITE_ROOK, image = toolkit.createImage(imageData));
        tracker.addImage(image, 0);

        imageData = IOUtilities.readToEnd(myClass.getResourceAsStream("xboard/"+size+"/br.gif"));
        pieces.put(ChessPiece.BLACK_ROOK, image = toolkit.createImage(imageData));
        tracker.addImage(image, 0);

        imageData = IOUtilities.readToEnd(myClass.getResourceAsStream("xboard/"+size+"/wb.gif"));
        pieces.put(ChessPiece.WHITE_BISHOP, image = toolkit.createImage(imageData));
        tracker.addImage(image, 0);

        imageData = IOUtilities.readToEnd(myClass.getResourceAsStream("xboard/"+size+"/bb.gif"));
        pieces.put(ChessPiece.BLACK_BISHOP, image = toolkit.createImage(imageData));
        tracker.addImage(image, 0);

        imageData = IOUtilities.readToEnd(myClass.getResourceAsStream("xboard/"+size+"/wn.gif"));
        pieces.put(ChessPiece.WHITE_KNIGHT, image = toolkit.createImage(imageData));
        tracker.addImage(image, 0);

        imageData = IOUtilities.readToEnd(myClass.getResourceAsStream("xboard/"+size+"/bn.gif"));
        pieces.put(ChessPiece.BLACK_KNIGHT, image = toolkit.createImage(imageData));
        tracker.addImage(image, 0);

        imageData = IOUtilities.readToEnd(myClass.getResourceAsStream("xboard/"+size+"/wp.gif"));
        pieces.put(ChessPiece.WHITE_PAWN, image = toolkit.createImage(imageData));
        tracker.addImage(image, 0);

        imageData = IOUtilities.readToEnd(myClass.getResourceAsStream("xboard/"+size+"/bp.gif"));
        pieces.put(ChessPiece.BLACK_PAWN, image = toolkit.createImage(imageData));
        tracker.addImage(image, 0);

        pieceImages.put(size, pieces);

        try{
          tracker.waitForAll();
        } catch (InterruptedException e){
            e.printStackTrace();
          }
      }
    } catch (IOException e){
        throw new RuntimeException("Unable to load xboard piece images");
      }
      catch (RuntimeException e){
        e.printStackTrace();
        throw e;
      }
  }




  /**
   * Creates a new XBoardPiecePainter.
   */

  public XBoardPiecePainter(){
    super(new Dimension(64, 64), pieceImages);
  }

}