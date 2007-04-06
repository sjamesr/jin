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

import java.nio.charset.Charset;

import free.jin.Connection;
import free.util.TextUtilities;

/**
 * The event sent when text that couldn't be identified as some known type
 * arrives from the server.
 */

public class PlainTextEvent extends JinEvent{
  
  
  
  /**
   * The text that arrived from the server.
   */

  private final String text;
  
  
  
  /**
   * The encoding of the text, that is, encoding such that a call to
   * <code>text.getBytes(textEncoding)</code> returns the text's raw
   * bytes. A value of <code>null</code> indicates that the text should not be
   * re-encoded because it is either already in Unicode or the server only
   * supports 7-bit ASCII (thus re-encoding will not help). 
   */
  
  private final String textEncoding;



  /**
   * Creates a new <code>PlainTextEvent</code> with the given text and its
   * encoding.
   * A value of <code>null</code> indicates that the text should not be
   * re-encoded because it is either already in Unicode or the server only
   * supports 7-bit ASCII (thus re-encoding will not help).
   */

  public PlainTextEvent(Connection conn, String text, String textEncoding){
    super(conn);
    
    if ((textEncoding != null) && !Charset.isSupported(textEncoding))
      throw new IllegalArgumentException("Unsupported encoding: " + textEncoding);
    
    this.text = text;
    this.textEncoding = textEncoding;
  }


  
  /**
   * Returns the text.
   */

  public String getText(){
    return text;
  }
  
  
  
  /**
   * Returns the text, encoded using the specified encoding.
   * This is useful because some servers don't support Unicode, but some do
   * support 8-bit characters, which allows some non English text to be sent, as
   * long as the sender and the receiver agree on the encoding.
   * For servers like these, this method encodes the raw bytes of the text into
   * a string via </code>new String(rawBytes, encoding)</code> and returns the
   * result. For servers which support Unicode or do not support even 8-bit
   * characters (only 7-bit ASCII), this method returns the same value as
   * <code>getText()</code>.
   */
  
  public String getText(String encoding){
    if (textEncoding == null)
      return text;
    else
      return TextUtilities.convert(text, textEncoding, encoding);
  }
  
  
  
}
