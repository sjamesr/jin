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

import java.io.IOException;

import javax.sound.sampled.*;


/**
 * A subclass of <code>JavaxSampledAudioPlayer</code> which (unlike
 * <code>NonBlockingJavaxSampledAudioPlayer</code> opens a single data line once
 * and writes audio data into it without ever closing it. This is needed because
 * closing the data line breaks on some Windows and OS X machines.  
 */

public class BlockingJavaxSampledAudioPlayer extends JavaxSampledAudioPlayer{


  /**
   * <code>Runnable</code> implementation. Plays the queued clips.
   */

  public void run(){
    SourceDataLine dataLine = null;
    while (true){
      try{
        AudioClip audioClip = (AudioClip)clipQueue.pop();
        
        byte [] data = audioClip.getData();
        AudioFormat format = getFormatForPlaying(data);
        data = convertAudioData(data, format);
        
        if (dataLine == null){
          DataLine.Info info = new DataLine.Info(SourceDataLine.class, format);
          dataLine = (SourceDataLine)AudioSystem.getLine(info);
        }
        
        if (!dataLine.isOpen())
          dataLine.open(format);
        
        if (!dataLine.isRunning())
          dataLine.start();
        
        dataLine.write(data, 0, data.length);
      } catch (IOException e){
        e.printStackTrace();
      } catch (UnsupportedAudioFileException e){
        e.printStackTrace();
      } catch (LineUnavailableException e){
        e.printStackTrace();
      } catch (IllegalArgumentException e){
        e.printStackTrace();
      } catch (Throwable t){
        t.printStackTrace();
      }
    }
    
  }

}