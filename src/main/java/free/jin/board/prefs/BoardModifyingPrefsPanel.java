/**
 * Jin - a chess client for internet chess servers.
 * More information is available at http://www.jinchess.com/.
 * Copyright (C) 2004 Alexander Maryanovsky.
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

package free.jin.board.prefs;

import free.jin.board.BoardManager;
import free.jin.board.JinBoard;
import free.jin.ui.PreferencesPanel;


/**
 * A common superclass for preference panels which modify the board. 
 */
 
public abstract class BoardModifyingPrefsPanel extends PreferencesPanel{
  
  
  
  /**
   * The board manager.
   */
   
  protected final BoardManager boardManager;
  
  
  
  /**
   * The preview board.
   */
   
  protected final JinBoard previewBoard;
  
  
  
  /**
   * Creates a new BoardModifyingPrefsPanel for the specified
   * <code>BoardManager</code> and with the specified preview board.
   */
   
  public BoardModifyingPrefsPanel(BoardManager boardManager, JinBoard previewBoard){
    this.boardManager = boardManager;
    this.previewBoard = previewBoard;
  }
  
  
  
  /**
   * Sets the initial properties of the preview board. This method is intended
   * to be overriden by subclasses to apply the initial properties of the board
   * that the particular subclass is responsible for.
   */
   
  public abstract void initPreviewBoard();
  
  
  
}