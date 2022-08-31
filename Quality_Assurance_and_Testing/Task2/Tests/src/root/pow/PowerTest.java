package root.pow;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PowerTest {

    static Power power = new Power();

    @Test
    void test1() {
        for (int a = -5; a < 6; a++) {
            for (int b = 0; b > -10; b--) {
                assertEquals(1, power.pow(a, b));
            }
        }
    }

    @Test
    void test2() {
        for (int b = 1; b < 11; b++) {
            assertEquals(0, power.pow(0, b));
        }
    }

    @Test
    void test3(){
        assertEquals(1, power.pow(1, 1));
        assertEquals(1, power.pow(1, 5));
        assertEquals(2, power.pow(2, 1));
        assertEquals(32, power.pow(2, 5));
    }

    @Test
    void test4(){
        assertEquals(0, power.pow(2, 31));
    }
}