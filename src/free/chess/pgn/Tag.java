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

package free.chess.pgn;


/**
 * An abstract representation of a PGN tag. This class provides a placeholder
 * for the tag name, but leaves the details of the internal representation of
 * the tag value to subclasses.
 */

public abstract class Tag{


  /**
   * The tag name.
   */

  private final String name;



  /**
   * Creates a new PGN tag with the specified tag name. The tag name may not be
   * <code>null</code> or an empty string.
   */

  public Tag(String name){
    if (name == null)
      throw new IllegalArgumentException("The tag name may not be null");
    if (name.equals(""))
      throw new IllegalArgumentException("The tag name may not be an empty string");

    this.name = name;
  }




  /**
   * Returns the name of the tag.
   */

  public final String getName(){
    return name;
  }




  /**
   * Returs the value of the tag.
   */

  public abstract String getValue();




  /**
   * Returns <code>true</code> if the specified object is a Tag that has the
   * same type, name and value as this tag.
   */

  public boolean equals(Object o){
    if (o.getClass() != this.getClass())
      return false;

    Tag tag = (Tag)o;

    return getName().equals(tag.getName()) && getValue().equals(tag.getValue());
  }




  /**
   * Returns the hashCode of this object.
   */

  public int hashCode(){
    return getName().hashCode() & getValue().hashCode();
  }




  /**
   * Returns a textual representation of this tag.
   */

  public String toString(){
    return "["+getName()+" \""+getValue()+"\"]";
  }


}