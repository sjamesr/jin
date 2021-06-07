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
package free.chess.variants.kriegspiel;

import free.chess.BoardPainter;
import free.chess.Chess;
import free.chess.ChessMove;
import free.chess.ChessPiece;
import free.chess.ChesslikeGenericVariant;
import free.chess.DefaultBoardPainter;
import free.chess.DefaultPiecePainter;
import free.chess.Move;
import free.chess.Piece;
import free.chess.PiecePainter;
import free.chess.Player;
import free.chess.Position;
import free.chess.Square;
import free.chess.WildVariant;
import java.util.Collection;

/**
 * Implements the Kriegspiel wild variant. See <A
 * HREF="http://www.chessclub.com/help/Kriegspiel">http://www.chessclub.com/help/Kriegspiel</A> for
 * a description of Kriegspiel.
 */
public class Kriegspiel implements WildVariant {

  /** The sole instance of this class. */
  private static final Kriegspiel INSTANCE = new Kriegspiel();

  /** Returns an instance of Kriegspiel. */
  public static Kriegspiel getInstance() {
    return INSTANCE;
  }

  /**
   * Creates a new Kriegspiel object. This constructor is private and there is only one instance of
   * this class.
   */
  private Kriegspiel() {}

  /**
   * Checks if the given position's wild variant is Kriegspiel and returns normally if it is, throws
   * an IllegalArgumentException otherwise.
   */
  private void checkPosition(Position pos) {
    if (!pos.getVariant().equals(this))
      throw new IllegalArgumentException("Wrong position variant: " + pos.getVariant());
  }

  /**
   * Initializes the given position.
   *
   * @throws IllegalArgumentException If the given Position's wild variant is not Kriegspiel.
   */
  @Override
  public void init(Position pos) {
    checkPosition(pos);

    pos.setLexigraphic(Chess.INITIAL_POSITION_LEXIGRAPHIC);
  }

  /**
   * If the a move created by the given starting square and ending square in the given position is a
   * promotion, returns an array containing a knight, bishop, rook and queen of the color of the
   * promoted pawn. Otherwise returns null.
   *
   * @throws IllegalArgumentException If the given Position's wild variant is not Kriegspiel.
   */
  @Override
  public Piece[] getPromotionTargets(Position pos, Square startingSquare, Square endingSquare) {
    checkPosition(pos);

    return ChesslikeGenericVariant.getChessPromotionTargets(pos, startingSquare, endingSquare);
  }

  /**
   * Creates a new KriegspielMove or ChessMove based on the given arguments. If the end square is
   * null, the created move will be a completely hidden KriegspielMove. If the end square is not
   * null, but the start square is, the created move is a partially hidden KriegspielMove. If both
   * are not null, then it's a completely visible, regular ChessMove.
   *
   * @see KriegspielMove
   */
  @Override
  public Move createMove(
      Position pos,
      Square startSquare,
      Square endSquare,
      Piece promotionTarget,
      String stringRepresentation) {

    checkPosition(pos);

    if (endSquare == null) {
      return new KriegspielMove(pos.getCurrentPlayer(), stringRepresentation);
    } else if (startSquare == null) {
      return new KriegspielMove(pos, endSquare, stringRepresentation);
    } else {
      return Chess.getInstance()
          .createChessMove(pos, startSquare, endSquare, promotionTarget, stringRepresentation);
    }
  }

  /**
   * Creates a <code>Move</code> object representing a move just like the specified one, but made in
   * the specified position.
   */
  @Override
  public Move createMove(Position pos, Move move) {
    checkPosition(pos);

    if (move instanceof ChessMove) {
      ChessMove cmove = (ChessMove) move;
      return createMove(
          pos,
          cmove.getStartingSquare(),
          cmove.getEndingSquare(),
          cmove.getPromotionTarget(),
          cmove.getStringRepresentation());
    } else
      return createMove(
          pos,
          move.getStartingSquare(),
          move.getEndingSquare(),
          null,
          move.getStringRepresentation());
  }

