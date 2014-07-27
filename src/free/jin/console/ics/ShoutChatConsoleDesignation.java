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



/**
 * A console designation which displays shouts.
 */

public class ShoutChatConsoleDesignation extends ChatConsoleDesignation{
  
  
  
  /**
   * Creates a new <code>ShoutChatConsoleDesignation</code>.
   * 
   * @param connection The connection to the server.
   * @param encoding The encoding to use for encoding/decoding messages.
   * @param isConsoleCloseable Whether the console should be closeable. 
   */
  
  private ShoutChatConsoleDesignation(Connection connection, String encoding, boolean isConsoleCloseable){
    super(connection, I18n.get(ShoutChatConsoleDesignation.class).getString("name"),
        encoding, isConsoleCloseable);
    
    addAccepted("shout", null, ANY_SENDER);
    addAccepted("ishout", null, ANY_SENDER);
    
    I18n i18n = I18n.get(ShoutChatConsoleDesignation.class);
    addCommandType(new AbstractCommandType(i18n.getString("shout.commandName")){
      @Override
      protected void send(String userText){
        sendCommand("shout " + userText);
      }
    });
    
    addCommandType(new AbstractCommandType(i18n.getString("ishout.commandName")){
      @Override
      protected void send(String userText){
        sendCommand("i " + userText);
      }
    });
  }
  
  
  
}
