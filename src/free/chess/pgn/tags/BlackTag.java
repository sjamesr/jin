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
 * A representation of the Black PGN tag. The Black tag value is the name of the
 * player or players of the black pieces. The names are given as they would
 * appear in a telephone directory. The family or last name appears first. If a
 * first name or first initial is available, it is separated from the family
 * name by a comma and a space. Finally, one or more middle initials may appear.
 * (Wherever a comma appears, the very next character should be a space.
 * Wherever an initial appears, the very next character should be a period.)
 */

public class BlackTag extends WhiteOrBlackTag{


  /**
   * The name of the Black tag - the String "Black".
   */

  public static final String TAG_NAME = "Black";



  /**
   * A BlackTag instance representing a Black tag with an unknown player name.
   */

  public static final BlackTag BLACK_UNKNOWN = new BlackTag((String)null);



  /**
   * Creates a new BlackTag with the specified player name. 
   * The player name may be <code>null</code> to specify that the name of the 
   * player is unknown, but it is preferable to use the
   * <code>BLACK_UNKNOWN</code> constant instance instead. The player name may
   * not be an empty string. The formatting of the player name according to the
   * pgn spec. ("[last name], [first name] [middle name initials]") is the
   * responsibility of the caller.
   */

  public BlackTag(String playerName){
    super(TAG_NAME, playerName);
  }




  /**
   * Creates a new BlackTag with the specified first and last names of the
   * player. Either of the names may be replaced with its initial
   * followed by a period. Neither of the names may be <code>null</code> or
   * empty strings.
   */

  public BlackTag(String firstName, String lastName){
    super(TAG_NAME, firstName, lastName);
  }



  /**
   * Creates a new BlackTag with the specified first, last names of the player
   * and middle name initials. Either of the names may be replaced with its 
   * initial followed by a period. Neither of the names may be
   * <code>null</code>. Only the middle name initials may be an empty string.
   * The middle name initials will be used as is. Each middle name initial must
   * be followed by a period.
   */

  public BlackTag(String firstName, String lastName, String middleNameInitials){
    super(TAG_NAME, firstName, lastName, middleNameInitials);
  }




  /**
   * Creates a new BlackTag with the specified tag name and names of players 
   * playing with the black pieces. Neither of the names may be
   * <code>null</code> or empty strings. The specified array may not be
   * <code>null</code> and its length may not be 0. The formatting of the names
   * according to the pgn spec
   * ("[last name], [first name] [middle name initials]") is the responsibility
   * of the caller.
   */

  public BlackTag(String [] playerNames){
    super(TAG_NAME, playerNames);
  }



  /**
   * Returns the name of the player with the black pieces (or the name of the
   * first player with the black pieces, if there is more than one), or
   * <code>null</code> if his name is unknown.
   */

  public String getBlackName(){
    return getPlayerName();
  }



  /**
   * Returns the amount of players playing with the black pieces.
   */

  public int getBlackPlayerCount(){
    return getPlayerCount();
  }



  /**
   * Returns the name of the <code>n</code>th player playing with the black
   * pieces. Note that the index is 1-based, not 0-based. The index must be
   * within the appropriate range.
   */

  public String getBlackName(int index){
    return getPlayerName(index);
  }


}
