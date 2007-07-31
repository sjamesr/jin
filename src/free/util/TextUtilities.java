/**
 * The utillib library.
 * More information is available at http://www.jinchess.com/.
 * Copyright (C) 2002-2003 Alexander Maryanovsky.
 * All rights reserved.
 *
 * The utillib library is free software; you can redistribute
 * it and/or modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 2 of the
 * License, or (at your option) any later version.
 *
 * The utillib library is distributed in the hope that it will
 * be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser
 * General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with utillib library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package free.util;

import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.StringTokenizer;


/**
 * A utility class which provides many useful text manipulation methods.
 */

public class TextUtilities{
  
  
  
  /**
   * Pads the beginning of the given String with the given character until it's
   * <code>length</code> characters long. If the given String's size is already
   * <code>length</code> or larger, the given string is returned as is.
   */

  public static String padStart(String s, char c, int length){
    if (s.length()>=length)
      return s;

    StringBuffer buf = new StringBuffer(s);
    for (int i=s.length();i<length;i++)
      buf.insert(0,c);

    return buf.toString();
  }



  /**
   * Pads the end of the given String with the given character until it's
   * <code>length</code> characters long. If the given String's size is already
   * <code>length</code> or larger, the given string is returned as is.
   */

  public static String padEnd(String s, char c, int length){
    if (s.length()>=length)
      return s;

    StringBuffer buf = new StringBuffer(s);
    for (int i=s.length();i<length;i++)
      buf.append(c);

    return buf.toString();
  }



  /**
   * Pads the given String on both sides equally (if possible) with the given 
   * character until it's <code>length</code> characters long. If the given 
   * String's size is already <code>length</code> or larger, the given 
   * string is returned as is.
   */

  public static String padSides(String s, char c, int length){
    if (s.length()>=length)
      return s;

    StringBuffer buf = new StringBuffer(s);
    for (int i=s.length();i<length-1;i+=2){
      buf.insert(0,c);
      buf.append(c);
    }

    if (buf.length()<length)
      buf.insert(0,c);

    return buf.toString();
  }
  
  
  
  /**
   * Trims the specified string on the right only.
   */
   
   public static String trimRight(String s){
     StringBuffer buf = new StringBuffer(s);
     int length = buf.length();
     while (Character.isWhitespace(buf.charAt(length - 1)))
       buf.setLength(--length);
     
     return buf.toString();
   }



  /**
   * Trims the specified string on the left only.
   */
   
   public static String trimLeft(String s){
     int i = 0;
     while (Character.isWhitespace(s.charAt(i)))
       i++;
     
     return s.substring(i);
   }



  /**
   * <P>Returns a substring of the given StringBuffer's string which consists of
   * the characters from the beginning of it until the first occurrence of the
   * given delimiter string or if the delimiter doesn't occur, until the end
   * of the string. The StringBuffer is modified so it no longer contains those
   * characters or the delimiter.
   * <P>Examples:
   * <UL>
   *   <LI>nextToken(new StringBuffer("abcdefgh"), "de") returns "abc" and
   *       the StringBuffer is modified to represent the string "fgh".
   *   <LI>nextToken(new StringBuffer("abcdefgh"), "a") returns an empty string
   *       and the StringBuffer is modified to represent the string "bcdefgh".
   *   <LI>nextToken(new StringBuffer("abcdefgh"), "k") returns "abcdefgh" and
   *       the StringBuffer is modified to represent an empty string.
   * </UL>
   */

  public static String nextToken(StringBuffer buf, String delimiter){
    String bufStr = buf.toString();
    int delimIndex = bufStr.indexOf(delimiter);

    if (delimIndex==-1){
      buf.setLength(0);
      return bufStr;
    }
      
    String str = bufStr.substring(0,delimIndex);
    buf.reverse();
    buf.setLength(buf.length()-delimIndex-delimiter.length());
    buf.reverse();

    return str;
  }





  /**
   * Returns an array of the tokens produced by the specified string with the
   * specified delimiter characters.
   */

  public static String [] getTokens(String text, String delimiters){
    StringTokenizer tokenizer = new StringTokenizer(text, delimiters);
    String [] tokens = new String[tokenizer.countTokens()];
    for (int i = 0; i < tokens.length; i++)
      tokens[i] = tokenizer.nextToken();

    return tokens;
  }
  
  
  
