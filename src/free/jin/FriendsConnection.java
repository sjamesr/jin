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

package free.jin;

import java.util.Collection;

import free.jin.event.FriendsListenerManager;


/**
 * An extension of the <code>Connection</code> interface which adds support for
 * friend lists (known as notify lists on ICC and FICS).
 * Note that methods which modify friends data do not necessarily cause an
 * immediate change, but may send the appropriate request to the server, and
 * change the local data when it responds. Methods which query the data return
 * immediately, based on the local data.
 * Also, note that friends data is only guaranteed to be retrieved when at least
 * one friends listener is registered. 
 */

public interface FriendsConnection extends Connection{
  
  
  
  /**
   * Returns the <code>FriendsListenerManager</code> which allows registering
   * and unregistering <code>FriendsListener</code>s.
   */
  
  FriendsListenerManager getFriendsListenerManager();
  
  
  
  /**
   * Adds the given user to the list of friends.
   */
  
  void addFriend(ServerUser user);
  
  
  
  /**
   * Removes the given user from the list of friends.
   */
  
  void removeFriend(ServerUser user);
  
  
  
  /**
   * Returns the current set of friends (a set of <code>ServerUser</code>s).
   **/
  
  Collection getFriends();
  
  
  
  /**
   * Returns whether the specified user is a friend.
   */
  
  boolean isFriend(ServerUser user);
  
  
  
  /**
   * Returns the set of online friends (a set of <code>ServerUser</code>s).
   **/
  
  Collection getOnlineFriends();
  
  
  
  /**
   * Returns whether the specified user is a friend and is online.
   */

  boolean isFriendOnline(ServerUser user);
  
  
  
}
