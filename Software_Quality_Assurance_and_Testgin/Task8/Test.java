class Test {

    //@ ensures \result == a || \result == b;
    //@ ensures \result >= a && \result >= b;
    public static int max(int a, int b) {
        return a > b ? a : b;
    }

    //@ ensures \result == a || \result == b;
    //@ ensures \result <= a && \result <= b;
    public static int min(int a, int b) {
        return a < b ? a : b;
    }

    //@ requires array != null && array.length > 0;
    //@ requires \forall int n; 0 <= n < array.length; array[n] != null && array[n].length != 0;
    /*@ ensures \exists int n; 0 <= n < array.length; (\exists int p; 0 <= p < array[n].length; \result == array[n][p] &&
            (\forall int q; 0 <= q < array[n].length; \result >= array[n][q]) &&
            (\forall int q; 0 <= q < array.length; (\exists int r; 0 <= r < array[q].length; \result <= array[q][r])));
    */
    public static int findMinAmongRowMaxes(int[][] array) {
        int minAmongMaxes = array[0][0];
        //@ loop_invariant 0 <= i && i <= array.length;
        /*@ loop_invariant \exists int l; 0 <= l && l < array.length; (\exists int m; 0 <= m && m < array[l].length; minAmongMaxes == array[l][m] &&
                (\forall int n; 0 <= n && n < array[l].length; minAmongMaxes >= array[l][n]));
        */
        //@ loop_invariant \forall int l; 0 <= l && l < i; (\exists int m; 0 <= m && m < array[l].length; minAmongMaxes <= array[l][m]);
        for (int i = 0; i < array.length; i++) {
            int maxInRow = array[i][0];
            //@ loop_invariant 0 <= j && j <= array[i].length;
            //@ loop_invariant \exists int k; 0 <= k && k < array[i].length; maxInRow == array[i][k];
            //@ loop_invariant \forall int k; 0 <= k && k < j; maxInRow >= array[i][k];
            for (int j = 0; j < array[i].length; j++) {
                maxInRow = max(maxInRow, array[i][j]);
            }
            minAmongMaxes = min(minAmongMaxes, maxInRow);
        }
        return minAmongMaxes;
    }
}