/**
 * Jin - a chess client for internet chess servers.
 * More information is available at http://www.jinchess.com/.
 * Copyright (C) 2004 Alexander Maryanovsky.
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

package free.jin.freechess.event;

import free.jin.freechess.JinFreechessConnection;
import free.jin.event.JinEvent;
import free.freechess.Ivar;


/**
 * The class for events fired when the state of an ivariable changes. 
 */
 
public class IvarStateChangeEvent extends JinEvent{
  
  
  
  /**
   * The ivar whose state changed.
   */
   
  private final Ivar ivar;
  
  
  
  /**
   * The new state of the ivar.
   */
   
  private final boolean state;
  
  
  
  /**
   * Creates a new <code>IvarStateChangeEvent</code> with the specified
   * <code>JinFreechessConnection</code>, <code>Ivar</code> and its new state.
   */
   
  public IvarStateChangeEvent(JinFreechessConnection conn, Ivar ivar, boolean state){
    super(conn);
    
    if (ivar == null)
      throw new IllegalArgumentException("Null ivar specified");
    
    this.ivar = ivar;
    this.state = state;
  }
  
  
  
  /**
   * Returns the <code>Ivar</code> whose state changed.
   */
   
  public Ivar getIvar(){
    return ivar;
  }
  
  
  
  /**
   * Returns the new state of the ivar.
   */
   
  public boolean getState(){
    return state;
  }
  
  
   
}


