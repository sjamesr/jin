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

package free.jin.chessclub.board;


import javax.swing.JInternalFrame;
import javax.swing.event.InternalFrameAdapter;
import javax.swing.event.InternalFrameEvent;
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



  /**
   * Creates a JInternalFrame to be used for displaying the given
   * BoardPanel. Overrides the superclass' method to register a listener so that
   * we can set the primary game appropriately.
   */

  protected JInternalFrame createBoardFrame(final BoardPanel boardPanel){
    JInternalFrame boardFrame = super.createBoardFrame(boardPanel);

    if (boardPanel.getGame().getGameType() != Game.ISOLATED_BOARD){
      boardFrame.addInternalFrameListener(new InternalFrameAdapter(){
        public void internalFrameActivated(InternalFrameEvent evt){
          Game game = boardPanel.getGame();
          getConnection().sendCommand("primary "+game.getID());
        }
      });
    }

    return boardFrame;
  }


}
