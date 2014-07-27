/**
 * Jin - a chess client for internet chess servers.
 * More information is available at http://www.jinchess.com/.
 * Copyright (C) 2008 Alexander Maryanovsky.
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

package free.jin.console.ics;

import free.jin.Connection;
import free.jin.console.SystemConsoleDesignation;
import free.jin.event.ChatEvent;



/**
 * A partial system console implementation for ICS-based servers. 
 *
 * @author Maryanovsky Alexander
 */

public abstract class IcsSystemConsoleDesignation extends SystemConsoleDesignation{
  
  
  
  /**
   * Creates a new <code>IcsSystemConsoleDesignation</code> with the specified
   * connection and encoding.
   */
  
  public IcsSystemConsoleDesignation(Connection connection, String encoding){
    super(connection, encoding);
  }
  
  
  
  /**
   * Returns whether the specified chat event is a personal tell to the user.
   */
  
  @Override
  protected boolean isPersonalTell(ChatEvent evt){
    String type = evt.getType();
    return "tell".equals(type) || "say".equals(type) || "ptell".equals(type);
  }
  
  
  
}
