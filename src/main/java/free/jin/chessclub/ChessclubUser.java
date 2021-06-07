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
package free.jin.chessclub;

import free.jin.PlainServerUser;

/** A user of ICC. */
public final class ChessclubUser extends PlainServerUser {

  /** Creates a new <code>ChessclubUser</code> with the specified handle. */
  private ChessclubUser(String handle) {
    super(handle, false, handle.startsWith("&"));
  }

  /**
   * Returns a <code>ChessclubUser</code> with the specified handle. We use this method to allow
   * caching in the future.
   */
  public static ChessclubUser get(String handle) {
    return new ChessclubUser(handle);
  }
}
