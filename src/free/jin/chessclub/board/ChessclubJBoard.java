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


package free.jin.chessclub.board;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.util.Vector;
import free.chess.JBoard;
import free.chess.Position;
import free.chess.Square;
import free.util.PaintHook;
import free.jin.chessclub.board.event.ArrowCircleListener;



/**
 * An extension of JBoard which implements chessclub.com specific functionality
 * such as arrows, circles and (in the future) others.
 */

public class ChessclubJBoard extends JBoard implements PaintHook{


  
  /**
   * A Vector holding triplets of [fromSquare], [toSquare], [Color] for every
   * arrow on the board.
   */

  private final Vector arrows = new Vector(3);




  /**
   * A Vector holding couples of [circleSquare], [Color] for every circle on
   * the board.
   */

  private final Vector circles = new Vector(2);




  /**
   * The default arrow color.
   */

  private Color defaultArrowColor = Color.lightGray;
  



  /**
   * The default circle color.
   */

  private Color defaultCircleColor = Color.lightGray;




  /**
   * Is the arrow/circle adding functionality enabled (for the user).
   */

  private boolean isArrowCircleEnabled = true;




  /**
   * The temporary arrow's starting square.
   */

  private Square tmpArrowFrom = null;




  /**
   * The temporary arrow's ending square.
   */

  private Square tmpArrowTo = null;




  /**
   * Creates a new ChessclubJBoard with the given initial Position.
   */

  public ChessclubJBoard(Position initPosition){
    super(initPosition);

    addPaintHook(this);

    enableEvents(MouseEvent.MOUSE_EVENT_MASK|MouseEvent.MOUSE_MOTION_EVENT_MASK);
  }




  /**
   * Returns <code>true</code> if the arrow/circle adding functionality enabled
   * (for the user), <code>false</code> otherwise.
   */

  public boolean isArrowCircleEnabled(){
    return isArrowCircleEnabled;
  }




  /**
   * Sets whether the arrow/circle adding functionality is enabled for the user.
   */

  public void setArrowCircleEnabled(boolean enabled){
    this.isArrowCircleEnabled = enabled;
  }




  /**
   * Sets the default arrow color.
   */

  public void setDefaultArrowColor(Color color){
    this.defaultArrowColor = color;
  }




  /**
   * Sets the default circle color.
   */

  public void setDefaultCircleColor(Color color){
    this.defaultCircleColor = color;
  }




  /**
   * Returns the default arrow color.
   */

  public Color getDefaultArrowColor(){
    return defaultArrowColor;
  }




  /**
   * Returns the default circle color.
   */

  public Color getDefaultCircleColor(){
    return defaultCircleColor;
  }




  /**
   * Adds the given ArrowCircleListener to the list of listeners receiving
   * notifications when a circle or an arrow is added.
   */

  public void addArrowCircleListener(ArrowCircleListener listener){
    listenerList.add(ArrowCircleListener.class, listener);
  }




  /**
   * Removes the given ArrowCircleListener from the list of listeners receiving
   * notifications when a circle or an arrow is added.
   */

  public void removeArrowCircleListener(ArrowCircleListener listener){
    listenerList.remove(ArrowCircleListener.class, listener);
  }



  
  /**
   * Notifies all registered ArrowCircleListeners that an arrow has been added.
   */

  protected void fireArrowAdded(Square from, Square to){
   Object [] listeners = listenerList.getListenerList();
    for (int i = 0; i < listeners.length; i += 2){
      if (listeners[i]==ArrowCircleListener.class){
        ArrowCircleListener listener = (ArrowCircleListener)listeners[i+1];
        listener.arrowAdded(this, from, to);
      }
    }
  }




  /**
   * Notifies all registered ArrowCircleListeners that a circle has been added.
   */

  protected void fireCircleAdded(Square circleSquare){
   Object [] listeners = listenerList.getListenerList();
    for (int i = 0; i < listeners.length; i += 2){
      if (listeners[i]==ArrowCircleListener.class){
        ArrowCircleListener listener = (ArrowCircleListener)listeners[i+1];
        listener.circleAdded(this, circleSquare);
      }
    }
  }




  /**
   * Notifies all registered ArrowCircleListeners that an arrow has been
   * removed.
   */

  protected void fireArrowRemoved(Square from, Square to){
   Object [] listeners = listenerList.getListenerList();
    for (int i = 0; i < listeners.length; i += 2){
      if (listeners[i]==ArrowCircleListener.class){
        ArrowCircleListener listener = (ArrowCircleListener)listeners[i+1];
        listener.arrowRemoved(this, from, to);
      }
    }
  }




