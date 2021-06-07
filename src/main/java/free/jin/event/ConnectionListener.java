/**
 * Jin - a chess client for internet chess servers. More information is available at
 * http://www.jinchess.com/. Copyright (C) 2002 Alexander Maryanovsky. All rights reserved.
 *
 * <p>This program is free software; you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation; either version 2 of the
 * License, or (at your option) any later version.
 *
 * <p>This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * <p>You should have received a copy of the GNU General Public License along with this program; if
 * not, write to the Free Software Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA
 * 02111-1307, USA.
 */
package free.jin.event;

import free.jin.Connection;
import java.util.EventListener;

/**
 * A listener for receiving notifications about changes in the state of the connection to the
 * server.
 */
public interface ConnectionListener extends EventListener {

  /** Gets called when a connection attempt is made. */
  void connectionAttempted(Connection conn, String hostname, int port);

  /** Gets called when the connection to the server is established. */
  void connectionEstablished(Connection conn);

  /** Gets called when the connection attempt failed. */
  void connectingFailed(Connection conn, String reason);

  /** Gets called when the login procedure is successful. */
  void loginSucceeded(Connection conn);

  /** Gets called when the login procedure fails. Note that <code>reason</code> may be null. */
  void loginFailed(Connection conn, String reason);

  /** Gets called when the connection to the server is lost. */
  void connectionLost(Connection conn);
}
