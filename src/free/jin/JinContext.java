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

package free.jin;

import java.util.Locale;
import java.util.Map;

import free.jin.action.ActionInfo;
import free.jin.plugin.Plugin;
import free.jin.plugin.PluginInfo;


/**
 * Defines methods via which Jin obtains information from and interacts with
 * the environment in which it runs. The implementation would depend on whether
 * Jin is running as an applet, application, webstart application etc.
 */

public interface JinContext{
  
  
  
  /**
   * Returns the locale for this instance of Jin.
   */
  
  Locale getLocale();



  /**
   * Returns the value of the parameter with the specified name, as it was
   * passed to Jin.
   */
   
  String getParameter(String paramName);
  
  
  
  /**
   * Returns the application-wide user preferences.
   */

  Preferences getPrefs();
  
  
  
  
  /**
   * Loads and returns the resources of the specified type for the specified
   * plugin. The returned map is from resource IDs to <code>Resource</code>s.
   * Resources are typically used when there is a need to allow the user
   * (or some other 3rd party) to add his own customizations to Jin
   * (or a plugin). For example, this mechanism is used for loading piece sets
   * and boards by the board manager plugin. A <code>JinContext</code>
   * implementation may then look for piece set "packs" in some predefined
   * directories, allowing the user to add/remove piece sets simply by
   * adding/deleting files from those directories.
   */
   
  Map getResources(String resourceType, Plugin plugin);
  
  
  
  /**
   * Returns the resource with the specified type and id.
   */
  
  Resource getResource(String resourceType, String id, Plugin plugin);
  
  
  
  /**
   * Quits the application. This method doesn't necessarily return.
   * Note that the instance of Jin will no longer exist when this method is
   * invoked.
   */

  void shutdown();



  /**
   * Returns a list of supported servers.
   */

  Server [] getServers();
  
  
  
  /**
   * Returns a list of known users (accounts on known servers).
   */
   
  User [] getUsers();
  
  
  
  /**
   * Informs the context of the list of known users. This is needed so that the
   * context can store the users on-exit. 
   */
   
  void setUsers(User [] users);


  
  /**
   * Returns a list of <code>ActionInfo</code> objects describing the standalone
   * actions for the specified server.
   */
   
  ActionInfo [] getActions(Server server);
  
  
  
  /**
   * Returns a list of <code>PluginInfo</code> objects describing the plugins
   * for the specified server.
   */

  PluginInfo [] getPlugins(Server server);



  /**
   * Returns whether this context is capable of saving preferences.
   */
   
  boolean isSavePrefsCapable();

  

  /**
   * Returns the text that should be displayed in the warning to the user when
   * he chooses to save his password for future use. The text should explain
   * where and how the user's password will be stored and ask him whether he is
   * sure he wants to save it. The returned may contain newlines. The returned
   * value may be <code>null</code>, in which case no warning is displayed.
   */
   
  String getPasswordSaveWarning();
  
  
  
  /**
   * Returns whether this context allows the end-user to extend Jin by running
   * extra plugins, actions, resources etc.
   */
   
  boolean isUserExtensible();



}
