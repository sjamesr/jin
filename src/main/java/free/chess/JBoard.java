/**
 * The chess framework library.
 * More information is available at http://www.jinchess.com/.
 * Copyright (C) 2002 Alexander Maryanovsky.
 * All rights reserved.
 *
 * The chess framework library is free software; you can redistribute
 * it and/or modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 2 of the
 * License, or (at your option) any later version.
 *
 * The chess framework library is distributed in the hope that it will
 * be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser
 * General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with the chess framework library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package free.chess;

import java.awt.AWTEvent;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.util.Collection;
import java.util.Iterator;
import java.util.Vector;

import javax.swing.JComponent;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import free.chess.event.MoveEvent;
import free.chess.event.MoveListener;
import free.chess.event.MoveProgressEvent;
import free.chess.event.MoveProgressListener;
import free.util.MathUtilities;
import free.util.PaintHook;
import free.util.PlatformUtils;
import free.util.Utilities;


/**
 * An implementation of a chess board component.
 * <B>IMPORTANT:</B> This class is not thread safe - all modifications should be
 * done in the AWT event dispatching thread.
 */

public class JBoard extends JComponent{


  
  /**
   * The default move highlighting color.
   */
   
  private static final Color DEFAULT_MOVE_HIGHLIGHT_COLOR = Color.cyan.darker();
  


  /**
   * The default coordinate display color.
   */
   
  private static final Color DEFAULT_COORDS_DISPLAY_COLOR = Color.blue.darker();
  
  
  
  /**
   * The default highlighting color for the squares of the move currently being
   * made.
   */
   
  private static final Color DEFAULT_MADE_MOVE_SQUARES_HIGHLIGHT_COLOR = Color.blue;
  
  
  
  /**
   * The default color for highlighting possible target squares.
   */
  
  private static final Color DEFAULT_POSSIBLE_TARGET_SQUARES_HIGHLIGHT_COLOR = new Color(255, 255, 255, 80);
   
  
  
  /**
   * The constant for unified move input style. This style allows both
   * drag'n'drop and click'n'click move gestures, similarly to how menus work on
   * many graphical systems.
   */
  
  public static final int UNIFIED_MOVE_INPUT_STYLE = 0;
  
  
  
  /**
   * The constant for drag'n'drop move input style.
   */

  public static final int DRAG_N_DROP_MOVE_INPUT_STYLE = 1;



  /**
   * The constant for click'n'click move input style.
   */

  public static final int CLICK_N_CLICK_MOVE_INPUT_STYLE = 2;



  /**
   * The constant for move input mode which doesn't let the user move any of the
   * pieces.
   */

  public static final int NO_PIECES_MOVE = 0;



  /**
   * The constant for move input mode which only lets the user move white pieces.
   */

  public static final int WHITE_PIECES_MOVE = 1;



  /**
   * The constant for move input mode which only lets the user move black pieces.
   */

  public static final int BLACK_PIECES_MOVE = 2;



  /**
   * The constant for move input mode which lets the user move both white and
   * black pieces.
   */

  public static final int ALL_PIECES_MOVE = 3;



  /**
   * The constant for move input mode which only lets the user move the pieces
   * of the player to move.
   */

  public static final int CURRENT_PLAYER_MOVES = 4;



  /**
   * The constant for no move highlighting.
   */

  public static final int NO_MOVE_HIGHLIGHTING = 0;



  /**
   * The constant for move highlighting done by highlighting the target square
   * of the move.
   */

  public static final int TARGET_SQUARE_MOVE_HIGHLIGHTING = 1;



  /**
   * The constant for move highlighting done by highlighting the source and
   * target squares of the move.
   */

  public static final int BOTH_SQUARES_MOVE_HIGHLIGHTING = 2;



  /**
   * The constant for move highlighting done by drawing an arrow from the source
   * square to the target square.
   */

  public static final int ARROW_MOVE_HIGHLIGHTING = 3;

  
  
  /**
   * The constant for not displaying coordinates at all.
   */
   
  public static final int NO_COORDS = 0;
  
  
  
  /**
   * The constant for displaying row and column coordinates on the outer rim
   * of the board, xboard style.
   */
   
  public static final int RIM_COORDS = 1;
  


  /**
   * The constant for displaying row and column coordinates outside of the
   * actual board.
   */
   
  public static final int OUTSIDE_COORDS = 2;
   
  
  
  /**
   * The constant for displaying square coordinates in each square.
   */
   
  public static final int EVERY_SQUARE_COORDS = 3;
  
  

  /**
   * The Position on the board.
   */

  private Position position;
  
  
  
  /**
   * A copy of the current position on the board. We keep this so that when the
   * real position changes, we know which squares need repainting.
   */
  
  private Position positionCopy;
  
  
  
  /**
   * A flag we set when we make a move entered by the user, on this board.
   */
  
  private boolean isMakingUserMove = false;
  
  /**
   * A flag we set when the position was modified because of a move (not just a
   * general change) so we know to ignore the next change event.
   */
  
  private boolean positionChangedByMove = false;
  
  /**
   * The <code>ChangeListener</code> to the <code>Position</code>.
   */

  private final ChangeListener positionChangeListener = new ChangeListener(){
    @Override
    public void stateChanged(ChangeEvent evt){
      if (positionChangedByMove){
        positionChangedByMove = false;
        return;
      }
      
      updateBoard(positionCopy, position);
    }
  };
  
  
  
  /**
   * Updates the board from the specified current position to the specified
   * target position, causing the required areas to be repainted.
   */
  
  private void updateBoard(Position startPosition, Position endPosition){
    // Repaint only the parts that really need to be repainted by checking
    // which squares changed.
    boolean checkMovingPieceSquare = (movedPieceSquare != null);
    Rectangle tmpRect = new Rectangle();


    // Repaint the dragged piece position.
    if (checkMovingPieceSquare)
      repaint(tmpRect = getMoveAreaRect(tmpRect));

    for (int file = 0; file < 8; file++){
      for (int rank = 0; rank < 8; rank++){
        Piece oldPiece = startPosition.getPieceAt(file, rank);
        Piece newPiece = endPosition.getPieceAt(file, rank);

        // We don't need to repaint the origin square of the moving piece.
        if (checkMovingPieceSquare && (file == movedPieceSquare.getFile()) && (rank == movedPieceSquare.getRank())){
          checkMovingPieceSquare = false;
          continue;
        }

        if (!Utilities.areEqual(oldPiece, newPiece))
          repaint(tmpRect = squareToRect(file, rank, tmpRect));
      }
    }

    if (isHighlightMadeMoveSquares() && (movedPieceSquare != null))
      repaint(tmpRect = squareToRect(movedPieceSquare, tmpRect));

    if ((movedPieceSquare != null) && !isMakingUserMove){    // We were dragging a piece
      if (endPosition.getPieceAt(movedPieceSquare) == null){ // But the piece we were dragging 
        cancelMovingPiece();                                 // is no longer there
      }
    }
    
    // Clean up after and end any sliding
    if (slideStartSquare != null){
      repaint(tmpRect = slideRect(((double)slideTime)/getSlideDuration(), tmpRect));
      repaint(tmpRect = squareToRect(slideStartSquare, tmpRect));
      
      slideStartSquare = null;
      slideEndSquare = null;
      slidePiece = null;
      slideTakenPiece = null;
      slideTimer.stop();
    }
    
    positionCopy.copyFrom(endPosition);
  }
  
  
  
  /**
   * The <code>MoveListener</code> of the position.
   */
  
  private final MoveListener positionMoveListener = new MoveListener(){
    @Override
    public void moveMade(MoveEvent evt){
      if (isMakingUserMove)
        return;
      
      if (!(evt.getMove().getClass().equals(ChessMove.class))) // We can't slide for such moves
        return;
      
      if (getSlideDuration() < 0)
        return;
      
      // Finish up the previous animation, if any
      if (slideStartSquare != null)
        updateBoard(positionCopy, slideTargetPosition);
      
      ChessMove move = (ChessMove)evt.getMove();
      
      positionChangedByMove = true;
      
      if (slideTargetPosition == null)
        slideTargetPosition = new Position(position);
      else
        slideTargetPosition.copyFrom(position);
      slideStartSquare = move.getStartingSquare();
      slideEndSquare = move.getEndingSquare();
      slidePiece = positionCopy.getPieceAt(slideStartSquare);
      slideTakenPiece = positionCopy.getPieceAt(slideEndSquare);
      slideStartTime = System.currentTimeMillis();
      slideTime = 0;
      
      slideTimer.restart();
    }
  };
  
  

