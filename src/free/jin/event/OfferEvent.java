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

package free.jin.event;

import free.jin.Game;
import free.jin.Connection;
import free.chess.Player;


/**
 * The event fired when an offer, such as a draw, abort or adjourn offer, is
 * made or withdrawn in a game.
 */

public class OfferEvent extends GameEvent{



  /**
   * The id for a draw offer.
   */

  public static final int DRAW_OFFER = 1;



  /**
   * The id for an adjourn offer.
   */

  public static final int ADJOURN_OFFER = 2;



  /**
   * The id for an abort offer.
   */

  public static final int ABORT_OFFER = 3;



  /**
   * The id for a takeback offer.
   */

  public static final int TAKEBACK_OFFER = 4;



  /**
   * The id of the offer of this offer event.
   */

  private final int offerId;



  /**
   * True if the offer is "on", false otherwise.
   */

  private final boolean isOffered;



  /**
   * The <code>Player</code> who made or withdrew the offer.
   */

  private final Player player;



  /**
   * The number of plies offerred to takeback, in case of a takeback offer.
   */

  private final int takebackCount;




  /**
   * Creates a new <code>OfferEvent</code> with the specified offer id, whether
   * the offer is relevant ("on") and the player who made (or withdrew) the
   * offer. The offer id may not be {@link #TAKEBACK_OFFER} - for takeback
   * offers use the other constructor.
   */

  public OfferEvent(Connection conn, Game game, int offerId, boolean isOffered, Player player){
    super(conn, game);

    switch (offerId){
      case DRAW_OFFER:
      case ADJOURN_OFFER:
      case ABORT_OFFER:
        break;
      default:
        throw new IllegalArgumentException("Bad offer id: " + offerId);
    }

    if (player == null)
      throw new IllegalArgumentException("player may not be null");

    this.offerId = offerId;
    this.isOffered = isOffered;
    this.player = player;
    this.takebackCount = -1;
  }



  /**
   * Creates a new <code>OfferEvent</code> for a takeback offer.
   */

  public OfferEvent(Connection conn, Game game, boolean isOffered, Player player,
      int takebackCount){
    super(conn, game);

    if (takebackCount <= 0)
      throw new IllegalArgumentException("takebackCount must be positive");

    this.offerId = TAKEBACK_OFFER;
    this.isOffered = isOffered;
    this.player = player;
    this.takebackCount = takebackCount;
  }



  /**
   * Returns the offer id. Possible values are defined in this class.
   */

  public int getOfferId(){
    return offerId;
  }



  /**
   * Returns <code>true</code> if the offer is relevant (that is, the opponent
   * can accept it), <code>false</code> otherwise.
   */

  public boolean isOffered(){
    return isOffered;
  }



  /**
   * Returns the player who made (or withdrew) the offer.
   */

  public Player getPlayer(){
    return player;
  }



  /**
   * Returns the amount of plies offered to take back. Throws an exception if
   * the offer is not a takeback offer.
   */

  public int getTakebackCount(){
    if (offerId != TAKEBACK_OFFER)
      throw new IllegalStateException("The offer is not takeback");

    return takebackCount;
  }
  
  
  
  /**
   * Returns a textual representation of this <code>OfferEvent</code>.
   */
   
  public String toString(){
    StringBuffer buf = new StringBuffer("OfferEvent[");
    switch (getOfferId()){
      case DRAW_OFFER: buf.append("DRAW_OFFER,"); break;
      case ABORT_OFFER: buf.append("ABORT_OFFER,"); break;
      case ADJOURN_OFFER: buf.append("ADJOURN_OFFER,"); break;
      case TAKEBACK_OFFER: buf.append("TAKEBACK_OFFER("+getTakebackCount()+"), "); break;
    }
    
    buf.append(getPlayer() + ", ");
    
    if (isOffered())
      buf.append("offered]");
    else
      buf.append("not offered]");

    return buf.toString();    
  }

}
