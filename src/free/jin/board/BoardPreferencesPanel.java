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

import free.jin.board.prefs.*;
import javax.swing.*;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.border.TitledBorder;
import free.jin.plugin.PreferencesPanel;
import free.jin.plugin.BadChangesException;
import free.util.SquareLayout;
import free.chess.Position;


/**
 * The preferences panel for the BoardManager plugin.
 */

public class BoardPreferencesPanel extends PreferencesPanel{



  /**
   * The BoardManager whose preferences panel this BoardPreferencesPanel is.
   */

  protected final BoardManager boardManager;
  
  
  
  /**
   * The preview board.
   */
   
  protected final JinBoard previewBoard;
  
  
  
  /**
   * The "Board Looks" panel.
   */
   
  protected final BoardModifyingPrefsPanel boardLooksPanel;
  
  
  
  /**
   * The "Move Input" panel.
   */
   
  protected final BoardModifyingPrefsPanel moveInputPanel;
  
  
  
  /**
   * The "Move Highlighting" panel.
   */
   
  protected final BoardModifyingPrefsPanel moveHighlightingPanel;
  
  
  
  /**
   * The "Square Coordinates" panel.
   */
   
  protected final BoardModifyingPrefsPanel squareCoordsPanel;



  /**
   * Creates a new BoardPreferencesPanel for the specified board manager. 
   */
   
  public BoardPreferencesPanel(BoardManager boardManager){
    this.boardManager = boardManager;
    this.previewBoard = createPreviewBoard();
    
    boardLooksPanel = createBoardLooksPanel();
    moveInputPanel = createMoveInputPanel();
    moveHighlightingPanel = createMoveHighlightingPanel();
    squareCoordsPanel = createSquareCoordsPanel();
    
    initPreviewBoard();
    
    JTabbedPane tabs = new JTabbedPane();
    tabs.addTab("Board Looks", null, boardLooksPanel, "Specify preferred piece set and board pattern");
    tabs.addTab("Move Input", null, moveInputPanel, "Specify how moves are made by the user");
    tabs.addTab("Move Highlighting", null, moveHighlightingPanel, "Specify how the last move is highlighted");
    tabs.addTab("Coordinates", null, squareCoordsPanel, "Specify how square coordinates are displayed");
    
    ChangeListener changeListener = new ChangeListener(){
      public void stateChanged(ChangeEvent evt){
        fireStateChanged();
      }
    };
    
    boardLooksPanel.addChangeListener(changeListener);
    moveInputPanel.addChangeListener(changeListener);
    moveHighlightingPanel.addChangeListener(changeListener);
    squareCoordsPanel.addChangeListener(changeListener);
    
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
      BorderFactory.createTitledBorder(null,  "Testing Board",
        TitledBorder.CENTER, TitledBorder.DEFAULT_POSITION),
      BorderFactory.createEmptyBorder(5, 5, 5, 5)));
    
    squarePanel.setAlignmentX(Component.CENTER_ALIGNMENT);
    resetPosition.setAlignmentX(Component.CENTER_ALIGNMENT);
    
    boardPanel.add(squarePanel);
    boardPanel.add(Box.createVerticalStrut(7));
    boardPanel.add(resetPosition);
    
    setLayout(new BorderLayout(5, 0));
    add(tabs, BorderLayout.WEST);
    add(boardPanel, BorderLayout.CENTER);
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
    
    boardLooksPanel.initPreviewBoard();
    moveInputPanel.initPreviewBoard();
    moveHighlightingPanel.initPreviewBoard();
    squareCoordsPanel.initPreviewBoard();
  }
  
  
  
  /**
   * Applies any changes the user has made.
   */
   
  public void applyChanges() throws BadChangesException{
    boardLooksPanel.applyChanges();
    moveInputPanel.applyChanges();
    moveHighlightingPanel.applyChanges();
    squareCoordsPanel.applyChanges();
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
