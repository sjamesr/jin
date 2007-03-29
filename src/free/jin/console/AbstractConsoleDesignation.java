package free.jin.console;




/**
 * A skeleton implementation of <code>ConsoleDesignation</code>
 */

public abstract class AbstractConsoleDesignation implements ConsoleDesignation{
  
  
  
  /**
   * The name of the designation.
   */
  
  private final String name;
  
  
  
  /**
   * Whether the console is temporary.
   */
  
  private final boolean isConsoleTemporary;
  
  
  
  /**
   * Creates a new <code>AbstractConsoleDesignation</code> with the specified
   * name and temporary status.
   */
  
  public AbstractConsoleDesignation(String name, boolean isConsoleTemporary){
    this.name = name;
    this.isConsoleTemporary = isConsoleTemporary;
  }
  
  
  
  /**
   * Returns the name of this designation.
   */

  public String getName(){
    return name;
  }
  
  
  
  /**
   * Returns whether the console is temporary.
   */
  
  public boolean isConsoleTemporary(){
    return isConsoleTemporary;
  }
  
  
  
}
