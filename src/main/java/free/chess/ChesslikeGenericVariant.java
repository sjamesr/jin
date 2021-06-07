/**
 * The chess framework library. More information is available at http://www.jinchess.com/. Copyright
 * (C) 2002, 2003 Alexander Maryanovsky. All rights reserved.
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
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * Implements WildVariant for chesslike variants. Subclasses must only define the initial position
 * and the name of the variant. Although this class is not abstract, directly instantiating it is
 * discouraged except for cases where the name or the initial position is unknown at compile time.
 * When the initial position and the name are known at compile time, its best to instantiate a
 * subclass which defines them.
 *
 * <p>This class also provides some methods for determining the various properties of a move in the
 * game of chess (en-passant, promotion, castling, etc.). Variants that only differ from chess in
 * their definition of these terms can override these methods and implement them accordingly. Note
 * that it is not necessary to override the {@link #createMove(Position, Square, Square, Piece,
 * String)} method as it already calls the forementioned methods when determining the properties of
 * the created ChessMove.
 */
public class ChesslikeGenericVariant implements WildVariant {

  /**
   * An array containing WHITE_QUEEN, WHITE_ROOK, WHITE_BISHOP and WHITE_KNIGHT. These are the
   * pieces to which a white pawn can be promoted.
   */
  private static final ChessPiece[] WHITE_PROMOTION_TARGETS =
      new ChessPiece[] {
        ChessPiece.WHITE_QUEEN,
        ChessPiece.WHITE_ROOK,
        ChessPiece.WHITE_BISHOP,
        ChessPiece.WHITE_KNIGHT
      };

  /**
   * An array containing BLACK_QUEEN, BLACK_ROOK, BLACK_BISHOP and BLACK_KNIGHT. These are the
   * pieces to which a black pawn can be promoted.
   */
  private static final ChessPiece[] BLACK_PROMOTION_TARGETS =
      new ChessPiece[] {
        ChessPiece.BLACK_QUEEN,
        ChessPiece.BLACK_ROOK,
        ChessPiece.BLACK_BISHOP,
        ChessPiece.BLACK_KNIGHT
      };

  /** A white short castling move. */
  public static final ChessMove WHITE_SHORT_CASTLING =
      new ChessMove(
          Square.parseSquare("e1"),
          Square.parseSquare("g1"),
          Player.WHITE_PLAYER,
          false,
          true,
          false,
          null,
          -1,
          null,
          "O-O");

  /** A white long castling move. */
  public static final ChessMove WHITE_LONG_CASTLING =
      new ChessMove(
          Square.parseSquare("e1"),
          Square.parseSquare("c1"),
          Player.WHITE_PLAYER,
          false,
          false,
          true,
          null,
          -1,
          null,
          "O-O-O");

  /** A black short castling move. */
  public static final ChessMove BLACK_SHORT_CASTLING =
      new ChessMove(
          Square.parseSquare("e8"),
          Square.parseSquare("g8"),
          Player.BLACK_PLAYER,
          false,
          true,
          false,
          null,
          -1,
          null,
          "O-O");

  /** A black long castling move. */
  public static final ChessMove BLACK_LONG_CASTLING =
      new ChessMove(
          Square.parseSquare("e8"),
          Square.parseSquare("c8"),
          Player.BLACK_PLAYER,
          false,
          false,
          true,
          null,
          -1,
          null,
          "O-O-O");

  /** The initial position of this variant, in FEN format. */
  private final String initialPositionFEN;

  /** The name of this variant. */
  private final String variantName;

  /**
   * Creates a new ChesslikeGenericVariant with the given initial position and name. The position is
   * specified in FEN format.
   */
  public ChesslikeGenericVariant(String initialPositionFEN, String variantName) {
    if (initialPositionFEN == null)
      throw new IllegalArgumentException("The initial position argument may not be null");
    if (variantName == null)
      throw new IllegalArgumentException("The variant name argument may not be null");

    this.initialPositionFEN = initialPositionFEN;
    this.variantName = variantName;
  }

