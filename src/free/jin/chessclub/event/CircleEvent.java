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
 * The event fired when a circle has been added/removed to/from the board.
 */

public class CircleEvent extends GameEvent{
  
  
  
  /**
   * The id for a circle added event.
   */
   
  public static final int CIRCLE_ADDED = 1;
  
  
  
  /**
   * The id for a circle removed event.
   */
   
  public static final int CIRCLE_REMOVED = 2;
  
  
  
  /**
   * The id of this event. Either {@link #CIRCLE_ADDED} or
   * {@link #CIRCLE_REMOVED}. 
   */
   
  private final int id;



  /**
   * The square where the circle is.
   */

  private final Square circleSquare;





  /**
   * Creates a new ArrowEvent with the specified source
   * <code>JinChessclubConnection</code>, <code>Game</code>, id and the square
   * of the circle.
   */
  
  public CircleEvent(JinChessclubConnection conn, Game game, int id, Square circleSquare){
    super(conn, game);
    
    switch(id){
      case CIRCLE_ADDED:
      case CIRCLE_REMOVED:
        break;
      default:
        throw new IllegalArgumentException("Unrecognized id: " + id);
    }

    this.id = id;
    this.circleSquare = circleSquare;
  }



  /**
   * Returns the id of this event, either {@link #CIRCLE_ADDED} or
   * {@link #CIRCLE_REMOVED}.  
   */
   
  public int getId(){
    return id;
  }
  
  
  
  /**
   * Returns the square of the circle.
   */

  public Square getCircleSquare(){
    return circleSquare;
  }



}
