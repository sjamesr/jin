/**
 * Jin - a chess client for internet chess servers. More information is available at
 * http://www.jinchess.com/. Copyright (C) 2004 Alexander Maryanovsky. All rights reserved.
 *
 * This program is free software; you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation; either version 2 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program; if
 * not, write to the Free Software Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA
 * 02111-1307, USA.
 */
package free.jin.board.prefs;

import java.awt.Component;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.UIManager;
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
import free.util.TableLayout;
import free.util.swing.ColorChooser;

/**
 * A preferences panel allowing the user to modify move input preferences.
 */
public class MoveInputPanel extends BoardModifyingPrefsPanel {

  /**
   * The "unified" move input style selecting radio button.
   */
  private final JRadioButton unified;

  /**
   * The "drag and drop" move input style selecting radio button.
   */
  private final JRadioButton dragndrop;

  /**
   * The "click and click" move input style selecting radio button.
   */
  private final JRadioButton clicknclick;

  /**
   * The "auto promote" checkbox.
   */
  private final JCheckBox autoPromote;

  /**
   * The "piece follows cursor" checkbox.
   */
  private final JCheckBox pieceFollowsCursor;

  /**
   * The "draw piece in target square" checkbox.
   */
  private final JCheckBox showPieceInTargetSquare;

  /**
   * The "highlight move squares" checkbox.
   */
  private final JCheckBox highlightMadeMoveSquares;

  /**
   * The color chooser for the made move squares highlight color.
   */
  private final ColorChooser madeMoveSquaresHighlightColor;

  /**
   * The radio button for disallowing making moves when it isn't the user's turn.
   */
  private final JRadioButton disallowMoveInAdvance;

  /**
   * The radio button for sending a move made when it isn't the user's turn immediately.
   */
  private final JRadioButton immediateSendMove;

  /**
   * The radio button for enabling premove.
   */
  private final JRadioButton premove;

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
   * The "highlight legal target squares" checkbox.
   */
  private final JCheckBox highlightLegalTargetSquares;

  /**
   * The "snap to legal squares" checkbox.
   */
  private final JCheckBox snapToLegalTargetSquares;

  /**
   * The last move made on the board.
   */
  private Move lastMove = null;

