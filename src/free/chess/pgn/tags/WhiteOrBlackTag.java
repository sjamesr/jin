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
 * A representation of either the White or Black PGN tag. This class is package
 * friendly and abstract - it should not be used directly. Instead, use one of 
 * its subclasses - either WhiteTag or BlackTag.
 */

abstract class WhiteOrBlackTag extends Tag{


  /**
   * An array of the names of players.
   */

  private final String [] playerNames;



  /**
   * Creates a new WhiteOrBlackTag with the specified tag name and player name.
   * The player name may be <code>null</code> to specify that the name of the 
   * player is unknown. The player name may not be an empty string. The
   * formatting of the player name according to the pgn spec
   * ("[last name], [first name] [middle name initials]") is left to the user.
   */

  public WhiteOrBlackTag(String tagName, String playerName){
    super(tagName);
    if ("".equals(playerName))
      throw new IllegalArgumentException("The name may not be an empty string");

    playerNames = new String[1];
    playerNames[0] = playerName;
  }




  /**
   * Creates a new WhiteOrBlackTag with the specified tag name, first and last
   * names of the player. Either of the names may be replaced with its initial
   * followed by a period. Neither of the names may be <code>null</code> or
   * empty strings.
   */

  public WhiteOrBlackTag(String tagName, String firstName, String lastName){
    this(tagName, createPlayerName(firstName, lastName, ""));
  }




  /**
   * Creates a new WhiteOrBlackTag with the specified tag name, first, last
   * names of the player and middle name initials. Either of the names may be
   * replaced with its initial followed by a period. Neither of the names may
   * be <code>null</code>. Only the middle name initials may be an empty string.
   * The middle name initials will be used as is. Each middle name initial must
   * be followed by a period.
   */

  public WhiteOrBlackTag(String tagName, String firstName, String lastName, String middleNameInitials){
    this(tagName, createPlayerName(firstName, lastName, middleNameInitials));
  }




  /**
   * Creates a new WhiteOrBlackTag with the specified tag name and names of 
   * players playing with either the white or black pieces. Neither of the names
   * may be <code>null</code> or empty strings. The specified array may not be
   * <code>null</code> and its length may not be 0. The formatting of the names 
   * according to the pgn spec
   * ("[last name], [first name] [middle name initials]") is the responsibility
   * of the caller.
   */

  public WhiteOrBlackTag(String tagName, String [] playerNames){
    super(tagName);
    if (playerNames == null)
      throw new IllegalArgumentException("The player names array may not be null");
    if (playerNames.length == 0)
      throw new IllegalArgumentException("The player names array may not be empty");

    this.playerNames = new String[playerNames.length];
    for (int i = 0; i < playerNames.length; i++){
      String playerName = playerNames[i];
      if (playerName == null)
        throw new IllegalArgumentException("The name at index "+(i+1)+" is null");
      if (playerName.equals(""))
        throw new IllegalArgumentException("The name at index "+(i+1)+" is an empty string");

      this.playerNames[i] = playerName;
    }
  }



  /**
   * Creates a White or Black tag value from the specified arguments so that the
   * value corresponds to the PGN spec. Neither of the names may be
   * <code>null</code>. The middle name initials may not be <code>null</code>.
   * Only the middle name initials may be an empty string. The middle name
   * initials will be used as is. Each middle name initial must be followed by
   * a period.
   */

  private static String createPlayerName(String firstName, String lastName, String middleNameInitials){
    if (firstName == null)
      throw new IllegalArgumentException("The first name may not be null");
    if (firstName.equals(""))
      throw new IllegalArgumentException("The first name may not be an empty string");
    if (lastName == null)
      throw new IllegalArgumentException("The last name may not be null");
    if (lastName.equals(""))
      throw new IllegalArgumentException("The last name may not be an empty string");
    if (middleNameInitials == null)
      throw new IllegalArgumentException("The middle name initials string may not be null");

    StringBuffer playerName = new StringBuffer();
    playerName.append(firstName);
    playerName.append(", ");
    playerName.append(lastName);
    if (!middleNameInitials.equals("")){
      playerName.append(" ");
      playerName.append(middleNameInitials);
    }
    
    return playerName.toString();
  }




  /**
   * Returns the name of the white/black player (the name of the first
   * white/black player, if there is more than one) or <code>null</code> if the
   * name of the player is unknown.
   */

  public final String getPlayerName(){
    return playerNames[0];
  }



  /**
   * Returns the amount of white/black players.
   */

  public final int getPlayerCount(){
    return playerNames.length;
  }



  /**
   * Returns the <code>n</code>th white/black player. Note that the index is
   * 1-based, not 0-based. The index must be within the appropriate range.
   */

  public final String getPlayerName(int index){
    if ((index < 1) || (index > playerNames.length))
      throw new IllegalArgumentException("The player index is out of range: "+index);

    return playerNames[index-1];
  }



  /**
   * Returns the value of the tag.
   */

  public final String getValue(){
    if (getPlayerCount() == 1){
      String playerName = getPlayerName();
      if (playerName == null)
        return "?";
      else
        return playerName;
    }

    // Copy and sort the array. The array size is usually either 1 or a very
    // small number, so bubblesort should be fine.
    String [] copy = (String [])playerNames.clone();
    for (int i = 0; i < copy.length; i++)
      for (int j = i+1; j < copy.length; j++){
        if (copy[j].compareTo(copy[j-1]) < 0){
          String temp = copy[j];
          copy[j] = copy[j-1];
          copy[j-1] = temp;
        }
      }

    StringBuffer value = new StringBuffer(copy[0]);
    for (int i = 1; i < copy.length; i++){
      value.append(":");
      value.append(copy[i]);
    }

    return value.toString();
  }


}