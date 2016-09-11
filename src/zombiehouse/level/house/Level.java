package zombiehouse.level.house;

import java.util.ArrayList;
import java.util.Random;
import zombiehouse.level.zombie.*;
import zombiehouse.common.LevelVar;
import zombiehouse.common.Player;

/**
 * @author Rob
 *
 * Level is the master class for house generation
 * A single instance of Level should be created in the application and used for generation
 * 
 * Variables that are needed by multiple classes are kept in LevelVar
 * 
 * private final var:
 * MIN_HOUSE_SIZE - is the minimum number of MazeTiles per zone
 *                  NOTE: at 5, you are guaranteed to meet the Room number specification
 * 
 * package private vars:
 * mazeTiles_PerZone - is the current number of MazeTiles per zone (gets larger for each level)
 * houseWidth, houseHeight - is the true width and height of the house (including the outside wall)
 * nextZombie - is the ID number to assign the next Zombie
 * 
 * private vars:
 * firstGen - a boolean flag denoting if this is the very first level
 * rSeed - stores the random seed that was used to generate the last level - used if player dies
 * pG is the current ProGen - isn't currently used beyond its constructor and set-up (but could be)
 */
public class Level
{
  private static final int MIN_HOUSE_SIZE = 5;
  
  static int mazeTilesXPerZone = MIN_HOUSE_SIZE + LevelVar.levelNum; // getting bigger house each level
  static int mazeTilesYPerZone = MIN_HOUSE_SIZE + LevelVar.levelNum; // also only assumes 4 quartered zones
  
  static int houseWidth = mazeTilesXPerZone * 2 * 4 + 1;
  static int houseHeight = mazeTilesXPerZone * 2 * 4 + 1;
  static int nextZombie = 0;
  
  private static boolean firstGen;
  
  private static long rSeed;
  
  private static ProGen pG;
  
  /**
   * Level constructor
   * 
   * Does very little - initializes the Random obj that all generation will use
   *                    and sets firstGen flag = true
   */
  public Level()
  {
    LevelVar.rand = new Random();
    firstGen = true;
  }
  
  /**
   * nextLevel() should be called when the player reaches the exit
   * increases difficulty and player stats
   * re-initializes all variables needed for generation and sets new rSeed
   * and finishes by creating new ProGen
   */
  public void nextLevel()
  {
    if(firstGen) { firstGen = false; }
    else 
    { 
      upDificulty();
      playerLevelUp();
    }
    nextZombie = 0;
    houseWidth = mazeTilesXPerZone * 2 * 4 + 1;
    houseHeight = mazeTilesXPerZone * 2 * 4 + 1;
    LevelVar.house = new Tile[houseWidth][houseHeight];
    LevelVar.zombieCollection = new ArrayList<Zombie>();
    playerLevelUp();
    rSeed = LevelVar.rand.nextLong();
    LevelVar.rand = new Random(rSeed);
    pG = new ProGen();
    if(LevelVar.LEVEL_DEBUG_TEXT) { printHouse(); }
  }
  
  /**
   * restartLevel() should be called when the player is killed by a zombie
   * resets the appropriate variables, re-seeds random
   * then creates a new ProGen
   */
  public void restartLevel()
  {
    nextZombie = 0;
    LevelVar.house = new Tile[houseWidth][houseHeight];
    LevelVar.zombieCollection = new ArrayList<Zombie>();
    LevelVar.rand.setSeed(rSeed);
    pG = new ProGen();
    if(LevelVar.LEVEL_DEBUG_TEXT) { printHouse(); }
  }
  
  /**
   * raises the difficulty for each following level
   * (pillars can only spawn in preset areas, and are an obsical to movement and sight)
   * and the house gets a bit bigger between levels
   * levelNum also allows for bigger room sizes:
   *    - starts at (2-3) x (2-3)
   *    - the max increases every other level
   */
  private void upDificulty()
  {
    LevelVar.levelNum++;
    LevelVar.pillarSpawnChance += 0.2;
    mazeTilesXPerZone = MIN_HOUSE_SIZE + LevelVar.levelNum;
    mazeTilesYPerZone = MIN_HOUSE_SIZE + LevelVar.levelNum;
    LevelVar.zombieSpeed *= 1.25;
  }
  
  /**
   * a few minor 'upgrades' to the player for each level beat
   */
  private void playerLevelUp()
  {
    Player.playerSpeed += 0.25;
    Player.stamina += 1.0;
    Player.staminaRegen += 0.2;
  }
  
  /**
   * prints the layout of the level with basic characters to terminal
   * only used with debugging flag
   */
  private void printHouse()
  {
    StringBuilder print = new StringBuilder();
    for( int i = 0; i < houseWidth; i++ )
    {
      for( int j = 0; j < houseHeight; j++)
      {
        print.append( LevelVar.house[j][i].getChar() );
      }
      print.append("\n");
    }
    System.out.println(print.toString());
  }
  
  /**
   * used only in the 2d tester class, when sight range is turned on
   * naive approach of just quarrying all tiles at each update
   */
  public void checkSight()
  {
    for(int i = 0; i < houseWidth; i++)
    {
      for(int j = 0; j < houseHeight; j++)
      {
        LevelVar.house[i][j].isSeen();
      }
    }
  }
  
  public void nextGenStep()
  {
    pG.nextStep();
  }
  
  public void fullGen()
  {
    pG.shortCutGen();
  }
}
