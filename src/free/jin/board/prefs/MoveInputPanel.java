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

import javax.swing.*;
import javax.swing.event.ChangeListener;
import javax.swing.event.ChangeEvent;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import free.jin.OptionPanel;
import free.jin.board.BoardManager;
import free.jin.board.JinBoard;
import free.jin.plugin.BadChangesException;
import free.util.swing.ColorChooser;
import free.util.swing.PreferredSizedPanel;
import free.chess.JBoard;


/**
 * A preferences panel allowing the user to modify move input preferences.
 */
 
public class MoveInputPanel extends BoardModifyingPrefsPanel{
  
  
  
  /**
   * The "drag and drop" move input style selecting radio button.
   */
   
  protected final JRadioButton dragndrop;
  
  
  
  /**
   * The "click and click" move input style selecting radio button.
   */
   
  protected final JRadioButton clicknclick;
  
  
  
  /**
   * The "auto promote" checkbox.
   */
   
  protected final JCheckBox autoPromote;
  
  
  
  /**
   * The "piece follows cursor" checkbox.
   */
   
  protected final JCheckBox pieceFollowsCursor;
  
  
  
  /**
   * The "highlight move squares" checkbox.
   */
   
  protected final JCheckBox highlightMadeMoveSquares;
  
  
  
  /**
   * The color chooser for the made move squares highlight color.
   */
   
  protected final ColorChooser madeMoveSquaresHighlightColor;
  
  
  
  /**
   * The radio button for disallowing making moves when it isn't the user's
   * turn. 
   */
   
  protected final JRadioButton disallowMoveInAdvance;
  
  
  
  /**
   * The radio button for sending a move made when it isn't the user's turn
   * immediately. 
   */
   
  protected final JRadioButton immediateSendMove;
  
  
  
  /**
   * The radio button for enabling premove.
   */
   
  protected final JRadioButton premove;
  
  
  
  
  /**
   * Creates a new <code>MoveInputPanel</code> for the specified
   * <code>BoardManager</code> and with the specified preview board. 
   */
   
