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

package free.jin;

import free.jin.event.ChessEventListener;


/**
 * An extension of the <code>JinConnection</code> interface to provide access
 * to events announced on the server.
 */

public interface ChessEventJinConnection extends JinConnection{


  
  /**
   * Adds the given ChessEventListener to the list of listeners receiving
   * notifications when an event is added.
   */

  void addChessEventListener(ChessEventListener listener);



  /**
   * Removes the given ChessEventListener from the list of listeners receiving
   * notifications when an event is added.
   */

  void removeChessEventListener(ChessEventListener listener);

}
