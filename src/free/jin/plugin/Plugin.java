/**
 * Jin - a chess client for internet chess servers.
 * More information is available at http://www.hightemplar.com/jin/.
 * Copyright (C) 2002 Alexander Maryanovsky.
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

import free.jin.*;
import javax.swing.JMenu;
import javax.swing.JPanel;


/**
 * The superclass of all Plugins. Usually, a plugin only needs to override the
 * start() and stop() methods.
 */


public abstract class Plugin{



  /**
   * The PluginContext of this Plugin.
   */

  private PluginContext context = null;




  /**
   * Sets this plugin's context to the given PluginContext.
   */

  public void setContext(PluginContext context) throws UnsupportedContextException{
    if (this.context!=null)
      throw new IllegalStateException("Already has a PluginContext");

    this.context = context;
  }




  /**
   * Returns the PluginContext of this Plugin.
   */

  public PluginContext getPluginContext(){
    return context;
  }



  
  /**
   * Returns the value of the given User property, or null if the User has no
   * such property.
   */

  public String getUserProperty(String propertyName){
    return getUser().getProperty(propertyName);
  }





  /**
   * Returns the value of the given Plugin property, or null if the Plugin has
   * no such property.
   */

  public String getPluginProperty(String propertyName){
    return context.getProperty(propertyName);
  }




  /**
   * Returns the value of the User property with a name equal to the plugin 
   * id plus "." plus the given string. If such a property does not exist,
   * the value of the plugin property with a name equal to the given string
   * is returned.
   */

  public String getProperty(String propertyName){
    String val = getUserProperty(getID()+"."+propertyName);
    return val == null ? getPluginProperty(propertyName) : val;
  }





  /**
   * Returns the value of the user property with a name equal to the plugin 
   * id plus "." plus the given string. If such a property does not exist,
   * the value of the plugin property with a name equal to the given string
   * is returned. If property doesn't exist either, the given default value is
   * returned.
   */

  public String getProperty(String propertyName, String defaultValue){
    String val = getProperty(propertyName);
    return val == null ? defaultValue : val;
  }




  /**
   * Looks up and returns a value for the property with the given name. First
   * the property with the full given name is searched, if that is not found,
   * a property whose name is equal to the given name up to the last '.' is
   * looked up. This is repeated recursively until either the property exists
   * or there are no more '.' characters remaining in the property name (and that
   * property doesn't exist either). Returns null if the look up didn't find
   * any values. For example, looking up a property named "channel-foreground.atell"
   * will first try to find a property named just that, if that's not found, then 
   * one named "channel-foreground" and if that is not found, it will return null.
   */

  public String lookupProperty(String propertyName){
    String propertyValue = getProperty(propertyName);
    if (propertyValue==null){
      int dotIndex = propertyName.lastIndexOf(".");
      if (dotIndex==-1)
        return null;
      return lookupProperty(propertyName.substring(0,dotIndex));
    }
    else
      return propertyValue;
  }





  /**
   * Sets the user property with the name equal to the plugin id plus "." plus 
   * the given property name to the given value. The <code>userTriggered</code>
   * parameter specifies whether this is a property change triggered explicitly
   * by the user.
   */

  public void setProperty(String propertyName, String propertyValue, boolean userTriggered){
    getUser().setProperty(getID()+"."+propertyName, propertyValue, userTriggered);
  }





  /**
   * Returns the id of this plugin. This method is just an alias for
   * context.getProperty("id").
   */

  public String getID(){
    return context.getProperty("id");
  }




  /**
   * Returns the name of this plugin. This method is just an alias for
   * context.getProperty("name"). This is probably something that can be displayed
   * to the user
   */

  public String getName(){
    return context.getProperty("name");
  }




  /**
   * Returns the User this Plugin is working for.
   */                                           

  public User getUser(){
    return context.getUser();
  }




  /**
   * Returns the connection to the server.
   */

  public JinConnection getConnection(){
    return context.getConnection();
  }



  /**
   * Tells the Plugin it should start doing whatever it should be doing.
   * The default implementation does nothing.
   */

  public void start(){
    
  }




  /**
   * This method return the menu for this plugin. This menu will usually be put
   * on the menubar, but it can be ignored or put elsewhere depending on the
   * context. If the plugin has no menu, this method should return 
   * <code>null</code>. The default implementation returns <code>null</code>.
   * This method is guaranteed to be called after the <code>start()</code>
   * method returns.
   */

  public JMenu createPluginMenu(){
    return null;
  }





  /**
   * This method should return a JPanel containing UI which allows customizing
   * this plugin's functionality and/or setting its preferences.
   * The <code>hasPreferencesUI()</code> method specifies whether this plugin
   * has a preferences UI. If not, this method will not be called.
   * The default implementation returns <code>null</code>.
   */

  public PreferencesPanel getPreferencesUI(){
    return null;
  }




  /**
   * Specifies whether this plugin has a UI to modify its preferences. If this
   * method returns <code>true</code>, the <code>createPreferencesUI</code>
   * method may not return <code>null</code>. The default implementation returns
   * <code>false</code>.
   */

  public boolean hasPreferencesUI(){
    return false;
  }



  /**
   * Tells the Plugin it should stop doing whatever it's doing and cleanup.
   * The default implementation does nothing.
   */

  public void stop(){

  }

}