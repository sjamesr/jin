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

package free.jin;

import free.util.Struct;
import free.util.Utilities;


/**
 * A holder for the connection details, such as username, password, hostname,
 * port etc. This is the information the user fills in the login dialog.
 */

public class ConnectionDetails extends Struct{



  /**
   * A private constructor. See the various static <code>createXXX</code>
   * methods for obtaining an instance.
   */

  private ConnectionDetails(){

  }



  /**
   * Creates a new <code>ConnectionDetails</code> object with the specified
   * details.
   *
   * @param server The server to connect to.
   * @param user The account to connect with.
   * @param username The requested username.
   * @param password The password.
   * @param savePassword Whether the password should be saved.
   * @param hostname The hostname of the server to connect to.
   * @param ports A list of ports on which to try connecting.
   */

  public static ConnectionDetails create(Server server, User user, String username,
      String password, boolean savePassword, String hostname, int [] ports){
        
    ConnectionDetails details = new ConnectionDetails();

    details.setProperty("server", server);
    details.setProperty("user", user);
    details.setStringProperty("username", username);
    details.setStringProperty("password", password);
    details.setBooleanProperty("savePassword", savePassword);
    details.setStringProperty("hostname", hostname);
    details.setProperty("ports", ports == null ? null : ports.clone());

    return details;
  }



  /**
   * Creates a new <code>ConnectionDetails</code> object with details for
   * logging in as a guest.
   */

  public static ConnectionDetails createGuest(Server server, String username, 
      String hostname, int [] ports){
        
    ConnectionDetails details = new ConnectionDetails();

    details.setProperty("server", server);
    details.setProperty("user", server.getGuest());
    details.setStringProperty("username", username);
    details.setStringProperty("hostname", hostname);
    details.setProperty("ports", ports.clone());

    return details;
  }



  /**
   * Returns a new <code>ConnectionDetails</code> object which is exactly like
   * this one, but with the specified port used before any other ports. The
   * specified port must be an element of this <code>ConnectionDetail</code>'s
   * ports list.
   */

  public ConnectionDetails usePort(int port){
    int [] ports = getPorts();
    int index = Utilities.indexOf(ports, port);
    if (index == -1)
      throw new IllegalArgumentException("The specified port (" + port + ") is not in the list");

    for (int i = index; i > 0; i--)
      ports[i] = ports[i-1];
    ports[0] = port;

    if (isGuest())
      return createGuest(getServer(), getUsername(), getHost(), ports);
    else
      return create(getServer(), getUser(), getUsername(), getPassword(),
        isSavePassword(), getHost(), ports);
  }


  
  /**
   * Returns the server to which we are to connect.
   */
  
  public Server getServer(){
    return (Server)getProperty("server");
  }
  
  
  
  /**
   * Returns the account with which we are to connect. May be <code>null</code>
   * if a User object hasn't been created for the account yet.
   */
   
  public User getUser(){
    return (User)getProperty("user");
  }
  


  /**
   * Returns whether the created connection should be for a guest account.
   */

  public boolean isGuest(){
    return getUser().isGuest();
  }



  /**
   * Returns the requested username.
   */

  public String getUsername(){
    return getStringProperty("username");
  }



  /**
   * Returns the password for the requested username. A <code>null</code> value
   * implies that the user wishes to input the password himself, while an empty
   * string implies that there is no password.
   */

  public String getPassword(){
    if (isGuest())
      return "";

    return getStringProperty("password");
  }



  /**
   * Returns whether the password should be saved. Throws an
   * <code>IllegalStateException</code> if the created connection should be for
   * a guest account - see {@link #isGuest()}.
   */

  public boolean isSavePassword(){
    if (isGuest())
      throw new IllegalStateException();

    return getBooleanProperty("savePassword");
  }



  /**
   * Returns the hostname of the server to connect to.
   */

  public String getHost(){
    return getStringProperty("hostname");
  }



  /**
   * Returns the list of ports on which to try to connect.
   */

  public int [] getPorts(){
    return (int [])((int [])getProperty("ports")).clone();
  }



}