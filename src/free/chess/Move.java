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


/**
 * This is the superclass of move implementations for various wild chess variants.
 */

public abstract class Move{


  /**
   * The square on which the moving piece was standing before the move.
   */

  protected final Square startingSquare;



  /**
   * The square to which the moving piece moved.
   */

  protected final Square endingSquare;



  /**
   * The player making the move.
   */

  protected final Player player;



  /**
   * The string representation of this move. This may be null.
   */

  private final String stringRepresentation;




  /**
   * Creates a new Move from the given starting Square to the given ending
   * Square, with the given moving Player. 
   * <P>Both the starting and ending squares may be null, as they sometimes are,
   * for example in Kriegspiel.
   * <P>The given stringRepresentation will be the string returned from the
   * toString() method. It may be <code>null</code>, in which case a string
   * constructed from the move data will be returned by toString().
   */

  public Move(Square startingSquare, Square endingSquare, Player player,
      String stringRepresentation){

    if (player == null)
      throw new IllegalArgumentException("The moving player may not be null");

    this.startingSquare = startingSquare;
    this.endingSquare = endingSquare;
    this.player = player;
    this.stringRepresentation = stringRepresentation;
  }




  /**
   * Returns the initial square of the moving piece or <code>null</code> if
   * unknown.
   */

  public Square getStartingSquare(){
    return startingSquare;
  }


 
  /**
   * Returns the square to which the moving piece moved or <code>null</code> if
   * unknown
   */

  public Square getEndingSquare(){
    return endingSquare;
  }



  /**
   * Returns the Player making the move.
   */

  public Player getPlayer(){
    return player;
  }



  /**
   * Returns the string representation of the move passed in the constructor.
   * This may be null.
   */

  public final String getStringRepresentation(){
    return stringRepresentation;
  }



  /**
   * Returns a string representation of the move based on the move data.
   */

  public String getMoveString(){
    Square startingSquare = getStartingSquare();
    Square endingSquare = getEndingSquare();

    String startingString = startingSquare == null ? "??" : startingSquare.toString();
    String endingString = endingSquare == null ? "??" : endingSquare.toString();

    return startingString + endingString;
  }
  
  
  
  /**
   * Breaks the given move string, represented in Smith Warren format into
   * starting square, ending square and promotion target, and using
   * WildVariant.parsePiece(String) and
   * WildVariant.createMove(Position, Square, Square, Piece) creates a Move
   * object.
   */

  public static Move parseWarrenSmith(String moveSmith, Position position, String moveString){
    WildVariant variant = position.getVariant();
    
    char lastChar = moveSmith.charAt(moveSmith.length() - 1);
    if (lastChar == 'c') // Short castling
      return variant.createShortCastling(position);
    else if (lastChar == 'C') // Long castling
      return variant.createLongCastling(position);

    Square startSquare = Square.parseSquare(moveSmith.substring(0, 2));
    Square endSquare = Square.parseSquare(moveSmith.substring(2, 4));
    
    Piece promotionTarget = null;
    if ("NBRQK".indexOf(lastChar) != -1){
      // The 'K' can happen in Giveaway, where you can promote to a king
      String promotionTargetString = String.valueOf(moveSmith.charAt(moveSmith.length() - 1));
      if (position.getCurrentPlayer().isBlack())
        promotionTargetString = promotionTargetString.toLowerCase();
      promotionTarget = variant.parsePiece(promotionTargetString);
    }

    return variant.createMove(position, startSquare, endSquare, promotionTarget, moveString);
  }
  



  /**
   * Returns the string representation of this Move. If the value returned by
   * {@link #getStringRepresentation()} is not null, that is returned. Otherwise
   * the value of getMoveString() is returned.
   */

  public String toString(){
    if (getStringRepresentation()==null)
      return getMoveString();
    else
      return getStringRepresentation();
  }
  
  

}
