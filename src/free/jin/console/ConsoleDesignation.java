/**
 * Jin - a chess client for internet chess servers.
 * More information is available at http://www.jinchess.com/.
 * Copyright (C) 2007 Alexander Maryanovsky.
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

package free.jin.console;

import free.jin.event.JinEvent;



/**
 * Defines the types of events a console is willing to display and the types of
 * commands a user might issue through that console.
 */

public interface ConsoleDesignation{
  
  
  
  /**
   * Returns the name of this designation.
   */
  
  String getName();
  
  
  
  /**
   * Receives the specified <code>JinEvent</code>, possibly displaying it in
   * some manner in the specified console. Before displaying any text obtained
   * from the event, it should be encoded in the specified encoding (
   * <code>null</code> if shouldn't).
   */
  
  void receive(JinEvent evt, String encoding, Console console);
  
  
  
  /**
   * Returns whether the console is temporary.
   */
  
  boolean isConsoleTemporary();
  
  
  
}
