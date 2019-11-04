import java.util.Scanner;

/**
 * knight's tour problem solving class
 */
public class KnightsTour {
    private int n;  // chess board size
    private int nn; // square count in chess board
    private int[][] board; // chess board array
    private int[] cx; // production array of x
    private int[] cy; // production array of y
    private boolean exists;
    private static long CURRENT_ITERATION;
//    private static int CURRENT_MOVES;

    /**
     * knight's tour problem solving class constructor
     * @param n size of the chess board
     */
    public KnightsTour(int n) {
        this.n = n;
        this.nn = n*n;
        this.board = new int[n][n];
        this.cx = new int[8];
        this.cy = new int[8];
        this.exists = false;

        // 1. building production array
        cx[0] = 2;
        cx[1] = 1;
        cx[2] = -1;
        cx[3] = -2;
        cx[4] = -2;
        cx[5] = -1;
        cx[6] = 1;
        cx[7] = 2;

        cy[0] = 1;
        cy[1] = 2;
        cy[2] = 2;
        cy[3] = 1;
        cy[4] = -1;
        cy[5] = -2;
        cy[6] = -2;
        cy[7] = -1;

        initializeGDB();

    }

    private void initializeGDB() {
        // initializing GDB
        for (int i = 0; i < n; ++i) {
            for(int j = 0; j < n; ++j) {
                board[i][j] = 0;
            }
        }
    }


    /**
     * tries to traverse the board with starting coordinates [x,y]
     * entered by the user and prints out whether a solution exists
     * @return result - is there an answer to Knight's Tour problem from the given coordinates
     */
    public boolean traverseBoard(boolean printSteps, boolean printLog) {
        int x = 0;
        int y = 0;
        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter starting x coordinate [1, " + n+1 + "]: ");
        while(!scanner.hasNextInt() || x < 1 || x > n) {
            System.out.println("False input. Enter values in range  [1, " + n+1 + "]");
            x = scanner.nextInt();
        }

        System.out.print("Enter starting y coordinate [1, " + n+1 + "]: ");
        while(!scanner.hasNextInt()) {
            System.out.println("False input. Enter values in range  [1, " + n+1 + "]");
            y = scanner.nextInt();
        }
        --x;
        --y;

        initializeGDB();
        this.exists = false;
        board[x][y] = 1;
        CURRENT_ITERATION = 0;

        if (printSteps)
            showDataInfo(1, x+1, y+1);

        if (printSteps)
            System.out.println("2 DALIS. Vykdymas");
        move(0, x, y, printLog);

        if (printSteps)
            printResults();

        return exists;
    }


    /**
     * tries to traverse the board with given starting coordinates [x,y]
     * and prints out whether a solution exists
     * @param x starting coordinate
     * @param y starting coordinate
     * @return result - is there an answer to Knight's Tour problem from the given coordinates
     */
    public boolean traverseBoard(int x, int y, boolean printSteps, boolean printLog) {
        if (x < 1 || x > n || y < 1 || y > n) {
            System.err.println("ERROR. Entered false coordinates");
            return false;
        }

        --x;
        --y;

        initializeGDB();
        this.exists = false;
        board[x][y] = 1;
        CURRENT_ITERATION = 0;

        if (printSteps)
            showDataInfo(1, x+1, y+1);

        if (printSteps)
            System.out.println("2 DALIS. Vykdymas");

        move(1, x, y, printLog);

        if (printSteps) {
            printResults();
            System.out.println();
        }

        return exists;
    }

    /**
     * prints the board with each squared marked:
     * 0 if knight's tour is not possible from those coordinates
     * 1 if knight's tour is possible from those coordinates
     */
    public void printAllPossibleTours() {
        boolean[][] tourExists = new boolean[n][n];
        for(int i = 0; i < n; ++i) {
            for (int j = 0; j < n; ++j) {
                tourExists[i][j] = false;
            }
        }

        for(int i = 0; i < n; ++i) {
            for(int j = 0; j < n; ++j) {
                if (traverseBoard(i+1, j+1, true, false)) {
                    tourExists[i][j] = true;
                } else {
                    tourExists[i][j] = false;
                }
            }
        }

        printBooleanBoard(tourExists);
    }

