/**
 * Jin - a chess client for internet chess servers.
 * More information is available at http://www.hightemplar.com/jin/.
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

package free.jin.chessclub.board;

import free.jin.board.BoardManager;
import free.jin.board.BoardPanel;
import free.jin.Game;


/**
 * A chessclub.com specific extension of the BoardManager plugin.
 */

public class ChessclubBoardManager extends BoardManager{


  /**
   * Overrides BoardManager.createBoardPanel() to return a ChessclubBoardPanel.
   */

  protected BoardPanel createBoardPanel(Game game){
    BoardPanel boardPanel = new ChessclubBoardPanel(this, game);

    return boardPanel;
  }

}
