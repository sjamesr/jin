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
import java.io.InputStream;
import java.io.IOException;
import java.io.File;
import java.io.FileInputStream;


/**
 * This class encapsulates the various properties of a chess server. I haven't
 * yet decided whether it should be solely used as is and backed up by a 
 * properties file or subclassed by various server specific implementations.
 */

public class Server{

  
  /**
   * The properties of this server.
   */

  private final Properties props = new Properties();




  /**
   * Creates a new Server object.
   */

  protected Server(){

  }



  /**
   * Loads a Server object from the given InputStream.
   */

  public static Server load(InputStream serverIn) throws IOException{
    Server server = new Server();
    server.props.load(serverIn);

    return server;
  }




  /**
   * Returns a copy of the default user for this server.
   */

  public User createDefaultUser(){
    return new User(this);
  }




  /**
   * Creats and returns a new LoginDialog for this Server.
   */

  public LoginDialog createLoginDialog(){
    try{
      String dialogClassname = getProperty("login.dialog.classname");
      return (LoginDialog)Class.forName(dialogClassname).newInstance();
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
   * Returns this server's property with the given name or null if no such
   * property exists for this server.
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
