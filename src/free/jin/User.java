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

package free.jin;

import java.util.Properties;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.*;
import free.util.ImmutableProperties;
import free.util.IOUtilities;



/**
 * A holder for the user's settings and preferences. Throughout the documentation,
 * the actual user is referred to as "user" (lowercase 'u') and a User object is
 * referred to as "User" (uppercase 'u').
 */

public class User{



  /**
   * The properties of the User.
   */

  private final Properties props;



  /**
   * The name of the file from which this User was loaded or into which this
   * user was last saved.
   */

  private String filename;




  /**
   * The array containing binary data from which the User was loaded. This
   * is needed to be able to reset the User into its last kept state.
   */

  private byte [] data;



  
  /**
   * Has any of the properties of this User been modified explicitly by the user
   * since it was loaded or last saved.
   */

  private boolean isUserModified = false;





  /**
   * Creates a new User from the given File.
   */

  private User(File file) throws IOException{
    FileInputStream in = new FileInputStream(file);
    ByteArrayOutputStream buf = new ByteArrayOutputStream();
    IOUtilities.pump(in,buf);
    in.close();
    this.data = buf.toByteArray();
    this.props = new Properties();
    props.load(new ByteArrayInputStream(data));
    this.filename = file.getName();
  }




  /**
   * Loads a new User from the given InputStream.
   */

  public User(InputStream in) throws IOException{
    ByteArrayOutputStream buf = new ByteArrayOutputStream();
    IOUtilities.pump(in,buf);
    in.close();
    this.data = buf.toByteArray();
    this.props = new Properties();
    props.load(new ByteArrayInputStream(data));
  }




  /**
   * Creates a new User which is exactly like the given User.
   */

  public User(User source){
    props = (Properties)source.props.clone();
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    props.save(out, "");
    data = out.toByteArray();
  }





  /**
   * Loads a User object from the given File.
   */

  public static User load(File file) throws IOException{
    return new User(file);
  }




  /**
   * Writes this User into the given OutputStream. The <code>isModified</code> 
   * flag of this User is <B>not</B> cleared.
   */

  public void write(OutputStream out) throws IOException{
    props.save(out,getProperty("login.username")+"'s properties for "+getProperty("server"));
  }




  /**
   * Saves this User into the given File. The <code>isUserModified</code>
   * flag of this User is cleared and the filename is kept.
   */

  public void save(File file) throws IOException{
    OutputStream out = new FileOutputStream(file);
    write(out);
    out.close();
    ByteArrayOutputStream buf = new ByteArrayOutputStream();
    write(buf);
    data = buf.toByteArray();
    isUserModified = false;
    filename = file.getName();
  }




  /**
   * Restores this user's last saved state (or its state when it was first loaded).
   * The <code>isModified</code> flag of this User is cleared.
   */

  public void restore(){
    try{
      props.load(new ByteArrayInputStream(data));
      isUserModified = false;
    } catch (IOException e){}
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
   * Returns the name of the file from which this User was loaded or into which
   * it was last saved. Returns null if this is a new user.
   */

  public String getFilename(){
    return filename;
  }





  /**
   * Returns this User's properties. Absolutely no modification is allowed to
   * the returned Properties.
   */

  public Properties getProperties(){
    return new ImmutableProperties(props);
  }




  /**
   * Returns the value of the property of the user with the given name or null
   * if the user has no property with the given name.
   */

  public String getProperty(String propertyName){
    return props.getProperty(propertyName);
  }




  /**
   * Returns the value of the property of the user with the given name or the 
   * given default value if the user has no property with the given name.
   */

  public String getProperty(String propertyName, String defaultValue){
    return props.getProperty(propertyName, defaultValue);
  }





  /**
   * Returns the username of this user - the value of the "login.username"
   * property.
   */

  public String getUsername(){
    return getProperty("login.username");
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
    if (userChange&&!equal(oldValue,propertyValue))
      isUserModified = true;
  }



  
  /**
   * Returns true if the 2 given references reference equal values.
   */

  private static boolean equal(Object o1, Object o2){
    if (o1==null)
      if (o2==null)
        return true;
      else
        return false;
    else 
      return (o1.equals(o2));
  }



  /**
   * Returns the Server object represeting the serverthis user logs on to.
   */

  public Server getServer(){
    return Jin.getServer(getProperty("server"));
  }



  /**
   * Returns true if this User and the given User represent the same User.
   * Two Users are the same if their username and servers are equal (usernames
   * are compared case insensitively). Note that two Users with
   * <code>null</code> usernames are not equal (they are both guests).
   * Consequently, a User with a <code>null</code> username does not equal any
   * other User.
   */

  public boolean equals(Object o){
    if (!(o instanceof User))
      return false;

    User user = (User)o;
    if ((getUsername()==null)||(user.getUsername()==null))
      return false;
    return getUsername().equalsIgnoreCase(user.getUsername())&&getServer().equals(user.getServer());
  }




  /**
   * Returns the hashcode of this User.
   */

  public int hashCode(){
    return getUsername().hashCode()*getServer().hashCode();
  }



  /**
   * Returns a textual representation of the user.
   */

  public String toString(){
    return "User[server="+getProperty("server")+",login.username="+getProperty("login.username")+"]";
  } 

}