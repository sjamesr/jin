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

package free.jin;

import java.util.Hashtable;
import free.chess.Position;
import free.chess.WildVariant;
import free.chess.Player;


/**
 * Represents a game of chess (or any other variant) on a chess server.
 * This class includes many features that are not supported by some servers - 
 * it's up to the JinConnection implementation to emulate them as closely as
 * possible. If it's genuinely impossibly to implement the feature, the 
 * implementation should provide a reasonable blank value.
 * Server specific subclasses should add methods for retrieving all the 
 * information provided by the server. Game instances are immutable and so
 * should instances of Game subclasses be (There is an exception to this - see
 * the <code>setInitialPosition</code> method).
 */

public class Game{


  /**
   * The constant for the user's game.
   */

  public static final int MY_GAME = 1;



  /**
   * The constant for an observed game.
   */

  public static final int OBSERVED_GAME = 2;



  /**
   * The constant for a game which is just an isolated peek into someone's game.
   */

  public static final int ISOLATED_BOARD = 3;



  /**
   * All of the game properties are kept in this Hashtable. There are just too
   * many of them to be confortably kept in instance variables :-)
   */

  private final Hashtable gameProperties = new Hashtable(100, 0.25f);




  /**
   * The initial position.
   */

  private final Position initialPosition; 



  /**
   * Creates a new Game with the given game properties.
   *
   * @param gameType The type of the game - possible values are {@link #MY_GAME},
   * {@link #OBSERVED_GAME} and {@link #ISOLATED_BOARD}.
   * @param initialPosition The initial position in the game.
   * @param whiteName The name of the player with the white pieces.
   * @param blackName The name of the player with the black pieces.
   * @param whiteTime The initial amount of time on the white player's clock,
   * in milliseconds.
   * @param whiteInc The amount of time the white player's clock is incremented by
   * after each move he makes, in milliseconds.
   * @param blackTime The initial amount of time on the black player's clock,
   * in milliseconds.
   * @param blackInc The amount of time the black player's clocks is incremented by
   * after each move he makes, in milliseconds.
   * @param whiteRating The rating of the player with the white pieces, or -1 if
   * no rating (guest for example).
   * @param blackRating The rating of the player with the black pieces, or -1 if
   * no rating (guest for example).
   * @param isRated Is the game rated?
   * @param gameID A string identifying the game for the user.
   * @param ratingCategoryString A string identifying the rating category of the game 
   * ("Blitz" for example)
   * @param isPlayed Is this a real game? <code>false</code> if an examined game.
   * @param whiteTitles The titles of the player with the white pieces.
   * @param blackTitles the titles of the player with the black pieces.
   * @param initiallyFlipped Whether the board should be flipped initially.
   * @param userPlayer The Player for whom the user plays. It's only meaningful 
   * if this Game is of type MY_GAME and is a played game, this parameter should
   * be null for all other cases.
   */

  public Game(int gameType, Position initialPosition, String whiteName, String blackName, int whiteTime,
          int whiteInc, int blackTime, int blackInc, int whiteRating, int blackRating, String gameID, 
          String ratingCategoryString, boolean isRated, boolean isPlayed, String whiteTitles, 
          String blackTitles, boolean initiallyFlipped, Player userPlayer){

    switch(gameType){
      case MY_GAME:
      case OBSERVED_GAME:
      case ISOLATED_BOARD:
        break;
      default:
        throw new IllegalArgumentException("Unknown game type: "+gameType);
    }

    setProperty("GameType", new Integer(gameType));
    setProperty("WhiteName", whiteName);
    setProperty("BlackName", blackName);
    setProperty("WhiteTime", new Integer(whiteTime));
    setProperty("WhiteInc", new Integer(whiteInc));
    setProperty("BlackTime", new Integer(blackTime));
    setProperty("BlackInc", new Integer(blackInc));
    setProperty("WhiteRating", new Integer(whiteRating));
    setProperty("BlackRating", new Integer(blackRating));
    setProperty("IsRated", new Boolean(isRated));
    setProperty("GameID", gameID);
    setProperty("RatingCategoryString", ratingCategoryString);
    setProperty("IsPlayed", new Boolean(isPlayed));
    setProperty("WhiteTitles", whiteTitles);
    setProperty("BlackTitles", blackTitles);
    setProperty("InitiallyFlipped", new Boolean(initiallyFlipped));
    if (userPlayer!=null)
      setProperty("UserPlayer", userPlayer);

    setProperty("IsTimeOdds", new Boolean((whiteTime!=blackTime)||(whiteInc!=blackInc)));

    this.initialPosition = initialPosition;
  }