  /**
   * The BoardPainter painting the board.
   */

  private BoardPainter boardPainter;



  /**
   * The PiecePainter painting the pieces.
   */

  private PiecePainter piecePainter;



  /**
   * PaintHooks.
   */

  private Vector paintHooks = null;



  /**
   * The current move input style.
   */

  private int moveInputStyle = UNIFIED_MOVE_INPUT_STYLE;



  /**
   * The current move input mode.
   */

  private int moveInputMode = ALL_PIECES_MOVE;



  /**
   * Whether the piece follows the cursor as a move is being made.
   */

  private boolean isPieceFollowsCursor = true;
  
  
  
  /**
   * Whether the squares of the made move are highlighted.
   */
   
  private boolean isHighlightMadeMoveSquares = false;
  


  /**
   * The current move highlighting style.
   */

  private int moveHighlightingStyle = NO_MOVE_HIGHLIGHTING;
  
  
  
  /**
   * Whether a shadow piece is shown in the target square while making a move.
   */
  
  private boolean isShowShadowPieceInTargetSquare = false;
  
  
  
  /**
   * Whether legal target squares are highlighted when making a move.
   */
  
  private boolean isHighlightLegalTargetSquares = false;
  
  
  
  /**
   * Whether the target square snaps to the nearest legal square when making a
   * move.
   */
  
  private boolean isSnapToLegalSquare = false;
  
  
  
  /**
   * The current coordinates display style.
   */
   
  private int coordsDisplayStyle = NO_COORDS; 



  /**
   * Is the board editable?
   */

  private boolean isEditable = true;



  /**
   * Is the board flipped?
   */

  private boolean isFlipped = false;



  /**
   * Are we currently manual promotion mode?
   */

  private boolean isManualPromote = true;



  /**
   * The color used for move highlighting.
   */

  private Color moveHighlightingColor = DEFAULT_MOVE_HIGHLIGHT_COLOR;
  
  
  
  /**
   * The color used for coordinate display. <code>null</code> means the default
   * label color is used.
   */
   
  private Color coordsDisplayColor = DEFAULT_COORDS_DISPLAY_COLOR;
   


  /**
   * The color used for highlighting the squares of a move as it's being made.
   */

  private Color madeMoveSquaresHighlightColor = DEFAULT_MADE_MOVE_SQUARES_HIGHLIGHT_COLOR;
  
  
  
  /**
   * The color with which possible target squares are highlighted.
   */
  
  private Color legalTargetSquaresHighlightColor = DEFAULT_POSSIBLE_TARGET_SQUARES_HIGHLIGHT_COLOR;
  
  
  
  /**
   * The duration of piece sliding. When negative, no sliding occurs.
   */
  
  private int slideDuration = -1;
  
  
  
  /**
   * The current highlighted move.
   */
  
  private Move highlightedMove = null;



  /**
   * An array specifying which squares are shaded.
   */

  private boolean [][] isShaded = new boolean[8][8];



  /**
   * The square of the currently moved piece; <code>null</code> when not making
   * a move.
   */

  private Square movedPieceSquare = null;



  /**
   * The location of the cursor when a move is being made; <code>null</code>
   * when not making a move.
   */

  private Point movedPieceLoc = null;
  
  
  
  /**
   * The current target square during a move, <code>null</code> if none. Note
   * that this may be <code>null</code> even during a move (for example, if the
   * snap-to-legal-square mechanism can't determine the best target).
   */
  
  private Square targetSquare;
  
  
  
  /**
   * A set of possible target squares during a move; <code>null</code> if none.
   * May be <code>null</code> even during a move, if it's not required (neither
   * in snap-to-legal-square, nor in highlight-legal-moves mode).
   */
  
  private Collection legalTargetSquares = null;
  
  
  
  /**
   * Indicates the current move gesture if in <code>UNIFIED_MOVE_INPUT_STYLE</code>.
   * Possible values are:
   * <ul>
   *  <code>UNIFIED_MOVE_INPUT_STYLE</code> when no move gesture is in progress.
   *  <code>DRAG_N_DROP_MOVE_INPUT_STYLE</code> if the current move gesture is a drag'n'drop.
   *  <code>CLICK_N_CLICK_MOVE_INPUT_STYLE</code> if the current move gesture is a click'n'click.
   * </ul>
   * This is not a property, but a helper variable - it just reuses the
   * constants because it makes sense.
   */
  
  private int moveGesture = UNIFIED_MOVE_INPUT_STYLE;
  
  
  
  /**
   * The time when the last drag gesture started, in
   * <code>UNIFIED_MOVE_MODE</code>.
   */
  
  private long dragStartTime;
  
  
  
  /**
   * The starting square during a piece slide.
   */
  
  private Square slideStartSquare = null;
  
  
  
  /**
   * The ending square during a piece slide.
   */
  
  private Square slideEndSquare = null;
  
  
  
  /**
   * The sliding piece during a piece slide.
   */
  
  private Piece slidePiece = null;
  
  
  
  /**
   * The taken piece during a piece slide.
   */
  
  private Piece slideTakenPiece;
  
  
  
  /**
   * The end position for a slide animation.
   */
  
  private Position slideTargetPosition;
  
  
  
  /**
   * The time when the sliding started.
   */
  
  private long slideStartTime;
  
  
  
  /**
   * How far along are we in sliding. The time passed from
   * <code>slideStartTime</code> to the time repaint() was called with the
   * purpose of painting the next slide frame.
   */
  
  private long slideTime;
  
  
  
  /**
   * The time which invoked repaint() periodically to animate sliding.
   */
  
  private final Timer slideTimer = new Timer(20, new ActionListener(){
    private final Rectangle rect = new Rectangle();
    @Override
    public void actionPerformed(ActionEvent e){
      int slideDuration = getSlideDuration();
      
      // Repaint the old location
      repaint(slideRect(((double)slideTime)/slideDuration, rect));

      slideTime = System.currentTimeMillis() - slideStartTime;
      if (slideTime > slideDuration){ // we're done sliding
        // Clear sliding data
        slideStartSquare = null;
        slideEndSquare = null;
        slidePiece = null;
        slideTakenPiece = null;
        slideTimer.stop();
        
        // We're done animating, so procede as usual
        positionChangeListener.stateChanged(new ChangeEvent(position));
      }
      else{
        // Repaint the new location
        repaint(slideRect(((double)slideTime)/slideDuration, rect));
      }
    }
  });
  
  
  
  /**
   * A boolean telling us whether we're currently showing the promotion target
   * selection dialog. This is needed to workaround the bug which keeps sending
   * events after show() has been called on a modal dialog but before it's
   * actually displayed.
   */

  private boolean isShowingModalDialog = false;



  /**
   * Creates a new JBoard with the specified position and BoardPainter and
   * PiecePainter.
   */

  public JBoard(Position position, BoardPainter boardPainter, PiecePainter piecePainter){
    if (position == null)
      throw new IllegalArgumentException("The Position may not be null");
    if (boardPainter == null)
      throw new IllegalArgumentException("The BoardPainter may not be null");
    if (piecePainter == null)
      throw new IllegalArgumentException("The PiecePainter may not be null");

    setPosition(position);
    this.boardPainter = boardPainter;
    this.piecePainter = piecePainter;

    setOpaque(true);
    enableEvents(AWTEvent.MOUSE_EVENT_MASK |
                 AWTEvent.MOUSE_MOTION_EVENT_MASK);
  }



  /**
   * Creates a new JBoard with the given position set on it.
   */

  public JBoard(Position position){
    this(position, position.getVariant().createDefaultBoardPainter(), position.getVariant().createDefaultPiecePainter());
  }
  


  /**
   * Creates a new JBoard with the initial position set on it.
   */

  public JBoard(){
    this(new Position());
  }
  
  
  
  /**
   * Adds a <code>MoveProgressListener</code>.
   */
   
  public void addMoveProgressListener(MoveProgressListener listener){
    listenerList.add(MoveProgressListener.class, listener);
  }



  /**
   * Remove a <code>MoveProgressListener</code>.
   */
   
  public void removeMoveProgressListener(MoveProgressListener listener){
    listenerList.remove(MoveProgressListener.class, listener);
  }
  
  
  
