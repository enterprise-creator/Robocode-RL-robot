import robocode.RobocodeFileOutputStream;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;


public class LookUp implements LUTInterface {
    public double[][][] table;
    public int[][][] visits;
    public int dim1;
    public int dim2;
    public int dim3;



    public LookUp(int Dim1, int Dim2, int Dim3) {
        this.dim1 = Dim1;
        this.dim2 = Dim2;
        this.dim3 = Dim3;
        table = new double[Dim1][Dim2][Dim3];
        visits = new int[Dim1][Dim2][Dim3];
        this.initialiseLUT();
    }
    @Override
    public void initialiseLUT() {
        for (int i = 0; i < dim1; i++) {
            for (int j = 0; j < dim2; j++) {
                for (int k = 0; k < dim3; k++) {
                    table[i][j][k] = Math.random();
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
            return table[a][b][c];
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
            table[a][b][c] = target;
            visits[a][b][c]++;
        }
        return 0.0D;
    }



    @Override
    public void save(File argFile) {
        try {
            System.out.println("LUT is being logged..");
            RobocodeFileOutputStream fileOut = new RobocodeFileOutputStream(argFile);
            PrintStream out = new PrintStream(new BufferedOutputStream(fileOut));

            //out.format("| i | j | k | = Q-Value | Visits\n");
            for (int i = 0; i < dim1; i++) {
                for (int j = 0; j < dim2; j++) {
                    for (int k = 0; k < dim3; k++) {
                        out.format("| %d | %d | %d | = %f | %d \n", i, j, k, table[i][j][k], visits[i][j][k]);
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
