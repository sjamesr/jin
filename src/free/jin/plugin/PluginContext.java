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

import free.jin.*;
import javax.swing.ListModel;
import javax.swing.DefaultListModel;
import free.util.Utilities;
import free.jin.action.JinAction;


/**
 * The context of a plugin. Provides the plugin with methods to access the
 * environment in which it runs.
 */

public class PluginContext{



  /**
   * The connection to the server.
   */

  private final Connection conn;



  /**
   * The account with the server.
   */

  private final User user;


 
  /**
   * A list of plugins run in the session.
   */

  private final Plugin [] plugins;
  
  
  
  /**
   * Specifies which plugins passed <code>Plugin.setContext</code>.
   */
  
  private final boolean [] isPluginActive;
  
  
  
  /**
   * The number of "active" plugins.
   */
  
  private int activePluginsCount;



  /**
   * The plugins' preferences, in order matching the <code>plugins</code>
   * variable.
   */

  private final Preferences [] prefs;
  
  
  
  /**
   * A list of the actions in this session.
   */
   
  private final DefaultListModel actions = new DefaultListModel();



  /**
   * Creates a new <code>PluginContext</code> with the specified information.
   *
   * @param conn The connection to the server.
   * @param user The account with the server.
   * @param plugins The plugins run in the session.
   * @param prefs The plugins' preferences, in order matching the 
   * <code>plugins</code> argument.
   */

  public PluginContext(Connection conn, User user, Plugin [] plugins,
      Preferences [] prefs, JinAction [] actions){
        
    this.conn = conn;
    this.user = user;
    this.plugins = (Plugin[])plugins.clone();
    this.isPluginActive = new boolean[plugins.length];
    this.prefs = (Preferences[])prefs.clone();
    
    for (int i = 0; i < actions.length; i++)
      this.actions.addElement(actions[i]);
  }
  
  
  
  /**
   * Invokes {@link Plugin#setContext(PluginContext)} on all the plugins in this context. Any
   * incompatible plugins are removed, and on the rest, {@link Plugin#start()} is invoked.
   */
  
  public void setAndStart() throws PluginStartException{
    activePluginsCount = 0;
    for (int i = 0; i < plugins.length; i++){
      boolean ok = plugins[i].setContext(this);
      isPluginActive[i] = ok;
      if (ok)
        activePluginsCount++;
    }
    
    // Start the plugins
    for (int i = 0; i < plugins.length; i++){
      if (isPluginActive[i]){
        Plugin plugin = plugins[i];
        try{
          plugin.start();
        } catch (Exception e){
            throw new PluginStartException(e, "Failed to start plugin: " + plugin);
          }
      }
    }
  }




  /**
   * Returns the connection to the server.
   */

  public Connection getConnection(){
    return conn;
  }



  /**
   * Returns the <code>User</code> object representing the account on the
   * server.
   */

  public User getUser(){
    return user;
  }



  /**
   * Returns the plugin with the specified id.
   */

  public Plugin getPlugin(String id){
    for (int i = 0; i < plugins.length; i++)
      if (isPluginActive[i] && plugins[i].getId().equals(id))
        return plugins[i];

    return null;
  }
  
  
  
  /**
   * Returns a list of all the plugins within this context.
   */
   
  public Plugin [] getPlugins(){
    Plugin [] plugins = new Plugin[activePluginsCount];
    
    int pluginCount = 0;
    for (int i = 0; i < this.plugins.length; i++)
      if (isPluginActive[i])
        plugins[pluginCount++] = this.plugins[i];
    
    return plugins;
  }



  /**
   * Returns the <code>Preferences</code> of the specified plugin.
   */

  public synchronized Preferences getPreferences(Plugin plugin){
    return prefs[Utilities.indexOf(plugins, plugin)];
  }
  
  
  
  /**
   * Returns a list of currently available actions. Listeners to the
   * <code>ListModel</code> will receive notification when actions are added.
   */
   
  public ListModel getActions(){
    return actions;
  }
  
  
  
  /**
   * Returns the action with the specified id, or <code>null</code> if no such
   * action is available.
   */
   
  public JinAction getAction(String id){
    int size = actions.size();
    for (int i = 0; i < size; i++){
      JinAction action = (JinAction)actions.elementAt(i);
      if (action.getId().equals(id))
        return action;
    }
    
    return null;
  }
  
  
  
  /**
   * Adds the specified action to the list of available actions.
   */
  
  public void addAction(JinAction action){
    actions.addElement(action);
  }



}