  /**
   * Fires the specified <code>MoveProfressEvent</code> to interested listeners.
   */
   
  protected void fireMoveProgressEvent(MoveProgressEvent evt){
    int id = evt.getId();
    Object [] listeners = listenerList.getListenerList();
    for (int i = listeners.length-2; i>=0; i-=2 ){
      if (listeners[i] == MoveProgressListener.class){
        MoveProgressListener listener = (MoveProgressListener)listeners[i+1]; 
        switch (id){
          case MoveProgressEvent.MOVE_MAKING_STARTED:
            listener.moveMakingStarted(evt);
            break;
          case MoveProgressEvent.MOVE_MAKING_ENDED:
            listener.moveMakingEnded(evt);
            break;
        }
      }          
    }
  }



  /**
   * Adds the given PaintHook to the list of PaintHooks which are called
   * during the painting of this JBoard.
   */

  public void addPaintHook(PaintHook hook){
     if (paintHooks == null)
       paintHooks = new Vector(2);

     paintHooks.addElement(hook);
  }



  /**
   * Removes the given PaintHook from the list of PaintHooks which are called
   * during the painting of this JBoard.
   */

  public void removePaintHook(PaintHook hook){
    paintHooks.removeElement(hook);

    if (paintHooks.size() == 0)
      paintHooks = null;
  }



  /**
   * Returns the Position on this JBoard.
   */

  public Position getPosition(){
    return position;
  }



  /**
   * Sets this JBoard to display the given Position.
   */

  public void setPosition(Position newPosition){
    if (newPosition == null)
      throw new IllegalArgumentException("Null position");

    Position oldPosition = position;
    if (position != null){
      position.removeChangeListener(positionChangeListener);
      position.removeMoveListener(positionMoveListener);
    }
    position = newPosition;
    position.addChangeListener(positionChangeListener);
    position.addMoveListener(positionMoveListener);

    if (positionCopy == null)
      positionCopy = new Position(position);
    else
      positionCopy.copyFrom(position);

    repaint();

    firePropertyChange("position", oldPosition, newPosition);
  }



  /**
   * Sets the move input style of this JBoard to the given style. Possible values
   * are {@link #UNIFIED_MOVE_INPUT_STYLE}, 
   * {@link #DRAG_N_DROP_MOVE_INPUT_STYLE} and 
   * {@link #CLICK_N_CLICK_MOVE_INPUT_STYLE}.
   */

  public void setMoveInputStyle(int newStyle){
    switch(newStyle){
      case UNIFIED_MOVE_INPUT_STYLE:
      case DRAG_N_DROP_MOVE_INPUT_STYLE:
      case CLICK_N_CLICK_MOVE_INPUT_STYLE:
        break;
      default:
        throw new IllegalArgumentException("Illegal move input style value: "+newStyle);
    }
    
    int oldStyle = moveInputStyle;
    moveInputStyle = newStyle;
    firePropertyChange("moveInputStyle", oldStyle, newStyle);
  }



  /**
   * Returns the current move input style for this JBoard.
   */

  public int getMoveInputStyle(){
    return moveInputStyle;
  }



  /**
   * Sets the move input mode of this JBoard to the given mode. Possible values
   * are {@link #NO_PIECES_MOVE}, {@link #WHITE_PIECES_MOVE}, {@link #BLACK_PIECES_MOVE},
   * {@link #ALL_PIECES_MOVE} and {@link #CURRENT_PLAYER_MOVES}.
   */

  public void setMoveInputMode(int newMode){
    switch(newMode){
      case NO_PIECES_MOVE:
      case WHITE_PIECES_MOVE:
      case BLACK_PIECES_MOVE:
      case ALL_PIECES_MOVE:
      case CURRENT_PLAYER_MOVES:
        break;
      default:
        throw new IllegalArgumentException("Illegal move input mode value: "+newMode);
    }
    int oldMode = moveInputMode;
    moveInputMode = newMode;
    firePropertyChange("moveInputMode", oldMode, newMode);
  }
  


  /**
   * Returns the current move input mode for this JBoard.
   */

  public int getMoveInputMode(){
    return moveInputMode;
  }



  /**
   * Sets whether pieces can at all be moved.
   */

  public void setEditable(boolean isEditable){
    boolean oldEditable = this.isEditable;
    this.isEditable = isEditable;
    firePropertyChange("editable", oldEditable, isEditable);
  }



  /**
   * Returns <code>true</code> if the board is editable, i.e. the pieces can at
   * all be moved.
   */

  public boolean isEditable(){
    return isEditable;
  }



  /**
   * Sets whether the moved piece follows the mouse cursor while a move is being
   * made.
   */

  public void setPieceFollowsCursor(boolean isPieceFollowsCursor){
    boolean oldVal = this.isPieceFollowsCursor; 
    this.isPieceFollowsCursor = isPieceFollowsCursor;
    firePropertyChange("pieceFollowsCursor", oldVal, isPieceFollowsCursor);
  }



  /**
   * Returns whether the moved piece follows the mouse cursor while a move is
   * being made.
   */

  public boolean isPieceFollowsCursor(){
    return isPieceFollowsCursor;
  }


  
  /**
   * Sets whether the squares of a move being made are highlighted. 
   */

  public void setHighlightMadeMoveSquares(boolean highlight){
    boolean oldVal = this.isHighlightMadeMoveSquares; 
    this.isHighlightMadeMoveSquares = highlight;
    firePropertyChange("highlightMadeMoveSquares", oldVal, highlight);
  }



  /**
   * Returns whether the move being made is highlighted.
   */

  public boolean isHighlightMadeMoveSquares(){
    return isHighlightMadeMoveSquares;
  }

  

  /**
   * Sets the move highlighting style.
   */

  public void setMoveHighlightingStyle(int newStyle){
    switch(newStyle){
      case NO_MOVE_HIGHLIGHTING:
      case TARGET_SQUARE_MOVE_HIGHLIGHTING:
      case BOTH_SQUARES_MOVE_HIGHLIGHTING:
      case ARROW_MOVE_HIGHLIGHTING:
        break;
      default:
        throw new IllegalArgumentException("Illegal move highlighting style value: " + newStyle);
    }

    int oldStyle = moveHighlightingStyle;
    this.moveHighlightingStyle = newStyle;
    if (highlightedMove != null)
      repaint();
    firePropertyChange("moveHighlightingStyle", oldStyle, newStyle);
  }



  /**
   * Returns the move highlighting style.
   */

  public int getMoveHighlightingStyle(){
    return moveHighlightingStyle;
  }



  /**
   * Sets the currently highlighted move, or <code>null</code> if no move
   * should be highlighted.
   */

  public void setHighlightedMove(Move move){
    repaintHighlighting();

    highlightedMove = move;

    repaintHighlighting();
  }



  /**
   * Calculates the area that needs to be repainted for the current
   * highlighting and repaints it.
   */

  private void repaintHighlighting(){
    int moveHighlightingStyle = getMoveHighlightingStyle();
    if ((moveHighlightingStyle == NO_MOVE_HIGHLIGHTING) || (highlightedMove == null))
      return;

    Square from = highlightedMove.getStartingSquare();
    Square to = highlightedMove.getEndingSquare();

    if ((from == null) || (to == null))
      return;

    if (moveHighlightingStyle == TARGET_SQUARE_MOVE_HIGHLIGHTING)
      repaint(squareToRect(to, null));
    if (moveHighlightingStyle == BOTH_SQUARES_MOVE_HIGHLIGHTING){
      repaint(squareToRect(from, null));
      repaint(squareToRect(to, null));
    }
    else if (moveHighlightingStyle == ARROW_MOVE_HIGHLIGHTING)
      repaint(squareToRect(from, null).union(squareToRect(to, null)));
  }
  
  
  
  /**
   * Sets whether during a move a shadow piece is displayed at the target
   * square.
   */
  
  public void setShowShadowPieceInTargetSquare(boolean newValue){
    boolean oldValue = this.isShowShadowPieceInTargetSquare;
    this.isShowShadowPieceInTargetSquare = newValue;
    if (isMovingPiece())
      repaint(squareToRect(targetSquare, null));
    firePropertyChange("isShowShadowPieceInTargetSquare", oldValue, newValue);
  }
  
  
  
  /**
   * Returns whether during a move a shadow piece is displayed at the target
   * square.
   */
  
  public boolean isShowShadowPieceInTargetSquare(){
    return isShowShadowPieceInTargetSquare;
  }
  
  
  
