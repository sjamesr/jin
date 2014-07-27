/**
 * Jin - a chess client for internet chess servers.
 * More information is available at http://www.jinchess.com/.
 * Copyright (C) 2008 Alexander Maryanovsky.
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

package free.jin.console.ics;

import free.jin.Connection;
import free.jin.ServerUser;
import free.jin.console.Console;
import free.jin.console.PersonalChatConsoleDesignation;
import free.jin.event.ChatEvent;



/**
 * An ICS-specific personal chat console designation.
 * 
 * @author Maryanovsky Alexander
 */

public class IcsPersonalChatConsoleDesignation extends PersonalChatConsoleDesignation{
  
  
  
  /**
   * Creates a new {@linkplain IcsPersonalChatConsoleDesignation}.
   */
  
  public IcsPersonalChatConsoleDesignation(Connection connection, ServerUser conversationPartner,
      String encoding, boolean isConsoleCloseable){
    super(connection, conversationPartner, encoding, isConsoleCloseable);
  }
  
  
  
  /**
   * Appends the specified chat event to the console.
   */
  
  @Override
  protected void appendChat(ChatEvent evt){
    String type = evt.getType();
    
    if ("tell".equals(type) || "say".equals(type) || "atell".equals(type) || "ptell".equals(type)){
      Console console = getConsole();
      String senderName = evt.getSender().getName();
      String senderTitle = evt.getSenderTitle();
      String message = decode(evt.getMessage());
      
      String text = senderName + (senderTitle == null ? "" : senderTitle) + ": " + message;
      console.addToOutput(text, console.textTypeForEvent(evt));
    }
    else
      super.appendChat(evt);
  }
  
  
  
}
