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

import java.io.IOException;
import free.chess.art.ResourceImagePiecePainter;

/**
 * An implementation of <code>PiecePainter</code> which draws the bookup piece
 * set from blitzin. Note that the vector pieces belong exlusively to
 * chessclub.com - you may use them only as part of Jin and only with the
 * chessclub.com server. You may *not* use them for any other purpose, 
 * commercial or otherwise.
 */

public class Dyche1BoardPiecePainter extends ResourceImagePiecePainter{




  /**
   * Attempt to load the image data at classload time so that if it fails,
   * the user of the class will know immediately and also to cache the result
   * when the class is loaded, as is usually expected.
   */

  static{
    try{
      new ResourceImagePiecePainter(Dyche1BoardPiecePainter.class, "dyche1", "gif");
    } catch (IOException e){
        throw new RuntimeException("Unable to load the dyche1 piece set: "+e.getMessage());
      }
  }




  /**
   * Creates a new BookupBoardPiecePainter.
   */

  public Dyche1BoardPiecePainter() throws IOException{
    super(Dyche1BoardPiecePainter.class, "dyche1", "gif");
  }

}