/**
 * The utillib library.
 * More information is available at http://www.jinchess.com/.
 * Copyright (C) 2002 Alexander Maryanovsky.
 * All rights reserved.
 *
 * The utillib library is free software; you can redistribute
 * it and/or modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 2 of the
 * License, or (at your option) any later version.
 *
 * The utillib library is distributed in the hope that it will
 * be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser
 * General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with utillib library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package free.util.audio;

import javax.sound.sampled.*;
import java.util.Hashtable;
import java.net.URL;
import java.util.Vector;


/**
 * This is an AudioPlayer implementation which uses the javax.sound.sampled API
 * to play sounds. This API is only available since JDK1.3.
 */

public class JavaxSampledAudioPlayer implements AudioPlayer, LineListener{



  /**
   * The maximum Clips we're willing to queue.
   */

  private static final int MAX_QUEUE_SIZE = 2;



  /**
   * Maps free.util.audio.AudioClip instances to javax.sound.sampled.Clip
   * instances.
   */

  private final Hashtable clips = new Hashtable();



  /**
   * A Vector of queued Clips.
   */

  private final Vector clipQueue = new Vector(MAX_QUEUE_SIZE);



  /**
   * The currently playing clip.
   */

  private Clip playingClip = null;





  /**
   * Returns true if the value of "java.version" is 1.3 or later.
   */

  public boolean isSupported(){
    return System.getProperty("java.version").compareTo("1.3") >= 0;
  }




  /**
   * Plays the given AudioClip.
   */

  public void play(AudioClip clip) throws java.io.IOException{
    Clip newClip = (Clip)clips.get(clip);
    if (newClip == null){
      try{
        newClip = createClip(clip.getData());
      } catch (LineUnavailableException e){
          return; // Will try again later.
        }
        catch (UnsupportedAudioFileException e){
          throw new RuntimeException("Unable to load clip due to: "+e.getMessage());
        }
      clips.put(clip, newClip);
    }

    synchronized(this){
      if (playingClip == null) // Nothing is playing.
        startPlaying(newClip);
      else
        clipQueue.addElement(newClip);
    }
  }




  /**
   * Actually starts playing the given Clip.
   */

  private void startPlaying(Clip clip){
    playingClip = clip;
    playingClip.addLineListener(this);
    playingClip.setFramePosition(0);
    playingClip.start();
  }




  /**
   * LineListener implementation. Starts any queued clips.
   */

  public void update(LineEvent evt){
    if (evt.getType() == LineEvent.Type.STOP){
      synchronized(this){
        playingClip.removeLineListener(this);
        if (!clipQueue.isEmpty()){
          Clip clip = (Clip)clipQueue.firstElement();
          clipQueue.removeElementAt(0);
          startPlaying(clip);
        }
        else
          playingClip = null;
      }
    }
  }



  /**
   * Creates and loads a Clip from the given audio data.
   */

  private static Clip createClip(byte [] data) throws java.io.IOException, LineUnavailableException, UnsupportedAudioFileException{
    AudioInputStream stream = AudioSystem.getAudioInputStream(new java.io.ByteArrayInputStream(data));

    // At present, ALAW and ULAW encodings must be converted
    // to PCM_SIGNED before it can be played
    AudioFormat format = stream.getFormat();
    if (format.getEncoding() != AudioFormat.Encoding.PCM_SIGNED) {
      format = new AudioFormat(
              AudioFormat.Encoding.PCM_SIGNED,
              format.getSampleRate(),
              format.getSampleSizeInBits()*2,
              format.getChannels(),
              format.getFrameSize()*2,
              format.getFrameRate(),
              true);        // big endian
      stream = AudioSystem.getAudioInputStream(format, stream);
    }

    // Create the clip
    DataLine.Info info = new DataLine.Info(Clip.class, stream.getFormat(), ((int)stream.getFrameLength()*format.getFrameSize()));
    Clip clip = (Clip) AudioSystem.getLine(info);

    clip.open(stream);

    return clip;
  }


}

