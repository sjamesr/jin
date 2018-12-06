/**
 * The chess framework library. More information is available at http://www.jinchess.com/. Copyright
 * (C) 2002 Alexander Maryanovsky. All rights reserved.
 *
 * The chess framework library is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free Software Foundation;
 * either version 2 of the License, or (at your option) any later version.
 *
 * The chess framework library is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
 * PURPOSE. See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with the chess
 * framework library; if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite
 * 330, Boston, MA 02111-1307 USA
 */
package free.chess;

/**
 * Represents a location on the chess board.
 */
public class Square {

  /**
   * The pool of unique immutable instances of Square.
   */
  private static final Square[][] POOL = new Square[8][8];

  /**
   * The file of the square.
   */
  private final int file;

  /**
   * The rank of the square.
   */
  private final int rank;

  /**
   * Creates a new Square with the given coordinates.
   *
   * @param file The file index [0-7].
   * @param rank The rank index [0-7].
   */
  private Square(int file, int rank) {
    this.file = file;
    this.rank = rank;
  }

  /**
   * Returns whether the specified file and rank constitute a valid square.
   */
  public static boolean isValid(int file, int rank) {
    return (file >= 0) && (file < 8) && (rank >= 0) && (rank < 8);
  }

  /**
   * Returns an instance of Square representing the given coordinates. Valid values lie in the range
   * [0-7].
   *
   * @param file The file index [0-7]
   * @param rank The rank index [0-7]
   *
   * @throws IllegalArgumentException if the arguments do not constitute a valid square.
   */
  public static Square getInstance(int file, int rank) throws IllegalArgumentException {
    Square result = getInstanceNonStrict(file, rank);

    if (result == null)
      throw new IllegalArgumentException(
          "file and rank must be in the range [0-7] " + "(file:" + file + " rank:" + rank + ")");
    else return result;
  }

  /**
   * Same as {@link #getInstance(int, int)}, but instead of throwing an exception for illegal
   * values, simply returns <code>null</code>.
   */
  public static synchronized Square getInstanceNonStrict(int file, int rank) {
    if (!isValid(file, rank)) return null;

    if (POOL[file][rank] == null) POOL[file][rank] = new Square(file, rank);

    return POOL[file][rank];
  }

  /**
   * Returns an instance of Square representing the given square. The square is specified by a
   * string in the usual chess format that represents a square, for example: "e3".
   *
   * @param square The string representing the location.
   *
   * @throws IllegalArgumentException if the given string is not in the expected format or the
   * coordinates are out of the valid range.
   */
  public static Square parseSquare(String square) {
    try {
      int file = square.charAt(0) - 'a';
      int rank = square.charAt(1) - '1';
      return getInstance(file, rank);
    } catch (Exception e) {
      throw new IllegalArgumentException(
          "The given string(" + square + ") does not represent a valid location on a chess board");
    }
  }

  /**
   * Returns the file of this Square, a value in the range [0-7].
   */
  public int getFile() {
    return file;
  }

  /**
   * Returns the rank of this Square, a value in the range [0-7].
   */
  public int getRank() {
    return rank;
  }

  /**
   * Returns a character representing the file of this Square. The returned character will be in the
   * range ['a'-'h'].
   */
  public char getFileChar() {
    return (char) ('a' + getFile());
  }

  /**
   * Returns a character representing the rank of this Square. The returned character will be in the
   * range ['1'-'8'].
   */
  public char getRankChar() {
    return (char) ('1' + getRank());
  }

  /**
   * Returns a string representation of the square as is common in chess. For example, for a square
   * with file = 4 and rank = 1 this method will return "e2".
   */
  @Override
  public String toString() {
    char[] buff = new char[2];
    buff[0] = getFileChar();
    buff[1] = getRankChar();
    return new String(buff);
  }

  /**
   * Returns the hashCode of this Square.
   */
  @Override
  public int hashCode() {
    return (rank << 3) | file;
  }

  /**
   * Returns <code>true</code> if this square is the same as the square represented by the given
   * String.
   *
   * @see #parseSquare(String).
   */
  public boolean equals(String s) {
    return this == parseSquare(s);
  }

  /**
   * Returns <code>true</code> if this square is the same as the square with the given file and
   * rank.
   */
  public boolean equals(int file, int rank) {
    return this == getInstance(file, rank);
  }
}
