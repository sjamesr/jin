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



/**
 * The chat console designation for shouts. This is intended to be used as a
 * "general chat" console designation.
 */

public class ShoutChatConsoleDesignation extends ChatConsoleDesignation{
  
  
  
  /**
   * Creates a new <code>ShoutChatConsoleDesignation</code>.
   * 
   * @param connection The connection to the server.
   * @param encoding The encoding to use for encoding/decoding messages.
   * @param isConsoleCloseable Whether the console should be closeable. 
   */
  
  public ShoutChatConsoleDesignation(Connection connection, String encoding, boolean isConsoleCloseable){
    super(connection, I18n.get(ShoutChatConsoleDesignation.class).getString("name"),
        encoding, isConsoleCloseable);
    
    addAccepted("shout", null, ANY_SENDER);
    addAccepted("ishout", null, ANY_SENDER);
  }
  
  
  
  /**
   * Returns the "shout" and "i" command types.
   */
  
  public CommandType [] createCommandTypes(){
    I18n i18n = I18n.get(ShoutChatConsoleDesignation.class);
    
    return new CommandType[]{
      new AbstractCommandType(i18n.getString("shoutCommandName")){
        protected void send(String userText){
          connection.sendTaggedCommand("shout " + userText, getTag());
        }
        protected void echo(String userText){}
      },
      
      new AbstractCommandType(i18n.getString("ishoutCommandName")){
        protected void send(String userText){
          connection.sendTaggedCommand("i " + userText, getTag());
        }
        protected void echo(String userText){}
      }
    };
  }
  
  
  
  /**
   * Appends the text for the specified chat event to the console.
   */
  
  protected void appendChat(ChatEvent evt){
    if ("shout".equals(evt.getType()))
      super.appendChat(evt);
    else{
      Console console = getConsole();
      console.addToOutput("--> " + evt.getSender().getName() + " " + evt.getMessage(),
          console.textTypeForEvent(evt));
    }
  }
  
  
  
}
