package zombiehouse.level.zombie;

import zombiehouse.level.house.*;
import zombiehouse.common.*;

/**
 * The LineWalkZombie class contains the behavior for a
 * LineWalkZombie
 * @author Stephen Sagartz
 * @since 2016-03-05
 */
public class LineWalkZombie extends Zombie
{
  /**
   * creates a LineWalkZombie that behaves uniquely
   * @param heading this LineWalkZombie's heading
   * @param positionX this LineWalkZombie's positionX
   * @param positionY this LineWalkZombie's positionY
   * @param curTile this LineWalkZombie's curTile
   */
  public LineWalkZombie(double heading, double positionX, double positionY,
      Tile curTile, int id) {
    super(heading, positionX, positionY, curTile, id);
  }

  /**
   * Updates and sets this Zombie's heading every zombie_Decision_Rate milliseconds
   * and adjusts the behavior according to the ZombieHouse Project specifications.
   */
  @Override
  public void makeDecision()
  {
    if (super.scentDetection(super.getZombieSmell(), LevelVar.house)) {
      super.setSmell(true);
      super.calcPath(LevelVar.house);
      this.setCollided(false);
    } else {
      super.setSmell(false);
      if (super.getCollide()) {
        double curHeading = super.getHeading();
        double boundA = (curHeading + 90) % 360;
        double boundB = (curHeading - 90) % 360;
        if (boundA < boundB) {
          super.setHeading((180 + curHeading)%360);
          super.setCollided(false);
        } else {
          super.setHeading((180+ curHeading)%360);
          super.setCollided(false);
        }
      }

      else if(super.scentDetection(super.getZombieSmell(), LevelVar.house) || this.getSmell())
	  {
		super.setSmell(true);
		super.calcPath(LevelVar.house);
	  }
	  else
	  {
		super.setSmell(false);
		if(super.getCollide())
		{
		  double curHeading = super.getHeading();
		    super.setHeading((180 + curHeading)%360);
		    super.setCollided(false);

		}
	  }
	}
  }
}
