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

import java.io.IOException;
import java.util.Properties;
import javax.swing.ListModel;
import free.util.IOUtilities;


/**
 * Various Jin related utilities.
 */

public class JinUtilities{
  
  
  
  /**
   * Loads and returns the application properties.
   */
  
  static Properties loadAppProps() throws IOException{
    return IOUtilities.loadProperties(JinUtilities.class.getResourceAsStream("resources/app.props"));
  }



  /**
   * Returns the server with the specified id. Returns <code>null</code> if no
   * such server found.
   */

  public static Server getServerById(JinContext context, String id){
    Server [] servers = context.getServers();
    for (int i = 0; i < servers.length; i++)
      if (servers[i].getId().equals(id))
        return servers[i];

    return null;
  }



  /**
   * Returns the user with the specified username on the specified server or
   * <code>null</code> if no such user exists.
   */

  public static User getUser(JinContext context, Server server, String username){
    ListModel users = context.getUsers();
    for (int i = 0; i < users.getSize(); i++){
      User user = (User)users.getElementAt(i);
      if ((user.getServer() == server) && 
          server.getUsernamePolicy().isSame(username, user.getUsername()))
        return user;
    }

    return null;
  }



  /**
   * Returns whether the specified User represents a "known" account, that is,
   * it appears in the list returned by <code>context.getUsers()</code>.
   */

  public static boolean isKnownUser(JinContext context, User user){
    ListModel users = context.getUsers();
    for (int i = 0; i < users.getSize(); i++)
      if (users.getElementAt(i).equals(user))
        return true;

    return false;
  }



}