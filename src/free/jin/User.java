/**
 * Jin - a chess client for internet chess servers.
 * More information is available at http://www.jinchess.com/.
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

package free.jin;

import java.io.*;
import java.util.Properties;
import free.util.Utilities;


/**
 * A holder for the user's settings and preferences. Throughout the documentation,
 * the actual user is referred to as "user" (lowercase 'u') and a User object is
 * referred to as "User" (uppercase 'u'). 
 */

public class User{



  /**
   * The server of this user.
   */

  private final Server server;



  /**
   * The properties of the User.
   */

  private final Properties props;



  
  /**
   * Has any of the properties of this User been modified explicitly by the user
   * since it was loaded or last saved.
   */

  private boolean isUserModified = false;




  /**
   * Creates a new <code>User</code> with the specified <code>Server</code> and
   * properties.
   */

  User(Server server, Properties props){
    if (server == null)
      throw new IllegalArgumentException("The specified Server object may not be null");
    if (props == null)
      throw new IllegalArgumentException("The specified Properties object may not be null");

    this.server = server;
    this.props = props;
  }




  /**
   * Loads and returns a <code>User</code> object from the specified
   * InputStream.
   */

  static User read(Server server, InputStream in) throws IOException{
    Properties props = new Properties();
    props.load(in);
    return new User(server, props);
  }




  /**
   * Writes this User into the given OutputStream. The <code>isModified</code> 
   * flag of this User is <B>not</B> cleared.
   */

  void write(OutputStream out) throws IOException{
    props.save(out, getUsername()+"'s properties for "+getServer().getLongName());
  }





  /**
   * Returns true if any of the properties of this User have been modified
   * explicitly by the user since it was loaded or saved. This is used for
   * example to determine whether to ask the user whether to save the User into
   * a file.
   */

  public boolean isUserModified(){
    return isUserModified;
  }




  /**
   * Clears the <code>isUserModified</code> flag.
   */

  void clearUserModified(){
    isUserModified = false;
  }
  



  /**
   * Returns the value of the property of the user with the given name or null
   * if the user has no property with the given name.
   */

  public String getProperty(String propertyName){
    String propertyValue = props.getProperty(propertyName);
    return propertyValue == null ? getServer().getProperty(propertyName) : propertyValue;
  }




  /**
   * Returns the value of the property of the user with the given name or the 
   * given default value if the user has no property with the given name.
   */

  public String getProperty(String propertyName, String defaultValue){
    String propertyValue = getProperty(propertyName);
    return propertyValue == null ? defaultValue : propertyValue;
  }





  /**
   * Returns the username of this user - the value of the "login.username"
   * property, or the string "guest" if the user is a guest.
   */

  public String getUsername(){
    String username = getProperty("login.username");
    return username == null ? "guest" : username;
  }




  /**
   * Returns <code>true</code> if this <code>User</code> object represents a
   * guest.
   */

  public boolean isGuest(){
    return (getProperty("login.username") == null);
  }




  /**
   * Sets this user's property with the given name to the given value.
   * The change is considered "user triggered".
   * 
   * @param propertyName The name of the property.
   * @param propertyValue The new value of the property.
   */

  public void setProperty(String propertyName, String propertyValue){
    setProperty(propertyName, propertyValue, true);
  }




  /**
   * Sets this user's property with the given name to the given value.
   * 
   * @param propertyName The name of the property.
   * @param propertyValue The new value of the property.
   * @param userChange Whether this is a change of property triggered explicitly
   * by the user.
   */

  public void setProperty(String propertyName, String propertyValue, boolean userChange){
    Object oldValue = props.put(propertyName, propertyValue);
    if (userChange && !Utilities.areEqual(oldValue, propertyValue))
      isUserModified = true;
  }


 


  /**
   * Returns the Server object represeting the serverthis user logs on to.
   */

  public Server getServer(){
    return server;
  }




  /**
   * Returns a textual representation of the user.
   */

  public String toString(){
    return "User[server=\""+getServer()+"\",login.username="+getUsername()+"]";
  } 

}