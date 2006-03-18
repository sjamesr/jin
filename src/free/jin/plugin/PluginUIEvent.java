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

import java.util.EventObject;


/**
 * The event object thrown when a plugin ui container event occurs.
 */

public class PluginUIEvent extends EventObject{



  /**
   * The id for when a plugin ui is made visible.
   */

  public static final int PLUGIN_UI_SHOWN = 1;



  /**
   * The id for when a plugin ui is made invisible.
   */

  public static final int PLUGIN_UI_HIDDEN = 2;



  /**
   * The id for when the user performs the operation that closes the plugin ui.
   */

  public static final int PLUGIN_UI_CLOSING = 3;



  /**
   * The id for when the plugin ui is activated.
   */

  public static final int PLUGIN_UI_ACTIVATED = 4;



  /**
   * The id for when the plugin ui is deactivated.
   */

  public static final int PLUGIN_UI_DEACTIVATED = 5;
  
  
  
  /**
   * The id for when the plugin ui is disposed.
   */
  
  public static final int PLUGIN_UI_DISPOSED = 6;
  
  
  
  /**
   * The id for when the title of the plugin ui changes.
   */
  
  public static final int PLUGIN_UI_TITLE_CHANGED = 7;
  
  
  
  /**
   * The id for when the icon of the plugin ui changes.
   */
  
  public static final int PLUGIN_UI_ICON_CHANGED = 8;



  /**
   * The id of this event.
   */

  private final int id;



  /**
   * Creates a new <code>PluginUIEvent</code> with the specified source
   * <code>PluginUIContainer</code> and event id.
   */

  public PluginUIEvent(PluginUIContainer source, int id){
    super(source);

    switch (id){
      case PLUGIN_UI_SHOWN:
      case PLUGIN_UI_HIDDEN:
      case PLUGIN_UI_CLOSING:
      case PLUGIN_UI_ACTIVATED:
      case PLUGIN_UI_DEACTIVATED:
      case PLUGIN_UI_DISPOSED:
      case PLUGIN_UI_TITLE_CHANGED:
      case PLUGIN_UI_ICON_CHANGED:
        break;
      default:
        throw new IllegalArgumentException("Bad event id: " + id);
    }

    this.id = id;
  }



  /**
   * Returns the source <code>PluginContainerUI</code> of this event.
   */

  public PluginUIContainer getPluginUIContainer(){
    return (PluginUIContainer)getSource();
  }



  /**
   * Returns the id of this event.
   */

  public int getId(){
    return id;
  }



  /**
   * Dispatches this event on the specified listener.
   */

  public void dispatch(PluginUIListener l){
    switch (id){
      case PLUGIN_UI_SHOWN: l.pluginUIShown(this); break;
      case PLUGIN_UI_HIDDEN: l.pluginUIHidden(this); break;
      case PLUGIN_UI_CLOSING: l.pluginUIClosing(this); break;
      case PLUGIN_UI_ACTIVATED: l.pluginUIActivated(this); break;
      case PLUGIN_UI_DEACTIVATED: l.pluginUIDeactivated(this); break;
      case PLUGIN_UI_DISPOSED: l.pluginUIDisposed(this); break;
      case PLUGIN_UI_TITLE_CHANGED: l.pluginUITitleChanged(this); break;
      case PLUGIN_UI_ICON_CHANGED: l.pluginUIIconChanged(this); break;
    }
  }



}