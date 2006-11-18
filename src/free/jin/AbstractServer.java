/**
 * Jin - a chess client for internet chess servers.
 * More information is available at http://www.jinchess.com/.
 * Copyright (C) 2003 Alexander Maryanovsky.
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

package free.jin;

import java.util.Properties;
import java.io.IOException;
import free.util.IOUtilities;
import free.util.TextUtilities;


/**
 * An implementation of some of the <code>Server</code> interface by loading
 * the required information from a properties file. The properties file must
 * be named "/properties" and must be loadable via the classloader that loaded
 * the concrete subclass of this class. See the methods documentation for the
 * names and format of the various properties.
 */

public abstract class AbstractServer implements Server{
  
  
  
  /**
   * The <code>I18n</code> for this class.
   */
  
  private I18n i18n;



  /**
   * The server properties.
   */

  private final Properties props;



  /**
   * The username policy.
   */

  private final UsernamePolicy usernamePolicy;

  
  
  /**
   * The guest account.
   */

  private User guest;



  /**
   * Creates a new <code>AbstractServer</code> object by loading the required
   * information from a properties file.
   */

  protected AbstractServer(){
    try{
      props = IOUtilities.loadProperties(getClass().getResource("properties"), true);
    } catch (IOException e){
        throw new IllegalStateException("No properties file found");
      }

    usernamePolicy = createUsernamePolicy();
  }



  /**
   * Creates and returns the username policy. This method is invoked once, from
   * the constructor, so it may not depend on any information passed to the
   * subclass' constructor.
   */

  protected abstract UsernamePolicy createUsernamePolicy();
  
  
  
  /**
   * Returns the <code>I18n</code> for this server.
   */
  
  public I18n getI18n(){
    if (i18n == null)
      i18n = I18n.get(getClass(), AbstractServer.class);
    
    return i18n; 
  }



  /**
   * Returns the username policy of this server.
   */

  public final UsernamePolicy getUsernamePolicy(){
    return usernamePolicy;
  }



  /**
   * Sets the guest account.
   */

  public synchronized void setGuestUser(User user){
    if (guest != null)
      throw new IllegalStateException("Guest already created");

    String guestUsername = usernamePolicy.getGuestUsername();
    guest = (user != null) ? user : new User(this, guestUsername);
  }

  
  
  /**
   * Returns the guest account.
   */

  public User getGuest(){
    return guest;
  }

  
  
  /**
   * Returns the hostname of the default host for this server. The property name
   * is <code>defaultHost</code>.
   */

  public String getDefaultHost(){
    return props.getProperty("defaultHost");
  }
  
  
  
  /**
   * Returns the list of hostnames for the hosts of this server. The property
   * name is <code>hosts</code> and the property value is a space delimited
   * list of hostnames.
   */

  public String [] getHosts(){
    return TextUtilities.getTokens(props.getProperty("hosts"), " ");
  }



  /**
   * Sets the host to connect to.
   */
   
  public void setHost(String host){
    props.put("defaultHost", host);
    props.put("hosts", host);
  }



  /**
   * Returns a list of the ports on which the server is listening. The property
   * name is <code>ports</code> and the property value is a space delimited list
   * of valid port values.
   */

  public int [] getPorts(){
    return TextUtilities.parseIntList(props.getProperty("ports"), " ");
  }
  
  
  
  /**
   * Sets the port to connect on.
   */
   
  public void setPort(int port){
    props.put("ports", String.valueOf(port));
  }



  /**
   * Returns the id of this server. The property name is <code>id</code>.
   */

  public String getId(){
    return props.getProperty("id");
  }



  /**
   * Returns the short name of this server. The property name is
   * <code>shortName</code>.
   */

  public String getShortName(){
    return getI18n().getString(props.getProperty("shortNameKey"));
  }



  /**
   * Returns the long name of this server. The property name is
   * <code>longName</code>.
   */

  public String getLongName(){
    return getI18n().getString(props.getProperty("longNameKey"));
  }



  /**
   * Returns the URL of the website for this server. The property name is
   * <code>website</code>.
   */

  public String getWebsite(){
    return props.getProperty("website");
  }



  /**
   * Returns the URL of the server's registration page. The property name is
   * <code>registrationPage</code>.
   */

  public String getRegistrationPage(){
    return props.getProperty("registrationPage");
  }



  /**
   * Returns the URL of server's password retrieval page. The property name is
   * <code>passwordRetrievalPage</code>.
   */

  public String getPasswordRetrievalPage(){
    return props.getProperty("passwordRetrievalPage");
  }



  /**
   * Returns a textual representation of this Server.
   */

  public String toString(){
    return getLongName()+" ("+getWebsite()+")";
  }


}