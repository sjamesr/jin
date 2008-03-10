package free.jin.console.ics;

import free.jin.console.Channel;



/**
 * A channel on an ICS server.
 * 
 * @author Maryanovsky Alexander
 */

public class IcsChannel extends Channel{
  
  
  
  /**
   * Creates a new IcsChannel with the specified channel number.
   */
  
  public IcsChannel(int channelNumber){
    super(new Integer(channelNumber));
  }
  
  
  
}
