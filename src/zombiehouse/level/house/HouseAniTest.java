package zombiehouse.level.house;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import zombiehouse.common.InputContainer;
import zombiehouse.common.LevelVar;
import zombiehouse.common.Player;
import zombiehouse.level.zombie.*;

public class HouseAniTest extends Application
{

  private Stage stage;
  private Canvas canvas;
  private int canvasWidth = 1000;
  private int canvasHeight = 1000;
  
  private static double PLAYER_MOVE_SPEED = 1.0 / 6.0;
  
  private int sqrPix = canvasWidth / Level.houseWidth;
  
  private GraphicsContext gfx;
  
  private Level l;
  
  @Override
  public void start(Stage primaryStage) throws Exception
  {
    primaryStage.setOnCloseRequest(event -> System.exit(0));
    stage = primaryStage;
    
    System.out.println("First Level!");
    
    l = new Level();
    LevelVar.zombie3D = false;
    
    l.nextLevel();
    if(!LevelVar.HOUSE_PRESENTATION)
    {
      l.fullGen();
    }
    sqrPix = canvasWidth / Level.houseWidth;
    
    primaryStage.setTitle("SPOOKY DEAD MAN HOUSE - Level: " + (LevelVar.levelNum + 1) );
    
    BorderPane root = new BorderPane(); 
    canvas = new Canvas(canvasWidth, canvasHeight); 
    gfx = canvas.getGraphicsContext2D(); 
    root.setCenter(canvas);
    
    Scene scene = new Scene(root, Color.WHITE);
    primaryStage.setScene(scene);
    primaryStage.show();

    
    // borrowed
    scene.setOnKeyPressed(event -> 
    {
      KeyCode keycode = event.getCode();
      if (keycode == KeyCode.UP)
      {
        InputContainer.forward = true;
      }
      else if (keycode == KeyCode.DOWN)
      {
        InputContainer.backward = true;
      }
      else if (keycode == KeyCode.LEFT)
      {
        InputContainer.left = true;
      }
      else if (keycode == KeyCode.RIGHT)
      {
        InputContainer.right = true;
      }
      else if (keycode == KeyCode.L)
      {
        levelUp();
      }
      else if (keycode == KeyCode.ESCAPE)
      {
        System.exit(0);
      }
      else if (keycode == KeyCode.K)
      {
        if(LevelVar.HOUSE_PRESENTATION) { l.nextGenStep(); }
      }
      else if (keycode == KeyCode.J)
      {
        LevelVar.WITH_SIGHT = ! LevelVar.WITH_SIGHT;
      }
      else if (keycode == KeyCode.O)
      {
        l.fullGen();
//        LevelVar.HOUSE_PRESENTATION = ! LevelVar.HOUSE_PRESENTATION;
//        levelUp();
      }
    });

    scene.setOnKeyReleased(event -> 
    {
      KeyCode keycode = event.getCode();
      if (keycode == KeyCode.UP)
      {
        InputContainer.forward = false;
      }
      else if (keycode == KeyCode.DOWN)
      {
        InputContainer.backward = false;
      }
      else if (keycode == KeyCode.LEFT)
      {
        InputContainer.left = false;
      }
      else if (keycode == KeyCode.RIGHT)
      {
        InputContainer.right = false;
      }
    });
    
    ZTimer zMoves = new ZTimer();
    zMoves.zUpdateTimer.schedule(zMoves.myUpdate, Zombie.getDecisionRate(), Zombie.getDecisionRate());
    
    AnimationTimer gameLoop = new MainGameLoop();
    gameLoop.start();
  }
  
  private void levelUp()
  {
    l.nextLevel();
    stage.setTitle("SPOOKY DEAD MAN HOUSE - Level: " + (LevelVar.levelNum + 1));
    System.out.println("\nNext Level! #" + (LevelVar.levelNum + 1));
    sqrPix = canvasWidth / Level.houseWidth;
  }
  
  public class MainGameLoop extends AnimationTimer
  {

    @Override
    public void handle(long now)
    {
      gfx.setFill(Color.BLACK);
      gfx.fillRect(0, 0, canvasWidth, canvasHeight);
      
      for(int i = 0; i < Level.houseWidth; i++)
      {
        for(int j = 0; j < Level.houseHeight; j++)
        {
          gfx.setFill(LevelVar.house[i][j].getColor() );
          gfx.fillRect(i * sqrPix, j * sqrPix, sqrPix, sqrPix);
        }
      }
      
      gfx.setFill(Color.BLUE);
      gfx.fillOval(Player.xPosition * sqrPix - sqrPix / 2, Player.yPosition * sqrPix - sqrPix / 2, sqrPix * 1, sqrPix * 1);
      
      for(Zombie z : LevelVar.zombieCollection)
      {
//        z.makeDecision();
        z.move();
        if(z instanceof LineWalkZombie) { gfx.setFill(Color.DARKOLIVEGREEN); }
        else if(z instanceof RandomWalkZombie) { gfx.setFill(Color.LIGHTGREEN); }
        else { gfx.setFill(Color.VIOLET); }
        gfx.fillOval(z.positionX * sqrPix - sqrPix / 2, z.positionY * sqrPix - sqrPix / 2, sqrPix * 1, sqrPix * 1);
      }
      
      movePlayerIfRequested();
      if(LevelVar.WITH_SIGHT) { l.checkSight(); }
    }
  }
  
  public void movePlayerIfRequested()
  {
    
    double desiredXDisplacement = 0;
    desiredXDisplacement -= (InputContainer.left) ? (1) : 0;
    desiredXDisplacement += (InputContainer.right) ? (1) : 0;
    
    double desiredYDisplacement = 0;
    desiredYDisplacement -= (InputContainer.forward) ? (1) : 0; 
    desiredYDisplacement += (InputContainer.backward) ? (1) : 0;
    
    double desiredPlayerXPosition = Player.xPosition + (desiredXDisplacement * PLAYER_MOVE_SPEED * Player.playerSpeed);
    double desiredPlayerYPosition = Player.yPosition + (desiredYDisplacement * PLAYER_MOVE_SPEED * Player.playerSpeed);
    
    if (LevelVar.house[(int)desiredPlayerXPosition][(int)desiredPlayerYPosition] instanceof Floor) 
    {
      Player.xPosition += desiredXDisplacement * PLAYER_MOVE_SPEED;
      Player.yPosition += desiredYDisplacement * PLAYER_MOVE_SPEED;
    }
    else if (LevelVar.house[(int)desiredPlayerXPosition][(int)Player.yPosition] instanceof Floor)
    {
      Player.xPosition += desiredXDisplacement * PLAYER_MOVE_SPEED;
    }
    else if (LevelVar.house[(int)Player.xPosition][(int)desiredPlayerYPosition] instanceof Floor)
    {
      Player.yPosition += desiredYDisplacement * PLAYER_MOVE_SPEED;
    }
    for(Zombie z: LevelVar.zombieCollection)
    {
      double deltaX = Player.xPosition - z.positionX;
      double deltaY = Player.yPosition - z.positionY;
      if( deltaX * deltaX + deltaY * deltaY < 1  )
      {
        System.out.println("player dead");
        l.restartLevel();
      }
    }
    if (LevelVar.house[(int)desiredPlayerXPosition][(int)desiredPlayerYPosition] instanceof Exit)
    {
      levelUp();
    }
  }
  
  public static void main(String[] args)
  {
    launch(args);
  }
}

