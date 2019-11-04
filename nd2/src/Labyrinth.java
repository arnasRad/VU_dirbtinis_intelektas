import java.io.*;
import java.util.ArrayList;
import java.util.Scanner;

public class Labyrinth {

    private int m, n;
    private int[][] Labyrinth; // backup labyrinth array for resetting
    private int[][] L;
    private static final int[] CX = new int[] {-1, 0, 1, 0};
    private static final int[] CY = new int[] {0, -1, 0, 1};
    private boolean exists;
    private ArrayList<Integer> prodPath;
    private ArrayList<Vertex> vertexPath;

    private static int CURRENT_ITERATION;

    private FileWriter fileWriter;

    public Labyrinth() {
    }

    public boolean findPath() {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter input file name (without type extension): ");
        String inputFileName = scanner.nextLine();
        inputFileName = inputFileName.concat(".txt");

        if (!initializeLabyrinth(inputFileName))
            return false;

        System.out.println("Enter output file name (without type extension): ");
        String outputFileName = scanner.nextLine();
        outputFileName = outputFileName.concat(".txt");

        System.out.println("Enter backtrack option (1 for BACKTRACK_WITH_MEMORY, 2 for BACKTRACK1 and 3 for BACKTRACK): ");
        int backtrackOption = getIntInput(1, 3);

        printBoardConsole();

        System.out.println("Enter starting x coordinate: ");
        int x = getIntInput(1, n);

        System.out.println("Enter starting y coordinate: ");
        int y = getIntInput(1, m);

        return findPath(outputFileName, x-1, y-1, backtrackOption);
    }

    public boolean findPath(String inputFileName, String outputFileName, int x, int y, int backtrackOption) {
        if(!initializeLabyrinth(inputFileName))
            return false;

        return findPath(outputFileName, x-1, y-1, backtrackOption);
    }

    public boolean findPath(int[][] labyrinth, String outputFileName, int x, int y, int backtrackOption) {
        if(!initializeLabyrinth(labyrinth))
            return false;

        return findPath(outputFileName, x-1, y-1, backtrackOption);
    }

