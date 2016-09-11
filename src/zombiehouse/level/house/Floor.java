package zombiehouse.level.house;

import javafx.scene.paint.Color;
import zombiehouse.common.LevelVar;
import zombiehouse.level.zombie.*;

/**
 * @author Rob
 *
 * The 'most important' tile type, Floor
 * Is where the player and Zombies can move
 * 
 * Floors have an added member variable:
 * isEmpty - denotes if a player or zombie was spawned in this tile
 *           used because player/master/exit are placed after the fact and need to know
 *           what space is still available
 *           
 * Floor overwrites isFloor, isEmpty() and isUsed() from tile
 */
public class Floor extends Tile
{
  private boolean isEmpty = true;
  private static final Color[] COLOR_ARRAY = { Color.BROWN, Color.LIGHTGREY, Color.GRAY, Color.DARKRED };
  
  /**
   * Simple constructor
   * @param xCor the x-coordinate (index) on LevelVar.house
   * @param yCor the y-coordinate (index) on LevelVar.house
   * @param zone the zone ID for this tile
   * @param allowSpawn since Zombies can only start in full-size rooms, need a flag to allow
   */
  public Floor(int xCor, int yCor, int zone, boolean allowSpawn)
  {
    super(xCor, yCor, zone);
    
    if(allowSpawn && LevelVar.SPAWN_MONSTERS)
    {
      if(LevelVar.rand.nextDouble() < 0.01 + (LevelVar.levelNum * LevelVar.spawnModifier) )
      {
        if(LevelVar.rand.nextBoolean()) 
        { 
          LevelVar.zombieCollection.add(new LineWalkZombie(LevelVar.rand.nextDouble() * 360, xCor + 0.5, yCor + 0.5, this, Level.nextZombie++));
        }
        else
        { 
          LevelVar.zombieCollection.add(new RandomWalkZombie(LevelVar.rand.nextDouble() * 360, xCor + 0.5, yCor + 0.5, this, Level.nextZombie++));
        }
        isEmpty = false;
      }
    }
  }
  
  /**
   * getChar() only used by printHouse()
   */
  public char getChar() { return '.'; }
  
  /**
   * getColor only used by 2d House Animation
   */
  public Color getColor()
  { 
    if(LevelVar.WITH_SIGHT && !hasBeenSeen) { return Color.BLACK; }
    return COLOR_ARRAY[zone];
  }
  
  public boolean isFloor() { return true; }
  
  public boolean isEmpty() { return isEmpty; }
  
  public void isUsed() { isEmpty = false; }
}
