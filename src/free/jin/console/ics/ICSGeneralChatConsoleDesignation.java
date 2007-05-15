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

package free.jin.console.ics;

import free.jin.Connection;
import free.jin.I18n;
import free.jin.console.ChatConsoleDesignation;
import free.jin.console.Console;
import free.jin.event.ChatEvent;
import free.jin.event.JinEvent;



/**
 * The general chat console designation for ICS-type servers.
 */

public class ICSGeneralChatConsoleDesignation extends ChatConsoleDesignation{
  
  
  
  /**
   * Creates a new <code>ICSGeneralChatConsoleDesignation</code> with
   * the specified encoding and closeable status.
   */
  
  public ICSGeneralChatConsoleDesignation(String encoding, boolean isConsoleCloseable){
    super(I18n.get(ICSGeneralChatConsoleDesignation.class).getString("name"),
        encoding, isConsoleCloseable);
    
    addAccepted("shout", null, null);
    addAccepted("ishout", null, null);
    
    I18n i18n = I18n.get(ICSGeneralChatConsoleDesignation.class);
    
    addCommandType(new CommandType(i18n.getString("shoutCommandName")){
      public void executeCommand(String userText, Connection connection){
        connection.sendCommand("shout " + userText);
      }
    });

    addCommandType(new CommandType(i18n.getString("ishoutCommandName")){
      public void executeCommand(String userText, Connection connection){
        connection.sendCommand("i " + userText);
      }
    });
  }
  
  
  
  /**
   * Appends the text for the specified chat event to the console.
   */
  
  protected void append(JinEvent evt){
    ChatEvent chatEvent = (ChatEvent)evt;
    if ("shout".equals(chatEvent.getType()))
      super.append(evt);
    else{
      Console console = getConsole();
      console.addToOutput("--> " + chatEvent.getSender().getName() + " " + chatEvent.getMessage(),
          console.textTypeForEvent(evt));
    }
  }
  
  
  
}
