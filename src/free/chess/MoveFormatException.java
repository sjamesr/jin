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

package free.chess;

import free.util.FormatException;


/**
 * Thrown when the format of a chess move is wrong.
 */

public class MoveFormatException extends FormatException{


  /**
   * Creates a new MoveFormatException for the given real Throwable and the given
   * message.
   */

  public MoveFormatException(Throwable realException, String message){
    super(realException, message);
  }



  /**
   * Creates a new MoveFormatException for the given real Throwable.
   */

  public MoveFormatException(Throwable realException){
    super(realException);
  }



  /**
   * Creates a new MoveFormatException with the given message.
   */

  public MoveFormatException(String message){
    super(message);
  }



  /**
   * Creates a new MoveFormatException.
   */

  public MoveFormatException(){
    super();
  }

}