  public MoveInputPanel(BoardManager boardManager, JinBoard previewBoard){
    super(boardManager, previewBoard);
    
    dragndrop = new JRadioButton("Drag and drop", 
      boardManager.getMoveInputStyle() == JBoard.DRAG_N_DROP);
    clicknclick = new JRadioButton("Click and click",
      boardManager.getMoveInputStyle() == JBoard.CLICK_N_CLICK);
    
    dragndrop.setToolTipText("Moves are entered by dragging pieces and "+
                             "dropping them into their target squares");
    clicknclick.setToolTipText("Moves are entered by clicking a piece, moving it "+
                               "to its target square and then clicking again");
    
    ButtonGroup moveInputGroup = new ButtonGroup();
    moveInputGroup.add(dragndrop);
    moveInputGroup.add(clicknclick);
    dragndrop.setMnemonic('D');
    clicknclick.setMnemonic('C');
    ActionListener moveInputListener = new ActionListener(){
      public void actionPerformed(ActionEvent evt){
        MoveInputPanel.this.previewBoard.setMoveInputStyle(getMoveInputStyle());
        
        fireStateChanged();
      }
    };
    dragndrop.addActionListener(moveInputListener);
    clicknclick.addActionListener(moveInputListener);
      
    autoPromote = new JCheckBox("Auto-Promote", boardManager.isAutoPromote());
    autoPromote.setToolTipText("Automatically promotes to a queen instead of "+
                               "asking the user which piece to promote to");
    autoPromote.setMnemonic('A');
    autoPromote.addActionListener(new ActionListener(){
      public void actionPerformed(ActionEvent evt){
        MoveInputPanel.this.previewBoard.setManualPromote(!autoPromote.isSelected());
        
        fireStateChanged();
      }
    });
    
    pieceFollowsCursor = new JCheckBox("Piece follows cursor", 
      boardManager.isPieceFollowsCursor());
    pieceFollowsCursor.setToolTipText("When in the process of moving a piece, the piece will follow the mouse cursor");
    pieceFollowsCursor.setMnemonic('P');
    pieceFollowsCursor.addActionListener(new ActionListener(){
      public void actionPerformed(ActionEvent evt){
        MoveInputPanel.this.previewBoard.setPieceFollowsCursor(pieceFollowsCursor.isSelected());
        
        fireStateChanged();
      }
    });
      
    highlightMadeMoveSquares = new JCheckBox("Highlight move squares",
      boardManager.isHighlightMadeMoveSquares());
    highlightMadeMoveSquares.setToolTipText("When in the process of moving a piece, the origin square and the square under the cursor are highlighted");
    highlightMadeMoveSquares.setMnemonic('H');
    highlightMadeMoveSquares.addActionListener(new ActionListener(){
      public void actionPerformed(ActionEvent evt){
        boolean selected = highlightMadeMoveSquares.isSelected();
        MoveInputPanel.this.previewBoard.setHighlightMadeMoveSquares(selected);
        madeMoveSquaresHighlightColor.setEnabled(selected);
       
        fireStateChanged();
      }
    });
    
    
    madeMoveSquaresHighlightColor = 
      new ColorChooser("Highlight color:", boardManager.getMadeMoveSquaresHighlightColor());
    madeMoveSquaresHighlightColor.setToolTipText("The color of the highlight \"Highlight move squares\" mode");
    madeMoveSquaresHighlightColor.setMnemonic('t');
    madeMoveSquaresHighlightColor.addChangeListener(new ChangeListener(){
      public void stateChanged(ChangeEvent evt){
        MoveInputPanel.this.previewBoard.setMadeMoveSquaresHighlightColor(
          madeMoveSquaresHighlightColor.getColor());
          
        fireStateChanged();
      }
    });
    
    
    disallowMoveInAdvance = new JRadioButton("Disallow",
      boardManager.getMoveSendingMode() == BoardManager.LEGAL_CHESS_MOVE_SENDING_MODE);
    immediateSendMove = new JRadioButton("Send move immediately",
      boardManager.getMoveSendingMode() == BoardManager.PREDRAG_MOVE_SENDING_MODE);
    premove = new JRadioButton("Wait for turn (premove)",
      boardManager.getMoveSendingMode() == BoardManager.PREMOVE_MOVE_SENDING_MODE);
    
    disallowMoveInAdvance.setToolTipText("Disallows entering a move when it isn't your turn");
    immediateSendMove.setToolTipText("Moves entered when it isn't your turn are sent immediately to the server");
    premove.setToolTipText("Moves entered when it isn't your turn are sent when it becomes your turn");
    
    ButtonGroup movingInAdvanceButtonGroup = new ButtonGroup();
    movingInAdvanceButtonGroup.add(disallowMoveInAdvance);
    movingInAdvanceButtonGroup.add(immediateSendMove);
    movingInAdvanceButtonGroup.add(premove);
    disallowMoveInAdvance.setMnemonic('D');
    immediateSendMove.setMnemonic('S');
    premove.setMnemonic('W');
    ActionListener movingInAdvanceListener = new ActionListener(){
      public void actionPerformed(ActionEvent evt){
        BoardManager boardManager = MoveInputPanel.this.boardManager;
        if (boardManager.isUserPlaying()){
          OptionPanel.error("Unable to change setting",
            "Moving in advance settings may not be modified while playing a game");
            
          disallowMoveInAdvance.setSelected(
            boardManager.getMoveSendingMode() == BoardManager.LEGAL_CHESS_MOVE_SENDING_MODE);
          immediateSendMove.setSelected(
            boardManager.getMoveSendingMode() == BoardManager.PREDRAG_MOVE_SENDING_MODE);
          premove.setSelected(
            boardManager.getMoveSendingMode() == BoardManager.PREMOVE_MOVE_SENDING_MODE);
            
          return;
        }
        
        fireStateChanged();
      }
    };
    disallowMoveInAdvance.addActionListener(movingInAdvanceListener);
    immediateSendMove.addActionListener(movingInAdvanceListener);
    premove.addActionListener(movingInAdvanceListener);
    
    
    JComponent moveInputPanel = createMoveInputUI();
    JComponent promotePanel = createPromotionUI();
    JComponent moveVisualizationPanel = createMoveVisualizationUI();
    JComponent movingInAdvancePanel = createMovingInAdvanceUI();
    
    madeMoveSquaresHighlightColor.setEnabled(highlightMadeMoveSquares.isSelected());
    
    
    JPanel topPanel = new PreferredSizedPanel();
    topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.X_AXIS));
    
    moveInputPanel.setAlignmentY(JComponent.TOP_ALIGNMENT);
    promotePanel.setAlignmentY(JComponent.TOP_ALIGNMENT);
                                 
    topPanel.add(moveInputPanel);
    topPanel.add(Box.createHorizontalStrut(10));
    topPanel.add(promotePanel);
    topPanel.add(Box.createHorizontalGlue());
    
    JPanel bottomPanel = new PreferredSizedPanel();
    bottomPanel.setLayout(new BoxLayout(bottomPanel, BoxLayout.X_AXIS));
    bottomPanel.add(moveVisualizationPanel);
    bottomPanel.add(Box.createHorizontalStrut(10));
    bottomPanel.add(movingInAdvancePanel);
    bottomPanel.add(Box.createHorizontalGlue());
    
    
    setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
    setBorder(BorderFactory.createEmptyBorder(15, 10, 15, 10));
    
    topPanel.setAlignmentX(JComponent.LEFT_ALIGNMENT);
    bottomPanel.setAlignmentX(JComponent.LEFT_ALIGNMENT);
    
    add(topPanel);
    add(Box.createVerticalStrut(10));
    add(bottomPanel);
    add(Box.createVerticalGlue());
  }
  
  
  
  /**
   * Returns the currently selected move input style.
   */
   
  private int getMoveInputStyle(){
    if (dragndrop.isSelected())
      return JBoard.DRAG_N_DROP;
    else if (clicknclick.isSelected())
      return JBoard.CLICK_N_CLICK;
    else
      throw new IllegalStateException("None of the radio buttons are selected");
  }
  
  
  
  /**
   * Returns the currently selected move sending mode.
   */
   
  private int getMoveSendingMode(){
    if (disallowMoveInAdvance.isSelected())
      return BoardManager.LEGAL_CHESS_MOVE_SENDING_MODE;
    else if (immediateSendMove.isSelected())
      return BoardManager.PREDRAG_MOVE_SENDING_MODE;
    else if (premove.isSelected())
      return BoardManager.PREMOVE_MOVE_SENDING_MODE;
    else
      throw new IllegalStateException("None of the radio buttons are selected");
  }
  
  
  
  
  /**
   * Sets the initial properties of the preview board.
   */
   
  public void initPreviewBoard(){
    previewBoard.setMoveInputStyle(getMoveInputStyle());
    previewBoard.setPieceFollowsCursor(pieceFollowsCursor.isSelected());
    previewBoard.setHighlightMadeMoveSquares(highlightMadeMoveSquares.isSelected());
    previewBoard.setManualPromote(!autoPromote.isSelected());
  }
  
  
  
  /**
   * Creates the move input panel.
   */
   
  private JComponent createMoveInputUI(){
    JPanel panel = new PreferredSizedPanel();
    panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
    panel.setBorder(BorderFactory.createCompoundBorder(
      BorderFactory.createTitledBorder("Move Input"),
      BorderFactory.createEmptyBorder(0, 5, 5, 5)));
        
    dragndrop.setAlignmentX(JComponent.LEFT_ALIGNMENT);
    clicknclick.setAlignmentX(JComponent.LEFT_ALIGNMENT);
    
    panel.add(dragndrop);
    panel.add(clicknclick);
    panel.add(Box.createVerticalGlue());
    
    return panel;
  }
  
  
  
  /**
   * Creates the promotion panel.
   */
   
  private JComponent createPromotionUI(){
    JPanel panel = new JPanel();
    panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
    panel.setBorder(BorderFactory.createCompoundBorder(
      BorderFactory.createTitledBorder("Promoting"),
      BorderFactory.createEmptyBorder(0, 5, 5, 5)));
    
    autoPromote.setAlignmentX(JComponent.LEFT_ALIGNMENT);
    
    panel.add(autoPromote);
    panel.add(Box.createVerticalGlue());
    
    return panel;
  }

  
  
  /**
   * Creates the move visualization panel.
   */
   
  private JComponent createMoveVisualizationUI(){
    JPanel panel = new JPanel();
    panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
    panel.setBorder(BorderFactory.createCompoundBorder(
      BorderFactory.createTitledBorder("Move Visualization"),
      BorderFactory.createEmptyBorder(0, 5, 5, 5)));

    pieceFollowsCursor.setAlignmentX(JComponent.LEFT_ALIGNMENT);    
    highlightMadeMoveSquares.setAlignmentX(JComponent.LEFT_ALIGNMENT);
    madeMoveSquaresHighlightColor.setAlignmentX(JComponent.LEFT_ALIGNMENT);
    
    panel.add(pieceFollowsCursor);
    panel.add(highlightMadeMoveSquares);
    panel.add(madeMoveSquaresHighlightColor);
    panel.add(Box.createVerticalGlue());
    
    return panel;
  }
  
  
  
  /**
   * Creates the moving in advance panel.
   */
   
  private JComponent createMovingInAdvanceUI(){
    JPanel panel = new JPanel();
    panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
    panel.setBorder(BorderFactory.createCompoundBorder(
      BorderFactory.createTitledBorder("Moving in Advance"),
      BorderFactory.createEmptyBorder(0, 5, 5, 5)));
    
    disallowMoveInAdvance.setAlignmentX(JComponent.LEFT_ALIGNMENT);
    immediateSendMove.setAlignmentX(JComponent.LEFT_ALIGNMENT);
    premove.setAlignmentX(JComponent.LEFT_ALIGNMENT);
    
    panel.add(disallowMoveInAdvance);
    panel.add(immediateSendMove);
    panel.add(premove);
    panel.add(Box.createVerticalStrut(5));
    panel.add(new JLabel("These settings may not")); 
    panel.add(new JLabel("be modified during a game")); 
    panel.add(Box.createVerticalGlue());

    return panel;    
  }
  
  

  
  /**
   * Applies any changes made by the user.
   */
   
  public void applyChanges() throws BadChangesException{
    boardManager.setMoveInputStyle(getMoveInputStyle());
    boardManager.setAutoPromote(autoPromote.isSelected());
    
    boardManager.setPieceFollowsCursor(pieceFollowsCursor.isSelected());
    boardManager.setHighlightMadeMoveSquares(highlightMadeMoveSquares.isSelected());
    boardManager.setMadeMoveSquaresHighlightColor(madeMoveSquaresHighlightColor.getColor());
    
    boardManager.setMoveSendingMode(getMoveSendingMode());   
  }
  
  
   
}