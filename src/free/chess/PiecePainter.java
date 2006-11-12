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
 * along with the chess framework library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package free.chess;

import java.awt.Graphics;
import java.awt.Component;
import java.awt.Rectangle;


/**
 * An interface for classes that paint the pieces (normally on a
 * <code>JBoard</code>).
 */

public interface PiecePainter{


  
  /**
   * Paints the given piece at the given coordinates on the given
   * <code>Graphics</code> object scaled to the given size. The
   * <code>component</code> argument may be null. The <code>shaded</code>
   * argument specifies whether the piece should be drawn in a special, shaded
   * manner.
   */

  void paintPiece(Piece piece, Graphics g, Component component, Rectangle rect, boolean shaded);
  
  
  
  /**
   * Returns a new <code>PiecePainter</code> of the same class as this one.
   * Implementations which are immutable may return this instance itself instead
   * of an actual copy.
   */
  
  PiecePainter freshInstance();
  
  
  
}
