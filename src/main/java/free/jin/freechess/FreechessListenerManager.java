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
package free.jin.freechess;

import free.freechess.Ivar;
import free.jin.event.BasicListenerManager;
import free.jin.event.SeekListener;
import free.jin.freechess.event.IvarStateChangeEvent;
import free.jin.freechess.event.IvarStateChangeListener;

/**
 * A freechess.org specific extension of BasicListenerManager. Used by <code>
 * free.jin.freechess.JinFreechessConnection</code>
 */
public class FreechessListenerManager extends BasicListenerManager {

  /** The source JinFreechessConnection. */
  private final JinFreechessConnection source;

  /**
   * Creates a new FreechessListenerManager with the given source <code>JinFreechessConnection
   * </code>.
   */
  public FreechessListenerManager(JinFreechessConnection source) {
    super(source);

    this.source = source;
  }

  /** Adds the given SeekListener to the list of listeners receiving notification of SeekEvents. */
  @Override
  public void addSeekListener(SeekListener listener) {
    super.addSeekListener(listener);

    if (listenerList.getListenerCount(SeekListener.class) == 1)
      source.setIvarState(Ivar.SEEKINFO, true);
  }

  /**
   * Removes the given SeekListener from the list of listeners receiving notification of SeekEvents.
   */
  @Override
  public void removeSeekListener(SeekListener listener) {
    super.removeSeekListener(listener);

    if (listenerList.getListenerCount(SeekListener.class) == 0)
      source.setIvarState(Ivar.SEEKINFO, false);
  }

  /**
   * Adds the specified IvarStateChangeListener to the list of listeners receiving notification of
   * changes in states of ivars.
   */
  public void addIvarStateChangeListener(IvarStateChangeListener listener) {
    listenerList.add(IvarStateChangeListener.class, listener);
  }

  /**
   * Removes the specified IvarStateChangeListener from the list of listeners receiving notification
   * of changes in states of ivars.
   */
  public void removeIvarStateChangeListener(IvarStateChangeListener listener) {
    listenerList.remove(IvarStateChangeListener.class, listener);
  }

  /** Notifies all registered IvarStateChangeListeners of the specified IvarStateChangeEvent. */
  public void fireIvarStateChangeEvent(IvarStateChangeEvent evt) {
    Object[] listeners = listenerList.getListenerList();
    for (int i = 0; i < listeners.length; i += 2) {
      if (listeners[i] == IvarStateChangeListener.class) {
        IvarStateChangeListener listener = (IvarStateChangeListener) listeners[i + 1];
        listener.ivarStateChanged(evt);
      }
    }
  }
}
