package zombiehouse.level.house;

import java.util.ArrayList;
import java.util.LinkedList;
import zombiehouse.common.Direction;
import zombiehouse.common.LevelVar;
import zombiehouse.common.Player;
import zombiehouse.level.zombie.*;

/**
 * @author Rob
 *
 * ProGen (Procedural-Generation)
 * ProGen is a one-time use 'black box' tool for generating the floor plan of a level
 * 
 * It is expected to create new ProGen object each time the map needs to be built
 * (for either a 'next' build or a 'rebuild')
 * 
 *  ProGen will be seeded by a Random held in LevelVars
 *  And ProGen will pass all 'relevant' data to LevelVars for other classes to read
 *  
 *  package private:
 *  MazeTile[][] scaledHouse - is a 2d array of MazeTiles that holds the entirety of the level
 *  
 *  final vars:
 *  SCALER - is the width and height for turning a MazeTile into a set of Tiles
 *  NUM_ZONES - the total number of Zones that the level will be divided into
 *              NOTE: it was hoped that the zones could be more 'fluid' than they are
 *              but instead they are set to be the 4 quadrants of the map
 *  
 *  private:
 *  fullX, fullY - is the "full" width and height of the level
 *                 NOTE: actually is 1-less than, with an outside bounding wall
 *  scaledX, scaledY - is the width and height of the Zones in this level
 *  fullSizeHouse - is the 2d array of Tiles for the level
 *                  NOTE: this is only temporarily held here, for the last
 *                  components of generation and then it is sent to LevelVar
 *   zoneList - is the list of Zones in the level (indexed by ID)
 *   nextRoom - is assigned to a new Room then incremented (maintains unique IDs)
 *   allRoomList - is a list of all Rooms in the level 
 */
public class ProGen
{
  private static final int SCALER = 4;
  private static final int NUM_ZONES = 4;
  
  MazeTile[][] scaledHouse;
  private int fullX, fullY;
  private int scaledX, scaledY;
  private Tile[][] fullSizeHouse;
  private Zone[] zoneList;
  private static int nextRoom = 0;
  private ArrayList<Room> allRoomList;
  private int nextGenStep = 0;
  private double savePillarChance;
//  private boolean skipAll = false;
  
  public ProGen()
  {
    fullX = Level.houseWidth - 1;
    fullY = Level.houseHeight - 1;
    scaledX = fullX / SCALER;
    scaledY = fullY / SCALER;
    scaledHouse = new MazeTile[scaledX][scaledY];
    fullSizeHouse = new Tile[fullX + 1][fullY + 1];
    zoneList = new Zone[NUM_ZONES];
    for(int i = 0; i < NUM_ZONES; i++)
    {
      zoneList[i] = new Zone(i, scaledX / 2, scaledY / 2);
    }
    allRoomList = new ArrayList<Room>();
    nextRoom = 0;
    assignSquareZones();
//    if(!LevelVar.HOUSE_PRESENTATION)
//    {
//      System.out.println("Setting new house...");
//      shortCutGen();
//      findNeighbors();
//    }
//    else
//    {
      savePillarChance = LevelVar.pillarSpawnChance;
      LevelVar.WITH_SIGHT = false;
      LevelVar.pillarSpawnChance = 0.0;
      LevelVar.SPAWN_MONSTERS = false;
      if(LevelVar.HOUSE_PRESENTATION)
      {
        LevelVar.house = mazeTileToTile();
      }
//    }
      if(!LevelVar.HOUSE_PRESENTATION)
      {
        shortCutGen();
      }
  }
  
  private void assignSquareZones()
  {
    for(int i = 0; i < scaledX; i++)
    {
      for(int j = 0; j < scaledY; j++)
      {
        if(i < scaledX / 2)
        {
          if(j < scaledY / 2)
          {
            scaledHouse[i][j] = new MazeTile(0);
          }
          else
          {
            scaledHouse[i][j] = new MazeTile(1);
          }
        }
        else
        {
          if(j < scaledY / 2)
          {
            scaledHouse[i][j] = new MazeTile(2);
          }
          else
          {
            scaledHouse[i][j] = new MazeTile(3);
          }
        }
      }
    }
    for(int i = 0; i < 4; i++)
    {
      zoneList[i].initializeSubHouse();
    }
    if(!LevelVar.HOUSE_PRESENTATION)
    {
//      for(int i = 0; i < 4; i++)
//      {
//        zoneList[i].initializeSubHouse();
//      }
      shortCutGen();
    }
  }
  
