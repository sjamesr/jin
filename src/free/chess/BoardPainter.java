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


/**
 * An interface for classes who paint the board "background" on JBoard.
 */

public interface BoardPainter{



  /**
   * Forces the board painter to load any resources it requires to draw the
   * board. It's up to the implementation whether the resources should be loaded
   * in the current thread (preferably, only if they are small and guaranteed
   * not to block for a long time), or in a background thread.
   */
  
  void loadResources();
  
   
  
  /**
   * Paints the board at the given location on the given Graphics scaled to
   * the given size. The <code>component</code> argument may be null.
   */

  void paintBoard(Graphics g, Component component, int x, int y, int width, int height);

  
  
}