  /**
   * Returns <code>true</code> if the chess move defined by the given arguments is an en-passant
   * move. Returns false otherwise. The result for an illegal move is undefined, but it should throw
   * no exceptions.
   */
  public boolean isEnPassant(
      Position pos, Square startingSquare, Square endingSquare, ChessPiece promotionTarget) {

    if (promotionTarget != null) return false;

    ChessPiece movingPiece = (ChessPiece) pos.getPieceAt(startingSquare);
    ChessPiece takenPiece = (ChessPiece) pos.getPieceAt(endingSquare);

    if (movingPiece.isPawn()) {
      Square squareAtIntersection =
          Square.getInstance(endingSquare.getFile(), startingSquare.getRank());
      ChessPiece pieceAtIntersection = (ChessPiece) pos.getPieceAt(squareAtIntersection);

      if (takenPiece != null) return false;
      else if (pieceAtIntersection == null) return false;
      else if (!pieceAtIntersection.isPawn()) return false;
      else if (pieceAtIntersection.isSameColor(movingPiece)) return false;
      else if (Math.abs(startingSquare.getFile() - endingSquare.getFile()) != 1) return false;
      else if (movingPiece.isWhite()) {
        if ((startingSquare.getRank() != 4) || (endingSquare.getRank() != 5)) return false;
        else return true;
      } else {
        if ((startingSquare.getRank() != 3) || (endingSquare.getRank() != 2)) return false;
        else return true;
      }
    } else return false;
  }

  /**
   * Returns <code>true</code> if the move defined by the given arguments is a short castling move.
   * Returns <code>false</code> otherwise. The result for an illegal move is undefined, but it
   * should throw no exceptions.
   */
  public boolean isShortCastling(
      Position pos, Square startingSquare, Square endingSquare, ChessPiece promotionTarget) {

    if (promotionTarget != null) return false;

    ChessPiece movingPiece = (ChessPiece) pos.getPieceAt(startingSquare);
    ChessPiece takenPiece = (ChessPiece) pos.getPieceAt(endingSquare);

    if (takenPiece != null) return false;

    if (movingPiece == ChessPiece.WHITE_KING) {
      if (!startingSquare.equals("e1")) return false;
      else if (!endingSquare.equals("g1")) return false;
      else if (pos.getPieceAt("h1") != ChessPiece.WHITE_ROOK) return false;
      else if (pos.getPieceAt("f1") != null) return false;
      else return true;
    } else if (movingPiece == ChessPiece.BLACK_KING) {
      if (!startingSquare.equals("e8")) return false;
      else if (!endingSquare.equals("g8")) return false;
      else if (pos.getPieceAt("h8") != ChessPiece.BLACK_ROOK) return false;
      else if (pos.getPieceAt("f8") != null) return false;
      else return true;
    } else return false;
  }

  /**
   * Returns <code>true</code> if the move defined by the given arguments is a long castling move.
   * Returns <code>false</code> otherwise. The result for an illegal move is undefined, but it
   * should throw no exceptions.
   */
  public boolean isLongCastling(
      Position pos, Square startingSquare, Square endingSquare, ChessPiece promotionTarget) {

    if (promotionTarget != null) return false;

    ChessPiece movingPiece = (ChessPiece) pos.getPieceAt(startingSquare);
    ChessPiece takenPiece = (ChessPiece) pos.getPieceAt(endingSquare);

    if (takenPiece != null) return false;

    if (movingPiece == ChessPiece.WHITE_KING) {
      if (!startingSquare.equals("e1")) return false;
      else if (!endingSquare.equals("c1")) return false;
      else if (pos.getPieceAt("a1") != ChessPiece.WHITE_ROOK) return false;
      else if (pos.getPieceAt("b1") != null) return false;
      else if (pos.getPieceAt("d1") != null) return false;
      else return true;
    } else if (movingPiece == ChessPiece.BLACK_KING) {
      if (!startingSquare.equals("e8")) return false;
      else if (!endingSquare.equals("c8")) return false;
      else if (pos.getPieceAt("a8") != ChessPiece.BLACK_ROOK) return false;
      else if (pos.getPieceAt("b8") != null) return false;
      else if (pos.getPieceAt("d8") != null) return false;
      else return true;
    } else return false;
  }