  public void nextStep()
  {
    LevelVar.SPAWN_MONSTERS = false;
    switch(nextGenStep)
    {
    case(0):
      startRoomCarve(0);
      break;
    case(1):
      startRoomCarve(1);
      break;
    case(2):
      startRoomCarve(2);
      break;
    case(3):
      startRoomCarve(3);
      break;
    case(4):
      fillWithRooms(0);
      break;
    case(5):
      fillWithRooms(1);
      break;
    case(6):
      fillWithRooms(2);
      break;
    case(7):
      fillWithRooms(3);
      break;
    case(8):
      fillWithCoordoors(0);
      break;
    case(9):
      fillWithCoordoors(1);
      break;
    case(10):
      fillWithCoordoors(2);
      break;
    case(11):
      fillWithCoordoors(3);
      break;
    case(12):
      createPaths(0);
      break;
    case(13):
      createPaths(1);
      break;
    case(14):
      createPaths(2);
      break;
    case(15):
      createPaths(3);
      break;
    case(16):
      hallPathsMin(0);
      ensureConnectedZone(0);
      break;
    case(17):
      hallPathsMin(1);
      ensureConnectedZone(1);
      break;
    case(18):
      hallPathsMin(2);
      ensureConnectedZone(2);
      break;
    case(19):
      hallPathsMin(3);
      ensureConnectedZone(3);
      break;
    case(20):
      connectZones();
      break;
    case(21):
      LevelVar.pillarSpawnChance = savePillarChance;
      LevelVar.SPAWN_MONSTERS = true;
      break;
    case(22):
      nextGenStep++;
      LevelVar.house = mazeTileToTile();
      splitPlayerAndExit();
      return;
    default:
      return;
    }
    nextGenStep++;
    LevelVar.house = mazeTileToTile();
  }
  
  public void shortCutGen()
  {
////    if(nextGenStep != 0) { return; }
//    LevelVar.pillarSpawnChance = savePillarChance;
//    LevelVar.SPAWN_MONSTERS = true;
//    nextGenStep = 100;
//    for(int i = 0; i < 4; i++)
//    {
//      zoneList[i].initializeSubHouse();
//      startRoomCarve(i);
//      fillWithRooms(i);
//      fillWithCoordoors(i);
//      hallPathsMin(i);
//      ensureConnectedZone(i);
//    }
//    connectZones();
//    LevelVar.house = mazeTileToTile();
//    splitPlayerAndExit();
    
//    skipAll = true;
    for(int i = nextGenStep; i < 23; i++)
    {
      nextStep();
    }
    
  }
  
  private Tile[][] mazeTileToTile()
  {
    for(int i = 0; i < scaledX; i++)
    {
      for(int j = 0; j < scaledY; j++)
      {
        for(int x = 0; x < SCALER; x++)
        {
          for(int y = 0; y < SCALER; y++)
          {
            if(x == 0 && y == 0 && LevelVar.rand.nextDouble() < LevelVar.pillarSpawnChance)
            {
              fullSizeHouse[i * SCALER + x][j * SCALER + y] = new Wall(i * SCALER + x, j * SCALER + y, scaledHouse[i][j].zone);
            }
            else if(x == 0 && scaledHouse[i][j].westWall)
            {
              if( scaledHouse[i][j].westExit && (y == 1 || y == 2) )
              {
                fullSizeHouse[i * SCALER + x][j * SCALER + y] = new Floor(i* SCALER + x, j * SCALER + y, scaledHouse[i][j].zone, false);
              }
              else
              {
                fullSizeHouse[i * SCALER + x][j * SCALER + y] = new Wall(i * SCALER + x, j * SCALER + y, scaledHouse[i][j].zone );
              }
              
            }
            else if(y == 0 && scaledHouse[i][j].northWall)
            {
              if( scaledHouse[i][j].northExit && (x == 1 || x == 2) )
              {
                fullSizeHouse[i * SCALER + x][j * SCALER + y] = new Floor(i* SCALER + x, j * SCALER + y, scaledHouse[i][j].zone, false);
              }
              else
              {
                fullSizeHouse[i * SCALER + x][j * SCALER + y] = new Wall(i * SCALER + x, j * SCALER + y, scaledHouse[i][j].zone );
              }
            }
            else
            {
              fullSizeHouse[i * SCALER + x][j * SCALER + y] = new Floor(i* SCALER + x, j * SCALER + y, scaledHouse[i][j].zone, scaledHouse[i][j].isRoom);
            }
          }
        }
      }
    }
    int tempZone;
    for(int i = 0; i < fullX + 1; i++)
    {
      if(i < fullX / 2) { tempZone = 1; }
      else { tempZone = 3; }
      fullSizeHouse[fullX][i] = new Wall(fullX, i, tempZone);
    }
    for(int i = 0; i < fullY + 1; i++)
    {
      if(i < fullY / 2) { tempZone = 2; }
      else { tempZone = 3; }
      fullSizeHouse[i][fullY] = new Wall(i, fullY, tempZone);
    }
    
    return fullSizeHouse;
  }
  
