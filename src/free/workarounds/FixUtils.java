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

package free.workarounds;

import java.awt.event.KeyEvent;


/**
 * A class containing various utilities allowing to work around various bugs
 * in Java.
 */

public class FixUtils{



  /**
   * The value of CHAR_UNDEFINED in this JRE. This is needed because
   * its value changed between 1.1 and 1.2. See <A HREF="http://developer.java.sun.com/developer/bugParade/bugs/4106162.html">http://developer.java.sun.com/developer/bugParade/bugs/4106162.html</A> for more information.
   */

  public static final char CHAR_UNDEFINED;



  static{
    try{
      CHAR_UNDEFINED = KeyEvent.class.getField("CHAR_UNDEFINED").getChar(null);
    } catch (IllegalAccessException e){
        throw new InternalError();
      }
      catch (NoSuchFieldException e){
        throw new InternalError();
      }
  }


}


