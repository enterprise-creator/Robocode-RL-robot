import java.io.File;
import java.io.IOException;
/*The Look Up Table*/
public class LookUpTable implements LUTInterface {
    public double[][][] LUT;
    public int[][][] visits;
    public int Dim1Levels;
    public int Dim2Levels;
    public int Dim3Levels;
    // Constructor
    public LookUpTable(int Dim1Levels, int Dim2Levels, int Dim3Levels) {
        this.Dim1Levels = Dim1Levels;
        this.Dim2Levels = Dim2Levels;
        this.Dim3Levels = Dim3Levels;
        LUT = new double[Dim1Levels][Dim2Levels][Dim3Levels];
        visits = new int[Dim1Levels][Dim2Levels][Dim3Levels];
        this.initialiseLUT();
    }
    @Override
    public void initialiseLUT() {
        for (int i = 0; i < Dim1Levels; i++) {
            for (int j = 0; j < Dim2Levels; j++) {
                for (int k = 0; k < Dim3Levels; k++) {
                    LUT[i][j][k] = Math.random();
                    visits[i][j][k] = 0;
                }
            }
        }
    }
    public int visits(double[] x) throws ArrayIndexOutOfBoundsException {
        if (x.length != 3) {
            throw new ArrayIndexOutOfBoundsException();
        } else {
            int a = (int) x[0];
            int b = (int) x[1];
            int c = (int) x[2];
            return visits[a][b][c];
        }
    }
    @Override
    public double outputFor(double[] x) {
        if (x.length != 3) {
            throw new ArrayIndexOutOfBoundsException();
        } else {
            int a = (int) x[0];
            int b = (int) x[1];
            int c = (int) x[2];
            return LUT[a][b][c];
        }
    }
    @Override
    public double train(double[] x, double target) {
        if (x.length != 3) {
            throw new ArrayIndexOutOfBoundsException();
        } else {
            int a = (int) x[0];
            int b = (int) x[1];
            int c = (int) x[2];
            LUT[a][b][c] = target;
            visits[a][b][c]++;
        }
        return 0.0D;
    }
    public void PrintVisit() {
        System.out.println("| i | j | k | = Q-Value | Visits\n");
        for (int i = 0; i < Dim1Levels; i++) {
            for (int j = 0; j < Dim2Levels; j++) {
                for (int k = 0; k < Dim3Levels; k++) {
                    System.out.printf("| %d | %d | %d | = %f | %d \n", i, j, k, LUT[i][j][k], visits[i][j][k]);
                }
            }
        }
    }
    public void PrintLUT() {
        System.out.println("| i | j | k | = Q-Value\n");
        for (int i = 0; i < Dim1Levels; i++) {
            for (int j = 0; j < Dim2Levels; j++) {
                for (int k = 0; k < Dim3Levels; k++) {
                    System.out.printf("| %d | %d | %d | = %f | %d \n", i, j, k, LUT[i][j][k]);
                }
            }
        }
    }
    @Override
    public void save(File argFile) {
    }
    @Override
    public void load(String argFileName) throws IOException {
// TODO Auto-generated method stub
    }
    @Override
    public int indexFor(double[] X) {
// TODO Auto-generated method stub
        return 0;
    }
}