  private void startRoomCarve(int zone)
  {
      
    int roomCarveAttempts = 50; // arbitrary upper-bound of attempting to place 'random' rooms
    for(int i = 0; i < roomCarveAttempts; i++)
    {
      int roomWidth = LevelVar.rand.nextInt(LevelVar.levelNum / 2 + 2) + 2; // hard code value 2 allows for 2-3
      int roomHeight = LevelVar.rand.nextInt(LevelVar.levelNum / 2 + 2) + 2;
      int topCornerX = LevelVar.rand.nextInt(zoneList[zone].zoneX - roomWidth + 1);
      int topCornerY = LevelVar.rand.nextInt(zoneList[zone].zoneY - roomHeight + 1);
      if( attemptToPlace(topCornerX, topCornerY, roomWidth, roomHeight, zoneList[zone]) )
      {
        Room temp = new Room(nextRoom, roomWidth, roomHeight, topCornerX, topCornerY, zone);
        allRoomList.add(temp);
        zoneList[zone].rooms.add( temp );
        temp.setMazeTileRooms();
        nextRoom++;
      }
    }
  }
  
  private void fillWithRooms(int zone)
  { 
    for(int i = 0; i < zoneList[zone].zoneX; i++)
    {
      for(int j = 0; j < zoneList[zone].zoneY; j++)
      {
        for(int x = LevelVar.levelNum / 2 + 4; x > 1; x--)
        {
          for(int y = LevelVar.levelNum / 2 + 4; y > 1; y--)
          {
            if( attemptToPlace(i, j, x, y, zoneList[zone]) )
            {

              Room temp = new Room(nextRoom, x, y, i, j, zone);
              allRoomList.add(temp);
              zoneList[zone].rooms.add( temp );
              temp.setMazeTileRooms();
              nextRoom++;
            }
          }
        }
      }
    }
  }
  
  private void fillWithCoordoors(int zone)
  {
    int length = Math.max(zoneList[zone].zoneX, zoneList[zone].zoneY);
    for(int i = 0; i < zoneList[zone].zoneX; i++)
    {
      for(int j = 0; j < zoneList[zone].zoneY; j++)
      {
        for(int len = length; len >= 1; len--)
        {
          if(!zoneList[zone].subHouse[i][j].allWallsIntact()) { continue; }
          if( attemptToPlace(i, j, len, 1, zoneList[zone]) )
          { 
            Room temp = new Room(nextRoom, len, 1, i, j, zone);
            allRoomList.add(temp);
            zoneList[zone].rooms.add( temp );
            temp.setMazeTileRooms();
            nextRoom++;
            continue;
          }
          if( attemptToPlace(i, j, 1, len, zoneList[zone]) )
          { 
            Room temp = new Room(nextRoom, 1, len, i, j, zone);
            allRoomList.add(temp);
            zoneList[zone].rooms.add( temp );
            temp.setMazeTileRooms();
            nextRoom++;
            continue;
          }
        }
      }
    }
  }
  
