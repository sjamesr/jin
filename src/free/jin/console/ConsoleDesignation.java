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

import free.jin.Connection;
import free.jin.event.JinEvent;
import free.util.AbstractNamed;



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
   * some manner in the specified console.
   */
  
  void receive(JinEvent evt, Console console);
  
  
  
  /**
   * Returns the list of command types this console designation is able to
   * issue.
   */
  
  CommandType [] getCommandTypes();
  
  
  
  /**
   * Returns whether the console is closeable.
   */
  
  boolean isConsoleCloseable();
  
  
  
  /**
   * Defines a certain type of command which can be issued by the user.
   */
  
  public static abstract class CommandType extends AbstractNamed{
    
    
    
    /**
     * Creates a new <code>CommandType</code> with the specified name.
     */
    
    public CommandType(String name){
      super(name);
    }
    
    
    
    /**
     * Executes the command specified by the text typed by the user.
     */
    
    public abstract void executeCommand(String userText, Connection connection);
    
    
    
  }
  
  
  
}