  /**
   * Returns the piece captured by the move defined by the given arguments. Returns <code>null
   * </code> if none. The result for an illegal move is undefined, but it should throw no
   * exceptions. This is a convenience method which determines whether the move defined by the given
   * properties is an en-passant by invoking {@link #isEnPassant(Position, Square, Square,
   * ChessPiece)}. Keep in mind that the called method may be overriden.
   */
  public ChessPiece getCapturedPiece(
      Position pos, Square startingSquare, Square endingSquare, ChessPiece promotionTarget) {

    boolean isEnPassant = isEnPassant(pos, startingSquare, endingSquare, promotionTarget);
    return getCapturedPiece(pos, startingSquare, endingSquare, promotionTarget, isEnPassant);
  }

  /**
   * Returns the piece captured by the move defined by the given arguments. Returns <code>null
   * </code> if none. The result for an illegal move is undefined, but it should throw no
   * exceptions.
   */
  public ChessPiece getCapturedPiece(
      Position pos,
      Square startingSquare,
      Square endingSquare,
      ChessPiece promotionTarget,
      boolean isEnPassant) {

    ChessPiece movingPiece = (ChessPiece) pos.getPieceAt(startingSquare);
    ChessPiece takenPiece = (ChessPiece) pos.getPieceAt(endingSquare);

    if (isEnPassant) return (movingPiece.isWhite() ? ChessPiece.BLACK_PAWN : ChessPiece.WHITE_PAWN);
    else return takenPiece;
  }

  /**
   * Returns the double pawn push file of the move defined by the given arguments, or -1 if that
   * move is not a double pawn push.
   */
  public int getDoublePawnPushFile(Position pos, Square startingSquare, Square endingSquare) {
    Piece piece = pos.getPieceAt(startingSquare);
    if (((piece == ChessPiece.WHITE_PAWN)
            && (startingSquare.getRank() == 1)
            && (endingSquare.getRank() == 3)
            && (startingSquare.getFile() == endingSquare.getFile())
            && (pos.getPieceAt(Square.getInstance(endingSquare.getFile(), 2)) == null))
        || ((piece == ChessPiece.BLACK_PAWN)
            && (startingSquare.getRank() == 6)
            && (endingSquare.getRank() == 4)
            && (startingSquare.getFile() == endingSquare.getFile())
            && (pos.getPieceAt(Square.getInstance(endingSquare.getFile(), 5)) == null)))
      return startingSquare.getFile();
    else return -1;
  }

  /**
   * Returns normally (and does nothing) if the given Position object can be used with this
   * WildVariant. Throws an IllegalArgumentException if the given Position object cannot be used
   * with this WildVariant. The default implementation throws an IllegalArgumentException if the
   * given Position object's wild variant is different from this one. The comparison is done by
   * calling the <code>equals(Object)</code> method.
   */
  protected void checkPosition(Position pos) {
    if (!pos.getVariant().equals(this))
      throw new IllegalArgumentException("Wrong position variant: " + pos.getVariant());
  }

  /**
   * Sets the initial position of this chess variant on the given Position object.
   *
   * @throws IllegalArgumentException If the given Position is incompatible with this WildVariant as
   *     defined by {@link #checkPosition(Position)}
   */
  @Override
  public void init(Position pos) {
    checkPosition(pos);

    pos.setFEN(initialPositionFEN);
  }