  /**
   * Creates a new Game with the given game properties.
   *
   * @param initialPosition The initial position in the game.
   * @param whiteName The name of the player with the white pieces.
   * @param blackName The name of the player with the black pieces.
   * @param time The initial amount of time on the players' clock, in milliseconds.
   * @param inc The amount of time the players' clock is incremented by
   * @param whiteRating The rating of the player with the white pieces, or -1 if
   * no rating (guest for example).
   * @param blackRating The rating of the player with the black pieces, or -1 if
   * no rating (guest for example).
   * @param isRated Is the game rated?
   * @param gameID A string identifying the game for the user.
   * @param ratingCategoryString A string identifying the rating category of the game 
   * ("Blitz" for example)
   * @param gameType The type of the game - possible values are {@link #MY_GAME},
   * {@link #OBSERVED_GAME} and {@link #ISOLATED_BOARD}.
   * @param isPlayed Is this a real game? <code>false</code> if an examined game.
   * @param whiteTitles The titles of the player with the white pieces.
   * @param blackTitles the titles of the player with the black pieces.
   * @param initiallyFlipped Whether the board should be flipped initially.
   * @param userPlayer The Player for whom the user plays. It's only meaningful 
   * if this Game is of type MY_GAME and is a played game, this parameter should
   * be null for all other cases.
   */

  public Game(int gameType, Position initialPosition, String whiteName, String blackName, int time,
          int inc, int whiteRating, int blackRating, String gameID, String ratingCategoryString,
          boolean isRated, boolean isPlayed, String whiteTitles, String blackTitles,
          boolean initiallyFlipped, Player userPlayer){

    this(gameType, initialPosition, whiteName, blackName, time, inc, time, inc, whiteRating,
      blackRating, gameID, ratingCategoryString, isRated, isPlayed, whiteTitles, blackTitles, initiallyFlipped, userPlayer);
  }





  /**
   * Returns the type of the game, either {@link #MY_GAME}, {@link #OBSERVED_GAME}
   * or {@link #ISOLATED_BOARD}.
   */

  public int getGameType(){
    return getIntegerProperty("GameType");
  }




  /**
   * Returns a copy of the initial position in the game.
   */

  public Position getInitialPosition(){
    return new Position(initialPosition);
  }




  /**
   * Sets the initial position of the game to the given position. Although Game
   * objects are supposed to be immutable, this exception is necessary because
   * some commands (like clearboard, loadgame) modify the initial position of
   * the game for all practical purposes, and it's not feasible to recreate the
   * game on each such command. Note however, that whoever is modifying this
   * should also take care to notify all the clients of the change.
   */

  public void setInitialPosition(Position newInitPos){
    this.initialPosition.copyFrom(newInitPos);
  }




  /**
   * Returns the WildVariant of this Game.
   */

  public WildVariant getVariant(){
    return initialPosition.getVariant();
  }





  /**
   * Sets the value of the given property. A property may not have its value reset.
   */

  protected final void setProperty(Object property, Object propertyValue){
    Object oldValue = gameProperties.put(property, propertyValue);
    if (oldValue!=null){
      gameProperties.put(property, oldValue);
      throw new IllegalArgumentException("A property's value may not be reset - attempted to reset the value of "+property);
    }
  }




  /**
   * Returns the value of the given property.
   */

  protected final Object getProperty(Object property){
    return gameProperties.get(property);
  }




  /**
   * Returns the value of the given integer property.
   */

  protected final int getIntegerProperty(Object property){
    return ((Integer)getProperty(property)).intValue();
  }




  /**
   * Returns the value of the given string property.
   */

  protected final String getStringProperty(Object property){
    return (String)getProperty(property);
  }




  /**
   * Returns the value of the given boolean property.
   */

  protected final boolean getBooleanProperty(Object property){
    return ((Boolean)getProperty(property)).booleanValue();
  }




  /**
   * Returns the name of the player with the white pieces.
   */

  public String getWhiteName(){
    return getStringProperty("WhiteName");
  }




  /**
   * Returns the name of the player with the black pieces.
   */

  public String getBlackName(){
    return getStringProperty("BlackName");
  }




  /**
   * Returns the initial time on the clock of the player with the white pieces,
   * in milliseconds.
   */

  public int getWhiteTime(){
    return getIntegerProperty("WhiteTime");
  }



