package free.chess.variants.shatranj;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import free.chess.*;
import free.chess.variants.NoCastlingVariant;



/**
 * Implements the Shatranj wild variant. See
 * <a href="http://www.chessclub.com/help/Shatranj">http://www.chessclub.com/help/Shatranj</A>
 * for a description of Shatranj. 
 *
 * @author Ryan Propper
 * @author Alexander Maryanovsky
 */

public class Shatranj extends NoCastlingVariant{
  
  
  
  /**
   * The FEN representation of the initial position in Shatranj.
   */
  
  public static final String SHATRANJ_INITIAL_POSITION_FEN = "rnbkqbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBKQBNR w - - 0 1";
  
  

  /**
   * Pawns can promote only to a fers (queen) in Shatranj.
   */

  private static final ChessPiece[] WHITE_PROMOTION_TARGETS = new ChessPiece[]{ChessPiece.WHITE_QUEEN};
  
  
  
  /**
   * Pawns can promote only to a fers (queen) in Shatranj.
   */
  
  private static final ChessPiece[] BLACK_PROMOTION_TARGETS = new ChessPiece[]{ChessPiece.BLACK_QUEEN};
  
  
  
  /**
   * The sole instance of this class.
   */
  
  private static final Shatranj INSTANCE = new Shatranj();
  
  
  
  /**
   * Returns an instance of this class.
   */
  
  public static Shatranj getInstance(){
    return INSTANCE;
  }
  
  
  
  /**
   * Creates an instance of Shatranj.
   */

  private Shatranj(){
    super(SHATRANJ_INITIAL_POSITION_FEN, "Shatranj");
  }
  
  
  
  /**
   * If the move created by the given starting square and ending square in the
   * given position is a promotion, returns an array containing a fers (queen)
   * of the color of the promoted pawn. Otherwise returns <code>null</code>.
   */
  
  public Piece[] getPromotionTargets(Position pos, Square startingSquare, Square endingSquare){
    checkPosition(pos);
    
    ChessPiece movingPiece = (ChessPiece) pos.getPieceAt(startingSquare);
    
    if ((endingSquare.getRank() == 7) && (movingPiece == ChessPiece.WHITE_PAWN))
      return (Piece[])WHITE_PROMOTION_TARGETS.clone();
    
    if ((endingSquare.getRank() == 0) && (movingPiece == ChessPiece.BLACK_PAWN))
      return (Piece[])BLACK_PROMOTION_TARGETS.clone();
    
    return null;
  }
  
  
  
  /**
   * Since en passant is not possible in Shatranj, this method always returns
   * <code>false</code>.
   */
  
  public boolean isEnPassant(Position pos, Square startingSquare, Square endingSquare, ChessPiece promotionTarget){
    return false;
  }
  
  
  
  /**
   * Fers (queen) move directions. The fers can only move one square diagonally
   * in any direction.
   */
  
  private static final int[][] FERS_DIRECTIONS = new int[][]{
    new int[]{-1, -1},                   new int[]{1, -1},
    
    new int[]{-1, 1},                    new int[]{1, 1}
  };
  
  
  
  /**
   * Returns target squares for a fers.
   */
  
  private Collection getFersTargetSquares(Position pos, Square square){
    return getJumpingTargetSquares(pos, square, FERS_DIRECTIONS);
  }
  
  
  
  /**
   * Overrides to return the shatranj queen (fers) target squares.
   */
  
  protected Collection getQueenTargetSquares(Position pos, Square square){
    checkPosition(pos);
    
    return getFersTargetSquares(pos, square);
  }
  
  
  
  /**
   * Elephant (bishop) move directions. The elephant moves two squares at a time
   * diagonally, and can jump over a piece on the intermediate square.
   */
  
  private static final int [][] ELEPHANT_DIRECTIONS = new int[][]{
    new int[]{-2, -2},                   new int[]{2, -2},
    
    new int[]{-2, 2},                    new int[]{2, 2}    
  };
  
  
  
  /**
   * Returns target squares for an elephant (bishop).
   */
  
  private Collection getElephantTargetSquares(Position pos, Square square) {
    return getJumpingTargetSquares(pos, square, ELEPHANT_DIRECTIONS); 
  }
  
  
  
  /**
   * Overrides to return the shatranj bishop (elephant) target squares.
   */
  
  protected Collection getBishopTargetSquares(Position pos, Square square){
    checkPosition(pos);
    
    return getElephantTargetSquares(pos, square);
  }
  
  
  
  /**
   * Returns target squares for a pawn. In shatranj there is no double pawn push
   * and no en-passant.
   */

  private Collection getShatranjPawnTargetSquares(Position pos, Square square) {
    Collection chessPawnTargets = super.getPawnTargetSquares(pos, square);
    List targetSquares = new LinkedList();
    Chess chess = Chess.getInstance();
    
    // Filter en-passant and double pawn pushes.
    for (Iterator i = chessPawnTargets.iterator(); i.hasNext();){
      Square targetSquare = (Square)i.next();
      
      // No en-passant in Shatranj
      if (chess.isEnPassant(pos, square, targetSquare, null))
        continue;
      
      // No double pawn push in Shatranj
      if (chess.getDoublePawnPushFile(pos, square, targetSquare) != -1)
        continue;
      
      targetSquares.add(targetSquare);
    }
    
    return targetSquares;
  }
  
  
  
  /**
   * Overrides to return the shatranj pawn target squares.
   */
  
  protected Collection getPawnTargetSquares(Position pos, Square square){
    checkPosition(pos);
    
    return getShatranjPawnTargetSquares(pos, square);
  }
  
  
  
}