  /**
   * Sets whether during a move the legal target squares are highlighted.
   */
  
  public void setHighlightLegalTargetSquares(boolean newValue){
    boolean oldValue = this.isHighlightLegalTargetSquares;
    this.isHighlightLegalTargetSquares = newValue;
    
    if (isMovingPiece()){
      if (newValue)
        legalTargetSquares = position.getTargetSquares(movedPieceSquare);
      else
        legalTargetSquares = null;
      
      repaint();
    }
    
    firePropertyChange("isHighlightLegalTargetSquares", oldValue, newValue);
  }
  
  
  
  /**
   * Returns whether during a move the legal target squares are highlighted.
   */
  
  public boolean isHighlightLegalTargetSquares(){
    return isHighlightLegalTargetSquares;
  }
  
  
  
  /**
   * Sets the color with which legal target squares are highlighted. This
   * should normally be a translucent color, so that for captures, the captured
   * piece is visible. Passing <code>null</code> causes the color to be reset
   * to the default one.
   */
  
  public void setLegalTargetSquaresHighlightColor(Color color){
    if (color == null)
      color = DEFAULT_POSSIBLE_TARGET_SQUARES_HIGHLIGHT_COLOR;
    
    Object oldColor = this.legalTargetSquaresHighlightColor;
    this.legalTargetSquaresHighlightColor = color;
    
    if (legalTargetSquares != null)
      repaintLegalTargetSquares(null);
    
    firePropertyChange("legalTargetSquaresHighlightColor", oldColor, color);
  }
  
  
  
  /**
   * Returns the color with which legal target squares are highlighted.
   */
  
  public Color getLegalTargetSquaresHighlightColor(){
    return legalTargetSquaresHighlightColor;
  }
  
  
  
  /**
   * Sets whether the target square snaps to the nearest legal square when
   * making a move.
   */
  
  public void setSnapToLegalSquare(boolean newValue){
    boolean oldValue = this.isSnapToLegalSquare;
    this.isSnapToLegalSquare = newValue;
    
    firePropertyChange("isSnapToLegalSquare", oldValue, newValue);
  }
  
  
  
  /**
   * Returns whether the target square snaps to the nearest legal square when
   * making a move.
   */
  
  public boolean isSnapToLegalSquare(){
    return isSnapToLegalSquare;
  }
  
  
  
  /**
   * Sets the coordinate display style. Possible values are {@link #NO_COORDS},
   * {@link #RIM_COORDS}, {@link #OUTSIDE_COORDS} and
   * {@link #EVERY_SQUARE_COORDS}.
   */
   
  public void setCoordsDisplayStyle(int newStyle){
    switch (newStyle){
      case NO_COORDS:
      case RIM_COORDS:
      case OUTSIDE_COORDS:
      case EVERY_SQUARE_COORDS:
        break;
      default:
        throw new IllegalArgumentException("Illegal coordinates display style value: " + newStyle);
    }
    
    int oldStyle = coordsDisplayStyle;
    this.coordsDisplayStyle = newStyle;
    repaint();
    firePropertyChange("coordsDisplayStyle", oldStyle, newStyle);
  }
  
  
  
  /**
   * Returns the current coordinates display style.
   */
   
  public int getCoordsDisplayStyle(){
    return coordsDisplayStyle;
  }



  /**
   * Sets the board's flipped state. When the board is flipped, it displays the
   * black side at the bottom.
   */

  public void setFlipped(boolean isFlipped){
    boolean oldFlipped = this.isFlipped;
    this.isFlipped = isFlipped;
    repaint();
    firePropertyChange("flipped", oldFlipped, isFlipped);
  }



  /**
   * Returns true if the JBoard is flipped, false otherwise.
   */

  public boolean isFlipped(){
    return isFlipped;
  }



  /**
   * Sets whether the user will be prompted which piece to promote to when a
   * promotion occurs or will the default promotion piece be used.
   */

  public void setManualPromote(boolean isManualPromote){
    boolean oldManualPromote = this.isManualPromote;
    this.isManualPromote = isManualPromote;
    firePropertyChange("manualPromote", oldManualPromote, isManualPromote);
  }



  /**
   * Returns true if on a promotion move, the user will be prompted which piece
   * he wants to promote to, returns false if the default promotion piece
   * will be used without prompting the user.
   */

  public boolean isManualPromote(){
    return isManualPromote;
  }



  /**
   * Returns the BoardPainter of this JBoard.
   */

  public BoardPainter getBoardPainter(){
    return boardPainter;
  }



  /**
   * Returns the PiecePainter of this JBoard.
   */

  public PiecePainter getPiecePainter(){
    return piecePainter;
  }



  /**
   * Sets the BoardPainter for this JBoard. Passing <code>null</code> is
   * equivalent to setting the board painter to the default one specified by the
   * current position's variant.
   */

  public void setBoardPainter(BoardPainter boardPainter){
    if (boardPainter == null)
      boardPainter = position.getVariant().createDefaultBoardPainter();
    
    Object oldBoardPainter = this.boardPainter;
    this.boardPainter = boardPainter;
    repaint();
    firePropertyChange("boardPainter", oldBoardPainter, boardPainter);
  } 



  /**
   * Sets the PiecePainter for this JBoard. Passing <code>null</code> is
   * equivalent to setting the piece painter to the default one specified by the
   * current position's variant.
   */

  public void setPiecePainter(PiecePainter piecePainter){
    if (piecePainter == null)
      piecePainter = position.getVariant().createDefaultPiecePainter();
    
    Object oldPiecePainter = this.piecePainter;
    this.piecePainter = piecePainter;
    repaint();
    firePropertyChange("piecePainter", oldPiecePainter, piecePainter);
  }



  /**
   * Sets the color used for move highlighting to the specified color. Passing
   * <code>null</code> is equivalent to setting the color to the default one.
   * Note: this refers to highlighting the move specified by setHighlightedMove,
   * not the move being made by the user.
   */

  public void setMoveHighlightingColor(Color moveHighlightingColor){
    if (moveHighlightingColor == null)
      moveHighlightingColor = DEFAULT_MOVE_HIGHLIGHT_COLOR;
    
    Object oldColor = this.moveHighlightingColor;
    this.moveHighlightingColor = moveHighlightingColor;
    repaint();
    firePropertyChange("moveHighlightingColor", oldColor, moveHighlightingColor);
  }



  /**
   * Returns the color used for move highlighting.
   */

  public Color getMoveHighlightingColor(){
    return moveHighlightingColor;
  }
  
  
  
  /**
   * Sets the color used for coordinate display. Passing <code>null</code> is
   * equivalent to setting the color to the default one.
   */

  public void setCoordsDisplayColor(Color coordsDisplayColor){
    if (coordsDisplayColor == null)
      coordsDisplayColor = DEFAULT_COORDS_DISPLAY_COLOR;
    
    Object oldColor = this.coordsDisplayColor;
    this.coordsDisplayColor = coordsDisplayColor;
    repaint();
    firePropertyChange("coordsDisplayColor", oldColor, coordsDisplayColor);
  }



  /**
   * Returns the color used for coordinate display.
   */

  public Color getCoordsDisplayColor(){
    return coordsDisplayColor;
  }
  


  /**
   * Sets the color used for highlighting the squares of the move being made.
   * This refers to the highlighting specified by the
   * <code>highlightMadeMoveSquares</code> property. Passing <code>null</code>
   * is equivalent to setting it to the default color.
   */

  public void setMadeMoveSquaresHighlightColor(Color newColor){
    if (newColor == null)
      newColor = DEFAULT_MADE_MOVE_SQUARES_HIGHLIGHT_COLOR;
    
    Object oldColor = this.madeMoveSquaresHighlightColor;
    this.madeMoveSquaresHighlightColor = newColor;
    repaint();
    firePropertyChange("madeMoveSquaresHighlightColor", oldColor, newColor);
  }


  
  /**
   * Returns the color used for highlighting the squares of the move being made.
   */

  public Color getMadeMoveSquaresHighlightColor(){
    return madeMoveSquaresHighlightColor;
  }
  
  
  
  /**
   * Returns the duration of piece slide animation. A negative value indicates
   * that no sliding occurs.
   */
  
  public int getSlideDuration(){
    return slideDuration;
  }
  
  
  
  /**
   * Sets the duration of piece slide animation. A negative value indicates that
   * no sliding should occur.
   */
  
