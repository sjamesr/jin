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

import java.util.EventObject;
import free.chess.JBoard;


/**
 * The event sent when the state of the move making process changes.
 */
 
public class MoveProgressEvent extends EventObject{
  
  
  
  /**
   * The code for a MOVE_MAKING_STARTED event.
   */
   
  public static final int MOVE_MAKING_STARTED = 1;
  
  
  
  /**
   * The code for a MOVE_MAKING_ENDED event.
   */
   
  public static final int MOVE_MAKING_ENDED = 2;
  
  
  
  /**
   * The id of this event.
   */
   
  private final int id;
  
  
  
  /**
   * Creates a new MoveProgressEvent with the specified source JBoard and event
   * id.
   */
  
  public MoveProgressEvent(JBoard source, int id){
    super(source);
    
    switch(id){
      case MOVE_MAKING_STARTED:
      case MOVE_MAKING_ENDED:
        break;
      default:
        throw new IllegalStateException("Invalid event it: " + id); 
    }
    
    this.id = id;
  }
  
  
  
  /**
   * Returns the board on which this event occured.
   */
  
  public JBoard getJBoard(){
    return (JBoard)super.getSource();
  }
  
  
  
  /**
   * Returns the id of this event. Possible values are
   * <code>MOVE_MAKING_STARTED</code> and <code>MOVE_MAKING_ENDED</code>.
   */
   
  public int getId(){
    return id;
  }
   
  
  
}