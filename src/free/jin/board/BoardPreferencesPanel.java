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

import free.chess.Position;
import free.jin.board.prefs.*;
import free.jin.ui.CompositePreferencesPanel;
import free.jin.ui.PreferencesPanel;
import free.util.SquareLayout;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.*;


/**
 * The preferences panel for the <code>BoardManager</code> plugin.
 */

public class BoardPreferencesPanel extends CompositePreferencesPanel{



  /**
   * The BoardManager whose preferences panel this BoardPreferencesPanel is.
   */

  protected final BoardManager boardManager;
  
  
  
  /**
   * The preview board.
   */
   
  protected final JinBoard previewBoard;
  
  
  
  /**
   * The tabbed pane.
   */
  
  private final JTabbedPane tabs = new JTabbedPane();
  
  
  
  /**
   * Creates a new BoardPreferencesPanel for the specified board manager. 
   */
   
  public BoardPreferencesPanel(BoardManager boardManager){
    this.boardManager = boardManager;
    this.previewBoard = createPreviewBoard();
    
    JPanel squarePanel = new JPanel(new SquareLayout());
    squarePanel.add(previewBoard);
    previewBoard.setAlignmentX(Component.CENTER_ALIGNMENT);
    previewBoard.setAlignmentY(Component.CENTER_ALIGNMENT);
    previewBoard.setPreferredSize(new Dimension(320, 320));
    
    JButton resetPosition = new JButton("Reset Position");
    resetPosition.setMnemonic('R');
    resetPosition.addActionListener(new ActionListener(){
      public void actionPerformed(ActionEvent evt){
        initPreviewBoard();
      }
    });
    
    JPanel boardPanel = new JPanel();
    boardPanel.setLayout(new BoxLayout(boardPanel, BoxLayout.Y_AXIS));
    boardPanel.setBorder(BorderFactory.createCompoundBorder(
      BorderFactory.createTitledBorder(null,  "Testing Board"),
      BorderFactory.createEmptyBorder(5, 5, 5, 5)));
    
    squarePanel.setAlignmentX(Component.CENTER_ALIGNMENT);
    resetPosition.setAlignmentX(Component.CENTER_ALIGNMENT);
    
    boardPanel.add(squarePanel);
    boardPanel.add(Box.createVerticalStrut(7));
    boardPanel.add(resetPosition);
    
    setLayout(new BorderLayout(5, 0));
    add(tabs, BorderLayout.WEST);
    add(boardPanel, BorderLayout.CENTER);
    
    addPanel(createBoardLooksPanel(), "Board Looks", "Specify preferred piece set and board pattern");
    addPanel(createMoveInputPanel(), "Move Input", "Specify how moves are made by the user");
    addPanel(createMoveHighlightingPanel(), "Move Highlighting", "Specify how the last move is highlighted");
    addPanel(createSquareCoordsPanel(), "Coordinates", "Specify how square coordinates are displayed");
    
    initPreviewBoard();
  }
  
  
  
  /**
   * Adds the specified panel to a new tab.
   */
  
  protected void addPanelToUi(PreferencesPanel panel, String name, String tooltip){
    tabs.addTab(name, null, panel, tooltip);
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
      ((BoardModifyingPrefsPanel)panels.elementAt(i)).initPreviewBoard();
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
   * Creates the "Move Highlighting" panel.
   */
   
  protected BoardModifyingPrefsPanel createMoveHighlightingPanel(){
    return new MoveHighlightPanel(boardManager, previewBoard);
  }



  /**
   * Creates the "Square Coordinates" panel.
   */
   
  protected BoardModifyingPrefsPanel createSquareCoordsPanel(){
    return new SquareCoordinatesPanel(boardManager, previewBoard);
  }

  

}
