/**
 * Jin - a chess client for internet chess servers.
 * More information is available at http://www.hightemplar.com/jin/.
 * Copyright (C) 2002 Alexander Maryanovsky.
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

package free.util.audio;

import sun.audio.*;
import java.io.*;
import java.net.URL;
import java.util.Vector;
import free.util.IOUtilities;


/**
 * This class allows you to play sounds in an application in JDK 1.1 - it uses
 * sun.audio classes and is thus not platform independent, but it simply won't
 * do anything if it fails. The "au" format is most likely to be supported, but
 * others may work too.
 */

public class AudioClip{


  /**
   * The AudioPlayer that succeeded in playing sounds. This one, if not null is
   * tried first.
   */

  private static AudioPlayer successfulPlayer = null;



  /**
   * A list of classnames of AudioPlayer implementations which we will try to
   * see if they succeed.
   */

  private static final String [] playerClassnames;



  /**
   * Loads the AudioPlayer classnames.
   */

  static{
    String [] classnames = new String[0];
    try{
      try{
        BufferedReader reader = new BufferedReader(new InputStreamReader(AudioClip.class.getResourceAsStream("players.txt")));

        Vector tempVec = new Vector();
        String line;
        while ((line = reader.readLine())!=null)
          tempVec.addElement(line);

        classnames = new String[tempVec.size()];
        for (int i=0;i<classnames.length;i++)
          classnames[i] = (String)tempVec.elementAt(i);
      } catch (IOException e){
          e.printStackTrace();
          classnames = new String[0];
        }

    } catch (Exception e){
        e.printStackTrace();
      }
    playerClassnames = classnames;
  }



  /**
   * The byte array containing the sound in some format.
   */

  private final byte [] sound;


  
  /**
   * Creates a new AudioClip from the given byte array. This constructor does
   * not block.
   */
  /*
  public AudioClip(byte [] sound){
    this.sound = sound;
  }
  */



  /**
   * Creates a new AudioClip from the given InputStream. This constructor blocks
   * until all the sound data is read from the InputStream.
   */

  public AudioClip(InputStream in) throws IOException{
    ByteArrayOutputStream buf = new ByteArrayOutputStream();
    IOUtilities.pump(in, buf);
    in.close();

    this.sound = buf.toByteArray();
  }




  /**
   * Creates a new AudioClip from the given URL. This constructor blocks until
   * all the sound data is read from the URL. 
   */

  public AudioClip(URL url) throws IOException{
    this(url.openStream());
  } 



  /**
   * Creates a new AudioClip from the given file. This constructor blocks until
   * all the sound data is read from the file.
   */

  public AudioClip(File file) throws IOException{
    this(new FileInputStream(file));
  }





  /**
   * Attempts to play this AudioClip, the method returns immediately and never
   * throws exceptions. If playing fails, it fails silently.
   */

  public synchronized void play(){
    try{
      if (successfulPlayer!=null){
        successfulPlayer.play(this);
        return;
      }

      for (int i=0;i<playerClassnames.length;i++){
        String classname = playerClassnames[i];
        try{
          Class playerClass = Class.forName(classname);
          AudioPlayer player = (AudioPlayer)playerClass.newInstance();
          player.play(this);
          successfulPlayer = player;
        } catch (Exception e){
            continue;
          }
        break;
      }
    } catch (Exception e){} // Silently ignore any exceptions
  }




  /**
   * Returns the audio data.
   */

  public byte [] getData(){
    return sound;
  }

  

}
