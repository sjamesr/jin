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

package free.jin.chessclub.board;

import java.awt.*;
import java.io.IOException;
import java.io.InputStream;
import java.util.StringTokenizer;
import java.util.Hashtable;
import free.chess.ImagePiecePainter;
import free.chess.PiecePainter;
import free.chess.ChessPiece;
import free.chess.art.ResourceImagePiecePainter;
import free.util.IOUtilities;

/**
 * An implementation of <code>PiecePainter</code> which draws the bookup piece
 * set from blitzin. Note that the piece set belongs exlusively to
 * chessclub.com - you may use it only as part of Jin and only with the
 * chessclub.com server. You may *not* use it for any other purpose, 
 * commercial or otherwise.
 */

public class BookupBoardPiecePainter extends ResourceImagePiecePainter{




  /**
   * Attempt to load the image data at classload time so that if it fails,
   * the user of the class will know immediately and also to cache the result
   * when the class is loaded, as is usually expected.
   */

  static{
    try{
      new ResourceImagePiecePainter(BookupBoardPiecePainter.class, "bookup", "gif");
    } catch (IOException e){
        throw new RuntimeException("Unable to load the bookup piece set: "+e.getMessage());
      }
  }




  /**
   * Creates a new BookupBoardPiecePainter.
   */

  public BookupBoardPiecePainter() throws IOException{
    super(BookupBoardPiecePainter.class, "bookup", "gif");
  }

}