  /**
   * If the a move created by the given starting square and ending square in the given position is a
   * promotion, returns an array containing a knight, bishop, rook and queen of the color of the
   * promoted pawn. Otherwise returns null. If you want to use this method from the implementation
   * of some other wild variant, use the static {@link #getChessPromotionTargets(Position, Square,
   * Square)} method instead - it doesn't check the wild variant of the position.
   *
   * @throws IllegalArgumentException If the given Position is incompatible with this WildVariant as
   *     defined by {@link #checkPosition(Position)}
   */
  @Override
  public Piece[] getPromotionTargets(Position pos, Square startingSquare, Square endingSquare) {
    checkPosition(pos);

    return getChessPromotionTargets(pos, startingSquare, endingSquare);
  }

  /**
   * Returns the same thing as {@link #getPromotionTargets(Position, Square, Square)}, only it's
   * static and doesn't check the wild variant of the position. This method is useful for other wild
   * variants who have the same promotion targets and don't want to rewrite the functionality. The
   * only constraint is that the piece at the starting square has to be a ChessPiece.
   */
  public static Piece[] getChessPromotionTargets(
      Position pos, Square startingSquare, Square endingSquare) {
    ChessPiece movingPiece = (ChessPiece) pos.getPieceAt(startingSquare);

    if ((endingSquare.getRank() == 7) && (movingPiece == ChessPiece.WHITE_PAWN))
      return WHITE_PROMOTION_TARGETS.clone();

    if ((endingSquare.getRank() == 0) && (movingPiece == ChessPiece.BLACK_PAWN))
      return BLACK_PROMOTION_TARGETS.clone();

    return null;
  }

  /**
   * Creates a ChessMove from the given starting square and ending square in the given Position. If
   * the move is not a promotion, promotionTarget should be null. The ChessMove is created with the
   * constructor which takes all the properties. The properties are determined by calling the
   * various methods in this class that determine those properties. This means that overriding these
   * methods and redefining their behaviour is the only thing needed to implement variants which are
   * mildly different from Chess.
   *
   * @throws IllegalArgumentException If the given Position is incompatible with this WildVariant as
   *     defined by {@link #checkPosition(Position)}
   * @throws IllegalArgumentException If the promotionTarget is not null and is not an instance of
   *     <code>ChessPiece</code>.
   * @throws IllegalArgumentException If the there is no piece at the starting square.
   */
  @Override
  public Move createMove(
      Position pos,
      Square startingSquare,
      Square endingSquare,
      Piece promotionTarget,
      String moveSAN) {

    checkPosition(pos);

    return createChessMove(pos, startingSquare, endingSquare, promotionTarget, moveSAN);
  }

  /**
   * Creates a <code>ChessMove</code> from the specified parameters as documented in {@link
   * #createMove(Position, Square, Square, Piece, String)} This method is here solely for the
   * benefit of <code>WildVariant</code> implementations which need to create normal chess moves but
   * don't want to reimplement this method. Note that because this method is not static, to actually
   * use it, you still need to obtain an instance of <code>Chess</code> via <code>
   * Chess.getInstance()</code>. Unlike the {@link #createMove(Position, Square, Square, Piece,
   * String)} method, this method does not enforce the wild variant of the position by calling
   * {@link #checkPosition(Position)}.
   */
  public ChessMove createChessMove(
      Position pos,
      Square startingSquare,
      Square endingSquare,
      Piece promotionTarget,
      String moveSAN) {
    // This method is not static because it calls non-static methods (isEnPassant and such).
    // It's still possible to use it in a static-like manner with:
    // Chess.getInstance().createChessMove(...)

    if ((promotionTarget != null) && !(promotionTarget instanceof ChessPiece))
      throw new IllegalArgumentException(
          "Wrong promotion target type: " + promotionTarget.getClass());

    Piece movingPiece = pos.getPieceAt(startingSquare);

    if (movingPiece == null) throw new IllegalArgumentException("The moving piece may not be null");

    Player movingPlayer = movingPiece.getPlayer();
    ChessPiece promotionChessTarget = (ChessPiece) promotionTarget;

    boolean isEnPassant = isEnPassant(pos, startingSquare, endingSquare, promotionChessTarget);
    boolean isShortCastling =
        isShortCastling(pos, startingSquare, endingSquare, promotionChessTarget);
    boolean isLongCastling =
        isLongCastling(pos, startingSquare, endingSquare, promotionChessTarget);
    ChessPiece capturedPiece =
        getCapturedPiece(pos, startingSquare, endingSquare, promotionChessTarget, isEnPassant);
    int doublePawnPushFile = getDoublePawnPushFile(pos, startingSquare, endingSquare);

    return new ChessMove(
        startingSquare,
        endingSquare,
        movingPlayer,
        isEnPassant,
        isShortCastling,
        isLongCastling,
        capturedPiece,
        doublePawnPushFile,
        promotionChessTarget,
        moveSAN);
  }

