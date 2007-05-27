/**
 * Jin - a chess client for internet chess servers.
 * More information is available at http://www.jinchess.com/.
 * Copyright (C) 2007 Alexander Maryanovsky.
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

import free.chess.Player;
import free.chess.TimeControl;
import free.chess.WildVariant;
import free.util.Struct;



/**
 * Encapsulates a match offer.
 */

public class MatchOffer extends Struct{
  
  
  
  /**
   * Creates a new <code>MatchOffer</code> with the specified arguments.
   * 
   * @param challenger The player making the offer.
   * @param challengerTitles The titles of the player making the offer.
   * @param challengerRating The rating of the challenger.
   * @param isChallengerProvisional Whether the challenger's rating is
   * provisional.
   * @param receiver The player being challenged.
   * @param receiverTitles The titles of the player being challenged.
   * @param receiverRating The rating of the player being challenger.
   * @param isReceiverProvisional Whether the rating of the player receiving the
   * challenger is provisional.
   * @param challengerTimeControl The time control of the challenger in the
   * offered game. 
   * @param receiverTimeControl The time control of the receiver in the offered
   * game. 
   * @param isRated The ratedness of the offered game.
   * @param variant The wild variant of the offered game.
   * @param ratingCategory The name of the rating category to which the offered
   * game belongs.
   * @param challengerColor The color with which the challenger requested to
   * play, <code>null</code> if none.
   */
  
  public MatchOffer(
      ServerUser challenger, String challengerTites, int challengerRating, boolean isChallengerProvisional, 
      ServerUser receiver, String receiverTitles, int receiverRating, boolean isReceiverProvisional,
      TimeControl challengerTimeControl, TimeControl receiverTimeControl,
      boolean isRated, WildVariant variant, String ratingCategory, Player challengerColor){
    
    if (challenger == null)
      throw new IllegalArgumentException("challenger may not be null");
    if (receiver == null)
      throw new IllegalArgumentException("receiver may not be null");
    if (variant == null)
      throw new IllegalArgumentException("variant may not be null");
    if (ratingCategory == null)
      throw new IllegalArgumentException("ratingCategory may not be null");
    
    setProperty("Challenger", challenger);
    setStringProperty("ChallengerTitles", challengerTites);
    setIntegerProperty("ChallengerRating", challengerRating);
    setBooleanProperty("IsChallengerProvisional", isChallengerProvisional);
    setProperty("Receiver", receiver);
    setStringProperty("ReceiverTitles", receiverTitles);
    setIntegerProperty("ReceiverRating", receiverRating);
    setBooleanProperty("IsReceiverProvisional", isReceiverProvisional);
    setProperty("ChallengerTimeControl", challengerTimeControl);
    setProperty("ReceiverTimeControl", receiverTimeControl);
    setBooleanProperty("IsRated", isRated);
    setProperty("Variant", variant);
    setStringProperty("RatingCategory", ratingCategory);
    if (challengerColor != null)
      setProperty("ChallengerColor", challengerColor);
  }
  
  
  
  /**
   * Returns the challenger.
   */
  
  public ServerUser getChallenger(){
    return (ServerUser)getProperty("Challenger");
  }
  
  
  
  /**
   * Returns the challenger's titles.
   */
  
  public String getChallengerTitles(){
    return getStringProperty("ChallengerTitles");
  }
  
  
  
  /**
   * Returns the challenger's rating.
   */
  
  public int getChallengerRating(){
    return getIntegerProperty("ChallengerRating");
  }
  
  
  
  /**
   * Returns whether the challenger's rating is provisional.
   */
  
  public boolean isChallengerProvisional(){
    return getBooleanProperty("IsChallengerProvisional");
  }
  
  
  
  /**
   * Returns the receiver of the challenge.
   */
  
  public ServerUser getReceiver(){
    return (ServerUser)getProperty("Receiver");
  }
  
  
  
  /**
   * Returns the titles of the receiver of the challenge.
   */
  
  public String getReceiverTitles(){
    return getStringProperty("ReceiverTitles");
  }
  
  
  
  /**
   * Returns the receiver's rating.
   */
  
  public int getReceiverRating(){
    return getIntegerProperty("ReceiverRating");
  }
  
  
  
  /**
   * Returns whether the receiver's rating is provisional.
   */
  
  public boolean isReceiverProvisional(){
    return getBooleanProperty("IsReceiverProvisional");
  }
  
  
  
  /**
   * Returns the time control of the challenger in the offered game.
   */
  
  public TimeControl getChallengerTimeControl(){
    return (TimeControl)getProperty("ChallengerTimeControl");
  }
  
  
  
  /**
   * Returns the time control of the receiver in the offered game.
   */
  
  public TimeControl getReceiverTimeControl(){
    return (TimeControl)getProperty("ReceiverTimeControl");
  }
  
  
  
  /**
   * Returns whether the offered game is rated.
   */
  
  public boolean isRated(){
    return getBooleanProperty("IsRated");
  }
  
  
  
  /**
   * Returns the variant of the offered game.
   */
  
  public WildVariant getVariant(){
    return (WildVariant)getProperty("Variant");
  }
  
  
  
  /**
   * Returns the name of the rating category to which the game belongs.
   */
  
  public String getRatingCategory(){
    return getStringProperty("RatingCategory");
  }
  
  
  
  /**
   * Returns the color with which the challenger asked to play, or
   * <code>null</code> if none.
   */
  
  public Player getChallengerColor(){
    return (Player)getProperty("ChallengerColor");
  }
  
  
  
}
