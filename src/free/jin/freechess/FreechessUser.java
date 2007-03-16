package free.jin.freechess;

import free.jin.PlainServerUser;



/**
 * A user of FICS.
 */

public final class FreechessUser extends PlainServerUser{
  
  
  
  /**
   * Creates a new <code>FreechessUser</code> with the specified handle.
   */
  
  private FreechessUser(String handle){
    super(handle, false);
  }
  
  
  
  /**
   * Returns a <code>FreechessUser</code> with the specified handle.
   * We use this method to allow caching in the future.
   */
  
  public static FreechessUser get(String handle){
    return new FreechessUser(handle);
  }
  
  
  
}
