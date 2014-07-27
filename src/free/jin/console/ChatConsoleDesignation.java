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

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import free.jin.Connection;
import free.jin.ServerUser;
import free.jin.event.ChatEvent;
import free.jin.event.JinEvent;
import free.util.Utilities;



/**
 * A <code>ConsoleDesignation</code> which accepts a specified subset of chat
 * events.
 */

public abstract class ChatConsoleDesignation extends AbstractConsoleDesignation{
  
  
  
  /**
   * The chat type object matching any chat type.
   */
  
  public static final String ANY_CHAT_TYPE = null;
  
  
  
  /**
   * The forum object matching any forum.
   */
  
  public static final Object ANY_FORUM = new Object();
  
  
  
  /**
   * The sender object matching any sender.
   */
  
  public static final ServerUser ANY_SENDER = null;
  
  
  
  /**
   * The set of chat types we accept.
   */
  
  private final List acceptedChatTypes = new LinkedList();
  
  
  
  /**
   * Creates a new <code>ChatConsoleDesignation</code>
   * 
   * @param connection The connection to the server.
   * @param name The name of the console.
   * @param encoding The encoding to use for encoding/decoding messages.
   * @param isConsoleCloseable Whether the console should be closeable. 
   */
  
  public ChatConsoleDesignation(Connection connection, String name, String encoding, boolean isConsoleCloseable){
    super(connection, name, encoding, isConsoleCloseable);
  }
  
  
  
  /**
   * Joins all the chat types we're accepting.
   */
  
  @Override
  protected void joinForums(){
    for (Iterator i = acceptedChatTypes.iterator(); i.hasNext();){
      ChatType chatType = (ChatType)i.next();
      connection.joinChat(chatType.getType(), chatType.getForum());
    }
  }
  
  
  
  /**
   * Make the specified chat type accepted by this chat console.
   * The combination of <code>type</code>, <code>forum</code> and
   * <code>sender</code> must be one which corresponds to an actual possible
   * chat type on the server.
   * 
   * @param type The chat type (as in {@link ChatEvent#getType()} to accept,
   * or {@link #ANY_CHAT_TYPE} to accept any chat type.
   * @param forum The forum (as in {@link ChatEvent#getForum()} to accept,
   * or {@link #ANY_FORUM} to accept any forum.
   * @param sender The sender (as in {@link ChatEvent#getSender()} to accept,
   * or {@link #ANY_SENDER} to accept any sender.
   */
  
  public void addAccepted(String type, Object forum, ServerUser sender){
    acceptedChatTypes.add(new ChatType(type, forum, sender));
  }
  
  
  
  /**
   * Returns whether the specified <code>JinEvent</code> is infact a
   * <code>ChatEvent</code> with one of the accepted types.
   */
  
  @Override
  protected boolean accept(JinEvent evt){
    if (isTaggedByUs(evt))
      return true;
    
    if (!(evt instanceof ChatEvent))
      return false;
    
    ChatEvent chatEvent = (ChatEvent)evt;
    for (Iterator i = acceptedChatTypes.iterator(); i.hasNext();){
      ChatType chatType = (ChatType)i.next();
      if (chatType.accept(chatEvent))
        return true;
    }
    
    return false;
  }
  
  
  
  /**
   * The representation of a subset of chat types.
   */
  
  private static class ChatType{
    
    
    
    /**
     * The actual accepted chat "type", as in <code>ChatEvent.getType()</code>;
     * <code>null</code> stands for "any type". 
     */
    
    private final String type;
    
    
    
    /**
     * The chat forum; <code>null</coe> stands for "any forum".
     */
    
    private final Object forum;
    
    
    
    /**
     * The sender; <code>null</code> stands for "any sender".
     */
    
    private final ServerUser sender;
    
    
    
    /**
     * Creates a new <code>ChatType</code> with the specified type (as in
     * <code>ChatEvent.getType()</code>), forum and sender. Passing
     * <code>null</code> for an the argument, means that any value for it will
     * be accepted.
     */
    
    public ChatType(String type, Object forum, ServerUser sender){
      this.type = type;
      this.forum = forum;
      this.sender = sender;
    }
    
    
    
    /**
     * Returns whether the specified <code>ChatEvent</code> is of this
     * <code>ChatType</code>.
     */
    
    public boolean accept(ChatEvent evt){
      if ((type != ANY_CHAT_TYPE) && !Utilities.areEqual(type, evt.getType()))
        return false;
      
      if ((forum != ANY_FORUM) && !Utilities.areEqual(forum, evt.getForum()))
        return false;
      
      if ((sender != ANY_SENDER) && !Utilities.areEqual(sender, evt.getSender()))
        return false;
      
      return true;
    }
    
    
    
    /**
     * Returns the type of this chat type.
     */
    
    public String getType(){
      return type;
    }
    
    
    
    /**
     * Returns the forum of this chat type.
     */
    
    public Object getForum(){
      return forum;
    }
    
    
    
    /**
     * Returns the sender of this chat type.
     */
    
    public ServerUser getSender(){
      return sender;
    }
    
    
    
  }
  
  
  
}
