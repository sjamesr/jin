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

package free.jin.board;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.net.URL;
import java.util.Vector;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JToggleButton;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableColumnModelEvent;
import javax.swing.event.TableColumnModelListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;

import free.chess.AbstractChessClock;
import free.chess.Chess;
import free.chess.ChessMove;
import free.chess.JChessClock;
import free.chess.Move;
import free.chess.Player;
import free.chess.Position;
import free.chess.WildVariant;
import free.chess.event.MoveEvent;
import free.chess.event.MoveListener;
import free.jin.Connection;
import free.jin.Game;
import free.jin.I18n;
import free.jin.board.event.UserMoveEvent;
import free.jin.board.event.UserMoveListener;
import free.jin.event.BoardFlipEvent;
import free.jin.event.ClockAdjustmentEvent;
import free.jin.event.GameEndEvent;
import free.jin.event.GameListener;
import free.jin.event.GameStartEvent;
import free.jin.event.IllegalMoveEvent;
import free.jin.event.MoveMadeEvent;
import free.jin.event.OfferEvent;
import free.jin.event.PositionChangedEvent;
import free.jin.event.TakebackEvent;
import free.util.PlatformUtils;
import free.util.Utilities;
import free.util.models.ModelUtils;
import free.util.swing.FullscreenPanel;
import free.util.swing.ImageComponent;
import free.util.swing.NonEditableTableModel;
import free.util.swing.SwingUtils;
import free.util.swing.WrapLayout;
import free.workarounds.FixedJPanel;
import free.workarounds.FixedJTable;


/**
 * A panel which displays a chess board and all related information, such
 * as opponents' names, clock, opponents' ratings etc. 
 * To use BoardPanel, you must register it as a GameListener to some source of
 * GameEvents, or alternatively, call the methods defined in GameListener
 * directly.
 */

