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

package free.jin.freechess.board;

import javax.swing.JInternalFrame;
import javax.swing.event.InternalFrameAdapter;
import javax.swing.event.InternalFrameEvent;
import free.jin.board.BoardManager;
import free.jin.board.BoardPanel;
import free.jin.Game;
import free.jin.event.GameStartEvent;


/**
 * A freechess.oorg specific extension of the BoardManager plugin.
 */

public class FreechessBoardManager extends BoardManager{



  /**
   * The current primary game.
   */

  private Object primaryGameID = null;




  /**
   * Overrides BoardManager.createBoardPanel() to return a FreechessBoardPanel.
   */

  protected BoardPanel createBoardPanel(Game game){
    BoardPanel boardPanel = new FreechessBoardPanel(this, game);

    return boardPanel;
  }




  /**
   * Overrides the superclass' method to set the primary game properly.
   */

  public void internalFrameActivated(InternalFrameEvent e){
    BoardPanel boardPanel = (BoardPanel)internalFramesToBoardPanels.get(e.getSource());
    Game game = boardPanel.getGame();
    if (game.getGameType() != Game.ISOLATED_BOARD){
      Object gameID = game.getID();
      if (!gameID.equals(primaryGameID)){
        getConnection().sendCommand("primary "+gameID);
        primaryGameID = gameID;
      }
    }

    super.internalFrameActivated(e);
  }


  /**
   * Overrides the superclass' method to set <code>primaryGameID</code>
   * properly.
   */

  public void gameStarted(GameStartEvent evt){
    if (evt.getGame().getGameType() != Game.ISOLATED_BOARD)
      primaryGameID = evt.getGame().getID();

    super.gameStarted(evt);
  }

}