  private void createPathFromRoom(Room r, int zone)
  {
    boolean needsExit = true;
    int attempts = 0;
    while( needsExit && attempts < 50)
    {
      attempts++;
      Direction randomDir = Direction.values()[LevelVar.rand.nextInt(4)];
      if( randomDir == Direction.NORTH ) //North seems to work most consistently
      {
        int randX = r.xOffSet + LevelVar.rand.nextInt(r.roomX);
        if( zoneList[zone].notInZone(randX, r.yOffSet + randomDir.dY) ) { continue; }
        zoneList[zone].subHouse[randX][r.yOffSet].makeExit(randomDir);
        zoneList[zone].subHouse[randX][r.yOffSet + randomDir.dY].makeExit(randomDir.getOppositeDir());
        if(LevelVar.LEVEL_DEBUG_TEXT)
        {
          System.out.println("Opened: " + randomDir + " exit in room " + r.roomNumber + " @ [" + randX + "," + r.yOffSet + "]");
        }
        needsExit = false;
        if(r.alreadyNeighbors( zoneList[zone].subHouse[randX][r.yOffSet + randomDir.dY].room ) ) { continue; }
        r.addNeighbor(allRoomList.get(zoneList[zone].subHouse[randX][r.yOffSet + randomDir.dY].room));
      }
      else if( randomDir == Direction.SOUTH )
      {
        int randX = r.xOffSet + LevelVar.rand.nextInt(r.roomX);
        if( zoneList[zone].notInZone(randX, r.yOffSet + r.roomY - 1 + randomDir.dY) ) { continue; }
        zoneList[zone].subHouse[randX][r.yOffSet + r.roomY - 1].makeExit(randomDir);
        zoneList[zone].subHouse[randX][r.yOffSet + r.roomY - 1 + randomDir.dY].makeExit(randomDir.getOppositeDir());
        if(LevelVar.LEVEL_DEBUG_TEXT)
        {
          System.out.println("Opened: " + randomDir + " exit in room " + r.roomNumber + " @ [" + randX + "," + (r.yOffSet + r.roomY - 1) + "]");
        }
        needsExit = false;
        if(r.alreadyNeighbors( zoneList[zone].subHouse[randX][r.yOffSet + r.roomY - 1 + randomDir.dY].room ) ) { continue; }
        r.addNeighbor(allRoomList.get(zoneList[zone].subHouse[randX][r.yOffSet + r.roomY - 1 + randomDir.dY].room));
      }
      else if( randomDir == Direction.EAST )
      {
        int randY = r.yOffSet + LevelVar.rand.nextInt(r.roomY);
        if( zoneList[zone].notInZone(r.xOffSet + r.roomX - 1 + randomDir.dX, randY) ) { continue; }
        zoneList[zone].subHouse[r.xOffSet + r.roomX - 1][randY].makeExit(randomDir);
        zoneList[zone].subHouse[r.xOffSet + r.roomX - 1 + randomDir.dX][randY].makeExit(randomDir.getOppositeDir());
        if(LevelVar.LEVEL_DEBUG_TEXT)
        {
          System.out.println("Opened: " + randomDir + " exit in room " + r.roomNumber + " @ [" + (r.xOffSet + r.roomX - 1) + "," + randY + "]");
        }
        needsExit = false;
        if(r.alreadyNeighbors( zoneList[zone].subHouse[r.xOffSet + r.roomX - 1 + randomDir.dX][randY].room ) ) { continue; }
        r.addNeighbor(allRoomList.get(zoneList[zone].subHouse[r.xOffSet + r.roomX - 1 + randomDir.dX][randY].room));
      }
      else if( randomDir == Direction.WEST )
      {
        int randY = r.yOffSet + LevelVar.rand.nextInt(r.roomY);
        if( zoneList[zone].notInZone(r.xOffSet + randomDir.dX, randY) ) { continue; }
        zoneList[zone].subHouse[r.xOffSet][randY].makeExit(randomDir);
        zoneList[zone].subHouse[r.xOffSet + randomDir.dX][randY].makeExit(randomDir.getOppositeDir());
        if(LevelVar.LEVEL_DEBUG_TEXT)
        {
          System.out.println("Opened: " + randomDir + " exit in room " + r.roomNumber + " @ [" + (r.xOffSet) + "," + randY + "]");
        }
        needsExit = false;
        if(r.alreadyNeighbors( zoneList[zone].subHouse[r.xOffSet + randomDir.dX][randY].room ) ) { continue; }
        r.addNeighbor(allRoomList.get(zoneList[zone].subHouse[r.xOffSet + randomDir.dX][randY].room));
      }
    }
  }
  
  private void createPaths(int zone)
  {
    for(Room r : zoneList[zone].rooms)
    {
      createPathFromRoom(r, zone);
    }
  }
  