  /**
   * Creates a <code>Move</code> object representing a move just like the specified one, but made in
   * the specified position.
   */
  @Override
  public Move createMove(Position pos, Move move) {
    checkPosition(pos);

    if (!(move instanceof ChessMove))
      throw new IllegalArgumentException("Wrong move type: " + move.getClass());

    ChessMove cmove = (ChessMove) move;
    return createMove(
        pos,
        cmove.getStartingSquare(),
        cmove.getEndingSquare(),
        cmove.getPromotionTarget(),
        cmove.getStringRepresentation());
  }

  /** Creates a short castling move for the current player in the specified position. */
  @Override
  public Move createShortCastling(Position pos) {
    checkPosition(pos);

    Player currentPlayer = pos.getCurrentPlayer();
    if (currentPlayer.isWhite()) return WHITE_SHORT_CASTLING;
    else return BLACK_SHORT_CASTLING;
  }

  /** Creates a long castling move for the current player in the specified position. */
  @Override
  public Move createLongCastling(Position pos) {
    checkPosition(pos);

    Player currentPlayer = pos.getCurrentPlayer();
    if (currentPlayer.isWhite()) return WHITE_LONG_CASTLING;
    else return BLACK_LONG_CASTLING;
  }

  /**
   * Makes the given ChessMove in the given Position. <B>This method shoudln't (and can't) be called
   * directly - call {@link Position#makeMove(Move)} instead.</B>
   *
   * <p>If you want to use this method from an implementation of some other wild variant, use the
   * static {@link #makeChessMove(ChessMove, Position, Position.Modifier)} method instead - it
   * doesn't check the wild variant of the position.
   *
   * @throws IllegalArgumentException If the given Position is incompatible with this WildVariant as
   *     defined by {@link #checkPosition(Position)}
   * @throws IllegalArgumentException if the given Move is not an instance of <code>ChessMove</code>
   *     .
   */
  @Override
  public void makeMove(Move move, Position pos, Position.Modifier modifier) {
    checkPosition(pos); // Practically redundant as (almost) nobody can call this
    // method except a method in the Position class, which will
    // be of this wild variant anyway.

    if (!(move instanceof ChessMove))
      throw new IllegalArgumentException("Wrong move type: " + move.getClass());

    ChessMove cmove = (ChessMove) move;

    makeChessMove(cmove, pos, modifier);
  }

