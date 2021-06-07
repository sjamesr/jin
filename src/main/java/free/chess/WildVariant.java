/**
 * The chess framework library. More information is available at http://www.jinchess.com/. Copyright
 * (C) 2002 Alexander Maryanovsky. All rights reserved.
 *
 * <p>The chess framework library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free Software Foundation;
 * either version 2 of the License, or (at your option) any later version.
 *
 * <p>The chess framework library is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
 * PURPOSE. See the GNU Lesser General Public License for more details.
 *
 * <p>You should have received a copy of the GNU Lesser General Public License along with the chess
 * framework library; if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite
 * 330, Boston, MA 02111-1307 USA
 */
package free.chess;

import java.util.Collection;

/** An interface classes defining rules for wild chess variants must implement. */
public interface WildVariant {

  /** Sets the initial position for this wild variant on the given Position. */
  void init(Position pos);

  /**
   * Returns the set of pieces to which the moving piece can be promoted. If the move is not a
   * promotion, returns null. The first piece in the array is considered to be the default promotion
   * piece (it'll be used if for example a dialog to choose the piece is displayed to the user and
   * he cancels it). If this method returns a non-null value, the array must contain at least one
   * element.
   */
  Piece[] getPromotionTargets(Position pos, Square startingSquare, Square endingSquare);

  /**
   * Creates a Move from the 2 given squares in the given Position. The created move may be illegal,
   * the implementation must ignore this and return some Move even if it's illegal. If the specified
   * information is so bad that it's impossible to construct a move from it altogether, the
   * implementation is allowed to throw an <code>IllegalArgumentException</code>. The given
   * stringRepresentation may be null.
   */
  Move createMove(
      Position pos,
      Square startSquare,
      Square endSquare,
      Piece promotionTarget,
      String stringRepresentation)
      throws IllegalArgumentException;

  /**
   * Creates a <code>Move</code> object representing a move just like the specified one, but made in
   * the specified position. If the specified information is so bad that it's impossible to
   * construct a move from it altogether, the implementation is allowed to throw an <code>
   * IllegalArgumentException</code>.
   */
  Move createMove(Position pos, Move move) throws IllegalArgumentException;

  /**
   * Creates a short castling move for the current player in the specified position. Short castling
   * must be legal in the specified position.
   */
  Move createShortCastling(Position pos);

  /**
   * Creates a long castling move for the current player in the specified position. Long castling
   * must be legal in the specified position.
   */
  Move createLongCastling(Position pos);

  /**
   * Makes the given Move in the given Position. This method shoudln't (and can't) be called
   * directly - call Position.makeMove(Move) instead. Implementations of this method should only
   * modify the Position via the modifier to avoid the change listeners of the position from being
   * notified in the middle of a move procedure where the position isn't "stable". There is no need
   * to trigger the listeners to be called after the move procedure is done - <code>
   * Position.makeMove(Move)</code> triggers them as needed by itself (and since this method can't
   * be called directly, that's the only way to make a move).
   */
  void makeMove(Move move, Position pos, Position.Modifier modifier);

  /**
   * Returns the piece represented by the given String.
   *
   * @throws IllegalArgumentException if the given character does not represent a piece in this
   *     WildVariant.
   */
  Piece parsePiece(String s);

  /**
   * Returns a string for the specified Piece. The piece must be one that was created by this
   * WildVariant and the returned value must be compatible with the <code>parsePiece</code> method
   * of this WildVariant.
   *
   * @throws IllegalArgumentException if the piece couldn't have been produced by this WildVariant.
   */
  String pieceToString(Piece piece);

  /**
   * Creates and returns a default PiecePainter for this WildVariant.
   *
   * @see PiecePainter
   */
  PiecePainter createDefaultPiecePainter();

  /**
   * Creates and returns a default BoardPainter for this WildVariant.
   *
   * @see BoardPainter
   */
  BoardPainter createDefaultBoardPainter();

  /**
   * Returns the squares to which the piece at the specified square may move. The set is not
   * guaranteed to contain only legal moves, but it is guaranteed that every legal move by the piece
   * has one of the squares as the target square. When implementing, keep in mind the case where the
   * moving piece is not of the same color as the player to move (premove or predrag modes).
   */
  Collection getTargetSquares(Position position, Square square);

  /** Returns the name of this WildVariant. */
  String getName();

  /**
   * Returns the approximate material value of the given piece in this variant. For example, for
   * chess, queen will be assigned the value 9, rooks 5, knights and bishops 3 and pawns 1.
   */
  int getApproximateMaterialValue(Piece piece);
}
