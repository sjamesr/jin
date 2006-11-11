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

import free.chess.*;

import java.awt.*;
import java.util.Vector;
import free.chess.Position;
import free.chess.Square;
import free.util.PaintHook;
import free.jin.board.event.ArrowCircleListener;


/**
 * An extension of <code>free.chess.JBoard</code> which implements certain Jin
 * specific features. This class adds the arrow/circle functionality but doesn't
 * listen to the appropriate mouse events to actually add arrows. This is done
 * in the server specific subclasses. The "official" purpose of this is to allow
 * server specific gestures to generate arrows/circles. The "unofficial" reason
 * is so that it's possible to add arrows/circles programmatically on any
 * server.
 */

public class JinBoard extends JBoard implements PaintHook{



  /**
   * A Vector holding the arrows of the board (instances of Arrow).
   */

  private final Vector arrows = new Vector(3);



  /**
   * A Vector holding the circles of the board (instances of Circle).
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
   * Creates a new <code>JinBoard</code> with the given initial
   * <code>Position</code>.
   */

  public JinBoard(Position initPosition){
    super(initPosition);

    addPaintHook(this);
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

  protected void fireArrowAdded(Arrow arrow){
   Object [] listeners = listenerList.getListenerList();
    for (int i = 0; i < listeners.length; i += 2){
      if (listeners[i] == ArrowCircleListener.class){
        ArrowCircleListener listener = (ArrowCircleListener)listeners[i+1];
        listener.arrowAdded(this, arrow);
      }
    }
  }




  /**
   * Notifies all registered ArrowCircleListeners that a circle has been added.
   */

  protected void fireCircleAdded(Circle circle){
   Object [] listeners = listenerList.getListenerList();
    for (int i = 0; i < listeners.length; i += 2){
      if (listeners[i]==ArrowCircleListener.class){
        ArrowCircleListener listener = (ArrowCircleListener)listeners[i+1];
        listener.circleAdded(this, circle);
      }
    }
  }




  /**
   * Notifies all registered ArrowCircleListeners that an arrow has been
   * removed.
   */

  protected void fireArrowRemoved(Arrow arrow){
   Object [] listeners = listenerList.getListenerList();
    for (int i = 0; i < listeners.length; i += 2){
      if (listeners[i]==ArrowCircleListener.class){
        ArrowCircleListener listener = (ArrowCircleListener)listeners[i+1];
        listener.arrowRemoved(this, arrow);
      }
    }
  }




  /**
   * Notifies all registered ArrowCircleListeners that a circle has been
   * removed.
   */

  protected void fireCircleRemoved(Circle circle){
   Object [] listeners = listenerList.getListenerList();
    for (int i = 0; i < listeners.length; i += 2){
      if (listeners[i]==ArrowCircleListener.class){
        ArrowCircleListener listener = (ArrowCircleListener)listeners[i+1];
        listener.circleRemoved(this, circle);
      }
    }
  }




  /**
   * Calculates the arrow size for the specified square size.
   */

  protected int calcArrowSize(int squareWidth, int squareHeight){
    return squareWidth/7;
  }

  


  /**
   * Calculates the circle size for the specified square size.
   */

  protected int calcCircleSize(int squareWidth, int squareHeight){
    return squareWidth/10;
  }




  /**
   * PaintHook implementation.
   */

  public void paint(Component component, Graphics g){
    if (component != this)
      throw new IllegalArgumentException("Can only paint on this");

    Rectangle rect = squareToRect(0, 0, null);

    int arrowSize = calcArrowSize(rect.width, rect.height);
    int circleSize = calcCircleSize(rect.width, rect.height);

    int arrowCount = arrows.size();
    for (int i = 0; i < arrowCount; i++){
      Arrow arrow = (Arrow)arrows.elementAt(i); 
      drawArrow(g, arrow.getFrom(), arrow.getTo(), arrowSize, arrow.getColor());
    }

    int circleCount = circles.size();
    for (int i = 0; i < circleCount; i++){
      Circle circle = (Circle)circles.elementAt(i);
      drawSquare(g, circle.getSquare(), circleSize, circle.getColor());
    }
  }




  /**
   * Adds the specified arrow to the board.
   */

  public void addArrow(Arrow arrow){
    arrows.addElement(arrow);
    
    fireArrowAdded(arrow);

    repaint();
  }




  /**
   * Removes all the arrows from the specified square to the specified square.
   */

  public void removeArrowsAt(Square from, Square to){
    for (int i = arrows.size() - 1; i >= 0; i--){
      Arrow arrow = (Arrow)arrows.elementAt(i);

      if (arrow.getFrom().equals(from) && arrow.getTo().equals(to)){
        arrows.removeElementAt(i);
        fireArrowRemoved(arrow);
      }
    }

    repaint();
  }
  
  
  
  /**
   * Returns whether there are any arrows at the specified coordinates.
   */
   
  public boolean areArrowsAt(Square from, Square to){
    for (int i = 0; i < arrows.size(); i++){
      Arrow arrow = (Arrow)arrows.elementAt(i);
      if (arrow.getFrom().equals(from) && arrow.getTo().equals(to))
        return true;
    }
    
    return false;
  }



  /**
   * Removes all arrows.
   */

  public void removeAllArrows(){
    for (int i = arrows.size() - 1; i >= 0; i--){
      Arrow arrow = (Arrow)arrows.elementAt(i);
      arrows.removeElementAt(i);
      fireArrowRemoved(arrow);
    }

    repaint();
  }




  /**
   * Adds the specified circle to the board.
   */

  public void addCircle(Circle circle){
    circles.addElement(circle);
    fireCircleAdded(circle);

    repaint();
  }




  /**
   * Removes the specified circle (or circles, if there's more than one) from 
   * the board.
   */

  public void removeCirclesAt(Square circleSquare){
    for (int i = circles.size() - 1; i >= 0; i--){
      Circle circle = (Circle)circles.elementAt(i);

      if (circle.getSquare().equals(circleSquare)){
        circles.removeElementAt(i);
        fireCircleRemoved(circle);
      }
    }
    
    repaint();
  }
  
  
  
  /**
   * Returns whether there are any circles at the specified coordinate.
   */
   
  public boolean areCirclesAt(Square square){
    for (int i = 0; i < circles.size(); i++){
      Circle circle = (Circle)circles.elementAt(i);
      if (circle.getSquare().equals(square))
        return true;
    }
    
    return false;
  }



  /**
   * Removes all circles.
   */

  public void removeAllCircles(){
    for (int i = circles.size() - 1; i >= 0; i--){
      Circle circle = (Circle)circles.elementAt(i);
      circles.removeElementAt(i);
      fireCircleRemoved(circle);
    }
    
    repaint();
  }



}