  /**
   * Notifies all registered ArrowCircleListeners that a circle has been
   * removed.
   */

  protected void fireCircleRemoved(Square circleSquare){
   Object [] listeners = listenerList.getListenerList();
    for (int i = 0; i < listeners.length; i += 2){
      if (listeners[i]==ArrowCircleListener.class){
        ArrowCircleListener listener = (ArrowCircleListener)listeners[i+1];
        listener.circleRemoved(this, circleSquare);
      }
    }
  }





  /**
   * PaintHook implementation.
   */

  public void paint(Component component, Graphics g){
    if (component != this)
      throw new IllegalArgumentException("Can only paint on this.");

    int arrowCount = arrows.size()/3;
    for (int i = 0; i < arrowCount; i++)
      drawArrow(g, (Square)arrows.elementAt(i*3), (Square)arrows.elementAt(i*3+1), (Color)arrows.elementAt(i*3+2));

    int circleCount = circles.size()/2;
    for (int i = 0; i < circleCount; i++)
      drawCircle(g, (Square)circles.elementAt(i*2), (Color)circles.elementAt(i*2+1));

    if ((tmpArrowFrom != null)&&(tmpArrowTo != null)&&!tmpArrowFrom.equals(tmpArrowTo))
      drawArrow(g, tmpArrowFrom, tmpArrowTo, getDefaultArrowColor());
  }




  /**
   * Draws the specified arrow on the given Graphics object.
   */

  private void drawArrow(Graphics g, Square from, Square to, Color color){
    g.setColor(color);

    Rectangle fromRect = squareToRect(from, null);
    Rectangle toRect = squareToRect(to, null);

    final int arrowSize = fromRect.width/7; // Assume width==height

    int fromX = fromRect.x + fromRect.width/2;
    int fromY = fromRect.y + fromRect.height/2;
    int toX = toRect.x + toRect.width/2;
    int toY = toRect.y + toRect.height/2;

    int dx = toX - fromX;
    int dy = toY - fromY;

    double angle = Math.atan2(dy, dx);
    double sin = Math.sin(angle);
    double cos = Math.cos(angle);

    int [] xpoints = new int[4];
    int [] ypoints = new int[4];

    // The arrow "stick"
    xpoints[0] = (int)(fromX+sin*arrowSize/2);
    ypoints[0] = (int)(fromY-cos*arrowSize/2);
    xpoints[1] = (int)(fromX-sin*arrowSize/2);
    ypoints[1] = (int)(fromY+cos*arrowSize/2);
    xpoints[2] = (int)(toX-sin*arrowSize/2);
    ypoints[2] = (int)(toY+cos*arrowSize/2);
    xpoints[3] = (int)(toX+sin*arrowSize/2);
    ypoints[3] = (int)(toY-cos*arrowSize/2);

    g.fillPolygon(xpoints, ypoints, 4);

    // The arrow "point"
    xpoints[0] = (int)(toX+cos*arrowSize);
    ypoints[0] = (int)(toY+sin*arrowSize);
    xpoints[1] = (int)(toX+Math.cos(angle-Math.PI*3/4)*arrowSize*2);
    ypoints[1] = (int)(toY+Math.sin(angle-Math.PI*3/4)*arrowSize*2);
    xpoints[2] = (int)(toX-cos*arrowSize/2);
    ypoints[2] = (int)(toY-sin*arrowSize/2);
    xpoints[3] = (int)(toX+Math.cos(angle+Math.PI*3/4)*arrowSize*2);
    ypoints[3] = (int)(toY+Math.sin(angle+Math.PI*3/4)*arrowSize*2);

    g.fillPolygon(xpoints, ypoints, 4);
  }




  /**
   * Draws the specified circle on the given Graphics object.
   */

  private void drawCircle(Graphics g, Square circleSquare, Color color){
    g.setColor(color);

    Rectangle rect = squareToRect(circleSquare, null);

    int size = rect.width/10; // Assume width==height

    g.translate(rect.x, rect.y);

    g.fillRect(size, 0, rect.width - size*2, size);
    g.fillRect(rect.width - size, size, size, rect.height - size*2);
    g.fillRect(size, rect.height - size, rect.width - size*2, size);
    g.fillRect(0, size, size, rect.height - size*2);

    g.fillArc(0, 0, size*2, size*2, 90, 90);
    g.fillArc(rect.width - size*2, 0, size*2, size*2, 0, 90);
    g.fillArc(rect.width - size*2, rect.height - size*2, size*2, size*2, 270, 90);
    g.fillArc(0, rect.height - size*2, size*2, size*2, 180, 90);

    g.translate(-rect.x, -rect.y);
  }




