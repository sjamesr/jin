/**
 * Jin - a chess client for internet chess servers.
 * More information is available at http://www.jinchess.com/.
 * Copyright (C) 2002 Alexander Maryanovsky.
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

package free.jin.event;

import java.util.EventObject;
import free.jin.Connection;


/**
 * The superclass for all events fired by <code>Connection</code>
 * implementations.
 */

public class JinEvent extends EventObject{



  /**
   * Creates a new <code>JinEvent</code> with the specified
   * <code>Connection</code> which created and fired (or will fire) it.
   */

  public JinEvent(Connection conn){
    super(conn);
  }




  /**
   * Returns the <code>Connection</code> which created and fired (or will fire)
   * this <code>JinEvent</code>.
   */

  public Connection getConnection(){
    return (Connection)getSource();
  }



}

