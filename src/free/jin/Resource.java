/**
 * Jin - a chess client for internet chess servers.
 * More information is available at http://www.jinchess.com/.
 * Copyright (C) 2004 Alexander Maryanovsky.
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

import java.net.URL;
import java.io.IOException;
import free.jin.plugin.Plugin;


/**
 * An interface any Jin resource must implement. An implementing class must
 * also have a no-arg constructor so that it can be instantiated.
 */
 
public interface Resource{
  
  
  
  /**
   * This method is called once immediately after the class is instantiated to
   * let the resource load itself from the specified URL and for the specified
   * plugin. Returns whether the resource was loaded successfully and is ready
   * to be used.
   */
   
  boolean load(URL url, Plugin plugin) throws IOException;
  
  
  
  /**
   * Returns the type of the resource.
   */
   
  String getType();
  
  
  
  /**
   * Returns the id of the resource. Between all resources with the same type,
   * the id should be unique.
   */
   
  String getId();
  
  
   
}