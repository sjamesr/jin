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
import free.jin.Game;
import free.jin.User;
import free.jin.plugin.Plugin;
import free.jin.plugin.PreferencesPanel;
import free.jin.board.event.UserMoveListener;
import free.jin.board.event.UserMoveEvent;
import free.util.StringParser;
import free.util.StringEncoder;
import free.util.SingleItemEnumeration;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.beans.PropertyVetoException;
import java.beans.VetoableChangeListener;
import java.beans.PropertyChangeEvent;
import java.net.URL;


/**
 * The plugin responsible for displaying boards and handling all things related
 * to that.
 */

public class BoardManager extends Plugin implements GameListener, UserMoveListener, VetoableChangeListener, InternalFrameListener{



  /**
   * A Hashtable mapping Game objects to BoardPanel objects which are currently
   * used.
   */

  protected final Hashtable gamesToBoardPanels = new Hashtable();




  /**
   * A list of JInternalFrames which are still on the screen but contain
   * inactive BoardPanels.
   */

  protected final Vector unusedInternalFrames = new Vector();




  /**
   * A Hashtable mapping JInternalFrame objects to BoardPanels they contain.
   */

  protected final Hashtable internalFramesToBoardPanels = new Hashtable();




  /**
   * A Hashtable mapping BoardPanels to their JInternalFrame containers.
   */

  protected final Hashtable boardPanelsToInternalFrames = new Hashtable();




  /**
   * A list of the JInternalFrames in the order they were created.
   */

  protected final Vector internalFrames = new Vector();




  /**
   * Starts this plugin.
   */

  public void start(){
    registerConnListeners();
  }



  /**
   * Stops this plugin.
   */

  public void stop(){
    unregisterConnListeners();
    removeBoards();
  }




  /**
   * Returns the current move input style. Possible values are defined in
   * <code>free.chess.JBoard</code>.
   */

  public int getMoveInputStyle(){
    return "click'n'click".equals(getProperty("move-input-style")) ? JBoard.CLICK_N_CLICK : JBoard.DRAG_N_DROP;
  }




  /**
   * Sets the move input style to the specified value.
   *
   * @param moveInputStyle The new move input style. Possible values are defined
   * in <code>free.chess.JBoard</code>.
   */

  public void setMoveInputStyle(int moveInputStyle){
    setProperty("move-input-style", moveInputStyle == JBoard.CLICK_N_CLICK ? "click'n'click" : "drag'n'drop");

    Enumeration boardPanels = boardPanelsToInternalFrames.keys();
    while (boardPanels.hasMoreElements()){
      BoardPanel boardPanel = (BoardPanel)boardPanels.nextElement();
      boardPanel.getBoard().setMoveInputStyle(moveInputStyle);
    }
  }




  /**
   * Returns the current dragged piece style. Possible values defined in
   * <code>free.chess.JBoard</code>.
   */

  public int getDraggedPieceStyle(){
    return "target-cursor".equals(getProperty("dragged-piece-style")) ? JBoard.CROSSHAIR_DRAGGED_PIECE : JBoard.NORMAL_DRAGGED_PIECE;
  }




  /**
   * Sets the dragged piece style to the specified value.
   *
   * @param draggedPieceStyle The new dragged piece style. Possible values are
   * defined in <code>free.chess.JBoard</code>.
   */

  public void setDraggedPieceStyle(int draggedPieceStyle){
    setProperty("dragged-piece-style", draggedPieceStyle == JBoard.CROSSHAIR_DRAGGED_PIECE ? "target-cursor" : "normal-cursor");

    Enumeration boardPanels = boardPanelsToInternalFrames.keys();
    while (boardPanels.hasMoreElements()){
      BoardPanel boardPanel = (BoardPanel)boardPanels.nextElement();
      boardPanel.getBoard().setDraggedPieceStyle(draggedPieceStyle);
    }
  }




  /**
   * Returns <code>true</code> if promotable pieces are automatically promoted
   * to the default promotion piece. Otherwise, returns <code>false</code>
   */

  public boolean isAutoPromote(){
    return Boolean.valueOf(getProperty("auto-promote", "false")).booleanValue();
  }




