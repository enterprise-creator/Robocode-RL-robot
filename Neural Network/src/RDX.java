import robocode.*;
import java.awt.Color;
import java.util.Random;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.io.BufferedOutputStream;


public class RDX extends AdvancedRobot {
    public enum enumEnergy {
        verylow, low, medium, high
    };
    public enum enumDistance {
        close, near, far, veryfar
    };
    public enum enumAction {
        circle, retreat, advance, fire, ram, right, left, diagonal
    };
    public enum enumOperationMode {
        scan, performAction
    };

    static private LookUp q = new LookUp(enumEnergy.values().length, enumDistance.values().length, enumAction.values().length);


    private enumEnergy currentEnergy = enumEnergy.low;
    private enumDistance currentDistance = enumDistance.near;
    private enumAction currentAction = enumAction.retreat;
    private enumEnergy previousEnergy = enumEnergy.high;
    private enumDistance previousDistance = enumDistance.far;
    private enumAction previousAction = enumAction.circle;
    private enumOperationMode operationMode = enumOperationMode.scan;
    public enumDistance enemyDistance = enumDistance.near;
    public enumEnergy myEnergy = enumEnergy.high;


    private double gamma = 0.75;
    private double alpha = 0.5;

    private double epsilon = 0.8;
    private boolean OFF_POLICY = true;
    private boolean changeEpsilon = true;

    private static int maxNumRounds = 5000;
    private static int[] winArray = new int[(maxNumRounds / 100)];


    // Q-values
    private double crrQ = 0.0;
    private double prevQ;
    private double currentReward = 0.0;

    // Rewards
    private double badInstantReward = 0;
    private double badTerminalReward = -1.0;

    private double goodInstantReward = 0;
    private double goodTerminalReward = 2;

    // Variables
    public double myX;
    public double myY;
    public double enemyBearing;
    public int circleDirection = 1;
    private double enemyDistanceX;
    private double enemyEnergy;
    private static final String winFileName = "win_percentage.csv";
    private static final String lutFile = "LUT.csv";
    private File winFile;
    private File LUTFile;

    public void run() {
        setBulletColor(Color.red);
        setColors(Color.black, Color.black, Color.blue);
        turnRadarLeft(180);
        winFile = getDataFile(winFileName);
        LUTFile = getDataFile(lutFile);

        if (changeEpsilon) {
            int n = getRoundNum();
            if (n > (3000)) {
                epsilon = 0.001;
            }
        }
        while (true) {
            switch (operationMode) {
                case scan: {
                    currentReward = 0.0;
                    turnRadarLeft(360);
                    break;
                }
                case performAction: {
                    if (Math.random() <= epsilon)
                        currentAction = selectRandomAction();
                    else
                        currentAction = bestAction(myEnergy.ordinal(), enemyDistance.ordinal());
                    switch (currentAction) {
                        case circle: {
                            setTurnRight(enemyBearing + 90);
                            setAhead(50 * circleDirection);
                            execute();
                            break;
                        }
                        case advance: {
                            setTurnRight(enemyBearing);
                            setAhead(50);
                            execute();
                            break;
                        }
                        case retreat: {
                            setTurnRight(180 + enemyBearing);
                            back(50);
                            execute();
                            break;
                        }
                        case fire: {
                            double a;
                            a = normalizeAngle(getHeading() - getGunHeading() + enemyBearing);
                            turnGunRight(a);
                            if (enemyEnergy > 16) {
                                fire(3);
                            } else if (enemyEnergy > 10) {
                                fire(2);
                            } else if (enemyEnergy > 4) {
                                fire(1);
                            } else if (enemyEnergy > 2) {
                                fire(.5);
                            } else if (enemyEnergy > .4) {
                                fire(.1);
                            }
                            break;
                        }
                        case ram: {
                            turnRight(enemyBearing);
                            if (enemyEnergy > 16) {
                                fire(3);
                            } else if (enemyEnergy > 10) {
                                fire(2);
                            } else if (enemyEnergy > 4) {
                                fire(1);
                            } else if (enemyEnergy > 2) {
                                fire(.5);
                            } else if (enemyEnergy > .4) {
                                fire(.1);
                            }
                            ahead(enemyDistanceX + 5);
                            break;
                        }
                        case right: {
                            setTurnRight(90);
                            setAhead(10);
                            execute();
                            break;
                        }
                        case left: {
                            setTurnLeft(90);
                            setAhead(10);
                            execute();
                            break;
                        }
                        case diagonal: {
                            setTurnLeft(45);
                            setAhead(10);
                            execute();
                            break;
                        }
                    }
//Update Previous
                    double[] x = new double[] { previousEnergy.ordinal(), previousDistance.ordinal(),
                            previousAction.ordinal() };
                    q.train(x, computeQ(currentReward));
                    operationMode = enumOperationMode.scan;
                }
            }
        }
    }

