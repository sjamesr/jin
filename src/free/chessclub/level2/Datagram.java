/**
 * The chessclub.com connection library.
 * More information is available at http://www.jinchess.com/.
 * Copyright (C) 2007 Alexander Maryanovsky.
 * All rights reserved.
 *
 * The chessclub.com connection library is free software; you can redistribute
 * it and/or modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 2 of the
 * License, or (at your option) any later version.
 *
 * The chessclub.com connection library is distributed in the hope that it will
 * be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser
 * General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with the chessclub.com connection library; if not, write to the Free
 * Software Foundation, Inc.,
 * 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package free.chessclub.level2;

import java.util.LinkedList;
import java.util.List;

import free.util.FormatException;


/**
 * Represents a datagram sent by the chessclub.com server in the format 
 * described at
 * <code>ftp://ftp.chessclub.com/pub/icc/formats/formats.txt</code>.
 * This class defines methods useful for parsing a datagram and retrieving
 * arguments from it.
 */

public class Datagram{
  
  
  
  /**
   * The start of a special string delimiter.
   */
  
  private final static char SPECIAL_STRING_DELIM_START = '\u0019';



	/**
	 * Definitions of all the DG type IDs.
	 */

  public static final int DG_WHO_AM_I						     = 0;
  public static final int DG_PLAYER_ARRIVED	         = 1;
  public static final int DG_PLAYER_LEFT	           = 2;
  public static final int DG_BULLET                  = 3;
  public static final int DG_BLITZ	                 = 4;
  public static final int DG_STANDARD                = 5;
  public static final int DG_WILD                    = 6;
  public static final int DG_BUGHOUSE                = 7;
  public static final int DG_TIMESTAMP               = 8;
  public static final int DG_TITLES							     = 9;
  public static final int DG_OPEN	                   = 10;
  public static final int DG_STATE                   = 11;
  public static final int DG_GAME_STARTED            = 12;
  public static final int DG_GAME_RESULT             = 13;
  public static final int DG_EXAMINED_GAME_IS_GONE   = 14;
  public static final int DG_MY_GAME_STARTED         = 15;
  public static final int DG_MY_GAME_RESULT          = 16;
  public static final int DG_MY_GAME_ENDED           = 17;
  public static final int DG_STARTED_OBSERVING       = 18;
  public static final int DG_STOP_OBSERVING          = 19;
  public static final int DG_PLAYERS_IN_MY_GAME      = 20;
  public static final int DG_OFFERS_IN_MY_GAME       = 21;
  public static final int DG_TAKEBACK                = 22;
  public static final int DG_BACKWARD                = 23;
  public static final int DG_SEND_MOVES              = 24;
  public static final int DG_MOVE_LIST               = 25;
  public static final int DG_KIBITZ                  = 26;
  public static final int DG_PEOPLE_IN_MY_CHANNEL    = 27;
  public static final int DG_CHANNEL_TELL            = 28;
  public static final int DG_MATCH                   = 29;
  public static final int DG_MATCH_REMOVED           = 30;
  public static final int DG_PERSONAL_TELL           = 31;
  public static final int DG_SHOUT                   = 32;
  public static final int DG_MOVE_ALGEBRAIC          = 33;
  public static final int DG_MOVE_SMITH              = 34;
  public static final int DG_MOVE_TIME               = 35;
  public static final int DG_MOVE_CLOCK              = 36;
  public static final int DG_BUGHOUSE_HOLDINGS       = 37;
  public static final int DG_SET_CLOCK               = 38;
  public static final int DG_FLIP                    = 39;
  public static final int DG_ISOLATED_BOARD          = 40;
  public static final int DG_REFRESH                 = 41;
  public static final int DG_ILLEGAL_MOVE            = 42;
  public static final int DG_MY_RELATION_TO_GAME     = 43;
  public static final int DG_PARTNERSHIP             = 44;
  public static final int DG_SEES_SHOUTS             = 45;
  public static final int DG_CHANNELS_SHARED         = 46;
  public static final int DG_MY_VARIABLE             = 47;
  public static final int DG_MY_STRING_VARIABLE      = 48;
  public static final int DG_JBOARD                  = 49;
  public static final int DG_SEEK                    = 50;
  public static final int DG_SEEK_REMOVED            = 51;
  public static final int DG_MY_RATING               = 52;
  public static final int DG_SOUND                   = 53;
  public static final int DG_PLAYER_ARRIVED_SIMPLE   = 55;
  public static final int DG_MSEC                    = 56;
  public static final int DG_BUGHOUSE_PASS           = 57;
  public static final int DG_IP                      = 58;
  public static final int DG_CIRCLE                  = 59;
  public static final int DG_ARROW                   = 60;
  public static final int DG_MORETIME                = 61;
  public static final int DG_PERSONAL_TELL_ECHO      = 62;
  public static final int DG_SUGGESTION              = 63;
  public static final int DG_NOTIFY_ARRIVED          = 64;
  public static final int DG_NOTIFY_LEFT             = 65;
  public static final int DG_NOTIFY_OPEN             = 66;
  public static final int DG_NOTIFY_STATE            = 67;
  public static final int DG_MY_NOTIFY_LIST          = 68;
  public static final int DG_LOGIN_FAILED            = 69;
  public static final int DG_FEN                     = 70;
  public static final int DG_TOURNEY_MATCH           = 71;
  public static final int DG_GAMELIST_BEGIN          = 72;
  public static final int DG_GAMELIST_ITEM           = 73;
  public static final int DG_IDLE                    = 74;
  public static final int DG_ACK_PING                = 75;
  public static final int DG_RATING_TYPE_KEY         = 76;
  public static final int DG_GAME_MESSAGE            = 77;
  public static final int DG_UNACCENTED              = 78;
  public static final int DG_STRINGLIST_BEGIN        = 79;
  public static final int DG_STRINGLIST_ITEM         = 80;
  public static final int DG_DUMMY_RESPONSE          = 81;
  public static final int DG_CHANNEL_QTELL           = 82;
  public static final int DG_PERSONAL_QTELL          = 83;
  public static final int DG_SET_BOARD               = 84;
  public static final int DG_MATCH_ASSESSMENT        = 85;
  public static final int DG_LOG_PGN                 = 86;
  public static final int DG_NEW_MY_RATING           = 87;
  public static final int DG_LOSERS                  = 88;
  public static final int DG_UNCIRCLE                = 89;
  public static final int DG_UNARROW                 = 90;
  public static final int DG_WSUGGEST                = 91;
  public static final int DG_TEMPORARY_PASSWORD      = 93;
  public static final int DG_MESSAGELIST_BEGIN       = 94;
  public static final int DG_MESSAGELIST_ITEM        = 95;
  public static final int DG_LIST                    = 96;
  public static final int DG_SJI_AD                  = 97;
  public static final int DG_RETRACT                 = 99;
  public static final int DG_MY_GAME_CHANGE          = 100;
  public static final int DG_POSITION_BEGIN          = 101;
  public static final int DG_TOURNEY                 = 103;
  public static final int DG_REMOVE_TOURNEY          = 104;
  public static final int DG_DIALOG_START            = 105;
  public static final int DG_DIALOG_DATA             = 106;
  public static final int DG_DIALOG_DEFAULT          = 107;
  public static final int DG_DIALOG_END              = 108;
  public static final int DG_DIALOG_RELEASE          = 109;
  public static final int DG_POSITION_BEGIN2         = 110;
  public static final int DG_PAST_MOVE               = 111;
  public static final int DG_PGN_TAG                 = 112;
  public static final int DG_IS_VARIATION            = 113;
  public static final int DG_PASSWORD                = 114;
  public static final int DG_WILD_KEY                = 116;
  public static final int DG_SET2                    = 124;
  public static final int DG_FIVEMINUTE              = 125;
  public static final int DG_ONEMINUTE               = 126;
  public static final int DG_KNOWS_FISCHER_RANDOM    = 132;
  
  
  
