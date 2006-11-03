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

package free.jin.console.icc;

import free.jin.console.Console;
import free.jin.console.ConsoleManager;
import free.jin.event.ChatEvent;
import free.jin.ui.PreferencesPanel;


/**
 * An extension of the default ConsoleManager for the chessclub.com server.
 */

public class ChessclubConsoleManager extends ConsoleManager{



  /**
   * Overrides <code>chatMessageReceived(ChatEvent)</code> to notify the
   * <code>Console</code> when tells are received.
   */

  public void chatMessageReceived(ChatEvent evt){
    super.chatMessageReceived(evt);
    
    if (isPaused())
      return;

    String type = evt.getType();
    if (type.equals("tell") || type.equals("say") || type.equals("atell") || type.equals("ptell"))
      console.tellReceived(evt.getSender());
  }

  
  /**
   * Returns the string that should be displayed according to the given
   * ChatEvent.
   */

  protected String translateChat(ChatEvent evt){
    String type = evt.getType();
    String sender = evt.getSender();
    String title = evt.getSenderTitle();
    String message = decode(evt.getMessage());
    Object forum = evt.getForum();
    
    if ("qtell".equals(type))
      return parseQTell(evt);
    else if ("channel-qtell".equals(type))
      return parseChannelQTell(evt);

    Object [] args = new Object[]{sender, title, String.valueOf(forum), message};
    return getI18n().getFormattedString(type + ".displayPattern", args);
  }




  /**
   * Creates a ChessclubConsole.
   */

  protected Console createConsole(){
    return new ChessclubConsole(this);
  }




  /**
   * Returns a string that should be displayed for the given ChatEvent when the
   * ChatEvent contains a qtell.
   */

  private String parseQTell(ChatEvent evt){
    String message = evt.getMessage();
    int index;
    while ((index = message.indexOf("\\n")) != -1)
      message = message.substring(0, index) + "\n:" + message.substring(index + 2);
    while ((index = message.indexOf("\\h")) != -1)
      message = message.substring(0, index) + message.substring(index + 2);
    while ((index = message.indexOf("\\H")) != -1)
      message = message.substring(0, index) + message.substring(index + 2);
    while ((index = message.indexOf("\\b")) != -1)
      message = message.substring(0, index) + message.substring(index + 2);
    return ":" + message;
  }
  
  
  
  /**
   * Returns a string that should be displayed for the given ChatEvent when the
   * ChatEvent contains a channel qtell.
   */

  private String parseChannelQTell(ChatEvent evt){
    String message = evt.getMessage();
    Object forum = evt.getForum();
    int index;
    while ((index = message.indexOf("\\n")) != -1)
      message = message.substring(0, index) + "\n" + forum + ">" + message.substring(index + 2);
    while ((index = message.indexOf("\\h")) != -1)
      message = message.substring(0, index) + message.substring(index + 2);
    while ((index = message.indexOf("\\H")) != -1)
      message = message.substring(0, index) + message.substring(index + 2);
    while ((index = message.indexOf("\\b")) != -1)
      message = message.substring(0, index) + message.substring(index + 2);
    return forum + ">" + message;
  }



  /**
   * Returns <code>true</code>. 
   */
  
  public boolean supportsMultipleEncodings(){
    return true;
  }
  


  /**
   * Return a <code>PreferencesPanel</code> for changing the console
   * manager's settings.
   */

  public PreferencesPanel getPreferencesUI(){
    return new ChessclubConsolePrefsPanel(this);
  }


}
