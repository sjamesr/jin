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

package free.jin.board;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;

import free.chess.Position;
import free.jin.I18n;
import free.jin.board.prefs.BoardLooksPanel;
import free.jin.board.prefs.BoardModifyingPrefsPanel;
import free.jin.board.prefs.MoveInputPanel;
import free.jin.board.prefs.SquareCoordinatesPanel;
import free.jin.ui.TabbedPreferencesPanel;
import free.util.SquareLayout;


/**
 * The preferences panel for the <code>BoardManager</code> plugin.
 */

public class BoardPreferencesPanel extends TabbedPreferencesPanel{



  /**
   * The BoardManager whose preferences panel this BoardPreferencesPanel is.
   */

  protected final BoardManager boardManager;
  
  
  
  /**
   * The preview board.
   */
   
  protected final JinBoard previewBoard;
  
  
  
  /**
   * The panel holding the preview board.
   */
  
  private final JComponent boardPanel;
  
  
  
  /**
   * Creates a new BoardPreferencesPanel for the specified board manager. 
   */
   
  public BoardPreferencesPanel(BoardManager boardManager){
    this.boardManager = boardManager;
    this.previewBoard = createPreviewBoard();
    this.boardPanel = new JPanel();
    
    I18n i18n = I18n.get(BoardPreferencesPanel.class);
    
    JPanel squarePanel = new JPanel(new SquareLayout());
    squarePanel.add(previewBoard);
    previewBoard.setAlignmentX(Component.CENTER_ALIGNMENT);
    previewBoard.setAlignmentY(Component.CENTER_ALIGNMENT);
    previewBoard.setPreferredSize(new Dimension(320, 320));
    
    JButton resetPosition = i18n.createButton("resetPositionButton");
    resetPosition.addActionListener(new ActionListener(){
      @Override
      public void actionPerformed(ActionEvent evt){
        initPreviewBoard();
      }
    });
    
    boardPanel.setLayout(new BoxLayout(boardPanel, BoxLayout.Y_AXIS));
    boardPanel.setBorder(BorderFactory.createCompoundBorder(
      i18n.createTitledBorder("testBoardPanel"),
      BorderFactory.createEmptyBorder(5, 5, 5, 5)));
    
    squarePanel.setAlignmentX(Component.CENTER_ALIGNMENT);
    resetPosition.setAlignmentX(Component.CENTER_ALIGNMENT);
    
    boardPanel.add(squarePanel);
    boardPanel.add(Box.createVerticalStrut(7));
    boardPanel.add(resetPosition);
    
    addPanel(createBoardLooksPanel(), "boardLooksPanel");
    addPanel(createMoveInputPanel(), "moveInputPanel");
    addPanel(createSquareCoordsPanel(), "coordsPanel");
    
    initPreviewBoard();
  }
  
  
  
  /**
   * Creates the preview board.
   */
   
  protected JinBoard createPreviewBoard(){
    return new JinBoard(new Position());
  }
  
  
  
  /**
   * Sets the initial state of the board.
   */
   
  protected void initPreviewBoard(){
    Position pos = previewBoard.getPosition();
    pos.setLexigraphic("rn-qkbnrPPP-pppp-------------b---------------------PPPPPRNBQKBNR");
    
    for (int i = 0; i < panels.size(); i++)
      ((BoardModifyingPrefsPanel)panels.get(i)).initPreviewBoard();
  }
  
  
  
  /**
   * Creates the "Board Looks" panel.
   */
   
  protected BoardModifyingPrefsPanel createBoardLooksPanel(){
    return new BoardLooksPanel(boardManager, previewBoard);
  }
   

  
  /**
   * Creates the "Move Input" panel.
   */
   
  protected BoardModifyingPrefsPanel createMoveInputPanel(){
    return new MoveInputPanel(boardManager, previewBoard);
  }



  /**
   * Creates the "Square Coordinates" panel.
   */
   
  protected BoardModifyingPrefsPanel createSquareCoordsPanel(){
    return new SquareCoordinatesPanel(boardManager, previewBoard);
  }
  
  
  
  /**
   * Creates the layout of this panel.
   */
  
  @Override
  protected void createLayout(){
    setLayout(new BorderLayout(5, 0));
    add(tabs, BorderLayout.WEST);
    add(boardPanel, BorderLayout.CENTER);
  }
  
  
  
}
