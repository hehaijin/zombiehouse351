package zombiehouse.audio;

import javafx.scene.media.AudioClip;

/**
 * Play sound files with decreasing volume and panning according
 * to the sound source's relative location.
 * 
 * @author Maxwell Sanchez
 *
 */
public class DirectionalPlayer 
{
  /**
   * Plays an AudioClip which is panned and dampened based on the 
   * distance and relative position (provided angle) of the source.
   * 
   * @param clip AudioClip to play
   * @param angle Angle between player's "forward" and the sound source
   * @param distance Distance between player and the sound source
   */
  public static void playSound(AudioClip clip, double angle, double distance)
  {
    double oldBalance = clip.getBalance();
    double oldPan = clip.getPan();
    double oldVolume = clip.getVolume();
    
    clip.setBalance(Math.sin(angle * 3.1415926 / 180));
    if (distance < 1) distance = 1;
    clip.setVolume(1 / distance);
    clip.play();
      
    clip.setBalance(oldBalance);
    clip.setPan(oldPan);
    clip.setVolume(oldVolume);
  }
}
