/**
 * The utillib library.
 * More information is available at http://www.jinchess.com/.
 * Copyright (C) 2002 Alexander Maryanovsky.
 * All rights reserved.
 *
 * The utillib library is free software; you can redistribute
 * it and/or modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 2 of the
 * License, or (at your option) any later version.
 *
 * The utillib library is distributed in the hope that it will
 * be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser
 * General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with utillib library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package free.util;


/**
 * A wrapper for any two other given object which return a hashcode made from
 * combining the hashcodes of the targets and will return <code>true</code> from
 * the <code>equals(Object)</code> method only if the given object is a PairKey
 * containing two other objects equivalent to those wrapped by this PairKey
 * (the order is important).
 */

public final class PairKey{

  
  /**
   * The first object.
   */

  private final Object first;




  /**
   * The second object.
   */

  private final Object second;




  /**
   * Creates a new PairKey with the two given objects.
   */

  public PairKey(Object first, Object second){
    this.first = first;
    this.second = second;
  }





  /**
   * Returns the first object.
   */

  public Object getFirst(){
    return first;
  }




  /**
   * Returns the second object.
   */

  public Object getSecond(){
    return second;
  }




  /**
   * Returns a hashcode combined from the hashcodes of the two target objects.
   */

  public int hashCode(){
    return (first.hashCode()+1)^second.hashCode();
  }




  /**
   * Returns true if the given Object is a PairKey, the first target object of
   * this PairKey equals to the first target object of the given PairKey and the
   * second target object of this PairKey equals to the second target object of
   * the given PairKey.
   */

  public boolean equals(Object o){
    if (!(o instanceof PairKey))
      return false;

    PairKey pk = (PairKey)o;

    return first.equals(pk.getFirst()) && second.equals(pk.getSecond());
  }

}