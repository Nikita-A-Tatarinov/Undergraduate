package matrix;

import com.code_intelligence.jazzer.api.FuzzedDataProvider;
import org.jblas.DoubleMatrix;
import org.jblas.Solve;

public class SolveFuzzer {
    public static void fuzzerTestOneInput(FuzzedDataProvider data) {
        try {
            DoubleMatrix A = DoubleMatrix.randn(data.consumeInt(), data.consumeInt());
            DoubleMatrix b = DoubleMatrix.rand(data.consumeInt(), data.consumeInt());
            Solve.solve(A, b);
        } catch (NegativeArraySizeException | OutOfMemoryError e) {
        }
    }
}
