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
import free.jin.event.*;
import javax.swing.event.ChangeListener;
import javax.swing.event.ChangeEvent;
import java.awt.Container;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;
import free.jin.Game;
import free.jin.board.BoardManager;
import free.jin.board.JinBoard;
import free.jin.plugin.BadChangesException;
import free.util.AWTUtilities;
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
   * The "piece follows cursor" move visualization style selecting radio button.
   */
   
  protected final JRadioButton pieceFollowsCursor;
  
  
  
  /**
   * The "highlight target square" move visualization style selecting radio
   * button.
   */
   
  protected final JRadioButton highlightTargetSquare;
  
  
  
  /**
   * The color chooser for the highlight target square color.
   */
   
  protected final ColorChooser highlightColor;
  
  
  
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
   * The container holding all of the moving in advance mode selection UI. This
   * is needed so that it can be disabled during games.
   */
   
  private Container movingInAdvancePanel;
   
  
  
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
    autoPromote.setMnemonic('A');
    autoPromote.addActionListener(new ActionListener(){
      public void actionPerformed(ActionEvent evt){
        MoveInputPanel.this.previewBoard.setManualPromote(!autoPromote.isSelected());
        
        fireStateChanged();
      }
    });
    
    pieceFollowsCursor = new JRadioButton("Piece follows cursor",
      boardManager.getDraggedPieceStyle() == JBoard.NORMAL_DRAGGED_PIECE);
    highlightTargetSquare = new JRadioButton("Highlight target square",
      boardManager.getDraggedPieceStyle() == JBoard.HIGHLIGHT_TARGET_DRAGGED_PIECE);
    ButtonGroup moveVisualizationGroup = new ButtonGroup();
    moveVisualizationGroup.add(pieceFollowsCursor);
    moveVisualizationGroup.add(highlightTargetSquare);
    pieceFollowsCursor.setMnemonic('P');
    highlightTargetSquare.setMnemonic('H');
    ActionListener moveVisualizationListener = new ActionListener(){
      public void actionPerformed(ActionEvent evt){
        MoveInputPanel.this.previewBoard.setDraggedPieceStyle(getDraggedPieceStyle());
        
        fireStateChanged();
      }
    };
    pieceFollowsCursor.addActionListener(moveVisualizationListener);
    highlightTargetSquare.addActionListener(moveVisualizationListener);
    
    highlightColor = new ColorChooser("Highlight color:", boardManager.getDragSquareHighlightingColor());
    highlightColor.setMnemonic('t');
    highlightColor.addChangeListener(new ChangeListener(){
      public void stateChanged(ChangeEvent evt){
        MoveInputPanel.this.previewBoard.setDragSquareHighlightingColor(
          highlightColor.getColor());
          
        fireStateChanged();
      }
    });
    
    
    disallowMoveInAdvance = new JRadioButton("Disallow",
      boardManager.getMoveSendingMode() == BoardManager.LEGAL_CHESS_MOVE_SENDING_MODE);
    immediateSendMove = new JRadioButton("Send move immediately",
      boardManager.getMoveSendingMode() == BoardManager.PREDRAG_MOVE_SENDING_MODE);
    premove = new JRadioButton("Wait for turn (premove)",
      boardManager.getMoveSendingMode() == BoardManager.PREMOVE_MOVE_SENDING_MODE);
    ButtonGroup movingInAdvanceButtonGroup = new ButtonGroup();
    movingInAdvanceButtonGroup.add(disallowMoveInAdvance);
    movingInAdvanceButtonGroup.add(immediateSendMove);
    movingInAdvanceButtonGroup.add(premove);
    disallowMoveInAdvance.setMnemonic('D');
    immediateSendMove.setMnemonic('S');
    premove.setMnemonic('W');
    ActionListener movingInAdvanceListener = new ActionListener(){
      public void actionPerformed(ActionEvent evt){
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
    
    this.movingInAdvancePanel = movingInAdvancePanel;

    highlightTargetSquare.addChangeListener(new ChangeListener(){
      public void stateChanged(ChangeEvent evt){
        highlightColor.setEnabled(highlightTargetSquare.isSelected());
      }
    });
    highlightColor.setEnabled(highlightTargetSquare.isSelected());
    
    
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
   * Returns the currently selected dragged piece style.
   */
   
  private int getDraggedPieceStyle(){
    if (pieceFollowsCursor.isSelected())
      return JBoard.NORMAL_DRAGGED_PIECE;
    else if (highlightTargetSquare.isSelected())
      return JBoard.HIGHLIGHT_TARGET_DRAGGED_PIECE;
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
    previewBoard.setDraggedPieceStyle(getDraggedPieceStyle());
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
    highlightTargetSquare.setAlignmentX(JComponent.LEFT_ALIGNMENT);
    highlightColor.setAlignmentX(JComponent.LEFT_ALIGNMENT);
    
    panel.add(pieceFollowsCursor);
    panel.add(highlightTargetSquare);
    panel.add(highlightColor);
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
    panel.add(Box.createVerticalGlue());

    return panel;    
  }
  
  
  
  /**
   * A game listener which allows us to disable the "moving in advance" panel
   * during a game.
   */
   
  private final GameListener gameListener = new GameAdapter(){
    
    public void gameStarted(GameStartEvent evt){
      Game game = evt.getGame();
      int gameType = game.getGameType();
      
      if ((gameType == Game.MY_GAME) && game.isPlayed())
        userStartedPlaying();
  
      game.addPropertyChangeListener(new PropertyChangeListener(){
        public void propertyChange(PropertyChangeEvent evt){
          if ("played".equals(evt.getPropertyName())){
            Game game = (Game)evt.getSource();
            if (game.isPlayed())
              userStartedPlaying();
            else
              userStoppedPlaying();
          }
        }
      });
    
    }
    
    public void gameEnded(GameEndEvent evt){
      Game game = evt.getGame();
      int gameType = game.getGameType();
  
      if ((gameType == Game.MY_GAME) && game.isPlayed())
        userStoppedPlaying();
    }
    
    
    private int playedGamesCount = 0;
    
    private void userStartedPlaying(){
      playedGamesCount++;
      
      if (playedGamesCount == 1)
        AWTUtilities.setContainerEnabled(movingInAdvancePanel, false);
    }
    
    private void userStoppedPlaying(){
      playedGamesCount--;
      
      if (playedGamesCount == 0)
        AWTUtilities.setContainerEnabled(movingInAdvancePanel, true);
    }
     
  };
  
  
  
  /**
   * Registers a game listener so that we can disable the moving in advance
   * panel during a game.
   */
   
  public void addNotify(){
    super.addNotify();
    
    boardManager.getConn().getListenerManager().addGameListener(gameListener);
  }
  
  
  
  /**
   * Unregisters the game listener registered by <code>addNotify</code>.
   */
   
  public void removeNotify(){
    super.removeNotify();
    
    boardManager.getConn().getListenerManager().removeGameListener(gameListener); 
  }
  

  
  /**
   * Applies any changes made by the user.
   */
   
  public void applyChanges() throws BadChangesException{
    boardManager.setMoveInputStyle(getMoveInputStyle());
    boardManager.setAutoPromote(autoPromote.isSelected());
    
    boardManager.setDraggedPieceStyle(getDraggedPieceStyle());
    boardManager.setDragSquareHighlightingColor(highlightColor.getColor());
    
   boardManager.setMoveSendingMode(getMoveSendingMode());   
  }
  
  
   
}