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
import free.util.TextUtilities;


/**
 * A representation of the Date PGN tag. The Date tag value gives the starting
 * date for the game. The date is given with respect to the local time of the
 * site given in the Event tag. The Date tag value field always uses a standard
 * ten character format: "YYYY.MM.DD". If the any of the digit fields are not
 * known, then question marks are used in place of the digits.
 */

public final class DateTag extends Tag{



  /**
   * The name of the Date tag - the String "Date".
   */

  public static final String TAG_NAME = "Date";



  /**
   * The constant to be used for the year value when the year is unknown.
   */

  public static final int YEAR_UNKNOWN = -1; // Let's hope nobody played any important chess games in the year 1 BC.



  /**
   * The constant to be used for the month value when the month is unknown.
   */

  public static final int MONTH_UNKNOWN = -1;



  /**
   * The constant to be used for the day of the month value when the day of the
   * month is unknown.
   */

  public static final int DAY_OF_MONTH_UNKNOWN = -1;



  /**
   * A DateTag instance representing a Date tag with an unknown value.
   */

  public static final DateTag DATE_UNKNOWN = new DateTag(YEAR_UNKNOWN, MONTH_UNKNOWN, DAY_OF_MONTH_UNKNOWN);



  /**
   * The year.
   */

  private final int year;



  /**
   * The month.
   */

  private final int month;



  /**
   * The day of the month.
   */

  private final int day;




  /**
   * Creates a new DateTag with the specified year, month and day. The values
   * must be within appropriate ranges, or equal to the appropriate
   * <cod>UNKNOWN</code> constants defined in this class.
   */

  public DateTag(int year, int month, int day){
    super(TAG_NAME);

    if ((month != MONTH_UNKNOWN) && ((month < 1) || (month > 12)))
      throw new IllegalArgumentException("Bad value for month: "+month);
    if ((day != DAY_OF_MONTH_UNKNOWN) && ((day < 1) || (day > 31))) // Humans suck, but we don't care.
      throw new IllegalArgumentException("Bad value for day: "+day);

    this.year = year;
    this.month = month;
    this.day = day;
  }




  /**
   * Returns the year of the game, or <code>YEAR_UNKNOWN</code> if the year
   * of the game is unknown.
   */

  public int getYear(){
    return year;
  }




  /**
   * Returns the month of the game, or <code>MONTH_UNKNOWN</code> if the month
   * is unknown.
   */

  public int getMonth(){
    return month;
  }




  /**
   * Returns the day of month of the game, or <code>DAY_OF_MONTH_UNKNOWN</code>
   * if the day of month is unknown.
   */

  public int getDay(){
    return day;
  }




  /**
   * Returns the value of this tag.
   */

  public String getValue(){
    String yearString = TextUtilities.padStart(String.valueOf(year), '0', 4);
    String monthString = TextUtilities.padStart(String.valueOf(month), '0', 2);
    String dayString = TextUtilities.padStart(String.valueOf(day), '0', 2);

    return yearString + "." + monthString + "." + dayString;
  }



}