  private void hallPathsMin(int zone)
  {
    for(Room r : zoneList[zone].rooms)
    {
      if(!r.isNormalRoom && r.openedNeighboringRooms.size() < 2)
      {
        createPathFromRoom(r, zone);
      }
    }
  }
  
  private void ensureConnectedZone(int zone)
  {
    int connectedSoFar = 0;
    int connectedRoomsTotal = zoneList[zone].rooms.size();
    // int attempts = 0; - used to use to ensure no 'infinate loops'
    while(connectedSoFar < connectedRoomsTotal)
    {
      connectedSoFar = 0;
      LinkedList<Room> bfs = new LinkedList<Room>();
      ArrayList<Room> connectedRooms = new ArrayList<Room>();
      ArrayList<Room> notConnectedRooms = new ArrayList<Room>();
      bfs.add(zoneList[zone].rooms.get(0));
      while(!bfs.isEmpty())
      {
        Room curRoom = bfs.poll();
        if(curRoom.visited) { continue; }
        connectedSoFar++;
        curRoom.visited = true;
        for(Room r : curRoom.openedNeighboringRooms)
        {
          bfs.add(r);
          connectedRooms.add(r);
        }
        // create inverse list
        for(Room r1 : zoneList[zone].rooms)
        {
          for(Room r2 : connectedRooms)
          {
            if(r1.roomNumber == r2.roomNumber)
            {
              continue;
            }
            notConnectedRooms.add(r1);
          }
        }
      }
      if(connectedSoFar == connectedRoomsTotal)
      { 
        if(LevelVar.LEVEL_DEBUG_TEXT)
        {
          System.out.println("Zone " + zone + " all connected!");
        }
        return; // success
      } 
      if( connectedRooms.size() > notConnectedRooms.size() )
      {
        int randVal = LevelVar.rand.nextInt(notConnectedRooms.size());
        createPathFromRoom(notConnectedRooms.get(randVal), zone);
      }
      else
      {
        int randVal = LevelVar.rand.nextInt(connectedRooms.size());
        createPathFromRoom(connectedRooms.get(randVal), zone);
      }

      for(Room r: connectedRooms)
      {
        r.visited = false;
      }
      if(LevelVar.LEVEL_DEBUG_TEXT)
      {
        System.out.println("Zone " + zone + " needs try again!");
      }
    }
    System.out.println("ERROR: Could not connect zones");
  }
  
  private boolean attemptToPlace(int topX, int topY, int width, int height, Zone zone)
  {
    for(int i = 0; i < width; i++)
    {
      for(int j = 0; j < height; j++)
      {
        if( zone.notInZone(topX + i, topY + j) ) { return false; }
        if( ! zone.subHouse[topX + i][topY + j].allWallsIntact() ) { return false; }
      }
    }
    for(int i = 0; i < width; i++)
    {
      for(int j = 0; j < height; j++)
      {
        if( i != width - 1) { zone.subHouse[topX + i][topY + j].breakWall(Direction.EAST); }
        if( i != 0 )        { zone.subHouse[topX + i][topY + j].breakWall(Direction.WEST); } 
        if( j != height - 1){ zone.subHouse[topX + i][topY + j].breakWall(Direction.SOUTH); }
        if( j != 0 )        { zone.subHouse[topX + i][topY + j].breakWall(Direction.NORTH); }
      }
    }
    return true;
  }
  
  private boolean illegalIndex(int x, int y)
  {
    if(x < 0 || x >= scaledX) { return true; }
    if(y < 0 || y >= scaledY) { return true; }
    return false;
  }
  
  private void findNeighbors()
  {
    for(int i = 1; i < fullX - 1; i++)
    {
      for(int j = 1; j < fullY - 1; j++)
      {
        for(Direction dir : Direction.values())
        {
          if( illegalIndex(i + dir.dX, j + dir.dY) ) { continue; }
          fullSizeHouse[i][j].addNeighbor(fullSizeHouse[i + dir.dX][j + dir.dY]);
        }
      }
    }
  }
  
