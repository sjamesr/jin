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

import free.jin.Connection;
import free.jin.I18n;
import free.jin.ServerUser;
import free.jin.event.ChatEvent;
import free.jin.event.JinEvent;



/**
 * A <code>ConsoleDesignation</code> for person-to-person chat.
 */

public final class PersonalChatConsoleDesignation extends AbstractConsoleDesignation{
  
  
  
  /**
   * The user with whom we're chatting.
   */
  
  private final ServerUser conversationPartner;
  
  
  
  /**
   * Our sole command type.
   */
  
  private final CommandType sendPersonalTell;
  
  
  
  /**
   * The number of unseen (added when the console is invisible) messages we've
   * received.
   */
  
  private int unseenMessageCount = 0;

  
  
  /**
   * Creates a new <code>PersonalChatConsoleDesignation</code> with the
   * specified conversation partner, encoding, and closeable status.
   */
  
  public PersonalChatConsoleDesignation(ServerUser conversationPartner, 
      String encoding, boolean isConsoleCloseable){
    super(conversationPartner.getName(), encoding, isConsoleCloseable);
    
    this.conversationPartner = conversationPartner;
    this.sendPersonalTell = new SendPersonalTell();
  }
  
  
  
  /**
   * Joins personal chat with out conversation partner.
   */
  
  protected void joinForums(Connection connection){
    connection.joinPersonalChat(conversationPartner);
  }
  
  
  
  /**
   * Clears the unseen message count.
   */
  
  protected void consoleShown(){
    unseenMessageCount = 0;
    setName(conversationPartner.getName());
  }
  
  
  
  /**
   * Returns our sole command type - sending a personal tell to our conversation
   * partner.
   */
  
  public CommandType [] getCommandTypes(){
    return new CommandType[]{sendPersonalTell};
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
  
  protected void append(JinEvent evt){
    // We already know it's a ChatEvent because it passed accept(JinEvent)
    ChatEvent chatEvent = (ChatEvent)evt;
    
    String senderName = chatEvent.getSender().getName();
    String senderTitle = chatEvent.getSenderTitle();
    String message = decode(chatEvent.getMessage(), chatEvent.getConnection());
    
    String text =
      senderName +
      (senderTitle == null ? "" : senderTitle) +
      ": " +
      message;
    
    Console console = getConsole();
    console.addToOutput(text, console.textTypeForEvent(evt));
    
    if (!console.isVisible()){
      unseenMessageCount++;
      setName(conversationPartner.getName() + " (" + unseenMessageCount + ")");
    }
  }
  
  
  
  /**
   * Returns whether the specified object is a
   * <code>PersonalChatConsoleDesignation</code> equals to this one. Two
   * <code>PersonalChatConsoleDesignation</code>s are equal if they have the
   * same conversation partner.
   */
  
  public boolean equals(Object o){
    if (!(o instanceof PersonalChatConsoleDesignation))
      return false;
    
    PersonalChatConsoleDesignation designation = (PersonalChatConsoleDesignation)o;
    return designation.conversationPartner.equals(conversationPartner);
  }
  
  
  
  /**
   * Returns the hash code of this <code>PersonalChatConsoleDesignation</code>.
   */
  
  public int hashCode(){
    return conversationPartner.hashCode();
  }
  
  
  
  /**
   * A command type which sends a personal tell to our conversation partner.
   */
  
  private class SendPersonalTell extends CommandType{
    
    
    
    /**
     * Creates a new <code>SendPersonalTell</code> with the specified
     * conversation parner.
     */
    
    public SendPersonalTell(){
      super(I18n.get(SendPersonalTell.class).getFormattedString("name", 
          new Object[]{conversationPartner.getName()}));
    }
    
    
    
    /**
     * Sends the specified personal tell to our conversation partner. 
     */
    
    public void executeCommand(String message, Connection connection){
      connection.sendPersonalTell(conversationPartner, 
          encode(message, connection));
    }
    
    
    
  }
  
  
  
}
