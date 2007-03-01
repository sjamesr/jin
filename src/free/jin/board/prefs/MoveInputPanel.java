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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import free.chess.Chess;
import free.chess.JBoard;
import free.chess.Move;
import free.chess.Position;
import free.chess.Square;
import free.chess.event.MoveEvent;
import free.chess.event.MoveListener;
import free.jin.BadChangesException;
import free.jin.I18n;
import free.jin.board.BoardManager;
import free.jin.board.JinBoard;
import free.util.swing.ColorChooser;
import free.util.swing.PreferredSizedPanel;


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
   * The radio button for no move highlighting.
   */
   
  private final JRadioButton none;
  
  
  
  /**
   * The radio button for target square move highlighting.
   */
   
  private final JRadioButton targetSquare;
  
  
  
  /**
   * The radio button for both squares move highlighting.
   */
   
  private final JRadioButton bothSquares;
  
  
  
  /**
   * The radio button for arrow move highlighting.
   */
   
  private final JRadioButton arrow;
  
  
  
  /**
   * The checkbox for whether the user's moves should be highlighted.
   */
   
  private final JCheckBox highlightOwnMoves;
  
  
  
  /**
   * The color chooser for the move highlighting color.
   */
   
  private final ColorChooser highlightColor;
  
  
  
  /**
   * The last move made on the board.
   */
   
  private Move lastMove = null;
  
  
  
  
  /**
   * Creates a new <code>MoveInputPanel</code> for the specified
   * <code>BoardManager</code> and with the specified preview board. 
   */
   
  public MoveInputPanel(BoardManager boardManager, JinBoard previewBoard){
    super(boardManager, previewBoard);
    
    I18n i18n = I18n.get(MoveInputPanel.class);
    
    dragndrop = i18n.createRadioButton("dragndropRadioButton");
    clicknclick = i18n.createRadioButton("clicknclickRadioButton");
    
    dragndrop.setSelected(boardManager.getMoveInputStyle() == JBoard.DRAG_N_DROP);
    clicknclick.setSelected(boardManager.getMoveInputStyle() == JBoard.CLICK_N_CLICK);
    
    ButtonGroup moveInputGroup = new ButtonGroup();
    moveInputGroup.add(dragndrop);
    moveInputGroup.add(clicknclick);
    ActionListener moveInputListener = new ActionListener(){
      public void actionPerformed(ActionEvent evt){
        MoveInputPanel.this.previewBoard.setMoveInputStyle(getMoveInputStyle());
        
        fireStateChanged();
      }
    };
    dragndrop.addActionListener(moveInputListener);
    clicknclick.addActionListener(moveInputListener);
      
    autoPromote = i18n.createCheckBox("autoPromoteCheckBox");
    autoPromote.setSelected(boardManager.isAutoPromote());
    autoPromote.addActionListener(new ActionListener(){
      public void actionPerformed(ActionEvent evt){
        MoveInputPanel.this.previewBoard.setManualPromote(!autoPromote.isSelected());
        
        fireStateChanged();
      }
    });
    
    pieceFollowsCursor = i18n.createCheckBox("pieceFollowsCursorCheckBox");
    pieceFollowsCursor.setSelected(boardManager.isPieceFollowsCursor());
    pieceFollowsCursor.addActionListener(new ActionListener(){
      public void actionPerformed(ActionEvent evt){
        MoveInputPanel.this.previewBoard.setPieceFollowsCursor(pieceFollowsCursor.isSelected());
        
        fireStateChanged();
      }
    });
      
    highlightMadeMoveSquares = i18n.createCheckBox("highlightMadeMoveSquaresCheckBox");
    highlightMadeMoveSquares.setSelected(boardManager.isHighlightMadeMoveSquares());
    highlightMadeMoveSquares.addActionListener(new ActionListener(){
      public void actionPerformed(ActionEvent evt){
        boolean selected = highlightMadeMoveSquares.isSelected();
        MoveInputPanel.this.previewBoard.setHighlightMadeMoveSquares(selected);
        madeMoveSquaresHighlightColor.setEnabled(selected);
       
        fireStateChanged();
      }
    });
    
    
    madeMoveSquaresHighlightColor = i18n.createColorChooser("madeMoveSquaresHighlightColorChooser");
    madeMoveSquaresHighlightColor.setColor(boardManager.getMadeMoveSquaresHighlightColor());
    madeMoveSquaresHighlightColor.addChangeListener(new ChangeListener(){
      public void stateChanged(ChangeEvent evt){
        MoveInputPanel.this.previewBoard.setMadeMoveSquaresHighlightColor(
          madeMoveSquaresHighlightColor.getColor());
          
        fireStateChanged();
      }
    });
    
    
    disallowMoveInAdvance = i18n.createRadioButton("disallowMoveInAdvanceRadioButton");
    immediateSendMove = i18n.createRadioButton("immediateSendMoveRadioButton");
    premove = i18n.createRadioButton("premoveRadioButton");
    
    disallowMoveInAdvance.setSelected(boardManager.getMoveSendingMode() == BoardManager.LEGAL_CHESS_MOVE_SENDING_MODE);
    immediateSendMove.setSelected(boardManager.getMoveSendingMode() == BoardManager.PREDRAG_MOVE_SENDING_MODE);
    premove.setSelected(boardManager.getMoveSendingMode() == BoardManager.PREMOVE_MOVE_SENDING_MODE);
    
    ButtonGroup movingInAdvanceButtonGroup = new ButtonGroup();
    movingInAdvanceButtonGroup.add(disallowMoveInAdvance);
    movingInAdvanceButtonGroup.add(immediateSendMove);
    movingInAdvanceButtonGroup.add(premove);
    ActionListener movingInAdvanceListener = new ActionListener(){
      public void actionPerformed(ActionEvent evt){
        BoardManager boardManager = MoveInputPanel.this.boardManager;
        if (boardManager.isUserPlaying()){
          I18n i18n = I18n.get(MoveInputPanel.class);
          i18n.error("moveInAdvanceChangeError", MoveInputPanel.this);
            
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
    
    int highlightStyle = boardManager.getMoveHighlightingStyle();
    none = i18n.createRadioButton("noMoveHighlightRadioButton");
    targetSquare = i18n.createRadioButton("targetSquareHighlightRadioButton");
    bothSquares = i18n.createRadioButton("bothSquaresHighlightRadioButton");
    arrow = i18n.createRadioButton("arrowHighlightRadioButton");
    
    none.setSelected(highlightStyle == JBoard.NO_MOVE_HIGHLIGHTING);
    targetSquare.setSelected(highlightStyle == JBoard.TARGET_SQUARE_MOVE_HIGHLIGHTING);
    bothSquares.setSelected(highlightStyle == JBoard.BOTH_SQUARES_MOVE_HIGHLIGHTING);
    arrow.setSelected(highlightStyle == JBoard.ARROW_MOVE_HIGHLIGHTING);
    
    highlightOwnMoves = i18n.createCheckBox("highlightOwnMovesCheckBox");
    highlightOwnMoves.setSelected(boardManager.isHighlightingOwnMoves());
    
    highlightColor = i18n.createColorChooser("moveHighlightColorChooser");
    highlightColor.setColor(boardManager.getMoveHighlightingColor());
    
    ButtonGroup group = new ButtonGroup();
    group.add(none);
    group.add(targetSquare);
    group.add(bothSquares);
    group.add(arrow);
    
    ActionListener styleListener = new ActionListener(){
      public void actionPerformed(ActionEvent evt){
        MoveInputPanel.this.previewBoard.setMoveHighlightingStyle(getMoveHighlightingStyle());
        
        highlightOwnMoves.setEnabled(!none.isSelected());
        highlightColor.setEnabled(!none.isSelected());
        
        fireStateChanged();
      }
    };
    none.addActionListener(styleListener);
    targetSquare.addActionListener(styleListener);
    bothSquares.addActionListener(styleListener);
    arrow.addActionListener(styleListener);
    
    highlightOwnMoves.addActionListener(new ActionListener(){
      public void actionPerformed(ActionEvent evt){
        if (lastMove.getPlayer().isWhite())
          MoveInputPanel.this.previewBoard.setHighlightedMove(highlightOwnMoves.isSelected() ? lastMove : null);
        
        fireStateChanged();
      }
    });
    
    highlightColor.addChangeListener(new ChangeListener(){
      public void stateChanged(ChangeEvent evt){
        MoveInputPanel.this.previewBoard.setMoveHighlightingColor(highlightColor.getColor());
        
        fireStateChanged();
      }
    });
    
    
    previewBoard.getPosition().addMoveListener(new MoveListener(){
      public void moveMade(MoveEvent evt){
        Move move = evt.getMove();
        if (move.getPlayer().isBlack() || highlightOwnMoves.isSelected())
          MoveInputPanel.this.previewBoard.setHighlightedMove(move);
        else
          MoveInputPanel.this.previewBoard.setHighlightedMove(null);
          
        lastMove = move;
      }
    });

    
    
    JComponent moveInputPanel = createMoveInputUI();
    JComponent promotePanel = createPromotionUI();
    JComponent moveVisualizationPanel = createMoveVisualizationUI();
    JComponent movingInAdvancePanel = createMovingInAdvanceUI();
    JComponent moveHighlightPanel = createMoveHighlightUI();
    
    madeMoveSquaresHighlightColor.setEnabled(highlightMadeMoveSquares.isSelected());
    
    
    JPanel row1Panel = new PreferredSizedPanel();
    row1Panel.setLayout(new BoxLayout(row1Panel, BoxLayout.X_AXIS));
    
    moveInputPanel.setAlignmentY(JComponent.TOP_ALIGNMENT);
    promotePanel.setAlignmentY(JComponent.TOP_ALIGNMENT);
                                 
    row1Panel.add(moveInputPanel);
    row1Panel.add(Box.createHorizontalStrut(10));
    row1Panel.add(promotePanel);
    row1Panel.add(Box.createHorizontalGlue());
    
    JPanel row2Panel = new PreferredSizedPanel();
    row2Panel.setLayout(new BoxLayout(row2Panel, BoxLayout.X_AXIS));
    row2Panel.add(moveVisualizationPanel);
    row2Panel.add(Box.createHorizontalStrut(10));
    row2Panel.add(movingInAdvancePanel);
    row2Panel.add(Box.createHorizontalGlue());
    
    JPanel row3Panel = new PreferredSizedPanel();
    row3Panel.setLayout(new BoxLayout(row3Panel, BoxLayout.X_AXIS));
    row3Panel.add(moveHighlightPanel);
    
    setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
    setBorder(BorderFactory.createEmptyBorder(15, 10, 15, 10));
    
    row1Panel.setAlignmentX(JComponent.LEFT_ALIGNMENT);
    row2Panel.setAlignmentX(JComponent.LEFT_ALIGNMENT);
    row3Panel.setAlignmentX(JComponent.LEFT_ALIGNMENT);
    
    add(row1Panel);
    add(Box.createVerticalStrut(10));
    add(row2Panel);
    add(Box.createVerticalStrut(10));
    add(row3Panel);
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
   * Returns the currently selected move highlighting style.
   */
   
  private int getMoveHighlightingStyle(){
    if (none.isSelected())
      return JBoard.NO_MOVE_HIGHLIGHTING;
    else if (targetSquare.isSelected())
      return JBoard.TARGET_SQUARE_MOVE_HIGHLIGHTING;
    else if (bothSquares.isSelected())
      return JBoard.BOTH_SQUARES_MOVE_HIGHLIGHTING;
    else if (arrow.isSelected())
      return JBoard.ARROW_MOVE_HIGHLIGHTING;
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
    
    previewBoard.setMoveHighlightingStyle(getMoveHighlightingStyle());
    previewBoard.setMoveHighlightingColor(highlightColor.getColor());
    
    Position pos = previewBoard.getPosition();
    
    Move move = Chess.getInstance().createMove(pos,
      Square.parseSquare("f5"), Square.parseSquare("c8"), null, "Bc8");
      
    pos.makeMove(move);
    previewBoard.setHighlightedMove(move);
  }
  
  
  
  /**
   * Creates the move input panel.
   */
   
  private JComponent createMoveInputUI(){
    JPanel panel = new PreferredSizedPanel();
    panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
    panel.setBorder(BorderFactory.createCompoundBorder(
      I18n.get(MoveInputPanel.class).createTitledBorder("moveInputPanel"),
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
      I18n.get(MoveInputPanel.class).createTitledBorder("promotionPanel"),
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
      I18n.get(MoveInputPanel.class).createTitledBorder("moveVisualizationPanel"),
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
    I18n i18n = I18n.get(MoveInputPanel.class);
    
    JPanel panel = new JPanel();
    panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
    panel.setBorder(BorderFactory.createCompoundBorder(
      i18n.createTitledBorder("movingInAdvancePanel"),
      BorderFactory.createEmptyBorder(0, 5, 5, 5)));
    
    disallowMoveInAdvance.setAlignmentX(JComponent.LEFT_ALIGNMENT);
    immediateSendMove.setAlignmentX(JComponent.LEFT_ALIGNMENT);
    premove.setAlignmentX(JComponent.LEFT_ALIGNMENT);
    
    panel.add(disallowMoveInAdvance);
    panel.add(immediateSendMove);
    panel.add(premove);
    panel.add(Box.createVerticalStrut(5));
    String [] warnings = i18n.getString("moveInAdvanceChangeWarning").split("\n");
    for (int i = 0; i < warnings.length; i++)
      panel.add(new JLabel(warnings[i]));
    panel.add(Box.createVerticalGlue());

    return panel;    
  }
  
  
  
  private JComponent createMoveHighlightUI(){
    I18n i18n = I18n.get(MoveInputPanel.class);

    JPanel contentPanel = new PreferredSizedPanel();
    contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
    contentPanel.setBorder(BorderFactory.createCompoundBorder(
      i18n.createTitledBorder("moveHighlightPanel"),
      BorderFactory.createEmptyBorder(0, 5, 5, 5)));
    
    none.setAlignmentX(JComponent.LEFT_ALIGNMENT);
    targetSquare.setAlignmentX(JComponent.LEFT_ALIGNMENT);
    bothSquares.setAlignmentX(JComponent.LEFT_ALIGNMENT);
    arrow.setAlignmentX(JComponent.LEFT_ALIGNMENT);
    highlightOwnMoves.setAlignmentX(JComponent.LEFT_ALIGNMENT);
    highlightColor.setAlignmentX(JComponent.LEFT_ALIGNMENT);
    
    contentPanel.add(none);
    contentPanel.add(targetSquare);
    contentPanel.add(bothSquares);
    contentPanel.add(arrow);
    contentPanel.add(highlightOwnMoves);
    contentPanel.add(highlightColor);
    contentPanel.add(Box.createVerticalGlue());
    
    contentPanel.setAlignmentX(JComponent.LEFT_ALIGNMENT);
    
    return contentPanel;
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
    
    boardManager.setMoveHighlightingStyle(getMoveHighlightingStyle());
    boardManager.setHighlightingOwnMoves(highlightOwnMoves.isSelected());
    boardManager.setMoveHighlightingColor(highlightColor.getColor());
  }
  
  
   
}