  /**
   * The maximum datagram ID.
   */
   
  public static final int MAX_DG_ID = 132;
                         

  
  /**
   * The ID of the datagram.
   */
   
  private final int id;
  
  
  
  /**
   * An array holding the datagram fields.
   */

  private final String [] fields; 

  
  
  /**
   * Creates a new <code>Datagram</code> with the specified datagram id and  
   * fields.
   */

  public Datagram(int id, String [] fields){
    if (fields == null)
      throw new IllegalArgumentException("Datagram fields may not be null");
    
    this.id = id;
    this.fields = fields;
  }

  
  
  /**
   * Returns the ID of the datagram.
   */
  
  public int getId(){
    return id;
  }
  

  /**
   * Returns the number of fields in this Datagram.
   */

  public int getFieldCount(){
    return fields.length;
  }


  
  /**
   * Returns the specified field, unparsed.
   */ 

  public String getField(int fieldIndex){
    return fields[fieldIndex];
  }



  /**
   * Returns the specified field parsed as a string.
   */

  public String getString(int fieldIndex){
    return getField(fieldIndex);    
  }



  /**
   * Returns the specified field parsed as an integer.
   */

  public int getInteger(int fieldIndex){
    return Integer.parseInt(getField(fieldIndex));
  }



  /**
   * Returns the specified field parsed as a long.
   */

