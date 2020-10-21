package jvm;

public class Calculation {
    public int add() {
        int count = 0;
        for (int i = 0; i <= 10; i++) {
            count = count + i;
        }
        return count;
    }

    public static void main(String[] args) {
        Calculation calculation = new Calculation();
        int add = calculation.add();
    }
}
