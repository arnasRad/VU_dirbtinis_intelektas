public class Main {
    public static void main(String[] args) {
        KnightsTour knightsTour = new KnightsTour(8);

        knightsTour.traverseBoard(1,1, true, false);
//        knightsTour.printAllPossibleTours();
    }
}
