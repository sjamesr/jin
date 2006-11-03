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

package free.jin.chessclub;

import free.jin.AbstractServer;
import free.jin.Connection;
import free.jin.ConnectionDetails;
import free.jin.UsernamePolicy;


/**
 * The <code>Server</code> implementation representing the chessclub.com server.
 * See http://www.chessclub.com for more information.
 */

public class ChessclubServer extends AbstractServer{



  /**
   * Creates and teturns the username policy.
   */

  protected UsernamePolicy createUsernamePolicy(){
    return new UsernamePolicy(){
      
      private boolean isLetter(int c){
        return ((c >= 97) && (c <= 122)) || ((c >= 65) && (c <= 90));
      }
      
      private boolean isDigit(int c){
        return (c >= 48) && (c <= 57);
      }

      public boolean isSame(String username1, String username2){
        return username1.equalsIgnoreCase(username2);
      }

      public String invalidityReason(String username){
        int usernameLength = username.length();
        if ((usernameLength < 2) || (usernameLength > 15))
          return getI18n().getString("usernameLengthErrorMessage");

        int firstChar = username.charAt(0);
        if (!isLetter(firstChar))
          return getI18n().getString("usernameBadFirstCharacterErrorMessage");
        
        int lastChar = username.charAt(username.length() - 1);
        if (lastChar == '-')
          return getI18n().getString("usernameLastCharHyphenErrorMessage");

        boolean hasHyphenAppeared = false;
        for (int i = 0; i < usernameLength; i++){
          char c = username.charAt(i);
          if (c == '-'){
            if (hasHyphenAppeared)
              return getI18n().getString("usernameMultipleHyphensErrorMessage");
            else
              hasHyphenAppeared = true;
          }

          int val = c;
          if (!(isLetter(val) || isDigit(val) || (c == '-')))
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
   * Creates and returns a new <code>JinChessclubConnection</code>.
   */

  public Connection createConnection(ConnectionDetails connDetails){
    return new JinChessclubConnection(connDetails.getUsername(), connDetails.getPassword());
  }



}