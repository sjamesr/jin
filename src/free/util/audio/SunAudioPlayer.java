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
import java.io.ByteArrayInputStream;
import java.io.IOException;


/**
 * An implementation of AudioPlayer which uses the sun.audio classes to play
 * sounds. The sounds are played in a separate thread because there seems to be
 * a bug with sound-playing on win98 which causes the thread calling
 * <code>sun.audio.AudioPlayer.player.start</code> to get stuck waiting on some
 * lock.
 */

public class SunAudioPlayer implements AudioPlayer, Runnable{



  /**
   * The time when the last sound finished playing.
   */

  private static long lastFinishedSoundTime = 0;



  /**
   * The current thread playing the sound.
   */

  private static volatile Thread playerThread = null;




  /**
   * True when the thread is ready to play the clip, false if it's currently
   * playing one.
   */

  private static volatile boolean ready = false;




  /**
   * The audio clip to play next;
   */

  private static AudioClip clipToPlay = null;




  /**
   * Plays the given AudioClip, throws an IOException if unsuccessful. Due to
   * bugs in mixing sound in sun.audio.AudioPlayer, this method tries to estimate
   * the amount of time the clip will take to play (which it can do pretty accurately
   * knowing that the sample rate must be 8000hz) and ignores subsequent
   * calls if they occur within the interval needed for the sound to finish playing.
   */

  public void play(AudioClip clip){
    synchronized(SunAudioPlayer.class){ // It doesn't mix well several sounds playing simultaneously.
      if (lastFinishedSoundTime+200>=System.currentTimeMillis()){ // Give a some time on top.
        System.err.println("Sound already playing, ignoring play request.");
        return; // Ignore.
      }

      if ((playerThread!=null)&&!ready){ // It should be null, time+200ms is up.
        playerThread.stop();
        playerThread = null;
      }

      this.clipToPlay = clip;

      if (playerThread==null){
        playerThread = new Thread(this);
        playerThread.setDaemon(true);
        playerThread.start();
      }
      else{
        SunAudioPlayer.class.notify();
      }
    }
  }




  /**
   * Plays the current <code>clipToPlay</code> when notified.
   */

  public void run(){
    while (true){
      synchronized(SunAudioPlayer.class){
        if (clipToPlay==null)
          try{
            SunAudioPlayer.class.wait();
          } catch (InterruptedException e){
              e.printStackTrace();
            }
      }
      AudioClip clip = clipToPlay;
      clipToPlay = null;
      ready = false;

      byte [] data = clip.getData();
      int timeToPlay = data.length/8;
      lastFinishedSoundTime = System.currentTimeMillis()+timeToPlay;
      try{
        NativeAudioStream audioStream = new NativeAudioStream(new ByteArrayInputStream(data));
        sun.audio.AudioPlayer.player.start(audioStream);
      } catch (IOException e){
          return;
        }

      ready = true;
    }
  }


}