  public void setSlideDuration(int slideDuration){
    int oldValue = this.slideDuration;
    
    this.slideDuration = slideDuration;
    
    firePropertyChange("slideDuration", oldValue, slideDuration);
  }



  /**
   * Sets the shaded state of the specified square.
   */

  public void setShaded(Square square, boolean isShaded){
    boolean oldState = this.isShaded[square.getFile()][square.getRank()];
    this.isShaded[square.getFile()][square.getRank()] = isShaded;
    if (oldState != isShaded)
      repaint(squareToRect(square, null));
  }



  /**
   * Sets all the squares to the unshaded state.
   */

  public void clearShaded(){
    Rectangle helpRect = new Rectangle();
    for (int file = 0; file < 8; file++)
      for (int rank = 0; rank < 8; rank++){
        boolean oldState = isShaded[file][rank];
        isShaded[file][rank] = false;
        if (oldState)
          repaint(squareToRect(file, rank, helpRect));
      }
  }



  /**
   * Returns <code>true</code> iff the specified square is shaded.
   */

  public boolean isShaded(Square square){
    return isShaded[square.getFile()][square.getRank()];
  }



  /**
   * Returns <code>true</code> iff a piece is currently being moved/dragged.
   */

  public boolean isMovingPiece(){
    return movedPieceSquare != null;
  }

  

  /**
   * Paints this JBoard on the given Graphics object.
   */

  @Override
  public void paintComponent(Graphics graphics){
    super.paintComponent(graphics);
    
    Rectangle originalClip = graphics.getClipBounds();
    
    // The documentation of JComponent#paintComponent(Graphics) says we
    // shouldn't make permanent changes to the Graphics object, but we want to
    // clip it.
    Graphics2D g = (Graphics2D)graphics.create();
    
    g.setColor(getBackground());
    g.fillRect(0, 0, getWidth(), getHeight());
    
    Rectangle rect = null; // Helper rect - reused many times 
    
    rect = getBoardRect(rect);
    g.clipRect(rect.x, rect.y, rect.width, rect.height);
    Rectangle clipRect = g.getClipBounds();

    Position displayedPosition = slideStartSquare == null ? getPosition() : positionCopy;
    BoardPainter boardPainter = getBoardPainter();
    PiecePainter piecePainter = getPiecePainter();

    boolean isPieceFollowsCursor = isPieceFollowsCursor();
    boolean isHighlightMadeMoveSquares = isHighlightMadeMoveSquares(); 
    int moveHighlightingStyle = getMoveHighlightingStyle();

    // Paint the board
    boardPainter.paintBoard(g, this, rect.x, rect.y, rect.width, rect.height);

    // Paint the stationary pieces
    for (int file = 0; file < 8; file++){
      for (int rank = 0; rank < 8; rank++){
        Square curSquare = Square.getInstance(file, rank);

        if (isPieceFollowsCursor && curSquare.equals(movedPieceSquare))
          continue;
        
        if (isShowShadowPieceInTargetSquare && curSquare.equals(targetSquare))
          continue;
        
        if (curSquare.equals(slideStartSquare))
          continue;
        
        Piece piece = displayedPosition.getPieceAt(curSquare);
        if (piece == null)
          continue;

        squareToRect(curSquare, rect);
        if (!rect.intersects(clipRect))
          continue;

        piecePainter.paintPiece(piece, g, this, rect, isShaded(curSquare));
      }
    }
    
    // Paint the taken piece during a slide.
    if (slideTakenPiece != null){
      squareToRect(slideEndSquare, rect);
      if (rect.intersects(clipRect))
        piecePainter.paintPiece(slideTakenPiece, g, this, rect, isShaded(slideEndSquare));
    }
    
    // Paint possible target squares
    if (isHighlightLegalTargetSquares && (legalTargetSquares != null)){
      g.setColor(legalTargetSquaresHighlightColor);
      for (Iterator i = legalTargetSquares.iterator(); i.hasNext();){
        rect = squareToRect((Square)i.next(), rect);
        if (rect.intersects(clipRect))
          g.fill(rect);
      }
    }

    // Paint move highlighting
    if ((moveHighlightingStyle != NO_MOVE_HIGHLIGHTING) && (highlightedMove != null)){
      Square from = highlightedMove.getStartingSquare();
      Square to = highlightedMove.getEndingSquare();
      
      squareToRect(Square.getInstance(0, 0), rect); // Just a sample square
      int highlightSize = Math.max(2, Math.min(rect.width, rect.height)/12);
      if ((from != null) && (to != null)){
        if (moveHighlightingStyle == BOTH_SQUARES_MOVE_HIGHLIGHTING){
          drawSquare(g, from, highlightSize - Math.max(1, highlightSize/3), getMoveHighlightingColor());
          drawSquare(g, to, highlightSize, getMoveHighlightingColor());
        }
        else if (moveHighlightingStyle == ARROW_MOVE_HIGHLIGHTING)
          drawArrow(g, from, to, highlightSize+1, getMoveHighlightingColor());
      }
      if ((to != null) && (moveHighlightingStyle == TARGET_SQUARE_MOVE_HIGHLIGHTING))
        drawSquare(g, to, highlightSize, getMoveHighlightingColor());
    }

    // Paint the coordinates. Reset the original clip because of
    // OUTSIDE_COORDS mode, where the coordinates are drawn outside of our
    // usual clip rectangle.
    rect = g.getClipBounds();
    g.setClip(originalClip);
    drawCoords(graphics);
    g.setClip(rect);
    
    // Allow PaintHooks to paint
    callPaintHooks(g);
    
    // Paint the sliding piece
    if (slidePiece != null){
      double totalSlideTime = getSlideDuration();
      rect = slideRect(slideTime/totalSlideTime, rect);
      piecePainter.paintPiece(slidePiece, g, this, rect, false);
    }
    
    // Paint stuff drawn during a move
    if (movedPieceSquare != null){
      
      // Paint shadow piece in target square.
      if (isShowShadowPieceInTargetSquare && (targetSquare != null)){
        squareToRect(targetSquare, rect);
        Piece piece = displayedPosition.getPieceAt(movedPieceSquare);
        piecePainter.paintPiece(piece, g, this, rect, true);
      }
      
      // Paint target square highlight 
      if (isHighlightMadeMoveSquares){
        squareToRect(Square.getInstance(0, 0), rect); // Just a sample square
        
        int targetHighlightSize = Math.max(2, Math.min(rect.width, rect.height)/15);
        int originHighlightSize = Math.min(2*targetHighlightSize/3, targetHighlightSize - 1);
        
        drawSquare(g, movedPieceSquare, originHighlightSize, getMadeMoveSquaresHighlightColor());
        if (targetSquare != null)
          drawSquare(g, targetSquare, targetHighlightSize, getMadeMoveSquaresHighlightColor());
      }
      
      // Paint moved piece
      if (isPieceFollowsCursor){
        getMovedPieceGraphicRect(rect);
        Piece piece = displayedPosition.getPieceAt(movedPieceSquare);
        piecePainter.paintPiece(piece, g, this, rect, false);
      }
    }
  }
  
  
  
  /**
   * Returns the rectangle in which the sliding piece should be drawn at the
   * specified progress in the sliding (a number between 0 and 1).
   */
  
  private Rectangle slideRect(double progress, Rectangle rect){
    rect = squareToRect(slideStartSquare, rect);
    int startX = rect.x;
    int startY = rect.y;
    
    rect = squareToRect(slideEndSquare, rect);
    int endX = rect.x;
    int endY = rect.y;
    
    rect.x = (int)(startX + (endX - startX) * progress);
    rect.y = (int)(startY + (endY - startY) * progress);
    
    return rect;
  }
  


  /**
   * The font we use for drawing coordinates. The size might be different in the
   * actual drawing though.
   */
   
  private static final Font COORDS_FONT = new Font("Monospaced", Font.BOLD, 10); 
                                                   
                                                   
  
  /**
   * Draws the coordinates.
   */
   
  private void drawCoords(Graphics g){
    g.setColor(getCoordsDisplayColor());
    
    switch (getCoordsDisplayStyle()){
      case NO_COORDS: break;
      case RIM_COORDS: drawRimCoords(g); break;
      case OUTSIDE_COORDS: drawOutsideCoords(g); break;
      case EVERY_SQUARE_COORDS: drawEverySquareCoords(g); break;
      default:
        throw new IllegalStateException("Unknown coordinates display style value: " + getCoordsDisplayStyle());
    }
  }
  
  
  
