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

import java.awt.Component;
import free.jin.plugin.Plugin;
import free.jin.plugin.PluginUIContainer;


/**
 * This interface is implemented by classes that provide different "top level"
 * UI containers for the rest of the application. An implementation might, for
 * example, choose to use a single main frame with many internal frames, or a
 * separate toplevel frame for each container request.
 */

public interface UIProvider{



  /**
   * Creates a new ui container for the specified plugin and id. The id is used
   * to identify the container between invocations of this method and to track
   * its properties (such as geometry). You may not request more than one
   * container with a given id, but if you use <code>null</code> as the id, the
   * container will not be tracked at all, and you can request as many as you
   * wish. It is recommended to use an empty string for the plugin's main
   * container, if applicable.
   */

  PluginUIContainer createPluginUIContainer(Plugin plugin, String id);



  /**
   * Displays the specified <code>DialogPanel</code> and waits for the user to
   * finish interacting with it. The specified "parent" component is used as a
   * hint as to where to display the <code>DialogPanel</code> and may be
   * <code>null</code> to indicate no preference. <strong>Note:</strong> Users
   * should not invoke this method, but instead use their
   * <code>DialogPanel</code>'s specific method which returns the proper type.
   */

  void showDialog(DialogPanel dialog, Component parent);



  /**
   * This method is called by the <code>ConnectionManager</code> to notify the
   * UI provider that a session has been created/closed. The UI provider, in
   * response, should make any relevant changes to the UI.
   */

  void setConnected(boolean isConnected, Session session);

  
 
}