import java.util.ArrayList;

public class TowersOfHanoi {
    private static int HB_ITERATION;
    private static ArrayList<Integer> A;
    private static ArrayList<Integer> B;
    private static ArrayList<Integer> C;

    public static void hb(char x, char y, char z, int n) {
        HB_ITERATION = 1;
        A = new ArrayList<>();
        B = new ArrayList<>();
        C = new ArrayList<>();
        for(int i = n; i > 0; --i) {
            A.add(i);
        }

        System.out.println("Pradinė būsena: A=" + A + ", B=" + B + ", C=" + C); // 2.

        hanoi_tower(x, y, z, n);
    }

    private static void hanoi_tower(char x, char y, char z, int n) {
        if (n > 0) {
            System.out.println("before first: " + n);
            hanoi_tower(x, z, y, n-1); // 1. Perkelti n-1 diską ant tarpinio
            moveDisk(n, x, z);
            System.out.println("after first: " + n);
            hanoi_tower(y, x, z, n-1); // 3. Perkelti n-1 diską ant tikslo
        }
    }

    private static void moveDisk(int n, char x, char z) {
        removeDisk(x);
        addDisk(z, n);
        System.out.println(HB_ITERATION++ + ". Diską " + n + " nuo " + x + " perkelti ant " + z +
                ". A=" + A + ", B=" + B + ", C=" + C); // 2.
    }

    private static void removeDisk(char x) {
        if (x == 'A') {
            A.remove(A.size()-1);
        } else if (x == 'B') {
            B.remove(B.size()-1);
        } else if (x == 'C') {
            C.remove(C.size()-1);
        }
    }

    private static void addDisk(char x, int n) {
        if (x == 'A') {
            A.add(n);
        } else if (x == 'B') {
            B.add(n);
        } else if (x == 'C') {
            C.add(n);
        }
    }
}
