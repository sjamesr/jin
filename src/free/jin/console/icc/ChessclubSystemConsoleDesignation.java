/**
 * Jin - a chess client for internet chess servers.
 * More information is available at http://www.jinchess.com/.
 * Copyright (C) 2007 Alexander Maryanovsky.
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

import free.jin.I18n;
import free.jin.ServerUser;
import free.jin.console.SystemConsoleDesignation;
import free.jin.event.ChatEvent;



/**
 * The ICC-specific system console designation. 
 */

public class ChessclubSystemConsoleDesignation extends SystemConsoleDesignation{
  
  
  
  /**
   * Returns whether the specified chat event is a personal tell. 
   */
  
  protected boolean isPersonalTell(ChatEvent evt){
    String type = evt.getType();
    return type.equals("tell") || type.equals("say") || type.equals("atell") ||
        type.equals("ptell");
  }
  
  
  
  /**
   * Returns the text which should be added to the console for the specified
   * chat event.
   */

  protected String textForChat(ChatEvent evt, String encoding){
    String type = evt.getType();
    ServerUser sender = evt.getSender();
    String title = evt.getSenderTitle();
    String message = evt.getMessage(encoding);
    Object forum = evt.getForum();
    
    if ("qtell".equals(type))
      return parseQTell(evt);
    else if ("channel-qtell".equals(type))
      return parseChannelQTell(evt);

    Object [] args = new Object[]{String.valueOf(sender), title, String.valueOf(forum), message};
    
    I18n i18n = I18n.get(ChessclubSystemConsoleDesignation.class);
    return i18n.getFormattedString(type + ".displayPattern", args);
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
  
  
  
}
