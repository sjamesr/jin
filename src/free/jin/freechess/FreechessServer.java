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

package free.jin.freechess;

import free.jin.AbstractServer;
import free.jin.Connection;
import free.jin.ConnectionDetails;
import free.jin.UsernamePolicy;


/**
 * The <code>Server</code> implementation representing the freechess.org server.
 * See http://www.freechess.org for more information.
 */

public class FreechessServer extends AbstractServer{


  
  /**
   * Creates the username policy.
   */

  protected UsernamePolicy createUsernamePolicy(){
    return new UsernamePolicy(){

      public boolean isSame(String username1, String username2){
        return username1.equalsIgnoreCase(username2);
      }

      public String invalidityReason(String username){
        int usernameLength = username.length();
        if ((usernameLength < 3) || (usernameLength > 17))
          return getI18n().getString("usernameLengthErrorMessage");

        for (int i = 0; i < usernameLength; i++){
          int c = username.charAt(i);
          if (!((c >= 97) && (c <= 122) || // Lowercase characters.
                (c >= 65) && (c <= 90)))    // Uppercase characters.
            return getI18n().getFormattedString("usernameIllegalCharacterErrorMessage", new Object[]{"" + c});
        }

        return null;
      }

      public String getGuestUsername(){
        return "guest";
      }

    };
  }



  /**
   * Creates and returns a new <code>JinFreechessConnection</code>.
   */

  public Connection createConnection(ConnectionDetails connDetails){
    return new JinFreechessConnection(connDetails.getUsername(), connDetails.getPassword());
  }



}