  /**
   * Draws the coordinates for <code>RIM_COORDS</code> style. 
   */
   
  private void drawRimCoords(Graphics g){
    Rectangle boardRect = getBoardRect(null);
    int squareWidth = boardRect.width/8;
    int squareHeight = boardRect.height/8;
    
    Rectangle clipRect = g.getClipBounds();
    
    int fontSize = Math.max(Math.min(squareWidth, squareHeight)/4, 8);
    Font font = new Font(COORDS_FONT.getName(), Font.BOLD, fontSize);
    g.setFont(font);
    
    FontMetrics fm = g.getFontMetrics(font);
    int fontWidth = fm.stringWidth("a");
    int fontHeight = fm.getMaxAscent() + fm.getMaxDescent();

    int dir = isFlipped() ? 1 : -1;

    // Row coordinates
    if (clipRect.intersects(new Rectangle(boardRect.x, boardRect.y, squareWidth/2, boardRect.height))){
      char row = isFlipped() ? '1' : '8';
      for (int i = 0; i < 8; i++){
        g.drawString(String.valueOf(row),
          boardRect.x + 3, boardRect.y + i*squareHeight + fontHeight);
        row += dir;
      }
    }

    // Column coordinates
    if (clipRect.intersects(new Rectangle(boardRect.x, boardRect.y + boardRect.height - squareHeight/2,
                                          boardRect.width, squareHeight/2))){
      char col = isFlipped() ? 'h' : 'a';
      for (int i = 0; i < 8; i++){
        g.drawString(String.valueOf(col),
          boardRect.x + (i+1)*squareWidth - fontWidth - 3, boardRect.y + boardRect.height - 3);
        col -= dir;
      }
    }
  }
  
  
  
  /**
   * Draws the coordinates for <code>OUTSIDE_COORDS</code> style.
   */
   
  private void drawOutsideCoords(Graphics g){
    Insets insets = getInsets();    
    Rectangle boardRect = getBoardRect(null);
    int squareWidth = boardRect.width/8;
    int squareHeight = boardRect.height/8;
    
    Rectangle clipRect = g.getClipBounds();
    
    // IMPORTANT: If you modify this, you need to modify getBoardRect too
    int textWidth = squareWidth/3;
    int textHeight = squareHeight/3;
    
    int fontSize = Math.max(Math.min(textWidth, textHeight), 8);
    Font font = new Font(COORDS_FONT.getName(), COORDS_FONT.getStyle(), fontSize);
    g.setFont(font);
    
    FontMetrics fm = g.getFontMetrics(font);
    int fontWidth = fm.stringWidth("a");
    
    int dir = isFlipped() ? 1 : -1;
    
    // Row coordinates
    if (clipRect.intersects(new Rectangle(insets.left, insets.top, boardRect.x - insets.left, boardRect.height))){
      char row = isFlipped() ? '1' : '8';
      for (int i = 0; i < 8; i++){
        g.drawString(String.valueOf(row),
          insets.left + (boardRect.x - insets.left - fontWidth + 1)/2,
          boardRect.y + i*squareHeight + (squareHeight + fm.getAscent())/2 - 1);
        row += dir;
      }
    }
    
    // Column coordinates
    int bottomBorderHeight = getHeight() - boardRect.y - boardRect.height - insets.bottom;
    if (clipRect.intersects(new Rectangle(boardRect.x, boardRect.y + boardRect.height, boardRect.width, bottomBorderHeight))){
      char col = isFlipped() ? 'h' : 'a';
      for (int i = 0; i < 8; i++){
        g.drawString(String.valueOf(col),
          boardRect.x + i*squareWidth + (squareWidth - fontWidth)/2,
          boardRect.y + boardRect.height + (bottomBorderHeight + fm.getAscent())/2 - 1); 
        col -= dir;
      }
    }
  }
  
  
  
  /**
   * Draws the coordinates for <code>EVERY_SQUARE_COORDS</code> style.
   */
   
  private void drawEverySquareCoords(Graphics g){
    Rectangle boardRect = getBoardRect(null);
    int squareWidth = boardRect.width/8;
    int squareHeight = boardRect.height/8;
    
    Rectangle clipRect = g.getClipBounds();
    
    int fontSize = Math.max(Math.min(squareWidth, squareHeight)/5, 8);
    
    Font font = new Font(COORDS_FONT.getName(), COORDS_FONT.getStyle(), fontSize);
    g.setFont(font);
    
    FontMetrics fm = g.getFontMetrics(font);
    int fontHeight = fm.getMaxAscent() + fm.getMaxDescent();
    
    Rectangle rect = new Rectangle(boardRect.x, boardRect.y, squareWidth, squareHeight);

    int dir = isFlipped() ? 1 : -1;
    char row = isFlipped() ? '1' : '8';    
    for (int i = 0; i < 8; i++){
      char col = isFlipped() ? 'h' : 'a';      
      for (int j = 0; j < 8; j++){
        rect.x = boardRect.x + j*squareWidth;
        rect.y = boardRect.y + i*squareHeight;
        
        if (clipRect.intersects(rect))
          g.drawString(String.valueOf(col) + String.valueOf(row), rect.x + 3, rect.y + fontHeight);
        
        col -= dir;
      }
      row += dir;
    }
  }



  /**
   * Draws an arrow of the given size between the two specified squares on the
   * given <code>Graphics</code> object using the specified color.
   */

  protected void drawArrow(Graphics graphics, Square from, Square to,
      float arrowSize, Color color){
    
    Graphics2D g = (Graphics2D)graphics.create();
    g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    
    Rectangle fromRect = squareToRect(from, null);
    Rectangle toRect = squareToRect(to, null);

    float fromX = fromRect.x + fromRect.width/2;
    float fromY = fromRect.y + fromRect.height/2;
    float toX = toRect.x + toRect.width/2;
    float toY = toRect.y + toRect.height/2;

    double angle = Math.atan2(toY - fromY, toX - fromX);
    double sin = Math.sin(angle);
    double cos = Math.cos(angle);
    
    float cosXarrowSize = (float)(cos*arrowSize);
    float sinXarrowSize = (float)(sin*arrowSize);
    float cosXhalfArrowSize = cosXarrowSize/2.0f;
    float sinXhalfArrowSize = sinXarrowSize/2.0f;
    
    toX -= (int)(cos*toRect.width/2);
    toY -= (int)(sin*toRect.height/2);
    
    GeneralPath path = new GeneralPath();
    
    path.moveTo(fromX + sinXhalfArrowSize, fromY - cosXhalfArrowSize);
    path.lineTo(fromX + cosXhalfArrowSize*0.6f, fromY + sinXhalfArrowSize*0.6f);
    path.lineTo(fromX - sinXhalfArrowSize, fromY + cosXhalfArrowSize);
    path.lineTo(
        toX - cosXarrowSize*1.2f - sinXhalfArrowSize, 
        toY - sinXarrowSize*1.2f + cosXhalfArrowSize);
    path.lineTo(
        (float)(toX + Math.cos(angle + Math.PI*3/4) * arrowSize*3),
        (float)(toY + Math.sin(angle + Math.PI*3/4) * arrowSize*3));
    path.lineTo(toX + cosXarrowSize, toY + sinXarrowSize);
    path.lineTo(
        (float)(toX + Math.cos(angle - Math.PI*3/4) * arrowSize*3),
        (float)(toY + Math.sin(angle - Math.PI*3/4) * arrowSize*3));
    path.lineTo(
        toX - cosXarrowSize*1.2f + sinXhalfArrowSize, 
        toY - sinXarrowSize*1.2f - cosXhalfArrowSize);
    
    path.closePath();
    
    g.setColor(color);
    g.fill(path);
  }



  /**
   * Draws an outline of a square of the given size at the specified square on
   * the given <code>Graphics</code> object with the specific color. The size
   * specifies the width of the outline.
   */

