/**
 * The chessclub.com connection library. More information is available at http://www.jinchess.com/.
 * Copyright (C) 2007 Alexander Maryanovsky. All rights reserved.
 *
 * <p>The chessclub.com connection library is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later version.
 *
 * <p>The chessclub.com connection library is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.
 *
 * <p>You should have received a copy of the GNU Lesser General Public License along with the
 * chessclub.com connection library; if not, write to the Free Software Foundation, Inc., 59 Temple
 * Place, Suite 330, Boston, MA 02111-1307 USA
 */
package free.chessclub.level1;

/**
 * Encapsulates a level1 packet. See <code>ftp://ftp.chessclub.com/pub/icc/formats/formats.txt
 * </code> for documentation.
 */
public class Packet {

  /** The command code. */
  private final int commandCode;

  /** The name of the player who issued the commandCode. */
  private final String playerName;

  /** The client tag (the "arbitrary-word" in formats.txt). May be <code>null</code>. */
  private final String clientTag;

  /**
   * The contents of the packet. May be datagrams, other packets, lines of text or anything else the
   * server may decide to send in the future.
   */
  private final Object[] items;

  /**
   * Creates a new <code>Packet</code> with the specified arguments.
   *
   * @param commandCode The code of the commandCode which triggered this packet.
   * @param playerName The name of the player who issued the commandCode.
   * @param clientTag The client tag ("arbitrary-word" in formats.txt). May be <code>null</code>.
   * @param items The contents of the datagram.
   */
  public Packet(int commandCode, String playerName, String clientTag, Object[] items) {
    this.commandCode = commandCode;
    this.playerName = playerName;
    this.clientTag = clientTag;
    this.items = items;
  }

  /** Returns the code of the commandCode which triggered this packet. */
  public int getCommandCode() {
    return commandCode;
  }

  /** Returns the name of the player who issued the command. */
  public String getPlayerName() {
    return playerName;
  }

  /** Returns the client tag ("arbitrary-word" in formats.txt). May be <code>null</code>. */
  public String getClientTag() {
    return clientTag;
  }

  /** Returns the number of items in the contents of the packet. */
  public int getItemCount() {
    return items.length;
  }

  /**
   * Returns the <code>n/code>th item of the contents of the packet.
   */
  public Object getItem(int n) {
    return items[n];
  }
}
