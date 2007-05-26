/**
 * Jin - a chess client for internet chess servers.
 * More information is available at http://www.jinchess.com/.
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

package free.jin.event;

import free.jin.Game;
import free.jin.Connection;


/**
 * The event fired when a game ends, or the user stopped
 * playing/examining/observing it. The game's result may be obtained via
 * {@link Game#getResultCode()} and {@link Game#getGameEndReasonCode()}, and the
 * source of the event should set it before firing the event.
 */

public class GameEndEvent extends GameEvent{



  /**
   * Creates a new GameEndEvent with the given Game, source Connection and
   * result.
   */

  public GameEndEvent(Connection conn, String clientTag, Game game){
    super(conn, clientTag, game);
  }
  
  
  
}