  /** Creates a short castling move for the current player in the specified position. */
  @Override
  public Move createShortCastling(Position pos) {
    checkPosition(pos);

    Player currentPlayer = pos.getCurrentPlayer();
    if (currentPlayer.isWhite()) return ChesslikeGenericVariant.WHITE_SHORT_CASTLING;
    else return ChesslikeGenericVariant.BLACK_SHORT_CASTLING;
  }

  /** Creates a long castling move for the current player in the specified position. */
  @Override
  public Move createLongCastling(Position pos) {
    checkPosition(pos);

    Player currentPlayer = pos.getCurrentPlayer();
    if (currentPlayer.isWhite()) return ChesslikeGenericVariant.WHITE_LONG_CASTLING;
    else return ChesslikeGenericVariant.BLACK_LONG_CASTLING;
  }

  /** Makes the given KriegspielMove on the given position. */
  @Override
  public void makeMove(Move move, Position pos, Position.Modifier modifier) {
    checkPosition(pos);

    if ((!(move instanceof KriegspielMove)) && (!(move instanceof ChessMove)))
      throw new IllegalArgumentException(
          "The given move must be an instance of "
              + ChessMove.class.getName()
              + " or "
              + KriegspielMove.class.getName());

    if (move instanceof KriegspielMove) {
      KriegspielMove kmove = (KriegspielMove) move;

      if (!kmove.isCompletelyHidden()) {
        Square endSquare = kmove.getEndingSquare();
        modifier.setPieceAt(null, endSquare);
      }

      modifier.setCurrentPlayer(pos.getCurrentPlayer().getOpponent());
    } else { // Completely visible
      Chess.getInstance().makeChessMove((ChessMove) move, pos, modifier);
    }
  }

  /**
   * Returns a ChessPiece corresponding to the given string. See {@link
   * free.chess.ChesslikeGenericVariant#parseChessPiece(String)} for more details.
   *
   * @throws IllegalArgumentException if the given string is in a bad format.
   */
  @Override
  public Piece parsePiece(String piece) {
    return ChesslikeGenericVariant.parseChessPiece(piece);
  }

  /**
   * Returns a String corresponding to the given Piece. See {@link
   * free.chess.ChesslikeGenericVariant#chessPieceToString(ChessPiece)} for more details.
   */
  @Override
  public String pieceToString(Piece piece) {
    if (!(piece instanceof ChessPiece))
      throw new IllegalArgumentException("The given Piece must be an instance of ChessPiece.");

    return ChesslikeGenericVariant.chessPieceToString((ChessPiece) piece);
  }

  /** Returns an instance of <code>DefaultPiecePainter</code>. */
  @Override
  public PiecePainter createDefaultPiecePainter() {
    return new DefaultPiecePainter();
  }

  /** Returns an instance of <code>DefaultBoardPainter</code>. */
  @Override
  public BoardPainter createDefaultBoardPainter() {
    return new DefaultBoardPainter();
  }

  /** {@inheritDoc} */
  @Override
  public Collection getTargetSquares(Position pos, Square square) {
    checkPosition(pos);

    return Chess.getInstance().getTargetSquares(pos, square);
  }

  /** Returns the string "Kriegspiel". */
  @Override
  public String getName() {
    return "Kriegspiel";
  }

  /** Returns a textual representation of this WildVariant. */
  @Override
  public String toString() {
    return getName();
  }

  @Override
  public int getApproximateMaterialValue(Piece piece) {
    switch (piece.getType()) {
      case ChessPiece.QUEEN:
        return 9;
      case ChessPiece.ROOK:
        return 5;
      case ChessPiece.KNIGHT:
      case ChessPiece.BISHOP:
        return 3;
      case ChessPiece.PAWN:
        return 1;
      default:
        return 0;
    }
  }
}
