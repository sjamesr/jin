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

import java.util.Properties;
import java.io.InputStream;
import java.io.IOException;
import java.io.File;
import java.io.FileInputStream;


/**
 * This class encapsulates the various properties of a chess server.
 */

public abstract class Server{

  
  /**
   * The properties of this server.
   */

  private Properties props;




  /**
   * Creates a new Server object.
   */

  protected Server(){

  }



  /**
   * Loads a Server object from the given InputStream.
   */

  public static Server load(InputStream serverIn) throws IOException{
    Properties props = new Properties();
    props.load(serverIn);

    try{
      String classname = props.getProperty("classname");
      Server server = (Server)Class.forName(classname).newInstance();
      server.props = props;
      return server;
    } catch (InstantiationException e){
        e.printStackTrace();
      }
      catch (IllegalAccessException e){
        e.printStackTrace();
      }
      catch (ClassNotFoundException e){
        e.printStackTrace();
      }

    return null;
  }




  /**
   * Returns a <code>User</code> for this Server with the specified
   * <code>Properties</code>.
   */

  public User createUser(Properties props){
    return new User(this, props);
  }



  /**
   * Creates a guest <code>User</code>.
   */

  public User createGuest(){
    return new User(this, new Properties());
  }




  /**
   * Creates and returns a new <code>LoginDialog</code> for this
   * <code>Server</code>.
   */

  public abstract LoginDialog createLoginDialog();



  /**
   * Creates and returns a new <code>LoginDialog</code> for this
   * <code>Server</code> using the specified <code>User</code> for the dialog
   * default.
   */

  public abstract LoginDialog createLoginDialog(User user);




  /**
   * Returns this server's property with the given name or <code>null</code> if
   * no such property exists for this server.
   */

  public String getProperty(String propertyName){
    return props.getProperty(propertyName);
  }



  /**
   * Returns the name of this server.
   */

  public String getName(){
    return getProperty("name");
  }




  /**
   * Returns a long server name.
   */

  public String getLongName(){
    return getProperty("name.long");
  }



  /**
   * Returns a textual representation of this Server.
   */

  public String toString(){
    return getLongName();
  }

}
