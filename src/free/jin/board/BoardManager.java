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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Toolkit;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import free.chess.*;
import free.jin.Connection;
import free.jin.Game;
import free.jin.I18n;
import free.jin.Preferences;
import free.jin.board.event.UserMoveEvent;
import free.jin.board.event.UserMoveListener;
import free.jin.event.*;
import free.jin.plugin.*;
import free.jin.ui.OptionPanel;
import free.jin.ui.PreferencesPanel;
import free.jin.ui.UIProvider;
import free.util.BeanProperties;


/**
 * The plugin responsible for displaying boards and handling all things related
 * to that.
 */

public class BoardManager extends Plugin implements GameListener, UserMoveListener,
    PluginUIListener, ConnectionListener{



  /**
   * The code for move sending mode where the user is not allowed to move his
   * pieces when it's not his turn.
   */

  public static final int LEGAL_CHESS_MOVE_SENDING_MODE = 1;



  /**
   * The code for move sending mode where the user is allowed to pick up his
   * pieces (more than once) and make moves when it's not his turn. The moves
   * are then sent immediately to the server.
   */

  public static final int PREDRAG_MOVE_SENDING_MODE = 2;



  /**
   * The code for premove move sending mode. The user is allowed to pick up
   * a piece and make a move when it's not his turn. The move is then not sent
   * until it is the user's move. After making the "premove", the user cannot
   * make additional "premoves".
   */

  public static final int PREMOVE_MOVE_SENDING_MODE = 3;

  

  /**
   * A reference to the sound manager, if one exists.
   */

  private Plugin soundManager = null;



  /**
   * A Hashtable mapping Game objects to BoardPanel objects which are currently
   * used.
   */

  protected final Hashtable gamesToBoardPanels = new Hashtable();




  /**
   * A Hashtable mapping PluginUIContainer objects to BoardPanels they contain.
   */

  protected final Hashtable containersToBoardPanels = new Hashtable();




  /**
   * A Hashtable mapping BoardPanels to their PluginUIContainers.
   */

  protected final Hashtable boardPanelsToContainers = new Hashtable();




  /**
   * A list of the PluginUIContainers in the order they were created.
   */

  protected final Vector containers = new Vector();




  /**
   * A <code>BeanProperties</code> object where we keep our properties.
   */

  private final BeanProperties props = new BeanProperties(this);
  
  
  
  /**
   * The current piece painter.
   */
   
  private PiecePainter piecePainter;
  
  
  
  /**
   * The current board painter.
   */
   
  private BoardPainter boardPainter;




  /**
   * Starts this plugin.
   */

  public void start() throws PluginStartException{
    obtainSoundManager();
    initPreferences();
    registerConnListeners();
    preload();
  }



  /**
   * Stops this plugin.
   */

  public void stop(){
    unregisterConnListeners();
  }




  /**
   * Obtains a reference to the SoundManager, if one exists.
   */

  private void obtainSoundManager(){
    soundManager = getPlugin("sound");
  }



  /**
   * Plays the sound for the given event name.
   */

  public void playSound(String eventName){
    if (soundManager != null)
      playSoundImpl(eventName);
    else
      Toolkit.getDefaultToolkit().beep();
  }
  
  
  
  /**
   * Actually plays the sound for the specified event name. This is (or should
   * be only called if a sound manager exists).
   */
   
  protected void playSoundImpl(String eventName){
    try{
      Class soundManagerClass = Class.forName("free.jin.sound.SoundManager");
      Method playEventSoundMethod = soundManagerClass.getMethod("playEventSound",
        new Class[]{String.class});
        
      playEventSoundMethod.invoke(soundManager, new Object[]{eventName});
    } catch (ClassNotFoundException e){e.printStackTrace();}
      catch (NoSuchMethodException e){e.printStackTrace();}
      catch (IllegalAccessException e){e.printStackTrace();}
      catch (InvocationTargetException e){e.printStackTrace();}
    
  }



  /**
   * Loads the piece sets.
   */
   
  // protected void loadPieceSets() throws PluginStartException{
  //   Resource [] pieceSetsArr = getResources("pieces");

  //   if ((pieceSetsArr == null) || (pieceSetsArr.length == 0)){
  //     pieceSets.put("default", PieceSet.DEFAULT_PIECE_SET);
  //     return;
  //   }
      
  //   for (int i = 0; i < pieceSetsArr.length; i++){
  //     PieceSet pieceSet = (PieceSet)pieceSetsArr[i];
  //     if (pieceSet.isCompatibleWith(getServer()))
  //       pieceSets.put(pieceSet.getId(), pieceSet);
  //   }
  // }
  
  
  
  /**
   * Loads the board patterns.
   */
   
  // protected void loadBoardPatterns() throws PluginStartException{
  //   Resource [] boardPatternsArr = getResources("boards");

  //   if ((boardPatternsArr == null) || (boardPatternsArr.length == 0)){
  //     boardPatterns.put("default", BoardPattern.DEFAULT_BOARD_PATTERN);
  //     return;
  //   }
      
  //   for (int i = 0; i < boardPatternsArr.length; i++){
  //     BoardPattern boardPattern = (BoardPattern)boardPatternsArr[i];
  //     if (boardPattern.isCompatibleWith(getServer()))
  //       boardPatterns.put(boardPattern.getId(), boardPattern);
  //   }
  // }



  /**
   * Initializes various preferences from the user's properties.
   */

  protected void initPreferences(){
    Preferences prefs = getPrefs();

    setAutoPromote(prefs.getBool("auto-promote", false));

    setMoveInputStyle("click'n'click".equals(prefs.getString("move-input-style", "drag'n'drop")) ? 
      JBoard.CLICK_N_CLICK : JBoard.DRAG_N_DROP);

    setPieceFollowsCursor(prefs.getBool("piece-follows-cursor", true));
    
    setHighlightMadeMoveSquares(prefs.getBool("highlight-made-move-squares", false));
    setMadeMoveSquaresHighlightColor(
      prefs.getColor("highlight-made-move-squares.color", new Color(0x0000ff)));

    String moveHighlightingStyleString = prefs.getString("move-highlight.style", "square");
    if ("target-square".equals(moveHighlightingStyleString))
      setMoveHighlightingStyle(JBoard.TARGET_SQUARE_MOVE_HIGHLIGHTING);
    else if ("square".equals(moveHighlightingStyleString))
      setMoveHighlightingStyle(JBoard.BOTH_SQUARES_MOVE_HIGHLIGHTING);
    else if ("arrow".equals(moveHighlightingStyleString))
      setMoveHighlightingStyle(JBoard.ARROW_MOVE_HIGHLIGHTING);
    else
      setMoveHighlightingStyle(JBoard.NO_MOVE_HIGHLIGHTING);

    setHighlightingOwnMoves(prefs.getBool("move-highlight.highlight-own", false));

    setMoveHighlightingColor(prefs.getColor("move-highlight.color", new Color(0x00b2b2)));

    String moveSendingModeString = prefs.getString("move-sending-mode", "predrag");
    if ("legal-chess".equals(moveSendingModeString))
      setMoveSendingMode(LEGAL_CHESS_MOVE_SENDING_MODE);
    else if ("premove".equals(moveSendingModeString))
      setMoveSendingMode(PREMOVE_MOVE_SENDING_MODE);
    else
      setMoveSendingMode(PREDRAG_MOVE_SENDING_MODE);
    
    String coordsDisplayStyleString = prefs.getString("coords-display.style", "none");
    if ("rim".equals(coordsDisplayStyleString))
      setCoordsDisplayStyle(JBoard.RIM_COORDS);
    else if ("outside".equals(coordsDisplayStyleString))
      setCoordsDisplayStyle(JBoard.OUTSIDE_COORDS);
    else if ("every-square".equals(coordsDisplayStyleString))
      setCoordsDisplayStyle(JBoard.EVERY_SQUARE_COORDS);
    else
      setCoordsDisplayStyle(JBoard.NO_COORDS);
    
    setCoordsDisplayColor(prefs.getColor("coords-display.color", Color.blue.darker()));

    String pieceSetId = prefs.getString("piece-set-id", null);
    if (pieceSetId != null)
      setPieceSet((PieceSet)getResource("pieces", pieceSetId));
    
    String boardPatternId = prefs.getString("board-pattern-id", null);
    if (boardPatternId != null)
      setBoardPattern((BoardPattern)getResource("boards", boardPatternId));
    
    setWhitePieceColor(prefs.getColor("white-piece-color", Color.white));
    setBlackPieceColor(prefs.getColor("black-piece-color", Color.black));
    setWhiteOutlineColor(prefs.getColor("white-outline-color", Color.black));
    setBlackOutlineColor(prefs.getColor("black-outline-color", Color.white));

    setLightSquareColor(prefs.getColor("light-square-color", new Color(0xffcf90)));
    setDarkSquareColor(prefs.getColor("dark-square-color", new Color(0x8f604f)));
  }



  /**
   * Registers the specified <code>PropertyChangeListener</code>.
   */

  public void addPropertyChangeListener(PropertyChangeListener listener){
    props.addPropertyChangeListener(listener);
  }

  


  /**
   * Unregisters the specified <code>PropertyChangeListener</code>.
   */

  public void removePropertyChangeListener(PropertyChangeListener listener){
    props.removePropertyChangeListener(listener);
  }



  /**
   * Configures the current <code>PiecePainter</code> to prepare it for usage.
   */

  protected void configurePiecePainter(){
    if (piecePainter instanceof ColoredPiecePainter){
      ColoredPiecePainter coloredPiecePainter = (ColoredPiecePainter)piecePainter;

      Color whiteColor = getWhitePieceColor(); 
      Color blackColor = getBlackPieceColor(); 
      Color whiteOutline = getWhiteOutlineColor();
      Color blackOutline = getBlackOutlineColor();

      if (whiteColor != null)
        coloredPiecePainter.setWhiteColor(whiteColor);
      if (blackColor != null)
        coloredPiecePainter.setBlackColor(blackColor);
      if (whiteOutline != null)
        coloredPiecePainter.setWhiteOutline(whiteOutline);
      if (blackOutline != null)
        coloredPiecePainter.setBlackOutline(blackOutline);
    }
  }



  /**
   * Configures the current <code>BoardPainter</code> preparing it to be used.
   */

  protected void configureBoardPainter(){
    if (boardPainter instanceof ColoredBoardPainter){
      ColoredBoardPainter coloredBoardPainter = (ColoredBoardPainter)boardPainter;

      Color lightColor = getLightSquareColor();
      Color darkColor = getDarkSquareColor();

      if (lightColor != null)
        coloredBoardPainter.setLightColor(lightColor);
      if (darkColor != null)
        coloredBoardPainter.setDarkColor(darkColor);
    }
  }



  /**
   * Returns the current white pieces color.
   */

  public Color getWhitePieceColor(){
    return (Color)props.getProperty("whitePieceColor", null);
  }



  /**
   * Sets the white piece color to the specified value.
   */

  public void setWhitePieceColor(Color color){
    props.setProperty("whitePieceColor", color);
    configurePiecePainter();
  }



  /**
   * Returns the current black pieces color.
   */

  public Color getBlackPieceColor(){
    return (Color)props.getProperty("blackPieceColor", null);
  }



  /**
   * Sets the black piece color to the specified value.
   */

  public void setBlackPieceColor(Color color){
    props.setProperty("blackPieceColor", color);
    configurePiecePainter();
  }



  /**
   * Returns the current white pieces outline color.
   */

  public Color getWhiteOutlineColor(){
    return (Color)props.getProperty("whiteOutlineColor", null);
  }



  /**
   * Sets the white piece outline color to the specified value.
   */

  public void setWhiteOutlineColor(Color color){
    props.setProperty("whiteOutlineColor", color);
    configurePiecePainter();
  }



  /**
   * Returns the current black pieces outline color.
   */

  public Color getBlackOutlineColor(){
    return (Color)props.getProperty("blackOutlineColor", null);
  }



  /**
   * Sets the black piece outline color to the specified value.
   */

  public void setBlackOutlineColor(Color color){
    props.setProperty("blackOutlineColor", color);
    configurePiecePainter();
  }



  /**
   * Returns the current light squares color.
   */

  public Color getLightSquareColor(){
    return (Color)props.getProperty("lightSquareColor", null);
  }



  /**
   * Sets the light squares color to the specified value.
   */

  public void setLightSquareColor(Color color){
    props.setProperty("lightSquareColor", color);
    configureBoardPainter();
  }



  /**
   * Returns the current dark squares color.
   */

  public Color getDarkSquareColor(){
    return (Color)props.getProperty("darkSquareColor", null);
  }



  /**
   * Sets the dark squares color to the specified value.
   */

  public void setDarkSquareColor(Color color){
    props.setProperty("darkSquareColor", color);
    configureBoardPainter();
  }



  /**
   * Returns the current move input style. Possible values are defined in
   * <code>free.chess.JBoard</code>.
   */

  public int getMoveInputStyle(){
    return props.getIntegerProperty("moveInputStyle");
  }




  /**
   * Sets the move input style to the specified value.
   *
   * @param moveInputStyle The new move input style. Possible values are defined
   * in <code>free.chess.JBoard</code>.
   */

  public void setMoveInputStyle(int moveInputStyle){
    switch (moveInputStyle){
      case JBoard.CLICK_N_CLICK:
      case JBoard.DRAG_N_DROP:
        break;
      default:
        throw new IllegalArgumentException("Unknown move input style: "+moveInputStyle);
    }

    props.setIntegerProperty("moveInputStyle", moveInputStyle);
  }



  /**
   * Returns whether the moved piece follows the mouse cursor.
   */
   
  public boolean isPieceFollowsCursor(){
    return props.getBooleanProperty("pieceFollowsCursor");
  }
  
  
  
  /**
   * Sets whether the moved piece follows the mouse cursor.
   */
   
  public void setPieceFollowsCursor(boolean pieceFollowsCursor){
    props.setBooleanProperty("pieceFollowsCursor", pieceFollowsCursor);
  }



  /**
   * Returns whether the currently made move is highlighted by highlighting the
   * origin and target squares. This refers to the move that is currently being
   * made by the user (that is, in the middle of the move-making process), as
   * opposed to the last move highlighting, which is specified by the
   * <code>setMoveHighlightingStyle</code> method.
   */
   
  public boolean isHighlightMadeMoveSquares(){
    return props.getBooleanProperty("highlightMadeMoveSquares");
  }
  
  
  
  /**
   * Sets whether the currently made move is highlighted.
   */
   
  public void setHighlightMadeMoveSquares(boolean highlight){
    props.setBooleanProperty("highlightMadeMoveSquares", highlight);
  }


  

  /**
   * Returns the color used for highlighting in
   * <code>highlightMadeMoveSquares</code> mode.
   */

  public Color getMadeMoveSquaresHighlightColor(){
    return (Color)props.getProperty("madeMoveSquaresHighlightColor");
  }



  /**
   * Sets the color used for highlighting in
   * <code>highlightMadeMoveSquares</code> mode.
   */

  public void setMadeMoveSquaresHighlightColor(Color color){
    props.setProperty("madeMoveSquaresHighlightColor", color);
  }


  

  /**
   * Returns <code>true</code> if promotable pieces are automatically promoted
   * to the default promotion piece. Otherwise, returns <code>false</code>
   */

  public boolean isAutoPromote(){
    return props.getBooleanProperty("autoPromote");
  }




  /**
   * Sets whether promotable pieces are promoted automatically to the default
   * promotion piece.
   *
   * @param autoPromote <code>true</code> if promotion should be automatic.
   */

  public void setAutoPromote(boolean isAutoPromote){
    props.setBooleanProperty("autoPromote", isAutoPromote);
  }




  /**
   * Returns the current move highlighting style. Posssible values are defined
   * in <code>free.chess.JBoard</code>.
   */

  public int getMoveHighlightingStyle(){
    return props.getIntegerProperty("moveHighlightingStyle");
  }




  /**
   * Sets the current move highlighting style to the specified value.
   * 
   * @param moveHighlightingStyle The new move highlighting style. Possible
   * values are defined in <code>free.chess.JBoard</code>.
   */

  public void setMoveHighlightingStyle(int moveHighlightingStyle){
    switch (moveHighlightingStyle){
      case JBoard.NO_MOVE_HIGHLIGHTING:
      case JBoard.TARGET_SQUARE_MOVE_HIGHLIGHTING:
      case JBoard.BOTH_SQUARES_MOVE_HIGHLIGHTING:
      case JBoard.ARROW_MOVE_HIGHLIGHTING:
        break;
      default:
        throw new IllegalStateException("Unknown move higlighting style: "+moveHighlightingStyle);
    }

    props.setIntegerProperty("moveHighlightingStyle", moveHighlightingStyle);
  }





  /**
   * Returns <code>true</code> if own moves are highlighted as well as the
   * opponent's moves. Returns <code>false</code> if only the opponent's moves
   * are highlighted (if move highlighting is enabled of course). 
   */

  public boolean isHighlightingOwnMoves(){
    return props.getBooleanProperty("highlightingOwnMoves");
  }




  /**
   * Sets whether own moves will be highlighted.
   *
   * @param isHighlightingOwnMoves <code>true</code> if own moves should be
   * highlighted as well as the opponent's moves. <code>false</code> if only
   * the opponent's moves should be highlighted.
   */

  public void setHighlightingOwnMoves(boolean isHighlightingOwnMoves){
    props.setBooleanProperty("highlightingOwnMoves", isHighlightingOwnMoves);
  }




  /**
   * Returns the current move highlighting color.
   */

  public Color getMoveHighlightingColor(){
    return (Color)props.getProperty("moveHighlightingColor");
  }




  /**
   * Sets the current move highlighting color to the specified value.
   *
   * @param moveHighlightingColor The new move highlighting color.
   */

  public void setMoveHighlightingColor(Color moveHighlightingColor){
    props.setProperty("moveHighlightingColor", moveHighlightingColor);
  }



  
  /**
   * Returns the current move sending mode.
   */

  public int getMoveSendingMode(){
    return props.getIntegerProperty("moveSendingMode");
  }




  /**
   * Sets the move sending mode to the specified value. Possible values are
   * <UL>
   *   <LI><code>LEGAL_CHESS_MOVE_SENDING_MODE</code>
   *   <LI><code>PREDRAG_MOVE_SENDING_MODE</code>
   *   <LI><code>PREMOVE_MOVE_SENDING_MODE</code>
   * </UL>
   */

  public void setMoveSendingMode(int moveSendingMode){
    switch(moveSendingMode){
      case LEGAL_CHESS_MOVE_SENDING_MODE:
      case PREDRAG_MOVE_SENDING_MODE:
      case PREMOVE_MOVE_SENDING_MODE:
        break;
      default:
        throw new IllegalArgumentException("Unrecognized value for move sending mode: " +
          moveSendingMode);
    }

    props.setIntegerProperty("moveSendingMode", moveSendingMode);
  }
  
  
  
  /**
   * Returns the current coordinate display style.  Possible values are defined
   * in <code>free.chess.JBoard</code>.
   */
   
  public int getCoordsDisplayStyle(){
    return props.getIntegerProperty("coordsDisplayStyle");
  }
  
  
  
  
  /**
   * Sets the coordinate display style to the specified value. Possible values
   * are defined in <code>free.chess.JBoard</code>.
   */
   
  public void setCoordsDisplayStyle(int coordsDisplayStyle){
    switch (coordsDisplayStyle){
      case JBoard.NO_COORDS:
      case JBoard.RIM_COORDS:
      case JBoard.OUTSIDE_COORDS:
      case JBoard.EVERY_SQUARE_COORDS:
        break;
      default:
        throw new IllegalArgumentException("Unrecognized coordinates display style value: " + coordsDisplayStyle);
    }
    
    props.setIntegerProperty("coordsDisplayStyle", coordsDisplayStyle);
  }


  
  /**
   * Returns the color used for coordinates display.
   */
   
  public Color getCoordsDisplayColor(){
    return (Color)props.getProperty("coordsDisplayColor");
  }
  
  
  
  /**
   * Sets the color used for coordinates display to the specified value.
   */
   
  public void setCoordsDisplayColor(Color color){
    props.setProperty("coordsDisplayColor", color);
  }
  
  

  /**
   * Returns the current piece set.
   */

  public PieceSet getPieceSet(){
    return (PieceSet)props.getProperty("pieceSet", null);
  }
  
  
  
  /**
   * Returns the current piece painter. The returned value should not be
   * modified.
   */
   
  public PiecePainter getPiecePainter(){
    return piecePainter;
  }



  /**
   * Returns the current board pattern.
   */

  public BoardPattern getBoardPattern(){
    return (BoardPattern)props.getProperty("boardPattern", null);
  }
  
  
  
  /**
   * Returns the current board painter. The returned value should not be
   * modified.
   */
   
   public BoardPainter getBoardPainter(){
     return boardPainter;
   }



  /**
   * Sets the piece set to the specified one. If the specified piece set is
   * <code>null</code>, the default piece set is used.
   */
   
  public void setPieceSet(PieceSet set){
    PieceSet pieceSet = set == null ? PieceSet.DEFAULT_PIECE_SET : set;
    piecePainter = pieceSet.getPiecePainter();
    configurePiecePainter();
    
    props.setProperty("pieceSet", pieceSet);    
  }



  /**
   * Sets the board pattern to the specified one. If the specified pattern is
   * <code>null</code>, the default one is used.
   */

  public void setBoardPattern(BoardPattern pattern){
    BoardPattern boardPattern = pattern == null ? BoardPattern.DEFAULT_BOARD_PATTERN : pattern;
    boardPainter = boardPattern.getBoardPainter();
    configureBoardPainter();
    
    props.setProperty("boardPattern", boardPattern);
  }
  
  

  /**
   * Registers all the necessary listeners with the Connection.
   */

  protected void registerConnListeners(){
    ListenerManager listenerManager = getConn().getListenerManager();

    listenerManager.addGameListener(this);
    listenerManager.addConnectionListener(this);
  }
  
  
  
  /**
   * Preloads certain things. This method is called once when the plugin starts.
   */
   
  private void preload(){
    createBoardPanel(new Game(Game.MY_GAME, new Position(), 0, "AlexTheGreat", "Kasparov", 5*60*1000, 2000,
        5*60*1000, 2000, 1800, 2852, "blah", "Blitz", true, true, "C", "GM", false, Player.WHITE_PLAYER));
  }



  /**
   * Unregisters all the necessary listeners from the Connection. 
   */

  protected void unregisterConnListeners(){
    ListenerManager listenerManager = getConn().getListenerManager();

    listenerManager.removeGameListener(this);
    listenerManager.removeConnectionListener(this);
  } 



  /**
   * Returns the BoardPanel displaying the given Game.
   */

  public BoardPanel getBoardPanel(Game game){
    return (BoardPanel)gamesToBoardPanels.get(game);
  }




  /**
   * Gets called when a game starts.
   */

  public void gameStarted(GameStartEvent evt){
    createNewBoardPanel(evt.getGame());
  }
  
  
  
  /**
   * Creates a new BoardPanel for the specified game, a container for it and
   * displays them.
   */
  
  protected void createNewBoardPanel(Game game){
    BoardPanel boardPanel = createBoardPanel(game);
    initBoardPanel(game, boardPanel);

    game.addPropertyChangeListener(new PropertyChangeListener(){
      public void propertyChange(PropertyChangeEvent evt){
        Game game = (Game)evt.getSource();
        BoardPanel boardPanel = (BoardPanel)gamesToBoardPanels.get(game);        
        
        if (boardPanel != null){
          PluginUIContainer boardContainer =
            (PluginUIContainer)boardPanelsToContainers.get(boardPanel);

          if (boardContainer != null) // It could be null if the container has been closed
            boardContainer.setTitle(getBoardTitle(boardPanel));
        }
      }
    });
  }



  /**
   * Obtains and returns a PluginUIContainer to be used for a board.
   * An unused container is looked for, and if none is found, a new one is
   * created.
   */

  private PluginUIContainer createBoardContainer(){
    for (int i = 0; i < containers.size(); i++){
      PluginUIContainer container = (PluginUIContainer)containers.elementAt(i);
      BoardPanel panel = (BoardPanel)containersToBoardPanels.get(container);
      if ((panel == null) || !panel.isActive())
        return recycleContainer(container);
    }

    return createNewBoardContainer();
  }




  /**
   * Creates and configures a new PluginUIContainer for use with a board.
   */

  private PluginUIContainer createNewBoardContainer(){
    PluginUIContainer boardContainer = 
      createContainer(String.valueOf(containers.size()), UIProvider.SELF_MANAGED_CONTAINER_MODE);

    containers.addElement(boardContainer);

    URL iconImageURL = BoardManager.class.getResource("board.gif");
    if (iconImageURL!= null)
      boardContainer.setIcon(Toolkit.getDefaultToolkit().getImage(iconImageURL));

    boardContainer.addPluginUIListener(this);

    return boardContainer;
  }





  /**
   * Recycles the specified container, making it ready to be used again.
   */

  private PluginUIContainer recycleContainer(PluginUIContainer container){
    BoardPanel oldBoardPanel = (BoardPanel)containersToBoardPanels.remove(container);
    if (oldBoardPanel != null){
      oldBoardPanel.done();
      boardPanelsToContainers.remove(oldBoardPanel);
    }

    Container contentPane = container.getContentPane();
    contentPane.removeAll(); // wash it

    return container;
  }

  


  /**
   * Creates and configures a new BoardPanel for the given Game.
   */

  protected BoardPanel createBoardPanel(Game game){
    BoardPanel boardPanel = new BoardPanel(this, game);

    return boardPanel;
  }



  
  /**
   * Initializes the given BoardPanel, making it ready for use.
   */

  protected void initBoardPanel(Game game, BoardPanel boardPanel){
    boardPanel.addUserMoveListener(this);
    getConn().getListenerManager().addGameListener(boardPanel);
    gamesToBoardPanels.put(game, boardPanel);

    PluginUIContainer boardContainer = createBoardContainer();
    Container content = boardContainer.getContentPane();
    content.setLayout(new BorderLayout());
    content.add(boardPanel, BorderLayout.CENTER);
    content.invalidate(); // Better wash this container - no 
    content.validate();   // knowing what used to be in it ;-)

    boardContainer.setTitle(getBoardTitle(boardPanel));

    containersToBoardPanels.put(boardContainer, boardPanel);
    boardPanelsToContainers.put(boardPanel, boardContainer);

    boardContainer.setActive(true);
  }



  /*
   * GameListener implementation.
   */

  public void moveMade(MoveMadeEvent evt){}
  public void positionChanged(PositionChangedEvent evt){}
  public void takebackOccurred(TakebackEvent evt){}
  public void illegalMoveAttempted(IllegalMoveEvent evt){}
  public void clockAdjusted(ClockAdjustmentEvent evt){}
  public void boardFlipped(BoardFlipEvent evt){}
  public void offerUpdated(OfferEvent evt){}




  /**
   * Cleanup the game.
   */

  public void gameEnded(GameEndEvent evt){
    gameEndCleanup(evt.getGame());
  }
  
  
  
  /**
   * ConnectionListener implementation.
   */
  
  public void connectingFailed(Connection conn, String reason){}
  public void connectionAttempted(Connection conn, String hostname, int port){}
  public void connectionEstablished(Connection conn){}
  public void loginFailed(Connection conn, String reason){}
  public void loginSucceeded(Connection conn){}
  
  
  
  /**
   * Cleanup all current games, otherwise the BoardPanels think everything is
   * dandy, and so may the user.   
   */
  
  public void connectionLost(Connection conn){
    // We need to copy them to a Vector because you can't use an Enumeration
    // while modifying its HashTable, and gameEndCleanup modifies the HashTable
    Vector games = new Vector();
    Enumeration gamesEnum = gamesToBoardPanels.keys();
    while (gamesEnum.hasMoreElements())
      games.addElement(gamesEnum.nextElement());
    
    for (int i = 0; i < games.size(); i++)
      gameEndCleanup((Game)games.elementAt(i));
  }

  
    
  /**
   * Performs cleanup when we stop looking at the specified game.
   */
  
  protected void gameEndCleanup(Game game){
    BoardPanel boardPanel = (BoardPanel)gamesToBoardPanels.remove(game);
    if (boardPanel != null){
      getConn().getListenerManager().removeGameListener(boardPanel);
      boardPanel.removeUserMoveListener(this);
      boardPanel.setInactive();
      
      PluginUIContainer boardContainer =
        (PluginUIContainer)boardPanelsToContainers.get(boardPanel);

      if (boardContainer != null) // It could be null if the container has been closed
        boardContainer.setTitle(getBoardTitle(boardPanel));
    }
  }
  
  
  
  
  /**
   * Returns the ui container title used for the specified board panel.
   */
  
  protected String getBoardTitle(BoardPanel boardPanel){
    Game game = boardPanel.getGame();
    I18n i18n = getI18n();
    Object [] titleArgs = new Object[]{game};

    if (boardPanel.isActive()){
      if (game.getGameType() == Game.MY_GAME){
        if (game.isPlayed())
          return i18n.getFormattedString("playingBoardTitle", titleArgs);
        else
          return i18n.getFormattedString("examiningBoardTitle", titleArgs);
      }
      else
        return i18n.getFormattedString("observingBoardTitle", titleArgs);
    }
    else
      return i18n.getFormattedString("inactiveBoardTitle", titleArgs);
  }
  
  
  
  
  /**
   * Returns whether the user is currently playing a game.
   */
   
  public boolean isUserPlaying(){
    Enumeration games = gamesToBoardPanels.keys();
    while (games.hasMoreElements()){
      Game game = (Game)games.nextElement();
      if ((game.getGameType() == Game.MY_GAME) && game.isPlayed())
        return true;
    }
    
    return false;
  }



  /**
   * Gets called when the user makes a move on the board.
   */

  public void userMadeMove(UserMoveEvent evt){
    Object src = evt.getSource();

    if (src instanceof BoardPanel){
      BoardPanel boardPanel = (BoardPanel)src;
      Game game = boardPanel.getGame();
      getConn().makeMove(game, evt.getMove());
    }
  }



  /**
   * PluginUIListener implementation. Handles proper closing of the frame.
   */

  public void pluginUIClosing(PluginUIEvent evt){
    PluginUIContainer boardContainer = evt.getPluginUIContainer();
    BoardPanel boardPanel = (BoardPanel)containersToBoardPanels.get(boardContainer);

    if (boardPanel.isActive()){ // otherwise, the user is just closing a "dead" frame.
      Game game = boardPanel.getGame();

      Object result = OptionPanel.OK;
      if (game.getGameType() == Game.MY_GAME)
        result = getI18n().confirm(OptionPanel.OK, game.isPlayed() ? "resignDialog" : "unexamineDialog", boardPanel);

      if (result == OptionPanel.OK){
        getConn().quitGame(game);
        boardContainer.setVisible(false);
      }
    }
    else
      boardContainer.setVisible(false);
  }



  /**
   * Performs any necessary cleanup.
   */

  public void pluginUIHidden(PluginUIEvent evt){
    PluginUIContainer boardContainer = evt.getPluginUIContainer();
    BoardPanel boardPanel = (BoardPanel)containersToBoardPanels.remove(boardContainer);
    boardPanelsToContainers.remove(boardPanel);

    boardPanel.done();  
  }



  /**
   * PluginUIListener implementation.
   */

  public void pluginUIShown(PluginUIEvent evt){}
  public void pluginUIActivated(PluginUIEvent evt){}
  public void pluginUIDeactivated(PluginUIEvent evt){}
  public void pluginUIDisposed(PluginUIEvent evt){}
  public void pluginUITitleChanged(PluginUIEvent evt){}
  public void pluginUIIconChanged(PluginUIEvent evt){}
  



  /**
   * Overrides <code>Plugin.hasPreferencesUI()</code> to return
   * <code>true</code>.
   */
  
  public boolean hasPreferencesUI(){
    return true;
  }




  /**
   * Creates and returns a PreferencesPanel which allows the user to modify
   * some of this plugin's settings.
   */

  public PreferencesPanel getPreferencesUI(){
    return new BoardPreferencesPanel(this);
  }




  /**
   * Saves the user preferences.
   */

  public void saveState(){
    Preferences prefs = getPrefs();

    prefs.setString("piece-set-id", getPieceSet().getId());    

    prefs.setString("board-pattern-id", getBoardPattern().getId());

    prefs.setBool("auto-promote", isAutoPromote());

    prefs.setString("move-input-style", getMoveInputStyle() == JBoard.CLICK_N_CLICK ?
      "click'n'click" : "drag'n'drop");

    prefs.setBool("piece-follows-cursor", isPieceFollowsCursor());
    
    prefs.setBool("highlight-made-move-squares", isHighlightMadeMoveSquares());
    prefs.setColor("highlight-made-move-squares.color",getMadeMoveSquaresHighlightColor());

    String moveHighlightingString;
    switch (getMoveHighlightingStyle()){
      case JBoard.NO_MOVE_HIGHLIGHTING: moveHighlightingString = "none"; break;
      case JBoard.TARGET_SQUARE_MOVE_HIGHLIGHTING: moveHighlightingString = "target-square"; break;
      case JBoard.BOTH_SQUARES_MOVE_HIGHLIGHTING: moveHighlightingString = "square"; break;
      case JBoard.ARROW_MOVE_HIGHLIGHTING: moveHighlightingString = "arrow"; break;
      default:
        throw new IllegalStateException("Unrecognized move highlighting style: " + getMoveHighlightingStyle()); 
    }
    prefs.setString("move-highlight.style", moveHighlightingString);
    
    String coordsDisplayStyleString;
    switch (getCoordsDisplayStyle()){
      case JBoard.NO_COORDS: coordsDisplayStyleString = "none"; break;
      case JBoard.RIM_COORDS: coordsDisplayStyleString = "rim"; break;
      case JBoard.OUTSIDE_COORDS: coordsDisplayStyleString = "outside"; break;
      case JBoard.EVERY_SQUARE_COORDS: coordsDisplayStyleString = "every-square"; break;
      default:
        throw new IllegalStateException("Unrecognized coords display style: " + getCoordsDisplayStyle());
    }
    prefs.setString("coords-display.style", coordsDisplayStyleString);
    prefs.setColor("coords-display.color", getCoordsDisplayColor());

    prefs.setBool("move-highlight.highlight-own", isHighlightingOwnMoves());

    prefs.setColor("move-highlight.color", getMoveHighlightingColor());

    int moveSendingMode = getMoveSendingMode();
    String moveSendingModeString;
    if (moveSendingMode == LEGAL_CHESS_MOVE_SENDING_MODE)
      moveSendingModeString = "legal-chess";
    else if (moveSendingMode == PREDRAG_MOVE_SENDING_MODE)
      moveSendingModeString = "predrag";
    else // if (moveSendingMode == PREMOVE_MOVE_SENDING_MODE)
      moveSendingModeString = "premove";
    prefs.setString("move-sending-mode", moveSendingModeString);

    prefs.setColor("white-piece-color", getWhitePieceColor());
    prefs.setColor("black-piece-color", getBlackPieceColor());
    prefs.setColor("white-outline-color", getWhiteOutlineColor());
    prefs.setColor("black-outline-color", getBlackOutlineColor());

    prefs.setColor("light-square-color", getLightSquareColor());
    prefs.setColor("dark-square-color", getDarkSquareColor());
  }




  /**
   * Returns the string "board".
   */

  public String getId(){
    return "board";
  }



  /**
   * Returns the plugin name.
   */

  public String getName(){
    return getI18n().getString("pluginName");
  }


  
}
