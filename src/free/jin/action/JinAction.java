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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import free.jin.*;


/**
 * The base class for jin actions. Jin actions are miniature plugins, which
 * encapsulate a single action. An action can be created in two ways - either
 * it is created by the Jin framework as a standalone action or it can be
 * created and exported by a plugin. Subclasses which implement standalone
 * actions must have a no-arg constructor. For convenience,
 * <code>JinAction</code> implements <code>ActionListener</code> which simply
 * runs the action.
 */
 
public abstract class JinAction implements ActionListener{
  
  
  
  /**
   * The action's context.
   */
   
  private ActionContext context;
  
  
  
  /**
   * The <code>Preferences</code> object this action uses to store its
   * preferences.
   */
  
  private Preferences prefs;
  
  
  
  /**
   * The <code>I18n</code> for this action.
   */
  
  private I18n i18n;
  
  

  /**
   * Sets the action's context. Returns whether the context is supported.
   */
   
  public boolean setContext(ActionContext context){
    if (this.context != null)
      throw new IllegalStateException("ActionContext already set");
    
    this.context = context;
    
    return isContextSupported(context);
  }
  
  
  
  /**
   * Returns whether the specified context is supported by this action. The
   * default implementation always returns <code>true</code>. 
   */
   
  protected boolean isContextSupported(ActionContext context){
    return true;
  }
  
  
  
  /**
   * Returns the connection to the server.
   */
   
  public Connection getConn(){
    return context.getConnection();
  }
  
  
  
  /**
   * Returns the user for the session.
   */
  
  public User getUser(){
    return context.getUser();
  }
  
  
  
  /**
   * Returns the server for the session.
   */
   
  public Server getServer(){
    return getUser().getServer();
  }
  
  
  
  /**
   * Returns the action's preferences.
   */
   
  public Preferences getPrefs(){
    if (prefs == null){
      Preferences actionPrefs = context.getPreferences();
      Preferences userPrefs = getUser().getPrefs();
      prefs = Preferences.createBackedUp(Preferences.createWrapped(userPrefs, "action." + getId() + "."), actionPrefs);
    }

    return prefs;
  }
  
  
  
  /**
   * A helper function which returns the <code>I18n</code> for this action.
   */
  
  public I18n getI18n(){
    if (i18n == null)
      i18n = I18n.get(getClass(), JinAction.class);
    
    return i18n;
  }
  
  
  
  
  /**
   * <code>ActionListener</code> implementation. Simply forwards the call to the
   * <code>go</code> method.
   */
   
  public void actionPerformed(ActionEvent evt){
    go(evt.getSource());
  }
  
  
  
  /**
   * Invokes the action.
   * 
   * @param actor the object which, in some way, triggered the action. May be <code>null</code>.
   */
  
  public abstract void go(Object actor);
  
  
  
  /**
   * Returns the action's name. This may be displayed to the user.
   */
   
  public abstract String getName();
  
  
  
  /**
   * Returns the action's id. This will not be displayed to the user and must
   * be unique between all actions.
   */
   
  public abstract String getId();
  
  
   
}

