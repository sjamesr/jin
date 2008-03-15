/**
 * Jin - a chess client for internet chess servers.
 * More information is available at http://www.jinchess.com/.
 * Copyright (C) 2008 Alexander Maryanovsky.
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

package free.jin.console;

import free.jin.I18n;



/**
 * Encapsulates information about a channel (room) on a server.
 * 
 * @author Maryanovsky Alexander
 */

public class Channel{
  
  
  
  /**
   * The id of this channel.
   */
  
  private final Object id;
  
  
  
  /**
   * Creates a new <code>Channel</code> with the specified id (which also serves
   * as an i18n key, after being converted to a string).
   */
  
  public Channel(Object id){
    if (id == null)
      throw new IllegalArgumentException("id may not be null");
    
    this.id = id;
  }
  
  
  
  /**
   * Returns the id of this channel.
   */
  
  public final Object getId(){
    return id;
  }
  
  
  
  /**
   * Returns the short name of the channel. This is a short (preferably one
   * word) string which will be displayed to the user. The name is retrieved
   * from the <code>I18n</code> object associated with the class of the object
   * by specifying "[id].shortName" as the i18n key. If there is no translation
   * associated with this key, <code>id.toString()</code> is returned.
   */
  
  public String getShortName(){
    return I18n.get(getClass()).getString(id + ".shortName", id.toString());
  }
  
  
  
  /**
   * Returns the long name of the channel. This is a medium sized (a few words)
   * string which will be displayed to the user. The name is retrieved from the
   * <code>I18n</code> object associated with the class of the object by
   * specifying "[id].longName" as the i18n key. If there is no translation
   * associated with this key, the short name of the channel is returned.
   */
  
  public String getLongName(){
    return I18n.get(getClass()).getString(id + ".longName", getShortName());
  }
  
  
  
  /**
   * Returns the description of the channel. The name is retrieved from
   * the <code>I18n</code> object associated with the class of the object by
   * specifying "[id].description" as the i18n key. If there is no translation
   * associated with this key, returns <code>null</code>;
   */
  
  public String getDescription(){
    I18n i18n = I18n.get(getClass(), Channel.class);
    return i18n.getString(id + ".description", null);
  }
  
  
  
}
