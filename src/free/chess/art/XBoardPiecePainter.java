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
 * by xboard/winboard. The piece set is available under the General Public
 * License from the xboard/winboard author, Tim Mann. More information is
 * available at http://www.tim-mann.org/xboard.html.
 */

public class XBoardPiecePainter extends ResourceImagePiecePainter{




  /**
   * Attempt to load the image data at classload time so that if it fails,
   * the user of the class will know immediately and also to cache the result
   * when the class is loaded, as is usually expected.
   */

  static{
    try{
      new ResourceImagePiecePainter(XBoardPiecePainter.class, "xboard", "gif");
    } catch (IOException e){
        throw new RuntimeException("Unable to load the xboard piece set: "+e.getMessage());
      }
  }




  /**
   * Creates a new XBoardPiecePainter.
   */

  public XBoardPiecePainter() throws IOException{
    super(XBoardPiecePainter.class, "xboard", "gif");
  }

}