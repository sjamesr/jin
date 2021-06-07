/**
 * Jin - a chess client for internet chess servers. More information is available at
 * http://www.jinchess.com/. Copyright (C) 2005 Alexander Maryanovsky. All rights reserved.
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
package free.jin.ui;

import free.jin.I18n;
import free.jin.Jin;
import free.jin.Session;
import free.jin.plugin.Plugin;
import free.jin.plugin.PluginUIContainer;
import free.jin.plugin.PluginUIEvent;
import free.jin.plugin.PluginUIListener;
import free.util.RectDouble;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Image;
import javax.swing.event.EventListenerList;

/** A base implementation of PluginUIContainer. */
public abstract class AbstractPluginUIContainer implements PluginUIContainer {

  /** The plugin this plugin container is for. */
  private final Plugin plugin;

  /** The id of this plugin ui container. */
  private final String id;

  /** The mode of this plugin container. */
  private final int mode;

  /** A list of our plugin ui listeners. */
  private final EventListenerList listenerList = new EventListenerList();

  /** Our title. */
  private String title;

  /** Our icon image. */
  private Image icon;

  /** Whether this container is visible the very first it is created. */
  private boolean isVisibleFirstTime = true;

  /** Creates a new <code>AbstractPluginUIContainer</code>. */
  public AbstractPluginUIContainer(Plugin plugin, String id, int mode) {
    this.plugin = plugin;
    this.id = id;
    this.mode = mode;
  }

  /** Disposes of this plugin container. */
  @Override
  public final void dispose() {
    disposeImpl();

    firePluginUIEvent(new PluginUIEvent(this, PluginUIEvent.PLUGIN_UI_DISPOSED));
  }

  /** Actually disposes of this plugin ui container. */
  protected abstract void disposeImpl();

  /**
   * Loads the state of this plugion ui from preferences. This method should probably be called from
   * the constructor of the implementing class.
   */
  protected abstract void loadState();

  /**
   * Saves the current state of this plugin ui container into preferences. This method should
   * probably be called from the <code>disposeImpl<code>
   * method of the implementing class.
   */
  protected abstract void saveState();

  /** Returns the plugin to which we belong. */
  @Override
  public Plugin getPlugin() {
    return plugin;
  }

  /** Returns the id of this plugin ui container. */
  @Override
  public final String getId() {
    return id;
  }

  /** Returns the mode of this plugin ui container. */
  @Override
  public final int getMode() {
    return mode;
  }

  /**
   * Returns the desired initial proportions for the specified actual container used by this plugin
   * container. <code>enclosingSize</code> specifies the size of the parent.
   */
  protected RectDouble getInitialBounds(Container container, Dimension enclosingSize) {
    String pluginId = plugin.getId();

    if ("console".equals(pluginId)) {
      if ("".equals(id)) return new RectDouble(0, 0, 0.75, 0.75);
      else if (id.startsWith("custom.")) {
        try {
          int customIndex = Integer.parseInt(id.substring("custom.".length()));
          double dx = 20.0 / enclosingSize.getWidth();
          double dy = 20.0 / enclosingSize.getHeight();
          return new RectDouble(0.25 + dx * customIndex, 0.25 + dy * customIndex, 0.5, 0.5);
        } catch (NumberFormatException e) {
        }
      }
    } else if ("seek".equals(pluginId)) {
      if ("".equals(id)) return new RectDouble(0.5, 0.5, 0.5, 0.5);
    } else if ("board".equals(pluginId)) {
      try {
        int boardIndex = Integer.parseInt(id);
        return new RectDouble(0.25, 0.05 * (boardIndex % 6), 0.75, 0.75);
      } catch (NumberFormatException e) {
      }
    }

    // This makes the insets of the frame known, which is important for
    // determining its real preferred size
    if ((container instanceof Frame) && !container.isDisplayable()) ((Frame) container).pack();

    Dimension prefSize = container.getPreferredSize();
    double w = Math.min(prefSize.width, enclosingSize.width);
    double h = Math.min(prefSize.height, enclosingSize.height);
    double x = (enclosingSize.width - w) / 2;
    double y = (enclosingSize.height - h) / 2;

    if ("actions".equals(pluginId) && "".equals(id)) {
      x = enclosingSize.width - w;
      y = 0;
    }

    return new RectDouble(
        x / enclosingSize.width, y / enclosingSize.height,
        w / enclosingSize.width, h / enclosingSize.height);
  }

  /** Returns the preferences prefix used when storing the settings of this container. */
  public String getPrefsPrefix() {
    return "".equals(id) ? "" : id + ".";
  }

  /** Adds a <code>PluginUIListener</code>. */
  @Override
  public void addPluginUIListener(PluginUIListener listener) {
    listenerList.add(PluginUIListener.class, listener);
  }

  /** Removes a <code>PluginUIListener</code>. */
  @Override
  public void removePluginUIListener(PluginUIListener listener) {
    listenerList.remove(PluginUIListener.class, listener);
  }

  /** Fires the specified PluginUIEvent. */
  protected void firePluginUIEvent(PluginUIEvent evt) {
    PluginUIListener[] listeners = listenerList.getListeners(PluginUIListener.class);
    for (int i = 0; i < listeners.length; i++) {
      PluginUIListener listener = listeners[i];
      evt.dispatch(listener);
    }
  }

  /**
   * Asks for user confirmation and then closes the current session, if there is one. <code>
   * hintParent</code> specifies the hint parent component for the close session confirmation
   * dialog.
   */
  protected void closeSession(Component hintParent) {
    Object result = OptionPanel.OK;
    Session session = Jin.getInstance().getConnManager().getSession();
    if ((session != null) && session.isConnected()) {
      I18n i18n = I18n.get(AbstractPluginUIContainer.class);
      result = i18n.confirm(OptionPanel.OK, "closeSessionDialog", hintParent);
    }

    if (result == OptionPanel.OK) {
      Jin.getInstance().getConnManager().closeSession();
      Jin.getInstance().quitIfNoUiVisible();
    }
  }

  /** Sets the title of this plugin ui container. */
  @Override
  public final void setTitle(String title) {
    setTitleImpl(title);
    this.title = title;
    firePluginUIEvent(new PluginUIEvent(this, PluginUIEvent.PLUGIN_UI_TITLE_CHANGED));
  }

  /** Returns the current title of this plugin ui container. */
  @Override
  public final String getTitle() {
    return title;
  }

  /** Actually sets the title of this plugin ui container. */
  protected abstract void setTitleImpl(String title);

  /** Sets the icon of this plugin ui container. */
  @Override
  public final void setIcon(Image icon) {
    setIconImpl(icon);
    this.icon = icon;
    firePluginUIEvent(new PluginUIEvent(this, PluginUIEvent.PLUGIN_UI_ICON_CHANGED));
  }

  /** Returns the current icon of this plugin ui container. */
  @Override
  public final Image getIcon() {
    return icon;
  }

  /** Actually sets the icon of this plugin ui container. */
  protected abstract void setIconImpl(Image icon);

  /** {@inheritDoc} */
  @Override
  public void setVisibleFirstTime(boolean isVisibleFirstTime) {
    this.isVisibleFirstTime = isVisibleFirstTime;
  }

  /** {@inheritDoc} */
  @Override
  public boolean isVisibleFirstTime() {
    return isVisibleFirstTime;
  }
}
