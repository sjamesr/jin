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

package free.chess;

import java.awt.*;
import java.awt.event.MouseEvent;
import javax.swing.JComponent;
import javax.swing.RepaintManager;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.util.Vector;
import free.util.PaintHook;
import free.util.Utilities;


/**
 * An implementation of a chess board component.
 * <B>IMPORTANT:</B> This class is not thread safe - all modifications should be
 * done in the AWT event dispatching thread.
 */

public class JBoard extends JComponent{


  /**
   * The constant for drag'n'drop move input style.
   */

  public static final int DRAG_N_DROP = 1;



  /**
   * The constant for click'n'click move input style.
   */

  public static final int CLICK_N_CLICK = 2;




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
   * The constant for regular dragged piece style (the piece is being dragged).
   */

  public static final int NORMAL_DRAGGED_PIECE = 0;




  /**
   * The constant for dragged piece style where no piece is being shown, but 
   * instead the cursor becomes CROSSHAIR_CURSOR while dragging.
   */

  public static final int CROSSHAIR_DRAGGED_PIECE = 1;




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
   * The ChangeListener to the Position.
   */

  private ChangeListener positionChangeListener = new ChangeListener(){
    
    public void stateChanged(ChangeEvent evt){
      // Repaint only the parts that really need to be repainted by checking
      // which squares changed.
      Dimension size = getSize();
      boolean checkMovingPieceSquare = (movedPieceSquare != null);
      Rectangle tmpRect = new Rectangle();


      // Repaint the dragged piece position.
      if (checkMovingPieceSquare)
        repaint(tmpRect = getMovedPieceRect(tmpRect));

      for (int file = 0; file < 8; file++){
        for (int rank = 0; rank < 8; rank++){
          Piece oldPiece = positionCopy.getPieceAt(file, rank);
          Piece newPiece = position.getPieceAt(file, rank);

          // We don't need to repaint the origin square of the moving piece.
          if (checkMovingPieceSquare && (file == movedPieceSquare.getFile()) && (rank == movedPieceSquare.getRank())){
            checkMovingPieceSquare = false;
            continue;
          }

          if (!Utilities.areEqual(oldPiece, newPiece)){
            tmpRect = squareToRect(file, rank, tmpRect);
            repaint(tmpRect);
          }
        }
      }

      if (movedPieceSquare != null){                        // We were dragging a piece
        if (position.getPieceAt(movedPieceSquare) == null){ // But the piece we were dragging is no longer there
          movedPieceSquare = null;
          movedPieceLoc = null;
        }
      }

      positionCopy.copyFrom(position);
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

  private int moveInputStyle = DRAG_N_DROP;




  /**
   * The current move input mode.
   */

  private int moveInputMode = NO_PIECES_MOVE;




  /**
   * The current dragged piece style.
   */

  private int draggedPieceStyle = NORMAL_DRAGGED_PIECE;




  /**
   * Is the board flipped?
   */

  private boolean isFlipped = false;




  /**
   * Are we currently manual promotion mode?
   */

  private boolean isManualPromote = true;




  /**
   * The square of the currently moved/dragged piece, or null if none.
   */

  private Square movedPieceSquare = null;




  /**
   * The location of the cursor when a piece is moved/dragged, null if no piece
   * is currently moved/dragged.
   */

  private Point movedPieceLoc = null;




  /**
   * We use this image to cache whatever we draw.
   */

  private Image cacheImage;




  /**
   * True when we can use the cache image when painting, false when we cannot.
   */

  private boolean cacheImageUsable = false;





  /**
   * A boolean telling us whether we're currently showing the promotion target
   * selection dialog. This is needed to workaround the bug which keeps sending
   * events after show() has been called on a modal dialog but before it's
   * actually displayed.
   */

  private boolean isShowingModalDialog = false;




  /**
   * Creates a new JBoard with the given position set on it.
   */

  public JBoard(Position position){
    if (position == null)
      throw new IllegalArgumentException("The Position may not be null");

    setOpaque(true);
    setDoubleBuffered(false); // We're double buffering ourselves.

    setPosition(position);
    this.boardPainter = position.getVariant().createDefaultBoardPainter();
    this.piecePainter = position.getVariant().createDefaultPiecePainter();

    enableEvents(MouseEvent.MOUSE_EVENT_MASK|MouseEvent.MOUSE_MOTION_EVENT_MASK);
  }
  



  /**
   * Creates a new JBoard with the initial position set on it.
   */

  public JBoard(){
    this(new Position());
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
    if (newPosition==null)
      throw new IllegalArgumentException("Null position");

    Position oldPosition = position;
    if (position != null)
      position.removeChangeListener(positionChangeListener);
    position = newPosition;
    position.addChangeListener(positionChangeListener);

    if (positionCopy == null)
      positionCopy = new Position(position);
    else
      positionCopy.copyFrom(position);

    repaint();

    firePropertyChange("position", oldPosition, newPosition);
  }




  /**
   * Sets the move input style of this JBoard to the given style. Possible values
   * are {@link #DRAG_N_DROP} and {@link #CLICK_N_CLICK}.
   */

  public void setMoveInputStyle(int newStyle){
    switch(newStyle){
      case DRAG_N_DROP:
      case CLICK_N_CLICK:
        break;
      default:
        throw new IllegalArgumentException("Illegal move input style value: "+newStyle);
    }
    int oldStyle = moveInputStyle;
    moveInputStyle = newStyle;
    firePropertyChange("moveInputStyle",oldStyle, newStyle);
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
    firePropertyChange("moveInputMode",oldMode, newMode);
  }
  




  /**
   * Returns the current move input mode for this JBoard.
   */

  public int getMoveInputMode(){
    return moveInputMode;
  }




  /**
   * Sets the dragged piece style to the given style. Possible values are
   * {@link #NORMAL_DRAGGED_PIECE} and {@link #CROSSHAIR_DRAGGED_PIECE}.
   */

  public void setDraggedPieceStyle(int newStyle){
    switch(newStyle){
      case NORMAL_DRAGGED_PIECE:
      case CROSSHAIR_DRAGGED_PIECE:
        break;
      default:
        throw new IllegalArgumentException("Illegal dragged piece style value: "+newStyle);
    }

    this.draggedPieceStyle = newStyle;
  }




  /**
   * Returns the current dragged piece style.
   */

  public int getDraggedPieceStyle(){
    return draggedPieceStyle;
  }





  /**
   * Sets the board's flipped state. When the board is flipped, it displays the
   * black side at the bottom.
   */

  public void setFlipped(boolean isFlipped){
    this.isFlipped = isFlipped;
    repaint();
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

  public void setManualPromote(boolean manualPromote){
    this.isManualPromote = manualPromote;
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
   * Sets the BoardPainter for this JBoard.
   */

  public void setBoardPainter(BoardPainter boardPainter){
    this.boardPainter = boardPainter;
    repaint();
  } 



  /**
   * Sets the PiecePainter for this JBoard.
   */

  public void setPiecePainter(PiecePainter piecePainter){
    this.piecePainter = piecePainter;
    repaint();
  }



  
  /**
   * Paints this JBoard on the given Graphics object.
   */

  public void paintComponent(Graphics componentGraphics){
    if (cacheImageUsable){
      componentGraphics.drawImage(cacheImage, 0, 0, null);
      return;
    }

    Dimension size = getSize();

    if ((cacheImage == null) || (cacheImage.getWidth(null) != size.width) || (cacheImage.getHeight(null) != size.height)){
      if (cacheImage != null)
        cacheImage.flush();
      cacheImage = createImage(size.width, size.height);
    }

    Graphics cacheGraphics = cacheImage.getGraphics();
    Rectangle clipRect = componentGraphics.getClipBounds();
    cacheGraphics.clipRect(clipRect.x, clipRect.y, clipRect.width, clipRect.height);

    super.paintComponent(cacheGraphics);

    // Fill the borders outside the actual board.
    cacheGraphics.setColor(getBackground());
    cacheGraphics.fillRect(size.width - size.width%8, 0, size.width%8, size.height);
    cacheGraphics.fillRect(0, size.height - size.height%8, size.width, size.height%8);

    Rectangle squareRect = new Rectangle(0, 0, size.width/8, size.height/8);
    cacheGraphics.clipRect(0, 0, squareRect.width*8, squareRect.height*8);
    clipRect = cacheGraphics.getClipBounds();

    Position position = getPosition();
    BoardPainter boardPainter = getBoardPainter();
    PiecePainter piecePainter = getPiecePainter();

    int draggedPieceStyle = getDraggedPieceStyle();

    boardPainter.paintBoard(cacheGraphics, this, 0, 0, size.width, size.height);

    for (int file = 0; file < 8; file++)
      for (int rank = 0; rank < 8; rank++){
        Square curSquare = Square.getInstance(file, rank);

        if ((draggedPieceStyle == NORMAL_DRAGGED_PIECE) && curSquare.equals(movedPieceSquare))
          continue;

        Piece piece = position.getPieceAt(curSquare);
        if (piece == null)
          continue;

        squareRect = squareToRect(curSquare, squareRect);
        if (!squareRect.intersects(clipRect))
          continue;

        piecePainter.paintPiece(piece, cacheGraphics, this, squareRect.x, squareRect.y, squareRect.width, squareRect.height);
      }

    callPaintHooks(cacheGraphics);

    if (movedPieceSquare != null){
      getMovedPieceRect(squareRect);
      if (draggedPieceStyle == NORMAL_DRAGGED_PIECE){
        Piece piece = position.getPieceAt(movedPieceSquare);
        piecePainter.paintPiece(piece, cacheGraphics, this, squareRect.x, squareRect.y, squareRect.width, squareRect.height);
      }
      else if (draggedPieceStyle==CROSSHAIR_DRAGGED_PIECE){
        cacheGraphics.setColor(Color.blue);
        squareRect.width--;
        squareRect.height--;
        for (int i = 0; i < 2; i++){
          cacheGraphics.drawRect(squareRect.x, squareRect.y, squareRect.width, squareRect.height);
          squareRect.x++;
          squareRect.y++;
          squareRect.width-=2;
          squareRect.height-=2;

          if ((squareRect.width <= 0) || (squareRect.height <= 0))
            break;
        }
      }
    }

    componentGraphics.drawImage(cacheImage, 0, 0, null);
    cacheImageUsable = true;
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
   * Overrides Component.repaint(long, int, int, int, int) to invalidate the
   * cache image.
   */

  public void repaint(long time, int x, int y, int width, int height){
    cacheImageUsable = false;
    super.repaint(time, x, y, width, height);
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
    if (squareRect == null)
      squareRect = new Rectangle();

    squareRect.width = getWidth()/8;
    squareRect.height = getHeight()/8;
    if (isFlipped()){
      squareRect.x = (7-file)*squareRect.width;
      squareRect.y = rank*squareRect.height;
    }
    else{
      squareRect.x = file*squareRect.width;
      squareRect.y = (7-rank)*squareRect.height;
    }

    return squareRect;
  }




  /**
   * Returns a Rectangle (in pixels) of the piece currently being dragged, or
   * <code>null</code> if no piece is currently being dragged. The resulting
   * value is put in the specified Rectangle (unless the resulting value is
   * <code>null</code>, or the specified Rectangle is).
   */

  public Rectangle getMovedPieceRect(Rectangle rect){
    if (movedPieceLoc == null)
      return null;

    if (rect == null)
      rect = new Rectangle();

    int squareWidth = getWidth()/8;
    int squareHeight = getHeight()/8;

    if (getDraggedPieceStyle() == NORMAL_DRAGGED_PIECE){
      rect.x = movedPieceLoc.x - squareWidth/2;
      rect.y = movedPieceLoc.y - squareHeight/2;
    }
    else{
      rect.x = movedPieceLoc.x - movedPieceLoc.x%squareWidth;
      rect.y = movedPieceLoc.y - movedPieceLoc.y%squareHeight;
    }
    rect.width = squareWidth;
    rect.height = squareHeight;

    return rect;
  }




  /**
   * Returns the square corresponding to the given coordinate (in pixels).
   * Returns null if the given location is not on the visible board.
   */

  public Square locationToSquare(int x, int y){
    int squareWidth = getWidth()/8;
    int squareHeight = getHeight()/8;
    int file = x/squareWidth;
    int rank = 7-(y/squareHeight);
    if ((file>7)||(file<0)||(rank>7)||(rank<0))
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
   * Processes a mouse event.
   */

  protected void processMouseEvent(MouseEvent evt){
    super.processMouseEvent(evt);

    if (!isEnabled())
      return;

    if ((evt.getModifiers() & MouseEvent.BUTTON1_MASK) == 0) 
      return;

    int inputStyle = getMoveInputStyle();

    int evtID = evt.getID();

    Dimension size = getSize();
    int squareWidth = size.width/8;
    int squareHeight = size.height/8;
    int x = evt.getX();
    int y = evt.getY();
    int draggedPieceStyle = getDraggedPieceStyle();

    Square square = locationToSquare(x,y);

    if (square == null){
      if (evtID == MouseEvent.MOUSE_RELEASED){
        movedPieceSquare = null;
        movedPieceLoc = null;
        repaint();
      }
      return;
    }

    Rectangle helpRect = null;

    if ((evtID==MouseEvent.MOUSE_PRESSED)||((evtID==MouseEvent.MOUSE_RELEASED)&&(inputStyle==DRAG_N_DROP))){
      if (movedPieceSquare==null){
        if (evtID==MouseEvent.MOUSE_RELEASED) // This happens if the user tries to drag an empty square into a piece.
          return;
        movedPieceSquare = square;
        Piece piece = position.getPieceAt(movedPieceSquare);
        if ((piece == null) || (!canBeMoved(piece))){
          movedPieceSquare = null;
          return;
        }
        movedPieceLoc = new Point(x, y);

        if (draggedPieceStyle == CROSSHAIR_DRAGGED_PIECE)
          setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));

        repaint(helpRect = squareToRect(square, helpRect));
        if (draggedPieceStyle == NORMAL_DRAGGED_PIECE)
          repaint(helpRect = getMovedPieceRect(helpRect));

        // 17/02/2002 I can't see why this is needed, as "repaint(squareToRect(square, null))" already repaints the exact same area
        //            this call does.
//        else if (draggedPieceStyle==CROSSHAIR_DRAGGED_PIECE)
//          repaint(movedPieceLoc.x-movedPieceLoc.x%squareWidth, movedPieceLoc.y-movedPieceLoc.y%squareHeight, squareWidth, squareHeight);

      }
      else{
        movedPieceLoc.x = x;
        movedPieceLoc.y = y;

        if (!square.equals(movedPieceSquare)){
          WildVariant variant = position.getVariant();
          Piece [] promotionTargets = variant.getPromotionTargets(position, movedPieceSquare, square);
          Move madeMove;
          if (promotionTargets!=null){
            Piece promotionTarget;
            if (isManualPromote()){
              isShowingModalDialog = true;
              promotionTarget = PieceChooser.showPieceChooser(this, promotionTargets, piecePainter, promotionTargets[0]);
              isShowingModalDialog = false;
            }
            else
              promotionTarget = promotionTargets[0];

            madeMove = variant.createMove(position, movedPieceSquare, square, promotionTarget, null);
          }
          else
            madeMove = variant.createMove(position, movedPieceSquare, square, null, null);

          position.makeMove(madeMove);
        }
        else{ // Picked up the piece and left it immediately.
          repaint(helpRect = getMovedPieceRect(helpRect));
          repaint(helpRect = squareToRect(movedPieceSquare, helpRect));
        }

        movedPieceSquare = null;
        movedPieceLoc = null;
        if (draggedPieceStyle==CROSSHAIR_DRAGGED_PIECE)
          setCursor(Cursor.getDefaultCursor());
      }
    }

    RepaintManager repaintManager = RepaintManager.currentManager(this);
    paintImmediately(repaintManager.getDirtyRegion(this));
    repaintManager.markCompletelyClean(this);
  }




  /**
   * Processes a mouse motion event.
   */

  protected void processMouseMotionEvent(MouseEvent evt){
    super.processMouseMotionEvent(evt);

    if (!isEnabled())
      return;

    // Only respond to the left mouse button.
//    if ((evt.getModifiers() & MouseEvent.BUTTON1_MASK) == 0) 
//      return;
    // This doesn't work under Sun's JDK1.1, because evt.getModifiers() always returns
    // 0 for some reason. In any case, this check can be rather safely omitted
    // because there already is a check for the mouse button in processMouseEvent
    // which gives a (non null) value to movedPieceSquare which we check in this
    // method.

    int inputStyle = getMoveInputStyle();
    if (movedPieceSquare==null)
      return;

    int evtID = evt.getID();

    Dimension size = getSize();
    int squareWidth = size.width/8;
    int squareHeight = size.height/8;
    int x = evt.getX();
    int y = evt.getY();
    Square square = locationToSquare(x,y);

    if (square==null)
      return;

    Rectangle helpRect = null;

    if ((evtID==MouseEvent.MOUSE_DRAGGED)||((evtID==MouseEvent.MOUSE_MOVED)&&(inputStyle==CLICK_N_CLICK))){
      repaint(helpRect = getMovedPieceRect(helpRect));
      movedPieceLoc.x = x;
      movedPieceLoc.y = y;
      repaint(helpRect = getMovedPieceRect(helpRect));
    }

    RepaintManager repaintManager = RepaintManager.currentManager(this);
    paintImmediately(repaintManager.getDirtyRegion(this));
    repaintManager.markCompletelyClean(this);
  }




  /**
   * Returns the preferred size of this JBoard. The PiecePainter is consulted
   * when determining the preferred size. If it returns a Dimension that has
   * no area (width<=0 or height<=0), the BoardPainter is consulted. If it returns 
   * a Dimension with no area too, some acceptable size in which width==height 
   * is returned.
   */

  public Dimension getPreferredSize(){
    Dimension piecePrefSize = piecePainter.getPreferredPieceSize();
    if ((piecePrefSize.width<=0)||(piecePrefSize.height<=0)){
      Dimension boardPrefSize = boardPainter.getPreferredBoardSize();
      if ((boardPrefSize.width<=0)||(boardPrefSize.height<=0))
        return new Dimension(400,400);
      else return boardPrefSize;
    }
    else return new Dimension(piecePrefSize.width*8, piecePrefSize.height*8);
  }




  /**
   * Returns the minimum size of this JBoard. This method returns the minimum
   * reasonable size of a chess board - about 80x80.
   */

  public Dimension getMinimumSize(){
    return new Dimension(80,80);
  }


}