    public enumEnergy quantizeEnergy(double energy) {
        enumEnergy e = null;
        if (energy < 15)
            e = enumEnergy.verylow;
        else if (energy >= 15 && energy < 30)
            e = enumEnergy.low;
        else if (energy >= 30 && energy < 75)
            e = enumEnergy.medium;
        else if (energy >= 75)
            e = enumEnergy.high;
        return e;
    }
    public enumDistance quantizeDistance(double distance) {
        enumDistance d = null;
        if (distance < 50)
            d = enumDistance.close;
        else if (distance >= 50 && distance < 400)
            d = enumDistance.near;
        else if (distance >= 400 && distance < 700)
            d = enumDistance.far;
        else if (distance >= 700)
            d = enumDistance.veryfar;
        return d;
    }

    public double computeQ(double r) {

        double[] p = new double[] { previousEnergy.ordinal(), previousDistance.ordinal(),
                previousAction.ordinal() };
        double c[];
        if (OFF_POLICY) {
            enumAction max = bestAction(currentEnergy.ordinal(), currentDistance.ordinal());
            c = new double[] { currentEnergy.ordinal(), currentDistance.ordinal(), max.ordinal() };
        }
        else {
            c = new double[] { currentEnergy.ordinal(), currentDistance.ordinal(), currentAction.ordinal() };
        }
        prevQ = q.outputFor(p);
        crrQ = q.outputFor(c);
        double updatedQ = prevQ + (alpha * (r + (gamma * crrQ) - prevQ));
        return updatedQ;
    }
    private double normalizeAngle(double d) {
        double result = d;
        while (result > 180)
            result -= 360;
        while (result < -180)
            result += 360;
        return result;
    }

    private enumAction selectRandomAction() {
        int e = new Random().nextInt(enumAction.values().length);
        return enumAction.values()[e];
    }
    private enumAction bestAction(int energy, int dist) {
        double val;
        double max = -10000;
        int selectAction = 0;

        Random ran1 = new Random();

        if (ran1.nextDouble() < epsilon) {
            selectAction = new Random().nextInt(enumAction.values().length);
        }
        else {
            for (int i = 0; i < 8; i++) {
                double[] x = new double[]{energy, dist, i};
                val = q.outputFor(x);
                if (val >= max) {
                    max = val;
                    selectAction = i;
                }

            }
        }

        return enumAction.values()[selectAction];

    }

    /**
     * onScannedRobot: What to do when you see another robot
     */
    public void onScannedRobot(ScannedRobotEvent e) {
        myX = getX();
        myY = getY();
        enemyBearing = e.getBearing();
        myEnergy = quantizeEnergy(getEnergy());
        enemyDistance = quantizeDistance(e.getDistance());
        enemyDistanceX = e.getDistance();
        enemyEnergy = e.getEnergy();
        previousEnergy = currentEnergy;
        previousDistance = currentDistance;
        previousAction = currentAction;
        currentEnergy = quantizeEnergy(getEnergy());
        currentDistance = quantizeDistance(e.getDistance());
        operationMode = enumOperationMode.performAction;
    }
    public void onWin(WinEvent e) {
        currentReward += goodTerminalReward;
        winArray[(getRoundNum() - 1) / 100]++;
    }
    public void onKeyPressed(KeyEvent event) {
    }
    public void onDeath(DeathEvent e) {
        currentReward -= badTerminalReward;
        double[] x = new double[] { previousEnergy.ordinal(), previousDistance.ordinal(), previousAction.ordinal() };
        q.train(x, computeQ(currentReward));
    }

    public void onHitByBullet(HitByBulletEvent e) { currentReward -= badInstantReward;    }
    public void onBulletMissed(BulletMissedEvent event) { currentReward -= badInstantReward;    }
    public void onHitWall(HitWallEvent e) {  currentReward -= badInstantReward;    }
    public void onBulletHit(BulletHitEvent e) {  currentReward += goodInstantReward;   }
    public void onHitRobot(HitRobotEvent e) {  currentReward += goodInstantReward;    }


    public void onBattleEnded(BattleEndedEvent event) {

        logfile(winFile);
        q.save(LUTFile);
    }

    public void logfile(File OutFile) {

        try {


            RobocodeFileOutputStream fileOut = new RobocodeFileOutputStream(OutFile);
            PrintStream out = new PrintStream(new BufferedOutputStream(fileOut));

            for (int i = 0; i <= getRoundNum() / 100; i++) {
                 out.format("%d, %d,\n", i + 1, winArray[i]);
            }
            out.close();
            fileOut.close();
        }
        catch (IOException exception) {
            exception.printStackTrace();
        }
    }
}
