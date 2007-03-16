package free.jin.chessclub;

import free.jin.PlainServerUser;



/**
 * A user of ICC.
 */

public final class ChessclubUser extends PlainServerUser{
  
  
  
  /**
   * Creates a new <code>ChessclubUser</code> with the specified handle.
   */
  
  private ChessclubUser(String handle){
    super(handle, false);
  }
  
  
  
  /**
   * Returns a <code>ChessclubUser</code> with the specified handle.
   * We use this method to allow caching in the future.
   */
  
  public static ChessclubUser get(String handle){
    return new ChessclubUser(handle);
  }
  
  
  
}