  /**
   * Makes the given ChessMove on the given Position using the given position modifier. This method
   * is here solely for the benefit of WildVariant implementations which sometimes need to make a
   * regular ChessMove on a position and don't want to reimplement this method. Note that because
   * this method is not static, to actually use it, you still need to obtain an instance of <code>
   * Chess</code> via <code>Chess.getInstance()</code>. Unlike the {@link #makeMove(Move, Position,
   * Position.Modifier)} method, this method does not enforce the wild variant of the position by
   * calling {@link #checkPosition(Position)}.
   */
  public void makeChessMove(ChessMove cmove, Position pos, Position.Modifier modifier) {
    // This method is not static in solidarity with the createChessMove method
    // which has trouble being static due to using non-static methods (isEnPassant and such).
    // It's still possible to use it in a static-like manner with:
    // Chess.getInstance().makeChessMove(...)

    Square startingSquare = cmove.getStartingSquare();
    Square endingSquare = cmove.getEndingSquare();
    ChessPiece movingPiece = (ChessPiece) pos.getPieceAt(startingSquare);

    modifier.setPieceAt(null, startingSquare);
    if (cmove.isPromotion()) modifier.setPieceAt(cmove.getPromotionTarget(), endingSquare);
    else modifier.setPieceAt(movingPiece, endingSquare);

    if (cmove.isEnPassant())
      modifier.setPieceAt(
          null, Square.getInstance(endingSquare.getFile(), startingSquare.getRank()));

    if (cmove.isCastling()) {
      int rookStartFile = cmove.isShortCastling() ? 7 : 0;
      int rookEndFile = cmove.isShortCastling() ? 5 : 3;

      Square rookStartingSquare = Square.getInstance(rookStartFile, startingSquare.getRank());
      Square rookEndingSquare = Square.getInstance(rookEndFile, startingSquare.getRank());
      ChessPiece rook = (ChessPiece) pos.getPieceAt(rookStartingSquare);

      modifier.setPieceAt(null, rookStartingSquare);
      modifier.setPieceAt(rook, rookEndingSquare);
    }

    modifier.setCurrentPlayer(cmove.getPlayer().getOpponent());
  }

  /**
   * Returns what is described by the {@link #parseChessPiece(String)} method.
   *
   * <p>If you want to use this method from an implementation of some other wild variant, use the
   * static {@link #parseChessPiece(String)} method instead.
   *
   * @param piece The string representing the piece.
   * @throws IllegalArgumentException if the string is not in the correctformat.
   */
  @Override
  public Piece parsePiece(String piece) {
    return parseChessPiece(piece);
  }

  /**
   * Calls <code>ChessPiece.fromShortString(String)</code> to parse the piece.
   *
   * @param piece The string representing the piece.
   * @throws IllegalArgumentException if the string is not in the correctformat.
   */
  public static ChessPiece parseChessPiece(String piece) {
    return ChessPiece.fromShortString(piece);
  }

  /**
   * Returns what is described by the {@link #chessPieceToString(ChessPiece)} method.
   *
   * <p>If you want to use this method from an implementation of some other wild variant, use the
   * static {@link #chessPieceToString(ChessPiece)} method instead.
   *
   * @param piece The string representing the piece.
   * @throws IllegalArgumentException if the string is not in the correctformat.
   */
  @Override
  public String pieceToString(Piece piece) {
    if (!(piece instanceof ChessPiece))
      throw new IllegalArgumentException("The piece must be an instance of ChessPiece.");

    return chessPieceToString((ChessPiece) piece);
  }

  /**
   * Calls <code>ChessPiece.toShortString()</code> to obtain a textual representation of the piece.
   */
  public static String chessPieceToString(ChessPiece piece) {
    return piece.toShortString();
  }

  /** {@inheritDoc} */
  @Override
  public Collection getTargetSquares(Position pos, Square square) {
    checkPosition(pos);

    return getChessTargetSquares(pos, square);
  }

  /**
   * Same as {@link #getTargetSquares(Position, Square)}, but does not check that the wild variant
   * of the specified position is <code>this</code> variant. This method is here for the benefit of
   * <code>WildVariant</code>s which don't subclass this class, but have the same target squares as
   * normal chess. The method isn't static because it uses instance methods such as {@link
   * #isEnPassant(Position, Square, Square, ChessPiece)}, which may be overridden, but it may be
   * used via {@link Chess#getInstance()} from a non-subclass variant.
   */
  public Collection getChessTargetSquares(Position pos, Square square) {
    ChessPiece piece = (ChessPiece) pos.getPieceAt(square);
    if (piece == null) return Collections.EMPTY_SET;

    if (piece.isKing()) return getKingTargetSquares(pos, square);
    else if (piece.isQueen()) return getQueenTargetSquares(pos, square);
    else if (piece.isRook()) return getRookTargetSquares(pos, square);
    else if (piece.isBishop()) return getBishopTargetSquares(pos, square);
    else if (piece.isKnight()) return getKnightTargetSquares(pos, square);
    else if (piece.isPawn()) return getPawnTargetSquares(pos, square);
    else throw new IllegalStateException("Unknown piece: " + piece);
  }

