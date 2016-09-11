/**
 *Zombie class written for ZombieHouse CS351 project that contains
 *the pathfinding and behavior algorithms for the Zombie objects in 
 *the game.
 *@Author Stephen Sagartz
 *@version 1.0
 *@since 2016-03-05 
 */

package zombiehouse.level.zombie;

import java.util.ArrayList;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Comparator;
import java.util.LinkedList;
import zombiehouse.level.house.*;
import zombiehouse.common.*;
import zombiehouse.graphics.Zombie3D;

/**
 *Zombie class that contains methods inherited by the sub-classes of Zombie
 *as well as all Zombie variables. 
 */
public class Zombie
{
  /**
   * the number of Tiles a Zombie can traverse over 1 second
   */
  private double zombie_Speed = 0.5;
  /**
   * the amount of time between Zombie heading updates
   */
  private static long zombie_Decision_Rate = 2000;
  /**
   * the number of Tiles away that a Zombie can smell
   */
  private int zombie_Smell = 15;
  /**
   * whether or not a Zombie has scent of the Player
   */
  private boolean canSmell = false;
  /**
   * whether or not a Zombie has collided with an Object
   */
  private boolean collided = false;
  /**
   * this Zombie's ID number
   */
  public int zombieID;
  /**
   * array of Tiles that lead to the Player
   */
  public ArrayList<Tile> path = new ArrayList<>();
  /**
   * a queue to hold Tiles that search for the Player for scent detection
   */
  public Queue<Tile> bfsQueue = new LinkedList<>();
  /**
   * a priority queue that holds Tiles examined while finding Zombie's path to
   * Player
   */
  public PriorityQueue<Tile> searchQueue = new PriorityQueue<>(25,
      new Comparator<Tile>() {

        public int compare(Tile one, Tile two)
        {
          if (one.cost > two.cost)
            return 1;
          if (one.cost < two.cost)
            return -1;
          return 0;
        }
      });
  /**
   * the direction the Zombie will head in degrees
   */
  public double heading;
  /**
   * the Zombie's current X coordinate in the ZombieHouse
   */
  public double positionX;
  /**
   * the Zombie's current Y coordinate in the ZombieHouse
   */
  public double positionY;
  /**
   * the Tile the Zombie is currently in inside the ZombieHouse
   */
  private Tile curTile;

  /**
   * The Zombie3D that represents this zombie in a 3D graphical world
   */
  public Zombie3D zombie3D;

  /**
   * Constructs a Zombie object with the specified heading, X coordinate position,
   * Y coordinate position, and the Tile it is in, preferably as given by its
   * X and Y coordinates
   */
  public Zombie(double heading, double positionX, double positionY,
      Tile curTile, int id) {
    this.heading = heading;
    this.positionX = positionX;
    this.positionY = positionY;
    this.curTile = curTile;
    this.zombieID = id;
    if(LevelVar.zombie3D) { zombie3D = new Zombie3D(); }
  }

  /**
   * @return the Zombie class' zombie_Smell
   */
  public int getZombieSmell()
  {
  return this.zombie_Smell;
  }
  
  /**
   * @return the Zombie class' zombie_Decision_Rate
   */
  public static long getDecisionRate()
  {
    return zombie_Decision_Rate;
  }

  /**
   * Sets this Zombie object's collided value to value
   */
  public void setCollided(boolean value)
  {
  //System.out.println("Set Collided to " + value);
    this.collided = value;
  }

  /**
   * @return this Zombie's collided value
   */
  public boolean getCollide()
  {
    return this.collided;
  }

  /**
   * Sets this Zombie's canSmell value to value
   */
  public void setSmell(boolean value)
  {
    this.canSmell = value;
  }

  /**
   * @return this Zombie's canSmell value
   */
  public boolean getSmell()
  {
    return this.canSmell;
  }
  
  /**
   * @return this Zombie's curTile parameter
   */
  public Tile getPosition()
  {
    return curTile;
  }

