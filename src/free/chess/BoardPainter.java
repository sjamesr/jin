/**
 * The chess framework library.
 * More information is available at http://www.jinchess.com/.
 * Copyright (C) 2002 Alexander Maryanovsky.
 * All rights reserved.
 *
 * The chess framework library is free software; you can redistribute
 * it and/or modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 2 of the
 * License, or (at your option) any later version.
 *
 * The chess framework library is distributed in the hope that it will
 * be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser
 * General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Foobar; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package free.chess;

import java.awt.Graphics;
import java.awt.Dimension;
import java.awt.image.ImageObserver;


/**
 * An interface for classes who paint the board image on the Board component
 * to implement.
 */

public interface BoardPainter{


  /**
   * Hints this BoardPainter that the next paintBoard call will probably be with
   * the given width and height. If relevant, the BoardPainter can use this
   * call to pre-scale the drawn image.
   */

  void scaleHint(int width, int height);



  /**
   * Returns the preferred board size. Should return a dimension that has no area
   * if this BoardPainter has no preferred size.
   */

  Dimension getPreferredBoardSize();

  

  /**
   * Paints the board at the given location on the given Graphics scaled to
   * the given size.
   */

  void paintBoard(Graphics g, ImageObserver observer, int x, int y, int width, int height);

}
