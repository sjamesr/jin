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

package free.jin.action;

import free.jin.*;


/**
 * The context of a <code>JinAction</code>.
 */
 
public class ActionContext{
  
  

  /**
   * The connection to the server.
   */
   
  private final Connection conn;
  
  
  
  /**
   * The user.
   */
   
  private final User user;
  
  
  
  /**
   * The preferences of the action.
   */
   
  private final Preferences prefs;
  
  
  
  /**
   * Creates a new <code>ActionContext</code> with the specified
   * <code>Connection</code>, <code>User</code> and the
   * action's preferences.
   */
   
  public ActionContext(Connection conn, User user, Preferences prefs){
    this.conn = conn;
    this.user = user;
    this.prefs = prefs;
  }
  
  
  
  /**
   * Returns the connection to the server.
   */
   
  public Connection getConnection(){
    return conn;
  }
  
  
  
  /**
   * Returns the <code>User</code> for the session.
   */
   
  public User getUser(){
    return user;
  }
  
  
  
  /**
   * Returns the action's preferences.
   */
   
  public Preferences getPreferences(){
    return prefs;
  }
  
  
   
}