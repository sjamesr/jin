/**
 * Jin - a chess client for internet chess servers.
 * More information is available at http://www.hightemplar.com/jin/.
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
import free.jin.JinConnection;
import free.jin.Game;
import free.jin.User;
import free.jin.plugin.Plugin;
import free.jin.plugin.PreferencesPanel;
import free.jin.board.event.UserMoveListener;
import free.jin.board.event.UserMoveEvent;
import free.util.StringParser;
import free.util.StringEncoder;
import free.util.GraphicsUtilities;
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
   * A Hashtable mapping Game objects to BoardPanel objects which are currently used.
   */

  protected final Hashtable gamesToBoardPanels = new Hashtable();




  /**
   * A list of JInternalFrames which are still on the screen but contain inactive
   * BoardPanels.
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
   * The current move input style.
   */

  protected int moveInputStyle;




  /**
   * The current dragged piece style.
   */

  protected int draggedPieceStyle;




  /**
   * Are we in auto promotion mode?
   */

  protected boolean autoPromote;




  /**
   * The current piece set.
   */

  protected PiecePainter piecePainter;




  /**
   * The current board.
   */

  protected BoardPainter boardPainter;




  /**
   * Starts this plugin.
   */

  public void start(){
    init();
    registerConnListeners();
  }



  /**
   * Stops this plugin.
   */

  public void stop(){
    saveState();
    unregisterConnListeners();
    removeBoards();
  }




  /**
   * Initializes all kinds of variables and other stuff. This is called from the
   * start() method.
   */

  protected void init(){
    moveInputStyle = "click'n'click".equals(getProperty("move-input-style")) ? JBoard.CLICK_N_CLICK : JBoard.DRAG_N_DROP;
    draggedPieceStyle = "target-cursor".equals(getProperty("dragged-piece-style")) ? JBoard.CROSSHAIR_DRAGGED_PIECE : JBoard.NORMAL_DRAGGED_PIECE;
    autoPromote = Boolean.valueOf(getProperty("auto-promote","false")).booleanValue();

    String piecePainterClassName = getProperty("piece-set-class-name");
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

    String boardPainterClassName = getProperty("board-class-name");
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
    JMenu moveInputMenu = new JMenu("Move-Input");

    final JCheckBoxMenuItem dragndrop = new JCheckBoxMenuItem("Drag'n'Drop", moveInputStyle==JBoard.DRAG_N_DROP);
    JCheckBoxMenuItem clicknclick = new JCheckBoxMenuItem("Click'n'Click", moveInputStyle==JBoard.CLICK_N_CLICK);
    ButtonGroup inputModeGroup = new ButtonGroup();
    inputModeGroup.add(dragndrop);
    inputModeGroup.add(clicknclick);
    dragndrop.addChangeListener(new ChangeListener(){
      
      public void stateChanged(ChangeEvent evt){
        moveInputStyle = dragndrop.isSelected() ? JBoard.DRAG_N_DROP : JBoard.CLICK_N_CLICK;

        Enumeration boardPanels = boardPanelsToInternalFrames.keys();
        while (boardPanels.hasMoreElements()){
          BoardPanel boardPanel = (BoardPanel)boardPanels.nextElement();
          boardPanel.getBoard().setMoveInputStyle(moveInputStyle);
        }
      }
      
    });


    final JCheckBoxMenuItem normalDraggedPieceStyle = new JCheckBoxMenuItem("Normal Dragged Piece", draggedPieceStyle==JBoard.NORMAL_DRAGGED_PIECE);
    JCheckBoxMenuItem targetDraggedPieceStyle = new JCheckBoxMenuItem("Target Cursor", draggedPieceStyle==JBoard.CROSSHAIR_DRAGGED_PIECE);
    ButtonGroup draggedPieceStyleGroup = new ButtonGroup();
    draggedPieceStyleGroup.add(normalDraggedPieceStyle);
    draggedPieceStyleGroup.add(targetDraggedPieceStyle);
    normalDraggedPieceStyle.addChangeListener(new ChangeListener(){
      
      public void stateChanged(ChangeEvent evt){
        draggedPieceStyle = normalDraggedPieceStyle.isSelected() ? JBoard.NORMAL_DRAGGED_PIECE : JBoard.CROSSHAIR_DRAGGED_PIECE;

        Enumeration boardPanels = boardPanelsToInternalFrames.keys();
        while (boardPanels.hasMoreElements()){
          BoardPanel boardPanel = (BoardPanel)boardPanels.nextElement();
          boardPanel.getBoard().setDraggedPieceStyle(draggedPieceStyle);
        }
      }
      
    });



    final JCheckBoxMenuItem alwaysQueen = new JCheckBoxMenuItem("Auto Queen");
    alwaysQueen.addChangeListener(new ChangeListener(){

      public void stateChanged(ChangeEvent evt){
        autoPromote = alwaysQueen.isSelected();

        Enumeration boardPanels = boardPanelsToInternalFrames.keys();
        while (boardPanels.hasMoreElements()){
          BoardPanel boardPanel = (BoardPanel)boardPanels.nextElement();
          boardPanel.getBoard().setManualPromote(!autoPromote);
        }
      }

    });

    moveInputMenu.add(dragndrop);
    moveInputMenu.add(clicknclick);
    moveInputMenu.addSeparator();
    moveInputMenu.add(normalDraggedPieceStyle);
    moveInputMenu.add(targetDraggedPieceStyle);
    moveInputMenu.addSeparator();
    moveInputMenu.add(alwaysQueen);

    return moveInputMenu;
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
        String piecePainterClassName = button.getActionCommand();
        setProperty("piece-set-class-name", piecePainterClassName, true);
        updatePieceSet();
      } 
    };

    JMenu pieceSetsMenu = new JMenu("Piece Sets");
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
      JCheckBoxMenuItem menuCheckBox = new JCheckBoxMenuItem(pieceSetName);
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
        String boardPainterClassName = button.getActionCommand();
        setProperty("board-class-name", boardPainterClassName, true);
        updateBoard();
      } 
    };

    JMenu boardsMenu = new JMenu("Boards");
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
      JCheckBoxMenuItem menuCheckBox = new JCheckBoxMenuItem(boardName);
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
    updatePieceSet();
    updateBoard();

    // TODO: finish implementing for the rest of the properties.
  }




  /**
   * Updates the piece painter from properties.
   */

  protected void updatePieceSet(){
    String piecePainterClassName = getProperty("piece-set-class-name");
    try{
      if (piecePainterClassName == null)
        piecePainter = new DefaultPiecePainter();
      else
        piecePainter = (PiecePainter)Class.forName(piecePainterClassName).newInstance();
      configurePiecePainter(piecePainter);

      Enumeration boardPanels = boardPanelsToInternalFrames.keys();
      while (boardPanels.hasMoreElements()){
        BoardPanel boardPanel = (BoardPanel)boardPanels.nextElement();
        boardPanel.getBoard().setPiecePainter(piecePainter);
      }
    } catch (ClassNotFoundException e){
        System.err.println("Unable to find class "+piecePainterClassName);
      }
      catch (InstantiationException e){
        System.err.println("Unable to instantiate class "+piecePainterClassName); 
      }
      catch (IllegalAccessException e){
        System.err.println("Unable to instantiate class "+piecePainterClassName+" due to access restrictions"); 
      }
      catch (ClassCastException e){
        System.err.println("Unable to cast "+piecePainterClassName+" into PiecePainter"); 
      }
  }




  /**
   * Updates the board painter from properties.
   */

  protected void updateBoard(){
    String boardPainterClassName = getProperty("board-class-name");
    try{
      if (boardPainterClassName == null)
        boardPainter = new DefaultBoardPainter();
      else
        boardPainter = (BoardPainter)Class.forName(boardPainterClassName).newInstance();
      configureBoardPainter(boardPainter);

      Enumeration boardPanels = boardPanelsToInternalFrames.keys();
      while (boardPanels.hasMoreElements()){
        BoardPanel boardPanel = (BoardPanel)boardPanels.nextElement();
        boardPanel.getBoard().setBoardPainter(boardPainter);
      }
    } catch (ClassNotFoundException e){
        System.err.println("Unable to find class "+boardPainterClassName);
      }
      catch (InstantiationException e){
        System.err.println("Unable to instantiate class "+boardPainterClassName); 
      }
      catch (IllegalAccessException e){
        System.err.println("Unable to instantiate class "+boardPainterClassName+" due to access restrictions"); 
      }
      catch (ClassCastException e){
        System.err.println("Unable to cast "+boardPainterClassName+" into BoardPainter"); 
      }
  }




  /**
   * Saves the current properties of the BoardManager into the user properties.
   */

  protected void saveState(){
    int oldMoveInputStyle = "click'n'click".equals(getProperty("move-input-style")) ? JBoard.CLICK_N_CLICK : JBoard.DRAG_N_DROP;
    if (oldMoveInputStyle!=moveInputStyle)
      setProperty("move-input-style", moveInputStyle==JBoard.CLICK_N_CLICK ? "click'n'click" : "drag'n'drop", true);


    int oldDraggedPieceStyle = "target-cursor".equals(getProperty("dragged-piece-style")) ? JBoard.CROSSHAIR_DRAGGED_PIECE : JBoard.NORMAL_DRAGGED_PIECE;
    if (oldDraggedPieceStyle!=draggedPieceStyle)
      setProperty("dragged-piece-style", draggedPieceStyle==JBoard.CROSSHAIR_DRAGGED_PIECE ? "target-cursor" : "normal-cursor", true);

    boolean oldAutoPromote = Boolean.valueOf(getProperty("auto-promote","false")).booleanValue();
    if (oldAutoPromote!=autoPromote)
      setProperty("auto-promote", String.valueOf(autoPromote), true);
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
    for (int i=0;i<numUnusedFrames;i++){
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
   * Gets called when a game starts. Creates a new BoardPanel and a JInternalFrame
   * to put it in and displays it.
   */

  public void gameStarted(GameStartEvent evt){
    Game game = evt.getGame();
    BoardPanel boardPanel = createBoardPanel(game);
    boardPanel.getBoard().setMoveInputStyle(moveInputStyle);
    boardPanel.getBoard().setDraggedPieceStyle(draggedPieceStyle);
    boardPanel.getBoard().setManualPromote(!autoPromote);
    boardPanel.getBoard().setPiecePainter(piecePainter);
    boardPanel.getBoard().setBoardPainter(boardPainter);

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
      if (pce.getPropertyName().equals(JInternalFrame.IS_CLOSED_PROPERTY)&&
          pce.getOldValue().equals(Boolean.FALSE)&&pce.getNewValue().equals(Boolean.TRUE)){
        JInternalFrame boardFrame =  (JInternalFrame)source;
        BoardPanel boardPanel = (BoardPanel)internalFramesToBoardPanels.get(boardFrame);

        if (boardPanel.isActive()){ // isActive()==true, otherwise, the user is just closing a "dead" frame.
          Game game = ((BoardPanel)internalFramesToBoardPanels.get(boardFrame)).getGame();

          int result = JOptionPane.showConfirmDialog(getPluginContext().getMainFrame(),"Are you sure you want to quit this game?","Select an option",JOptionPane.YES_NO_OPTION);
          if (result==JOptionPane.YES_OPTION)
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
    user.setProperty(prefix+"maximized-"+index, String.valueOf(isMaximized), false);

    boolean isIconified = frame.isIcon();
    user.setProperty(prefix+"iconified-"+index, String.valueOf(isIconified), false);

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
      user.setProperty(prefix+"frame-bounds-"+index,StringEncoder.encodeRectangle(frameBounds),false);

    Rectangle iconBounds = frame.getDesktopIcon().getBounds();
    user.setProperty(prefix+"frame-icon-bounds-"+index,StringEncoder.encodeRectangle(iconBounds),false);

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
