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

package free.jin.ui;

import java.awt.Component;
import free.jin.plugin.Plugin;
import free.jin.plugin.PluginUIContainer;


/**
 * This interface is implemented by classes that provide different "top level"
 * UI containers for the rest of the application. An implementation might, for
 * example, choose to use a single main frame with many internal frames, or a
 * separate toplevel frame for each container request.
 * An implementation must have a no-arg constructor.
 */

public interface UIProvider{
  
  
  
  /**
   * The code for plugin container mode which specifies that the container must
   * be open as long as the session to the server exists. If the user attempts
   * to close it, he will be asked for confirmation and then the session will be
   * closed along with the window.
   */
  
  public static final int ESSENTIAL_CONTAINER_MODE = 0;
  
  
  
  /**
   * The code for plugin container mode which specifies that the container may 
   * be hidden and then shown again by the user as he wishes. The
   * <code>UIProvider</code> will provide the user with UI that allows him to
   * hide/show the container. 
   */
  
  public static final int HIDEABLE_CONTAINER_MODE = 1;
  
  
  
  /**
   * The code for plugin container mode which specifies that the container may
   * be closed by the user, but not reopened again. This should be mainly used
   * for temporary containers.
   */
  
  public static final int CLOSEABLE_CONTAINER_MODE = 2;
  
  
  
  /**
   * The code for plugin container mode which specifies that the container is
   * not to be managed by the ui provider. 
   */
  
  public static final int SELF_MANAGED_CONTAINER_MODE = -1;
  
  
  
  /**
   * This method is called at startup to ask the UIProvider to display its
   * initial UI. Presumably, the user can then use that UI to connect to the
   * server.
   */
   
  void start();



  /**
   * <p>Creates a new ui container for the specified plugin with the specified
   * id and in the specified mode.
   * <p>The id is used to identify the container between invocations of this
   * method and to track its properties (such as geometry). You may not request
   * more than one container with a given id, but if you use <code>null</code>
   * as the id, the container will not be tracked at all, and you can request as
   * many as you wish. It is recommended to use an empty string for the plugin's
   * main container, if applicable.
   * <p>The mode specifies how the container is treated by the ui provider.
   * Available modes are:
   * <ul>
   *   <li> {@link #ESSENTIAL_CONTAINER_MODE}
   *   <li> {@link #CLOSABLE_CONTAINER_MODE}
   *   <li> {@link #HIDEABLE_CONTAINER_MODE}     
   *   <li> {@link #SELF_MANAGED_CONTAINER_MODE} 
   * </ul>
   */

  PluginUIContainer createPluginUIContainer(Plugin plugin, String id, int mode);



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
   * Returns whether any UI is currently visible to the user. This is used by
   * certain places in code to quit Jin if no UI is visible. The proper way to
   * solve this problem is to register shutdown hooks, but that's not available
   * under Java 1.1 (and Java 1.1 doesn't automatically shut down when no UI is
   * visible).
   */
  
  boolean isUiVisible();


  
  /**
   * This method is called to notify the <code>UIProvider</code> that Jin is
   * being shut down. The implementation is supposed to save any state it
   * requires into preferences and close any UI elements it owns. 
   */
   
  void stop();
  
  
 
}