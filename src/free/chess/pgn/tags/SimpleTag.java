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
 * A simple representation of a PGN tag via two String values - the name and
 * value of the tag.
 */

public class SimpleTag extends Tag{



  /**
   * The tag value.
   */

  private final String value;




  /**
   * Creates a new PGN tag with the specified name and value. Neither the name
   * nor the value may be <code>null</code>. The name of the tag may not be
   * an empty string.
   */

  public SimpleTag(String name, String value){
    super(name);

    if (value == null)
      throw new IllegalArgumentException("The tag value may not be null");

    this.value = value;
  }




  /**
   * Returs the value of the tag.
   */

  public final String getValue(){
    return value;
  }



}