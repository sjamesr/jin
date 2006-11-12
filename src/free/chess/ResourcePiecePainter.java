/**
 * The chess framework library.
 * More information is available at http://www.jinchess.com/.
 * Copyright (C) 2004 Alexander Maryanovsky.
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

import java.net.URL;
import java.io.IOException;


/**
 * An interface for piece painters which depend on additional resources and are
 * capable of loading them from a specified URL. An implementation is also
 * expected to have a no-arg constructor, so that it can be created via
 * <code>Class.newInstance</code> and initialized by invoking the
 * <code>load</code> method.
 */
 
public interface ResourcePiecePainter extends PiecePainter{
  
  
  
  /**
   * Loads this <code>ResourcePiecePainter</code> from the specified URL.
   * This method should be called immediately after creating the painter
   * and may only be called once.
   */
   
  public void load(URL url) throws IOException;
  
   
   
}