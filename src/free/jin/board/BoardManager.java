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
   * The radio button that sets the move sending mode to premove. We need an
   * instance reference to it because subclasses (such as the freechess.org one)
   * may want to disable it during games.
   */

  protected JRadioButtonMenuItem premoveRadioButton;




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
      JBoard.CROSSHAIR_DRAGGED_PIECE : JBoard.NORMAL_DRAGGED_PIECE);

    String moveHighlightingStyleString = prefs.getString("move-highlight.style", "square");
    if ("square".equals(moveHighlightingStyleString))
      setMoveHighlightingStyle(JBoard.SQUARE_MOVE_HIGHLIGHTING);
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
   * Configures the given <code>PiecePainter</code> to prepare it for usage.
   */

  protected void configurePiecePainter(PiecePainter piecePainter){
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
   * Configures the given BoardPainter preparing it to be used.
   */

  protected void configureBoardPainter(BoardPainter boardPainter){
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
    configurePiecePainter(getPieceSet().getPiecePainter());
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
    configurePiecePainter(getPieceSet().getPiecePainter());
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
    configurePiecePainter(getPieceSet().getPiecePainter());
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
    configurePiecePainter(getPieceSet().getPiecePainter());
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
    configureBoardPainter(getBoardPattern().getBoardPainter());
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
    configureBoardPainter(getBoardPattern().getBoardPainter());
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
      case JBoard.CROSSHAIR_DRAGGED_PIECE:
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
      case JBoard.SQUARE_MOVE_HIGHLIGHTING:
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
   * Returns the current piece set.
   */

  public PieceSet getPieceSet(){
    return (PieceSet)props.getProperty("pieceSet", null);
  }



  /**
   * Returns the current board pattern.
   */

  public BoardPattern getBoardPattern(){
    return (BoardPattern)props.getProperty("boardPattern", null);
  }



  /**
   * Sets the current piece set to be of the specified type.
   *
   * @param id The identifier of the piece set.
   */

  public void setPieceSet(String id){
    Object pieceSet = (id == null ? null : pieceSets.get(id));
    props.setProperty("pieceSet", pieceSet == null ? defaultPieceSet : pieceSet);
  }




  /**
   * Sets the current board pattern to be of the specified type.
   */

  public void setBoardPattern(String id){
    Object boardPattern = (id == null ? null : boardPatterns.get(id));
    props.setProperty("boardPattern", boardPattern == null ? defaultBoardPattern : boardPattern);
  }



  /**
   * Registers all the necessary listeners with the Connection.
   */

  protected void registerConnListeners(){
    ListenerManager listenerManager = getConn().getListenerManager();

    listenerManager.addGameListener(this);
  }




  /**
   * Creates and returns the JMenu for this plugin.
   */

  public JMenu createPluginMenu(){
    JMenu myMenu = new JMenu(getName());
    
    myMenu.add(createMoveInputMenu());

    myMenu.add(createMoveHighlightingMenu());

    JMenu pieceSetsMenu = createPieceSetsMenu();
    if (pieceSetsMenu != null)
      myMenu.add(pieceSetsMenu);

    JMenu boardsMenu = createBoardsMenu();
    if (boardsMenu != null)
      myMenu.add(boardsMenu);

    return myMenu;
  }




  /**
   * Creates and returns the "Move Input" menu.
   */

  protected JMenu createMoveInputMenu(){
    JMenu moveInputMenu = new JMenu("Move Input");
    moveInputMenu.setMnemonic('M');

    int moveInputStyle = getMoveInputStyle();
    JRadioButtonMenuItem dragndropMenuItem = new JRadioButtonMenuItem("Drag'n'Drop", moveInputStyle == JBoard.DRAG_N_DROP);
    JRadioButtonMenuItem clicknclickMenuItem = new JRadioButtonMenuItem("Click'n'Click", moveInputStyle == JBoard.CLICK_N_CLICK);

    dragndropMenuItem.setMnemonic('D');
    clicknclickMenuItem.setMnemonic('C');

    ButtonGroup inputModeGroup = new ButtonGroup();
    inputModeGroup.add(dragndropMenuItem);
    inputModeGroup.add(clicknclickMenuItem);

    dragndropMenuItem.setActionCommand("dragndrop");
    clicknclickMenuItem.setActionCommand("clicknclick");

    ActionListener inputModeListener = new ActionListener(){
      public void actionPerformed(ActionEvent evt){
        String actionCommand = evt.getActionCommand();
        if ("dragndrop".equals(actionCommand))
          setMoveInputStyle(JBoard.DRAG_N_DROP);
        else if ("clicknclick".equals(actionCommand))
          setMoveInputStyle(JBoard.CLICK_N_CLICK);
        else
          throw new IllegalStateException("Unknown action command: "+actionCommand);
      }
    };

    dragndropMenuItem.addActionListener(inputModeListener);
    clicknclickMenuItem.addActionListener(inputModeListener);

    int draggedPieceStyle = getDraggedPieceStyle();
    JRadioButtonMenuItem normalDraggedPieceStyleMenuItem = new JRadioButtonMenuItem("Normal Dragged Piece",
      draggedPieceStyle == JBoard.NORMAL_DRAGGED_PIECE);
    JRadioButtonMenuItem targetDraggedPieceStyleMenuItem = new JRadioButtonMenuItem("Target Cursor",
      draggedPieceStyle == JBoard.CROSSHAIR_DRAGGED_PIECE);

    normalDraggedPieceStyleMenuItem.setMnemonic('N');
    targetDraggedPieceStyleMenuItem.setMnemonic('T');

    ButtonGroup draggedPieceStyleGroup = new ButtonGroup();
    draggedPieceStyleGroup.add(normalDraggedPieceStyleMenuItem);
    draggedPieceStyleGroup.add(targetDraggedPieceStyleMenuItem);

    normalDraggedPieceStyleMenuItem.setActionCommand("normal");
    targetDraggedPieceStyleMenuItem.setActionCommand("target");

    ActionListener draggedPieceStyleListener = new ActionListener(){
      public void actionPerformed(ActionEvent evt){
        String actionCommand = evt.getActionCommand();

        if ("normal".equals(actionCommand))
          setDraggedPieceStyle(JBoard.NORMAL_DRAGGED_PIECE);
        else if ("target".equals(actionCommand))
          setDraggedPieceStyle(JBoard.CROSSHAIR_DRAGGED_PIECE);
        else
          throw new IllegalStateException("Unknown action command: "+actionCommand);
      }
    };

    normalDraggedPieceStyleMenuItem.addActionListener(draggedPieceStyleListener);
    targetDraggedPieceStyleMenuItem.addActionListener(draggedPieceStyleListener);

    JRadioButtonMenuItem legalChess = new JRadioButtonMenuItem("Legal Chess");
    JRadioButtonMenuItem predrag = new JRadioButtonMenuItem("Predrag");
    premoveRadioButton = new JRadioButtonMenuItem("Premove");

    switch (getMoveSendingMode()){
      case LEGAL_CHESS_MOVE_SENDING_MODE: legalChess.setSelected(true); break;
      case PREDRAG_MOVE_SENDING_MODE: predrag.setSelected(true); break;
      case PREMOVE_MOVE_SENDING_MODE: premoveRadioButton.setSelected(true); break;
      default:
        throw new IllegalStateException("Unrecognized move sending mode: "+getMoveSendingMode());
    }

    legalChess.setMnemonic('l');
    predrag.setMnemonic('p');
    premoveRadioButton.setMnemonic('m');

    legalChess.setActionCommand("legalchess");
    predrag.setActionCommand("predrag");
    premoveRadioButton.setActionCommand("premove");

    ActionListener moveSendingModeActionListener = new ActionListener(){
      public void actionPerformed(ActionEvent evt){
        String actionCommand = evt.getActionCommand();

        if ("legalchess".equals(actionCommand))
          setMoveSendingMode(LEGAL_CHESS_MOVE_SENDING_MODE);
        else if ("predrag".equals(actionCommand))
          setMoveSendingMode(PREDRAG_MOVE_SENDING_MODE);
        else if ("premove".equals(actionCommand))
          setMoveSendingMode(PREMOVE_MOVE_SENDING_MODE);
        else
          throw new IllegalStateException("Unknown action command: "+actionCommand);
      }
    };

    legalChess.addActionListener(moveSendingModeActionListener);
    predrag.addActionListener(moveSendingModeActionListener);
    premoveRadioButton.addActionListener(moveSendingModeActionListener);

    ButtonGroup moveSendingModeButtonGroup = new ButtonGroup();
    moveSendingModeButtonGroup.add(legalChess);
    moveSendingModeButtonGroup.add(predrag);
    moveSendingModeButtonGroup.add(premoveRadioButton);

   
    final JCheckBoxMenuItem autoQueenMenuItem =
      new JCheckBoxMenuItem("Auto Queen", isAutoPromote());
    autoQueenMenuItem.setMnemonic('A');
    autoQueenMenuItem.addChangeListener(new ChangeListener(){
      public void stateChanged(ChangeEvent evt){
        setAutoPromote(autoQueenMenuItem.isSelected());
      }
    });

    moveInputMenu.add(dragndropMenuItem);
    moveInputMenu.add(clicknclickMenuItem);
    moveInputMenu.addSeparator();
    moveInputMenu.add(normalDraggedPieceStyleMenuItem);
    moveInputMenu.add(targetDraggedPieceStyleMenuItem);
    moveInputMenu.addSeparator();
    moveInputMenu.add(legalChess);
    moveInputMenu.add(predrag);
    moveInputMenu.add(premoveRadioButton);
    moveInputMenu.addSeparator();
    moveInputMenu.add(autoQueenMenuItem);

    return moveInputMenu;
  }




  /**
   * Creates and returns the "Move Highlighting" menu.
   */

  protected JMenu createMoveHighlightingMenu(){
    int moveHighlightingStyle = getMoveHighlightingStyle();
    JMenu moveHighlightingMenu =  new JMenu("Move Highlighting");
    moveHighlightingMenu.setMnemonic('H');
    
    JRadioButtonMenuItem noMoveHighlightingStyleMenuItem = new JRadioButtonMenuItem("None",
      moveHighlightingStyle == JBoard.NO_MOVE_HIGHLIGHTING);
    JRadioButtonMenuItem squareMoveHighlightingStyleMenuItem = new JRadioButtonMenuItem("Square",
      moveHighlightingStyle == JBoard.SQUARE_MOVE_HIGHLIGHTING);
    JRadioButtonMenuItem arrowMoveHighlightingStyleMenuItem = new JRadioButtonMenuItem("Arrow",
      moveHighlightingStyle == JBoard.ARROW_MOVE_HIGHLIGHTING);

    noMoveHighlightingStyleMenuItem.setMnemonic('N');
    squareMoveHighlightingStyleMenuItem.setMnemonic('S');
    arrowMoveHighlightingStyleMenuItem.setMnemonic('A');

    ButtonGroup moveHighlightStyleGroup = new ButtonGroup();
    moveHighlightStyleGroup.add(noMoveHighlightingStyleMenuItem);
    moveHighlightStyleGroup.add(squareMoveHighlightingStyleMenuItem);
    moveHighlightStyleGroup.add(arrowMoveHighlightingStyleMenuItem);

    noMoveHighlightingStyleMenuItem.setActionCommand("none");
    squareMoveHighlightingStyleMenuItem.setActionCommand("square");
    arrowMoveHighlightingStyleMenuItem.setActionCommand("arrow");

    ActionListener moveHighlightingStyleListener = new ActionListener(){
      public void actionPerformed(ActionEvent evt){
        String actionCommand = evt.getActionCommand();

        if ("none".equals(actionCommand))
          setMoveHighlightingStyle(JBoard.NO_MOVE_HIGHLIGHTING);
        else if ("square".equals(actionCommand))
          setMoveHighlightingStyle(JBoard.SQUARE_MOVE_HIGHLIGHTING);
        else if ("arrow".equals(actionCommand))
          setMoveHighlightingStyle(JBoard.ARROW_MOVE_HIGHLIGHTING);
        else
          throw new IllegalStateException("Unknown action command: "+actionCommand);
      }
    };

    noMoveHighlightingStyleMenuItem.addActionListener(moveHighlightingStyleListener);
    squareMoveHighlightingStyleMenuItem.addActionListener(moveHighlightingStyleListener);
    arrowMoveHighlightingStyleMenuItem.addActionListener(moveHighlightingStyleListener);


    boolean highlightOwnMoves = isHighlightingOwnMoves();
    final JCheckBoxMenuItem highlightOwnMovesCheckBox = new JCheckBoxMenuItem("Highlight Own Moves", highlightOwnMoves);
    highlightOwnMovesCheckBox.setMnemonic('H');
    highlightOwnMovesCheckBox.addChangeListener(new ChangeListener(){
      public void stateChanged(ChangeEvent evt){
        setHighlightingOwnMoves(highlightOwnMovesCheckBox.isSelected());
      }
    });
    
    moveHighlightingMenu.add(noMoveHighlightingStyleMenuItem);
    moveHighlightingMenu.add(squareMoveHighlightingStyleMenuItem);
    moveHighlightingMenu.add(arrowMoveHighlightingStyleMenuItem);
    moveHighlightingMenu.addSeparator();
    moveHighlightingMenu.add(highlightOwnMovesCheckBox);

    return moveHighlightingMenu;
  }




  /**
   * Creates and returns the "Piece Sets" menu. This may return
   * <code>null</code> so that no such menu is displayed. The default
   * implementation will return <code>null</code> if less than 2 piece sets are
   * specified in parameters.
   */

  protected JMenu createPieceSetsMenu(){
    if (pieceSets.size() < 2)
      return null;
    
    ActionListener pieceSetListener = new ActionListener(){

      public void actionPerformed(ActionEvent evt){
        AbstractButton button = (AbstractButton)evt.getSource();
        setPieceSet(button.getActionCommand());
      } 
    };
    
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

    JMenu pieceSetsMenu = new JMenu("Piece Sets");
    pieceSetsMenu.setMnemonic('P');
    String curPieceSetId = getPieceSet().getId();
    ButtonGroup pieceSetsCheckBoxGroup = new ButtonGroup();
    for (int i = 0; i < pieceSetsVec.size(); i++){
      PieceSet pieceSet = (PieceSet)pieceSetsVec.elementAt(i);
      String pieceSetName = pieceSet.getName();
      String id = pieceSet.getId();
      
      JRadioButtonMenuItem menuCheckBox = new JRadioButtonMenuItem(pieceSetName);
      menuCheckBox.setActionCommand(id);
      if (id.equals(curPieceSetId))
        menuCheckBox.setSelected(true);
      menuCheckBox.addActionListener(pieceSetListener);
      pieceSetsCheckBoxGroup.add(menuCheckBox);
      pieceSetsMenu.add(menuCheckBox);
    }
    
    return pieceSetsMenu;
  }




  /**
   * Creates and returns the "Boards" menu. This may return null so
   * that no such menu is displayed. The default implementation will return null
   * if less than 2 piece sets are specified in parameters.
   */

  protected JMenu createBoardsMenu(){
    if (boardPatterns.size() < 2)
      return null;
    
    ActionListener boardChangeListener = new ActionListener(){

      public void actionPerformed(ActionEvent evt){
        AbstractButton button = (AbstractButton)evt.getSource();
        setBoardPattern(button.getActionCommand());
      } 
    };
    
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
    

    JMenu boardsMenu = new JMenu("Boards");
    boardsMenu.setMnemonic('B');
    String curBoardPatternId = getBoardPattern().getId();
    ButtonGroup boardsCheckBoxGroup = new ButtonGroup();
    for (int i = 0 ; i < boardPatternsVec.size(); i++){
      BoardPattern boardPattern = (BoardPattern)boardPatternsVec.elementAt(i);
      String boardName = boardPattern.getName();
      String id = boardPattern.getId();
      
      JRadioButtonMenuItem menuCheckBox = new JRadioButtonMenuItem(boardName);
      menuCheckBox.setActionCommand(id);
      if (id.equals(curBoardPatternId))
        menuCheckBox.setSelected(true);
      menuCheckBox.addActionListener(boardChangeListener);
      boardsCheckBoxGroup.add(menuCheckBox);
      boardsMenu.add(menuCheckBox);
    }
    
    return boardsMenu;
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

    URL iconImageURL = BoardManager.class.getResource("icon.gif");
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
      getDraggedPieceStyle() == JBoard.CROSSHAIR_DRAGGED_PIECE ? "target-cursor" : "normal");

    int moveHighlightingStyle = getMoveHighlightingStyle();
    String moveHighlightingString;
    if (moveHighlightingStyle == JBoard.NO_MOVE_HIGHLIGHTING)
      moveHighlightingString = "none";
    else if (moveHighlightingStyle == JBoard.SQUARE_MOVE_HIGHLIGHTING)
      moveHighlightingString = "square";
    else // if (moveHighlightingStyle == JBoard.ARROW_MOVE_HIGHLIGHTING)
      moveHighlightingString = "arrow";
    prefs.setString("move-highlight.style", moveHighlightingString);

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
     * The piece painter for this piece set. Loaded lazily.
     */
     
    private PiecePainter piecePainter;
    
    
    
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
     * Returns the PiecePainter for this piece set or null if unable to load it.
     */
     
    public PiecePainter getPiecePainter(){
      if (piecePainter == null){
        String className = definition.getProperty("classname");
        if (className == null){
          System.err.println("Unable to find classname property in definition of piece set: "
            + getId());
          return null;
        }
  
        try{
          piecePainter = (PiecePainter)loader.loadClass(className).newInstance();
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
      }
      
      return piecePainter;
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
     * The board painter for this piece set. Loaded lazily.
     */
     
    private BoardPainter boardPainter;
    
    
    
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
     * Returns the BoardPainter for this piece set or null if unable to load it.
     */
     
    public BoardPainter getBoardPainter(){
      if (boardPainter == null){
        String className = definition.getProperty("classname");
        if (className == null){
          System.err.println("Unable to find classname property in definition of piece set: "
            + getId());
          return null;
        }
  
        try{
          boardPainter = (BoardPainter)loader.loadClass(className).newInstance();
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
      }
      
      return boardPainter;
    }


    
  }
  


}
