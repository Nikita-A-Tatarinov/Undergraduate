package calc;

import com.code_intelligence.jazzer.api.FuzzedDataProvider;

public class CalcFuzzer {
    public static void fuzzerTestOneInput(FuzzedDataProvider data) {
        try {
            Calc.calculate(data.consumeRemainingAsAsciiString());
        } catch (CalcException e) {
        }
    }
}