public class BoardPanel extends FixedJPanel implements MoveListener, GameListener, 
    AdjustmentListener, PropertyChangeListener{
  
  
  
  /**
   * The <code>BoardManager</code> this BoardPanel is used by.
   */
  
  protected final BoardManager boardManager;
  
  
  
  /**
   * The Game this BoardPanel is displaying.
   */
  
  protected final Game game;
  
  
  
  /**
   * Is this BoardPanel active, i.e. is the game displayed is in progress?
   */
  
  private boolean isActive = true;
  
  
  
  /**
   * Is this BoardPanel flipped.
   */
  
  protected boolean isFlipped = false;
  
  
  
  /**
   * The JinBoard showing the current position.
   */
  
  protected JinBoard board;
  
  
  
  /**
   * True if we're highlighting our own moves. False if only opponent's moves.
   */
  
  private boolean highlightOwnMoves;
  
  
  
  /**
   * The current move sending mode. Possible values are defined in
   * <code>BoardManager</code>.
   */
  
  private int moveSendingMode;
  
  
  
  /**
   * The list of made moves.
   */
  
  protected final Vector madeMoves = new Vector();
  
  
  
  /**
   * The actual position in the game, this may differ than the one on the board
   * because the one on the board may include moves not yet inspected by the
   * server.
   */
  
  protected final Position realPosition;
  
  
  
  /**
   * The number of the move after which the position displayed on the board 
   * occurs.
   */
  
  protected int displayedMoveNumber = 0;
  
  
  
  /**
   * The JLabel displaying information about the player with the white pieces.
   */
  
  protected JLabel whiteLabel;
  
  
  
  /**
   * The JLabel displaying information about the player with the black pieces.
   */
  
  protected JLabel blackLabel;
  
  
  
  /**
   * The Container of the action buttons (the button panel).
   */
  
  protected JPanel buttonPanel;
  
  
  
  /**
   * The button that makes the board fullscreen and restores it.
   */
  
  protected JToggleButton fullscreenButton;
  
  
  
  /**
   * The JChessClock displaying the time on white's clock.
   */
  
  protected JChessClock whiteClock;
  
  
  
  /**
   * The JChessClock displaying the time on black's clock.
   */
  
  protected JChessClock blackClock;
  
  
  
  /**
   * The JLabel displaying information about the game.
   */
  
  protected JLabel gameLabel;
  
  
  /**
   * The JTable displaying the move list.
   */
  
  protected JTable moveListTable;
  
  
  
  /**
   * The image component for the white player.
   */
  
  protected ImageComponent whiteImageComponent;
  
  
  
  /**
   * The image component for the black player.
   */
  
  protected ImageComponent blackImageComponent;
  
  
  
  /**
   * A boolean specifying whether the moveListTable selection is already being
   * changed and so the listener should ignore any calls to avoid endless
   * recursion.
   */
  
  private boolean isMoveListTableSelectionUpdating = false;
  
  
  
  /**
   * Needed because JDK 1.1 doesn't allow setting the selected table cell with
   * one method (and one event fired). The selection listener ignores the call
   * when this is set to true.
   */
  
  private boolean settingMoveListTableSelection = false;
  
  
  
  /**
   * The TableModel of the JTable displaying the move list.
   */
  
  protected TableModel moveListTableModel;
  
  
  
  /**
   * The JScrollPane in which we put the moveListTable.
   */
  
  protected JScrollPane moveListTableScrollPane;
  
  
  
  /**
   * The scrollbar which lets you scroll through all the positions that occurred
   * in the game.
   */
  
  protected JScrollBar positionScrollBar;
  
  
  
  /**
   * A boolean specifying whether the positionScrollBar is being changed
   * programmatically. When it is, the method(s) listening to events from it,
   * should probably ignore them.
   */
  
  protected boolean isPositionScrollBarUpdating = false;
  
  
  
  /**
   * True when the board position is being updated programmatically (and not as
   * a result of user interaction) and thus events from it should probably be
   * ignored.
   */
  
  protected boolean isBoardPositionUpdating = false;
  
  
  
  /**
   * The move made when we've fired a UserMadeMove and it hasn't been echoed to
   * us yet. Null if none.
   */
  
  private Move moveEnRoute = null;
  
  
  
  
  /**
   * The queued move - the user may make a move when it's not his turn, and it
   * will then be saved here until his opponent makes a move.
   */
  
  private Move queuedMove = null;
  
  
  
  /**
   * The time when we sent the last user's move to the server. We need this
   * because if the move was illegal, we need to know how much time it spent
   * in transit to subtract that time from the player's clock (we can't count
   * on getting a clock update from the server).
   */
  
  private long sentMoveTimestamp = -1;
  
  
  
  /**
   * The FullscreenPanel allowing us to maximize the board panel.
   */
  
  private final FullscreenPanel fullscreenPanel;
  
  
  
  /**
   * The target of the fullscreen panel, to which we add all the components.
   */
  
  private final ContentPanel contentPanel;
  
  
  
  /**
   * Creates a new <code>BoardPanel</code> which will be used by the given
   * <code>BoardManager</code>, will display the given Game and will have the
   * given move input mode.
   */
  
  public BoardPanel(BoardManager boardManager, Game game){
    this.game = game;
    this.boardManager = boardManager;
    this.realPosition = game.getInitialPosition();
    
    boardManager.addPropertyChangeListener(this);
    game.addPropertyChangeListener(this);
    
    this.contentPanel = new ContentPanel();
    this.fullscreenPanel = new FullscreenPanel(contentPanel);
    fullscreenPanel.setAllowExclusiveMode(false); // Too buggy
    
    init();
    
    setLayout(WrapLayout.getInstance());
    add(fullscreenPanel);
    
    // Fullscreen mode locks up the application under OS X (older versions of Java).
    // Fullscreen mode is broken under Java 1.5.0 when used as an Applet, see
    // http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=5085648
    if (!PlatformUtils.isJavaBetterThan("1.4") ||
        (PlatformUtils.isMacOSX() && !PlatformUtils.isJavaBetterThan("1.4.2")) ||
        ((System.getSecurityManager() != null) && PlatformUtils.isJavaBetterThan("1.5"))){
      fullscreenButton.setEnabled(false);
      fullscreenButton.setToolTipText(I18n.get(BoardPanel.class).getString("fullscreenUnavailableMessage"));
    }
    else{
      KeyStroke fullscreenKeyStroke = 
        KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask());
      contentPanel.registerKeyboardAction(new ActionListener(){
        public void actionPerformed(ActionEvent evt){
          fullscreenPanel.getFullscreenModeModel().flip();
        }
      }, fullscreenKeyStroke, WHEN_IN_FOCUSED_WINDOW);
    }
  }
  
  
  
  /**
   * Adds the given UserMoveListener to the list of listeners receiving events about
   * moves made on the board by the user. 
   */
  
  public void addUserMoveListener(UserMoveListener listener){
    listenerList.add(UserMoveListener.class, listener);
  }
  
  
  
  /**
   * Removes the given UserMoveListener from the list of listeners receiving events
   * about moves made by the user on the board.
   */
  
  public void removeUserMoveListener(UserMoveListener listener){
    listenerList.remove(UserMoveListener.class, listener); 
  }
  
  
  
  /**
   * Dispatches the given UserMoveEvent to all interested UserMoveListeners of this BoardPanel.
   */
  
  protected void fireUserMadeMove(UserMoveEvent evt){
    Object [] listenerList = this.listenerList.getListenerList();
    for (int i = 0; i < listenerList.length; i += 2){
      if (listenerList[i] == UserMoveListener.class){
        UserMoveListener listener = (UserMoveListener)listenerList[i+1];
        listener.userMadeMove(evt);
      }
    }
  }
  
  
  
  /**
   * This method is called by the constructor, it's meant to initialize all the
   * components and add them.
   */
  
  protected void init(){
    isFlipped = game.isBoardInitiallyFlipped();
    highlightOwnMoves = boardManager.isHighlightingOwnMoves();
    moveSendingMode = boardManager.getMoveSendingMode();
    
    createComponents(game);
    
    addComponents(); 
  }
  
  
  
  /**
   * A mouse listener which pauses other plugins when the mouse moves over the
   * board in order to give the board maximum preference.
   */
  
  private class OtherPluginsPauser implements MouseListener, MouseMotionListener{
    
    private boolean paused = false;
    
    private Timer timer = new Timer(1000, new ActionListener(){
      public void actionPerformed(ActionEvent evt){
        boardManager.setOtherPluginsPaused(false);
        paused = false;
      }
    });
    
    private void go(){
      if (!paused){
        paused = true;
        boardManager.setOtherPluginsPaused(true);
      }
      
      if (timer.isRunning())
        timer.restart();
      else{
        timer.setRepeats(false);
        timer.start();
      }
    }
    
    
    public void mouseDragged(MouseEvent e){go();}
    public void mouseMoved(MouseEvent e){go();}
    public void mouseClicked(MouseEvent e){go();}
    public void mouseEntered(MouseEvent e){go();}
    public void mouseExited(MouseEvent e){go();}
    public void mousePressed(MouseEvent e){go();}
    public void mouseReleased(MouseEvent e){go();}
  }
  
  
  
  /**
   * This method is meant to create all the sub-components used by the
   * BoardPanel.
   */
  
  protected void createComponents(Game game){
    board = createBoard(game);
    initBoard(game, board);
    board.getPosition().addMoveListener(this);
    
    gameLabel = createGameLabel(game);
    whiteLabel = createWhiteLabel(game);
    blackLabel = createBlackLabel(game);
    fullscreenButton = createFullscreenButton();
    whiteClock = createWhiteClock(game);
    blackClock = createBlackClock(game);
    buttonPanel = createButtonPanel(game);
    moveListTableModel = createMoveListTableModel(game);
    moveListTable = createMoveListTable(game, moveListTableModel);
    moveListTableScrollPane = createMoveListTableScrollPane(game, moveListTable);
    whiteImageComponent = createWhiteImageComponent(game);
    blackImageComponent = createBlackImageComponent(game);
    
    // Pauses other plugins when a move is in progress.
    OtherPluginsPauser pauser = new OtherPluginsPauser();
    board.addMouseListener(pauser);
    board.addMouseMotionListener(pauser);
    
    moveListTable.getSelectionModel().addListSelectionListener(new ListSelectionListener(){
      
      public void valueChanged(ListSelectionEvent evt){
        moveListTableSelectionChanged();
      }
      
    });
    
    moveListTable.getColumnModel().addColumnModelListener(new TableColumnModelListener(){
      public void columnAdded(TableColumnModelEvent e){}
      public void columnMarginChanged(ChangeEvent e){}
      public void columnMoved(TableColumnModelEvent e){}
      public void columnRemoved(TableColumnModelEvent e){}
      public void columnSelectionChanged(ListSelectionEvent e){
        moveListTableSelectionChanged();
      }
    });
    
    positionScrollBar = createPositionScrollBar();
    positionScrollBar.addAdjustmentListener(this);
    
    updateClockActiveness();
  }
  
  
  
  /**
   * Adds all the components to the content panel.
   */
  
  private void addComponents(){
    contentPanel.add(board);
    contentPanel.add(whiteLabel);
    contentPanel.add(blackLabel);
    contentPanel.add(whiteClock);
    contentPanel.add(blackClock);
    contentPanel.add(gameLabel);
    contentPanel.add(positionScrollBar);
    contentPanel.add(moveListTableScrollPane);
    contentPanel.add(buttonPanel);
    contentPanel.add(fullscreenButton);
    contentPanel.add(whiteImageComponent);
    contentPanel.add(blackImageComponent);
  }
  
  
  
  /**
   * Creates and returns the <code>JinBoard</code>.
   */
  
  protected JinBoard createBoard(Game game){
    return new JinBoard(game.getInitialPosition());
  }
  
  
  
  /**
   * Initializes the board.
   */
  
  protected void initBoard(Game game, JinBoard board){
    if (isFlipped())
      board.setFlipped(true);
    
    configureBoardFromGame(game, board);
    configureBoardFromBoardManager(board);
    
    ActionListener escapeListener = new ActionListener(){
      public void actionPerformed(ActionEvent evt){
        JinBoard board = BoardPanel.this.board;
        if (board.isMovingPiece())
          board.cancelMovingPiece();
        else if (queuedMove != null){
          setQueuedMove(null);
          isBoardPositionUpdating = true;
          board.getPosition().copyFrom(realPosition);
          if (isMoveEnRoute())
            board.getPosition().makeMove(moveEnRoute);
          isBoardPositionUpdating = false;
        }
        else
          fullscreenPanel.getFullscreenModeModel().setOff();
      }
    };
    
    contentPanel.registerKeyboardAction(escapeListener,
        KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, MouseEvent.BUTTON1_MASK), JComponent.WHEN_IN_FOCUSED_WINDOW);
    contentPanel.registerKeyboardAction(escapeListener, 
        KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);
    
    if (!isUserTurn() && (moveSendingMode == BoardManager.LEGAL_CHESS_MOVE_SENDING_MODE))
      board.setEditable(false);
  }
  
  
  
  /**
   * Configures the board according to the <code>Game</code> settings.
   */
  
  protected void configureBoardFromGame(Game game, JinBoard board){
    board.setMoveInputMode(calcMoveInputMode(game));
  }
  
  
  
  /**
   * Configures the board according to the <code>BoardManager</code> settings.
   */
  
  protected void configureBoardFromBoardManager(JinBoard board){
    board.setPiecePainter(boardManager.getPiecePainter());
    board.setBoardPainter(boardManager.getBoardPainter());
    board.setMoveInputStyle(boardManager.getMoveInputStyle());
    board.setPieceFollowsCursor(boardManager.isPieceFollowsCursor());
    board.setHighlightMadeMoveSquares(boardManager.isHighlightMadeMoveSquares());
    board.setMadeMoveSquaresHighlightColor(boardManager.getMadeMoveSquaresHighlightColor());
    board.setMoveHighlightingStyle(boardManager.getMoveHighlightingStyle());
    board.setCoordsDisplayStyle(boardManager.getCoordsDisplayStyle());
    board.setCoordsDisplayColor(boardManager.getCoordsDisplayColor());
    board.setManualPromote(!boardManager.isAutoPromote());
    board.setMoveHighlightingColor(boardManager.getMoveHighlightingColor());
    board.setShowShadowPieceInTargetSquare(boardManager.isShowShadowPieceInTargetSquare());
    board.setHighlightLegalTargetSquares(boardManager.isHighlightLegalTargetSquares());
    board.setSnapToLegalSquare(boardManager.isSnapToLegalSquare());
  }
  
  
  
  /**
   * Returns the move input mode that should be used for the specified game.
   */
  
  protected int calcMoveInputMode(Game game){
    if (game.getGameType() == Game.MY_GAME)
      if (game.isPlayed())
        if (game.getUserPlayer() == Player.WHITE_PLAYER)
          return JinBoard.WHITE_PIECES_MOVE;
        else
          return JinBoard.BLACK_PIECES_MOVE;
      else 
        return JinBoard.CURRENT_PLAYER_MOVES; 
    else // This counts for both ISOLATED_BOARD and OBSERVED_GAME.
      return JinBoard.NO_PIECES_MOVE;
  }
  
  
  
  /**
   * Creates the JLabel displaying information about the game.
   */
  
  protected JLabel createGameLabel(Game game){
    JLabel gameLabel = new JLabel(createGameLabelText(game));
    return gameLabel;
  }
  
  
  
  /**
   * Returns the text that should be displayed on the game label.
   */
  
  protected String createGameLabelText(Game game){
    WildVariant variant = game.getVariant();
    String category = variant.equals(Chess.getInstance()) ?
        game.getRatingCategoryString() : variant.getName();
        
        I18n i18n = I18n.get(BoardPanel.class);
        return i18n.getFormattedString("gameLabelFormat", new Object[]{
            game.isRated() ? i18n.getString("rated") : i18n.getString("unrated"),
                game.getTimeControl().getLocalizedShortDescription(),
                category
        });
  }
  
  
  
  /**
   * Creates the JLabel displaying information about the player with the white
   * pieces.
   */
  
  protected JLabel createWhiteLabel(Game game){
    JLabel whiteLabel = new JLabel(createWhiteLabelText(game));
    whiteLabel.setFont(whiteLabel.getFont().deriveFont(20f).deriveFont(Font.BOLD));
    return whiteLabel;
  }
  
  
  
  /**
   * Returns the text that should be displayed by the white player's label.
   */
  
  protected String createWhiteLabelText(Game game){
    return game.getWhiteName() + game.getWhiteTitles() + " " + game.getWhiteRating();
  }
  
  
  
  /**
   * Creates the JLabel displaying information about the player with the black
   * pieces.
   */
  
  protected JLabel createBlackLabel(Game game){
    JLabel blackLabel = new JLabel(createBlackLabelText(game));
    blackLabel.setFont(whiteLabel.getFont().deriveFont(20f).deriveFont(Font.BOLD));
    return blackLabel;
  }
  
  
  
  /**
   * Returns the text that should be displayed by the black player's label.
   */
  
  protected String createBlackLabelText(Game game){
    return game.getBlackName() + game.getBlackTitles() + " " + game.getBlackRating();
  }
  
  
  
  /**
   * Creates the button that makes the board fullscreen and restores it.
   */
  
  protected JToggleButton createFullscreenButton(){
    I18n i18n = I18n.get(BoardPanel.class);
    JToggleButton button = new JToggleButton();
    button.setRequestFocusEnabled(false);
    button.setToolTipText(i18n.getString("fullscreenButton.tooltip"));
    
    Class loader = BoardPanel.class;
    Image image = Toolkit.getDefaultToolkit().getImage(
        loader.getResource("images/view-fullscreen.png"));
    SwingUtils.confIconButton(button, image);
    
    ModelUtils.linkSelected(fullscreenPanel.getFullscreenModeModel(), button.getModel());
    
    return button;
  }
  
  
  
  /**
   * Creates the AbstractChessClock which will display the amount of time remaining
   * on white's clock.
   */
  
  protected JChessClock createWhiteClock(Game game){
    JChessClock clock = new JChessClock(game.getTimeControl().getInitialTime(Player.WHITE_PLAYER));
    clock.setTimeDependentDisplayModeThresholds(Integer.MAX_VALUE, 10*1000);
    return clock;
  }
  
  
  
  /**
   * Creates the AbstractChessClock which will display the amount of time remaining
   * on black's clock.
   */
  
  protected JChessClock createBlackClock(Game game){
    JChessClock clock = new JChessClock(game.getTimeControl().getInitialTime(Player.BLACK_PLAYER));
    clock.setTimeDependentDisplayModeThresholds(Integer.MAX_VALUE, 10*1000);
    return clock;
  }
  
  
  
  /**
   * Creates the button panel - a panel with various action buttons on it.
   * If the given Game is not of type Game.MY_GAME, this method returns null.
   * If the given Game is a played game, the actual creation is delegated to the
   * {@link #createPlayedButtonPanel(Game)} method, otherwise (it's an examined game)
   * the creation is delegated to the {@link #createExaminedButtonPanel(Game)} method.
   */
  
  protected JPanel createButtonPanel(Game game){
    JPanel buttonPanel;
    if (game.getGameType() != Game.MY_GAME)
      return new JPanel();
    else if (game.isPlayed())
      buttonPanel = createPlayedGameButtonPanel(game); 
    else
      buttonPanel = createExaminedGameButtonPanel(game);
    
    return buttonPanel;
  }
  
  
  
  /**
   * Creates the button panel for a game played by the user.
   */
  
  protected JPanel createPlayedGameButtonPanel(Game game){
    return new PlayedGameButtonPanel(boardManager, game, contentPanel);
  }
  
  
  
  /**
   * Creates the button panel for a game examined by the user.
   */
  
  protected JPanel createExaminedGameButtonPanel(Game game){
    return new ExaminedGameButtonPanel(boardManager, game);
  }
  
  
  
  /**
   * Creates the TableModel for the JTable which will be used for displaying
   * the move list. If you override this method, you should also see if you need
   * to override {@link #addMoveToListTable(Move)} and {@link #updateMoveListTable()}.
   */
  
  protected TableModel createMoveListTableModel(Game game){
    I18n i18n = I18n.get(BoardPanel.class);
    return new NonEditableTableModel(new String[]{
        i18n.getString("moveListTable.moveNo"),
        i18n.getString("moveListTable.white"),
        i18n.getString("moveListTable.black")
    }, 0);
  }
  
  
  
  /**
   * Creates the JTable which will display the move list. The JTable must be
   * created with the given TableModel.
   */
  
  protected JTable createMoveListTable(Game game, TableModel moveListTableModel){
    JTable table = new FixedJTable(moveListTableModel);
    table.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
    table.setCellSelectionEnabled(true);
    table.getTableHeader().setPreferredSize(new Dimension(150, 18));
    table.getTableHeader().setReorderingAllowed(false);
    table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    table.setRequestFocusEnabled(false);
    
    return table;
  }
  
  
  
  /**
   * Creates the JScrollPane in which we put the moveListTable.
   */
  
  protected JScrollPane createMoveListTableScrollPane(Game game, JTable moveListTable){
    JScrollPane scrollPane = new JScrollPane(moveListTable);
    scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
    scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
    
    return scrollPane;
  }
  
  
  
  /**
   * Creates the component to display the image of the white player.
   */
  
  protected ImageComponent createWhiteImageComponent(Game game){
    return createPlayerImageComponent(game.getWhiteName());
  }
  
  
  
  /**
   * Creates the component to display the image of the black player.
   */
  
  protected ImageComponent createBlackImageComponent(Game game){
    return createPlayerImageComponent(game.getBlackName());
  }
  
  
  
  /**
   * Creates the component to display the image of the specified player.
   */
  
  protected ImageComponent createPlayerImageComponent(String name){
    ImageComponent imageComponent = new ImageComponent(getPlayerImage(name));
    imageComponent.setImageToolTip(true);
    return imageComponent;
  }
  
  
  
  /**
   * Returns the image to display for the specified player. May return
   * <code>null</code>.
   */
  
  protected Image getPlayerImage(String name){
    Connection conn = boardManager.getConn();
    URL url = conn.getPlayerPictureURL(conn.userForName(name));
    return url == null ? null : getToolkit().getImage(url);
  }
  
  
  
  /**
   * A Runnable we SwingUtilities.invokeLater() to set the move list table vertical
   * scrollbar value.
   */
  
  private class MoveListScrollBarUpdater implements Runnable{
    public void run(){
      JScrollBar vScroller = moveListTableScrollPane.getVerticalScrollBar();
      int selectedRow = moveListTable.getSelectedRow();
      int selectedColumn = moveListTable.getSelectedColumn();
      if ((selectedRow==-1)||(selectedColumn==-1))
        if (displayedMoveNumber==0)
          vScroller.setValue(vScroller.getMinimum());
        else
          vScroller.setValue(vScroller.getMaximum());
      else{
        moveListTable.scrollRectToVisible(moveListTable.getCellRect(selectedRow, selectedColumn, true));
      }
    }
  }
  
  
  
  /**
   * Adds a single move to the move list TableModel.
   */
  
  protected void addMoveToListTable(Move move){
    DefaultTableModel model = (DefaultTableModel)moveListTableModel;
    int movesSinceStart = game.getPliesSinceStart()/2;
    int rowCount = moveListTable.getRowCount();
    if ((rowCount == 0) || move.getPlayer().isWhite()){
      model.setNumRows(++rowCount);
      model.setValueAt((rowCount + movesSinceStart)+".", rowCount-1, 0);
    }
    if (move.getPlayer().isWhite())
      model.setValueAt(move, rowCount-1, 1);
    else{
      model.setValueAt(move, rowCount-1, 2);
    }
    
    if (displayedMoveNumber == 0){
      moveListTable.clearSelection();
      positionScrollBar.setValues(0, 1, 0, madeMoves.size() + 1); 
    }
    else{
      boolean isFirstMoveBlack = ((Move)madeMoves.elementAt(0)).getPlayer().isBlack();
      int visualMoveNumber = isFirstMoveBlack ? displayedMoveNumber + 1 : displayedMoveNumber;
      int row = (visualMoveNumber - 1) / 2;
      int column = 2 - (visualMoveNumber%2);
      
      isPositionScrollBarUpdating = true;
      isMoveListTableSelectionUpdating = true;
      setMoveListTableSelection(row, column);
      positionScrollBar.setValues(displayedMoveNumber, 1, 0, madeMoves.size() + 1); 
      isMoveListTableSelectionUpdating = false;
      isPositionScrollBarUpdating = false;
    }
    
    SwingUtilities.invokeLater(new MoveListScrollBarUpdater());
  }
  
  
  
  /**
   * Brings the the move list table up to date with the current move list and
   * displayed position.
   */
  
  protected void updateMoveListTable(){
    DefaultTableModel model = (DefaultTableModel)moveListTableModel;
    int movesSinceStart = game.getPliesSinceStart()/2;
    int moveCount = madeMoves.size();
    boolean isFirstMoveBlack = (moveCount > 0) && ((Move)madeMoves.elementAt(0)).getPlayer().isBlack();
    int numRows = isFirstMoveBlack ? 1+moveCount/2 : (moveCount+1)/2;
    model.setNumRows(numRows);
    for (int i = 0; i < numRows; i++)
      model.setValueAt((i + 1 + movesSinceStart)+".", i, 0);
    
    int row = 0;
    int column = isFirstMoveBlack ? 2 : 1;
    for (int i = 0; i < moveCount; i++){
      Object move = madeMoves.elementAt(i);
      model.setValueAt(move, row, column);
      column++;
      if (column == 3){
        row++;
        column = 1;
      }
    }
    
    if (column == 2) // There's an extra, empty cell, which we need to clear.
      model.setValueAt(null, row, column);
    
    
    if (displayedMoveNumber == 0){
      moveListTable.clearSelection();
      positionScrollBar.setValues(0, 1, 0, madeMoves.size() + 1);
    }
    else{
      int visualMoveNumber = isFirstMoveBlack ? displayedMoveNumber + 1 : displayedMoveNumber;
      row = (visualMoveNumber - 1) / 2;
      column = 2 - (visualMoveNumber%2);
      
      isPositionScrollBarUpdating = true;
      isMoveListTableSelectionUpdating = true;
      setMoveListTableSelection(row, column);
      positionScrollBar.setValues(displayedMoveNumber, 1, 0, madeMoves.size() + 1); 
      isMoveListTableSelectionUpdating = false;
      isPositionScrollBarUpdating = false;
    }
    
    SwingUtilities.invokeLater(new MoveListScrollBarUpdater());
  }
  
  
  
  /**
   * Sets the specified cell to be the selected cell in the move list table.
   */
  
  protected void setMoveListTableSelection(int row, int column){
    int oldRow = moveListTable.getSelectedRow();
    int oldColumn = moveListTable.getSelectedColumn();
    
    if (oldRow != row){
      settingMoveListTableSelection = true;
      moveListTable.setColumnSelectionInterval(column, column);
      settingMoveListTableSelection = false;
      moveListTable.setRowSelectionInterval(row, row);
    }
    else if (oldColumn != column){
      settingMoveListTableSelection = true;
      moveListTable.setRowSelectionInterval(row, row);
      settingMoveListTableSelection = false;
      moveListTable.setColumnSelectionInterval(column, column);
    }
  }
  
  
  
  /**
   * Updates the move highlighting on the board. The boolean argument specifies
   * whether the last made move (on the board, not the real position) is a move
   * made by the user and should therefore be ignored if
   * isHighlightingOwnMoves() returns false. Note that the callers don't 
   * really determine whether the last move is the user's - they only pass
   * true when it's a move that has just been made by the user.
   */
  
  private void updateMoveHighlighting(boolean isOwnMove){
    if ((displayedMoveNumber == 0) || (isOwnMove && !highlightOwnMoves))
      board.setHighlightedMove(null);
    else{
      Move move = (Move)madeMoves.elementAt(displayedMoveNumber - 1);
      board.setHighlightedMove(move);
    }
  }
  
  
  
  /**
   * Creates the JScrollBar which controls the position displayed on the board.
   */
  
  protected JScrollBar createPositionScrollBar(){
    JScrollBar scrollbar = new JScrollBar(JScrollBar.HORIZONTAL, 0, 1, 0, 1);
    return scrollbar;
  }
  
  
  
  /**
   * The panel actually containing all the components.
   */
  
  private class ContentPanel extends JPanel{
    
    
    
    /**
     * The gap between the container and components
     */
    
    private static final int CONTAINER_GAP = 6;
    
    
    
    /**
     * The gap between components.
     */
    
    private static final int GAP = 6;
    
    
    
    /**
     * Creates a new <code>ContentPanel</code>.
     */
    
    public ContentPanel(){
      setLayout(null);
    }
    
    
    
    /*
     * Preferred sizes of our children.
     */
    
    private Dimension whiteLabelPrefSize = null;
    private Dimension blackLabelPrefSize = null;
    private Dimension fullscreenButtonPrefSize = null;
    private Dimension gameLabelPrefSize = null;
    private Dimension positionScrollbarPrefSize = null;
    private Dimension moveListTableScrollPanePrefSize = null;
    private Dimension buttonPanelPrefSize = null;
    
    
    
    /**
     * Determines whether at the current size we should be in horizontal mode.
     */
    
    private boolean isHorizontal(){
      return (getWidth() - 20 > getHeight()) || (getWidth() == 0) || (getHeight() == 0);
    }
    
    /**
     * Lays out the panel.
     */
    
    public void doLayout(){
      calcPrefSizes();
      
      if (isHorizontal())
        layoutHorizontal();
      else
        layoutVertical();
    }
    
    
    
    /**
     * Queries our children for their preferred sizes.
     */
    
    private void calcPrefSizes(){
      if (whiteLabelPrefSize == null)
        whiteLabelPrefSize = whiteLabel.getPreferredSize();
      if (blackLabelPrefSize == null)
        blackLabelPrefSize = blackLabel.getPreferredSize();
      if (fullscreenButtonPrefSize == null)
        fullscreenButtonPrefSize = fullscreenButton.getPreferredSize();
      if (gameLabelPrefSize == null)
        gameLabelPrefSize = gameLabel.getPreferredSize();
      if (positionScrollbarPrefSize == null)
        positionScrollbarPrefSize = positionScrollBar.getPreferredSize();
      if (moveListTableScrollPanePrefSize == null)
        moveListTableScrollPanePrefSize = moveListTableScrollPane.getPreferredSize();
      if (buttonPanelPrefSize == null)
        buttonPanelPrefSize = buttonPanel.getPreferredSize();
    }
    
    
    
    /**
     * Lays out the panel in horizontal mode.
     */
    
    private void layoutHorizontal(){
      int width = getWidth();
      int height = getHeight();
      
      int boardSize = (height/8)*8;
      board.setBounds(0, (height - boardSize)/2, boardSize, boardSize);
      
      fullscreenButton.setBounds(width - fullscreenButtonPrefSize.width - CONTAINER_GAP, CONTAINER_GAP,
          fullscreenButtonPrefSize.width, fullscreenButtonPrefSize.height);
      
      int x = board.getWidth() + CONTAINER_GAP;
      int y = board.getY();
      width -= x + CONTAINER_GAP;
      height = boardSize;
      
      JComponent topLabel, bottomLabel, topClock, bottomClock;
      ImageComponent topImageComponent, bottomImageComponent;
      Dimension topLabelPrefSize, bottomLabelPrefSize;
      
      if (isFlipped){
        topLabel = whiteLabel;
        topClock = whiteClock;
        topImageComponent = whiteImageComponent;
        bottomLabel = blackLabel;
        bottomClock = blackClock;
        bottomImageComponent = blackImageComponent;
        topLabelPrefSize = whiteLabelPrefSize;
        bottomLabelPrefSize = blackLabelPrefSize;
      }
      else{
        topLabel = blackLabel;
        topClock = blackClock;
        topImageComponent = blackImageComponent;
        bottomLabel = whiteLabel;
        bottomClock = whiteClock;
        bottomImageComponent = whiteImageComponent;
        topLabelPrefSize = blackLabelPrefSize;
        bottomLabelPrefSize = whiteLabelPrefSize;
      }
      
      int clockHeight = height/10;
      
      if (topImageComponent.isImageLoaded())
        topImageComponent.setBounds(x, y, topLabelPrefSize.height, topLabelPrefSize.height);
      else
        topImageComponent.setBounds(x - GAP, y, 0, 0);
      topLabel.setBounds(topImageComponent.getX() + topImageComponent.getWidth() + GAP, y, 
          fullscreenButton.getX() - (topImageComponent.getWidth() + topImageComponent.getX()) - GAP, 
          topLabelPrefSize.height);
      y += topLabel.getHeight() + GAP;
      height -= topLabel.getHeight() + GAP;
      
      topClock.setBounds(x, y, width, clockHeight);
      y += topClock.getHeight() + GAP;
      height -= topClock.getHeight() + GAP;
      
      gameLabel.setBounds(x, y, width, gameLabelPrefSize.height);
      y += gameLabel.getHeight() + GAP;
      height -= gameLabel.getHeight() + GAP;
      
      positionScrollBar.setBounds(x, y, width, positionScrollbarPrefSize.height);
      y += positionScrollBar.getHeight() + GAP;
      height -= positionScrollBar.getHeight() + GAP;
      
      if (bottomImageComponent.isImageLoaded())
        bottomImageComponent.setBounds(x, y + height - bottomLabelPrefSize.height,
            bottomLabelPrefSize.height, bottomLabelPrefSize.height);
      else
        bottomImageComponent.setBounds(x - GAP, y + height - bottomLabelPrefSize.height, 0, 0);
      bottomLabel.setBounds(bottomImageComponent.getX() + bottomImageComponent.getWidth() + GAP, 
          y + height - bottomLabelPrefSize.height, width, bottomLabelPrefSize.height);
      height -= bottomLabel.getHeight() + GAP;
      
      bottomClock.setBounds(x, y + height - clockHeight, width, clockHeight);
      height -= bottomClock.getHeight() + GAP;
      
      buttonPanel.setBounds(x , y + height - buttonPanelPrefSize.height, width, buttonPanelPrefSize.height);
      height -= buttonPanelPrefSize.height + GAP;
      
      if (height >= 40)
        moveListTableScrollPane.setBounds(x, y, width, height);
      else
        moveListTableScrollPane.setBounds(0, 0, 0, 0);
    }
    
    
    
    /**
     * Lays out the panel in vertical mode.
     */
    
    private void layoutVertical(){
      JComponent topLabel, bottomLabel;
      JChessClock topClock, bottomClock;
      ImageComponent topImageComponent, bottomImageComponent;
      Dimension topLabelPrefSize, bottomLabelPrefSize;
      if (isFlipped){
        topLabel = whiteLabel;
        topClock = whiteClock;
        topImageComponent = whiteImageComponent;
        bottomLabel = blackLabel;
        bottomClock = blackClock;
        bottomImageComponent = blackImageComponent;
        topLabelPrefSize = whiteLabelPrefSize;
        bottomLabelPrefSize = blackLabelPrefSize;
      }
      else{
        topLabel = blackLabel;
        topClock = blackClock;
        topImageComponent = blackImageComponent;
        bottomLabel = whiteLabel;
        bottomClock = whiteClock;
        bottomImageComponent = whiteImageComponent;
        topLabelPrefSize = blackLabelPrefSize;
        bottomLabelPrefSize = whiteLabelPrefSize;
      }
      
      buttonPanel.setBounds(0, 0, 0, 0);
      positionScrollBar.setBounds(0, 0, 0, 0);
      moveListTableScrollPane.setBounds(0, 0, 0, 0);
      
      int x = CONTAINER_GAP;
      int y = CONTAINER_GAP;
      int width = getWidth() - 2*CONTAINER_GAP;
      int height = getHeight() - 2*CONTAINER_GAP;
      
      int clockWidth = Math.max(
          topClock.getPreferredWidth(topLabelPrefSize.height),
          bottomClock.getPreferredWidth(bottomLabelPrefSize.height));
      
      fullscreenButton.setBounds(x + width - CONTAINER_GAP - fullscreenButtonPrefSize.width, CONTAINER_GAP,
          fullscreenButtonPrefSize.width, fullscreenButtonPrefSize.height);
      
      gameLabel.setBounds(x, y, gameLabelPrefSize.width, gameLabelPrefSize.height);
      y += gameLabel.getHeight() + GAP;
      height -= gameLabel.getHeight() + GAP;
      
      if (topImageComponent.isImageLoaded())
        topImageComponent.setBounds(x, y, topLabelPrefSize.height, topLabelPrefSize.height);
      else
        topImageComponent.setBounds(x - GAP, y, 0, 0);
      topLabel.setBounds(topImageComponent.getX() + topImageComponent.getWidth() + GAP, y,
          topLabelPrefSize.width, topLabelPrefSize.height);
      topClock.setBounds(x + width - clockWidth, y, clockWidth, topLabelPrefSize.height);
      y += topLabel.getHeight() + GAP;
      height -= topLabel.getHeight() + GAP;
      
      int boardSize = (Math.min(height - bottomLabelPrefSize.height - 2*GAP - CONTAINER_GAP, width)/8)*8;
      board.setBounds((getWidth() - boardSize)/2, y, boardSize, boardSize);
      y += board.getHeight() + GAP;
      height -= board.getHeight() + GAP;
      
      if (bottomImageComponent.isImageLoaded())
        bottomImageComponent.setBounds(x, y, bottomLabelPrefSize.height, bottomLabelPrefSize.height);
      else
        bottomImageComponent.setBounds(x - GAP, y, 0, 0);
      bottomLabel.setBounds(bottomImageComponent.getX() + bottomImageComponent.getWidth() + GAP, y,
          bottomLabelPrefSize.width, bottomLabelPrefSize.height);
      bottomClock.setBounds(x + width - clockWidth, y, clockWidth, bottomLabelPrefSize.height);
      if ((buttonPanelPrefSize.width <= bottomClock.getX() - bottomLabel.getX() - bottomLabel.getWidth() - 2*GAP) &&
          (height >= buttonPanelPrefSize.height))
        buttonPanel.setBounds(bottomLabel.getX() + bottomLabel.getWidth() + GAP, y,
            bottomClock.getX() - bottomLabel.getX() - bottomLabel.getWidth() - 2*GAP, buttonPanelPrefSize.height);
      y += Math.max(bottomLabel.getHeight(), buttonPanel.getHeight()) + GAP;
      height -= Math.max(bottomLabel.getHeight(), buttonPanel.getHeight()) + GAP;
      
      if ((buttonPanel.getHeight() == 0) && (height - CONTAINER_GAP >= buttonPanelPrefSize.height)){
        buttonPanel.setBounds(x, y, width, buttonPanelPrefSize.height);
        y += buttonPanel.getHeight() + GAP;
        height -= buttonPanel.getHeight() + GAP;
      }
    }
    
    
    /**
     * Clears the remembered preferred sizes of the children.
     */
    
    public void invalidate(){
      super.invalidate();
      
      whiteLabelPrefSize = null;
      blackLabelPrefSize = null;
      fullscreenButtonPrefSize = null;
      gameLabelPrefSize = null;
      positionScrollbarPrefSize = null;
      moveListTableScrollPanePrefSize = null;
      buttonPanelPrefSize = null;
    }
    
    
    
    /**
     * Returns our preferred size.
     */
    
    public Dimension getPreferredSize(){
      calcPrefSizes();
      
      if (isHorizontal())
        return getHorizontalPrefSize();
      else
        return getVerticalPrefSize();
    }
    
    
    
    /**
     * Returns our preferred size in horizontal mode.
     */
    
    public Dimension getHorizontalPrefSize(){
      return new Dimension(
          320 + Math.max(
              Math.max(blackLabelPrefSize.width, whiteLabelPrefSize.width) + fullscreenButtonPrefSize.width,
              buttonPanelPrefSize.width) + 2*CONTAINER_GAP,
              Math.max(320, blackLabelPrefSize.height + 30 + gameLabelPrefSize.height + positionScrollbarPrefSize.height +
                  moveListTableScrollPanePrefSize.height + buttonPanelPrefSize.height + 30 + whiteLabelPrefSize.height +
                  7*GAP));
    }
    
    
    
    /**
     * Returns our preferred size in vertical mode.
     */
    
    public Dimension getVerticalPrefSize(){
      return new Dimension(320,
          320 + blackLabelPrefSize.height + gameLabelPrefSize.height + 
          Math.max(whiteLabelPrefSize.height, buttonPanelPrefSize.height) + 4*GAP + 2*CONTAINER_GAP);
    }
    
    
    
    /**
     * Returns our minimum size.
     */
    
    public Dimension getMinimumSize(){
      calcPrefSizes();
      
      if (isHorizontal())
        return getHorizontalMinSize();
      else
        return getVerticalMinSize();
    }
    
    
    
    /**
     * Returns our minimum size in horizontal mode.
     */
    
    public Dimension getHorizontalMinSize(){
      return new Dimension(
          240 + Math.max(
              Math.max(blackLabelPrefSize.width, whiteLabelPrefSize.width) + fullscreenButtonPrefSize.width,
              buttonPanelPrefSize.width) + 2*CONTAINER_GAP,
              Math.max(240, blackLabelPrefSize.height + 30 + gameLabelPrefSize.height + positionScrollbarPrefSize.height +
                  buttonPanelPrefSize.height + 30 + whiteLabelPrefSize.height +
                  6*GAP));
    }
    
    
    
    /**
     * Returns our minimum size in vertical mode.
     */
    
    public Dimension getVerticalMinSize(){
      return new Dimension(240,
          240 + blackLabelPrefSize.height + gameLabelPrefSize.height + whiteLabelPrefSize.height + 3*GAP + 2*CONTAINER_GAP);
    }
    
    
    
  }
  
  
  
  /**
   * Returns true if the BoardPanel is flipped (black at bottom).
   */
  
  public final boolean isFlipped(){
    // Final because we directly set the isFlipped *variable* in init(Game) and
    // thus must not let anyone redefine the meaning of "flipped".
    
    return isFlipped;
  }
  
  
  
  /**
   * Sets the flipped state of this BoardPanel.
   */
  
  public void setFlipped(boolean b){
    if (isFlipped() != b){
      isFlipped = b;
      board.setFlipped(isFlipped);
      contentPanel.revalidate();
    }
  }
  
  
  
  /**
   * Returns the Game displayed by this BoardPanel.
   */
  
  public Game getGame(){
    return game;
  }
  
  
  
  /**
   * Returns the game board.
   */
  
  public JinBoard getBoard(){
    return board;
  }
  
  
  
  /**
   * Returns the specified player's clock.
   */
  
  protected AbstractChessClock getClockForPlayer(Player player){
    return player.isWhite() ? whiteClock : blackClock;
  }
  
  
  
  /**
   * Updates the clock's activeness according to the current player to move on the
   * board.
   */
  
  protected void updateClockActiveness(){
    Player curPlayer = board.getPosition().getCurrentPlayer();
    AbstractChessClock curClock = getClockForPlayer(curPlayer);
    AbstractChessClock oppClock = getClockForPlayer(curPlayer.getOpponent());
    
    curClock.setActive(true);
    oppClock.setActive(false);
  }
  
  
  
  /**
   * Plays the sound corresponding to the specified event.
   */
  
  public void playSound(String eventName){
    boardManager.playSound(eventName);
  }
  
  
  
  /**
   * Returns if there is currently a move enRoute. This simply checks that the
   * <code>moveInRoute</code> variable is non-null.
   */
  
  private boolean isMoveEnRoute(){
    return moveEnRoute != null;
  }
  
  
  
  /**
   * Sets the queued move to the specified Move. Null is a valid value, which
   * clears the queued move.
   */
  
  private void setQueuedMove(Move move){
    if (queuedMove != null)
      board.setShaded(queuedMove.getEndingSquare(), false);
    
    queuedMove = move;
    if (queuedMove != null)
      board.setShaded(queuedMove.getEndingSquare(), true);
    
    board.setEditable((queuedMove == null) && (displayedMoveNumber == madeMoves.size()));
  }
  
  
  
  /**
   * Sets the currently displayed move to the specified move.
   */
  
  private void setDisplayedMove(int moveNum){
    if ((moveNum < 0) || (moveNum > madeMoves.size()))
      throw new IllegalArgumentException("displayed move number out of range");
    
    displayedMoveNumber = moveNum;
    
    if (displayedMoveNumber != madeMoves.size())
      board.setEditable(false);
    else if (moveSendingMode == BoardManager.PREMOVE_MOVE_SENDING_MODE)
      board.setEditable(queuedMove == null);
    else if (moveSendingMode == BoardManager.LEGAL_CHESS_MOVE_SENDING_MODE)
      board.setEditable(isUserTurn());
    else if (moveSendingMode == BoardManager.PREDRAG_MOVE_SENDING_MODE)
      board.setEditable(true);
    else
      throw new IllegalStateException("Unrecognized move sending mode: "+moveSendingMode);
  }
  
  
  
  /**
   * GameListener implementation. Note that this method is never actually called
   * because a BoardPanel is created (by the BoardManager) in response to a game
   * start event, so when we register for game events, the game start event has
   * already been dispatched.
   */
  
  public void gameStarted(GameStartEvent evt){}
  
  
  
  /**
   * GameListener implementation. Makes the appropriate move on the board.
   */
  
  public void moveMade(MoveMadeEvent evt){
    if (evt.getGame() != game)
      return;
    
    Move move = evt.getMove();
    
    // Is this an echo to the current move en route?
    boolean moveEnRouteEcho = (moveEnRoute != null) &&
    Utilities.areEqual(move.getStartingSquare(), moveEnRoute.getStartingSquare()) &&
    Utilities.areEqual(move.getEndingSquare(), moveEnRoute.getEndingSquare()) &&
    Utilities.areEqual(move.getPlayer(), moveEnRoute.getPlayer());
    
    boolean shouldUpdateBoard = true; 
    
    // The user is looking at a position other than the last one, so we don't
    // want to update the board when a new move arrives.
    if (displayedMoveNumber != madeMoves.size())
      shouldUpdateBoard = false;
    
    madeMoves.addElement(move);
    realPosition.makeMove(move);
    
    // This is not the server echoeing a move made on the board
    // Note that this may still be a move done by the user (from the console).
    if (!isMoveEnRoute()){
      if (evt.isNew())
        playAudioClipForMove(move);
      
      if (shouldUpdateBoard){
        isBoardPositionUpdating = true;
        board.getPosition().copyFrom(realPosition);
        isBoardPositionUpdating = false;
      }
    }
    
    if (shouldUpdateBoard){
      setDisplayedMove(madeMoves.size());
      updateMoveHighlighting(isMoveEnRoute());
    }
    
    // queuedMove.getPlayer() == realPosition.getCurrentPlayer() makes sure
    // that we only send the queued on the correct move, not when getting *any* response
    if ((queuedMove != null) && (queuedMove.getPlayer() == realPosition.getCurrentPlayer())){
      Move premove = null;
      try{
        premove = game.getVariant().createMove(realPosition, queuedMove);
        if (!checkLegality(realPosition, premove))
          premove = null;
      } catch (IllegalArgumentException e){}
      if (premove == null){ // Illegal premove
        isBoardPositionUpdating = true;
        board.getPosition().copyFrom(realPosition);
        isBoardPositionUpdating = false;
        setQueuedMove(null);
        playSound("IllegalMove");
      }
      else{
        UserMoveEvent evt2 = new UserMoveEvent(this, premove);
        isBoardPositionUpdating = true;
        board.getPosition().copyFrom(realPosition);
        board.getPosition().makeMove(premove);
        isBoardPositionUpdating = false;
        moveEnRoute = premove;
        setQueuedMove(null);
        fireUserMadeMove(evt2);
      }
    }
    else if (moveEnRouteEcho)
      moveEnRoute = null;
    
    updateClockActiveness();
    addMoveToListTable(move);
  }
  
  
  
  /**
   * If the specified move is legal in the specified position, this method
   * returns <code>true</code>. If the specified move is illegal, it may return
   * <code>false</code>. It's not meant as a complete move legality check,
   * instead, it (currently) only detects some obviously illegal moves so that
   * they can be rejected immediately, instead of wasting time by sending them
   * to the server. With time, more complex (and eventually complete) move
   * legality checking will be added. 
   */
  
  protected boolean checkLegality(Position pos, Move move){
    if (move instanceof ChessMove){
      ChessMove cmove = (ChessMove)move;
      
      // Trying to move a piece that isn't your own.
      if (pos.getCurrentPlayer() != cmove.getPlayer())
        return false;
      
      // Trying to capture your own piece
      if (cmove.isCapture() && (cmove.getCapturedPiece().getPlayer() == pos.getCurrentPlayer()))
        return false;
    }
    
    return true;
  }
  
  
  
  /**
   * GameListener implementation. Sets the appropriate position on the board.
   */
  
  public void positionChanged(PositionChangedEvent evt){
    if (evt.getGame() != game)
      return;
    
    madeMoves.removeAllElements();
    realPosition.copyFrom(evt.getPosition());
    
    isBoardPositionUpdating = true;
    board.getPosition().copyFrom(realPosition);
    isBoardPositionUpdating = false;
    
    setDisplayedMove(0);
    
    updateMoveHighlighting(false);
    
    moveEnRoute = null;   // We shouldn't keep state between 
    setQueuedMove(null);  // such drastic position changes
    
    updateClockActiveness();
    updateMoveListTable();
  }
  
  
  
  /**
   * GameListener implementation. Reverts the position on the board to the position
   * that occured on it "the amount of moves taken back" moves ago.
   */
  
  public void takebackOccurred(TakebackEvent evt){
    if (evt.getGame()!=game)
      return;
    
    int takebackCount = evt.getTakebackCount();
    int numMadeMoves = madeMoves.size()-takebackCount;
    for (int i = madeMoves.size()-1; i >= numMadeMoves; i--)
      madeMoves.removeElementAt(i);
    
    realPosition.copyFrom(game.getInitialPosition());
    for (int i = 0; i < numMadeMoves; i++)
      realPosition.makeMove((Move)madeMoves.elementAt(i));
    
    moveEnRoute = null;
    setQueuedMove(null);
    
    // Try not to change the board if possible. If, however we were displaying the position
    // after a move that was taken back, we have to update the board.
    if (displayedMoveNumber >= madeMoves.size()){
      isBoardPositionUpdating = true;
      board.getPosition().copyFrom(realPosition);
      isBoardPositionUpdating = false;
      setDisplayedMove(madeMoves.size());
      updateMoveHighlighting(false);
    }
    
    updateClockActiveness();
    updateMoveListTable();
  }
  
  
  
  /**
   * GameListener implementation. Returns the position on the board to its current
   * "real" state.
   */
  
  public void illegalMoveAttempted(IllegalMoveEvent evt){
    if (evt.getGame() != game)
      return;
    
    if (!isMoveEnRoute()) // We didn't make this move. 
      return;             // It could've been sent by one of the other plugins.
    
    moveEnRoute = null;
    setQueuedMove(null);
    
    isBoardPositionUpdating = true;
    board.getPosition().copyFrom(realPosition);
    isBoardPositionUpdating = false;
    
    // Subtract the time spent on sending the move from the player's clock
    // and restart the it.
    AbstractChessClock playersClock = getClockForPlayer(board.getPosition().getCurrentPlayer());
    playersClock.setTime((int)(playersClock.getTime() - (System.currentTimeMillis() - sentMoveTimestamp)));
    playersClock.setRunning(true);
    
    updateClockActiveness();
    setDisplayedMove(madeMoves.size());
    updateMoveListTable();
  }
  
  
  
  /**
   * GameListener implementation. Adjusts the clock time and activeness.
   */
  
  public void clockAdjusted(ClockAdjustmentEvent evt){
    if (evt.getGame() != game)
      return;
    
    Player player = evt.getPlayer();
    int time = evt.getTime();
    boolean isRunning = evt.isClockRunning();
    AbstractChessClock clock = player.equals(Player.WHITE_PLAYER) ?
        whiteClock : blackClock;
    
    
    clock.setTime(time);
    clock.setRunning(isRunning);
  }
  
  
  
  /**
   * GameListener implementation.
   */
  
  public void boardFlipped(BoardFlipEvent evt){
    if (evt.getGame()!=game)
      return;
    
    setFlipped(evt.isFlipped());
  }
  
  
  
  /**
   * GameListener implementation.
   */
  
  public void offerUpdated(OfferEvent evt){
    
  }
  
  
  
  /**
   * GameListener implementation.
   */
  
  public void gameEnded(GameEndEvent evt){
    if (evt.getGame()!=game)
      return;
    
    whiteClock.setRunning(false);
    blackClock.setRunning(false);
    
    setInactive();
  }
  
  
  
  /**
   * Plays the audio clip appropriate for the given <code>Move</code>.
   */
  
  protected void playAudioClipForMove(Move move){
    if (move instanceof ChessMove){
      ChessMove cMove = (ChessMove)move;
      if (cMove.isCapture())
        playSound("Capture");
      else if (cMove.isCastling())
        playSound("Castling");
      else
        playSound("Move");
    }
    else
      playSound("Move");
  }
  
  
  
  /**
   * This is called when the user makes a move on the board (MoveListener implementation).
   */
  
  public void moveMade(MoveEvent evt){
    if (isBoardPositionUpdating)
      return;
    
    Position source = evt.getPosition();
    Move move = evt.getMove();
    
    if (source == board.getPosition()){
      playAudioClipForMove(move);
      if (game.isPlayed() && (moveSendingMode == BoardManager.PREMOVE_MOVE_SENDING_MODE) &&
          (isMoveEnRoute() || !isUserTurn()))
        setQueuedMove(move);
      else{
        UserMoveEvent evt2 = new UserMoveEvent(this, move);
        fireUserMadeMove(evt2);
        moveEnRoute = evt.getMove();
        
        // Stop the clock of the player who moved
        getClockForPlayer(move.getPlayer()).setRunning(false);
        
        // Remember the time when it was stopped because if the move is illegal
        // we will need to restart the clock with the correct amount of time
        // (including the time the move spent in transit).
        sentMoveTimestamp = System.currentTimeMillis();
        
        // Update clock activeness
        updateClockActiveness();
        
        if (moveSendingMode == BoardManager.LEGAL_CHESS_MOVE_SENDING_MODE)
          board.setEditable(false);
      }
    }
  }
  
  
  
  /**
   * Returns true if this is currently the user's turn to move. If this is an
   * observed game, returns false. If this is a game examined by the user returns
   * true. If this is a game played by the user returns whether it's currently
   * the user's turn.
   */
  
  protected boolean isUserTurn(){
    if (game.getGameType() == Game.OBSERVED_GAME)
      return false;
    else if (game.getGameType() == Game.ISOLATED_BOARD)
      return false;
    else{ // MY_GAME
      if (game.isPlayed()){
        Player userPlayer = game.getUserPlayer();
        return realPosition.getCurrentPlayer().equals(userPlayer);
      }
      else
        return true;
    }
  }
  
  
  
  /**
   * This method is called when either the row or column selection in the moveListTable
   * changes. Changes the board position to the position after the selected move.
   */
  
  protected void moveListTableSelectionChanged(){
    if (settingMoveListTableSelection)
      return;
    
    if (isMoveListTableSelectionUpdating)
      return;
    
    int row = moveListTable.getSelectedRow();
    int column = moveListTable.getSelectedColumn();
    
    if ((row == -1) || (column == -1))
      return;
    
    isMoveListTableSelectionUpdating = true;
    
    try{
      boolean isFirstMoveBlack = ((Move)madeMoves.elementAt(0)).getPlayer().isBlack();
      
      int moveNum = column + row*2;
      if (isFirstMoveBlack && (moveNum > 0))
        moveNum--;
      
      if (moveNum == madeMoves.size() + 1) // The user pressed the last empty cell
        moveNum--;
      
      if (moveNum > madeMoves.size()) // Shouldn't happen
        throw new IllegalStateException();
      
      Position pos = game.getInitialPosition();
      for (int i = 0; i < moveNum; i++){
        Move move = (Move)madeMoves.elementAt(i);
        pos.makeMove(move);
      }
      
      board.clearShaded();
      
      if ((moveNum == madeMoves.size()) && (queuedMove != null)){
        pos.makeMove(queuedMove);
        board.setShaded(queuedMove.getEndingSquare(), true);
      }
      
      isBoardPositionUpdating = true;
      board.getPosition().copyFrom(pos);
      isBoardPositionUpdating = false;
      setDisplayedMove(moveNum);
      
      if (!isPositionScrollBarUpdating)
        positionScrollBar.setValues(displayedMoveNumber, 1, 0, madeMoves.size() + 1); 
      
      board.setEditable(displayedMoveNumber == madeMoves.size());
      
      updateMoveHighlighting(false);
      
      SwingUtilities.invokeLater(new MoveListScrollBarUpdater());
    } finally{
      isMoveListTableSelectionUpdating = false;
    }
  }
  
  
  
  /**
   * AdjustmentListener implementation. Listens to events from the positionScrollBar
   * and updates the position on the board accordingly.
   */
  
  public void adjustmentValueChanged(AdjustmentEvent evt){
    Object source = evt.getSource();
    if (source == positionScrollBar){
      if (isPositionScrollBarUpdating)
        return;
      
      isPositionScrollBarUpdating = true;
      
      if (madeMoves.size() > 0){
        int moveNum = positionScrollBar.getValue();
        
        boolean isFirstMoveBlack = ((Move)madeMoves.elementAt(0)).getPlayer().isBlack();
        int visualMoveNumber = isFirstMoveBlack ? moveNum + 1 : moveNum;
        int row = (visualMoveNumber-1)/2;
        int column = (visualMoveNumber == 0) ? 0 : 2 - (visualMoveNumber%2);
        
        if (!isMoveListTableSelectionUpdating){
          setMoveListTableSelection(row, column);
        }
      }
      
      isPositionScrollBarUpdating = false;
    }
  }
  
  
  
  /**
   * <code>PropertyChangeListener</code> implementation.
   */
  
  public void propertyChange(PropertyChangeEvent evt){
    Object src = evt.getSource();
    String propertyName = evt.getPropertyName();
    if (src == boardManager){
      if ("moveInputStyle".equals(propertyName))
        board.setMoveInputStyle(boardManager.getMoveInputStyle());
      else if ("pieceFollowsCursor".equals(propertyName))
        board.setPieceFollowsCursor(boardManager.isPieceFollowsCursor());
      else if ("highlightMadeMoveSquares".equals(propertyName))
        board.setHighlightMadeMoveSquares(boardManager.isHighlightMadeMoveSquares());
      else if ("madeMoveSquaresHighlightColor".equals(propertyName))
        board.setMadeMoveSquaresHighlightColor(boardManager.getMadeMoveSquaresHighlightColor());
      else if ("moveHighlightingStyle".equals(propertyName))
        board.setMoveHighlightingStyle(boardManager.getMoveHighlightingStyle());
      else if ("autoPromote".equals(propertyName))
        board.setManualPromote(!boardManager.isAutoPromote());
      else if ("moveHighlightingColor".equals(propertyName))
        board.setMoveHighlightingColor(boardManager.getMoveHighlightingColor());
      else if ("highlightingOwnMoves".equals(propertyName))
        highlightOwnMoves = boardManager.isHighlightingOwnMoves();
      else if ("moveSendingMode".equals(propertyName)){
        moveSendingMode = boardManager.getMoveSendingMode();
        
        if (displayedMoveNumber == madeMoves.size()){
          if ((moveSendingMode == BoardManager.PREMOVE_MOVE_SENDING_MODE) || 
              (moveSendingMode == BoardManager.PREDRAG_MOVE_SENDING_MODE))
            board.setEditable(true);
          else if (moveSendingMode == BoardManager.LEGAL_CHESS_MOVE_SENDING_MODE)
            board.setEditable(isUserTurn());
          else
            throw new IllegalStateException("Unrecognized move sending mode: " + moveSendingMode);
        }
      }
      else if ("coordsDisplayStyle".equals(propertyName))
        board.setCoordsDisplayStyle(boardManager.getCoordsDisplayStyle());
      else if ("coordsDisplayColor".equals(propertyName))
        board.setCoordsDisplayColor(boardManager.getCoordsDisplayColor());
      else if ("pieceSet".equals(propertyName) ||
          "whitePieceColor".equals(propertyName) ||
          "blackPieceColor".equals(propertyName) ||
          "whiteOutlineColor".equals(propertyName) ||
          "blackOutlineColor".equals(propertyName)){
        board.setPiecePainter(boardManager.getPiecePainter());
      }
      else if ("boardPattern".equals(propertyName) ||
          "lightSquareColor".equals(propertyName) ||
          "darkSquareColor".equals(propertyName)){
        board.setBoardPainter(boardManager.getBoardPainter());
      }
      else if ("showShadowPieceInTargetSquare".equals(propertyName))
        board.setShowShadowPieceInTargetSquare(boardManager.isShowShadowPieceInTargetSquare());
      else if ("highlightLegalTargetSquares".equals(propertyName))
        board.setHighlightLegalTargetSquares(boardManager.isHighlightLegalTargetSquares());
      else if ("snapToLegalSquare".equals(propertyName))
        board.setSnapToLegalSquare(boardManager.isSnapToLegalSquare());
    }
    else if (src == game){
      gameLabel.setText(createGameLabelText(game));
      if ("whiteName".equals(propertyName)){
        whiteLabel.setText(createWhiteLabelText(game));
        whiteImageComponent.setImage(getPlayerImage(game.getWhiteName()));
        contentPanel.revalidate();
      }
      else if ("blackName".equals(propertyName)){
        blackLabel.setText(createBlackLabelText(game));
        blackImageComponent.setImage(getPlayerImage(game.getBlackName()));
        contentPanel.revalidate();
      }
      else if ("gameType".equals(propertyName) || "played".equals(propertyName)){
        configureBoardFromGame(game, board);
        board.setEditable(isUserTurn() || (moveSendingMode != BoardManager.LEGAL_CHESS_MOVE_SENDING_MODE));
        
        contentPanel.remove(buttonPanel);
        contentPanel.remove(gameLabel);
        buttonPanel = createButtonPanel(game);
        gameLabel = createGameLabel(game);
        contentPanel.add(buttonPanel);
        contentPanel.add(gameLabel);
        
        contentPanel.revalidate();
      }
    }
  }
  
  
  
  /**
   * Returns true if this BoardPanel is active, false otherwise.
   */
  
  public boolean isActive(){
    return isActive;
  }
  
  
  
  /**
   * Makes the BoardPanel inactive. Calling this method will make the BoardPanel
   * stop notifying about moves made by the user. It will also change the
   * move input mode to {@link #ALL_PIECES_MOVE}.
   */
  
  public void setInactive(){
    this.isActive = false;
    board.getPosition().removeMoveListener(this);
    board.setMoveInputMode(JinBoard.ALL_PIECES_MOVE);
    board.setEditable(true);
    
    whiteClock.setRunning(false);
    blackClock.setRunning(false);
    
    if (whiteClock instanceof JChessClock){
      ((JChessClock)whiteClock).setActiveForeground(Color.lightGray);
      ((JChessClock)whiteClock).setInactiveForeground(Color.gray);
    }
    if (blackClock instanceof JChessClock){
      ((JChessClock)blackClock).setActiveForeground(Color.lightGray);
      ((JChessClock)blackClock).setInactiveForeground(Color.gray);
    }
  }
  
  
  
  /**
   * This method is called by the <code>BoardManager</code> when this
   * <code>BoardPanel</code> is no longer required.
   */
  
  public void done(){
    boardManager.removePropertyChangeListener(this); 
  }
  
  
  
  /**
   * Before we are removed, if we are in fullscreen mode, we must restore normal
   * mode.
   */
  
  public void removeNotify(){
    if (fullscreenPanel.getFullscreenModeModel().isOn())
      fullscreenPanel.getFullscreenModeModel().set(false);
    
    super.removeNotify();
  }
  
  
  
}