    /**
     * shows info of the first part of the procedure
     * @param l current move index
     * @param x starting x coordinate of the knight
     * @param y starting y coordinate of the knight
     */
    private void showDataInfo(int l, int x, int y) {
        System.out.println("1 DALIS. Duomenys");
        System.out.println("\t1) Lenta " + n + "x" + n);
        System.out.println("\t2) Pradinė žirgo padėtis X=" + x + ", Y=" + y + ". L = " + l);
    }

    /**
     * Print the results of knight's tour
     */
    private void printResults() {
        System.out.println("3 DALIS. Rezultatai");
        if (exists) {
            System.out.println("\t1) Apėjimas rastas. Bandymų " + CURRENT_ITERATION);
            System.out.println("\t2) Apėjimo pseudografika");
            System.out.println();
            printBoard();
        } else {
            System.out.println("\t1) Apėjimas nerastas");
        }
    }

    /**
     * prints the current board path indexes
     */
    private void printBoard() {
        System.out.print("Y,\tV ^\n");
        for(int i = n-1; i >= 0; --i) {
            System.out.format("\t" + (i+1) + " |");
            for(int j = 0; j < n; ++j) {
                System.out.format(" %2d", board[i][j]);
            }
            System.out.println();
        }
        printBoardBottom();
    }

    private void printBooleanBoard(boolean[][] boolBoard) {
        System.out.print("Y,\tV ^\n");
        for(int i = n-1; i >= 0; --i) {
            System.out.format("\t" + (i+1) + " |");
            for(int j = 0; j < n; ++j) {
                if (boolBoard[i][j]) {
                    System.out.format(" %2d", 1);
                } else {
                    System.out.format(" %2d", 0);

                }
            }
            System.out.println();
        }
        printBoardBottom();
    }

    private void printBoardBottom() {
        System.out.print("\t  -");
        for(int i = 0; i < n; ++i) {
            System.out.print("---");
        }
        System.out.print("> X, U\n");
        System.out.print("\t   ");
        for(int i = 0; i < n; ++i) {
            System.out.format(" %2d", (i+1));
        }
        System.out.println();
    }

    /**
     * moves a knight in the chess board
     * @param l move index
     * @param x latter knight x coordinate
     * @param y latter knight y coordinate
     */
    private void move(int l, int x, int y, boolean printLog) {
        if (l == nn) {
            exists = true;
            return;
        }

        ++l;
        int k = 0;

        while (!exists && k < 8) {
            ++CURRENT_ITERATION;
            int u = x + cx[k];
            int v = y + cy[k];

            StringBuilder sb = new StringBuilder();

            if (printLog) {
                sb.append("\t").append(CURRENT_ITERATION).append(") ");
                sb.append("-".repeat(Math.max(0, l - 1)));
                sb.append("R").append(k + 1).append(". ").append("U=")
                        .append(u+1).append(", V=").append(v+1)
                        .append(". L=").append(l).append(". ");
            }


            if (isCorrect(u, v)) {
                if (board[u][v] == 0) { // check's if knight already was in this square
                    board[u][v] = l;

                    if (printLog) {
                        sb.append("Laisva. LENTA[").append(u + 1)
                                .append(",").append(v + 1).append("]:=")
                                .append(l + 1).append(".");

                        System.out.println(sb.toString());
                    }

                    move(l, u, v, printLog);

                } else if (printLog) {
                    sb.append("Siūlas.");
                    System.out.println(sb.toString());
                }
            } else if (printLog) {
                sb.append("Už krašto.");
                System.out.println(sb.toString());
            }

            ++k;
        }

        if (!exists) {
            board[x][y] = 0;
        }
    }

    /**
     * checks whether the move is correct (doesn't exceed board bounds)
     * @param u x coordinate that's checked
     * @param v y coordinate that's checked
     * @return boolean - true if move is correct; false otherwise
     */
    private boolean isCorrect(int u, int v) {
        return (u >= 0 && u < n && v >= 0 && v < n);
    }
}
