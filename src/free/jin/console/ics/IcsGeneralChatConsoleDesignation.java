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
import free.jin.I18n;
import free.jin.console.ChatConsoleDesignation;
import free.jin.console.Console;
import free.jin.event.ChatEvent;



/**
 * A base class for general chat designations for ICS-based servers. 
 * 
 * @author Maryanovsky Alexander
 */

public abstract class IcsGeneralChatConsoleDesignation extends ChatConsoleDesignation{
  
  
  
  /**
   * Creates a new <code>IccGeneralChatConsoleDesignation</code>.
   */
  
  public IcsGeneralChatConsoleDesignation(Connection connection, String encoding, boolean isConsoleCloseable){
    super(connection, 
        I18n.get(IcsGeneralChatConsoleDesignation.class).getString("name"),
        encoding, isConsoleCloseable);
    
    I18n i18n = I18n.get(IcsGeneralChatConsoleDesignation.class);
    addCommandType(new AbstractCommandType(i18n.getString("message.commandName")){
      @Override
      protected void send(String userText){
        sendStandardChatMessage(userText);
      }
    });
  }
  
  
  
  /**
   * Sends the specified string as the message to the chat forum.
   */
  
  protected abstract void sendStandardChatMessage(String userText);
  
  
  
  /**
   * Returns whether the specified message is a standard chat message.
   */
  
  protected abstract boolean isStandardChatMessage(ChatEvent evt);
  
  
  
  /**
   * Displays the specified message in the console in a standard chat format.
   */
  
  @Override
  protected void appendChat(ChatEvent evt){
    if (isStandardChatMessage(evt)){
      String senderName = evt.getSender().getName();
      String senderTitle = evt.getSenderTitle();
      String message = decode(evt.getMessage());
      
      String text = 
        senderName +
        (senderTitle == null ? "" : senderTitle) +
        ": " +
        message;
      
      Console console = getConsole();
      console.addToOutput(text, console.textTypeForEvent(evt));
    }
    else
      super.appendChat(evt);
  }
 
  
  
}