  /**
   * Sets whether promotable pieces are promoted automatically to the default
   * promotion piece.
   *
   * @param autoPromote <code>true</code> if promotion should be automatic.
   */

  public void setAutoPromote(boolean autoPromote){
    setProperty("auto-promote", String.valueOf(autoPromote));

    Enumeration boardPanels = boardPanelsToInternalFrames.keys();
    while (boardPanels.hasMoreElements()){
      BoardPanel boardPanel = (BoardPanel)boardPanels.nextElement();
      boardPanel.getBoard().setManualPromote(!autoPromote);
    }
  }




  /**
   * Returns the current move highlighting style. Posssible values are defined
   * in <code>free.chess.JBoard</code>.
   */

  public int getMoveHighlightingStyle(){
    String moveHighlightingStyleString = getProperty("move-highlight.style", "square");
    if ("square".equals(moveHighlightingStyleString))
      return JBoard.SQUARE_MOVE_HIGHLIGHTING;
    else if ("arrow".equals(moveHighlightingStyleString))
      return JBoard.ARROW_MOVE_HIGHLIGHTING;
    else
      return JBoard.NO_MOVE_HIGHLIGHTING;
  }




  /**
   * Sets the current move highlighting style to the specified value.
   * 
   * @param moveHighlightingStyle The new move highlighting style. Possible
   * values are defined in <code>free.chess.JBoard</code>.
   */

  public void setMoveHighlightingStyle(int moveHighlightingStyle){
    String moveHighlightingString;
    if (moveHighlightingStyle == JBoard.NO_MOVE_HIGHLIGHTING)
      moveHighlightingString = "none";
    else if (moveHighlightingStyle == JBoard.SQUARE_MOVE_HIGHLIGHTING)
      moveHighlightingString = "square";
    else if (moveHighlightingStyle == JBoard.ARROW_MOVE_HIGHLIGHTING)
      moveHighlightingString = "arrow";
    else
      throw new IllegalStateException("Unknown move higlighting style: "+moveHighlightingStyle);
    setProperty("move-highlight.style", moveHighlightingString);

    Enumeration boardPanels = boardPanelsToInternalFrames.keys();
    while (boardPanels.hasMoreElements()){
      BoardPanel boardPanel = (BoardPanel)boardPanels.nextElement();
      boardPanel.getBoard().setMoveHighlightingStyle(moveHighlightingStyle);
    }
  }





  /**
   * Returns <code>true</code> if own moves are highlighted as well as the
   * opponent's moves. Returns <code>false</code> if only the opponent's moves
   * are highlighted (if move highlighting is enabled of course). 
   */

  public boolean isHighlightingOwnMoves(){
    return Boolean.valueOf(getProperty("move-highlight.highlight-own", "false")).booleanValue();
  }




  /**
   * Sets whether own moves will be highlighted.
   *
   * @param highlightOwnMoves <code>true</code> if own moves should be
   * highlighted as well as the opponent's moves. <code>false</code> if only
   * the opponent's moves should be highlighted.
   */

  public void setHighlightingOwnMoves(boolean highlightOwnMoves){
    setProperty("move-highlight.highlight-own", String.valueOf(highlightOwnMoves));

    Enumeration boardPanels = boardPanelsToInternalFrames.keys();
    while (boardPanels.hasMoreElements()){
      BoardPanel boardPanel = (BoardPanel)boardPanels.nextElement();
      boardPanel.setHighlightingOwnMoves(highlightOwnMoves);
    }
  }




  /**
   * Returns the current move highlighting color.
   */

  public Color getMoveHighlightingColor(){
    return StringParser.parseColor(getProperty("move-highlighting-color", "00b2b2"));
  }




  /**
   * Sets the current move highlighting color to the specified value.
   *
   * @param moveHighlightingColor The new move highlighting color.
   */

  public void setMoveHighlightingColor(Color moveHighlightingColor){
    setProperty("move-highlighting-color", StringEncoder.encodeColor(moveHighlightingColor));

    Enumeration boardPanels = boardPanelsToInternalFrames.keys();
    while (boardPanels.hasMoreElements()){
      BoardPanel boardPanel = (BoardPanel)boardPanels.nextElement();
      boardPanel.getBoard().setMoveHighlightingColor(moveHighlightingColor);
    }
  }




