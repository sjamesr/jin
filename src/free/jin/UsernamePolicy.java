/**
 * Jin - a chess client for internet chess servers.
 * More information is available at http://www.jinchess.com/.
 * Copyright (C) 2003 Alexander Maryanovsky.
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


/**
 * Defines various username policies, such as which usernames are legal, whether
 * they are case sensitive etc.
 */

public interface UsernamePolicy{



  /**
   * Returns whether the two specified usernames are considered to be the same.
   */

  boolean isSame(String username1, String username2);



  /**
   * Given a username, returns a string describing a reason why it is invalid,
   * or <code>null</code> if the username is valid.
   */

  String invalidityReason(String username);



  /**
   * Returns the usual username used for logging in as guest.
   */

  String getGuestUsername();



}