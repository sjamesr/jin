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


package free.jin.chessclub.event;

import free.jin.Game;
import free.jin.chessclub.JinChessclubConnection;
import free.jin.event.GameEvent;
import free.chess.Square;


/**
 * The event fired when an arrow is added/removed to/from the board.
 */

public class ArrowEvent extends GameEvent{
  
  
  
  /**
   * The id for an arrow added event.
   */
   
  public static final int ARROW_ADDED = 1;
  
  
  
  /**
   * The id for an arrow removed event.
   */
   
  public static final int ARROW_REMOVED = 2;
  
  
  
  /**
   * The id of this event. Either {@link #ARROW_ADDED} or
   * {@link #ARROW_REMOVED}. 
   */
  
  private final int id;



  /**
   * The square where the arrow starts. 
   */

  private final Square fromSquare;




  /**
   * The square where the arrow ends.
   */

  private final Square toSquare;




  /**
   * Creates a new ArrowEvent with the specified source
   * <code>JinChessclubConnection</code>, <code>Game</code>, id, square where
   * the arrow starts and square where the arrow ends.
   */
  
  public ArrowEvent(JinChessclubConnection conn, Game game, int id, Square fromSquare, Square toSquare){
    super(conn, game);
    
    switch (id){
      case ARROW_ADDED:
      case ARROW_REMOVED:
        break;
      default:
        throw new IllegalArgumentException("Unrecognized id: " + id);
    }
    
    this.id = id;
    this.fromSquare = fromSquare;
    this.toSquare = toSquare;
  }




  /**
   * Returns the id of this event, either {@link #ARROW_ADDED} or
   * {@link #ARROW_REMOVED}
   */
   
  public int getId(){
    return id;
  }
   
   
   
   
  /**
   * Returns the square where the arrow starts.
   */

  public Square getFromSquare(){
    return fromSquare;
  }




  /**
   * Returns the square where the arrow ends.
   */

  public Square getToSquare(){
    return toSquare;
  }


}