  /**
   * Returns the current drag square highlighting color.
   */

  public Color getDragSquareHighlightingColor(){
    return StringParser.parseColor(getProperty("drag-square-highlighting-color", "0000ff"));
  }




  /**
   * Sets the drag square highlighting color to the specified value.
   *
   * @param dragSquareHighlightingColor The new drag square highlighting color.
   */

  public void setDragSquareHighlightingColor(Color dragSquareHighlightingColor){
    setProperty("drag-square-highlighting-color", StringEncoder.encodeColor(dragSquareHighlightingColor));

    Enumeration boardPanels = boardPanelsToInternalFrames.keys();
    while (boardPanels.hasMoreElements()){
      BoardPanel boardPanel = (BoardPanel)boardPanels.nextElement();
      boardPanel.getBoard().setDragSquareHighlightingColor(dragSquareHighlightingColor);
    }
  }




  /**
   * Returns an instance of the current PiecePainter.
   */

  public PiecePainter getPiecePainter(){
    PiecePainter piecePainter = null;

    String piecePainterClassName = getProperty("piece-painter-class-name");
    if (piecePainterClassName != null){
      try{
        piecePainter = (PiecePainter)Class.forName(piecePainterClassName).newInstance();
      } catch (ClassNotFoundException e){
          System.err.println("Unable to find class "+piecePainterClassName+", will use the default piece painter.");
        }
        catch (InstantiationException e){
          System.err.println("Unable to instantiate class "+piecePainterClassName+", will use the default piece painter."); 
        }
        catch (IllegalAccessException e){
          System.err.println("Unable to instantiate class "+piecePainterClassName+" due to access restrictions, will use the default piece painter."); 
        }
        catch (ClassCastException e){
          System.err.println("Unable to cast "+piecePainterClassName+" into PiecePainter"); 
        }
    }

    if (piecePainter == null)
      piecePainter = new DefaultPiecePainter();

    configurePiecePainter(piecePainter);

    return piecePainter;
  }




  /**
   * Configures the given PiecePainter before it's used.
   */

  protected void configurePiecePainter(PiecePainter piecePainter){
    if (piecePainter instanceof ColoredPiecePainter){
      ColoredPiecePainter coloredPiecePainter = (ColoredPiecePainter)piecePainter;

      String whiteColor = getProperty("white-piece-color"); 
      String blackColor = getProperty("black-piece-color"); 
      String whiteOutline = getProperty("white-outline-color"); 
      String blackOutline = getProperty("black-outline-color"); 

      if (whiteColor != null)
        coloredPiecePainter.setWhiteColor(StringParser.parseColor(whiteColor));
      if (blackColor != null)
        coloredPiecePainter.setBlackColor(StringParser.parseColor(blackColor));
      if (whiteOutline != null)
        coloredPiecePainter.setWhiteOutline(StringParser.parseColor(whiteOutline));
      if (blackOutline != null)
        coloredPiecePainter.setBlackOutline(StringParser.parseColor(blackOutline));
    }
  }





  /**
   * Sets the current piece painter to be of the specified type.
   *
   * @param piecePainterClassName The class name of the PiecePainter.
   */

  public void setPiecePainterClassName(String piecePainterClassName){
    setProperty("piece-painter-class-name", piecePainterClassName);

    PiecePainter piecePainter = getPiecePainter();

    Enumeration boardPanels = boardPanelsToInternalFrames.keys();
    while (boardPanels.hasMoreElements()){
      BoardPanel boardPanel = (BoardPanel)boardPanels.nextElement();
      boardPanel.getBoard().setPiecePainter(piecePainter);
    }
  }




  /**
   * Returns an instance of the current board painter.
   */

