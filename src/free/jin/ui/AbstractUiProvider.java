/**
 * Jin - a chess client for internet chess servers.
 * More information is available at http://www.jinchess.com/.
 * Copyright (C) 2005 Alexander Maryanovsky.
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

import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Window;
import java.util.Enumeration;
import java.util.Hashtable;

import javax.swing.event.EventListenerList;

import free.jin.Jin;
import free.jin.Preferences;
import free.jin.Session;
import free.jin.SessionEvent;
import free.jin.SessionListener;
import free.jin.User;
import free.jin.plugin.Plugin;
import free.jin.plugin.PluginUIContainer;
import free.util.AWTUtilities;
import free.util.Pair;
import free.util.RectDouble;



/**
 * A skeleton implementation of the most likely way a UIProvider would work.
 */

public abstract class AbstractUiProvider implements UIProvider, SessionListener{
  
  
  
  /**
   * Maps Pair<String, String> (<pluginId, containerId>) to the
   * <code>PluginContainers</code> for that plugin and container id.
   */

  private final Hashtable pluginContainers = new Hashtable();
  
  
  
  /**
   * The list of our listeners.   
   */
  
  private final EventListenerList listenerList = new EventListenerList();
  

  
  /**
   * {@inheritDoc} 
   */
  
  public void init(){
    Jin.getInstance().getConnManager().addSessionListener(this);
  }
  
  
  
  /**
   * {@inheridDoc}
   */
  
  public void start(){
    
  }
  
  
  
  /**
   * SessionListener implementation.
   */
  
  public void sessionStarting(SessionEvent evt){
    
  }
  
  
  
  /**
   * SessionListener implementation. Restores various preferences.
   */
  
  public void sessionEstablished(SessionEvent evt){
    loadSelectedFrame(evt.getSession());
    
    Enumeration pluginContainers = getExistingPluginUIContainers();
    while (pluginContainers.hasMoreElements()){
      AbstractPluginUIContainer container = 
        (AbstractPluginUIContainer)pluginContainers.nextElement();
      if (container.getMode() == HIDEABLE_CONTAINER_MODE)
        container.setVisible(container.getPlugin().getPrefs().getBool(container.getPrefsPrefix() + "visible",
            container.isVisibleFirstTime()));
    }
  }
  
  
  
  /**
   * SessionListener implementation. Stores various preferences.
   */
  
  public void sessionClosing(SessionEvent evt){
    saveSelectedFrame(evt.getSession());
    
    Enumeration pluginContainers = getExistingPluginUIContainers();
    while (pluginContainers.hasMoreElements()){
      AbstractPluginUIContainer container = 
        (AbstractPluginUIContainer)pluginContainers.nextElement();
      if (container.getMode() == HIDEABLE_CONTAINER_MODE)
        container.getPlugin().getPrefs().setBool(container.getPrefsPrefix() + "visible", container.isVisible());
    }
    
    removePluginContainers();
  }

  
  
  
  /**
   * SessionListener implementation.
   */
  
  public void sessionClosed(SessionEvent evt){
    
  }
  
  
  
  /**
   * Sets the selected frame to the frame we remembered was selected last time.
   */

  private void loadSelectedFrame(Session session){
    User user = session.getUser();
    String pluginId = user.getPrefs().getString("selected.plugin", null);
    String containerId = user.getPrefs().getString("selected.container", null);

    Enumeration e = pluginContainers.elements();
    while (e.hasMoreElements()){
      PluginUIContainer c = (PluginUIContainer)e.nextElement();

      if (c.getPlugin().getId().equals(pluginId) && c.getId().equals(containerId)){
        if (c.isVisible())
          c.setActive(true);
        break;
      }
    }
  }



  /**
   * Sets a preference specifying which frame was selected.
   */

  private void saveSelectedFrame(Session session){
    User user = session.getUser();

    Enumeration e = pluginContainers.elements();
    while (e.hasMoreElements()){
      PluginUIContainer c = (PluginUIContainer)e.nextElement();
      if (c.isActive()){
        String id = c.getId();
        String pluginId = c.getPlugin().getId();

        if (id != null){
          user.getPrefs().setString("selected.plugin", pluginId);
          user.getPrefs().setString("selected.container", id);
        }
        
        break;
      }
    }
  }
  
  

  /**
   * Adds the specified plugin ui container to the table of 
   * <code>pluginContainers</code>. This method should be called by the
   * implementation's <code>createPluginUIContainer</code> method.
   */
  
  protected final void addPluginContainer(Plugin plugin, String id, AbstractPluginUIContainer container){
    if (id != null){
      Object key = new Pair(plugin.getId(), id);
      Object oldContainer = pluginContainers.put(key, container);
      
      if (oldContainer != null){
        pluginContainers.put(key, oldContainer);
        throw new IllegalArgumentException("Cannot allocate a container with the same id twice");
      }
    }
    
    Object [] listeners = listenerList.getListenerList();
    for (int i = 0; i < listeners.length; i += 2){
      if (listeners[i] == PluginUIContainerCreationListener.class){
        PluginUIContainerCreationListener listener = (PluginUIContainerCreationListener)listeners[i+1];
        listener.pluginContainerAdded(container);
      }
    }
  }
  
  
  
  /**
   * Returns the plugin ui container with the specified container id, for the
   * plugin with the specified id.
   */
  
