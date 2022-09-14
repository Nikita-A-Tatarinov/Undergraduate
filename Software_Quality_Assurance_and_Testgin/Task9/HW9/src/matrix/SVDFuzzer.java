package matrix;

import com.code_intelligence.jazzer.api.FuzzedDataProvider;
import org.jblas.DoubleMatrix;
import org.jblas.Singular;

public class SVDFuzzer {
    public static void fuzzerTestOneInput(FuzzedDataProvider data) {
        try {
            DoubleMatrix matrix = DoubleMatrix.randn(data.consumeInt(), data.consumeInt());
            Singular.fullSVD(matrix);
        } catch (NegativeArraySizeException | OutOfMemoryError e) {
        }
    }
}
