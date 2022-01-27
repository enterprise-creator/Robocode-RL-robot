import robocode.*;
import java.awt.Color;
import java.util.Random;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.BufferedOutputStream;
// API help : https://robocode.sourceforge.io/docs/robocode/robocode/Robot.html
public class Robot2 extends AdvancedRobot {
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
    static private LookUpTable m = new LookUpTable(enumEnergy.values().length, enumDistance.values().length,
            enumAction.values().length);
    static int numRounds = 0;
    static int numWins;
    private enumEnergy currentEnergy = enumEnergy.low;
    private enumDistance currentDistance = enumDistance.near;
    private enumAction currentAction = enumAction.retreat;
    private enumEnergy previousEnergy = enumEnergy.high;
    private enumDistance previousDistance = enumDistance.far;
    private enumAction previousAction = enumAction.circle;
    private enumOperationMode operationMode = enumOperationMode.scan;
    public enumDistance enemyDistance = enumDistance.near;
    public enumEnergy myEnergy = enumEnergy.high;
    // HyperParameters
    private static final double gamma = 0.75;
    private static final double alpha = 0.8;
    private static final double epsilon_initial = 0.8;
    private double epsilon = epsilon_initial;
    private static final boolean OFF_POLICY = false; // TRUE = Q-Learning, FALSE = SARSA
    private boolean decayEpsilonMid = true;
    private boolean decayEpsilon = false;
    private static int maxNumRounds = 5000;
    private static int[] mNumWinArray = new int[(maxNumRounds / 100)];
    private static int[] numBulletHits = new int[(maxNumRounds / 100)];
    // Q
    private double currentQ = 0.0;
    private double previousQ;
    private double currentReward = 0.0;
    // private double badInstantReward = 0.0;
    private double badInstantReward = -0.25;
    private double badTerminalReward = -0.50;
    // private double goodInstantReward = 0;
    private double goodInstantReward = 1;
    private double goodTerminalReward = 2;
    double previousEnergy1 = 100;
    int movementDirection = 1;
    int gunDirection = 1;
    public double myX;
    public double myY;
    public double enemyBearing;
    public int circleDirection = 1;
    private double enemyDistanceX;
    private double enemyEnergy;
    private static final String STATS_FILE_NAME = "s.csv";
    private static final String LUT_FILE_NAME = "LUT.csv";
    private File sOutFile;
    private File sLUTFile;
    public enumEnergy enumEnergyOf(double energy) {
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
    public enumDistance enumDistanceOf(double distance) {
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
    public void printState() {
        out.printf("previous energy [%s], distance [%s], action [%s]\n", previousEnergy, previousDistance,
                previousAction);
    }
    public double computeQ(double r) {
        enumAction max = bestAction(currentEnergy.ordinal(), currentDistance.ordinal());
        if (OFF_POLICY) {
            double[] p = new double[] { previousEnergy.ordinal(), previousDistance.ordinal(),
                    previousAction.ordinal() };
            double[] c = new double[] { currentEnergy.ordinal(), currentDistance.ordinal(), max.ordinal() };
            previousQ = m.outputFor(p);
            currentQ = m.outputFor(c);
        }
        else {
            double[] p = new double[] { previousEnergy.ordinal(), previousDistance.ordinal(),
                    previousAction.ordinal() };
            double[] c = new double[] { currentEnergy.ordinal(), currentDistance.ordinal(), currentAction.ordinal() };
            previousQ = m.outputFor(p);
            currentQ = m.outputFor(c);
        }
        double updatedQ = previousQ + (alpha * (r + (gamma * currentQ) - previousQ));
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
    public void run() {
        setBulletColor(Color.green);
        setColors(Color.yellow, Color.black, Color.green); // body,gun,radar
        turnRadarLeft(360);
        sOutFile = getDataFile(STATS_FILE_NAME);
        sLUTFile = getDataFile(LUT_FILE_NAME);
// Dynamic decay
        if (decayEpsilon)
            epsilon = epsilon_initial - epsilon_initial * (getRoundNum() / maxNumRounds);
        if (decayEpsilonMid) {
            int n = getRoundNum();
            if (n > (0.6 * maxNumRounds)) {
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
// Determine a shot that won't kill the robot...
// We want to ram him instead for bonus points
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
                    m.train(x, computeQ(currentReward));
                    operationMode = enumOperationMode.scan;
                }
            }
        }
    }
    private enumAction selectRandomAction() {
        int e = new Random().nextInt(enumAction.values().length);
        return enumAction.values()[e];
    }
    private enumAction bestAction(int e, int d) {
        double[] mval = new double[8];
        double max = -10000;
        int act = 0;
        enumAction k = enumAction.circle;
        for (int i = 0; i < 8; i++) {
            double[] p = new double[] { e, d, i };
            mval[i] = m.outputFor(p);
            if (mval[i] >= max) {
                max = mval[i];
                act = i;
            } // new
        }
        switch (act) {
            case 0:
                k = enumAction.circle;
                break;
            case 1:
                k = enumAction.advance;
                break;
            case 2:
                k = enumAction.retreat;
                break;
            case 3:
                k = enumAction.fire;
                break;
            case 4:
                k = enumAction.ram;
                break;
            case 5:
                k = enumAction.right;
                break;
            case 6:
                k = enumAction.left;
                break;
            case 7:
                k = enumAction.diagonal;
                break;
        }
        return k;
    }
    /**
     * onScannedRobot: What to do when you see another robot
     */
    public void onScannedRobot(ScannedRobotEvent e) {
        /* System.out.println("Scanned"); */
        myX = getX();
        myY = getY();
        enemyBearing = e.getBearing();
        myEnergy = enumEnergyOf(getEnergy());
        enemyDistance = enumDistanceOf(e.getDistance());
        enemyDistanceX = e.getDistance();
        enemyEnergy = e.getEnergy();
        previousEnergy = currentEnergy;
        previousDistance = currentDistance;
        previousAction = currentAction;
        currentEnergy = enumEnergyOf(getEnergy());
        currentDistance = enumDistanceOf(e.getDistance());
        operationMode = enumOperationMode.performAction;
    }
    public void onWin(WinEvent e) {
        currentReward = goodTerminalReward;
// currentReward = goodTerminalReward;
// currentReward += 100;
        mNumWinArray[(getRoundNum() - 1) / 100]++;
// System.out.println(numWins);
    }
    public void onKeyPressed(KeyEvent event) {
    }
    public void onDeath(DeathEvent e) {
        currentReward = badTerminalReward;
// currentReward = badTerminalReward;
// currentReward -= 100;
        double[] x = new double[] { previousEnergy.ordinal(), previousDistance.ordinal(), previousAction.ordinal() };
        m.train(x, computeQ(currentReward));
// Update for print needed here
        printState();
        System.out.println(m.visits(x));
    }
    /**
     * onHitByBullet: What to do when you're hit by a bullet
     */
// --------------------- Instant BAD rewards
    public void onHitByBullet(HitByBulletEvent e) {
        currentReward = badInstantReward;
// currentReward -= 30;
    }
    public void onBulletMissed(BulletMissedEvent event) {
        currentReward = badInstantReward;
// currentReward -= 10;
    }
    public void onHitWall(HitWallEvent e) {
        currentReward = badInstantReward;
// currentReward -= 20;
    }
    // -------------------- Instant GOOD Rewards
    public void onBulletHit(BulletHitEvent e) {
        currentReward = goodInstantReward;
        numBulletHits[(getRoundNum() - 1) / 100]++;
// currentReward += 30;
    }
    public void onHitRobot(HitRobotEvent e) {
        currentReward = goodInstantReward;
// double a;
// a = normalizeAngle(getHeading() - getGunHeading() + enemyBearing);
// turnGunRight(a);
// fire(2);
// currentReward += 10;
    }
    public void onBattleEnded(BattleEndedEvent event) {
        m.PrintVisit();
        System.out.println("logging......");
        logfile(sOutFile);
        logLUTfile(sLUTFile);
        System.out.println("Bye.....");
    }
    public void logLUTfile(File OutFile) {
        try {
            System.out.println("LUT is being logged..");
            RobocodeFileOutputStream fileOut = new RobocodeFileOutputStream(OutFile);
            PrintStream out = new PrintStream(new BufferedOutputStream(fileOut));
            //m.PrintLUT();
            //out.format("| i | j | k | = Q-Value | Visits\n");
            for (int i = 0; i < m.Dim1Levels; i++) {
                for (int j = 0; j < m.Dim2Levels; j++) {
                    for (int k = 0; k < m.Dim3Levels; k++) {
                        out.format("| %d | %d | %d | = %f | %d \n", i, j, k, m.LUT[i][j][k], m.visits[i][j][k]);
                    }
                }
            }
            System.out.println("\n");
            out.close();
            fileOut.close();
        }
        catch (IOException exception) {
            exception.printStackTrace();
        }
    }
    public void logfile(File OutFile) {

        try {
            m.PrintVisit();
            System.out.println("OutFile is being logged..");
            RobocodeFileOutputStream fileOut = new RobocodeFileOutputStream(OutFile);
            PrintStream out = new PrintStream(new BufferedOutputStream(fileOut));

            out.format("gamma: %2.2f\n", gamma);
            out.format("alpha: %2.2f\n", alpha);
            out.format("epsilon: %2.2f\n", epsilon);
            out.format("badInstantReward: %2.2f\n", badInstantReward);
            out.format("badTerminalReward: %2.2f\n", badTerminalReward);
            out.format("goodInstantReward: %2.2f\n", goodInstantReward);
            out.format("goodTerminalReward: %2.2f\n", goodTerminalReward);
            out.format("\nRounds/100, Wins,\n");
            for (int i = 0; i <= getRoundNum() / 100; i++) {
// out.format("%d, %d,\n", i + 1, mNumWinArray[i]);
                out.format("%d, %d, %d,\n", i + 1, mNumWinArray[i], numBulletHits[i]);
            }
            System.out.println("\n");
            out.close();
            fileOut.close();
        }
        catch (IOException exception) {
            exception.printStackTrace();
        }
    }
}
