package zombiehouse.level.house;

import java.util.ArrayList;
import javafx.scene.paint.Color;
import zombiehouse.common.LevelVar;
import zombiehouse.common.Player;

/**
 * @author Rob
 *
 * Tile is the super-class for all the 'space' elements of a level (Floor, Wall, Exit)
 * Contains all the common elements of Floor, Wall, Exit
 * In theory Tile should not be initialized
 * 
 * 
 */
public class Tile
{
  public boolean visited = false; // only concerned if visited was used in generation
  public int xCor, yCor;
  public int zone;
  public ArrayList<Tile> neighbors = new ArrayList<>();
  public Tile ancestor;
  public int cost;
  public boolean toRemove = false;
  public boolean hasBeenSeen = false;
	  
  /**
   * Simple constructor
   * @param xCor the x-coordinate (index) on LevelVar.house
   * @param yCor the y-coordinate (index) on LevelVar.house
   * @param zone the zone ID for this tile
   */
  public Tile(int xCor, int yCor, int zone)
  {
    this.xCor = xCor;
    this.yCor = yCor;
    this.zone = zone;
  }
	
  
  public void setNeighbors(Tile[][] house)
  {
    if(xCor+1 < house.length)     { if( house[xCor+1][yCor].isFloor() ) { neighbors.add(house[xCor+1][yCor]); } }
    if(yCor+1 < house[0].length)  { if( house[xCor][yCor+1].isFloor() ) { neighbors.add(house[xCor][yCor+1]); } }
    if(yCor-1 >= 0)               { if( house[xCor][yCor-1].isFloor() ) { neighbors.add(house[xCor][yCor-1]); } }
    if(xCor-1 >= 0)               { if( house[xCor-1][yCor].isFloor() ) { neighbors.add(house[xCor-1][yCor]); } }
  }
  
  public void addNeighbor(Tile nextTile) { neighbors.add(nextTile); }
  
  public ArrayList<Tile> getNeighbors() { return neighbors; }
	  
  public void setAncestor(Tile parentTile) { ancestor = parentTile; }
	  
  public void setVisited(boolean value) { visited = value; }
	  
  public void setCost(int cost) { this.cost = cost; }
  
  public void setToRemove(boolean value) { this.toRemove = value; }
	  
  public double getXCor() { return xCor; }
	  
  public double getYCor() { return yCor; }
  
  public void isUsed() {}
  
  public char getChar() { return 'f'; }
  
  public Color getColor()
  { 
    if(LevelVar.WITH_SIGHT && !hasBeenSeen) { return Color.BLACK; }
    return Color.WHITE; 
  }
  
  public boolean isEmpty() { return false; }
  
  public boolean isFloor() { return false; }
  
  public void isSeen()
  {
    if(hasBeenSeen) { return; }
    double distFromPlayer = Math.abs(Player.xPosition - xCor) + Math.abs(Player.yPosition - yCor);
    if(distFromPlayer <= Player.playerSightRange) { hasBeenSeen = true; }
  }
  
}