  protected void drawSquare(Graphics graphics, Square circleSquare, int size, Color color){
    Graphics2D g = (Graphics2D)graphics;
    
    Rectangle rect = squareToRect(circleSquare, null);

    g.translate(rect.x, rect.y);
    
    float halfSize = size/2.0f;
    
    GeneralPath path = new GeneralPath();
    path.moveTo(halfSize, halfSize);
    path.lineTo(rect.width - halfSize, halfSize);
    path.lineTo(rect.width - halfSize, rect.height - halfSize);
    path.lineTo(halfSize, rect.height - halfSize);
    path.closePath();
    
    // Mac OS X draws it differently from other platforms for some reason
    if (PlatformUtils.isMacOSX())
      path.transform(AffineTransform.getTranslateInstance(-0.5, -0.5));
    
    Color oldColor = g.getColor();
    Stroke oldStroke = g.getStroke();
    
    g.setColor(color);
    g.setStroke(new BasicStroke(size, BasicStroke.CAP_ROUND, BasicStroke.JOIN_BEVEL));
    
    g.draw(path);
    
    g.setColor(oldColor);
    g.setStroke(oldStroke);
    
    g.translate(-rect.x, -rect.y);
  }



  /**
   * Calls all the registered PaintHooks.
   */

  private void callPaintHooks(Graphics g){
    int size = paintHooks == null ? 0 : paintHooks.size();
    for (int i = 0; i < size; i++){
      PaintHook hook = (PaintHook)paintHooks.elementAt(i);
      hook.paint(this, g);
    }
  }
  
  
  
  /**
   * Returns the rectangle actually occupied by the board, without the borders
   * or anything else that surrounds the board.
   */
   
  public Rectangle getBoardRect(Rectangle rect){
    if (rect == null)
      rect = new Rectangle();
    
    Insets insets = getInsets();
   
    rect.x = insets.left;
    rect.y = insets.top;
    rect.width = getWidth() - insets.left - insets.right;
    rect.height = getHeight() - insets.top - insets.bottom;
    
    if (getCoordsDisplayStyle() == OUTSIDE_COORDS){
      int w = rect.width/24;
      int h = rect.height/24;
      
      rect.x += w;
      rect.width -= w;
      rect.height -= h;
    }
    
    rect.width -= rect.width%8;
    rect.height -= rect.height%8;
    
    return rect;
  }

  

  /**
   * Returns the rectangle (in pixels) of the given square.
   */

  public Rectangle squareToRect(Square square, Rectangle squareRect){
    return squareToRect(square.getFile(), square.getRank(), squareRect);
  }



  /**
   * Returns the rectangle (in pixels) of the given square.
   */

  public Rectangle squareToRect(int file, int rank, Rectangle squareRect){
    squareRect = getBoardRect(squareRect);

    squareRect.width /= 8;
    squareRect.height /= 8;
    
    if (isFlipped()){
      squareRect.x += (7-file)*squareRect.width;
      squareRect.y += rank*squareRect.height;
    }
    else{
      squareRect.x += file*squareRect.width;
      squareRect.y += (7-rank)*squareRect.height;
    }

    return squareRect;
  }
  
  
  
  /**
   * Calculates a rectangle (in pixels) which completely contains the graphic of
   * the piece currently being moved, if we are in
   * <code>pieceFollowsCursor</code> mode.
   * Throws an <code>IllegalStateException</code> if we are not in in
   * <code>pieceFollowsCursor</code> mode or if a piece is not currently being
   * moved.
   */
   
  private Rectangle getMovedPieceGraphicRect(Rectangle rect){
    if (!isPieceFollowsCursor())
      throw new IllegalStateException("Not in pieceFollowsCursor mode");
    
    if (movedPieceLoc == null)
      throw new IllegalStateException("No piece is being moved");
    
    rect = getBoardRect(rect);
    int squareWidth = rect.width/8;
    int squareHeight = rect.height/8;
    rect.x = movedPieceLoc.x - squareWidth/2;
    rect.y = movedPieceLoc.y - squareHeight/2;
    rect.width = squareWidth;
    rect.height = squareHeight;
    
    return rect;
  }
  
  
  
  /**
   * Calculates the target square during a move based on the specified cursor
   * location.
   */
   
  private Square calcTargetSquare(Point cursorLocation){
    if (isSnapToLegalSquare && (legalTargetSquares != null)){
      // Check the usual case - square under the cursor
      Square cursorSquare = locationToSquare(cursorLocation);
      if (movedPieceSquare.equals(cursorSquare) || legalTargetSquares.contains(cursorSquare))
        return cursorSquare;
      
      Rectangle rect = squareToRect(0, 0, null);
      int minDistanceSquared = // We don't want squares which are too far.
        MathUtilities.sqr((int)(1.5*Math.max(rect.width, rect.height))); 
      Square nearestSquare = null;
      for (Iterator i = legalTargetSquares.iterator(); i.hasNext();){
        Square square = (Square)i.next();
        squareToRect(square, rect);
        
        int dx = cursorLocation.x - (rect.x + rect.width/2);
        int dy = cursorLocation.y - (rect.y + rect.height/2);
        int distanceSquared = dx*dx + dy*dy;
        
        if (distanceSquared < minDistanceSquared){
          minDistanceSquared = distanceSquared;
          nearestSquare = square;
        }
      }
      
      return nearestSquare;
    }
    else
      return locationToSquare(cursorLocation);
  }
   
  


  /**
   * Returns a rectangle (in pixels) which completely covers the area that needs
   * to be redrawn when a piece is being moved.
   * Throws an <code>IllegalStateException</code> if a piece is not currently
   * being moved.
   */

  private Rectangle getMoveAreaRect(Rectangle rect){
    if (movedPieceLoc == null)
      throw new IllegalStateException("No piece is being moved");
    
    if (isPieceFollowsCursor()){
      if (isHighlightMadeMoveSquares() || isShowShadowPieceInTargetSquare()){
        if (rect == null)
          rect = new Rectangle();
        
        Rectangle pieceGraphic = getMovedPieceGraphicRect(null);
        Rectangle targetRect = targetSquare == null ? null : squareToRect(targetSquare, null);
        
        if (targetRect == null)
          rect.setBounds(pieceGraphic);
        else
          rect.setBounds(pieceGraphic.union(targetRect));
        
        return rect;
      }
      else
        return getMovedPieceGraphicRect(rect);
    }
    else{
      if (isHighlightMadeMoveSquares() || isShowShadowPieceInTargetSquare())
        return squareToRect(targetSquare, rect);
      else{
        if (rect == null)
          return new Rectangle(0,0,0,0);
        
        rect.setBounds(0,0,0,0);
        return rect;
      }
    }
  }


  
  /**
   * Returns the square corresponding to the given coordinate (in pixels).
   * Returns null if the given location is not on the visible board.
   */

  public Square locationToSquare(int x, int y){
    Rectangle boardRect = getBoardRect(null);
    x -= boardRect.x;
    y -= boardRect.y;
    
    if ((x < 0) || (y < 0))
      return null;

    int squareWidth = boardRect.width/8;
    int squareHeight = boardRect.height/8;
    int file = x/squareWidth;
    int rank = 7-(y/squareHeight);
    if ((file > 7) || (rank < 0))
      return null;

    if (isFlipped())
      return Square.getInstance(7-file, 7-rank);
    else
      return Square.getInstance(file, rank);
  }



  /**
   * Returns the square corresponding to the given coordinate (in pixels).
   */

  public Square locationToSquare(Point p){
    return locationToSquare(p.x, p.y);
  }
  


  /**
   * Makes sure no events are dispatched to this JBoard while its showing a modal
   * dialog.
   */

  @Override
  protected void processEvent(AWTEvent evt){
    if (!isShowingModalDialog)
      super.processEvent(evt);
  }



  /**
   * Returns whether the given piece can be moved, considering the current move
   * mode.
   */

  private boolean canBeMoved(Piece piece){
    switch(getMoveInputMode()){
      case NO_PIECES_MOVE:
        return false;
      case WHITE_PIECES_MOVE:
        return piece.isWhite();
      case BLACK_PIECES_MOVE:
        return piece.isBlack();
      case ALL_PIECES_MOVE:
        return true;
      case CURRENT_PLAYER_MOVES:
        Player player = getPosition().getCurrentPlayer();
        return (player.isWhite()&&piece.isWhite())||(player.isBlack()&&piece.isBlack());
    }
    throw new IllegalStateException();
  }
  
  
  
  /**
   * Returns whether we are currently performing the specified gesture or are
   * simply in the specified move input style. This is just a helper method.
   */
  
  private boolean isGesture(int style){
    int moveInputStyle = getMoveInputStyle();
    
    return ((moveInputStyle == UNIFIED_MOVE_INPUT_STYLE) && (moveGesture == style)) ||
        (moveInputStyle == style);
  }
  
  
  
