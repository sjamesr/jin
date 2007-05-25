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

import free.jin.Connection;
import free.jin.ServerUser;
import free.jin.console.PersonalChatConsoleDesignation;
import free.jin.event.ChatEvent;



/**
 * The ICC-specific personal chat console designation.
 */

public class ChessclubPersonalChatConsoleDesignation extends PersonalChatConsoleDesignation{
  
  
  
  /**
   * Creates a new <code>ChessclubPersonalChatConsoleDesignation</code>.
   * 
   * @param connection The connection to the server.
   * @param conversationPartner The user we're talking to.
   * @param encoding The encoding to use for encoding/decoding messages.
   */
  
  public ChessclubPersonalChatConsoleDesignation(Connection connection, ServerUser conversationPartner,
      String encoding, boolean isConsoleCloseable){
    super(connection, conversationPartner, encoding, isConsoleCloseable);
  }
  
  
  
  /**
   * Handles qtells.
   */
  
  protected String textForChat(ChatEvent evt){
    if ("qtell".equals(evt.getType()))
      return ChessclubConsoleManager.parseQTell(evt);
    else
      return super.textForChat(evt);
  }
  
  
  
}
