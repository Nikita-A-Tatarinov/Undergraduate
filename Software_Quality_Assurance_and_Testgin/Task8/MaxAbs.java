class MaxAbs {

    //@ ensures \result >= 0;
    //@ ensures (\result == a || \result == -a);
    public static int abs(int a) {
        //@ assume Integer.MIN_VALUE <= -a <= Integer.MAX_VALUE;
        return a >= 0 ? a : -a;
    }

    //@ ensures \result == a || \result == b;
    //@ ensures \result >= a && \result >= b;
    public static int max(int a, int b) {
        return a > b ? a : b;
    }

    //@ requires arr != null && arr.length > 0;
    //@ ensures \forall int i; 0 <= i < arr.length; \result >= arr[i] && \result >= -arr[i];
    //@ ensures \exists int i; 0 <= i < arr.length; \result == arr[i] || \result == -arr[i];
    public static int findMaxAbs(int[] arr) {
        int maxAbs = abs(arr[0]);
        //@ loop_invariant 0 <= i && i <= arr.length;
        //@ loop_invariant \forall int j; 0 <= j && j < i; maxAbs >= arr[j] && maxAbs >= -arr[j];
        //@ loop_invariant \exists int j; 0 <= j && j < arr.length; maxAbs == arr[j] || maxAbs == -arr[j];
        for (int i = 0; i < arr.length; i++) {
            maxAbs = max(maxAbs, abs(arr[i]));
        }
        return maxAbs;
    }
}