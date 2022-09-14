package matrix;

import com.code_intelligence.jazzer.api.FuzzedDataProvider;
import org.jblas.DoubleMatrix;


public class CreateMatrixFuzzer {
    public static void fuzzerTestOneInput(FuzzedDataProvider data) {
        try {
            DoubleMatrix matrix = DoubleMatrix.randn(data.consumeInt(), data.consumeInt());
        } catch (NegativeArraySizeException | OutOfMemoryError e) {
        }
    }
}
