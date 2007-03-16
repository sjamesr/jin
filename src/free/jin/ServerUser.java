package free.jin;



/**
 * Represents a user on a chess server.
 */

public interface ServerUser{
  
  
  
  /**
   * Returns the user's name/handle/nickname. This should be displayable to
   * the user (of the client).
   */
  
  String getName();
  
  
  
  /**
   * Returns whether the specified <code>ServerUser</code> is the same as this
   * one.
   */
  
  boolean equals(Object user);
  
  
  
  /**
   * Returns the hash code of this <code>ServerUser</code> object.
   */
  
  int hashCode();
  

}
