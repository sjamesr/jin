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

package free.chess.event;

import java.util.EventListener;


/**
 * The listener for events about the current state of the move-making process.
 */
 
public interface MoveProgressListener extends EventListener{
  
  
  
  /**
   * Gets called when the user starts making a move.
   */
   
  void moveMakingStarted(MoveProgressEvent evt);
  
  
  
  /**
   * Gets called when the user finishes making a move.
   */
   
  void moveMakingEnded(MoveProgressEvent evt);
  
  
  
}