    private boolean findPath(String outputFileName, int x, int y, int backtrackOption) {
        if (backtrackOption < 1 || backtrackOption > 3) {
            System.err.println("ERROR: invalid backtrack option entered. Valid values are 1, 2 or 3");
            return false;
        }

        if (x < 1 || x > L[0].length || y < 1 || y > L.length) {
            System.err.println("ERROR: incorrect initial coordinates entered. Coordinates cannot exceed board boundaries.");
            return false;
        } else if (L[y][x] != 0) {
            System.err.println("ERROR: incorrect initial coordinates given. Value in initial coordinate must be equal to 0");
            return false;
        }

        try {
            fileWriter = new FileWriter(new File(outputFileName));
        } catch (IOException e) {
            e.printStackTrace();
        }

        showDataInfo(x+1, y+1, backtrackOption);

        try {
            fileWriter.write("2 DALIS. Vykdymas.\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
        this.L[y][x] = 2;
        boolean result = move(x, y, 2, backtrackOption);

        printResults();

        try {
            fileWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return result;
    }

    private boolean initializeLabyrinth(String fileName) {
        try {
            BufferedReader br = new BufferedReader(
                    new FileReader(new File(fileName)));

            String line = br.readLine();
            String[] numbers = line.split(" ");

            if (numbers.length != 2) {
                System.err.println("ERROR: first line in input file must contain " +
                        "two numbers - labyrinth dimensions separated by whitespace y x");
                return false;
            }

            // two first numbers of input file must be labyrinth dimensions
            this.n = Integer.parseInt(numbers[1]);
            this.m = Integer.parseInt(numbers[0]);
            this.Labyrinth = new int[this.m][this.n];
            this.L = new int[this.m][this.n];

            int i = m-1;
            int j;
            int num;
            // read the rest of the input
            while ((line = br.readLine()) != null) {
                numbers = line.split(" ");
                if (numbers.length != this.n) {
                    System.err.println("ERROR: incorrect count of numbers in input file line " + i);
                    return false;
                }

                j = 0;
                for (String input : numbers) {
                    num = Integer.parseInt(input);
                    this.Labyrinth[i][j] = num;
                    this.L[i][j] = num;
                    ++j;
                }
                --i;
            }

            this.exists = false;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

        setUtilFields();
        return true;
    }

    private boolean initializeLabyrinth(int[][] labyrinth) {
        if (labyrinth == null) {
            System.err.println("Invalid labyrinth input: NULL");
            return false;
        }

        this.m = labyrinth.length;
        this.n = labyrinth[0].length;
        this.Labyrinth = new int[m][n];
        this.L = new int[m][n];

        for(int i = 0; i < m; ++i) {
            System.arraycopy(labyrinth[i], 0, Labyrinth[i], 0, n);
            System.arraycopy(labyrinth[i], 0, L[i], 0, n);
        }

        this.exists = false;

        setUtilFields();
        return true;
    }

    private void setUtilFields() {
        CURRENT_ITERATION = 0;
        prodPath = new ArrayList<>();
        vertexPath = new ArrayList<>();

        for (int i = 0; i < m; ++i) {
            for (int j = 0; j < n; ++j) {
                L[i][j] = Labyrinth[i][j];
            }
        }
    }

    private void showDataInfo(int x, int y, int backtrackOption) {
        try {
            fileWriter.write("1 DALIS. Duomenys\n");
            fileWriter.write("\t1.1. Lenta " + n + "x" + m + "\n\n");
            printBoard();
            fileWriter.write("\n\t1.2. Pradinė keliautojo padėtis X=" +
                    x + ", Y=" + y + ". L=" + 2 + "\n\n");
            fileWriter.write("\t1.2. Naudojama prodedūra: ");
            if (backtrackOption == 1) {
                fileWriter.write("BACKTRACK_SU_ATMINTIMI\n\n");
            } else if (backtrackOption == 2) {
                fileWriter.write("BACKTRACK1\n\n");
            } else if (backtrackOption == 3) {
                fileWriter.write("BACKTRACK\n\n");
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void printResults() {
        try {
            fileWriter.write("\n3 DALIS. Rezultatai\n");
            if (exists) {
                fileWriter.write("\t1) Kelias rastas. Bandymų " + CURRENT_ITERATION + "\n");
                fileWriter.write("\t2) Kelias grafiškai\n");
                printBoard();
                fileWriter.write("\n3.3. Kelias taisyklėmis: " + getProdPath() + "\n");
                fileWriter.write("\n3.4. Kelias viršūnėmis: " + getVertexPath() + "\n");
            } else {
                fileWriter.write("\t1) Kelias neegzistuoja\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String getProdPath() {
        if (prodPath == null || prodPath.size() == 0) {
            return "";
        }

        StringBuilder sb = new StringBuilder();

        int i = 0;
        for(Integer prod : prodPath) {
            sb.append("R").append(prod+1).append(" -> ");
            ++i;

            if (i % 9 == 0) {
                sb.append("\n\t\t\t\t\t\t");
            }
        }
        String str = sb.toString();
        str = str.substring(0, str.length() - 4);

        return str;
    }

    private String getVertexPath() {
        if (vertexPath == null || vertexPath.size() == 0) {
            return "";
        }

        StringBuilder sb = new StringBuilder();

        int i = 0;
        for (Vertex vertex : vertexPath) {

            sb.append(vertex).append(" -> ");
            ++i;

            if (i % 4 == 0) {
                sb.append("\n\t\t\t\t\t\t");
            }
        }
        String str = sb.toString();
        str = str.substring(0, str.length() - 4);

        return str;
    }

    /**
     * prints the current board path indexes
     */
    private void printBoard() {
        try {
            fileWriter.write("Y,\tV   ^\n");
            for (int i = m - 1; i >= 0; --i) {
                fileWriter.write(String.format("\t%3d |", (i + 1)));
                for (int j = 0; j < n; ++j) {
                    fileWriter.write(String.format(" %2d", L[i][j]));
                }
                fileWriter.write("\n");
            }
            printBoardBottom();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private void printBoardConsole() {
        System.out.println("\nInput Board:");
        System.out.print("Y,\tV   ^\n");
        for (int i = m - 1; i >= 0; --i) {
            System.out.print(String.format("\t%3d |", (i + 1)));
            for (int j = 0; j < n; ++j) {
                System.out.print(String.format(" %2d", L[i][j]));
            }
            System.out.print("\n");
        }
        printBoardBottomConsole();
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

    private void printBoardBottomConsole() {
        System.out.print("\t    -");
        for (int i = 0; i < n; ++i) {
            System.out.print("---");
        }
        System.out.print("> X, U\n");
        System.out.print("\t     ");
        for (int i = 0; i < n; ++i) {
            System.out.print(String.format(" %2d", (i + 1)));
        }
        System.out.print("\n");
    }

    /**
     *
     * @param x move coordinate
     * @param y move coordinate
     * @param l current step
     * @param backtrackOption 1 - BACKTRACK_SU_ATMINTIMI, 2 - BACKTRACK1, 3 - BACKTRACK
     * @return
     */
    private boolean move(int x, int y, int l, int backtrackOption) {
        if (x == 0 || x == (n-1) || y == 0 || y == (m-1)) {
            vertexPath.add(new Vertex(x+1, y+1));
            this.exists = true;
            return true;
        }

        int k = 0;
        int u, v;
        while (!exists && k < 4) {
            ++CURRENT_ITERATION;
            u = x + CX[k];
            v = y + CY[k];

            StringBuilder sb = new StringBuilder();

            sb.append("\t").append(CURRENT_ITERATION).append(") ");
            sb.append("-".repeat(Math.max(0, l - 1)));
            sb.append("R").append(k + 1).append(". ").append("U=")
                    .append(u+1).append(", V=").append(v+1)
                    .append(". L=").append(l).append(". ");

            if (L[v][u] == 0) {

                sb.append("Laisva. LENTA[").append(u + 1)
                        .append(",").append(v + 1).append("]:=")
                        .append(l + 1).append(".");

                try {
                    fileWriter.write(sb.toString() + "\n");
                } catch (IOException e) {
                    e.printStackTrace();
                }

                ++l;
                if (backtrackOption == 1 || backtrackOption == 2)
                    L[v][u] = l;

                prodPath.add(k);
                vertexPath.add(new Vertex(x+1, y+1));

                move(u, v, l, backtrackOption);
                if (!exists) {
                    if (backtrackOption == 1)
                        L[v][u] = -1;
                    else if (backtrackOption == 2 || backtrackOption == 3)
                        L[v][u] = 0;

                    --l;
                    prodPath.remove(prodPath.size()-1);
                    vertexPath.remove(vertexPath.size()-1);
                }
            } else if (L[v][u] == 1) {
                sb.append("Siena.");
                try {
                    fileWriter.write(sb.toString() + "\n");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                sb.append("Siūlas.");
                try {
                    fileWriter.write(sb.toString() + "\n");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            ++k;
        }
        if (!exists) {
            try {
                fileWriter.write("\t\t" + "-".repeat(Math.max(0, l - 1)) +
                        "Backtrack iš X=" + (x+1) + ", Y=" + (y+1) +
                        ", L=" + l + ". LAB=[" + (x+1) + "," + (y+1) +
                        "]:=" + -1 + ". L:=" + (l - 1) + "\n");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    private int getIntInput() {
        Scanner scanner = new Scanner(System.in);

        while(!scanner.hasNextInt()) {
//            System.err.println("False input. Enter values in range  [1, " + (m+1) + "]");
            System.err.println("False input. Enter Integer: ");
            scanner.next();
        }

        return scanner.nextInt();
    }

    private int getIntInput(int min, int max) {
        Scanner scanner = new Scanner(System.in);

        int input = 0;
        while(scanner.hasNext()) {
            if (scanner.hasNextInt()) {
                input = scanner.nextInt();
                if (input < min || input > max) {
                    System.err.println("False input. Input numbers between " + min + " and " + max);
                } else {
                    return input;
                }
            } else {
                System.err.println("False input. Enter Integer: ");
                scanner.next();
            }
        }

        return -1;
    }
}
