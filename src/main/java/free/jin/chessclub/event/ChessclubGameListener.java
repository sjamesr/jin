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
package free.jin.chessclub.event;

import free.jin.event.GameListener;

/** A chessclub.com specific extension to the GameListener interface. */
public interface ChessclubGameListener extends GameListener {

  /** This method gets called when an arrow has been added to the board. */
  void arrowAdded(ArrowEvent evt);

  /** This method gets called when an arrow has been removed from the board. */
  void arrowRemoved(ArrowEvent evt);

  /** This method gets called when a circle has been added to the board. */
  void circleAdded(CircleEvent evt);

  /** This method gets called when a circle has been removed from the board. */
  void circleRemoved(CircleEvent evt);
}
