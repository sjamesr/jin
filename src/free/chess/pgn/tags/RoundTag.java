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

import free.chess.pgn.Tag;


/**
 * A representation of the Round PGN tag. The Round tag value gives the playing
 * round for the game. In a match competition, this value is the number of the
 * game played.
 */

public final class RoundTag extends Tag{


  /**
   * The name of the Round tag - the String "Round".
   */

  public static final String TAG_NAME = "Round";



  /**
   * A constant to be used for the round number when the round number is
   * unknown.
   */

  public static final int ROUND_NUMBER_UNKNOWN = -1;



  /**
   * A constant to be used for the round number when the round number is
   * inappropriate/inapplicable.
   */

  public static final int ROUND_NUMBER_INAPPLICABLE = -2;



  /**
   * A RoundTag instance representing a Round tag with an unknown value.
   */

  public static final RoundTag ROUND_UNKNOWN = new RoundTag(ROUND_NUMBER_UNKNOWN);



  /**
   * A RoundTag instance representing a Round tag for a game where a round
   * tag is inapplicable/inappropriate.
   */

  public static final RoundTag ROUND_INAPPLICABLE = new RoundTag(ROUND_NUMBER_INAPPLICABLE);




  /**
   * An array containing the multipart round numbers (2,4,1 for example, for 
   * round "2.4.1").
   */

  private final int [] roundNumbers;




  /**
   * Creates a new RoundTag with the specified round number. You may use the
   * <code>ROUND_NUMBER_UNKNOWN</code> and
   * <code>ROUND_NUMBER_INAPPLICABLE</code> constants to specify that the round
   * number is either unknown or inapplicable/inappropriate for this game, but
   * it's preferable to use the <code>ROUND_UNKNOWN</code> and
   * <code>ROUND_INAPPLICABLE</code> constant instances.
   */

  public RoundTag(int roundNumber){
    super(TAG_NAME);
    roundNumbers = new int[1];
    roundNumbers[0] = roundNumber;
  }




  /**
   * Creates a new RoundTag with the specified multipart round number. You may
   * not use the <code>ROUND_NUMBER_UNKNOWN</code> and
   * <code>ROUND_NUMBER_INAPPLICABLE</code> constants in this constructor. The
   * specified array may not be <code>null</code> and its length may not be 0.
   */

  public RoundTag(int [] roundNumbers){
    super(TAG_NAME);
    if (roundNumbers == null)
      throw new IllegalArgumentException("The round numbers array may not be null");
    if (roundNumbers.length == 0)
      throw new IllegalArgumentException("The round numbers array may not be empty");

    this.roundNumbers = new int[roundNumbers.length];
    for (int i = 0; i < roundNumbers.length; i++){
      if (roundNumbers[i] <= 0)
        throw new IllegalArgumentException("Bad value for round part "+(i+1)+": "+roundNumbers[i]);
      this.roundNumbers[i] = roundNumbers[i];
    }
  }




  /**
   * Returns the round number (or the first part of the multipart round, if
   * this RoundTag represents a multipart round),
   * <code>ROUND_NUMBER_UNKNOWN</code>, if the round number is unknown, or
   * <code>ROUND_NUMBER_INAPPLICABLE</code> if a round number is unknown for
   * this game.
   */

  public int getRound(){
    return roundNumbers[0];
  }



  /**
   * Returns the number of parts in the multipart round represented by this
   * RoundTag, or 1 if the round is not multipart.
   */

  public int getRoundPartsCount(){
    return roundNumbers.length;
  }



  /**
   * Returns the specified part of the multipart round represented by this
   * RoundTag. The part index must be in the appropriate range. Note that the
   * index is 1-based, not 0-based.
   */

  public int getRoundPart(int index){
    if ((index < 1) || (index > roundNumbers.length))
      throw new IllegalArgumentException("Bad round part index: "+index);

    return roundNumbers[index-1];
  }




  /**
   * Returns the value of this tag.
   */

  public String getValue(){
    StringBuffer value = new StringBuffer(String.valueOf(roundNumbers[0]));
    for (int i = 1; i < roundNumbers.length; i++){
      value.append(".");
      value.append(String.valueOf(roundNumbers[i]));
    }

    return value.toString();
  }


}