  /**
   * Parses the specified list of strings delimited by the specified delimiters.
   */
  
  public static String [] parseStringList(String text, String delimiters){
    StringTokenizer tokenizer = new StringTokenizer(text, delimiters);
    String [] tokens = new String[tokenizer.countTokens()];
    for (int i = 0; i < tokens.length; i++)
      tokens[i] = tokenizer.nextToken();

    return tokens;
  }
  
  
  
  /**
   * Parses the specified list of integers delimited by the specified
   * delimiters.
   */

  public static int [] parseIntList(String text, String delimiters){
    StringTokenizer tokenizer = new StringTokenizer(text, delimiters);
    int [] tokens = new int[tokenizer.countTokens()];
    for (int i = 0; i < tokens.length; i++)
      tokens[i] = Integer.parseInt(tokenizer.nextToken());

    return tokens;
  }
  
  
  
  /**
   * Translates the specified resource name into the context of the specified
   * class. Basically, this method returns the name of the resource you need to
   * use when loading via a classloader, if via the specified class you would
   * load it simply with <code>Class.getResource(resourceName)</code>.
   */
   
  public static String translateResource(Class c, String resourceName){
    String path = c.getName();
    int dotIndex = path.lastIndexOf('.');
    if (dotIndex != -1){
      path = path.substring(0, dotIndex);
      path = path.replace('.', '/') + '/' + resourceName;
    }
    else
      return resourceName;
    
    return path;
  }
  
  
  
  /**
   * Breaks the specified text into lines no longer than the specified length,
   * without breaking words. The breaking is done by inserting newlines at
   * appropriate locations. Note that if there are words longer than
   * <code>maxLength</code>, they will not be broken.
   */
  
  public static String breakIntoLines(String text, int maxLength){
    StringBuffer buf = new StringBuffer(text);
    text = text + " "; // Fake an extra space at the end to simplify the algorithm 
    
    int lastNewline = -1; // The index of the last newline
    int lastSpace = -1; // The index of the last space
    int curSpace; // The index of the current space
    
    while ((curSpace = text.indexOf(' ', lastSpace + 1)) != -1){
      if ((curSpace - lastNewline - 1 > maxLength) && (lastSpace > lastNewline)){
        // lastSpace == lastNewline means the current word is longer than maxLength
        buf.setCharAt(lastSpace, '\n');
        lastNewline = lastSpace;
      }
    
      lastSpace = curSpace;
    }
  
    return buf.toString();
  }
  
  
  
  /**
   * The name of the system's default charset, lazily initialized.
   */
  
  private static String defaultCharsetName = null;
  
  
  
  /**
   * Returns the name of the system's default charset. This method is useful
   * under 1.4, because <code>Charset.defaultCharset()</code> is only available
   * since 1.5.
   */
  
  public static String getDefaultCharsetName(){
    if (defaultCharsetName == null)
      defaultCharsetName = 
        new InputStreamReader(new ByteArrayInputStream(new byte[0])).getEncoding();
    
    return defaultCharsetName;
  }
  
  
  
  /**
   * Converts the specified string between encodings. If either of the
   * encodings is <code>null</code>, no conversion is made. If both the
   * encodings are not <code>null</code> and one of the encodings
   * is unsupported, an <code>IllegalArgumentException</code> is thrown (as
   * opposed to an <code>UnsupportedEncodingException</code>. 
   */
  
  public static String convert(String s, String fromEncoding, String toEncoding)
      throws IllegalArgumentException{
    
    if ((fromEncoding == null) || (toEncoding == null))
      return s;
    
    byte [] rawBytes;
    try{
      rawBytes = s.getBytes(fromEncoding);
    } catch (UnsupportedEncodingException e){
      throw new IllegalArgumentException("Unsupported encoding: " + fromEncoding);
      }
    
    try{
      return new String(rawBytes, toEncoding);
    } catch (UnsupportedEncodingException e){
        throw new IllegalArgumentException("Unsupported encoding: " + toEncoding);
      }
  }
  
  
  
}
