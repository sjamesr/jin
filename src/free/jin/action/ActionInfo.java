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

import free.jin.Preferences;


/**
 * Encapsulates the information about a JinAction required for one to be created
 * properly.
 */
 
public class ActionInfo{
  
  
  
  /**
   * The class implementing the action.
   */
   
  private final Class actionClass;
  
  
  
  /**
   * The preferences of the action.
   */
   
  private final Preferences actionPrefs;
  
  
  
  /**
   * Creates a new <code>ActionInfo</code> object with the specified class
   * (implementing the action) and preferences.
   */
   
  public ActionInfo(Class actionClass, Preferences actionPrefs){
    this.actionClass = actionClass;
    this.actionPrefs = actionPrefs;
  }
  
  
  
  /**
   * Returns the class implementing the action.
   */
   
  public Class getActionClass(){
    return actionClass;
  }
  
  
  
  /**
   * Returns the Preferences of the action.
   */
   
  public Preferences getActionPreferences(){
    return actionPrefs;
  }
  
  
   
}