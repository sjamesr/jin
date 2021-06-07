/**
 * Jin - a chess client for internet chess servers. More information is available at
 * http://www.jinchess.com/. Copyright (C) 2007 Alexander Maryanovsky. All rights reserved.
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

import free.util.Named;

/** Represents a user on a chess server. */
public interface ServerUser extends Named {

  /**
   * Returns the user's name/handle/nickname. This should be displayable to the user (of the
   * client).
   */
  @Override
  String getName();

  /** Returns whether this user is an alias for another user, or a group of users. */
  boolean isAlias();

  /** Returns whether the specified <code>ServerUser</code> is the same as this one. */
  @Override
  boolean equals(Object user);

  /** Returns the hash code of this <code>ServerUser</code> object. */
  @Override
  int hashCode();
}