  private void connectZones()
  { 
    int zonePairs = 0;
    while( zonePairs < 3 )
    {
      boolean checkRow = LevelVar.rand.nextBoolean(); //draw line left or down
      if( checkRow )
      {
        int rowToCheck = LevelVar.rand.nextInt(scaledX);
        MazeTile tile1 = scaledHouse[scaledX / 2 - 1][rowToCheck];
        MazeTile tile2 = scaledHouse[scaledX / 2][rowToCheck];
        if(LevelVar.LEVEL_DEBUG_TEXT)
        {
          System.out.println("connecting zones: " + tile1.zone + " " + tile2.zone);
        }
        if(tile1.zone == tile2.zone) { break; }
        
        tile1.makeExit(Direction.EAST);
        tile2.makeExit(Direction.WEST);
        if(zoneList[tile1.zone].alreadyNeighbors(tile2.zone)) { continue; }
        zoneList[tile1.zone].addNeighbor(tile2.zone);
        zoneList[tile2.zone].addNeighbor(tile1.zone);
      }
      else //check col
      {
        int colToCheck = LevelVar.rand.nextInt(scaledY);
        MazeTile tile1 = scaledHouse[colToCheck][scaledY / 2 - 1];
        MazeTile tile2 = scaledHouse[colToCheck][scaledY / 2];
        if(LevelVar.LEVEL_DEBUG_TEXT)
        {
          System.out.println("connecting zones: " + tile1.zone + " " + tile2.zone);
        }
        if(tile1.zone == tile2.zone) { break; }
        
        tile1.makeExit(Direction.SOUTH);
        tile2.makeExit(Direction.NORTH);
        if(zoneList[tile1.zone].alreadyNeighbors(tile2.zone)) { continue; }
        zoneList[tile1.zone].addNeighbor(tile2.zone);
        zoneList[tile2.zone].addNeighbor(tile1.zone);
      }
      zonePairs += 1;
    }
  }
  
  private void splitPlayerAndExit()
  {
    boolean playerFirst = LevelVar.rand.nextBoolean();
    boolean doFirst = true;;
    for(int i = 0; i < NUM_ZONES; i++){
      if(zoneList[i].neighboringZones.size() == 1)
      {
        if(doFirst)
        {
          if(playerFirst)
          {
            placePlayer(i);
          }
          else
          {
            placeMasterZombie(i);
            placeExit(i);
          }
          doFirst = false;
        }
        else
        {
          if(!playerFirst)
          {
            placePlayer(i);
          }
          else
          {
            placeMasterZombie(i);
            placeExit(i);
          }
        }
      }
    }
  }
  
  private void placePlayer(int zoneNum)
  {
    boolean playerNotPlaced = true;
    while(playerNotPlaced)
    {
      int randX = LevelVar.rand.nextInt(fullX);
      int randY = LevelVar.rand.nextInt(fullY);
      if(LevelVar.house[randX][randY].isEmpty() && LevelVar.house[randX][randY].zone == zoneNum)
      {
        Player.xPosition = randX;
        Player.yPosition = randY;
        playerNotPlaced = false;
      }
    }
  }
  
  private void placeMasterZombie(int zoneNum)
  {
    if(!LevelVar.SPAWN_MONSTERS && !LevelVar.SPAWN_MASTER) { return; }
    boolean zombieNotPlaced = true;
    while(zombieNotPlaced)
    {
      int randX = LevelVar.rand.nextInt(fullX);
      int randY = LevelVar.rand.nextInt(fullY);
      if(LevelVar.house[randX][randY].isEmpty() && LevelVar.house[randX][randY].zone == zoneNum)
      {
        LevelVar.zombieCollection.add( new MasterZombie(0, randX + 0.5, randY + 0.5, LevelVar.house[randX][randY], Level.nextZombie++) );
        LevelVar.house[randX][randY].isUsed();
        zombieNotPlaced = false;
      }
    }
  }
  
