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
 * A representation of the Event PGN tag. The Event tag specifies the name of
 * the tournament or match event.
 */

public final class EventTag extends SimpleTag{


  /**
   * The name of the Event tag - the String "Event".
   */

  public static String TAG_NAME = "Event";



  /**
   * An EventTag instance representing an Event tag with an unknown value.
   */

  public static final EventTag EVENT_UNKNOWN = new EventTag("?");



  /**
   * Creates a new EventTag with the specified event name. The event name may
   * not be an empty string. The event name may be <code>null</code> to specify
   * that the event name is unknown, but it's preferable to use the
   * <code>EVENT_UNKNOWN</code> EventTag constant for that.
   */

  public EventTag(String eventName){
    super(TAG_NAME, eventName == null ? "?" : eventName);

    if ("".equals(eventName))
      throw new IllegalArgumentException("The event name may not be an empty string");
  }




  /**
   * Returns the name of the event, or <code>null</code> if the event name is
   * unknown.
   */

  public String getEventName(){
    String tagValue = getValue();
    return tagValue.equals("?") ? null : tagValue;
  } 

}