/**
 * Jin - a chess client for internet chess servers. More information is available at
 * http://www.jinchess.com/. Copyright (C) 2008 Alexander Maryanovsky. All rights reserved.
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
package free.jin.console.fics;

import free.jin.console.Channel;
import free.jin.console.ics.IcsChannel;

/**
 * A FICS channel. This class is needed not for its functionality, but because channel properties
 * (such as name, description) are retrieved via an <code>I18n</code> object corresponding to the
 * actual class of the {@link Channel} object.
 *
 * @author Maryanovsky Alexander
 */
public class FicsChannel extends IcsChannel {

  /** Creates a new <code>FicsChannel</code> with the specified channel number. */
  public FicsChannel(int channelNumber) {
    super(channelNumber);
  }
}
