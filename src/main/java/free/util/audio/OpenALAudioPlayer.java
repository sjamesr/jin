package free.util.audio;

import java.io.IOException;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import org.lwjgl.BufferUtils;
import org.lwjgl.LWJGLException;
import org.lwjgl.openal.AL;
import org.lwjgl.openal.AL10;
import org.lwjgl.util.WaveData;

/**
 * An audio player that uses OpenAL to play audio clips.
 */
public class OpenALAudioPlayer implements AudioPlayer {
  private boolean supported;
  IntBuffer buffer;
  IntBuffer source;
  FloatBuffer sourcePos;
  /** Velocity of the source sound. */
  FloatBuffer sourceVel;
  /** Position of the listener. */
  FloatBuffer listenerPos;
  FloatBuffer listenerVel;
  /**
   * Orientation of the listener. (first 3 elements are "at", second 3 are "up")
   */
  FloatBuffer listenerOri = (FloatBuffer) BufferUtils.createFloatBuffer(6)
      .put(new float[] { 0.0f, 0.0f, -1.0f, 0.0f, 1.0f, 0.0f }).rewind();

  public OpenALAudioPlayer() {
    try {
      AL.create();
      buffer = BufferUtils.createIntBuffer(1);
      source = BufferUtils.createIntBuffer(1);
      sourcePos = (FloatBuffer) BufferUtils.createFloatBuffer(3)
          .put(new float[] { 0.0f, 0.0f, 0.0f }).rewind();
      sourceVel = (FloatBuffer) BufferUtils.createFloatBuffer(3)
          .put(new float[] { 0.0f, 0.0f, 0.0f }).rewind();
      listenerPos = (FloatBuffer) BufferUtils.createFloatBuffer(3)
          .put(new float[] { 0.0f, 0.0f, 0.0f }).rewind();
      listenerVel = (FloatBuffer) BufferUtils.createFloatBuffer(3)
          .put(new float[] { 0.0f, 0.0f, 0.0f }).rewind();
      supported = true;
    } catch (LWJGLException e) {
      supported = false;
    }
  }

  @Override
  public boolean isSupported() {
    return supported;
  }

  @Override
  public void play(AudioClip clip) throws IOException {
    AL10.alGenBuffers(buffer);
    WaveData data = WaveData.create(clip.getData());
    AL10.alBufferData(buffer.get(0), data.format, data.data, data.samplerate);
    data.dispose();
    AL10.alGenSources(source);
    if (AL10.alGetError() != AL10.AL_NO_ERROR)
      return;
    AL10.alSourcei(source.get(0), AL10.AL_BUFFER, buffer.get(0));
    AL10.alSourcef(source.get(0), AL10.AL_PITCH, 1.0f);
    AL10.alSourcef(source.get(0), AL10.AL_GAIN, 1.0f);
    AL10.alSource(source.get(0), AL10.AL_POSITION, sourcePos);
    AL10.alSource(source.get(0), AL10.AL_VELOCITY, sourceVel);
    if (AL10.alGetError() != AL10.AL_NO_ERROR)
      return;
    AL10.alSourcePlay(source.get(0));
  }
}