  /**
   * Adds an arrow with the given color to the board.
   */

  public void addArrow(Square from, Square to, Color color){
    arrows.addElement(from);
    arrows.addElement(to);
    arrows.addElement(color);

    fireArrowAdded(from, to);

    repaint();
  }




  /**
   * Removes the specified arrow (or arrows if there's more than one) from the
   * board.
   */

  public void removeArrow(Square from, Square to){
    for (int i = 0; i < arrows.size()/3; i++){
      Square fromSquare = (Square)arrows.elementAt(i*3);
      Square toSquare = (Square)arrows.elementAt(i*3+1);

      if (from.equals(fromSquare) && to.equals(toSquare)){
        arrows.removeElementAt(i*3+2);
        arrows.removeElementAt(i*3+1);
        arrows.removeElementAt(i*3);
        i--;
      }
    }

    fireArrowRemoved(from, to);

    repaint();
  }





  /**
   * Removes all arrows.
   */

  public void removeAllArrows(){
    for (int i = arrows.size() - 3; i >= 0; i -= 3){
      Square from = (Square)arrows.elementAt(i);
      Square to = (Square)arrows.elementAt(i+1);
      
      arrows.removeElementAt(i+2);
      arrows.removeElementAt(i+1);
      arrows.removeElementAt(i);

      fireArrowRemoved(from, to);
    }
  }




  /**
   * Adds a circle with the given color to the board.
   */

  public void addCircle(Square circleSquare, Color color){
    circles.addElement(circleSquare);
    circles.addElement(color);

    fireCircleAdded(circleSquare);

    repaint();
  }




  /**
   * Removes the specified circle (or circles, if there's more than one) from 
   * the board.
   */

  public void removeCircle(Square circleSquare){
    for (int i = 0; i < circles.size()/2; i++){
      Square square = (Square)circles.elementAt(i*2);

      if (square.equals(circleSquare)){
        circles.removeElementAt(i*2+1);
        circles.removeElementAt(i*2);
        i--;
      }
    }

    fireCircleRemoved(circleSquare);

    repaint();
  }





  /**
   * Removes all circles.
   */

  public void removeAllCircles(){
    for (int i = circles.size() - 2; i >= 0; i -= 2){
      Square square = (Square)circles.elementAt(i);
      
      circles.removeElementAt(i+1);
      circles.removeElementAt(i);

      fireCircleRemoved(square);
    }
  }




  /**
   * Processes a mouse event.
   */

  protected void processMouseEvent(MouseEvent evt){
    super.processMouseEvent(evt);

    if (!isEnabled())
      return;

    // Only take into account the right mouse button
    if ((evt.getModifiers()&MouseEvent.BUTTON3_MASK) != MouseEvent.BUTTON3_MASK)
      return;

    int x = evt.getX();
    int y = evt.getY();

    Square square = locationToSquare(x, y);

    if (evt.getID() == MouseEvent.MOUSE_CLICKED)
      addCircle(square, getDefaultCircleColor());
    else if (evt.getID() == MouseEvent.MOUSE_PRESSED)
      tmpArrowFrom = square;
    else if (evt.getID() == MouseEvent.MOUSE_RELEASED){
      if ((tmpArrowFrom != null) && (tmpArrowTo != null) && !tmpArrowFrom.equals(tmpArrowTo))
        addArrow(tmpArrowFrom, tmpArrowTo, getDefaultArrowColor());
      tmpArrowFrom = null;
      tmpArrowTo = null;
    }
  }





  /**
   * Processes a mouse motion event.
   */

  protected void processMouseMotionEvent(MouseEvent evt){
    super.processMouseMotionEvent(evt);

    if (!isEnabled())
      return;

    // Only take into account the right mouse button
    if ((evt.getModifiers()&MouseEvent.BUTTON3_MASK) != MouseEvent.BUTTON3_MASK)
      return;

    int x = evt.getX();
    int y = evt.getY();

    Square square = locationToSquare(x, y);

    if ((evt.getID() == MouseEvent.MOUSE_DRAGGED)&&(tmpArrowFrom != null)){
      tmpArrowTo = square;
      repaint();
    }

  }


}