  /**
   * Sets the Zombie's Tile parameter to tile
   */
  public void setPosition(Tile tile)
  {
    this.curTile = tile;
  }

  /**
   * @return this Zombie's heading parameter
   */
  public double getHeading()
  {
    return this.heading;
  }

  /**
   * Sets this Zombie's heading parameter to heading
   */
  public void setHeading(double heading)
  {
    this.heading = heading;
  }

  /**
   * Sets this Zombie's X coordinate to posX
   */
  public void setPositionX(double posX)
  {
    this.positionX = posX;
  }

  /**
   * Sets this Zombie's Y coordinate to posY
   */
  public void setPositionY(double posY)
  {
    this.positionY = posY;
  }
  
  /**
   * round method borrowed from Max's MainApplication class
   */
  private int round(double toRound)
  {
    if (toRound - ((int)toRound) < 0.5)
    {
      return (int)toRound;
    }
    else
    {
      return (int)toRound + 1;
    }
  }

  /**
   * Sets the X and Y coordinates of this Zombie to the position
   * altered by a factor of zombie_Speed and by the heading of the Zombie
   * assuming the Zombie's collided value is false, otherwise, it will not
   * change its coordinate or curTile parameters.
   */
  public void move()
  {
    if (!this.collided) 
    {
      double moveX;
      double moveY;
      double step = (double)1/60;
      if(this instanceof MasterZombie)
      {
        moveX = (Math.cos(Math.toRadians(this.heading)) * (this.zombie_Speed + LevelVar.levelNum*0.125)) * step;
        moveY = (Math.sin(Math.toRadians(this.heading)) * (this.zombie_Speed + LevelVar.levelNum*0.125)) * step;
      }
      moveX = (Math.cos(Math.toRadians(this.heading)) * this.zombie_Speed) * step;
      moveY = (Math.sin(Math.toRadians(this.heading)) * this.zombie_Speed) * step;
      if(this.positionX > 0 && this.positionX <= LevelVar.house[0].length && this.positionY > 0 && this.positionY <= LevelVar.house.length)
      {
    	this.positionX += moveX;
    	this.positionY += moveY;
    	this.curTile = LevelVar.house[(int) this.positionX][(int) this.positionY];
      }
      this.setCollided(this.collide());
      if(this.getCollide())
      {
        while (!(LevelVar.house[round(this.positionX)][round(this.positionY)] instanceof Tile))
        {
          if (this.positionX < 5)
          {
            this.positionX += 1; 
          }
          else
          {
            this.positionX -= 1;
          }
        }
        this.setCollided(false);
      }
    }
  }

