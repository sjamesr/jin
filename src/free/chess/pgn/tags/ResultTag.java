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

package free.chess.pgn.tags;


/**
 * A representation of the Result PGN tag. The Result field value is the result
 * of the game. It is always exactly the same as the game termination marker
 * that concludes the associated movetext. It is always one of four possible
 * values: "1-0" (White wins), "0-1" (Black wins), "1/2-1/2" (drawn game), and
 * "*" (game still in progress, game abandoned, or result otherwise unknown).
 * Note that the digit zero is used in both of the first two cases; not the
 * letter "O".
 */

public final class ResultTag extends SimpleTag{


  /**
   * The name of the Result tag - the String "Result".
   */

  public static String TAG_NAME = "Result";



  /**
   * An instance of ResultTag representing a Result tag with a "1-0" value,
   * specifying that white won.
   */

  public static ResultTag RESULT_WHITE_WINS = new ResultTag("1-0");



  /**
   * An instance of ResultTag representing a Result tag with a "0-1" value,
   * specifying that black won.
   */

  public static ResultTag RESULT_BLACK_WINS = new ResultTag("0-1");



  /**
   * An instance of ResultTag representing a Result tag with a "1/2-1/2" value,
   * specifying that the game was drawn.
   */

  public static ResultTag RESULT_GAME_DRAW = new ResultTag("1/2-1/2");



  /**
   * An instance of ResultTag representing a Result tag with a "*" value,
   * specifying that the result of the game is still in progress, was abandoned,
   * or the result is otherwise unknown.
   */

  public static ResultTag RESULT_UNKNOWN = new ResultTag("*");




  /**
   * The sole, private constructor.
   */

  private ResultTag(String value){
    super(TAG_NAME, value);
  }

   
}
