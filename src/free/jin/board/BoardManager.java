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

import free.jin.event.*;
import java.awt.*;
import javax.swing.*;
import free.chess.*;
import java.util.*;
import javax.swing.event.*;
import free.jin.plugin.*;
import free.jin.*;
import free.jin.board.event.UserMoveListener;
import free.jin.board.event.UserMoveEvent;
import free.jin.sound.SoundManager;
import free.util.BeanProperties;
import free.util.IOUtilities;
import free.util.TextUtilities;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.net.URL;
import java.io.IOException;


/**
 * The plugin responsible for displaying boards and handling all things related
 * to that.
 */

public class BoardManager extends Plugin implements GameListener, UserMoveListener,
    PluginUIListener{



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

  private SoundManager soundManager = null;



  /**
   * Maps piece set identifiers to <code>PieceSet</code> objects.
   */
   
  private final Hashtable pieceSets = new Hashtable();



  /**
   * The default piece set.
   */
   
  private PieceSet defaultPieceSet;

  

  /**
   * Maps board pattern identifiers to <code>BoardPattern</code> objects.
   */

  private final Hashtable boardPatterns = new Hashtable();



  /**
   * The default board pattern.
   */
   
  private BoardPattern defaultBoardPattern;
  
  
  
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
    loadPieceSets();
    loadBoardPatterns();
    initPreferences();
    registerConnListeners();
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
    soundManager = (SoundManager)getPlugin("sound");
  }



  /**
   * Returns the <code>SoundManager</code>, or <code>null</code> if none exists.
   */

  public SoundManager getSoundManager(){
    return soundManager;
  }



  /**
   * Loads the piece sets.
   */
   
  protected void loadPieceSets() throws PluginStartException{
    ClassLoader [] pieceSetLoaders = loadResources("pieces");

    try{
      String resourceName = 
        TextUtilities.translateResource(BoardManager.class, "defaultPieceSetDef"); 
      defaultPieceSet = new PieceSet(BoardManager.class.getClassLoader(), resourceName);
    } catch (IOException e){
        throw new PluginStartException(e, "Unable to load default piece set");
      }
      
    if ((pieceSetLoaders == null) || (pieceSetLoaders.length == 0)){
      pieceSets.put("default", defaultPieceSet);
      return;
    }
      
    for (int i = 0; i < pieceSetLoaders.length; i++){
      try{
        PieceSet pieceSet = new PieceSet(pieceSetLoaders[i], "definition");
        if (pieceSet.isCompatibleWith(getServer()))
          pieceSets.put(pieceSet.getId(), pieceSet);
      } catch (IOException e){
          System.err.println("I/O error while loading piece set");
          e.printStackTrace();
        }
    }
  }
  
  
  
  /**
   * Loads the board patterns.
   */
   
  protected void loadBoardPatterns() throws PluginStartException{
    ClassLoader [] boardLoaders = loadResources("boards");

    try{
      String resourceName = 
        TextUtilities.translateResource(BoardManager.class, "defaultBoardPatternDef"); 
      defaultBoardPattern = new BoardPattern(BoardManager.class.getClassLoader(), resourceName);
    } catch (IOException e){
        throw new PluginStartException(e, "Unable to load default board pattern");
      }
      
    if ((boardLoaders == null) || (boardLoaders.length == 0)){
      boardPatterns.put("default", defaultBoardPattern);
      return;
    }
      
    for (int i = 0; i < boardLoaders.length; i++){
      try{
        BoardPattern boardPattern = new BoardPattern(boardLoaders[i], "definition");
        if (boardPattern.isCompatibleWith(getServer()))
          boardPatterns.put(boardPattern.getId(), boardPattern);
      } catch (IOException e){
          System.err.println("I/O error while loading board pattern");
          e.printStackTrace();
        }
    }
  }



  /**
   * Initializes various preferences from the user's properties.
   */

  protected void initPreferences(){
    Preferences prefs = getPrefs();

    setAutoPromote(prefs.getBool("auto-promote", false));

    setMoveInputStyle("click'n'click".equals(prefs.getString("move-input-style", null)) ? 
      JBoard.CLICK_N_CLICK : JBoard.DRAG_N_DROP);

    setDraggedPieceStyle("target-cursor".equals(prefs.getString("dragged-piece-style", null)) ?
      JBoard.HIGHLIGHT_TARGET_DRAGGED_PIECE : JBoard.NORMAL_DRAGGED_PIECE);

    String moveHighlightingStyleString = prefs.getString("move-highlight.style", "square");
    if ("target-square".equals(moveHighlightingStyleString))
      setMoveHighlightingStyle(JBoard.TARGET_SQUARE_MOVE_HIGHLIGHTING);
    if ("square".equals(moveHighlightingStyleString))
      setMoveHighlightingStyle(JBoard.BOTH_SQUARES_MOVE_HIGHLIGHTING);
    else if ("arrow".equals(moveHighlightingStyleString))
      setMoveHighlightingStyle(JBoard.ARROW_MOVE_HIGHLIGHTING);
    else
      setMoveHighlightingStyle(JBoard.NO_MOVE_HIGHLIGHTING);

    setHighlightingOwnMoves(prefs.getBool("move-highlight.highlight-own", false));

    setMoveHighlightingColor(prefs.getColor("move-highlight.color", Color.red));

    setDragSquareHighlightingColor(
      prefs.getColor("drag-square-highlighting.color", new Color(0x0000ff)));

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

    setPieceSet(prefs.getString("piece-set-id", null));
    setBoardPattern(prefs.getString("board-pattern-id", null));
    
    setWhitePieceColor(prefs.getColor("white-piece-color", Color.white));
    setBlackPieceColor(prefs.getColor("black-piece-color", Color.black));
    setWhiteOutlineColor(prefs.getColor("white-outline-color", Color.black));
    setBlackOutlineColor(prefs.getColor("black-outline-color", Color.white));

    setLightSquareColor(prefs.getColor("light-square-color", Color.cyan));
    setDarkSquareColor(prefs.getColor("dark-square-color", Color.magenta));
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
   * Returns the current dragged piece style. Possible values defined in
   * <code>free.chess.JBoard</code>.
   */

  public int getDraggedPieceStyle(){
    return props.getIntegerProperty("draggedPieceStyle");
  }




  /**
   * Sets the dragged piece style to the specified value.
   *
   * @param draggedPieceStyle The new dragged piece style. Possible values are
   * defined in <code>free.chess.JBoard</code>.
   */

  public void setDraggedPieceStyle(int draggedPieceStyle){
    switch (draggedPieceStyle){
      case JBoard.HIGHLIGHT_TARGET_DRAGGED_PIECE:
      case JBoard.NORMAL_DRAGGED_PIECE:
        break;
      default:
        throw new IllegalArgumentException("Unknown dragged piece style: "+draggedPieceStyle);
    }

    props.setIntegerProperty("draggedPieceStyle", draggedPieceStyle);
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
   * Returns the current drag square highlighting color.
   */

  public Color getDragSquareHighlightingColor(){
    return (Color)props.getProperty("dragSquareHighlightingColor");
  }




  /**
   * Sets the drag square highlighting color to the specified value.
   *
   * @param dragSquareHighlightingColor The new drag square highlighting color.
   */

  public void setDragSquareHighlightingColor(Color dragSquareHighlightingColor){
    props.setProperty("dragSquareHighlightingColor", dragSquareHighlightingColor);
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
    PieceSet pieceSet = set == null ? defaultPieceSet : set;
    piecePainter = pieceSet.createPiecePainter();
    configurePiecePainter();
    
    props.setProperty("pieceSet", pieceSet);    
  }



  /**
   * Sets the board pattern to the specified one. If the specified pattern is
   * <code>null</code>, the default one is used.
   */

  public void setBoardPattern(BoardPattern pattern){
    BoardPattern boardPattern = pattern == null ? defaultBoardPattern : pattern;
    boardPainter = boardPattern.createBoardPainter();
    configureBoardPainter();
    
    props.setProperty("boardPattern", boardPattern);
  }
  
  
  
  /**
   * Sets the current piece set to be of the specified type.
   *
   * @param id The identifier of the piece set.
   */

  public void setPieceSet(String id){
    PieceSet pieceSet = (id == null ? null : (PieceSet)pieceSets.get(id));
    setPieceSet(pieceSet);
  }
  
  
  
  /**
   * Sets the current board pattern to be of the specified type.
   *
   * @param id The identifier of the board pattern.   
   */

  public void setBoardPattern(String id){
    BoardPattern boardPattern = (id == null ? null : (BoardPattern)boardPatterns.get(id));
    setBoardPattern(boardPattern);
  }
  


  /**
   * Registers all the necessary listeners with the Connection.
   */

  protected void registerConnListeners(){
    ListenerManager listenerManager = getConn().getListenerManager();

    listenerManager.addGameListener(this);
  }



  /**
   * Returns a list of available piece sets.
   */
   
  public PieceSet [] getAvailablePieceSets(){
    // Put the piece sets in a vector ordered alphabetically by name
    Enumeration pieceSetIds = pieceSets.keys();
    Vector pieceSetsVec = new Vector(pieceSets.size());
    while (pieceSetIds.hasMoreElements()){
      Object id = pieceSetIds.nextElement();
      PieceSet pieceSet = (PieceSet)pieceSets.get(id);
      int i = 0;
      while (i < pieceSetsVec.size()){
        PieceSet cur = (PieceSet)pieceSetsVec.elementAt(i);
        if (pieceSet.getName().compareTo(cur.getName()) < 0)
          break;
        i++;
      }
      pieceSetsVec.insertElementAt(pieceSet, i);
    }
    
    PieceSet [] pieceSets = new PieceSet[pieceSetsVec.size()];
    pieceSetsVec.copyInto(pieceSets);
    
    return pieceSets;
  }
  
  
  
  /**
   * Returns a list of available board patterns.
   */
   
  public BoardPattern [] getAvailableBoardPatterns(){
    // Put the board patterns in a vector ordered alphabetically by name
    Enumeration boardPatternIds = boardPatterns.keys();
    Vector boardPatternsVec = new Vector(boardPatterns.size());
    while (boardPatternIds.hasMoreElements()){
      Object id = boardPatternIds.nextElement();
      BoardPattern boardPattern = (BoardPattern)boardPatterns.get(id);
      int i = 0;
      while (i < boardPatternsVec.size()){
        BoardPattern cur = (BoardPattern)boardPatternsVec.elementAt(i);
        if (boardPattern.getName().compareTo(cur.getName()) < 0)
          break;
        i++;
      }
      boardPatternsVec.insertElementAt(boardPattern, i);
    }
    
    BoardPattern [] boardPatterns = new BoardPattern[boardPatternsVec.size()];
    boardPatternsVec.copyInto(boardPatterns);
    
    return boardPatterns;
  }
  



  /**
   * Unregisters all the necessary listeners from the Connection. 
   */

  protected void unregisterConnListeners(){
    ListenerManager listenerManager = getConn().getListenerManager();

    listenerManager.removeGameListener(this);
  } 



  /**
   * Returns the BoardPanel displaying the given Game.
   */

  public BoardPanel getBoardPanel(Game game){
    return (BoardPanel)gamesToBoardPanels.get(game);
  }




  /**
   * Gets called when a game starts. Creates a new BoardPanel and a container
   * for it and displays it.
   */

  public void gameStarted(GameStartEvent evt){
    Game game = evt.getGame();
    BoardPanel boardPanel = createBoardPanel(game);
    initBoardPanel(game, boardPanel);

    // Artificially invoke this, since it would not get called otherwise
    // (we're already in the middle of the dispatching of that event).
    boardPanel.gameStarted(evt);
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
    PluginUIContainer boardContainer = createContainer(String.valueOf(containers.size()));

    containers.addElement(boardContainer);

    URL iconImageURL = BoardManager.class.getResource("board.gif");
    if (iconImageURL!= null)
      boardContainer.setIcon(Toolkit.getDefaultToolkit().getImage(iconImageURL));

    boardContainer.setCloseOperation(PluginUIContainer.DO_NOTHING_ON_CLOSE);
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

    boardContainer.setTitle(boardPanel.getTitle());

    containersToBoardPanels.put(boardContainer, boardPanel);
    boardPanelsToContainers.put(boardPanel, boardContainer);

    boardContainer.setActive(true);
  }



  /**
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
   * Gets called when a game ends. Notifies the BoardPanel displaying the board
   * marking it unused.
   */

  public void gameEnded(GameEndEvent evt){
    BoardPanel boardPanel = (BoardPanel)gamesToBoardPanels.remove(evt.getGame());
    if (boardPanel != null){
      getConn().getListenerManager().removeGameListener(boardPanel);
      boardPanel.removeUserMoveListener(this);
      boardPanel.setInactive();

      PluginUIContainer boardContainer =
        (PluginUIContainer)boardPanelsToContainers.get(boardPanel);

      if (boardContainer != null) // It could be null if the container has been closed
        boardContainer.setTitle(boardPanel.getTitle());
    }
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

      boolean shouldAsk = false;
      String question = null;

      if (game.getGameType() == Game.MY_GAME){
        shouldAsk = true;
        if (game.isPlayed())
          question = "RESIGN this game?";
        else
          question = "Stop examining this game?";
      }

      Object result = OptionPanel.OK;
      if (shouldAsk)
        result = OptionPanel.confirm(getUIProvider(), "Select an option", question, OptionPanel.OK);

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

    prefs.setString("dragged-piece-style",
      getDraggedPieceStyle() == JBoard.HIGHLIGHT_TARGET_DRAGGED_PIECE ? "target-cursor" : "normal");

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

    prefs.setColor("drag-square-highlighting.color",getDragSquareHighlightingColor());

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
   * Returns "Chess Board".
   */

  public String getName(){
    return "Chess Board";
  }
  
  
  
  /**
   * Encapsulates a piece set.
   */
   
  public static class PieceSet{
    

    
    /**
     * The definition properties of this piece set.
     */
     
    private final Properties definition;
    
    
    
    /**
     * The class loader of this piece set.
     */
     
    private final ClassLoader loader;
    
    
    
    /**
     * Loads a new <code>PieceSet</code> with the specified
     * <code>ClassLoader</code>. The definition resource name is also specified. 
     */
     
    public PieceSet(ClassLoader loader, String defResourceName) throws IOException{
      this.loader = loader;
      this.definition = IOUtilities.loadProperties(loader.getResourceAsStream(defResourceName));
      
      if (definition == null)
        throw new IOException("Unable to load piece set definition from: " + defResourceName);
    }
    
    
    
    /**
     * Returns the identifier of this piece set.
     */
     
    public String getId(){
      return definition.getProperty("id");
    }
    
    
    
    /**
     * Returns the name of this piece set.
     */
     
    public String getName(){
      return definition.getProperty("name");
    }
    
    
    
    /**
     * Returns whether this piece set can be used on the specified server.
     */
     
    public boolean isCompatibleWith(Server server){
      String allowedServers = definition.getProperty("servers");
      if (allowedServers == null)
        return true;
      
      String [] servers = TextUtilities.getTokens(allowedServers, " ");
      for (int i = 0; i < servers.length; i++){
        if (servers[i].equals(server.getId()))
          return true;
      }
      
      return false;
    }
    
    
    
    /**
     * Creates a PiecePainter for this piece set or null if unable to load it.
     */
     
    public PiecePainter createPiecePainter(){
      String className = definition.getProperty("classname");
      if (className == null){
        System.err.println("Unable to find classname property in definition of piece set: "
          + getId());
        return null;
      }

      try{
        return (PiecePainter)loader.loadClass(className).newInstance();
      } catch (ClassNotFoundException e){
          System.err.println("Unable to find class " + className);
        }
        catch (InstantiationException e){
          System.err.println("Unable to instantiate class " + className); 
        }
        catch (IllegalAccessException e){
          System.err.println("Unable to instantiate class " + className + " due to access restrictions."); 
        }
        catch (ClassCastException e){
          System.err.println("Unable to cast " + className + " into PiecePainter"); 
        }
        
      return null;
    }
    

    
    /**
     * Returns the name of the piece set.
     */
     
    public String toString(){
      return getName();
    }
    
    
    
  }
  
  
  
  /**
   * Encapsulates a board pattern.
   */
   
  public static class BoardPattern{

    

    /**
     * The definition properties of this board pattern.
     */
     
    private final Properties definition;
    
    
    
    /**
     * The class loader of this board pattern.
     */
     
    private final ClassLoader loader;
    
    
    
    
    /**
     * Loads a new <code>BoardPattern</code> with the specified
     * <code>ClassLoader</code>. The definition resource name is also specified. 
     */
     
    public BoardPattern(ClassLoader loader, String defResourceName) throws IOException{
      this.loader = loader;
      this.definition = IOUtilities.loadProperties(loader.getResourceAsStream(defResourceName));
      
      if (definition == null)
        throw new IOException("Unable to load board pattern definition from: " + defResourceName);
    }
    
    
    
    /**
     * Returns the identifier of this board pattern.
     */
     
    public String getId(){
      return definition.getProperty("id");
    }
    
    
    
    /**
     * Returns the name of this board pattern.
     */
     
    public String getName(){
      return definition.getProperty("name");
    }
    
    
    
    /**
     * Returns whether this piece set can be used on the specified server.
     */
     
    public boolean isCompatibleWith(Server server){
      String allowedServers = definition.getProperty("servers");
      if (allowedServers == null)
        return true;
      
      String [] servers = TextUtilities.getTokens(allowedServers, " ");
      for (int i = 0; i < servers.length; i++){
        if (servers[i].equals(server.getId()))
          return true;
      }
      
      return false;
    }



    /**
     * Creates a new BoardPainter for this piece set or null if unable to load it.
     */
     
    public BoardPainter createBoardPainter(){
      String className = definition.getProperty("classname");
      if (className == null){
        System.err.println("Unable to find classname property in definition of piece set: "
          + getId());
        return null;
      }

      try{
        return (BoardPainter)loader.loadClass(className).newInstance();
      } catch (ClassNotFoundException e){
          System.err.println("Unable to find class " + className);
        }
        catch (InstantiationException e){
          System.err.println("Unable to instantiate class " + className); 
        }
        catch (IllegalAccessException e){
          System.err.println("Unable to instantiate class " + className + " due to access restrictions."); 
        }
        catch (ClassCastException e){
          System.err.println("Unable to cast " + className + " into BoardPainter"); 
        }
        
      return null;
    }
    
    
    
    /**
     * Returns the name of the board pattern.
     */
     
    public String toString(){
      return getName();
    }


    
  }
  


}