  /**
   * Calculates whether the Zombie has collided with an object
   * and sets the Zombie's collided value accordingly
   * @return true if the Zombie has collided and false if the Zombie has not
   */
  public boolean collide()
  {
    for(Zombie z : LevelVar.zombieCollection)
    { 
      if(z.positionX != this.positionX && z.positionY != this.positionY)
      {
        double diffX = (z.positionX - this.positionX);
        double diffY = (z.positionY - this.positionY);
        if((diffX*diffX) + (diffY*diffY) <= 4)
        {
          return true;
        }
      }
    }
    for(int i = (int)this.positionY - 1; i < (int)this.positionY + 2; i++)
    {
      for(int j = (int)this.positionX - 1; j < (int)this.positionX + 2; j++)
      {
    	if(i >= 0 && j >= 0 && i <= LevelVar.house[j].length && j <= LevelVar.house.length)
    	{
          if(LevelVar.house[j][i] instanceof Wall || LevelVar.house[j][i] instanceof Exit)
          {
            double dist;
            if((int)this.positionX > j) 
            {
            if((int)this.positionY > i) 
            {
              dist = Math.sqrt(((this.positionX - ((j*2)+1.8)) * ((this.positionX - ((j*2)+1.8)))) + ((this.positionY - ((i*2)+1.8))*(this.positionY - ((i*2)+1.8))));
            }
            else if((int)this.positionY == i)
            {
              dist = this.positionX - ((j*2)+1.8);
            }
            else
            {
              dist = Math.sqrt(((this.positionX - ((j*2)+1.8))*((this.positionX - ((j*2)+1.8)))) + ((this.positionY - ((i * 2) - 0.2))*((this.positionY - ((i * 2) - 0.2)))));
            }
          }
          else if((int)this.positionX == j)
          {
            if((int)this.positionY > i )
            {
              dist = this.positionY - ((i * 2) + 1.8);
            }
            else if((int)this.positionY == i)
            {
              return true;
            }
            else
            {
              dist = ((i * 2) - 0.2) - this.positionY;
            }
          }
          else
          {
            if((int)this.positionY > i)
            {
              dist = Math.sqrt(((this.positionX - ((j * 2) - 0.2))*((this.positionX - ((j * 2) - 0.2)))) + ((this.positionY - ((i * 2)+1.8))*((this.positionY - ((i * 2)+1.8)))));
            }
            else if((int)this.positionY == i)
            {
              dist = ((j * 2) - 0.2) - this.positionX;
            }
            else
            {
              dist = Math.sqrt(((this.positionX - ((j * 2) - 0.2))*((this.positionX - ((j * 2) - 0.2)))) + ((this.positionY - ((i * 2) - 0.2))*((this.positionY - ((i * 2) - 0.2)))));
            }
          }
          if(dist <= 1.0) 
          {
            return true;
          }
          return false;
        }
      }
    }
  }
  return false;
  }

  /**
   * Tests to see if this Zombie can smell the player
   * @param searchDepth the Zombie's zombie_Smell
   * @param house the 2d array of Tiles to search through
   * @return true if the Zombie can smell the player, otherwise returns false
   */
  public boolean scentDetection(int searchDepth, Tile[][] house)
  {
    int depth = 0;
    int numTillDepthIncrease = 0;
    boolean increaseDepth = false;
    ArrayList<Tile> visitedTiles = new ArrayList<>();
    Tile destTile = LevelVar.house[(int)Player.xPosition][(int)Player.yPosition];
    
    this.bfsQueue.clear();
    this.bfsQueue.add(this.curTile);
    numTillDepthIncrease++;
    this.curTile.setVisited(true);
    visitedTiles.add(this.curTile);
    while (!(this.bfsQueue.isEmpty())) 
    {
      Tile currentTile = this.bfsQueue.poll();
      if(increaseDepth)
      {
        numTillDepthIncrease += this.bfsQueue.size();
        increaseDepth = false;
      }
      if(--numTillDepthIncrease == 0)
      {
        depth++;
        increaseDepth = true;
        if(depth > searchDepth)
        {
          for(Tile t : visitedTiles)
          {
            t.setVisited(false);
          }
          return false;
        }
      }
      if(currentTile == destTile)
      {
        for(Tile t : visitedTiles)
        {
          t.setVisited(false);
        }
        return true;
      }
      if(currentTile.neighbors.size() == 0) currentTile.setNeighbors(house);
      for(int i = 0; i < currentTile.neighbors.size(); i++)
      {
        if(!(currentTile.neighbors.get(i).visited))
        {
          this.bfsQueue.add(currentTile.neighbors.get(i));
          currentTile.neighbors.get(i).setVisited(true);
          visitedTiles.add(currentTile.neighbors.get(i));
        }
      }
    }
    for(Tile t : visitedTiles)
    {
      t.setVisited(false);
    }
    return false;
  }
  
  
  /**
   * A* algorithm for the Zombie to use once it's canSmell value is true
   * Sets the Zombie's path arrayList to a list of Tiles from itself to the
   * player.
   * @param house 2d array of Tiles to search
   */
  public void calcPath(Tile[][] house)
  {
    ArrayList<Tile> visitedTiles = new ArrayList<>();
    Tile destTile = house[(int)Player.xPosition][(int)Player.yPosition];
  
    this.searchQueue.clear();
    this.path.clear();
    this.searchQueue.add(this.curTile);
    this.curTile.setVisited(true);
    visitedTiles.add(this.curTile);
    while(!(this.searchQueue.isEmpty()))
    {
      Tile currentTile = this.searchQueue.poll();
      if(currentTile.xCor == destTile.xCor && currentTile.yCor == destTile.yCor)
      {
        this.path.add(0, currentTile);
        while(currentTile.ancestor != null)
        {
          this.path.add(0, currentTile.ancestor);
          if(currentTile.ancestor != null) currentTile = currentTile.ancestor;
        }
        for(Tile t : visitedTiles)
        {
          t.setVisited(false);
          t.setAncestor(null);
        }
        this.makeHeading();
        break;
      }
      if(currentTile.neighbors.size() == 0) 
      {
        currentTile.setNeighbors(house);
      }
      for(int i = 0; i < currentTile.neighbors.size(); i++)
      {
        if(!(currentTile.neighbors.get(i).visited))
        {
          int xCor = currentTile.neighbors.get(i).xCor;
          int yCor = currentTile.neighbors.get(i).yCor;
          int distance = ((int) Math.sqrt((xCor - ((int)Player.xPosition)) * (xCor - ((int)Player.xPosition)) + ((yCor - ((int)Player.yPosition)) * (yCor - ((int)Player.yPosition)))));
          if(currentTile.neighbors.get(i) instanceof Wall)
          {
            currentTile.neighbors.get(i).setCost(10000);
          }
          else 
          {
            currentTile.neighbors.get(i).setCost(distance + currentTile.cost + 1);
          }
          this.searchQueue.add(currentTile.neighbors.get(i));
          currentTile.neighbors.get(i).setVisited(true);
          visitedTiles.add(currentTile.neighbors.get(i));
          currentTile.neighbors.get(i).setAncestor(currentTile);
        }
      }
    }
  }

