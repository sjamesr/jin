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

import java.util.Hashtable;
import free.util.MemoryFile;


/**
 * Contains the details of an account on a chess server.
 */

public class User{



  /**
   * The server of this account.
   */

  private final Server server;



  /**
   * The username of this account.
   */

  private String username;



  /**
   * The preferences of the user.
   */

  private final Preferences prefs;



  /**
   * Maps filenames to <code>MemoryFile</code> objects.
   */

  private final Hashtable files;
  
  
  
  /**
   * True when this user has been modified but hasn't been saved yet.
   */
   
  private boolean isDirty;



  /**
   * Creates a new <code>User</code> object. Before creating a <code>User</code>
   * object with a user supplied username, make sure that the username is not
   * the guest username (via the username policy of the server).
   *
   * @param server The server of the account.
   * @param username The username of the account.
   * @param prefs The user's preferences.
   * @param files The user's files. Maps filenames to <code>MemoryFile</code>
   * objects.
   */

  public User(Server server, String username, Preferences prefs, Hashtable files){
    this.server = server;
    this.username = username;
    this.prefs = prefs;
    this.files = files;
  }



  /**
   * Creates a new <code>User</code> object with the specified server and
   * username. The user initially has no preferences or files.
   */

  public User(Server server, String username){
    this(server, username, Preferences.createNew(), new Hashtable());
  }



  /**
   * Returns the server on which this account is registered.
   */

  public Server getServer(){
    return server;
  }



  /**
   * Returns the username of this account.
   */

  public String getUsername(){
    return username;
  }



  /**
   * Returns the preferences of this account.
   */

  public Preferences getPrefs(){
    return prefs;
  }



  /**
   * Returns the files hashtable. This is used by code that stores the memory
   * files.
   */

  Hashtable getFilesMap(){
    return files;
  }



  /**
   * Returns the file with the specified name, or <code>null</code> if no file
   * with the specified name exists. Filenames are case sensitive.
   */

  public MemoryFile getFile(String filename){
    return (MemoryFile)files.get(filename);
  }



  /**
   * Sets the file with the specified name. If a file with the specified name
   * already exists, it is deleted. The specified file may be <code>null</code>
   * if you want to call this method just for the side effect of removing the
   * existing file.
   */

  public void setFile(String filename, MemoryFile file){
    if (file == null)
      files.remove(filename);
    else
      files.put(filename, file);
  }




  /**
   * Returns whether this <code>User</code> object represents the guest account.
   */

  public boolean isGuest(){
    return this == server.getGuest();
  }



  /**
   * Returns the preferred connection details of this user.
   */

  public ConnectionDetails getPreferredConnDetails(){
    String password = prefs.getString("login.password", "");
    boolean savePassword = prefs.getBool("login.savePassword", false);
    String hostname = prefs.getString("login.hostname", server.getDefaultHost());
    int [] ports = prefs.getIntList("login.ports", server.getPorts());

    if (isGuest())
      return ConnectionDetails.createGuest(getServer(), username, hostname, ports);
    else
      return ConnectionDetails.create(getServer(), this, username, password, savePassword, hostname, ports);
  }



  /**
   * Sets the specified user's connection details to the specified ones.
   */

  void setPreferredConnDetails(ConnectionDetails details){
    if (details.isGuest() != isGuest())
      throw new IllegalArgumentException("isGuest property mismatch");
    if (details.getServer() != getServer())
      throw new IllegalArgumentException("server property mismatch");
    if (details.getUser() != this)
      throw new IllegalArgumentException("user property mismatch");

    prefs.setString("login.hostname", details.getHost());
    prefs.setIntList("login.ports", details.getPorts());

    if (!details.isGuest()){
      boolean savePassword = details.isSavePassword();
      prefs.setBool("login.savePassword", savePassword);
      prefs.setString("login.password", savePassword ? details.getPassword() : "");
    }
  }
  
  
  
  /**
   * Marks this user as dirty, meaning that it has been modified, but hasn't
   * been saved yet.
   */
   
  public void markDirty(){
    isDirty = true;
  }
  
  
  
  /**
   * Returns whether this user is dirty.
   */
   
  public boolean isDirty(){
    return isDirty; 
  }
  
  
  
  /**
   * Returns a description of the user.
   */
   
  public String toString(){
    return getUsername() + "@" + getServer().getShortName();
  }
  


}