  /**
   * Causes the possible target squares to be repainted.
   */
  
  private void repaintLegalTargetSquares(Rectangle helpRect){
    for (Iterator i = legalTargetSquares.iterator(); i.hasNext();)
      repaint(helpRect = squareToRect((Square)i.next(), helpRect));
  }
  
  
  
  /**
   * If a piece is currently being moved/dragged, the moving/dragging is
   * canceled and the moved/dragged piece is returned to its original location.
   * If no piece is currently being moved/dragged, an
   * <code>IllegalStateException</code> is thrown.
   */

  public void cancelMovingPiece(){
    if (!isMovingPiece())
      throw new IllegalStateException();
    
    Rectangle rect = new Rectangle();
    repaint(getMoveAreaRect(rect));
    repaint(squareToRect(movedPieceSquare, rect));
    if (targetSquare != null)
      repaint(squareToRect(targetSquare, rect));
    if (legalTargetSquares != null)
      repaintLegalTargetSquares(rect);
    
    movedPieceSquare = null;
    movedPieceLoc = null;
    targetSquare = null;
    legalTargetSquares = null;
    
    fireMoveProgressEvent(new MoveProgressEvent(this, MoveProgressEvent.MOVE_MAKING_ENDED));
  }
  
  
  
  /**
   * Processes a mouse event.
   */

  @Override
  protected void processMouseEvent(MouseEvent evt){
    super.processMouseEvent(evt);

    if (!(isEnabled() && isEditable()))
      return;
    
    // A clean left mouse event - no other modifiers
    boolean isLeftMouseButton = 
      (evt.getButton() == MouseEvent.BUTTON1) && 
      ((evt.getModifiersEx() & (~InputEvent.BUTTON1_DOWN_MASK)) == 0);

    int evtID = evt.getID();

    int x = evt.getX();
    int y = evt.getY();
    
    Rectangle helpRect = new Rectangle();

    if ((evtID == MouseEvent.MOUSE_EXITED) && isGesture(CLICK_N_CLICK_MOVE_INPUT_STYLE)){
      if (movedPieceSquare != null){ // Fake the piece being at its original location
        repaint(getMoveAreaRect(helpRect));
        squareToRect(movedPieceSquare, helpRect);
        movedPieceLoc.x = helpRect.x + helpRect.width/2;
        movedPieceLoc.y = helpRect.y + helpRect.height/2;
        targetSquare = null;
        repaint(getMoveAreaRect(helpRect));
      }
    }

    Square square = locationToSquare(x,y);

    if (square == null){
      if ((evtID == MouseEvent.MOUSE_RELEASED) && isMovingPiece())
        cancelMovingPiece();
      return;
    }

    if (isLeftMouseButton && ((evtID == MouseEvent.MOUSE_PRESSED) ||
       ((evtID == MouseEvent.MOUSE_RELEASED) && isGesture(DRAG_N_DROP_MOVE_INPUT_STYLE)))){
      if (movedPieceSquare == null){

        // This happens if the user tries to drag an empty square into a piece.
        if (evtID == MouseEvent.MOUSE_RELEASED) 
          return;

        movedPieceSquare = square;
        Piece piece = position.getPieceAt(movedPieceSquare);
        if ((piece == null) || (!canBeMoved(piece))){
          movedPieceSquare = null;
          movedPieceLoc = null;
          return;
        }
        movedPieceLoc = new Point(x, y);
        
        if (isHighlightLegalTargetSquares() || isSnapToLegalSquare())
          legalTargetSquares = position.getTargetSquares(movedPieceSquare);
        
        targetSquare = calcTargetSquare(movedPieceLoc);
        
        repaint(squareToRect(square, helpRect));
        repaint(getMoveAreaRect(helpRect));
        if (isHighlightLegalTargetSquares())
          repaintLegalTargetSquares(helpRect);
        
        if (getMoveInputStyle() == UNIFIED_MOVE_INPUT_STYLE){
          moveGesture = DRAG_N_DROP_MOVE_INPUT_STYLE;
          dragStartTime = evt.getWhen();
        }
       
        fireMoveProgressEvent(new MoveProgressEvent(this, MoveProgressEvent.MOVE_MAKING_STARTED));
      }
      else{
        if (targetSquare == null){ // Moved to a non-target square
          
        }
        else if (!targetSquare.equals(movedPieceSquare)){
          WildVariant variant = position.getVariant();
          Piece [] promotionTargets = variant.getPromotionTargets(position, movedPieceSquare, targetSquare);
          Move madeMove;
          if (promotionTargets != null){
            Piece promotionTarget;
            if (isManualPromote()){
              isShowingModalDialog = true;
              promotionTarget = PieceChooser.showPieceChooser(this, x, y, promotionTargets, getPiecePainter(), promotionTargets[0]);
              isShowingModalDialog = false;
            }
            else
              promotionTarget = promotionTargets[0];

            madeMove = variant.createMove(position, movedPieceSquare, targetSquare, promotionTarget, null);
          }
          else
            madeMove = variant.createMove(position, movedPieceSquare, targetSquare, null, null);
          
          isMakingUserMove = true;
          position.makeMove(madeMove);
          isMakingUserMove = false;
        }
        else if ((getMoveInputStyle() == UNIFIED_MOVE_INPUT_STYLE) &&
            (moveGesture == DRAG_N_DROP_MOVE_INPUT_STYLE) && 
            (evt.getWhen() - dragStartTime < 300)){
          moveGesture = CLICK_N_CLICK_MOVE_INPUT_STYLE;
          return;
        }
        else{ // Picked up the piece and dropped it immediately.
          
        }
        
        repaint(getMoveAreaRect(helpRect));
        
        repaint(squareToRect(movedPieceSquare, helpRect));
        if (targetSquare != null)
          repaint(squareToRect(targetSquare, helpRect));
        
        if (isHighlightLegalTargetSquares())
          repaintLegalTargetSquares(helpRect);

        movedPieceSquare = null;
        movedPieceLoc = null;
        targetSquare = null;
        legalTargetSquares = null;
        moveGesture = 0;
        
        fireMoveProgressEvent(new MoveProgressEvent(this, MoveProgressEvent.MOVE_MAKING_ENDED));
      }
    }
  }



  /**
   * Processes a mouse motion event.
   */

  @Override
  protected void processMouseMotionEvent(MouseEvent evt){
    super.processMouseMotionEvent(evt);

    if (!(isEnabled() && isEditable()))
      return;

    int evtID = evt.getID();

    if ((evtID == MouseEvent.MOUSE_DRAGGED) && !SwingUtilities.isLeftMouseButton(evt)) 
      return;

    if (movedPieceSquare == null)
      return;

    int x = evt.getX();
    int y = evt.getY();

    Rectangle helpRect = null;

    if ((evtID == MouseEvent.MOUSE_DRAGGED) ||
       ((evtID == MouseEvent.MOUSE_MOVED) && isGesture(CLICK_N_CLICK_MOVE_INPUT_STYLE))){
      repaint(helpRect = getMoveAreaRect(helpRect));
      
      if ((locationToSquare(x, y) == null) && isGesture(CLICK_N_CLICK_MOVE_INPUT_STYLE)){
        // Fake the piece being at its original location
        squareToRect(movedPieceSquare, helpRect);
        movedPieceLoc.x = helpRect.x + helpRect.width/2;
        movedPieceLoc.y = helpRect.y + helpRect.height/2;
        targetSquare = null;
      }
      else{
        movedPieceLoc.x = x;
        movedPieceLoc.y = y;
        targetSquare = calcTargetSquare(movedPieceLoc);
      }
      repaint(helpRect = getMoveAreaRect(helpRect));
    }
  }



  /**
   * Displays a simple <code>JFrame</code> with a <code>JBoard</code>.
   */

  public static void main(String [] args){
    javax.swing.JFrame frame = new javax.swing.JFrame("JBoard Test");
    frame.addWindowListener(new free.util.AppKiller());
    frame.getContentPane().setLayout(new java.awt.BorderLayout());
    final JBoard board = new JBoard();
//    board.setShowShadowPieceInTargetSquare(true);
    board.setSnapToLegalSquare(true);
    frame.getContentPane().add(board, java.awt.BorderLayout.CENTER);
    frame.setBounds(50, 50, 400, 400);
    frame.setVisible(true);
  }


  
}
