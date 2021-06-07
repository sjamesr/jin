/**
 * Jin - a chess client for internet chess servers. More information is available at
 * http://www.jinchess.com/. Copyright (C) 2005 Alexander Maryanovsky. All rights reserved.
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
package free.jin;

import java.util.EventObject;

/** The event object fired when a session to the chess server is established or closed. */
public class SessionEvent extends EventObject {

  /** The code for an event fired just before a session is started. */
  public static final int SESSION_STARTING = -1;

  /** The code for when a session was started. */
  public static final int SESSION_ESTABLISHED = 1;

  /** The code for an event fired just before the session is being closed. */
  public static final int SESSION_CLOSING = 2;

  /** The code for when a session is closed. */
  public static final int SESSION_CLOSED = 0;

  /** The id of the event. */
  private final int id;

  /** The session. */
  private final Session session;

  /** Creates a new <code>SessionEvent</code>. */
  public SessionEvent(ConnectionManager connManager, int id, Session session) {
    super(connManager);

    switch (id) {
      case SESSION_ESTABLISHED:
      case SESSION_CLOSED:
      case SESSION_STARTING:
      case SESSION_CLOSING:
        break;
      default:
        throw new IllegalArgumentException("Invalid id: " + id);
    }

    this.id = id;
    this.session = session;
  }

  /** Returns the connection manager responsible for the session. */
  public ConnectionManager getConnManager() {
    return (ConnectionManager) getSource();
  }

  /**
   * Returns the id of the session event - either {@link #SESSION_ESTABLISHED} or {@link
   * #SESSION_CLOSED}.
   */
  public int getId() {
    return id;
  }

  /**
   * Returns the session. In the case of a <code>SESSION_STARTING</code> event, this is <code>null
   * </code>.
   */
  public Session getSession() {
    return session;
  }
}
