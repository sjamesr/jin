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

import java.awt.Color;


/**
 * An extension of the BoardPainter interface which allows setting and
 * retrieving the colors with which the squares are drawn.
 */

public interface ColoredBoardPainter extends BoardPainter{



  /**
   * Returns the color of the light squares.
   */

  Color getLightColor();




  /**
   * Sets the color of the light squares.
   */

  void setLightColor(Color color);




  /**
   * Returns the color of the dark squares.
   */

  Color getDarkColor();




  /**
   * Sets the color of the dark squares.
   */

  void setDarkColor(Color color);


}