  /**
   * Returns the number of milliseconds by which the clock of the white player
   * is incremented each time he makes a move. This could be used to indicate
   * increment in Bronstein-clock style, depending on the clock style used by
   * the server.
   */

  public int getWhiteInc(){
    return getIntegerProperty("WhiteInc");
  }




  /**
   * Returns the initial time on the clock of the player with the black pieces,
   * in milliseconds.
   */

  public int getBlackTime(){
    return getIntegerProperty("BlackTime");
  }




  /**
   * Returns the rating of the player with the white pieces.
   */

  public int getWhiteRating(){
    return getIntegerProperty("WhiteRating");
  } 




  /**
   * Returns the rating of the player with the black pieces.
   */

  public int getBlackRating(){
    return getIntegerProperty("BlackRating");
  } 






  /**
   * Returns the number of milliseconds by which the clock of the black player
   * is incremented each time he makes a move. This could be used to indicate
   * increment in Bronstein-clock style, depending on the clock style used by
   * the server.
   */

  public int getBlackInc(){
    return getIntegerProperty("BlackInc");
  }




  /**
   * Returns true if this game is rated, false otherwise.
   */

  public boolean isRated(){
    return getBooleanProperty("IsRated");
  }




  /**
   * Returns a string identifying the game. This could be displayed to the user,
   * but shouldn't be used as an actual identifier (to map id's to Games for example,
   * or compare if to games are equal according to their id's).
   */

  public String getID(){
    return getStringProperty("GameID");
  }




  /**
   * Returns a String identifying the rating category of the game - this should
   * be displayed to the user.
   */

  public String getRatingCategoryString(){
    return getStringProperty("RatingCategoryString");
  }



  /**
   * Returns true if this is a played game, false if examined.
   */

  public boolean isPlayed(){
    return getBooleanProperty("IsPlayed");
  }



  /**
   * Returns the titles of the player with the white pieces.
   */

  public String getWhiteTitles(){
    return getStringProperty("WhiteTitles");
  }



  /**
   * Returns the titles of the player with the black pieces.
   */

  public String getBlackTitles(){
    return getStringProperty("BlackTitles");
  }



  /**
   * Returns true if the board should be initially flipped.
   */

  public boolean isBoardInitiallyFlipped(){
    return getBooleanProperty("InitiallyFlipped");
  }



  /**
   * Returns true if this is a time-odds game.
   */

  public boolean isTimeOdds(){
    return getBooleanProperty("IsTimeOdds");
  }




  /**
   * Returns the player for whom the user plays. This is only relevant if the
   * game is of type MY_GAME and is a played game, in all other cases, this
   * method returns null.
   */

  public Player getUserPlayer(){
    if ((getGameType()==MY_GAME)&&isPlayed())
      return (Player)getProperty("UserPlayer");
    else
      return null;
  } 




  /**
   * Returns true if the user is allowed to move the pieces of the given Player.
   * Here are the values returned by this method in different cases:
   * <UL>
   *   <LI> The type of this game is MY_GAME and it's a played game - returns
   *        true if the given player is the userPlayer, otherwise returns false.
   *   <LI> The type of this game is MY_GAME and it's an examined game - returns
   *        true always.
   *   <LI> The type of this game is OBSERVED_GAME - returns false.
   *   <LI> The type of this game is ISOLATED_BOARD - returns false, although
   *        implementations may want to do whatever they want in this case (they
   *        can allow the user to move any pieces if they want).
   * </UL>
   */

  public boolean isUserAllowedToMovePieces(Player player){
    int gameType = getGameType();
    if (gameType==OBSERVED_GAME){
      return false;
    }
    else if (gameType==ISOLATED_BOARD){
      return false;
    }
    else{ // MY_GAME
      return player.equals(getProperty("UserPlayer"));
    }
  }




  /**
   * Returns a string representing the time control of the game.
   */

  public String getTCString(){
    String timeString = (getWhiteTime()/(1000*60))+" "+(getWhiteInc()/1000);
    if (isTimeOdds())
      timeString = "("+timeString+") ("+(getBlackTime()/(1000*60))+" "+(getBlackInc()/1000)+")";
    return timeString;
  }




  /**
   * Returns a textual representation of this Game.
   */

  public String toString(){
    return "#"+getID()+" "+(isRated() ? "Rated" : "Unrated") + " " + getTCString() + " " +getVariant().getName()+" " + getWhiteName()+" vs. "+getBlackName();
  }


}
