import robocode.*;
import java.io.File;
import java.util.Random;
import robocode.RobocodeFileWriter;
import static robocode.util.Utils.normalRelativeAngleDegrees;
public class Robot2021 extends AdvancedRobot {
    public double
            reward,rewardHitByBullet,rewardHitRobot,rewardHitWall,rewardBulletHit;
    public static int maximumNumberOfRounds = 6000; /*Maximum number of
Battle rounds*/
    public static int countb = -1;
    public static int acttion = 0; /*Chosen action in Switch*/
    public static int newActionState = 0; /*Action state*/
    public static int bearingLevel = 6; /*Bearing Level*/
    public static int distanceLevel = 4; /*Distance counter*/
    public static int energyLevel = 4; /*Energy level*/
    public static int actionsLevel = 6; /*Action Level*/
    public static double LearningRate = 0.5; /*Learning Rate for the
algorithm*/
    public static double DiscountRate = 0.75; /*gamma value for RL*/
    public static double epsilonValue = 0.8; /*Greedliy Epislon Value*/
    public static int[] state,newState;
    public static int offPolicy = 0; /*Boolean Value if 1 then off policy is
used, if 0 then on policy is used*/
    public static int rewardInstant = 0;/*Instant Rewards, If 1 => Terminal
and Intermediate Rewards, otherwise Terminal only*/
    public static double rewardTerm; /*Terminal Rewards*/
    public static double[] histWin =new double[maximumNumberOfRounds];
    public static ScannedRobotEvent scannedRobot; /*Scanned robot on the
radar*/
    public static LUT lutTable = new LUT(distanceLevel , energyLevel,
            bearingLevel, actionsLevel); /*Intiliasing LUT Constructor*/
    public void run() {
        setAdjustRadarForGunTurn(true);
        setAdjustRadarForRobotTurn(true);
        setAdjustGunForRobotTurn(true);
        turnRadarRight(360);
        countb = countb + 1;
        if (getRoundNum() >= 3000){
 /*Updating the epsilon value after first 3000 rounds for
measureing the learning process*/
            epsilonValue = 0.001;
        }
        state = lutTable.dimensionQuantization(scannedRobot.getDistance(),
                scannedRobot.getEnergy(),
                scannedRobot.getBearing()); /*Dimension Quantisation to
reduce the size of LUt space*/
        while (true) {
            Random rand = new Random();
            int ran = 5 - 0 + 1;
            int randd = rand.nextInt(ran) + 0;
            turnRadarRight(360);
            rewardHitByBullet = 0;
            rewardHitRobot = 0;
            rewardHitWall = 0;
            rewardBulletHit = 0;
            rewardTerm=0;
            acttion =
                    lutTable.chooseAction(state[0],state[1],state[2],epsilonValue);
            this.takeAction(acttion);
            newState =
                    lutTable.dimensionQuantization(scannedRobot.getDistance(),
                            scannedRobot.getEnergy(),
                            scannedRobot.getBearing()); /*New state for the Q
Learning Process*/
 /*Checking if Off Policy or On Policy to be used according to
hyper parameters*/
            if (offPolicy ==1) {
                newActionState =
                        lutTable.chooseAction(newState[0],newState[1],newState[2],0);
            }
            else {
                /*this is on Policy Selection*/
                newActionState =
                        lutTable.chooseAction(newState[0],newState[1],newState[2],epsilonValue);
            }
 /*checking for terminal and non terminal, and terminal only
rewards*/
            if(rewardInstant==1) {
                reward = rewardHitByBullet + rewardHitRobot + rewardHitWall +
                        rewardBulletHit;
            }
            else {
                reward = rewardTerm;
            }
            this.updateLUT(state, acttion, newState, newActionState, reward);
            state = newState; /*Updating the State*/
        }
    }
    /*Updating the LUt*/
    public void updateLUT(int[] state, int action, int[] newState, int
            newaction, double reward ){
        double oldVal = lutTable.table[state[0]][state[1]][state[2]][action];
        double newVal =
                lutTable.table[newState[0]][newState[1]][newState[2]][newaction];
        double updatedVal = oldVal +
                LearningRate*(reward+DiscountRate*newVal-oldVal);
        lutTable.setValue(state[0], state[1], state[2], action,updatedVal);
    }
    /*Robocode Overdidden Functions*/
    public void onHitByBullet(HitByBulletEvent e) {
        rewardHitByBullet = -5 ;
    }
    public void onHitWall(HitWallEvent e) {
        rewardHitWall = -2;
    }
    public void onHitRobot(HitRobotEvent e){
        rewardHitRobot = -3;
    }
    public void onBulletHit(BulletHitEvent e){
        rewardBulletHit = 4 ;
    }
    /*What to do if robot is scanned on Radar*/
    public void onScannedRobot(ScannedRobotEvent e) {
        scannedRobot = e;
        double absBearing = getHeading() + e.getBearing();
        double bearingFromGun = normalRelativeAngleDegrees(absBearing -
                getGunHeading());
        if (Math.abs(bearingFromGun) <= 3) {
            turnGunRight(bearingFromGun);
            if (getGunHeat() == 0) {
                fire(Math.min(3 - Math.abs(bearingFromGun), getEnergy() -
                        .1));
            }
        }
        else {
            turnGunRight(bearingFromGun);
        }
        if (bearingFromGun == 0) {
            scan();
        }
    }
    public void onWin(WinEvent event){
        if(rewardInstant== 0)
        {
            rewardTerm =3;
        }
        histWin[countb] = 1.0;
    }
    public void onDeath(DeathEvent e)
    {
        if(rewardInstant== 0)
        {
            rewardTerm = -3;
        }
    }
    /*What happens at round end*/
    public void onRoundEnded (RoundEndedEvent event){
        out.println("End of the round");
        int ended_round = event.getRound();
        String file_name = "terminal_LUtt.csv";
        File fileLUT = getDataFile(file_name);
        save(fileLUT);
    }
    /*what happens when the whole battle ends*/
    @Override
    public void onBattleEnded (BattleEndedEvent event){
        System.out.println("End of the battle");
        String file_name = "terminal_battleResult.csv";
        File fileLUT = getDataFile(file_name);
        saveWINS(fileLUT);
        for(int i = 0; i < distanceLevel; ++i) {
            for(int j = 0; j < energyLevel; ++j) {
                for(int k = 0; k < bearingLevel; ++k) {
                    for(int l = 0; l < actionsLevel; ++l) {
                        System.out.println( i+"   "+ j+"   "+ k+"   " +lutTable.table[i][j][k][l]);
                    }
                }
            }
        }



    }
    /*Saving the lut table*/
    public void save(File argFile) {
        try{
            RobocodeFileWriter writer = new RobocodeFileWriter(argFile);
            for (int i = 0; i < distanceLevel; i++){
                for (int j = 0; j < energyLevel; j++){
                    for (int k = 0; k< bearingLevel;k++){
                        for(int l = 0; l< actionsLevel;l++){

                            System.out.println(Double.toString(lutTable.getValue(i, j, k, l))+"\r\n");
                            writer.write(Double.toString(lutTable.getValue(i, j, k, l))+"\r\n");
                        }
                    }
                }
            }
            writer.flush();
            writer.close();
        }catch (Exception e){
            out.println("exception in save function");
        }
    }
    /*Action Course for the tank in the Learning process*/
    public void takeAction(int counter){
        switch(counter){
            case (0): {
                ahead(90);
                break;
            }
            case (1): {
                back(90);
                break;
            }
            case (2): {
                setAhead(90);
                setTurnLeft(90);
                execute();
                break;
            }
            case (3): {
                setAhead(90);
                setTurnRight(90);
                execute();
                break;
            }
            case (4): {
                setBack(90);
                setTurnLeft(90);
                execute();
                break;
            }
            case (5): {
                setBack(90);
                setTurnRight(90);
                execute();
                break;
            }
        }
    }
    /*Saving the Win % per 100 rounds*/
    public void saveWINS(File argFile) {
        double totalWins = 0;
        int batch_size = 100; //this is hardcoded
        int win_rate_size = maximumNumberOfRounds/ batch_size;
        double[] winRate =new double[win_rate_size];
        double[] totalBattles = new double[maximumNumberOfRounds];
        for (int i = 0; i < win_rate_size; i++)
        {
            double sum =0;
            int initial_val = i*batch_size;
            for (int j= i*batch_size; j< (initial_val+batch_size); j++)
            {
                sum += histWin[j];
            }
            winRate[i] = sum/batch_size;
        }
        try{
            RobocodeFileWriter writer = new RobocodeFileWriter(argFile);
            for (int i = 0; i<win_rate_size; i++){
                writer.write(Double.toString(winRate[i])+ "\r\n");
            }
            writer.flush();
            writer.close();
        }catch (Exception e){
            out.println("exception in saveWinRate function");
        }
    }
    public void saveWinRates3(File argFile) {
        // Calculate the total win rate
        double totalWins = 0;
        int batch_size = 100; //this is hard coded
        int win_rate_size = maximumNumberOfRounds/ batch_size;
        double[] winRate =new double[win_rate_size];
        double[] totalBattles = new double[maximumNumberOfRounds];
        for (int i = 0; i < win_rate_size; i++)
        {
            double sum =0;
            int initial_val = i*batch_size;
            for (int j= i*batch_size; j< (initial_val+batch_size); j++)
            {
                sum += histWin[j];
            }
        }
        double[] cumwin =new double[win_rate_size];
        cumwin[0] = winRate[0]/batch_size;
        int todivide = batch_size;
        for (int i=1 ; i< win_rate_size; i++)
        {
            todivide += batch_size;
            cumwin[i] = (cumwin[i-1] + winRate[i])/ todivide;
        }
        try{
            RobocodeFileWriter writer = new RobocodeFileWriter(argFile);
            for (int i = 0; i<win_rate_size; i++){
                writer.write(Double.toString(cumwin[i])+ "\r\n");
            }
            writer.flush();
            writer.close();
        }catch (Exception e){
            out.println("exception in saveWinRate function");
        }
    }
}