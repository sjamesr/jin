/**
 * The utillib library. More information is available at http://www.jinchess.com/. Copyright (C)
 * 2007 Alexander Maryanovsky. All rights reserved.
 *
 * <p>The utillib library is free software; you can redistribute it and/or modify it under the terms
 * of the GNU Lesser General Public License as published by the Free Software Foundation; either
 * version 2 of the License, or (at your option) any later version.
 *
 * <p>The utillib library is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
 * PURPOSE. See the GNU Lesser General Public License for more details.
 *
 * <p>You should have received a copy of the GNU Lesser General Public License along with utillib
 * library; if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston,
 * MA 02111-1307 USA
 */
package free.util;

/**
 * Allows one to attach a name to an object. The name is also returned by the <code>toString</code>
 * method, making this class useful for objects put into Swing container classes ( <code>JTables
 * </code> and such).
 */
public final class NamedWrapper extends AbstractNamed {

  /** The object. */
  private final Object target;

  /** Creates a new <code>NamedWrapper</code> with the specified target and name. */
  public NamedWrapper(Object target, String name) {
    super(name);

    this.target = target;
  }

  /** Returns the target. */
  public Object getTarget() {
    return target;
  }

  /** A <code>NamedWrapper</code> is equal to another iff their names and targets are equal. */
  @Override
  public boolean equals(Object o) {
    if (!(o instanceof NamedWrapper)) return false;

    NamedWrapper other = (NamedWrapper) o;
    return Utilities.areEqual(this.getName(), other.getName())
        && Utilities.areEqual(this.getTarget(), other.getTarget());
  }

  /** Returns the hash code of this object. */
  @Override
  public int hashCode() {
    return Utilities.hashCode(getName(), getTarget());
  }
}
