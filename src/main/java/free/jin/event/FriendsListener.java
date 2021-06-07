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
package free.jin.event;

import java.util.EventListener;

/**
 * The interface for listening to FriendsEvents. Note that not all "online friends" are actually in
 * the friend list because some of the elements in the friend list may be aliases, or because the
 * server decided to notify us of some other user (maybe because we have an adjourned game with
 * him).
 */
public interface FriendsListener extends EventListener {

  /**
   * Called to notify that the online state of the specified user changed. Note that this does not
   * necessarily mean that he logged on or off - it could be that he was already online but only now
   * got added to our friends, or he was "moved offline" because he was removed from the friends
   * list.
   */
  void friendStateChanged(FriendsEvent evt);

  /** Called when a friend logs on to the server. */
  void friendConnected(FriendsEvent evt);

  /** Called when a friend logs out from the server. */
  void friendDisconnected(FriendsEvent evt);

  /** Called when a new friend is added to the friend list. */
  void friendAdded(FriendsEvent evt);

  /** Called when a friend is removed form the friend list. */
  void friendRemoved(FriendsEvent evt);
}
