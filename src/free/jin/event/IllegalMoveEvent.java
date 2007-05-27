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
import free.chess.Move;
import free.jin.Connection;


/**
 * The event fired when the user tried to make a move and the server rejected
 * it. 
 */

public class IllegalMoveEvent extends GameEvent{
  
  
  
  /**
   * The reason code for the server rejecting the move for some other reason.
   */
  
  public static final int OTHER = -1;
  
  
  
  /**
   * The reason code for an attempt to make an illegal move.
   */
  
  public static final int ILLEGAL_MOVE = 1;
  
  
  
  /**
   * The reason code for an attempt to make a move when it isn't the user's
   * turn. 
   */
  
  public static final int NOT_YOUR_TURN = 2;
  
  
  
  /**
   * The attempted illegal move.
   */

  private final Move move;
  
  
  
  /**
   * The reason code. One of the constants defined in this class.
   */
  
  private final int reasonCode;
  
  
  
  /**
   * Creates a new IllegalMoveEvent with the given source Connection, Game 
   * and the attempted illegal move.
   */

  public IllegalMoveEvent(Connection conn, String clientTag, Game game, Move move, int reasonCode){
    super(conn, clientTag, game);
    
    switch (reasonCode){
      case ILLEGAL_MOVE:
      case NOT_YOUR_TURN:
      case OTHER:
        break;
      default:
        throw new IllegalArgumentException("Bad reasonCode value: " + reasonCode);
    }
    
    this.move = move;
    this.reasonCode = reasonCode;
  }



  /**
   * Returns the attempted illegal move.
   */

  public Move getMove(){
    return move;
  }
  
  
  
  /**
   * Returns the reason code for the rejection - one of the constants defined in
   * this class. 
   */
  
  public int getReasonCode(){
    return reasonCode;
  }
  
  
  
}