  private void placeExit(int zoneNum)
  {
    boolean exitNotPlaced = true;
    while(exitNotPlaced)
    {
      boolean onVertWall = LevelVar.rand.nextBoolean();
      if(onVertWall)
      {
        int exitX = 0;
        if(zoneNum == 2 || zoneNum == 3) { exitX = fullX; }
        int exitTop = LevelVar.rand.nextInt(fullY / 2 - 1);
        if(zoneNum == 1 || zoneNum == 3) { exitTop += fullX / 2; }
        Direction facing = Direction.EAST;
        if(zoneNum == 2 || zoneNum == 3) { facing = Direction.WEST; }
        
        if( !LevelVar.house[exitX + facing.dX][exitTop].isEmpty() ) { continue; }
        if( LevelVar.house[exitX + facing.dX][exitTop + 1].isEmpty() )
        {
          LevelVar.house[exitX][exitTop]     = new Exit(exitX, exitTop, zoneNum);
          LevelVar.house[exitX][exitTop + 1] = new Exit(exitX, exitTop + 1, zoneNum);
          exitNotPlaced = false;
        }
      }
      else // on a horizontal wall
      {
        int exitY = 0;
        if(zoneNum == 1 || zoneNum == 3) { exitY = fullY; }
        int exitLeft = LevelVar.rand.nextInt(fullX / 2 - 1);
        if(zoneNum == 2 || zoneNum == 3) { exitLeft += fullX / 2; }
        Direction facing = Direction.SOUTH;
        if(zoneNum == 1 || zoneNum == 3) { facing = Direction.NORTH; }
        
        if( !LevelVar.house[exitLeft][exitY + facing.dY].isEmpty() ) { continue; }
        if( LevelVar.house[exitLeft + 1][exitY + facing.dY].isEmpty() )
        {
          LevelVar.house[exitLeft][exitY]     = new Exit(exitLeft, exitY, zoneNum);
          LevelVar.house[exitLeft + 1][exitY] = new Exit(exitLeft + 1, exitY, zoneNum);
          exitNotPlaced = false;
        }
      }
    }  
  }
  
  /**
   * @author Rob
   *
   * MazeTile - a structure of ProGen (Attomic size)
   * MazeTile is the second lowest level structure of house generation
   * When expanded these become 4x4 Tile sets (their make up decided by 
   * the tripped flags)
   * Is the basic 'building block' for house - until player/zombie/exit placement
   * 
   * private:
   * zone - is the Zone ID number that this MazeTile is part of
   * room - is the Room ID number that this MazeTile is part of
   * __Wall - is a flag for if the specified wall should be present in generation
   * __Exit - is a flag for if the specified wall should be opened for a doorway in generation
   * isRoom - is a flag to denote if the tile is part of a (fullsize) room - not hallway
   */
  private class MazeTile
  {
    private int zone;
    private int room;
    
    private boolean northWall, eastWall, southWall, westWall;
    private boolean northExit, eastExit, southExit, westExit;
    private boolean isRoom;
    
    private MazeTile(int zone)
    {
      this.zone = zone;
      northWall = true;
      eastWall = true;
      southWall = true;
      westWall = true;
      northExit = false;
      eastExit = false;
      southExit = false;
      westExit = false;
    }
    
    private void setRoom(int val)
    { 
      room = val;
      isRoom = allRoomList.get(val).isNormalRoom;
    }
    
    private void breakWall(Direction dir)
    {
      switch(dir)
      {
      case NORTH:
        northWall = false;
        break;
      case EAST:
        eastWall = false;
        break;
      case SOUTH:
        southWall = false;
        break;
      case WEST:
        westWall = false;
        break;
      }
    }
    
    private void makeExit(Direction dir)
    {
      switch(dir)
      {
      case NORTH:
        northExit = true;
        break;
      case EAST:
        eastExit = true;
        break;
      case SOUTH:
        southExit = true;
        break;
      case WEST:
        westExit = true;
        break;
      }
    }
    
    private boolean allWallsIntact()
    {
      if(northWall && eastWall && southWall && westWall) { return true; }
      return false;
    }
  }
  
  
  /**
   * @author Rob
   *
   * Zone - a structure of ProGen (Middle size)
   * Used for subdividing the house into 4 quadrents with special specifications
   * Most importantly used as a container class, holding lots of helper values
   * 
   * package private:
   * MazeTile[][] subhouse - is the 2d array that holds the MazeTiles of this zone
   *                         like LevelVar.house, but for a small section
   * 
   * private:
   * zoneNum - [0,3] directly, and is the index of the zone in the zoneList
   * zoneX, zoneY - the width and height of the zone (separate in case level 
   *                incase the code can become more generalized
   * neighboringZones - a simple list of either 1 or 2 zones connected by door
   * rooms - a full list of the Rooms contained in this zone
   */
  private class Zone
  {
    MazeTile[][] subHouse;
    private int zoneNum;
    private int zoneX, zoneY;
    private ArrayList<Zone> neighboringZones;
    private ArrayList<Room> rooms;
    
