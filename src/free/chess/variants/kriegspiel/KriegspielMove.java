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

package free.chess.variants.kriegspiel;

import free.chess.*;



/**
 * A move in the Kriegspiel chess variant. A Kriegspiel move can be of one of
 * two types:
 * <UL>
 *   <LI> A partially hidden move, where only the target square is known because
 *        it was a capture of the player's piece.
 *   <LI> A completely hidden move.
 * </UL>
 * The various methods in this class will throw an IllegalStateException if the
 * requested information is unknown.
 */

public class KriegspielMove extends Move{



  /**
   * The ending square in case of a partially hidden move.
   */

  private final Square endingSquare;



  /**
   * The square where the piece moved in case of a partially hidden move.
   */

  private final ChessPiece capturedPiece;




  /**
   * Creates a new completely hidden Kriegspiel move. No information about the
   * move is available and all inquiring methods will throw an
   * IllegalStateException (except that we know it's not a capture). The
   * {@link #isCompletelyHidden()} method will return <code>true</code>.
   * <code>stringRepresentation</code> may be <code>null</code> in which case
   * a string constructed from the move data will be returned by toString()..
   */

  public KriegspielMove(String stringRepresentation){
    super(null, null, stringRepresentation);

    this.endingSquare = null;
    this.capturedPiece = null;
  }




  /**
   * Creates a partially hidden Kriegspiel move. It is known that this move is
   * a capture, the captured piece is known and the target (ending) square is
   * known. The {@link #isCompletelyHidden()} method will return
   * <code>false</code>. <code>stringRepresentation</code> may be
   * <code>null</code> in which case a string constructed from the move data
   * will be returned by toString()..
   */

  public KriegspielMove(Position pos, Square endingSquare, String stringRepresentation){
    super(null, endingSquare, stringRepresentation);

    this.endingSquare = endingSquare;
    this.capturedPiece = (ChessPiece)pos.getPieceAt(endingSquare);

    if (capturedPiece==null)
      throw new IllegalArgumentException("A partially hidden move must be a capture");
  }





  /**
   * Returns <code>true<code> if this is a completely hidden move. Returns
   * <code>false</code> otherwise.
   */

  public boolean isCompletelyHidden(){
    return endingSquare==null;
  }




  /**
   * Always throws an IllegalStateException, as the information about the
   * starting square is never available for a KriegspielMove.
   */

  public Square getStartingSquare(){
    throw new IllegalStateException("The move must be completely visible for the starting square to be known");
  }


 
  /**
   * Returns the square to which the moving piece moved.
   *
   * @throws IllegalStateException if the move is a completly hidden one.
   */

  public Square getEndingSquare(){
    if (isCompletelyHidden())
      throw new IllegalStateException("The move must be at least partially visible for the ending square to be known");

    return endingSquare;
  }




  /**
   * Returns a string representation of the move based on the move data.
   */

  public String getMoveString(){
    if (isCompletelyHidden())  
      return "?";
    else
      return "?x"+getEndingSquare().toString();
  }


}
