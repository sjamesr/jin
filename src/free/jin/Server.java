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


/**
 * Defines the interface that needs to be implemented in order to support a
 * chess server.
 */

public interface Server{



  /**
   * This method is called exactly once, immediately after the
   * <code>Server</code> object is created to tell the Server of the guest 
   * account preferences. The specified argument will be <code>null</code> if
   * the guest account hasn't been used yet (and has no preferences to speak
   * of).
   */

  void setGuestUser(User user);



  /**
   * Creates and returns an unconnected <code>Connection</code> object by using
   * the specified connection details.
   */

  Connection createConnection(ConnectionDetails connDetails);



  /**
   * Returns the <code>User</code> object representing the guest account on this
   * server.
   */

  User getGuest();



  /**
   * Returns the hostname of the default host for this server. May not be
   * <code>null</code>.
   */

  String getDefaultHost();
  
  
  
  /**
   * Returns the list of hostnames of all the hosts for this server. This may
   * not be <code>null</code> or empty.
   */

  String [] getHosts();



  /**
   * Sets the host to which to connect. This overrides the default settings of
   * the server and forces <code>getDefaultHost</code> to return the argument
   * passed to this method and <code>getHosts</code> to return an array
   * containing a single element - the argument passed to this method. This is
   * used when Jin is run in applet mode to force connection to the originating
   * host of the applet.
   */
   
  void setHost(String host);



  /**
   * Returns a list of the ports on which the server is listening, in the order
   * they should be tried.
   */

  int [] getPorts();
  
  
  
  /**
   * Sets the port on which to connect to this server. This overrides the
   * default settings of the server and forces <code>getPorts</code> to return
   * an array with a single element - the value passed to this function.
   */
   
  void setPort(int port);



  /**
   * Returns an ID of this server. This should be a short, lowercase only string
   * without any whitespace. It should also be unique between all servers. It is
   * not necessary (although preferable) for it to be descriptive.
   */

  String getId();



  /**
   * Returns the name of this server. The name is a descriptive string that can
   * be displayed to the user but is not necessarily the full name of the
   * server. The Free Internet Chess Server would be called here "FICS", for
   * example.
   */

  String getShortName();



  /**
   * Returns the server's full name. Example: "Internet Chess Club".
   */

  String getLongName();



  /**
   * Returns the URL of the server's website.
   */

  String getWebsite();



  /**
   * Returns the URL of the server's registration page, or <code>null</code>
   * if none exists.
   */

  String getRegistrationPage();



  /**
   * Returns the URL of server's password retrieval page, or <code>null</code>
   * if none exists.
   */

  String getPasswordRetrievalPage();



  /**
   * Returns the username policy of this server.
   */

  UsernamePolicy getUsernamePolicy();



}