    private Zone(int zoneNum, int zoneX, int zoneY)
    {
      this.zoneNum = zoneNum;
      this.zoneX = zoneX;
      this.zoneY = zoneY;
      subHouse = new MazeTile[zoneX][zoneY];
      neighboringZones = new ArrayList<Zone>();
      rooms = new ArrayList<Room>();
    }
    
    private boolean alreadyNeighbors(int i)
    {
      for(Zone z : neighboringZones)
      {
        if(i == z.zoneNum) { return true; }
      }
      return false;
    }
    
    private void addNeighbor(int i)
    {
      neighboringZones.add(zoneList[i]);
    }
    
    private boolean notInZone(int x, int y)
    {
      if(x >= zoneX || y >= zoneY) { return true; }
      if(x < 0 || y < 0) { return true; }
      return false;
    }
    
    private void initializeSubHouse()
    {
      int xOffSet = 0;
      int yOffSet = 0;
      if(zoneNum == 1 || zoneNum == 3) { xOffSet = scaledX / 2; }
      if(zoneNum == 2 || zoneNum == 3) { yOffSet = scaledY / 2; }
      for(int i = 0; i < zoneX; i++)
      {
        for(int j = 0; j < zoneY; j++)
        {
          subHouse[i][j] = scaledHouse[xOffSet + i][yOffSet + j];
        }
      }
    }
  }
  
  
  /**
   * @author Rob
   *
   * Room - a structure of ProGen (Small size)
   * Used for subdividing a Zone into specified rooms/hallways
   * Most importantly used as a container class, holding lots of helper values
   * 
   * private:
   * roomNumber - a unique (per generation) ID number for the room
   *              mostly used for test of equality
   * isNormalRoom - flag to denote a [2-x) x [2-x) room vs hallway
   * roomLayout - a 2d array that holds the MazeTiles of the room
   *              (like LevelVar.house and Zone.subhouse)
   * xOffSet, yOffSet - tell the top-left corner for the roomLayout
   *                    with respect to the Zone.subhouse
   * roomX, roomY - tell the width and height of the room
   * zoneNum - is the zone ID number that this room is part of
   * openedNeighboringRooms - list of rooms adjacent and reachable
   * visited - a boolean flag used in searching a zone for connectivity
   */
  private class Room
  {
    private int roomNumber;
    private boolean isNormalRoom;
    private MazeTile[][] roomLayout;
    private int xOffSet, yOffSet;
    private int roomX, roomY;
    private int zoneNum;
    private ArrayList<Room> openedNeighboringRooms;
    private boolean visited;
    
    private Room(int num, int width, int height, int topX, int topY, int zone)
    {
      roomNumber = num;
      roomX = width;
      roomY = height;
      xOffSet = topX;
      yOffSet = topY;
      zoneNum = zone;
      roomLayout = initalizeRoomTiles(topX, topY, width, height, zone);
      openedNeighboringRooms = new ArrayList<Room>();
      if(roomX == 1 || roomY == 1)  { isNormalRoom = false; }
      else                          { isNormalRoom = true; }
      if(LevelVar.LEVEL_DEBUG_TEXT)
      {
        System.out.println("New room in zone: " + zone + " @ [" + topX + "," + topY + "]");
      }
    }
    
    private MazeTile[][] initalizeRoomTiles(int topX, int topY, int width, int height, int zone)
    {
      MazeTile[][] layout = new MazeTile[width][height];
      for(int i = 0; i < width; i++)
      {
        for(int j = 0; j < height; j++)
        {
          layout[i][j] = zoneList[zoneNum].subHouse[topX + i][topY + j];
        }
      }
      return layout;
    }
    
    private boolean alreadyNeighbors(int roomNum)
    {
      for(Room neighbor : openedNeighboringRooms)
      {
        if(roomNum == neighbor.roomNumber) { return true; }
      }
      return false;
    }
    
    private void addNeighbor(Room r)
    {
      openedNeighboringRooms.add(r);
      r.openedNeighboringRooms.add(this);
    }
    
    private void setMazeTileRooms()
    {
      for(int i = 0; i < roomX; i++)
      {
        for(int j = 0; j < roomY; j++)
        {
          roomLayout[i][j].setRoom(roomNumber);
        }
      }
    }
  }
}