  public BoardPainter getBoardPainter(){
    BoardPainter boardPainter = null;

    String boardPainterClassName = getProperty("board-painter-class-name");
    if (boardPainterClassName != null){
      try{
        boardPainter = (BoardPainter)Class.forName(boardPainterClassName).newInstance();
      } catch (ClassNotFoundException e){
          System.err.println("Unable to find class "+boardPainterClassName+", will use the default board painter.");
        }
        catch (InstantiationException e){
          System.err.println("Unable to instantiate class "+boardPainterClassName+", will use the default board painter."); 
        }
        catch (IllegalAccessException e){
          System.err.println("Unable to instantiate class "+boardPainterClassName+" due to access restrictions, will use the default board painter."); 
        }
        catch (ClassCastException e){
          System.err.println("Unable to cast "+boardPainterClassName+" into BoardPainter"); 
        }
    }
    if (boardPainter == null)
      boardPainter = new DefaultBoardPainter();

     configureBoardPainter(boardPainter);

     return boardPainter;
  }




  /**
   * Sets the current board painter to be of the specified type.
   */

  public void setBoardPainterClassName(String boardPainterClassName){
    setProperty("board-painter-class-name", boardPainterClassName);

    BoardPainter boardPainter = getBoardPainter();
    Enumeration boardPanels = boardPanelsToInternalFrames.keys();
    while (boardPanels.hasMoreElements()){
      BoardPanel boardPanel = (BoardPanel)boardPanels.nextElement();
      boardPanel.getBoard().setBoardPainter(boardPainter);
    }
  }





  /**
   * Configures the given BoardPainter before it's used.
   */

  protected void configureBoardPainter(BoardPainter boardPainter){
    if (boardPainter instanceof ColoredBoardPainter){
      ColoredBoardPainter coloredBoardPainter = (ColoredBoardPainter)boardPainter;

      String lightColor = getProperty("light-square-color");
      String darkColor = getProperty("dark-square-color");

      if (lightColor != null)
        coloredBoardPainter.setLightColor(StringParser.parseColor(lightColor));
      if (darkColor != null)
        coloredBoardPainter.setDarkColor(StringParser.parseColor(darkColor));
    }
  }



  /**
   * Registers all the necessary listeners with the Connection.
   */

