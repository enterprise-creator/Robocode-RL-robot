import java.io.File;
import java.io.IOException;
import java.util.*;
public class LUT implements LUTInterface{
    public double [][][][] table; /*Lut table*/
    public int distanceFromEnemyState; /*Distance from the Enemy tank*/
    public int currentEnergyState;/* Current Energy state*/
    public int bearingState; /*Bearing Value */
    public int actions; /*Actions to be performed according to the policy*/
    /*Constructor Function*/
    /*Initialising the constructor function*/
    public LUT(int distanceState,int energyState,int bearingState,int
            actions){
        this.bearingState=bearingState;
        this.distanceFromEnemyState=distanceState;
        this.currentEnergyState=energyState;
        this.actions=actions;
        /*Initialising the LUT with Zeroes*/
        this.initialiseLUT();
    }
    @Override
    public void initialiseLUT() {
        this.table=new
                double[this.distanceFromEnemyState][this.currentEnergyState][this.bearingState][this.actions];
        for(int i=0;i<this.distanceFromEnemyState;i+=1){
            for(int j=0;j<this.currentEnergyState;j+=1){
                for(int k=0;k<this.bearingState;k+=1){
                    for(int l=0;l<this.actions;l+=1){
                        table[i][j][k][l]=0;
                    }
                }
            }
        }
    }
    public void setValue(int stateDistance,int stateEnergy, int stateBearing,
                         int actions,double newValue){
        table[stateDistance][stateEnergy][stateBearing][actions]=newValue;
    }
    public double getValue(int stateDistance,int stateEnergy, int
            stateBearing, int actions){
        return table[stateDistance][stateEnergy][stateBearing][actions];
    }
    /*Performing the dimenstion Quantisation for the LUT space Reduction*/
    public int[] dimensionQuantization(double stateDistance, double
            stateEnergy, double stateBearing){
        int result[]=new int[3];
        result[0]=this.distanceQuant(stateDistance);
        result[1]=this.energyQuant(stateEnergy);
        result[2]=this.bearingQuant(stateBearing);
        return result;
    }
    /*Performing the indvidual Quantisation of Parameters*/
    /*Bearing Quantisation*/
    public int bearingQuant(double e){
        double angle=360/this.bearingState;
        int result=(int)((180+e)/angle);
        if(result>=this.bearingState){
            result=0;
        }
        return result;
    }
    /*Distance Quantisation*/
    public int distanceQuant(double e){
        int distLevel=0;
        double distance =e;
        if(distance>0&&distance<=250){
            distLevel=0;
        }
        else if(distance>250&&distance<=500){
            distLevel=1;
        }
        else if (distance>500&&distance<=750){
            distLevel=2;
        }
        else if(distance>750&&distance<=1000){
            distLevel=3;
        }
        return distLevel;
    }
    /*Energy Quantisation*/
    public int energyQuant(double ennergy)
    {
        int levelOfEnergy = 0;
        double energy = ennergy;
        if (energy >= 0 && energy <= 25)
        {
            levelOfEnergy = 0;
        }
        else if (energy > 25 && energy <= 50)
        {
            levelOfEnergy = 1;
        }
        else if (energy > 50 && energy <= 75)
        {
            levelOfEnergy = 2;
        }
        else if (energy > 75 && energy <= 100)
        {
            levelOfEnergy = 3;
        }
        return levelOfEnergy;
    }
    public int maximumValue(int i, int j, int k){
        double max = -100;
        int counterr = 0;
        int actionTaken = 0;
        double current = 0.0;
        for (counterr = 0; counterr < this.actions; counterr++){
            current = this.getValue(i, j, k, counterr);
            if (current > max){
                max = current;
                actionTaken= counterr;
            }
        }
        return actionTaken;
    }
    /*Calculation of the Chosen Action for the learning course.*/
    public int chooseAction(int i, int j, int k, double epsilon){
        int result = 0;
        Random gen1 = new Random();
        Random gen2 = new Random();
        int temp = this.maximumValue(i, j, k);
        if(gen1.nextDouble() < epsilon){
            result = gen2.nextInt(this.actions);
        }
        else{
            result = this.maximumValue(i, j, k);
        }
        return result;
    }
    @Override
    public int indexFor(double[] X) {
        return 0;
    }
    @Override
    public double outputFor(double[] X) {
        return 0;
    }
    @Override
    public double train(double[] X, double argValue) {
        return argValue;
    }
    @Override
    public void save(File argFile) {
    }
    @Override
    public void load(String argFileName) throws IOException {
    }
}