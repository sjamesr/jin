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

import javax.swing.ListModel;
import free.jin.plugin.PluginInfo;


/**
 * Defines methods via which Jin obtains information from and interacts with
 * the environment in which it runs. The implementation would depend on whether
 * Jin is running as an applet, application, webstart application etc.
 */

public interface JinContext{



  /**
   * Returns the application-wide user preferences.
   */

  Preferences getPrefs();
  
  
  
  /**
   * Loads and returns the resources of the specified type. Resources are
   * typically used when there is a need to allow the user (or some other
   * 3rd party) to add his own customizations to Jin (or a plugin). For
   * example, this mechanism is used for loading piece sets and boards by
   * the board manager plugin. A <code>JinContext</code> implementation
   * may then look for piece set "packs" in some predefined directories,
   * allowing the user to add/remove piece sets simply by adding/deleting
   * files from those directories.
   */
   
  ClassLoader [] loadResources(String resourceType);
  
  
  
  /**
   * Quits the application, possible asking the user to confirm quitting first.
   * This method doesn't necessarily return.
   */

  void quit(boolean askToConfirm);



  /**
   * Returns the name of the application.
   */

  String getAppName();



  /**
   * Returns the client version.
   */

  String getAppVersion();



  /**
   * Returns a list of supported servers.
   */

  Server [] getServers();


  
  /**
   * Returns a list of <code>PluginInfo</code> objects describing the plugins
   * for the specified server.
   */

  PluginInfo [] getPlugins(Server server);



  /**
   * Returns the list of known users. This list will be updated as users are
   * added or removed, so you may register listeners with it if you wish to be
   * notified. The list does not include guest users.
   */

  ListModel getUsers();



  /**
   * Adds the specified user to the list of known accounts and returns whether
   * successful. It is the implementation's responsibility to notify the user if
   * adding fails, before returning. The specified user may not be a guest.
   */

  boolean addUser(User user);



  /**
   * Saves the specified user's information and returns whether successful. The
   * specified user must be a known account. It is the implementation's
   * responsibility to notify the user if storing fails, before returning. The
   * specified user may be a guest.
   */

  boolean storeUser(User user);



  /**
   * Removes the specified user from the list of known accounts and returns
   * whether successful. It is the implementation's responsibility to notify the
   * user if removing fails, before returning. The specified user may not be a
   * guest.
   */

  boolean removeUser(User user);

  

  /**
   * Returns the UI provider.
   */

  UIProvider getUIProvider();



  /**
   * Returns the connection manager.
   */

  ConnectionManager getConnManager();



}