  protected void registerConnListeners(){
    JinListenerManager listenerManager = getConnection().getJinListenerManager();

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
    dragndropMenuItem.setMnemonic('C');

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


    boolean isAutoPromote = isAutoPromote();
    final JCheckBoxMenuItem autoQueenMenuItem = new JCheckBoxMenuItem("Auto Queen", isAutoPromote);
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
    
    JRadioButtonMenuItem noMoveHighlightingStyleMenuItem = new JRadioButtonMenuItem("No Highlighting",
      moveHighlightingStyle == JBoard.NO_MOVE_HIGHLIGHTING);
    JRadioButtonMenuItem squareMoveHighlightingStyleMenuItem = new JRadioButtonMenuItem("Square Highlighting",
      moveHighlightingStyle == JBoard.SQUARE_MOVE_HIGHLIGHTING);
    JRadioButtonMenuItem arrowMoveHighlightingStyleMenuItem = new JRadioButtonMenuItem("Arrow Highlighting",
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
   * Creates and returns the "Piece Sets" menu. This may return null so
   * that no such menu is displayed. The default implementation will return null
   * if less than 2 piece sets are specified in parameters.
   */

  protected JMenu createPieceSetsMenu(){
    int pieceSetCount = Integer.parseInt(getProperty("piece-set-count", "0"));
    if (pieceSetCount<2)
      return null;

    ActionListener pieceSetListener = new ActionListener(){

      public void actionPerformed(ActionEvent evt){
        AbstractButton button = (AbstractButton)evt.getSource();
        setPiecePainterClassName(button.getActionCommand());
      } 
    };

    PiecePainter piecePainter = getPiecePainter();
    JMenu pieceSetsMenu = new JMenu("Piece Sets");
    pieceSetsMenu.setMnemonic('P');
    ButtonGroup pieceSetsCheckBoxGroup = new ButtonGroup();
    for (int i=0;i<pieceSetCount;i++){
      String pieceSet = getProperty("piece-set-"+i);
      StringTokenizer tokenizer = new StringTokenizer(pieceSet,";");
      String pieceSetName = tokenizer.nextToken();
      String className = tokenizer.nextToken();
      if (pieceSet==null){
        System.err.println("Piece set with index "+i+" is not specified");
        continue;
      }
      JRadioButtonMenuItem menuCheckBox = new JRadioButtonMenuItem(pieceSetName);
      menuCheckBox.setActionCommand(className);
      if (className.equals(piecePainter.getClass().getName()))
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
    int boardCount = Integer.parseInt(getProperty("board-count", "0"));
    if (boardCount<2)
      return null;

    ActionListener boardChangeListener = new ActionListener(){

      public void actionPerformed(ActionEvent evt){
        AbstractButton button = (AbstractButton)evt.getSource();
        setBoardPainterClassName(button.getActionCommand());
      } 
    };

    BoardPainter boardPainter = getBoardPainter();
    JMenu boardsMenu = new JMenu("Boards");
    boardsMenu.setMnemonic('B');
    ButtonGroup boardsCheckBoxGroup = new ButtonGroup();
    for (int i=0;i<boardCount;i++){
      String board = getProperty("board-"+i);
      StringTokenizer tokenizer = new StringTokenizer(board,";");
      String boardName = tokenizer.nextToken();
      String className = tokenizer.nextToken();
      if (board==null){
        System.err.println("Board with index "+i+" is not specified");
        continue;
      }
      JRadioButtonMenuItem menuCheckBox = new JRadioButtonMenuItem(boardName);
      menuCheckBox.setActionCommand(className);
      if (className.equals(boardPainter.getClass().getName()))
        menuCheckBox.setSelected(true);
      menuCheckBox.addActionListener(boardChangeListener);
      boardsCheckBoxGroup.add(menuCheckBox);
      boardsMenu.add(menuCheckBox);
    }
    
    return boardsMenu;
  }





  /**
   * Rereads the plugin/user properties and changes the assosiated board
   * manager's settings accordingly. This method should be called when the user
   * changes the preferences.
   */

  public void refreshFromProperties(){
    setMoveInputStyle(getMoveInputStyle());
    setDraggedPieceStyle(getDraggedPieceStyle());
    setAutoPromote(isAutoPromote());
    setMoveHighlightingStyle(getMoveHighlightingStyle());
    setHighlightingOwnMoves(isHighlightingOwnMoves());
    setMoveHighlightingColor(getMoveHighlightingColor());
    setDragSquareHighlightingColor(getDragSquareHighlightingColor());
    setPiecePainterClassName(getPiecePainter().getClass().getName());
    setBoardPainterClassName(getBoardPainter().getClass().getName());
  }




  /**
   * Unregisters all the necessary listeners from the Connection. 
   */

  protected void unregisterConnListeners(){
    JinListenerManager listenerManager = getConnection().getJinListenerManager();

    listenerManager.removeGameListener(this);
  } 



  /**
   * Removes the JInternalFrames of all the displayed BoardPanels. Also loses
   * references to all the BoardPanels.
   */

  protected void removeBoards(){
    Enumeration games = gamesToBoardPanels.keys();
    while (games.hasMoreElements()){
      Game game = (Game)games.nextElement();
      BoardPanel boardPanel = getBoardPanel(game);
      JInternalFrame boardFrame = (JInternalFrame)boardPanelsToInternalFrames.remove(boardPanel);
      boardFrame.dispose();
      internalFramesToBoardPanels.remove(boardFrame);
    }

    gamesToBoardPanels.clear();

    int numUnusedFrames = unusedInternalFrames.size();
    for (int i = 0; i < numUnusedFrames; i++){
      JInternalFrame frame = (JInternalFrame)unusedInternalFrames.elementAt(i);
      frame.dispose();
    }
    unusedInternalFrames.removeAllElements();
  }




  /**
   * Returns the BoardPanel displaying the given Game.
   */

  protected BoardPanel getBoardPanel(Game game){
    return (BoardPanel)gamesToBoardPanels.get(game);
  }





  /**
   * Gets called when a game starts. Creates a new BoardPanel and a
   * JInternalFrame to put it in and displays it.
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
   * Creates and configures a new JInternalFrame for use.
   */

  private JInternalFrame createNewBoardFrame(BoardPanel boardPanel){
    JInternalFrame boardFrame = createBoardFrame(boardPanel);

    int index = internalFrames.size();
    for (int i=0;i<internalFrames.size();i++){
      if (internalFrames.elementAt(i)==null){
        index = i;
        break;
      }
    }
    internalFrames.insertElementAt(boardFrame, index);
    boardFrame.addInternalFrameListener(this);

    JDesktopPane desktop = getPluginContext().getMainFrame().getDesktop();
    desktop.add(boardFrame);

    configureBoardFrame(boardFrame, index);

    Container boardFrameContentPane = boardFrame.getContentPane();
    boardFrameContentPane.setLayout(new BorderLayout());
    boardFrameContentPane.add(boardPanel, BorderLayout.CENTER);

    boardFrame.setVisible(true);

    boardFrameContentPane.invalidate();
    boardFrameContentPane.validate();


    /* See http://developer.java.sun.com/developer/bugParade/bugs/4176136.html for the 
       reason I do this instead of adding an InternalFrameListener like a sane person. */
    boardFrame.setDefaultCloseOperation(JInternalFrame.DISPOSE_ON_CLOSE);
    boardFrame.addVetoableChangeListener(this);

    return boardFrame;
  }





  /**
   * Takes a JInternalFrame from the unused board frames list and configures it
   * to be reused.
   */

  private JInternalFrame reuseBoardFrame(BoardPanel boardPanel){
    JInternalFrame boardFrame = (JInternalFrame)unusedInternalFrames.lastElement();
    unusedInternalFrames.removeElementAt(unusedInternalFrames.size()-1);

    BoardPanel oldBoardPanel = (BoardPanel)internalFramesToBoardPanels.remove(boardFrame);
    boardPanelsToInternalFrames.remove(oldBoardPanel);

    Container contentPane = boardFrame.getContentPane();
    contentPane.removeAll();
    contentPane.add(boardPanel, BorderLayout.CENTER);
    contentPane.invalidate();
    contentPane.validate();

    if (boardFrame.isIcon()){
      try{
        boardFrame.setIcon(false);
      } catch (PropertyVetoException e){}
    }

    return boardFrame;
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
    setBoardPanelSettings(new SingleItemEnumeration(boardPanel));

    boardPanel.addUserMoveListener(this);
    getConnection().getJinListenerManager().addGameListener(boardPanel);
    gamesToBoardPanels.put(game, boardPanel);

    JInternalFrame boardFrame;
    if (unusedInternalFrames.isEmpty()){
      boardFrame = createNewBoardFrame(boardPanel);
    }
    else{
      boardFrame = reuseBoardFrame(boardPanel);
    }

    boardFrame.setTitle(boardPanel.getTitle());
    boardFrame.repaint(); // It doesn't seem to repaint itself automatically.

    internalFramesToBoardPanels.put(boardFrame, boardPanel);
    boardPanelsToInternalFrames.put(boardPanel, boardFrame);
		
    boardFrame.toFront();
    try{
      boardFrame.setSelected(true);
    } catch (java.beans.PropertyVetoException e){} // Ignore.
  }




  /**
   * Sets the various settings of the given Enumeration of BoardPanels to the
   * current settings.
   */

  protected void setBoardPanelSettings(Enumeration boardPanels){
    int moveInputStyle = getMoveInputStyle();
    int draggedPieceStyle = getDraggedPieceStyle();
    int moveHighlightingStyle = getMoveHighlightingStyle();
    boolean autoPromote = isAutoPromote();
    PiecePainter piecePainter = getPiecePainter();
    BoardPainter boardPainter = getBoardPainter();
    Color moveHighlightColor = getMoveHighlightingColor();
    Color dragSquareHighlightColor = getDragSquareHighlightingColor();
    boolean isHighlightingOwnMoves = isHighlightingOwnMoves();

    while (boardPanels.hasMoreElements()){
      BoardPanel boardPanel = (BoardPanel)boardPanels.nextElement();
      JBoard board = boardPanel.getBoard();
      board.setMoveInputStyle(moveInputStyle);
      board.setDraggedPieceStyle(draggedPieceStyle);
      board.setMoveHighlightingStyle(moveHighlightingStyle);
      board.setManualPromote(!autoPromote);
      board.setPiecePainter(piecePainter);
      board.setBoardPainter(boardPainter);
      board.setMoveHighlightingColor(moveHighlightColor);
      board.setDragSquareHighlightingColor(dragSquareHighlightColor);

      boardPanel.setHighlightingOwnMoves(isHighlightingOwnMoves);
    }
  }



  /**
   * Creates a JInternalFrame to be used for displaying the given
   * BoardPanel. 
   */

  protected JInternalFrame createBoardFrame(BoardPanel boardPanel){
    JInternalFrame boardFrame = new JInternalFrame();

    boardFrame.setResizable(true);
    boardFrame.setClosable(true);
    boardFrame.setMaximizable(true);
    boardFrame.setIconifiable(true);

    return boardFrame;
  }





  /**
   * Sets the various properties of the given JInternalFrame from the saved
   * properties of the JInternalFrame with the same index. If no JInternalFrame
   * with the given index ever existed, sets those properties to some reasonable
   * defaults.
   */

  private void configureBoardFrame(JInternalFrame boardFrame, int index){
    JDesktopPane desktop = getPluginContext().getMainFrame().getDesktop();

    Rectangle desktopBounds = new Rectangle(desktop.getSize());
    String boundsString = getProperty("frame-bounds-"+index);
    Rectangle bounds = null;
    if (boundsString!=null)
      bounds = StringParser.parseRectangle(boundsString);

    if (bounds==null)
      boardFrame.setBounds(desktopBounds.width/4, 0, desktopBounds.width*3/4, desktopBounds.height*3/4);
    else
      boardFrame.setBounds(bounds);

    boolean isMaximized = Boolean.valueOf(getProperty("maximized-"+index,"false")).booleanValue();
    if (isMaximized){
      try{
        boardFrame.setMaximum(true);
      } catch (java.beans.PropertyVetoException e){}
    }

    boolean isIconified = Boolean.valueOf(getProperty("iconified-"+index,"false")).booleanValue();
    if (isIconified){
      try{
        boardFrame.setIcon(true);
      } catch (java.beans.PropertyVetoException e){}
    }

    JComponent icon = boardFrame.getDesktopIcon();
    String iconBoundsString = getProperty("frame-icon-bounds-"+index);
    if (iconBoundsString!=null){
      Rectangle iconBounds = StringParser.parseRectangle(iconBoundsString);
      icon.setBounds(iconBounds);
    }

    String iconImageName = getProperty("icon-image");
    if (iconImageName != null){
      URL iconImageURL = BoardManager.class.getResource(iconImageName);
      if (iconImageURL!= null)
        boardFrame.setFrameIcon(new ImageIcon(iconImageURL));
    } 
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





  /**
   * Gets called when a game ends. Notifies the BoardPanel displaying the board
   * marking it unused.
   */

  public void gameEnded(GameEndEvent evt){
    BoardPanel boardPanel = (BoardPanel)gamesToBoardPanels.remove(evt.getGame());
    if (boardPanel != null){
      getConnection().getJinListenerManager().removeGameListener(boardPanel);
      boardPanel.removeUserMoveListener(this);
      boardPanel.setInactive();
      JInternalFrame boardFrame = (JInternalFrame)boardPanelsToInternalFrames.get(boardPanel);
//      JInternalFrame boardFrame = (JInternalFrame)boardPanelsToInternalFrames.remove(boardPanel);
//      internalFramesToBoardPanels.remove(boardFrame);
      boardFrame.setTitle(boardPanel.getTitle());
      boardFrame.repaint(); // It doesn't seem to repaint itself.
      if (boardFrame.isClosed()){
        internalFramesToBoardPanels.remove(boardFrame);
        boardPanelsToInternalFrames.remove(boardPanel);
      }
      else
        unusedInternalFrames.addElement(boardFrame);
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
      getConnection().makeMove(game, evt.getMove());
    }
  }




  /**
   * VetoableChangeListener implementation. See http://developer.java.sun.com/developer/bugParade/bugs/4176136.html
   * for the reason this is needed.
   */

  public void vetoableChange(PropertyChangeEvent pce) throws PropertyVetoException{
    Object source = pce.getSource();

    if (source instanceof JInternalFrame){
      if (pce.getPropertyName().equals(JInternalFrame.IS_CLOSED_PROPERTY) &&
          pce.getOldValue().equals(Boolean.FALSE)&&pce.getNewValue().equals(Boolean.TRUE)){
        JInternalFrame boardFrame =  (JInternalFrame)source;
        BoardPanel boardPanel = (BoardPanel)internalFramesToBoardPanels.get(boardFrame);

        if (boardPanel.isActive()){ // isActive()==true, otherwise, the user is just closing a "dead" frame.
          Game game = ((BoardPanel)internalFramesToBoardPanels.get(boardFrame)).getGame();

          boolean shouldAsk = false;
          String question = null;

          if (game.getGameType() == Game.MY_GAME){
            shouldAsk = true;
            if (game.isPlayed())
              question = "Are you sure you want to RESIGN this game?";
            else
              question = "Are you sure you want to stop examining this game?";
          }

          int result;
          if (shouldAsk)
            result = JOptionPane.showConfirmDialog(getPluginContext().getMainFrame(), question, "Select an option", JOptionPane.YES_NO_OPTION);
          else
            result = JOptionPane.YES_OPTION;
          if (result == JOptionPane.YES_OPTION)
            getConnection().quitGame(game);
          else
            throw new PropertyVetoException("Canceled closing", pce);
        }
      }
    }
  }





  /**
   * InternalFrameListener implementation.
   */

  public void internalFrameClosed(InternalFrameEvent e){
    JInternalFrame frame = (JInternalFrame)e.getSource();

    int index = -1;
    for (int i=0;i<internalFrames.size();i++){
      if (internalFrames.elementAt(i)==frame){
        index = i;
        break;
      }
    }
    if (index==-1)
      throw new IllegalStateException("No matching frame found");

    User user = getUser();
    String prefix = getID()+".";

    boolean isMaximized = frame.isMaximum();
    user.setProperty(prefix+"maximized-"+index, String.valueOf(isMaximized));

    boolean isIconified = frame.isIcon();
    user.setProperty(prefix+"iconified-"+index, String.valueOf(isIconified));

    // This is the only way to retrieve the "normal" bounds of the frame under
    // JDK1.2 and earlier. JDK1.3 has a getNormalBounds() method.
    if (isMaximized){
      try{
        frame.setMaximum(false);
      } catch (java.beans.PropertyVetoException ex){}
    }

    Rectangle frameBounds = frame.getBounds();
    // If something bad happened, let's not save that state.
    if ((frameBounds.width>10)&&(frameBounds.height>10))
      user.setProperty(prefix + "frame-bounds-" + index, StringEncoder.encodeRectangle(frameBounds));

    Rectangle iconBounds = frame.getDesktopIcon().getBounds();
    user.setProperty(prefix + "frame-icon-bounds-" + index, StringEncoder.encodeRectangle(iconBounds));

    internalFrames.setElementAt(null, index);
    frame.removeInternalFrameListener(this);
    unusedInternalFrames.removeElement(frame);

    BoardPanel boardPanel = (BoardPanel)internalFramesToBoardPanels.get(frame);

    if (!boardPanel.isActive()){
      internalFramesToBoardPanels.remove(frame);
      boardPanelsToInternalFrames.remove(boardPanel);
    }
  }



  /**
   * InternalFrameListener implementation.
   */

  public void internalFrameActivated(InternalFrameEvent e){}
  public void internalFrameClosing(InternalFrameEvent e){}
  public void internalFrameDeactivated(InternalFrameEvent e){}
  public void internalFrameDeiconified(InternalFrameEvent e){}
  public void internalFrameIconified(InternalFrameEvent e){}
  public void internalFrameOpened(InternalFrameEvent e){}




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


}
