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
import java.util.Hashtable;
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
   * The <code>JinContext</code>.
   */

  private final JinContext context;



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
   * The plugins' preferences, in order matching the <code>plugins</code>
   * variable.
   */

  private final Preferences [] prefs;
  
  
  
  /**
   * A list of the actions in this session.
   */
   
  private final DefaultListModel actions = new DefaultListModel();



  /**
   * A lazily filled hashtable of plugins mapped to the <code>Preferences</code>
   * objects they should use. Note that these aren't the plugin preferences,
   * but the user preferences backed up by the plugin preferences.
   */

  private final Hashtable pluginsToPreferences = new Hashtable();

  

  /**
   * Creates a new <code>PluginContext</code> with the specified information.
   *
   * @param context The application's context.
   * @param conn The connection to the server.
   * @param user The account with the server.
   * @param plugins The plugins run in the session.
   * @param prefs The plugins' preferences, in order matching the 
   * <code>plugins</code> argument.
   */

  public PluginContext(JinContext context, Connection conn, User user,
      Plugin [] plugins, Preferences [] prefs, JinAction [] actions){
    this.context = context;
    this.conn = conn;
    this.user = user;
    this.plugins = plugins;
    this.prefs = prefs;
    
    for (int i = 0; i < actions.length; i++)
      this.actions.addElement(actions[i]);
  }



  /**
   * Returns the <code>JinContext</code> in which we're running.
   */

  public JinContext getJinContext(){
    return context;
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
      if (plugins[i].getId().equals(id))
        return plugins[i];

    return null;
  }
  
  
  
  /**
   * Returns a list of all the plugins within this context.
   */
   
  public Plugin [] getPlugins(){
    Plugin [] plugins = new Plugin[this.plugins.length];
    
    for (int i = 0; i < plugins.length; i++)
      plugins[i] = this.plugins[i];
    
    return plugins;
  }



  /**
   * Returns the <code>Preferences</code> the specified plugin should use. Note
   * that these aren't the plugin's preferences - these are wrapped user
   * preferences with a prefix of the plugin's id and backed up by the plugin's
   * own preferences.
   */

  public synchronized Preferences getPreferences(Plugin plugin){
    Preferences result = (Preferences)pluginsToPreferences.get(plugin);
    if (result == null){
      Preferences pluginPrefs = prefs[Utilities.indexOf(plugins, plugin)];
      Preferences userPrefs = user.getPrefs();
      String pluginId = plugin.getId();
      result = Preferences.createBackedUp(Preferences.createWrapped(userPrefs, pluginId + "."), pluginPrefs);

      pluginsToPreferences.put(plugin, result);
    }

    return result;
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