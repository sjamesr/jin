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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Hashtable;
import free.util.BlockingQueue;
import free.util.PlatformUtils;
import free.util.IOUtilities;


/**
 * This is an AudioPlayer implementation which uses the javax.sound.sampled API
 * to play sounds. This API is only available since JDK1.3.
 */

public class JavaxSampledAudioPlayer implements Runnable, AudioPlayer{



  /**
   * The current thread playing the sound.
   */

  private Thread playerThread = null;
  
  
  
  /**
   * A BlockingQueue of AudioClips queued for playing.
   */

  private final BlockingQueue clipQueue = new BlockingQueue();



  /**
   * Returns whether we're running under Java 1.3 or later.
   */

  public boolean isSupported(){
    return PlatformUtils.isJavaBetterThan("1.3");
  }



  /**
   * Plays the given AudioClip.
   */

  public synchronized void play(AudioClip clip) throws java.io.IOException{
    // Lazily initialize player thread.
    if (playerThread == null){
      playerThread = new Thread(this, "JavaxSampledAudioPlayer");
      playerThread.setDaemon(true);
      playerThread.start();
    }

    clipQueue.push(clip);
  }



  /**
   * <code>Runnable</code> implementation. Plays the queued clips.
   */

  public void run(){
    SourceDataLine dataLine = null;
    while (true){
      try{
        AudioClip audioClip;
        try{
          audioClip = (AudioClip)clipQueue.pop();
        } catch (InterruptedException e){
          e.printStackTrace();
          return;
        }
        
        byte [] data = audioClip.getData();
        AudioFormat format = getFormatForPlaying(data);
        data = convertAudioData(data, format);
        
        if (dataLine == null){
          DataLine.Info info = new DataLine.Info(SourceDataLine.class, format);
          dataLine = (SourceDataLine)AudioSystem.getLine(info);
        }
        
        if (!dataLine.isOpen())
          dataLine.open(format);
        
        if (!format.matches(dataLine.getFormat())){
          dataLine.close();
          dataLine.open(format);
        }
        
        long startTime = System.currentTimeMillis();
        if (!dataLine.isRunning())
          dataLine.start();
        
        dataLine.write(data, 0, data.length);
        dataLine.drain();
        
        // Bugfix - drain() seems to return before the clip actually finished playing 
        try{
          double playTimeSec = (data.length/format.getFrameSize())/format.getFrameRate();
          long timeToSleep = (long)(playTimeSec*1000 - (System.currentTimeMillis() - startTime));
          
          if (timeToSleep > 0)
            Thread.sleep(timeToSleep);
          dataLine.close();
        } catch (InterruptedException e){e.printStackTrace();}
      } catch (IOException e){
        e.printStackTrace();
      } catch (UnsupportedAudioFileException e){
        e.printStackTrace();
      } catch (LineUnavailableException e){
        e.printStackTrace();
      } catch (IllegalArgumentException e){
        e.printStackTrace();
      }
    }
  }




  /**
   * Finds and returns the AudioFormat appropriate for playing the specified
   * audio data.
   */

  private static AudioFormat getFormatForPlaying(byte [] audioData)
      throws UnsupportedAudioFileException, IOException{
    AudioFormat format = AudioSystem.getAudioFileFormat(
        new ByteArrayInputStream(audioData)).getFormat();

    // At present, ALAW and ULAW encodings must be converted
    // to PCM_SIGNED before it can be played
    if (format.getEncoding() != AudioFormat.Encoding.PCM_SIGNED)
      return new AudioFormat(AudioFormat.Encoding.PCM_SIGNED,
          format.getSampleRate(), format.getSampleSizeInBits() * 2,
          format.getChannels(), format.getFrameSize() * 2,
          format.getFrameRate(), true);
    else
      return format;
  }



  /**
   * Converts the specified audio data to the specified format. 
   */

  private static byte [] convertAudioData(byte [] audioData, AudioFormat format)
      throws UnsupportedAudioFileException, IOException{
    AudioInputStream stream = AudioSystem.getAudioInputStream(new ByteArrayInputStream(
        audioData));

    if (format.matches(stream.getFormat()))
      return audioData;

    stream = AudioSystem.getAudioInputStream(format, stream);

    return IOUtilities.readToEnd(stream);
  }

}