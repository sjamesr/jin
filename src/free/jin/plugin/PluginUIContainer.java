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

import java.awt.Container;
import java.awt.Image;


/**
 * A container for the user interface components of a plugin. This can be
 * implemented in different ways - via an internal frame, a single tab in a
 * tabbed pane, or a toplevel window.
 */

public interface PluginUIContainer{



  /**
   * The constant for hiding the plugin container on a close operation.
   */

  int HIDE_ON_CLOSE = 1;



  /**
   * The constant for closing the session on a close operation.
   */

  int CLOSE_SESSION_ON_CLOSE = 2;



  /**
   * The constant for not doing anything on a close operation.
   */

  int DO_NOTHING_ON_CLOSE = 3;



  /**
   * Adds a <code>PluginUIListener</code>.
   */

  void addPluginUIListener(PluginUIListener listener);



  /**
   * Removes a <code>PluginUIListener</code>.
   */

  void removePluginUIListener(PluginUIListener listener);



  /**
   * Returns the content pane to which plugins can add their user interface
   * components.
   */

  Container getContentPane();



  /**
   * Sets the title of the container.
   */

  void setTitle(String title);



  /**
   * Sets the icon of the container.
   */

  void setIcon(Image iconImage);



  /**
   * Sets whether the container is visible.
   */

  void setVisible(boolean visible);



  /**
   * Returns whether the container is currently visible.
   */

  boolean isVisible();



  /**
   * Sets whether this container is currently the "active" one. If it's
   * currently invisible, first makes it visible.
   * Being the active container generally means being the container most visible
   * to the user - the one the user is interacting with.
   */

  void setActive(boolean active);



  /**
   * Returns whether this container is currently the "active" one.
   */

  boolean isActive();



  /**
   * Sets this container's close operation. Possible values are
   * {@link #HIDE_ON_CLOSE}, {@link #CLOSE_SESSION_ON_CLOSE} and
   * {@link #DO_NOTHING_ON_CLOSE}. The default value depends on the actual
   * implementation and may even be neither of the operations described above.
   */

  void setCloseOperation(int val);



}