  /** Returns the target squares for a piece which jumps to its destination (king and knight). */
  public static Collection getJumpingTargetSquares(Position pos, Square square, int[][] offsets) {
    List targetSquares = new LinkedList();

    int file = square.getFile();
    int rank = square.getRank();
    int color = pos.getPieceAt(square).getColor();

    // Things get quite complicated when the moved piece does not belong to the
    // player whose turn it currently is. In such a case, we simply allow all
    // moves by the piece that would be possible on an empty board.
    boolean isMyTurn = pos.getCurrentPlayer().getPieceColor() == color;

    for (int i = 0; i < offsets.length; i++) {
      int[] offset = offsets[i];
      Square targetSquare = Square.getInstanceNonStrict(file + offset[0], rank + offset[1]);
      if (targetSquare == null) continue;

      Piece piece = pos.getPieceAt(targetSquare);
      if ((piece == null) || (piece.getColor() != color) || !isMyTurn)
        targetSquares.add(targetSquare);
    }

    return targetSquares;
  }

  /**
   * Returns the target squares for a piece which slides to its destination (queen, rook, bishop).
   */
  public static Collection getSlidingTargetSquares(
      Position pos, Square square, int[][] directions) {
    List targetSquares = new LinkedList();

    int file = square.getFile();
    int rank = square.getRank();
    int color = pos.getPieceAt(square).getColor();

    // Things get quite complicated when the moved piece does not belong to the
    // player whose turn it currently is. In such a case, we simply allow all
    // moves by the piece that would be possible on an empty board.
    boolean isMyTurn = pos.getCurrentPlayer().getPieceColor() == color;

    for (int i = 0; i < directions.length; i++) {
      int[] direction = directions[i];
      int fileDirection = direction[0];
      int rankDirection = direction[1];

      Square targetSquare = Square.getInstanceNonStrict(file + fileDirection, rank + rankDirection);
      while (targetSquare != null) {
        Piece piece = pos.getPieceAt(targetSquare);
        if ((piece == null) || (piece.getColor() != color) || !isMyTurn)
          targetSquares.add(targetSquare);

        if ((piece != null) && isMyTurn) break;

        targetSquare =
            Square.getInstanceNonStrict(
                targetSquare.getFile() + fileDirection, targetSquare.getRank() + rankDirection);
      }
    }

    return targetSquares;
  }

  /** King move offsets. */
  private static final int[][] KING_OFFSETS =
      new int[][] {
        new int[] {-1, -1},
        new int[] {0, -1},
        new int[] {1, -1},
        new int[] {-1, 0},
        new int[] {1, 0},
        new int[] {-1, 1},
        new int[] {0, 1},
        new int[] {1, 1}
      };

  /** Returns target squares for a king. */
  protected Collection getKingTargetSquares(Position pos, Square square) {
    Collection targetSquares = getJumpingTargetSquares(pos, square, KING_OFFSETS);

    // Attempt castling.
    for (int file = 0; file < 8; file++) {
      Square targetSquare = Square.getInstance(file, square.getRank());
      if (isShortCastling(pos, square, targetSquare, null)
          || isLongCastling(pos, square, targetSquare, null)) targetSquares.add(targetSquare);
    }

    return targetSquares;
  }

  /** Queen move directions. */
  private static final int[][] QUEEN_DIRECTIONS =
      new int[][] {
        new int[] {-1, -1},
        new int[] {0, -1},
        new int[] {1, -1},
        new int[] {-1, 0},
        new int[] {1, 0},
        new int[] {-1, 1},
        new int[] {0, 1},
        new int[] {1, 1}
      };

  /** Returns target squares for a queen. */
  protected Collection getQueenTargetSquares(Position pos, Square square) {
    return getSlidingTargetSquares(pos, square, QUEEN_DIRECTIONS);
  }

