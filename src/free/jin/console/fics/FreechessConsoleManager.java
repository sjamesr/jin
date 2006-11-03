/**
 * Jin - a chess client for internet chess servers.
 * More information is available at http://www.jinchess.com/.
 * Copyright (C) 2002, 2003 Alexander Maryanovsky.
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

package free.jin.console.fics;

import free.jin.console.Console;
import free.jin.console.ConsoleManager;
import free.jin.event.ChatEvent;
import free.jin.ui.PreferencesPanel;


/**
 * An extension of the default ConsoleManager for the freechess.org server.
 */

public class FreechessConsoleManager extends ConsoleManager{



  /**
   * Creates a FreechessConsole.
   */

  protected Console createConsole(){
    return new FreechessConsole(this);
  }




  /**
   * Overrides <code>chatMessageReceived(ChatEvent)</code> to notify the
   * <code>Console</code> when tells are received.
   */

  public void chatMessageReceived(ChatEvent evt){
    super.chatMessageReceived(evt);

    if (isPaused())
      return;
    
    String type = evt.getType();
    if (type.equals("tell") || type.equals("say") || type.equals("ptell"))
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
    String rating = evt.getSenderRating() == -1 ? "----" : String.valueOf(evt.getSenderRating());
    String message = evt.getMessage();
    Object forum = evt.getForum();
    
    Object [] args = new Object[]{sender, title, rating, String.valueOf(forum), message};
    return getI18n().getFormattedString(type + ".displayPattern", args);
  }
  
  
  
  /**
   * Returns the FICS encoding, which is 7-bit ASCII.
   */
  
  public String getEncoding(){
    return "ASCII";
  }
  
  
  
  /**
   * Returns <code>false</code>. 
   */
  
  public boolean supportsMultipleEncodings(){
    return false;
  }
  


  /**
   * Return a PreferencesPanel for changing the console manager's settings.
   */

  public PreferencesPanel getPreferencesUI(){
    return new FreechessConsolePrefsPanel(this);
  }


}
