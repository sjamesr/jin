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

package free.jin.console;


/**
 * An class encapsulating a link in a ConsoleTextPane. A Link consists of 2 integers
 * specifying the start and end of the link in the text and the 
 */

public class Link{


  /**
   * The starting index of the link.
   */

  private final int startIndex;



  /**
   * The ending index of the link.
   */

  private final int endIndex;




  /**
   * The Command to execute when the link is clicked.
   */

  private final Command command;




  /**
   * Creates a new Link with the given start index, end index and Command to
   * execute when the link is clicked.
   */

  public Link(int startIndex, int endIndex, Command command){
    this.startIndex = startIndex;
    this.endIndex = endIndex;
    this.command = command;
  }




  /**
   * Returns the starting index of the link in the text.
   */

  public int getStartIndex(){
    return startIndex;
  }



  /**
   * Returns the ending index of the link in the text.
   */

  public int getEndIndex(){
    return endIndex;
  }



  /**
   * Returns the Command to be issued when the link is clicked.
   */

  public Command getCommand(){
    return command;
  }



  /**
   * Returns a textual representation of this Link object.
   */

  public String getString(){
    return getClass().getName()+"[startIndex="+getStartIndex()+";endIndex="+getEndIndex()+";command="+getCommand()+"]";
  }


}
