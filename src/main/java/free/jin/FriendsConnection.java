/**
 * Jin - a chess client for internet chess servers. More information is available at
 * http://www.jinchess.com/. Copyright (C) 2002 Alexander Maryanovsky. All rights reserved.
 *
 * <p>This program is free software; you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation; either version 2 of the
 * License, or (at your option) any later version.
 *
 * <p>This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * <p>You should have received a copy of the GNU General Public License along with this program; if
 * not, write to the Free Software Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA
 * 02111-1307, USA.
 */
package free.jin;

import free.jin.event.FriendsListenerManager;
import java.util.Collection;

/**
 * An extension of the <code>Connection</code> interface which adds support for friend lists (known
 * as notify lists on ICC and FICS). Note that methods which modify friends data do not necessarily
 * cause an immediate change, but may send the appropriate request to the server, and change the
 * local data when it responds. Methods which query the data return immediately, based on the local
 * data. Also, note that friends data is only guaranteed to be retrieved when at least one friends
 * listener is registered.
 */
public interface FriendsConnection extends Connection {

  /** The mask for specifying whether the friend is online. */
  public static final int ONLINE_FRIEND_STATE_MASK = 1;

  /** The mask for player state specifying that a logged in friend is playing. */
  public static final int PLAYING_FRIEND_STATE_MASK = 2;

  /**
   * Returns the <code>FriendsListenerManager</code> which allows registering and unregistering
   * <code>FriendsListener</code>s.
   */
  FriendsListenerManager getFriendsListenerManager();

  /** Adds the given user to the list of friends. */
  void addFriend(ServerUser user);

  /** Removes the given user from the list of friends. */
  void removeFriend(ServerUser user);

  /** Returns the current set of friends (a set of <code>ServerUser</code>s). */
  Collection getFriends();

  /** Returns whether the specified user is a friend. */
  boolean isFriend(ServerUser user);

  /**
   * Returns the set of online friends (a set of <code>ServerUser</code>s). Note that this isn't
   * necessarily a subset of {@link #getFriends()} because some of the elements there may be aliases
   * or the server decided to notify us of the login of some user (for example, if we have an
   * adjourned game with him).
   */
  Collection getOnlineFriends();

  /** Returns whether the specified user is a friend and is online. */
  boolean isFriendOnline(ServerUser user);

  /**
   * Returns the state of the specified friend - an <code>OR</code> combination of the <code>
   * *_FRIEND_STATE_MASK</code> masks. This is only relevant if the friend is online.
   */
  int getFriendState(ServerUser user);
}
