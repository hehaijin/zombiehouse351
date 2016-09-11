package zombiehouse.common;

public enum Direction 
{
  NORTH ( 0,-1, 0),
  EAST  ( 1, 0, 1),
  SOUTH ( 0, 1, 2),
  WEST  (-1, 0, 3);
  
  public static final int TOTAL_DIR = 4;
  
  public final int dX, dY;
  private final int arrayVal;
  
  Direction(int dX, int dY, int arrayVal)
  {
    this.dX = dX;
    this.dY = dY;
    this.arrayVal = arrayVal;
  }
  
  public Direction getOppositeDir()
  {
    return Direction.values()[(arrayVal + 2) % 4];
  }
}
