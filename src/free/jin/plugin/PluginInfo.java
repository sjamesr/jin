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

import free.jin.Preferences;


/**
 * Holds information about a plugin which is available without actually
 * instantiating the plugin.
 */

public class PluginInfo{



  /**
   * The class of the plugin.
   */

  private final Class pluginClass;



  /**
   * The plugin preferences.
   */

  private final Preferences pluginPrefs;



  /**
   * Creates a new <code>PluginInfo</code> object with the specified plugin
   * class and preferences.
   */

  public PluginInfo(Class pluginClass, Preferences pluginPrefs){
    this.pluginClass = pluginClass;
    this.pluginPrefs = pluginPrefs;
  }



  /**
   * Returns the plugin class.
   */

  public Class getPluginClass(){
    return pluginClass;
  }



  /**
   * Returns the plugin preferences.
   */

  public Preferences getPluginPreferences(){
    return pluginPrefs;
  }



}