  /** Rook move directions. */
  private static final int[][] ROOK_DIRECTIONS =
      new int[][] {new int[] {0, -1}, new int[] {-1, 0}, new int[] {1, 0}, new int[] {0, 1}};

  /** Returns target squares for a rook. */
  protected Collection getRookTargetSquares(Position pos, Square square) {
    return getSlidingTargetSquares(pos, square, ROOK_DIRECTIONS);
  }

  /** Bishop move directions. */
  private static final int[][] BISHOP_DIRECTIONS =
      new int[][] {new int[] {-1, -1}, new int[] {1, -1}, new int[] {-1, 1}, new int[] {1, 1}};

  /** Returns target squares for a bishop. */
  protected Collection getBishopTargetSquares(Position pos, Square square) {
    return getSlidingTargetSquares(pos, square, BISHOP_DIRECTIONS);
  }

  /** Knight move offsets. */
  private static final int[][] KNIGHT_OFFSETS =
      new int[][] {
        new int[] {-1, -2},
        new int[] {1, -2},
        new int[] {-2, -1},
        new int[] {2, -1},
        new int[] {-2, 1},
        new int[] {2, 1},
        new int[] {-1, 2},
        new int[] {1, 2},
      };

  /** Returns target squares for a knight. */
  protected Collection getKnightTargetSquares(Position pos, Square square) {
    return getJumpingTargetSquares(pos, square, KNIGHT_OFFSETS);
  }

  /** Returns target squares for a pawn. */
  protected Collection getPawnTargetSquares(Position pos, Square square) {
    List targetSquares = new LinkedList();

    int file = square.getFile();
    int rank = square.getRank();
    int color = pos.getPieceAt(square).getColor();

    // Things get quite complicated when the moved piece does not belong to the
    // player whose turn it currently is. In such a case, we simply allow all
    // moves by the piece that would be possible on an empty board.
    boolean isMyTurn = pos.getCurrentPlayer().getPieceColor() == color;

    int rankMoveDirection = color == Piece.WHITE ? 1 : -1;
    boolean isInitialRank = (rank == (7 + rankMoveDirection) % 7);
    boolean isEnPassantRank = (rank == (7 + 4 * rankMoveDirection) % 7);

    Square targetSquare;

    // Move forward
    targetSquare = Square.getInstanceNonStrict(file, rank + rankMoveDirection);
    if (((targetSquare != null) && (pos.getPieceAt(targetSquare) == null)) || !isMyTurn) {
      targetSquares.add(targetSquare);

      // Double pawn push
      if (isInitialRank) {
        targetSquare = Square.getInstance(file, rank + 2 * rankMoveDirection);
        if ((pos.getPieceAt(targetSquare) == null) || !isMyTurn) targetSquares.add(targetSquare);
      }
    }

    // Capture or en-passant
    int[] fileMoveDirections = new int[] {-1, 1};
    for (int i = 0; i < fileMoveDirections.length; i++) {
      int fileMoveDirection = fileMoveDirections[i];
      targetSquare =
          Square.getInstanceNonStrict(file + fileMoveDirection, rank + rankMoveDirection);
      if (targetSquare != null) {
        Piece targetPiece = pos.getPieceAt(targetSquare);
        if (((targetPiece != null) && (targetPiece.getColor() != color)) || !isMyTurn)
          targetSquares.add(targetSquare);
        else if (isEnPassantRank && isEnPassant(pos, square, targetSquare, null))
          targetSquares.add(targetSquare);
      }
    }

    return targetSquares;
  }

  /** Returns an instance of DefaultPiecePainter. */
  @Override
  public PiecePainter createDefaultPiecePainter() {
    return new DefaultPiecePainter();
  }

  /** Returns an instance of DefaultBoardPainter. */
  @Override
  public BoardPainter createDefaultBoardPainter() {
    return new DefaultBoardPainter();
  }

  /** Returns the name of this wild variant. */
  @Override
  public String getName() {
    return variantName;
  }

  /** Returns a textual representation of this wild variant. */
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
