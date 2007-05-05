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

import free.jin.FriendsConnection;
import free.jin.ServerUser;


/**
 * The event fired when something friend related occurs.
 */

public class FriendsEvent extends JinEvent{
  
  
  
  /**
   * The id for events when a friend has logged on.
   */

  public static final int FRIEND_CONNECTED = 1;
  
  
  
  /**
   * The id for events when a friend has logged off.
   */

  public static final int FRIEND_DISCONNECTED = 2;
  
  
  
  /**
   * The id for events when a new friend is added to the list of friends.
   */

  public static final int FRIEND_ADDED = 3;
  
  
  
  /**
   * The id for events when a friend is removed form the list of friends.
   */

  public static final int FRIEND_REMOVED = 4;
  
  
  
  /**
   * The id for events notifying that the state of the user changed.
   */
  
  public static final int FRIEND_STATE_CHANGED = 5;
  
  
  
  /**
   * The id of the event.
   */

  private final int id;
  
  
  
  /**
   * The friend.
   */

  private final ServerUser friend;
  
  
  
  /**
   * The friend's state - an <code>OR</code> combination of the relevant friend
   * state masks in <code>FriendsConnection</code>.
   */
  
  private final int friendState;
  
  
  
  /**
   * Creates a new <code>FriendsEvent</code> with the specified source
   * <code>FriendsConnection</code> event id, friend and his state.
   */
  
  public FriendsEvent(FriendsConnection source, int id, ServerUser friend, int friendState){
    super(source);
    
    switch (id){
      case FRIEND_CONNECTED:
      case FRIEND_DISCONNECTED:
      case FRIEND_ADDED:
      case FRIEND_REMOVED:
      case FRIEND_STATE_CHANGED:
        break;
      default:
        throw new IllegalArgumentException("Unknown FriendsEvent id: "+id);
    }
    
    if (friend == null)
      throw new IllegalArgumentException("friend may not be null");

    this.id = id;
    this.friend = friend;
    this.friendState = friendState;
  }
  
  
  
  /**
   * Returns the id of the event.
   */
  
  public int getID(){
    return id;
  }
  
  
  
  /**
   * Returns the friend.
   */
  
  public ServerUser getFriend(){
    return friend;
  }
  
  
  
  /**
   * Returns the friend's state - an <code>OR</code> combination of the relevant
   * <code>*_FRIEND_STATE_MASK</code> masks in <code>FriendsConnection</code>. This
   * is only relevant if the friend is online.
   */
  
  public int getFriendState(){
    return friendState;
  }
  
  
  
  /**
   * Returns the source <code>FriendsConnection</code>.
   */
  
  public FriendsConnection getFriendsConnection(){
    return (FriendsConnection)getConnection();
  }
  
  
  
}