  protected PluginUIContainer getPluginUIContainer(String pluginId, String containerId){
    return (PluginUIContainer)pluginContainers.get(new Pair(pluginId, containerId));
  }
  
  
  
  /**
   * Returns a list of existing plugin ui containers.
   */
  
  public Enumeration getExistingPluginUIContainers(){
    return pluginContainers.elements();
  }
  
  
  
  
  /**
   * Adds a <code>PluginUIContainerCreationListener</code> to be notified when a new
   * plugin UI container is created.
   */
  
  public void addPluginUIContainerCreationListener(PluginUIContainerCreationListener listener){
    listenerList.add(PluginUIContainerCreationListener.class, listener);
  }
  
  
  
  /**
   * Removes a <code>PluginUIContainerCreationListener</code> from being notified when a
   * new plugin UI container is created.
   */
  
  public void removePluginUIContainerCreationListener(PluginUIContainerCreationListener listener){
    listenerList.remove(PluginUIContainerCreationListener.class, listener);
  }
  
  

  /**
   * Disposes of all the plugin containers and removes them from the
   * <code>pluginContainers</code> hashtable.
   */
  
  protected void removePluginContainers(){
    Enumeration e = pluginContainers.elements();
    while (e.hasMoreElements())
      ((PluginUIContainer)e.nextElement()).dispose();

    pluginContainers.clear();
  }

  
  
  
  /**
   * Restores the geometry of the specified window from the preferences.
   */
   
  public static void restoreWindowGeometry(Preferences prefs, Window window, 
      String prefNamePrefix, RectDouble defaultFrameBounds){
    
    Dimension screenSize = AWTUtilities.getUsableScreenBounds().getSize();
    
    // Restore bounds
    RectDouble relativeFrameBounds = prefs.getRectDouble(prefNamePrefix + "boundsRelative", null);
    Rectangle oldFrameBounds = prefs.getRect(prefNamePrefix + "bounds", null);
    if (relativeFrameBounds == null){ 
      if (oldFrameBounds != null) // Compatibility with non-relative bounds
        relativeFrameBounds = new RectDouble(oldFrameBounds).scale(1d/screenSize.width, 1d/screenSize.height);
      else
        relativeFrameBounds = defaultFrameBounds;
    }
    Rectangle realFrameBounds = relativeFrameBounds.scale(screenSize.width, screenSize.height).toRect();
    if (!windowBoundsOk(screenSize, realFrameBounds))
      realFrameBounds = defaultFrameBounds.scale(screenSize.width, screenSize.height).toRect();
    window.setBounds(realFrameBounds);

    if (window instanceof Frame){
      Frame frame = (Frame)window;
      
      // Restore maximized state 
      boolean vertMaximized = prefs.getBool(prefNamePrefix + "maximized.vert", false);
      boolean horizMaximized = prefs.getBool(prefNamePrefix + "maximized.horiz", false);
      
      // Bugfix for Java bug 4464714 - setExtendedState only works once the
      // the window is realized.
      if (frame.getPeer() == null)
        frame.addNotify();
     
      int state = ((vertMaximized ? Frame.MAXIMIZED_VERT : 0) | (horizMaximized ? Frame.MAXIMIZED_HORIZ : 0));
      AWTUtilities.setExtendedFrameState(frame, state);
    }
  }
  
  
  
  /**
   * Saves the geometry of the specified window into the preferences
   * with preference names prefixed with the specified string.
   */

  public static void saveWindowGeometry(Preferences prefs, Window window, String prefNamePrefix){
    Dimension screenSize = AWTUtilities.getUsableScreenBounds().getSize();
    
    // Save bounds
    Point frameLocation = window.isVisible() ? window.getLocationOnScreen() : window.getLocation();
    Dimension frameSize = window.getSize();
    
    RectDouble frameBounds = new RectDouble(frameLocation.x, frameLocation.y, frameSize.width, frameSize.height);
    frameBounds.scale(1d/screenSize.width, 1d/screenSize.height);
    prefs.setRectDouble(prefNamePrefix + "boundsRelative", frameBounds);
    
    if (window instanceof Frame){
      Frame frame = (Frame)window;
      
      // Save maximized state
      int state = AWTUtilities.getExtendedFrameState(frame);
      prefs.setBool(prefNamePrefix + "maximized.vert", (state & Frame.MAXIMIZED_VERT) != 0);
      prefs.setBool(prefNamePrefix + "maximized.horiz", (state & Frame.MAXIMIZED_HORIZ) != 0);
    }
  }
  
  

  /**
   * Returns whether the specified window bounds are reasonably placed on a
   * screen of the specified dimensions. This is used to avoid situations where
   * a window is displayed outside of the screen where the user can't change its
   * size and/or move it (can happen for example if the resolution is changed
   * between runs).
   */

  public static boolean windowBoundsOk(Dimension screenSize, Rectangle windowBounds){
    if (windowBounds.x + windowBounds.width < 50)
      return false;
    if (windowBounds.y < -10)
      return false;
    if (windowBounds.width < 30)
      return false;
    if (windowBounds.height < 40)
      return false;
    if (windowBounds.x > screenSize.width - 10)
      return false;
    if (windowBounds.y > screenSize.height - 20)
      return false;

    return true;
  }
  
  
  
}
