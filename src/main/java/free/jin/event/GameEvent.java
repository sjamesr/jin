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

import free.jin.Connection;
import free.jin.Game;

/** The superclass of all game related events. */
public abstract class GameEvent extends JinEvent {

  /** The game on which this event occurred. */
  private final Game game;

  /** Creates a new GameEvent with the given Game and source Connection. */
  public GameEvent(Connection conn, String clientTag, Game game) {
    super(conn, clientTag);

    this.game = game;
  }

  /** Returns the Game whose start or end triggered this event.. */
  public Game getGame() {
    return game;
  }
}
