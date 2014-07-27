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

import free.chess.Player;
import free.chess.Position;
import free.chess.TimeControl;
import free.chess.WildVariant;
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
   * Game end reason is unknown.
   */
  
  public static final int UNKNOWN_REASON = -2;
  
  
  
  /**
   * Some other game end reason than the ones defined here.
   */
  
  public static final int OTHER_REASON  = -1;
  
  
  
  /**
   * Actor resigns.
   */
  
  public static final int RESIGNS = 0;
  
  
  
  /**
   * Actor got checkmated.
   */
  
  public static final int CHECKMATED = 1;
  
  
  
  /**
   * Actor forfeits on time.
   */
  
  public static final int TIME_FORFEITS = 2;
  
  
  
  /**
   * Game adjudicated against actor.
   */
  
  public static final int ADJUDICATED = 3;
  
  
  
  /**
   * Actor disconnected and forfeits.
   */
  
  public static final int DISCONNECTED = 4;
  
  
  
  /**
   * Actor's partner resigns.
   */
  
  public static final int PARTNER_RESIGNS = 5;
  
  
  
  /**
   * Actor's partner got checkmated.
   */
  
  public static final int PARTNER_CHECKMATED = 6;
  
  
  
  /**
   * Actor's partner forfeits on time.
   */
  
  public static final int PARTNER_TIME_FORFEITS = 7;
  
  
  
  /**
   * Actor's partner disconnected and forfeits on time.
   */
  
  public static final int PARTNER_DISCONNECTED = 8;
  
  
  
  /**
   * Drawn by mutual agreement.
   */
  
  public static final int DRAW_AGREEMENT = 101;
  
  
  
  /**
   * Actor stalemated.
   */
  
  public static final int STALEMATE = 102;
  
  
  
  /**
   * Actor drew the game by repetition.
   */
  
  public static final int REPETITION = 103;
  
  
  
  /**
   * Actor drew the game by the 50 move rule.
   */
  
  public static final int FIFTY_MOVE_RULE = 104;
  
  
  
  /**
   * Actor out of time and his opponent has no material to mate.
   */
  
  public static final int OUT_OF_TIME_AND_OPP_HAS_NO_MATERIAL_TO_MATE = 105;
  
  
  
  /**
   * Drawn because neither player has material to mate.
   */
  
  public static final int BOTH_NO_MATERIAL_TO_MATE = 106;
  
  
  
  /**
   * Drawn because both players are out of time.
   */
  
  public static final int BOTH_OUT_OF_TIME = 107;
  
  
  
  /**
   * Drawn because partners agreed to draw.
   */
  
  public static final int PARTNER_DRAW_AGREEMENT = 108;
  
  
  
  /**
   * Drawn because both partners are out of time.
   */
  
  public static final int PARTNER_BOTH_OUT_OF_TIME = 109;
  
  
  
  /**
   * Adjourned by mutual agreement.
   */
  
  public static final int ADJOURNED_AGREEMENT = 201;
  
  
  
  /**
   * Adjourned when actor disconnected.
   */
  
  public static final int ADJOURNED_DISCONNECTED = 202;
  
  
  
  /**
   * Courtesy-adjourned by actor.
   */
  
  public static final int ADJOURNED_COURTESY = 203;
  
  
  
  /**
   * Adjourned by admin.
   */
  
  public static final int ADJOURNED_ADMIN = 204;
  
  
  
  /**
   * Aborted by mutual agreement.
   */
  
  public static final int ABORTED_AGREEMENT = 301;
  
  
  
  /**
   * Aborted when actor disconnected.
   */
  
  public static final int ABORTED_DISCONNECTED = 302;
  
  
  
  /**
   * Courtesy-aborted by actor.
   */
  
  public static final int ABORTED_COURTESY = 303;
  
  
  
  /**
   * Aborted by admin.
   */
  
  public static final int ABORTED_ADMIN = 304;
  
  
  
  /**
   * Aborted because game is too short.
   */
  
  public static final int ABORTED_TOO_SHORT = 305;
  
  
  
  /**
   * Aborted when black's partner disconnected.
   */
  
  public static final int ABORTED_PARTNER_DISCONNECTED = 306;
  
  
  
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
   * @param timeControl The time control of the game.
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
      String blackName, TimeControl timeControl, int whiteRating,
      int blackRating, Object gameId, String ratingCategoryString, boolean isRated,
      boolean isPlayed, String whiteTitles, String blackTitles, boolean initiallyFlipped,
      Player userPlayer){

    setGameType(gameType);
    setInitialPosition(initialPosition);
    setPliesSinceStart(pliesSinceStart);
    setWhiteName(whiteName);
    setBlackName(blackName);
    setTimeControl(timeControl);
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
   * Sets time control for this game.
   */

  public void setTimeControl(TimeControl timeControl){
    props.setProperty("timeControl", timeControl);
  }
  
  
  
  /**
   * Returns the time control for this game.
   */
  
  public TimeControl getTimeControl(){
    return (TimeControl)props.getProperty("timeControl");
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

  public int getResultCode(){
    Integer result = (Integer)props.getProperty("resultCode", null);
    if (result == null)
      return GAME_IN_PROGRESS;
    return result.intValue();
  }
  
  
  
  /**
   * Returns the reason code of the game end. Possible values are one of the
   * reason codes defined in this class. May only be invoked if the result code
   * is not <code>GAME_IN_PROGRESS</code>. See also {@link #getGameEndActor()}.
   */
  
  public int getGameEndReasonCode(){
    if (getResultCode() == GAME_IN_PROGRESS)
      throw new IllegalStateException("Game not ended yet");
    
    return props.getIntegerProperty("gameEndReasonCode");
  }
  
  
  
  /**
   * Returns the player who lost, or acted, in relation to
   * {@link #getGameEndReasonCode()}; <code>null</code> if inapplicable or
   * unknown.
   * May only be invoked if the result code is not
   * <code>GAME_IN_PROGRESS</code>.
   */
  
  public Player getGameEndActor(){
    if (getResultCode() == GAME_IN_PROGRESS)
      throw new IllegalStateException("Game not ended yet");
    
    return (Player)props.getProperty("gameEndActor", null);
  }
  
  
  
  /**
   * Returns a string describing the game end reason, which is suitable to be
   * displayed to the user.
   */
  
  public String getGameEndReasonDescription(){
    int resultCode = getResultCode();
    int reasonCode = getGameEndReasonCode();
    Player actor = getGameEndActor();
    
    String resultKey;
    switch (resultCode){
      case WHITE_WINS: resultKey = "whiteWins"; break;
      case BLACK_WINS: resultKey = "blackWins"; break;
      case DRAW: resultKey = "draw"; break;
      case UNKNOWN_RESULT: resultKey = "unknown"; break;
      default:
        throw new IllegalStateException("Wrong result code: " + resultCode);
    }
    
    String reasonKey = null;
    switch (reasonCode){
      case RESIGNS: reasonKey = "resigns"; break;
      case CHECKMATED: reasonKey = "checkmated"; break;
      case TIME_FORFEITS: reasonKey = "timeForfeits"; break;
      case ADJUDICATED: reasonKey = "adjudicated"; break;
      case DISCONNECTED: reasonKey = "disconnected"; break;
      case PARTNER_RESIGNS: reasonKey = "partnerResigns"; break;
      case PARTNER_CHECKMATED: reasonKey = "partnerCheckmated"; break;
      case PARTNER_TIME_FORFEITS: reasonKey = "partnerTimeForfeits"; break;
      case PARTNER_DISCONNECTED: reasonKey = "partnerDisconnected"; break;
      case DRAW_AGREEMENT: reasonKey = "drawAgreement"; break;
      case STALEMATE: reasonKey = "stalemate"; break;
      case REPETITION: reasonKey = "repetition"; break;
      case FIFTY_MOVE_RULE: reasonKey = "50move"; break;
      case OUT_OF_TIME_AND_OPP_HAS_NO_MATERIAL_TO_MATE: reasonKey = "outOfTimeAndOppHasNoMaterial"; break;
      case BOTH_NO_MATERIAL_TO_MATE: reasonKey = "bothNoMaterial"; break;
      case BOTH_OUT_OF_TIME: reasonKey = "bothOutOfTime"; break;
      case PARTNER_DRAW_AGREEMENT: reasonKey = "partnerDrawAgreement"; break;
      case PARTNER_BOTH_OUT_OF_TIME: reasonKey = "partnerBothOutOfTime"; break;
      case ADJOURNED_AGREEMENT: reasonKey = "adjournedAgreement"; break;
      case ADJOURNED_DISCONNECTED: reasonKey = "adjournedDisconnected"; break;
      case ADJOURNED_COURTESY: reasonKey = "adjournedCourtesy"; break;
      case ADJOURNED_ADMIN: reasonKey = "adjournedAdmin"; break;
      case ABORTED_AGREEMENT: reasonKey = "abortedAgreement"; break;
      case ABORTED_DISCONNECTED: reasonKey = "abortedDisconnected"; break;
      case ABORTED_COURTESY: reasonKey = "abortedCourtesy"; break;
      case ABORTED_ADMIN: reasonKey = "abortedAdmin"; break;
      case ABORTED_TOO_SHORT: reasonKey = "abortedTooShort"; break;
      case ABORTED_PARTNER_DISCONNECTED: reasonKey = "abortPartnerDisconnected"; break;
      default:
        reasonKey = null;
    }
    
    String actorKey;
    if (actor == null)
      actorKey = null;
    else if (actor.isWhite())
      actorKey = "white";
    else
      actorKey = "black";
    
    String [] keyParts = new String[]{"gameEndReason", resultKey, reasonKey, actorKey};
    String [] keys = new String[keyParts.length];
    keys[0] = keyParts[0];
    int keyCount = 1;
    for (int i = 1; i < keyParts.length; i++){
      if (keyParts[i] == null)
        break;
      
      keys[keyCount] = keys[keyCount - 1] + "." + keyParts[i];
      keyCount++;
    }
    
    Object [] args = new Object[]{getWhiteName(), getBlackName()};
    
    I18n i18n = I18n.get(Game.class);
    for (int i = keys.length - 1; i >= 1; i--){
      String description = i18n.getString(keys[i], null);
      if (description != null)
        return i18n.getFormattedString(keys[i], args);
    }
    
    return i18n.getFormattedString(keys[0], args);
  }  
  
  
  
  /**
   * Sets the result of the game to the specified value. This method may only be
   * called once per Game object (when the game ends).
   * 
   * @param result The result code: <code>WHITE_WINS</code>,
   * <code>BLACK_WINS</code>, <code>DRAW</code> or <code>UNKNOWN_RESULT</code>.
   * @param reason The reason code (many constants, defined in this class).
   * @param actor The player who lost (in case of a loss) or acted, or
   * <code>null</code> if inapplicable or unknown.
   */

  public void setResult(int result, int reason, Player actor){
    switch(result){
      case WHITE_WINS:
      case BLACK_WINS:
      case DRAW:
      case UNKNOWN_RESULT:
        break;
      default:
        throw new IllegalArgumentException("Bad value for game result specified: "+result);
    }

    if (getResultCode() != GAME_IN_PROGRESS)
      throw new IllegalStateException("Unable to set the result more than once");

    props.setIntegerProperty("gameEndReasonCode", reason);
    props.setProperty("gameEndActor", actor);
    
    // This needs to be last because it's the one to depend on to find out when
    // the game result is known.
    props.setIntegerProperty("resultCode", result); 
    
  }
  
  
  
  /**
   * Returns a short description of this game, suitable for displaying to the
   * end-user.
   */
  
  public String getShortDescription(){
    I18n i18n = I18n.get(Game.class);
    
    String white = getWhiteName() + getWhiteTitles();
    String black = getBlackName() + getBlackTitles();
    
    int result = getResultCode();
    String resultKey;
    switch (result){
      case WHITE_WINS: resultKey = "whiteWins"; break;
      case BLACK_WINS: resultKey = "blackWins"; break;
      case DRAW: resultKey = "draw"; break;
      case UNKNOWN_RESULT: resultKey = "unknown"; break;
      case GAME_IN_PROGRESS: resultKey = "inProgress"; break;
      default:
        throw new IllegalStateException("Unknown result code: " + result);
    }
    String resultString = i18n.getString("result." + resultKey);
    
    if (getGameType() == Game.MY_GAME){
      if (isPlayed())
        return i18n.getFormattedString("playingShortDescription", new Object[]{getUserPlayer().isWhite() ? black : white});
      else if (getWhiteName().equals(getBlackName()))
        return i18n.getFormattedString("examinationShortDescription", new Object[]{white, black, resultString});
      else
        return i18n.getFormattedString("examiningShortDescription", new Object[]{white, black, resultString});
    }
    else
      return i18n.getFormattedString("observingShortDescription", new Object[]{white, black, resultString});
  }




  /**
   * Returns a textual representation of this Game.
   */

  public String toString(){
    return "#"+getID()+" "+(isRated() ? "Rated" : "Unrated") + " " + getTimeControl().getLocalizedShortDescription() + " " +getVariant().getName()+" " + getWhiteName()+" vs. "+getBlackName();
  }


}
