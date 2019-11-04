import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;

public class EightQueensPuzzle {

    private int n, nm1, n2m1;
    private int[] X;
    private boolean[] VERT, KYL, LEID;
    private boolean exists;

    private long CURRENT_ITERATION;
    
    private FileWriter fileWriter;

    public EightQueensPuzzle(int n) {
        this.n = n;
        this.nm1 = n - 1;
        this.n2m1 = 2*n-1;

        this.X = new int[n];
        this.VERT = new boolean[n];
        this.KYL = new boolean[2*nm1];
        this.LEID = new boolean[n2m1];
        
    }

    public boolean placeQueens(boolean printLog) {
        CURRENT_ITERATION = 0;

        Arrays.fill(X, 0);
        Arrays.fill(VERT, true);
        Arrays.fill(KYL, true);
        Arrays.fill(LEID, true);
        exists = false;

        try {
            fileWriter = new FileWriter(new File("output.txt"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        showDataInfo();

        try {
            fileWriter.write("2 DALIS. Vykdymas.\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
        boolean result = tryPlacing(1, printLog);

        printResults();

        try {
            fileWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return result;
    }

    private void showDataInfo() {
        try {
            fileWriter.write("1 DALIS. Duomenys\n");
            fileWriter.write("\t1) Lenta " + n + "x" + n + "\n\n");

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void printResults() {
        try {
            fileWriter.write("\n3 DALIS. Rezultatai\n");
            if (exists) {
                fileWriter.write("\t1) Sprendinys egzistuoja. Bandymų " + CURRENT_ITERATION + "\n");
                fileWriter.write("\t2) Apėjimo pseudografika\n");
                printBoard();
            } else {
                fileWriter.write("\t1) Sprendinys neegzistuoja\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * prints the current board path indexes
     */
    private void printBoard() {
        try {
            fileWriter.write("Y,\tV   ^\n");
            for (int i = n - 1; i >= 0; --i) {
                fileWriter.write(String.format("\t%3d |", (i + 1)));
                for (int j = 0; j < n; ++j) {
                    if (X[i] == j) {
                        fileWriter.write(String.format(" %2d", 1));
                    } else {
                        fileWriter.write(String.format(" %2d", 0));
                    }
                }
                fileWriter.write("\n");
            }
            printBoardBottom();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void printBoardBottom() {
        try {
            fileWriter.write("\t    -");
            for (int i = 0; i < n; ++i) {
                fileWriter.write("---");
            }
            fileWriter.write("> X, U\n");
            fileWriter.write("\t     ");
            for (int i = 0; i < n; ++i) {
                fileWriter.write(String.format(" %2d", (i + 1)));
            }
            fileWriter.write("\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private boolean tryPlacing(int i, boolean printLog) {
        int k = 0;
        int vertInd, kylInd, leidInd;

        while(k < n && !exists) {
            ++CURRENT_ITERATION;

            StringBuilder sb = new StringBuilder();

            if (printLog) {
                sb.append("\t").append(CURRENT_ITERATION).append(") ");
                sb.append("-".repeat(Math.max(0, i - 1)));
                sb.append(">").append(" I=").append(i)
                        .append(", K=").append(k).append(". ");
            }

            vertInd = k;
            kylInd = k-i+n;
            leidInd = i+k-1;
            if (VERT[vertInd] && KYL[kylInd] && LEID[leidInd]) {

                if (printLog) {
                    sb.append("Laisva. LENTA[").append(k+1)
                            .append(",").append(i).append("]:=")
                            .append(i).append(".");

                    try {
                        fileWriter.write(sb.toString() + "\n");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                X[i-1] = k;
                VERT[vertInd] = false;
                KYL[kylInd] = false;
                LEID[leidInd] = false;
                if (i < n) {
                    tryPlacing(i+1, printLog);
                    if (!exists) {
                        VERT[vertInd] = true;
                        KYL[kylInd] = true;
                        LEID[leidInd] = true;
                    }
                } else {
                    exists = true;
                    return true;
                }
            } else {
                sb.append("Užimta.");
                try {
                    fileWriter.write(sb.toString() + "\n");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            ++k;
        }

        return false;
    }
}
