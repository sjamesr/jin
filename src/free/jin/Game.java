/**
 * Jin - a chess client for internet chess servers.
 * More information is available at http://www.jinchess.com/.
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

import java.beans.PropertyChangeListener;
import free.chess.Position;
import free.chess.WildVariant;
import free.chess.Player;
import free.util.BeanProperties;


/**
 * Represents a game of chess (or any other variant) on a chess server.
 * This class includes many features that are not supported by some servers - 
 * it's up to the JinConnection implementation to emulate them as closely as
 * possible. If it's genuinely impossibly to implement the feature, the 
 * implementation should provide a reasonable blank value.
 * Server specific subclasses should add methods for retrieving all the 
 * information provided by the server.
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
   * The constant for a "white wins" result.
   */

  public static final int WHITE_WINS = 1;



  /**
   * The constant for a "black wins" result.
   */

  public static final int BLACK_WINS = 2;



  /**
   * The constant for a "draw" result.
   */

  public static final int DRAW = 3;



  /**
   * The constant for an unknown game result.
   */

  public static final int UNKNOWN_RESULT = 4;



  /**
   * The constant for when the game result is not yet known because the game
   * hasn't finished yet.
   */

  public static final int GAME_IN_PROGRESS = 5;



  /**
   * The <code>BeanProperties</code> object actually holding the properties.
   */

  private final BeanProperties props = new BeanProperties(this);




  /**
   * Creates a new Game with the given game properties.
   *
   * @param gameType The type of the game - possible values are {@link #MY_GAME},
   * {@link #OBSERVED_GAME} and {@link #ISOLATED_BOARD}.
   * @param initialPosition The initial position in the game.
   * @param pliesSinceStart The amount of from the actual beginning of the game
   * to the inital position.
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
   * @param gameID A string identifying the game.
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

  public Game(int gameType, Position initialPosition, int pliesSinceStart, String whiteName,
      String blackName, int whiteTime, int whiteInc, int blackTime, int blackInc, int whiteRating,
      int blackRating, Object gameId, String ratingCategoryString, boolean isRated,
      boolean isPlayed, String whiteTitles, String blackTitles, boolean initiallyFlipped,
      Player userPlayer){

    setGameType(gameType);
    setInitialPosition(initialPosition);
    setPliesSinceStart(pliesSinceStart);
    setWhiteName(whiteName);
    setBlackName(blackName);
    setWhiteTime(whiteTime);
    setWhiteInc(whiteInc);
    setBlackTime(blackTime);
    setBlackInc(blackInc);
    setWhiteRating(whiteRating);
    setBlackRating(blackRating);
    setRated(isRated);
    setId(gameId);
    setRatingCategoryString(ratingCategoryString);
    setPlayed(isPlayed);
    setWhiteTitles(whiteTitles);
    setBlackTitles(blackTitles);
    setBoardInitiallyFlipped(initiallyFlipped);
    setUserPlayer(userPlayer);
  }





  /**
   * Creates a new Game with the given game properties. This constructor differs
   * from the other one in that it doesn't let you specify separate time and
   * increment for white and black.
   *
   * @param initialPosition The initial position in the game.
   * @param pliesSinceStart The amount of from the actual beginning of the game
   * to the inital position.
   * @param whiteName The name of the player with the white pieces.
   * @param blackName The name of the player with the black pieces.
   * @param time The initial amount of time on the players' clock, in milliseconds.
   * @param inc The amount of time the players' clock is incremented by
   * @param whiteRating The rating of the player with the white pieces, or -1 if
   * no rating (guest for example).
   * @param blackRating The rating of the player with the black pieces, or -1 if
   * no rating (guest for example).
   * @param isRated Is the game rated?
   * @param gameID A string identifying the game.
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

  public Game(int gameType, Position initialPosition, int pliesSinceStart, String whiteName,
      String blackName, int time, int inc, int whiteRating, int blackRating, Object gameID,
      String ratingCategoryString, boolean isRated, boolean isPlayed, String whiteTitles,
      String blackTitles, boolean initiallyFlipped, Player userPlayer){

    this(gameType, initialPosition, pliesSinceStart, whiteName, blackName, time, inc, time, inc,
      whiteRating, blackRating, gameID, ratingCategoryString, isRated, isPlayed, whiteTitles,
      blackTitles, initiallyFlipped, userPlayer);
  }




  /**
   * Registers the specified <code>PropertyChangeListener</code>.
   */

  public void addPropertyChangeListener(PropertyChangeListener listener){
    props.addPropertyChangeListener(listener);
  }




  /**
   * Unregisters the specified <code>PropertyChangeListener</code>.
   */

  public void removePropertyChangeListener(PropertyChangeListener listener){
    props.removePropertyChangeListener(listener);
  }




  /**
   * Sets the type of the game, either {@link #MY_GAME}, {@link #OBSERVED_GAME}
   * or {@link #ISOLATED_BOARD}.
   */

  public void setGameType(int gameType){
    switch(gameType){
      case MY_GAME:
      case OBSERVED_GAME:
      case ISOLATED_BOARD:
        break;
      default:
        throw new IllegalArgumentException("Unknown game type: "+gameType);
    }

    props.setIntegerProperty("gameType", gameType);
  }




  /**
   * Returns the type of the game, either {@link #MY_GAME}, {@link #OBSERVED_GAME}
   * or {@link #ISOLATED_BOARD}.
   */

  public int getGameType(){
    return props.getIntegerProperty("gameType");
  }



  /**
   * Sets the initial position of the game to the given position.
   */

  public void setInitialPosition(Position initialPosition){
    Position newInitPos = new Position(initialPosition.getVariant());
    newInitPos.copyFrom(initialPosition);
    props.setProperty("initialPosition", initialPosition);
  }




  /**
   * Returns a copy of the initial position in the game.
   */

  public Position getInitialPosition(){
    Position pos = (Position)props.getProperty("initialPosition");
    return new Position(pos);
  }



  /**
   * Sets the amount of plies made from the beginning of the actual game to the
   * initial position as specified by this <code>Game</code> object.
   */

  public void setPliesSinceStart(int plies){
    if (plies < 0)
      throw new IllegalArgumentException("plies may not be negative");

    props.setIntegerProperty("pliesSinceStart", plies);
  }



  /**
   * Returns the amount of half-moves made from the beginning of the actual game
   * to the initial position as specified by this <code>Game</code> object.
   */

  public int getPliesSinceStart(){
    return props.getIntegerProperty("pliesSinceStart");
  }




  /**
   * Returns the WildVariant of this Game.
   */

  public WildVariant getVariant(){
    return ((Position)props.getProperty("initialPosition")).getVariant();
  }




  /**
   * Sets the name of the player with the white pieces.
   */

  public void setWhiteName(String name){
    props.setStringProperty("whiteName", name);
  }



  /**
   * Returns the name of the player with the white pieces.
   */

  public String getWhiteName(){
    return props.getStringProperty("whiteName");
  }



  /**
   * Sets the name of the player with the black pieces.
   */

  public void setBlackName(String blackName){
    props.setStringProperty("blackName", blackName);
  }



  /**
   * Returns the name of the player with the black pieces.
   */

  public String getBlackName(){
    return props.getStringProperty("blackName");
  }



  /**
   * Sets the initial time on the clock of the player with the white pieces,
   * in milliseconds.
   */

  public void setWhiteTime(int time){
    props.setIntegerProperty("whiteTime", time);
  }


  /**
   * Returns the initial time on the clock of the player with the white pieces,
   * in milliseconds.
   */

  public int getWhiteTime(){
    return props.getIntegerProperty("whiteTime");
  }



  /**
   * Sets the the number of milliseconds by which the clock of the white player
   * is incremented each time he makes a move. 
   */

  public void setWhiteInc(int inc){
    props.setIntegerProperty("whiteInc", inc);
  }



  /**
   * Returns the number of milliseconds by which the clock of the white player
   * is incremented each time he makes a move.
   */

  public int getWhiteInc(){
    return props.getIntegerProperty("whiteInc");
  }



  /**
   * Sets the initial time on the clock of the player with the black pieces,
   * in milliseconds.
   */

  public void setBlackTime(int time){
    props.setIntegerProperty("blackTime", time);
  }



  /**
   * Returns the initial time on the clock of the player with the black pieces,
   * in milliseconds.
   */

  public int getBlackTime(){
    return props.getIntegerProperty("blackTime");
  }




  /**
   * Sets the number of milliseconds by which the clock of the black player
   * is incremented each time he makes a move.
   */

  public void setBlackInc(int time){
    props.setIntegerProperty("blackInc", time);
  }



  /**
   * Returns the number of milliseconds by which the clock of the black player
   * is incremented each time he makes a move.
   */

  public int getBlackInc(){
    return props.getIntegerProperty("blackInc");
  }



  /**
   * Sets the rating of the player with the white pieces.
   */

  public void setWhiteRating(int rating){
    props.setIntegerProperty("whiteRating", rating);
  }


  /**
   * Returns the rating of the player with the white pieces.
   */

  public int getWhiteRating(){
    return props.getIntegerProperty("whiteRating");
  } 




  /**
   * Sets the rating of the player with the black pieces.
   */

  public void setBlackRating(int rating){
    props.setIntegerProperty("blackRating", rating);
  }



  /**
   * Returns the rating of the player with the black pieces.
   */

  public int getBlackRating(){
    return props.getIntegerProperty("blackRating");
  } 



  /**
   * Sets the ratedness of this game.
   */

  public void setRated(boolean rated){
    props.setBooleanProperty("rated", rated);
  }


  /**
   * Returns true if this game is rated, false otherwise.
   */

  public boolean isRated(){
    return props.getBooleanProperty("rated");
  }



  /**
   * Sets the id of this game.
   */

  public void setId(Object id){
    props.setProperty("id", id);
  }



  /**
   * Returns an object identifying the game. This object, when converted to a
   * String (via the toString() method) could be displayed to the user.
   */

  public Object getID(){
    return props.getProperty("id");
  }



  /**
   * Sets the rating category of the game.
   */

  public void setRatingCategoryString(String category){
    props.setStringProperty("ratingCategoryString", category);
  }



  /**
   * Returns a String identifying the rating category of the game - this should
   * be displayed to the user.
   */

  public String getRatingCategoryString(){
    return props.getStringProperty("ratingCategoryString");
  }



  /**
   * Sets whether the game is played or examined.
   */

  public void setPlayed(boolean played){
    props.setBooleanProperty("played", played);
  }



  /**
   * Returns true if this is a played game, false if examined.
   */

  public boolean isPlayed(){
    return props.getBooleanProperty("played");
  }



  /**
   * Sets the titles of the player with the white pieces.
   */

  public void setWhiteTitles(String titles){
    props.setStringProperty("whiteTitles", titles);
  }



  /**
   * Returns the titles of the player with the white pieces.
   */

  public String getWhiteTitles(){
    return props.getStringProperty("whiteTitles");
  }



  /** 
   * Sets the titles of the player with the black pieces.
   */

  public void setBlackTitles(String titles){
    props.setStringProperty("blackTitles", titles);
  }



  /**
   * Returns the titles of the player with the black pieces.
   */

  public String getBlackTitles(){
    return props.getStringProperty("blackTitles");
  }



  /**
   * Sets whether the board should be initially flipped.
   */

  private void setBoardInitiallyFlipped(boolean flipped){
    props.setBooleanProperty("boardInitiallyFlipped", flipped);
  }



  /**
   * Returns true if the board should be initially flipped.
   */

  public boolean isBoardInitiallyFlipped(){
    return props.getBooleanProperty("boardInitiallyFlipped");
  }




  /**
   * Returns true if this is a time-odds game.
   */

  public boolean isTimeOdds(){
    return (getWhiteTime() != getBlackTime()) || (getWhiteInc() != getBlackInc());
  }



  /**
   * Sets the side the user is playing for.
   */

  private void setUserPlayer(Player player){
    props.setProperty("userPlayer", player);
  }



  /**
   * Returns the player for whom the user plays. This is only relevant if the
   * game is of type MY_GAME and is a played game, in all other cases, this
   * method returns null.
   */

  public Player getUserPlayer(){
    if ((getGameType()==MY_GAME)&&isPlayed())
      return (Player)props.getProperty("userPlayer");
    else
      return null;
  } 



  /**
   * Returns the player with the specified name.
   */

  public Player getPlayerNamed(String playerName){
    if (getWhiteName().equals(playerName))
      return Player.WHITE_PLAYER;
    else if (getBlackName().equals(playerName))
      return Player.BLACK_PLAYER;
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
    if (gameType==OBSERVED_GAME)
      return false;
    else if (gameType==ISOLATED_BOARD)
      return false;
    else // MY_GAME
      return player.equals(getUserPlayer());
  }




  /**
   * Returns the result of the game. Possible values are
   * <code>WHITE_WINS</code>, <code>BLACK_WINS</code>, <code>DRAW</code>,
   * <code>UNKNOWN_RESULT</code> and <code>GAME_IN_PROGRESS</code>.
   */

  public int getResult(){
    Integer result = (Integer)props.getProperty("result", null);
    if (result == null)
      return GAME_IN_PROGRESS;
    return result.intValue();
  }




  /**
   * Sets the result of the game to the specified value. Possible values are
   * <code>WHITE_WINS</code>, <code>BLACK_WINS</code>, <code>DRAW</code> and
   * <code>UNKNOWN_RESULT</code>. This method may only be called once per Game
   * object (when the game ends), and the caller is responsible for notifying 
   * all interested parties that the game ended.
   */

  public void setResult(int result){
    switch(result){
      case WHITE_WINS:
      case BLACK_WINS:
      case DRAW:
      case UNKNOWN_RESULT:
        break;
      default:
        throw new IllegalArgumentException("Bad value for game result specified: "+result);
    }

    if (getResult() != GAME_IN_PROGRESS)
      throw new IllegalStateException("Unable to set the result more than once");

    props.setProperty("result", new Integer(result));
  }




  /**
   * Returns a string representing the time control of the game.
   */

  public String getTCString(){
    String timeString = ""+(getWhiteTime()/(1000*60))+" "+(getWhiteInc()/1000);
    if (isTimeOdds())
      return "("+timeString+") ("+(getBlackTime()/(1000*60))+" "+(getBlackInc()/1000)+")";
    else
      return timeString;
  }




  /**
   * Returns a textual representation of this Game.
   */

  public String toString(){
    return "#"+getID()+" "+(isRated() ? "Rated" : "Unrated") + " " + getTCString() + " " +getVariant().getName()+" " + getWhiteName()+" vs. "+getBlackName();
  }


}