  /**
   * Used to tell the Zombie where to go once using the A* path obtained 
   * from calcPath()
   * @param time Used to set initial heading for first call
   */
  public void makeHeading()
  {
    Tile destTile = this.path.get(0);
    double diffX;
    double diffY;
    double dist;
    
      if (destTile.xCor == (int) this.positionX
          && destTile.yCor == (int) this.positionY) 
      {
        this.path.remove(0);
        destTile = this.path.get(0);
      }
      diffX = ((destTile.xCor*2) + 0.5) - this.positionX;
      diffY = ((destTile.yCor*2) + 0.5) - this.positionY;
      dist = Math.sqrt(((diffX) * (diffX)) + ((diffY) * (diffY)));
      System.out.println("diffX = " + diffX + " diffY = " + diffY + " dist = " + dist);
      if (destTile.xCor > this.positionX) 
      {
        if (destTile.yCor > this.positionY) 
        {
          double cosZ = Math.toDegrees(Math.acos(diffX/dist));
          this.setHeading(cosZ);
        } 
        else if (destTile.yCor == this.positionY)
        {
          this.setHeading(0.0);
        }
        else 
        {
          double cosZ = Math.toDegrees(Math.acos(diffX/dist));
          this.setHeading(360 - cosZ);
        }
      } 
      else if (destTile.xCor == this.positionX) 
      {
        if (destTile.yCor > this.positionY) 
        {
          this.setHeading(90.0);
        }
        else 
        {
          this.setHeading(270.0);
        }
      } 
      else if (destTile.xCor < this.positionX) 
      {
        if (destTile.yCor > this.positionY) 
        {
          double cosZ = Math.toDegrees(Math.acos((diffX/dist)));
          this.setHeading(90 + cosZ);
        }
        else if (destTile.yCor == this.positionY)
        {
          this.setHeading(180.0);
        }
        else 
        {
          double cosZ = Math.toDegrees(Math.acos(diffX/dist));
          this.setHeading(180 + cosZ);
        }
      }
    } 

  /**
   * An abstract method inherited and implements by all sub-classes
   * of Zombie
   */
  public void makeDecision()
  {}
}