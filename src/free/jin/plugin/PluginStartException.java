/**
 * Jin - a chess client for internet chess servers.
 * More information is available at http://www.jinchess.com/.
 * Copyright (C) 2003 Alexander Maryanovsky.
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

package free.jin.plugin;


/**
 * The exception thrown when creating, initializing or starting a plugin fails.
 */

public class PluginStartException extends Exception{



  /**
   * The original (cause) exception.
   */

  private final Exception cause;



  /**
   * Creates a new <code>PluginStartException</code>.
   */

  public PluginStartException(){
    this(null, null);
  }



  /**
   * Creates a new <code>PluginStartException</code> with the specified cause
   * exception.
   */

  public PluginStartException(Exception cause){
    this(cause, null);
  }



  /**
   * Creates a new PluginStartException with the specified error message and no
   * cause exception.
   */

  public PluginStartException(String message){
    this(null, message);
  }



  /**
   * Creates a new <code>PluginStartException</code> with the specified cause
   * exception and error message.
   */

  public PluginStartException(Exception cause, String message){
    super(message);

    this.cause = cause;
  }



  /**
   * Returns the cause exception - may be null.
   */

  public Exception getReason(){
    return cause;
  }
  
  
  
  /**
   * Prints the stack trace of the cause and then of this exception.
   */
   
  public void printStackTrace(){
    if (cause != null)
      cause.printStackTrace();
    
    super.printStackTrace();
  }



}