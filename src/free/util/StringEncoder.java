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

package free.util;

import java.awt.Rectangle;
import java.awt.Color;


/**
 * A utility class which encodes certain common types into Strings.
 * 
 * @see StringParser
 */


public class StringEncoder{

  
  /**
   * Encodes the given Rectangle into a String in the following format:
   * "<x>;<y>;<width>;<height>" where <value> is replaced with the appropriate
   * value of the given Rectangle.
   *
   * @see StringParser#parseRectangle(String)
   */

  public static String encodeRectangle(Rectangle rect){
    StringBuffer buf = new StringBuffer();
    buf.append(rect.x);
    buf.append(";");
    buf.append(rect.y);
    buf.append(";");
    buf.append(rect.width);
    buf.append(";");
    buf.append(rect.height);

    return buf.toString();
  }




  /**
   * Encodes the given Color into a String in RGB format where each component
   * is encoded into 2 characters as a hexadecimal string.
   */

  public static String encodeColor(Color color){
    return String.valueOf(color.getRGB());
  }

  

}
