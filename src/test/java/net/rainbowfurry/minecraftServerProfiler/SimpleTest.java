package net.rainbowfurry.minecraftServerProfiler;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class SimpleTest {

    @Test
    public void testSimpleAssertion() {
        // A very simple test to verify the test setup works
        assertTrue(true, "This simple assertion should always pass");
    }

    @Test
    public void testMath() {
        // Another simple test
        assertEquals(4, 2 + 2, "2 + 2 should equal 4");
    }
}
