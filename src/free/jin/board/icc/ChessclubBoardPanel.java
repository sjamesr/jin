/**
 * Jin - a chess client for internet chess servers.
 * More information is available at http://www.jinchess.com/.
 * Copyright (C) 2002, 2003 Alexander Maryanovsky.
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

package free.jin.board.icc;

import java.awt.Color;

import free.jin.Game;
import free.jin.board.*;
import free.jin.board.event.ArrowCircleListener;
import free.jin.chessclub.event.ArrowEvent;
import free.jin.chessclub.event.ChessclubGameListener;
import free.jin.chessclub.event.CircleEvent;
import free.jin.event.IllegalMoveEvent;
import free.jin.event.MoveMadeEvent;
import free.jin.event.PositionChangedEvent;
import free.jin.event.TakebackEvent;


/**
 * Extends <code>BoardPanel</code> to provide chessclub.com specific
 * functionaly.
 */

public class ChessclubBoardPanel extends BoardPanel implements ChessclubGameListener,
    ArrowCircleListener{



  /**
   * We set this to true when we're handling a circle/arrow adding event from
   * the server to avoid responding to the board's event when we add the
   * circle/arrow to it.
   */

  private boolean handlingArrowCircleEvent = false;



  /**
   * Creates a new <code>ChessclubBoardPanel</code> with the given
   * <code>BoardManager</code> and <code>Game</code>.
   */

  public ChessclubBoardPanel(BoardManager boardManager, Game game){
    super(boardManager, game);
  }



  /**
   * Overrides <code>createBoard(Game)</code> to return an instance of
   * <code>ChessclubJBoard</code>.
   */

  protected JinBoard createBoard(Game game){
    return new ChessclubJBoard(game.getInitialPosition());
  }



  /**
   * Overrides <code>configureBoard(Game, JinBoard)</code> to add ourselves as
   * an <code>ArrowCircleListener</code> to the board.
   */

  protected void configureBoardFromGame(Game game, JinBoard board){
    super.configureBoardFromGame(game, board);

    if ((game.getGameType() == Game.MY_GAME) && !game.isPlayed()){
      board.addArrowCircleListener(this);
      board.setArrowCircleEnabled(true);
    }
    else
      board.setArrowCircleEnabled(false);
  }
  
  
  
  /**
   * Overrides <code>moveMade(MoveMadeEvent)</code> to clear the board of any
   * arrows/circles.
   */

  public void moveMade(MoveMadeEvent evt){
    super.moveMade(evt);

    if (evt.getGame() != game)
      return;

    board.removeAllArrows();
    board.removeAllCircles();
  }




  /**
   * Overrides positionChanged(PositionChangedEvent) to clear the board of any
   * arrows/circles.
   */

  public void positionChanged(PositionChangedEvent evt){
    super.positionChanged(evt);

    if (evt.getGame() != game)
      return;

    board.removeAllArrows();
    board.removeAllCircles();
  }





  /**
   * Overrides takebackOccurred(TakebackEvent) to clear the board of any
   * arrows/circles.
   */

  public void takebackOccurred(TakebackEvent evt){
    super.takebackOccurred(evt);

    if (evt.getGame() != game)
      return;

    board.removeAllArrows();
    board.removeAllCircles();
  }





  /**
   * Overrides illegalMoveAttempted(IllegalMoveEvent) to clear the board of any
   * arrows/circles.
   */

  public void illegalMoveAttempted(IllegalMoveEvent evt){
    super.illegalMoveAttempted(evt);

    if (evt.getGame() != game)
      return;

    board.removeAllArrows();
    board.removeAllCircles();
  }



  /**
   * Overrides BoardPanel.createWhiteLabelText(Game) to return a chessclub.com
   * specific version.
   */

  protected String createWhiteLabelText(Game game){
    int rating = game.getWhiteRating();
    String ratingString = (rating > 0) ? (" "+rating) : "";
    return game.getWhiteName() + game.getWhiteTitles() + ratingString;
  }




  /**
   * Overrides BoardPanel.createBlackLabel(Game) to return a chessclub.com
   * specific version.
   */

  protected String createBlackLabelText(Game game){
    int rating = game.getBlackRating();
    String ratingString = (rating > 0) ? (" "+rating) : "";
    return game.getBlackName() + game.getBlackTitles() + ratingString;
  }



  /**
   * Gets called when an arrow is added to the board (by the server).
   */

  public void arrowAdded(ArrowEvent evt){
    if (evt.getGame() != game)
      return;

    Arrow arrow = new Arrow(evt.getFromSquare(), evt.getToSquare(), Color.blue);
    
    handlingArrowCircleEvent = true;
    board.removeArrowsAt(evt.getFromSquare(), evt.getToSquare());
    board.addArrow(arrow);
    handlingArrowCircleEvent = false;
  }
  
  
  
  /**
   * Gets called when an arrow is removed from the board (by the server).
   */
   
  public void arrowRemoved(ArrowEvent evt){
    if (evt.getGame() != game)
      return;
    
    handlingArrowCircleEvent = true;
    board.removeArrowsAt(evt.getFromSquare(), evt.getToSquare());
    handlingArrowCircleEvent = false;
  }




  /**
   * Gets called when a circle is added to the board (by the server).
   */

  public void circleAdded(CircleEvent evt){
    if (evt.getGame() != game)
      return;

    Circle circle = new Circle(evt.getCircleSquare(), Color.blue);
    handlingArrowCircleEvent = true;
    board.removeCirclesAt(evt.getCircleSquare());
    board.addCircle(circle);
    handlingArrowCircleEvent = false;
  }
  
  
  
  /**
   * Gets called when a circle is removed from the board (by the server).
   */
   
  public void circleRemoved(CircleEvent evt){
    if (evt.getGame() != game)
      return;
    
    handlingArrowCircleEvent = true;
    board.removeCirclesAt(evt.getCircleSquare());
    handlingArrowCircleEvent = false;
  }




  /**
   * Gets called when an arrow is added on the board (on the client, not server). 
   */

  public void arrowAdded(JinBoard board, Arrow arrow){
    if (handlingArrowCircleEvent)
      return;

    boardManager.getConn().sendCommand("arrow " + arrow.getFrom() + " " + arrow.getTo());
  }



  /**
   * Gets called when an arrow is removed on the board (on the client, not the
   * server.
   */

  public void arrowRemoved(JinBoard board, Arrow arrow){
    if (handlingArrowCircleEvent)
      return;
    
    boardManager.getConn().sendCommand("unarrow " + arrow.getFrom() + " " + arrow.getTo());
  }



  /**
   * Gets called when a circle is added (on the client, not the server).
   */

  public void circleAdded(JinBoard board, Circle circle){
    if (handlingArrowCircleEvent)
      return;

    boardManager.getConn().sendCommand("circle " + circle.getSquare());
  }



  /**
   * Gets called when a circle is removed (on the client, not the server).
   */

  public void circleRemoved(JinBoard board, Circle circle){
    if (handlingArrowCircleEvent)
      return;
    
    boardManager.getConn().sendCommand("uncircle " + circle.getSquare());
  }



}
