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

package free.jin.board.fics;

import free.jin.Game;
import free.jin.board.BoardManager;
import free.jin.board.BoardPanel;


/**
 * Extends BoardPanel to provide freechess.org specific functionalities.
 */

public class FreechessBoardPanel extends BoardPanel{



  /**
   * Creates a new <code>FreechessBoardPanel</code> with the given
   * <code>BoardManager</code> and <code>Game</code>.
   */

  public FreechessBoardPanel(BoardManager boardManager, Game game){
    super(boardManager, game);
  }



  /**
   * Overrides BoardPanel.createWhiteLabelText(Game) to return a freechess.org
   * specific version.
   */

  protected String createWhiteLabelText(Game game){
    int rating = game.getWhiteRating();
    String ratingString = (rating > 0) ? (" "+rating) : "";
    return game.getWhiteName() + game.getWhiteTitles() + ratingString;
  }




  /**
   * Overrides BoardPanel.createBlackLabelText(Game) to return a freechess.org
   * specific version.
   */

  protected String createBlackLabelText(Game game){
    int rating = game.getBlackRating();
    String ratingString = (rating > 0) ? (" "+rating) : "";
    return game.getBlackName() + game.getBlackTitles() + ratingString;
  }



}
