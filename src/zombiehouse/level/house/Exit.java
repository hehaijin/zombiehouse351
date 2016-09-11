package zombiehouse.level.house;

import javafx.scene.paint.Color;
import zombiehouse.common.LevelVar;

/**
 * @author Rob
 *
 * The 'special' tile that allows for beating levels
 * only will spawn these on the outside wall and only 2 adjacent tiles per level
 * 
 * nothing internally 'special' about Exit (only has the Tile values)
 */
public class Exit extends Tile
{
  /**
   * Simple constructor
   * @param xCor the x-coordinate (index) on LevelVar.house
   * @param yCor the y-coordinate (index) on LevelVar.house
   * @param zone the zone ID for this tile
   */
  public Exit(int xCor, int yCor, int zone) { super(xCor, yCor, zone); }
  
  /**
   * getChar() only used by printHouse()
   */
  public char getChar() { return '!'; }
  
  /**
   * getColor only used by 2d House Animation
   */
  public Color getColor()
  { 
    if(LevelVar.WITH_SIGHT && !hasBeenSeen) { return Color.BLACK; }
    return Color.YELLOW; 
  }
}
