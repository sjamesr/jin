/**
 * Jin - a chess client for internet chess servers. More information is available at
 * http://www.jinchess.com/. Copyright (C) 2007 Alexander Maryanovsky. All rights reserved.
 *
 * <p>This program is free software; you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation; either version 2 of the
 * License, or (at your option) any later version.
 *
 * <p>This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * <p>You should have received a copy of the GNU General Public License along with this program; if
 * not, write to the Free Software Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA
 * 02111-1307, USA.
 */
package free.jin.plugin;

import free.jin.ui.UIProvider;
import java.awt.Image;
import javax.swing.JComponent;

/**
 * A container for the user interface components of a plugin. This can be implemented in different
 * ways - via an internal frame, a single tab in a tabbed pane, or a toplevel window. Note that some
 * implementations may not support some of concepts.
 */
public interface PluginUIContainer {

  /** Returns the plugin to which this <code>PluginUIContainer</code> belongs. */
  Plugin getPlugin();

  /** Returns the id of this <code>PluginUIContainer</code> */
  String getId();

  /**
   * Returns the mode of this container. See {@link UIProvider#createPluginUIContainer(Plugin,
   * String, int)} for more information regarding the mode.
   */
  int getMode();

  /** Adds a <code>PluginUIListener</code>. */
  void addPluginUIListener(PluginUIListener listener);

  /** Removes a <code>PluginUIListener</code>. */
  void removePluginUIListener(PluginUIListener listener);

  /** Returns the content pane to which plugins can add their user interface components. */
  JComponent getContentPane();

  /** Sets the title of the container. */
  void setTitle(String title);

  /** Returns the title of the container. */
  String getTitle();

  /** Sets the icon of the container. */
  void setIcon(Image iconImage);

  /** Returns the icon of the container. */
  Image getIcon();

  /** Sets whether the container is visible. */
  void setVisible(boolean visible);

  /** Returns whether the container is currently visible. */
  boolean isVisible();

  /**
   * Sets whether this container is currently the "active" one. If it's currently invisible, first
   * makes it visible. Being the active container generally means being the container most visible
   * to the user - the one the user is interacting with.
   */
  void setActive(boolean active);

  /** Returns whether this container is currently the "active" one. */
  boolean isActive();

  /** Sets whether this container should be resizable by the user. */
  void setResizable(boolean resizable);

  /** Returns whether this container is currently resizable by the user. */
  boolean isResizable();

  /**
   * Sets whether this container is visible when it is used the very first time (only relevant for
   * hideable containers). During following uses, its visibility state will be restored from the
   * previous use. The default value for this property must be <code>true</code>.
   */
  void setVisibleFirstTime(boolean isVisibleFirstTime);

  /** Returns whether this container is visible when it is created the very first time. */
  boolean isVisibleFirstTime();

  /**
   * Sets the size of this container to its preferred size. If the container is not visible, it is
   * made visible first.
   */
  void pack();

  /**
   * Completely disposes of this PluginUIContainer - it will not be usable after this method is
   * called.
   */
  void dispose();
}
