package free.jin;



/**
 * A simple implementation of <code>ServerUser</code>, which keeps the username
 * as a property and may be set-up as case-sensitive or case-insensitive.
 */

public class PlainServerUser implements ServerUser{
  
  
  
  /**
   * The user's name/handle/nickname.
   */
  
  private final String name;
  
  
  
  /**
   * Whether the username is case sensitive.
   */
  
  private final boolean isCaseSensitive;
  
  
  
  /**
   * Creates a new <code>PlainServerUser</code> with the specified
   * case-sensitive or case-insensitive user name/handle/nickname. 
   */
  
  protected PlainServerUser(String name, boolean isCaseSensitive){
    if (name == null)
      throw new IllegalArgumentException("name may not be null");
    
    this.name = name;
    this.isCaseSensitive = isCaseSensitive;
  }
  
  
  
  /**
   * Returns the name/handle/nickname of the user.
   */

  public String getName(){
    return name;
  }
  
  
  
  /**
   * Returns whether the specified object has the same class as this one, and
   * its name matches the name of this user.
   */
  
  public boolean equals(Object o){
    if (!o.getClass().equals(this.getClass()))
      return false;
    
    PlainServerUser user = (PlainServerUser)o;
    
    if (isCaseSensitive != user.isCaseSensitive)
      return false;
    
    if (isCaseSensitive)
      return name.equals(user.name);
    else
      return name.equalsIgnoreCase(user.name);
  }
  
  
  
  /**
   * Returns the hash code of this <code>PlainServerUser</code>.
   */
  
  public int hashCode(){
    if (isCaseSensitive)
      return name.hashCode();
    else
      return name.toLowerCase().hashCode();
  }
  
  
  
  /**
   * Returns the user's name/handle/nickname.
   */
  
  public String toString(){
    return getName();
  }
  
  
  
}