  public long getLong(int fieldIndex){
    return Long.parseLong(getField(fieldIndex));
  }



  /**
   * Returns the specified field, parsed as a boolean.
   * If the value of the field is "1", <code>true</code> is returned, otherwise
   * <code>false</code> is returned.
   */

  public boolean getBoolean(int fieldIndex){
    return getField(fieldIndex).equals("1");
  }
  
  
  

  /**
   * Parses the specified string and returns a <code>Datagram</code> object
   * corresponding to the datagram represented by that string. The string should
   * be the contents of the datagram, without the surrounding delimiters.
   *
   * @throws FormatException if the specified string cannot be parsed as a
   * datagram because it is not in the proper format.
   */

  public static Datagram parseDatagram(String dgString) throws FormatException{
    try{
      int index = dgString.indexOf(' ');
      if (index == -1) // No arguments in the datagram
        index = dgString.length();
      int id = Integer.parseInt(dgString.substring(0, index));
      
      dgString += " "; // So that each field is suffixed with a space
      
      List fields = new LinkedList();
      
      index++;
      int dgLength = dgString.length();
      while (index < dgLength){
        int startIndex, endIndex;
        char firstChar = dgString.charAt(index); 
        if (firstChar == '{'){ // The delimiters are { and }
          startIndex = index + 1;
          endIndex = dgString.indexOf('}', startIndex);
          index = endIndex + 2; // "} "
        }
        else if (firstChar == SPECIAL_STRING_DELIM_START){ // The delimiters are ^Y{ and ^Y}
          startIndex = index + 2;
          endIndex = dgString.indexOf(SPECIAL_STRING_DELIM_START, startIndex);
          index = endIndex + 3; // "^Y} "
        }
        else{ 
          while (firstChar == ' '){ // Skip any extra spaces just in case
            index++;
            firstChar = dgString.charAt(index);
          }
          
          startIndex = index;
          endIndex = dgString.indexOf(' ', startIndex);
          index = endIndex + 1;
        }
        
        fields.add(dgString.substring(startIndex, endIndex));
      }
  
      return new Datagram(id, (String[])fields.toArray(new String[fields.size()]));
    } catch (NumberFormatException e){
        throw new FormatException(e);
      }
      catch (StringIndexOutOfBoundsException e){
        throw new FormatException(e);
      }
  }
  
  
  
  /**
   * Returns a textual representation of this datagram.
   */

  public String toString(){
    StringBuffer buf = new StringBuffer("[Datagram ID=" + getId());
    int fieldCount = getFieldCount();
    if (fieldCount != 0){
      buf.append(" Fields: ");
      for (int i = 0; i < fieldCount; i++){
        buf.append("{" + getField(i) + "}");
        if (i != fieldCount - 1)
          buf.append(",");
      }
    }
    buf.append("]");
    
    return buf.toString();
  }

  

}