  /**
   * Creates a new <code>MoveInputPanel</code> for the specified <code>BoardManager</code> and with
   * the specified preview board.
   */
  public MoveInputPanel(BoardManager boardManager, JinBoard previewBoard) {
    super(boardManager, previewBoard);

    I18n i18n = I18n.get(MoveInputPanel.class);

    unified = i18n.createRadioButton("unifiedRadioButton");
    dragndrop = i18n.createRadioButton("dragndropRadioButton");
    clicknclick = i18n.createRadioButton("clicknclickRadioButton");

    unified.setSelected(boardManager.getMoveInputStyle() == JBoard.UNIFIED_MOVE_INPUT_STYLE);
    dragndrop.setSelected(boardManager.getMoveInputStyle() == JBoard.DRAG_N_DROP_MOVE_INPUT_STYLE);
    clicknclick.setSelected(
        boardManager.getMoveInputStyle() == JBoard.CLICK_N_CLICK_MOVE_INPUT_STYLE);

    ButtonGroup moveInputGroup = new ButtonGroup();
    moveInputGroup.add(unified);
    moveInputGroup.add(dragndrop);
    moveInputGroup.add(clicknclick);

    ActionListener moveInputListener =
        new ActionListener() {
          @Override
          public void actionPerformed(ActionEvent evt) {
            MoveInputPanel.this.previewBoard.setMoveInputStyle(getMoveInputStyle());
            fireStateChanged();
          }
        };
    unified.addActionListener(moveInputListener);
    dragndrop.addActionListener(moveInputListener);
    clicknclick.addActionListener(moveInputListener);

    autoPromote = i18n.createCheckBox("autoPromoteCheckBox");
    autoPromote.setSelected(boardManager.isAutoPromote());
    autoPromote.addActionListener(
        new ActionListener() {
          @Override
          public void actionPerformed(ActionEvent evt) {
            MoveInputPanel.this.previewBoard.setManualPromote(!autoPromote.isSelected());
            fireStateChanged();
          }
        });

    pieceFollowsCursor = i18n.createCheckBox("pieceFollowsCursorCheckBox");
    pieceFollowsCursor.setSelected(boardManager.isPieceFollowsCursor());
    pieceFollowsCursor.addActionListener(
        new ActionListener() {
          @Override
          public void actionPerformed(ActionEvent evt) {
            MoveInputPanel.this.previewBoard.setPieceFollowsCursor(pieceFollowsCursor.isSelected());
            fireStateChanged();
          }
        });

    showPieceInTargetSquare = i18n.createCheckBox("showPieceInTargetSquareCheckBox");
    showPieceInTargetSquare.setSelected(boardManager.isShowShadowPieceInTargetSquare());
    showPieceInTargetSquare.addActionListener(
        new ActionListener() {
          @Override
          public void actionPerformed(ActionEvent evt) {
            MoveInputPanel.this.previewBoard.setShowShadowPieceInTargetSquare(
                showPieceInTargetSquare.isSelected());
            fireStateChanged();
          }
        });

    highlightMadeMoveSquares = i18n.createCheckBox("highlightMadeMoveSquaresCheckBox");
    highlightMadeMoveSquares.setSelected(boardManager.isHighlightMadeMoveSquares());
    highlightMadeMoveSquares.addActionListener(
        new ActionListener() {
          @Override
          public void actionPerformed(ActionEvent evt) {
            boolean selected = highlightMadeMoveSquares.isSelected();
            MoveInputPanel.this.previewBoard.setHighlightMadeMoveSquares(selected);
            madeMoveSquaresHighlightColor.setEnabled(selected);
            fireStateChanged();
          }
        });

    madeMoveSquaresHighlightColor = i18n.createColorChooser("madeMoveSquaresHighlightColorChooser");
    madeMoveSquaresHighlightColor.setColor(boardManager.getMadeMoveSquaresHighlightColor());
    madeMoveSquaresHighlightColor.setEnabled(highlightMadeMoveSquares.isSelected());
    madeMoveSquaresHighlightColor.addChangeListener(
        new ChangeListener() {
          @Override
          public void stateChanged(ChangeEvent evt) {
            MoveInputPanel.this.previewBoard.setMadeMoveSquaresHighlightColor(
                madeMoveSquaresHighlightColor.getColor());
            fireStateChanged();
          }
        });

    disallowMoveInAdvance = i18n.createRadioButton("disallowMoveInAdvanceRadioButton");
    immediateSendMove = i18n.createRadioButton("immediateSendMoveRadioButton");
    premove = i18n.createRadioButton("premoveRadioButton");

    disallowMoveInAdvance.setSelected(
        boardManager.getMoveSendingMode() == BoardManager.LEGAL_CHESS_MOVE_SENDING_MODE);
    immediateSendMove.setSelected(
        boardManager.getMoveSendingMode() == BoardManager.PREDRAG_MOVE_SENDING_MODE);
    premove.setSelected(
        boardManager.getMoveSendingMode() == BoardManager.PREMOVE_MOVE_SENDING_MODE);

    ButtonGroup movingInAdvanceButtonGroup = new ButtonGroup();
    movingInAdvanceButtonGroup.add(disallowMoveInAdvance);
    movingInAdvanceButtonGroup.add(immediateSendMove);
    movingInAdvanceButtonGroup.add(premove);
    ActionListener movingInAdvanceListener =
        new ActionListener() {
          @Override
          public void actionPerformed(ActionEvent evt) {
            BoardManager boardManager = MoveInputPanel.this.boardManager;
            if (boardManager.isUserPlaying()) {
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

    ActionListener styleListener =
        new ActionListener() {
          @Override
          public void actionPerformed(ActionEvent evt) {
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

    highlightOwnMoves.addActionListener(
        new ActionListener() {
          @Override
          public void actionPerformed(ActionEvent evt) {
            if (lastMove.getPlayer().isWhite())
              MoveInputPanel.this.previewBoard.setHighlightedMove(
                  highlightOwnMoves.isSelected() ? lastMove : null);
            fireStateChanged();
          }
        });

    highlightColor.addChangeListener(
        new ChangeListener() {
          @Override
          public void stateChanged(ChangeEvent evt) {
            MoveInputPanel.this.previewBoard.setMoveHighlightingColor(highlightColor.getColor());
            fireStateChanged();
          }
        });

    previewBoard
        .getPosition()
        .addMoveListener(
            new MoveListener() {
              @Override
              public void moveMade(MoveEvent evt) {
                Move move = evt.getMove();
                if (move.getPlayer().isBlack() || highlightOwnMoves.isSelected())
                  MoveInputPanel.this.previewBoard.setHighlightedMove(move);
                else MoveInputPanel.this.previewBoard.setHighlightedMove(null);

                lastMove = move;
              }
            });

    highlightLegalTargetSquares = i18n.createCheckBox("highlightLegalTargetSquaresCheckBox");
    highlightLegalTargetSquares.setSelected(boardManager.isHighlightLegalTargetSquares());
    highlightLegalTargetSquares.addActionListener(
        new ActionListener() {
          @Override
          public void actionPerformed(ActionEvent e) {
            MoveInputPanel.this.previewBoard.setHighlightLegalTargetSquares(
                highlightLegalTargetSquares.isSelected());
            fireStateChanged();
          }
        });

    snapToLegalTargetSquares = i18n.createCheckBox("snapToLegalSquaresCheckBox");
    snapToLegalTargetSquares.setSelected(boardManager.isSnapToLegalSquare());
    snapToLegalTargetSquares.addActionListener(
        new ActionListener() {
          @Override
          public void actionPerformed(ActionEvent e) {
            MoveInputPanel.this.previewBoard.setSnapToLegalSquare(
                snapToLegalTargetSquares.isSelected());
            fireStateChanged();
          }
        });

    JComponent moveInputPanel = createMoveInputUI();
    JComponent promotePanel = createPromotionUI();
    JComponent moveVisualizationPanel = createMoveVisualizationUI();
    JComponent movingInAdvancePanel = createMovingInAdvanceUI();
    JComponent moveHighlightPanel = createMoveHighlightUI();
    JComponent moveAssistancePanel = createMoveAssistanceUI();

    setBorder(BorderFactory.createEmptyBorder(15, 10, 15, 10));
    setLayout(new TableLayout(2, 10, 10));

    add(moveInputPanel);
    add(promotePanel);
    add(moveVisualizationPanel);
    add(moveAssistancePanel);
    add(moveHighlightPanel);
    add(movingInAdvancePanel);
  }

  /**
   * Returns the currently selected move input style.
   */
  private int getMoveInputStyle() {
    if (unified.isSelected()) return JBoard.UNIFIED_MOVE_INPUT_STYLE;
    else if (dragndrop.isSelected()) return JBoard.DRAG_N_DROP_MOVE_INPUT_STYLE;
    else if (clicknclick.isSelected()) return JBoard.CLICK_N_CLICK_MOVE_INPUT_STYLE;
    else throw new IllegalStateException("None of the radio buttons are selected");
  }

  /**
   * Returns the currently selected move sending mode.
   */
  private int getMoveSendingMode() {
    if (disallowMoveInAdvance.isSelected()) return BoardManager.LEGAL_CHESS_MOVE_SENDING_MODE;
    else if (immediateSendMove.isSelected()) return BoardManager.PREDRAG_MOVE_SENDING_MODE;
    else if (premove.isSelected()) return BoardManager.PREMOVE_MOVE_SENDING_MODE;
    else throw new IllegalStateException("None of the radio buttons are selected");
  }

  /**
   * Returns the currently selected move highlighting style.
   */
  private int getMoveHighlightingStyle() {
    if (none.isSelected()) return JBoard.NO_MOVE_HIGHLIGHTING;
    else if (targetSquare.isSelected()) return JBoard.TARGET_SQUARE_MOVE_HIGHLIGHTING;
    else if (bothSquares.isSelected()) return JBoard.BOTH_SQUARES_MOVE_HIGHLIGHTING;
    else if (arrow.isSelected()) return JBoard.ARROW_MOVE_HIGHLIGHTING;
    else throw new IllegalStateException("None of the radio buttons are selected");
  }

  /**
   * Sets the initial properties of the preview board.
   */
  @Override
  public void initPreviewBoard() {
    previewBoard.setMoveInputStyle(getMoveInputStyle());
    previewBoard.setPieceFollowsCursor(pieceFollowsCursor.isSelected());
    previewBoard.setShowShadowPieceInTargetSquare(showPieceInTargetSquare.isSelected());
    previewBoard.setHighlightMadeMoveSquares(highlightMadeMoveSquares.isSelected());
    previewBoard.setShowShadowPieceInTargetSquare(showPieceInTargetSquare.isSelected());
    previewBoard.setHighlightLegalTargetSquares(highlightLegalTargetSquares.isSelected());
    previewBoard.setSnapToLegalSquare(snapToLegalTargetSquares.isSelected());
    previewBoard.setManualPromote(!autoPromote.isSelected());

    previewBoard.setMoveHighlightingStyle(getMoveHighlightingStyle());
    previewBoard.setMoveHighlightingColor(highlightColor.getColor());

    Position pos = previewBoard.getPosition();

    Move move =
        Chess.getInstance()
            .createMove(pos, Square.parseSquare("f5"), Square.parseSquare("c8"), null, "Bc8");

    pos.makeMove(move);
    previewBoard.setHighlightedMove(move);
  }

  /**
   * Creates the move input panel.
   */
  private JComponent createMoveInputUI() {
    I18n i18n = I18n.get(MoveInputPanel.class);

    JPanel panel = new JPanel();
    panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
    panel.setBorder(
        BorderFactory.createCompoundBorder(
            i18n.createTitledBorder("moveInputPanel"),
            BorderFactory.createEmptyBorder(0, 5, 5, 5)));

    unified.setAlignmentX(Component.LEFT_ALIGNMENT);
    dragndrop.setAlignmentX(Component.LEFT_ALIGNMENT);
    clicknclick.setAlignmentX(Component.LEFT_ALIGNMENT);

    panel.add(unified);
    panel.add(dragndrop);
    panel.add(clicknclick);
    panel.add(Box.createGlue());

    return panel;
  }

  /**
   * Creates the promotion panel.
   */
  private JComponent createPromotionUI() {
    I18n i18n = I18n.get(MoveInputPanel.class);

    JPanel panel = new JPanel();
    panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
    panel.setBorder(
        BorderFactory.createCompoundBorder(
            i18n.createTitledBorder("promotionPanel"),
            BorderFactory.createEmptyBorder(0, 5, 5, 5)));

    autoPromote.setAlignmentX(Component.LEFT_ALIGNMENT);

    panel.add(autoPromote);
    panel.add(Box.createGlue());

    return panel;
  }

  /**
   * Creates the move visualization panel.
   */
  private JComponent createMoveVisualizationUI() {
    I18n i18n = I18n.get(MoveInputPanel.class);

    JPanel panel = new JPanel();
    panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
    panel.setBorder(
        BorderFactory.createCompoundBorder(
            i18n.createTitledBorder("moveVisualizationPanel"),
            BorderFactory.createEmptyBorder(0, 5, 5, 5)));

    pieceFollowsCursor.setAlignmentX(Component.LEFT_ALIGNMENT);
    showPieceInTargetSquare.setAlignmentX(Component.LEFT_ALIGNMENT);
    highlightMadeMoveSquares.setAlignmentX(Component.LEFT_ALIGNMENT);
    madeMoveSquaresHighlightColor.setAlignmentX(Component.LEFT_ALIGNMENT);

    panel.add(pieceFollowsCursor);
    panel.add(showPieceInTargetSquare);
    panel.add(highlightMadeMoveSquares);
    panel.add(madeMoveSquaresHighlightColor);
    panel.add(Box.createGlue());

    return panel;
  }

  /**
   * Creates the moving in advance panel.
   */
  private JComponent createMovingInAdvanceUI() {
    I18n i18n = I18n.get(MoveInputPanel.class);

    JPanel panel = new JPanel();
    panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
    panel.setBorder(
        BorderFactory.createCompoundBorder(
            i18n.createTitledBorder("movingInAdvancePanel"),
            BorderFactory.createEmptyBorder(0, 5, 5, 5)));

    disallowMoveInAdvance.setAlignmentX(Component.LEFT_ALIGNMENT);
    immediateSendMove.setAlignmentX(Component.LEFT_ALIGNMENT);
    premove.setAlignmentX(Component.LEFT_ALIGNMENT);

    panel.add(disallowMoveInAdvance);
    panel.add(immediateSendMove);
    panel.add(premove);
    panel.add(Box.createVerticalStrut(5));
    Font warningLabelFont = UIManager.getFont("Label.font");
    warningLabelFont = warningLabelFont.deriveFont(warningLabelFont.getSize2D() - 2);
    String[] warnings = i18n.getString("moveInAdvanceChangeWarning").split("\n");
    for (int i = 0; i < warnings.length; i++) {
      JLabel label = new JLabel(warnings[i]);
      label.setFont(warningLabelFont);
      panel.add(label);
    }
    panel.add(Box.createGlue());

    return panel;
  }

  /**
   * Creates the move highlight panel.
   */
  private JComponent createMoveHighlightUI() {
    I18n i18n = I18n.get(MoveInputPanel.class);

    JPanel panel = new JPanel();
    panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
    panel.setBorder(
        BorderFactory.createCompoundBorder(
            i18n.createTitledBorder("moveHighlightPanel"),
            BorderFactory.createEmptyBorder(0, 5, 5, 5)));

    none.setAlignmentX(Component.LEFT_ALIGNMENT);
    targetSquare.setAlignmentX(Component.LEFT_ALIGNMENT);
    bothSquares.setAlignmentX(Component.LEFT_ALIGNMENT);
    arrow.setAlignmentX(Component.LEFT_ALIGNMENT);
    highlightOwnMoves.setAlignmentX(Component.LEFT_ALIGNMENT);
    highlightColor.setAlignmentX(Component.LEFT_ALIGNMENT);

    panel.add(none);
    panel.add(targetSquare);
    panel.add(bothSquares);
    panel.add(arrow);
    panel.add(highlightOwnMoves);
    panel.add(highlightColor);
    panel.add(Box.createGlue());

    return panel;
  }

  /**
   * Creates the move assistance panel.
   */
  private JComponent createMoveAssistanceUI() {
    I18n i18n = I18n.get(MoveInputPanel.class);

    JPanel panel = new JPanel();
    panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
    panel.setBorder(
        BorderFactory.createCompoundBorder(
            i18n.createTitledBorder("moveAssistancePanel"),
            BorderFactory.createEmptyBorder(0, 5, 5, 5)));

    highlightLegalTargetSquares.setAlignmentX(Component.LEFT_ALIGNMENT);

    panel.add(highlightLegalTargetSquares);
    panel.add(snapToLegalTargetSquares);
    panel.add(Box.createGlue());

    return panel;
  }

  /**
   * Applies any changes made by the user.
   */
  @Override
  public void applyChanges() throws BadChangesException {
    boardManager.setMoveInputStyle(getMoveInputStyle());
    boardManager.setAutoPromote(autoPromote.isSelected());

    boardManager.setPieceFollowsCursor(pieceFollowsCursor.isSelected());
    boardManager.setShowShadowPieceInTargetSquare(showPieceInTargetSquare.isSelected());
    boardManager.setHighlightMadeMoveSquares(highlightMadeMoveSquares.isSelected());
    boardManager.setMadeMoveSquaresHighlightColor(madeMoveSquaresHighlightColor.getColor());

    boardManager.setMoveSendingMode(getMoveSendingMode());

    boardManager.setMoveHighlightingStyle(getMoveHighlightingStyle());
    boardManager.setHighlightingOwnMoves(highlightOwnMoves.isSelected());
    boardManager.setMoveHighlightingColor(highlightColor.getColor());

    boardManager.setHighlightLegalTargetSquares(highlightLegalTargetSquares.isSelected());
    boardManager.setSnapToLegalSquare(snapToLegalTargetSquares.isSelected());
  }
}
