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

package free.jin.chessclub;

import javax.swing.*;
import free.jin.StandardLoginDialog;
import free.jin.JinConnection;
import free.util.BrowserControl;


/**
 * The login dialog for the chessclub.com server.
 */

public class ChessclubLoginDialog extends StandardLoginDialog{



  /**
   * Creates a new ChessclubLoginDialog.
   */

  public ChessclubLoginDialog(){
    super("Chessclub.com Login Information");
  }




  /**
   * Logs in as a guest.
   */

  protected void connectAsGuestActionPerformed(){
    usernameField.setText("guest");
    passwordField.setText("");

    String inputIllegalityReason = findInputIllegalityReason();
    if (inputIllegalityReason!=null){
      JOptionPane.showMessageDialog(dialog, inputIllegalityReason, "Wrong Connection Settings", JOptionPane.ERROR_MESSAGE);
      return;
    }

    user = user.getServer().createDefaultUser();

    proceed();
  }




  /**
   * Opens the chessclub.com registration page in a browser.
   */

  protected void registerActionPerformed(){
    String url = "http://www.chessclub.com/register";
    try{
      BrowserControl.displayURL(url);
    } catch (java.io.IOException e){
        JOptionPane.showMessageDialog(dialog, "Unable to display URL: "+url);
      }
  }



  
  /**
   * Opens the chessclub.com password retrival page in a browser.
   */

  protected void forgotPasswordActionPerformed(){
    String url = "http://www.chessclub.com/mailpassword";
    try{
      BrowserControl.displayURL(url);
    } catch (java.io.IOException e){
        JOptionPane.showMessageDialog(dialog, "Unable to display URL: "+url);
      }
  }




  /**
   * Checks that the username is valid.
   */

  public String findInputIllegalityReason(){
    String username = usernameField.getText();
    int usernameLength = username.length();
    if ((usernameLength<2)||(usernameLength>15))
      return "Usernames must be between 2 and 15 characters long";

    boolean hasHyphenAppeared = false;
    for (int i=0;i<usernameLength;i++){
      char c = username.charAt(i);
      if (c=='-')
        if (hasHyphenAppeared)
          return "A username must contain at most one hyphen ('-')";
        else
          hasHyphenAppeared = true;
      if (!isValidUsernameCharacter(c))
        return "Your username contains at least one illegal character: "+c;
    }

    return super.findInputIllegalityReason();
  }




  /**
   * Returns true if the given character is a valid character for a username.
   */

  private boolean isValidUsernameCharacter(char c){
    int val = c;
    if ((val>=97)&&(val<=122)) // Lowercase characters.
      return true;
    if ((val>=65)&&(val<=90)) // Uppercase characters.
      return true;
    if ((val>=48)&&(val<=57)) // Digits
      return true;
    if (c=='-') // Hyphen
      return true;
    return false;
  }




  /**
   * Returns a JinChessclubConnection based on the hostname, port, username and
   * password chosen by the user.
   */

  public JinConnection createConnection(){
    String hostname = (String)hostnameBox.getSelectedItem();
    int port = Integer.parseInt(portField.getText());
    String username = usernameField.getText();
    String password = new String(passwordField.getPassword());
    return new JinChessclubConnection(hostname, port, username, password);
  }


}
