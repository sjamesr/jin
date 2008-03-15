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

package free.jin.console.icc;

import free.jin.Connection;
import free.jin.I18n;
import free.jin.console.ics.IcsGeneralChatConsoleDesignation;
import free.jin.event.ChatEvent;



/**
 * The "general chat" console for ICC's main server. This displays shouts.
 * 
 * @author Maryanovsky Alexander
 */

public class IccGeneralChatConsoleDesignation extends IcsGeneralChatConsoleDesignation{
  
  
  
  /**
   * Creates a new <code>IccGeneralChatConsoleDesignation</code>.
   */
  
  public IccGeneralChatConsoleDesignation(Connection connection, String encoding, boolean isConsoleCloseable){
    super(connection, encoding, isConsoleCloseable);
    
    addAccepted("shout", null, ANY_SENDER);
    addAccepted("ishout", null, ANY_SENDER);
    addAccepted("announcement", null, ANY_SENDER);
    
    I18n i18n = I18n.get(IccGeneralChatConsoleDesignation.class);
    addCommandType(new AbstractCommandType(i18n.getString("ishout.commandName")){
      protected void send(String userText){
        sendTaggedCommand("i " + userText);
      }
    });
  }
  
  
  
  /**
   * Returns whether the specified chat event is a shout.
   */

  protected boolean isStandardChatMessage(ChatEvent evt){
    return "shout".equals(evt.getType());
  }
  
  
  
  /**
   * Sends the specified text as a shout.
   */
  
  protected void sendStandardChatMessage(String userText){
    sendTaggedCommand("shout " + userText);
  }
  
  
  
}
