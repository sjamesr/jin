/**
 * Jin - a chess client for internet chess servers.
 * More information is available at http://www.jinchess.com/.
 * Copyright (C) 2002 Alexander Maryanovsky.
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

package free.jin.event;

import free.jin.event.JinEvent;
import free.jin.Connection;


/**
 * The event that is sent to ChatListeners when a chat related message
 * arrives from the server. The ChatEvent contains the type of the message,
 * the player who sent it, his title, the message itself and optionally
 * the forum on which it was sent (channel, game). For one-on-one (personal)
 * tells or tells that go to everyone the forum is null. <br>
 *
 * Note that this class isn't aware of the value of the tell type, it's a server
 * specific string which should be dealt by a server specific class.<br>
 *
 * The source of the event is the Connection it came from.
 */

public class ChatEvent extends JinEvent{
  
  
  
  /**
   * The constant for chat which doesn't fit into one of the other categories.
   */
   
  public static final int OTHER_CHAT_CATEGORY = -1;
  
  
  
  /**
   * The constant for person-to-person chat.
   */
   
  public static final int PERSON_TO_PERSON_CHAT_CATEGORY = 0;
  
  
  
  /**
   * The constant for in-game chat.
   */
   
  public static final int GAME_CHAT_CATEGORY = 1;
  
  
  
  /**
   * The constant for chat which is sent to all (or almost all) players.
   */
   
  public static final int BROADCAST_CHAT_CATEGORY = 2;
  
  
  
  /**
   * The constant for chat belonging to a certain "room" or "channel" which
   * players can choose to be in.
   */
   
  public static final int ROOM_CHAT_CATEGORY = 3;
  
  
  
  /**
   * The constant for chat associated with a certain tournament.
   */
   
  public static final int TOURNEY_CHAT_CATEGORY = 4;



  /**
   * The type of the ChatEvent. This is something server specific and thus
   * should only be handled by server specific classes.
   */

  private final String type;
  
  
  
  /**
   * The category of this chat event.
   */
   
  private final int category;



  /**
   * The name/handle of the player who sent the message.
   */

  private final String sender;



  /**
   * The title of the player who sent the string, must be a non-null
   * string.
   */

  private final String senderTitle;



  /**
   * The rating of the player who sent the string, -1 if unknown.
   */

  private final int senderRating;



  /**
   * The message itself. Must be a non-null string.
   */

  private final String message;



  /**
   * The forum on which the message was sent. Only applies to
   * messages that aren't one-on-one or go to everyone. For kibitzes
   * and whispers this is an Integer specifying the game number for example,
   * for channel tells, the channel number.
   */

  private final Object forum;



  /**
   * Creates a new ChatEvent with the given type, category, sender, sender
   * titles, sender rating (-1 if unknown), message and forum. Note that the
   * list of possible chat categories is not final (and will never be such).
   * If your chat type belongs to a category which does not exist yet, contact
   * the person responsible for the code and ask him to add a new category. In
   * the meanwhile (or if you are happy with it), use
   * <code>UNCATEGORIZED_CHAT</code>
   */

  public ChatEvent(Connection conn, String type, int category, String sender, String senderTitle,
      int senderRating, String message, Object forum){
    super(conn);

    if (type == null)
      throw new IllegalArgumentException("ChatEvent type may not be null");
    
    switch (category){
      case OTHER_CHAT_CATEGORY:
      case PERSON_TO_PERSON_CHAT_CATEGORY:
      case GAME_CHAT_CATEGORY:
      case BROADCAST_CHAT_CATEGORY:
      case ROOM_CHAT_CATEGORY:
      case TOURNEY_CHAT_CATEGORY:
        break;
      default:
        throw new IllegalArgumentException("Unknown chat category value: " + category);
    }

    this.type = type;
    this.category = category;
    this.sender = sender;
    this.senderTitle = senderTitle;
    this.senderRating = senderRating;
    this.message = message;
    this.forum = forum;
  }



  /**
   * Returns the type of this message. This is a server specific string and
   * should be dealt by a server specific class.
   */

  public String getType(){
    return type;
  }
  
  
  
  /**
   * Returns the category of the chat. Note that the list of possible categories
   * is not final (and will never be), so don't make your code assume that the
   * category is one of the current categories.
   */
   
  public int getCategory(){
    return category;
  }



  /**
   * Returns the name/handle of the player who sent the message.
   */

  public String getSender(){
    return sender;
  }



  /**
   * Returns the title of the player who sent the message.
   */

  public String getSenderTitle(){
    return senderTitle;
  }



  /**
   * Returns the rating of the sender, or -1 if unknown.
   */

  public int getSenderRating(){
    return senderRating;
  }



  /**
   * Returns the message itself.
   */

  public String getMessage(){
    return message;
  }



  /**
   * Returns the forum on which the message was sent. The forum identifies a
   * certain instance of a chat type. For room/channel tells this is a
   * <code>String/Integer</code> specifying the room/channel name/number.
   * For kibitzes and whispers, the game number. For chat types with only a
   * single instance (such as shouts, announcements) or where the instance is
   * already identified by the sender (personal tells) this is
   * <code>null</code>. This value is somewhat server specific, it should
   * probably be handled by server specific code.
   */

  public Object getForum(){
    return forum;
  }



  /**
   * Returns a textual representation of this ChatEvent.
   */

  public String toString(){
    return getClass().getName()+"[Sender="+getSender()+";Title="+getSenderTitle()+";Forum="+getForum()+";Message="+getMessage()+"]";
  } 
  

  
}
