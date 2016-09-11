package zombiehouse.level.house;

import javafx.scene.paint.Color;
import zombiehouse.common.LevelVar;

/**
 * @author Rob
 *
 * The most basic building block for house generation
 * all walls are the same and prohibit movement
 */
public class Wall extends Tile
{
  /**
   * Simple constructor
   * @param xCor the x-coordinate (index) on LevelVar.house
   * @param yCor the y-coordinate (index) on LevelVar.house
   * @param zone the zone ID for this tile
   */
  public Wall(int xCor, int yCor, int zone) { super(xCor, yCor, zone); }
  
  /**
   * getChar() only used by printHouse()
   */
  public char getChar() { return 'x'; }
  
  /**
   * getColor only used by 2d House Animation
   */
  public Color getColor() 
  { 
    if(LevelVar.WITH_SIGHT && !hasBeenSeen) { return Color.BLACK; }
    return Color.DARKGRAY;
  }
}
