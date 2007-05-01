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

package free.jin.event;

import free.jin.MatchOffer;
import free.jin.MatchOfferConnection;



/**
 * The event fired by a <code>MatchOfferConnection</code> when a match offer is
 * made or withdrawn.
 */

public class MatchOfferEvent extends JinEvent{
  
  
  
  /**
   * The id for a match offer made.
   */
  
  public static final int MATCH_OFFER_MADE = 1;
  
  
  
  /**
   * The id for a match offer withdrawn.
   */
  
  public static final int MATCH_OFFER_WITHDRAWN = 2;
  
  
  
  /**
   * The id of this event.
   */
  
  private final int id;
  
  
  
  /**
   * The actual match offer made/withdrawn.
   */
  
  private final MatchOffer matchOffer;
  
  
  
  /**
   * Creates a new <code>MatchOfferEvent</code> with the specified arguments.
   */
  
  public MatchOfferEvent(MatchOfferConnection source, int id, MatchOffer matchOffer){
    super(source);
    
    switch(id){
      case MATCH_OFFER_MADE:
      case MATCH_OFFER_WITHDRAWN:
        break;
      default:
        throw new IllegalArgumentException("Unknown MatchOfferEvent id: " + id);
    }
    if (matchOffer == null)
      throw new IllegalArgumentException("matchOffer may not be null");
    
    this.id = id;
    this.matchOffer = matchOffer;
  }
  
  
  
  /**
   * Returns the id of this event
   */
  
  public int getID(){
    return id;
  }
  
  
  
  /**
   * Returns the match offer made/withdrawn.
   */
  
  public MatchOffer getMatchOffer(){
    return matchOffer;
  }
  
  
  
  /**
   * Returns the source <code>MatchOfferConnection</code>.
   */
  
  public MatchOfferConnection getMatchOfferConnection(){
    return (MatchOfferConnection)getConnection();
  }
  
  
  
}
