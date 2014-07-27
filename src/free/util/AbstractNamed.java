/**
 * The utillib library.
 * More information is available at http://www.jinchess.com/.
 * Copyright (C) 2007 Alexander Maryanovsky.
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
 * An abstract base class for <code>Named</code> implementations.
 */

public abstract class AbstractNamed implements Named{
  
  
  
  /**
   * The name of the object.
   */
  
  private final String name;
  
  
  
  /**
   * Creates a new <code>Named</code> object with the specified name.
   */
  
  public AbstractNamed(String name){
    this.name = name;
  }
  
  
  
  /**
   * Returns the object's name.
   */
  
  @Override
  public String getName(){
    return name;
  }
  
  
  
  /**
   * Returns the object's name, or the string <code>"null"</code> if the name
   * is <code>null</code>.
   */
  
  @Override
  public String toString(){
    return String.valueOf(getName());
  }
  
  
  
}
