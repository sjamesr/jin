/**
 * Jin - a chess client for internet chess servers.
 * More information is available at http://www.jinchess.com/.
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

import java.io.IOException;
import free.chess.BoardPainter;
import free.chess.ProxyBoardPainter;
import free.chess.ResourceImageBoardPainter;


/**
 * This class allows one to package an image based board inside a jar, or any
 * other structure that can be accessed via a classloader. The format of the
*  jar is described in the
 * <code>free.chess.art.ResourceImageBoardPainter</code> class with the root
 * directory being the root of the jar. You then place the class file of this
 * class in the root.
 */
 
public class ImageBoardLoader extends ProxyBoardPainter{
  
  
  
  /**
   * The sole instance of the "real" board painter.
   */

  private static final BoardPainter boardPainter;
  
  
  
  /**
   * Creates the "real" board painter.
   */
  
  static{
    try{
      boardPainter =
        ResourceImageBoardPainter.getInstance(ImageBoardLoader.class, "");
    } catch (IOException e){
        e.printStackTrace();
        throw new RuntimeException("Unable to load the board pattern: " + e.getMessage());
      }
  }
  
  
  
  /**
   * Creates a new <code>ImageBoardLoader</code>.
   */

  public ImageBoardLoader() throws IOException{
    super(boardPainter);
  }
  
   
   
}