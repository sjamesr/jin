/**
 * Jin - a chess client for internet chess servers.
 * More information is available at http://www.jinchess.com/.
 * Copyright (C) 2004 Alexander Maryanovsky.
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

import javax.swing.*;
import java.util.Properties;
import java.io.IOException;
import free.jin.plugin.PluginInfo;



/**
 * An applet based implementation of JinContext.
 */

public class JinApplet extends JApplet implements JinContext{

  
  
  /**
   * The application properties.
   */
  
  private final Properties appProps;
  
  
  
  /**
   * User preferences.
   */

  private Preferences userPrefs;
  
  
  
  /**
   * Creates a new instance of <code>JinApplet</code>.
   */
  
  public JinApplet() throws IOException{
    
    // Load application properties.
    appProps = JinUtilities.loadAppProps();
  }
  
  

  /**
   * Initializes Jin.
   */
  
  public void init(){
    
    // Load user's preferences.
    userPrefs = Preferences.load(this, "user");
  }
  
  
  
  /**
   * Starts Jin.
   */
  
  public void start(){
    try{
      userPrefs.save(System.out);
    } catch (java.io.IOException e){}
  }
  
  
  /**
   * 
   */
  
  public Preferences getPrefs(){
    return null;
  }
  

  /**
   * 
   */

  public ClassLoader [] loadResources(String resourceType){
    return null;
  }



  /**
   * 
   */
  
  public void quit(boolean askToConfirm){
  }



  /**
   * Returns the application name.
   */

  public String getAppName(){
    return appProps.getProperty("app.name");
  }



  /**
   * Returns the application version.
   */

  public String getAppVersion(){
    return appProps.getProperty("app.version");
  }



  /**
   * 
   */
  
  public Server [] getServers(){
    return null;
  }



  /**
   * 
   */
  
  public PluginInfo [] getPlugins(Server server){
    return null;
  }



  /**
   * 
   */
  
  public ListModel getUsers(){
    return null;
  }



  /**
   * 
   */
  
  public boolean addUser(User user){
    return false;
  }



  /**
   * 
   */
  
  public boolean storeUser(User user){
    return false;
  }



  /**
   * 
   */

  public boolean removeUser(User user){
    return false;
  }



  /**
   * 
   */
  
  public UIProvider getUIProvider(){
    return null;
  }



  /**
   * 
   */
  
  public ConnectionManager getConnManager(){
    return null;
  }
  

  
}
