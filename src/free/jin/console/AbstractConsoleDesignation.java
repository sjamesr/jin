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
 * A skeleton implementation of <code>ConsoleDesignation</code>
 */

public abstract class AbstractConsoleDesignation implements ConsoleDesignation{
  
  
  
  /**
   * The name of the designation.
   */
  
  private final String name;
  
  
  
  /**
   * Whether the console is temporary.
   */
  
  private final boolean isConsoleTemporary;
  
  
  
  /**
   * Creates a new <code>AbstractConsoleDesignation</code> with the specified
   * name and temporary status.
   */
  
  public AbstractConsoleDesignation(String name, boolean isConsoleTemporary){
    this.name = name;
    this.isConsoleTemporary = isConsoleTemporary;
  }
  
  
  
  /**
   * Returns the name of this designation.
   */

  public String getName(){
    return name;
  }
  
  
  
  /**
   * Returns whether the console is temporary.
   */
  
  public boolean isConsoleTemporary(){
    return isConsoleTemporary;
  }
  
  
  
  /**
   * Splits the reception of an event into two phases - accepting (or declining)
   * it and, if accepted, sending it to the console.
   */
  
  public void receive(JinEvent evt, String encoding, Console console){
    if (accept(evt))
      append(evt, encoding, console);
  }
  
  
  
  /**
   * Returns whether the specified event is accepted by this
   * <code>ConsoleDesignation</code>.
   */
  
  protected abstract boolean accept(JinEvent evt);
  
  
  
  /**
   * Appends the specified event to the console, causing it to be displayed
   * there in some manner. <code>encoding</code> specifies the encoding into
   * which text originating from the server should be converted before being
   * displayed (may be <code>null</code> if no conversion is necessary).
   */
  
  protected abstract void append(JinEvent evt, String encoding, Console console);
  
  
  
}
