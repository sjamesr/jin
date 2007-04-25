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



/**
 * A simple implementation of <code>ServerUser</code>, which keeps the username
 * as a property and may be set-up as case-sensitive or case-insensitive.
 */

public class PlainServerUser implements ServerUser{
  
  
  
  /**
   * The user's name/handle/nickname.
   */
  
  private final String name;
  
  
  
  /**
   * Whether the username is case sensitive.
   */
  
  private final boolean isCaseSensitive;
  
  
  
  /**
   * Whether the username is an alias.
   */
  
  private final boolean isAlias;
  
  
  
  /**
   * Creates a new <code>PlainServerUser</code> with the specified
   * case-sensitive or case-insensitive user name/handle/nickname.
   */
  
  protected PlainServerUser(String name, boolean isCaseSensitive, boolean isAlias){
    if (name == null)
      throw new IllegalArgumentException("name may not be null");
    
    this.name = name;
    this.isCaseSensitive = isCaseSensitive;
    this.isAlias = isAlias;
  }
  
  
  
  /**
   * Returns the name/handle/nickname of the user.
   */

  public String getName(){
    return name;
  }
  
  
  
  /**
   * Returns the value of <code>isAlias</code> passed to the constructor.
   */
  
  public boolean isAlias(){
    return isAlias;
  }
  
  
  
  /**
   * Returns whether the specified object has the same class as this one, and
   * its name matches the name of this user.
   */
  
  public boolean equals(Object o){
    if (o == null)
      return false;
    
    if (!o.getClass().equals(this.getClass()))
      return false;
    
    PlainServerUser user = (PlainServerUser)o;
    
    if (isCaseSensitive != user.isCaseSensitive)
      return false;
    
    if (isCaseSensitive)
      return name.equals(user.name);
    else
      return name.equalsIgnoreCase(user.name);
  }
  
  
  
  /**
   * Returns the hash code of this <code>PlainServerUser</code>.
   */
  
  public int hashCode(){
    if (isCaseSensitive)
      return name.hashCode();
    else
      return name.toLowerCase().hashCode();
  }
  
  
  
  /**
   * Returns the user's name/handle/nickname.
   */
  
  public String toString(){
    return getName();
  }
  
  
  
}
