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

package free.jin.event;

import free.jin.Game;
import free.chess.Move;
import free.jin.JinConnection;


/**
 * The event sent when a move is made in a game on a chess server. The source of
 * the event is the game in which the move was made.
 */

public class MoveMadeEvent extends GameEvent{



  /**
   * The move that was made.
   */

  private final Move move;




  /**
   * Creates a new MoveMadeEvent with the given source JinConnection, Move and 
   * the Game in which the move was made.
   */

  public MoveMadeEvent(JinConnection conn, Game game, Move move){
    super(conn, game);

    this.move = move;
  } 




  /**
   * Returns the move that was made.
   */

  public Move getMove(){
    return move;
  }

}