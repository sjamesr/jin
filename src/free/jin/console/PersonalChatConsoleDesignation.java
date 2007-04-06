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

package free.jin.console;

import free.jin.ServerUser;
import free.jin.event.ChatEvent;
import free.jin.event.JinEvent;



/**
 * A <code>ConsoleDesignation</code> for person-to-person chat.
 */

public class PersonalChatConsoleDesignation extends AbstractConsoleDesignation{
  
  
  
  /**
   * The user with whom we're chatting.
   */
  
  private final ServerUser conversationPartner;
  
  
  
  /**
   * Creates a new <code>PersonalChatConsoleDesignation</code> with the
   * specified conversation partner.
   */
  
  public PersonalChatConsoleDesignation(ServerUser conversationPartner, boolean isConsoleTemporary){
    super(conversationPartner.getName(), isConsoleTemporary);
    
    this.conversationPartner = conversationPartner;
  }
  
  
  
  /**
   * Accepts only person-to-person chat events.
   */
  
  protected boolean accept(JinEvent evt){
    if (!(evt instanceof ChatEvent))
      return false;
    
    ChatEvent chatEvent = (ChatEvent)evt;
    return (chatEvent.getCategory() == ChatEvent.PERSON_TO_PERSON_CHAT_CATEGORY) &&
      chatEvent.getSender().equals(conversationPartner);
  }
  
  
  
  /**
   * Appends the specified chat event to the console.
   */
  
  protected void append(JinEvent evt, String encoding, Console console){
    // We already know it's a ChatEvent because it passed accept(JinEvent)
    ChatEvent chatEvent = (ChatEvent)evt;
    
    String senderName = chatEvent.getSender().getName();
    String senderTitle = chatEvent.getSenderTitle();
    String message = chatEvent.getMessage(encoding);
    
    String text =
      senderName +
      (senderTitle == null ? "" : senderTitle) +
      ": " +
      message;
    
    console.addToOutput(text, console.textTypeForEvent(evt));
  }
  
  
  
}
