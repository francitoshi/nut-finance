package io.nut.finance.xirr;

import static io.nut.finance.xirr.NewtonRaphson.TOLERANCE;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class NewtonRaphsonTest {

    @Test
    public void sqrt() throws Exception {
        NewtonRaphson nr = NewtonRaphson.builder()
            .withFunction(x -> x * x)
            .withDerivative(x -> 2 * x)
            .build();
        assertEquals(2, nr.inverse(4, 4), TOLERANCE);
        assertEquals(-3, nr.inverse(9, -9), TOLERANCE);
        assertEquals(25, nr.inverse(625, 625), TOLERANCE);
    }

    @Test
    public void cubeRoot() throws Exception {
        NewtonRaphson nr = NewtonRaphson.builder()
            .withFunction(x -> x * x * x)
            .withDerivative(x -> 3 * x * x)
            .build();
        assertEquals(2, nr.inverse(8, 8), TOLERANCE);
        assertEquals(-3, nr.inverse(-27, 27), TOLERANCE);
        assertEquals(25, nr.inverse(15_625, 15_625), TOLERANCE);
    }

    @Test
    public void quadratic() throws Exception {
        NewtonRaphson nr = NewtonRaphson.builder()
            .withFunction(x -> (x - 4) * (x + 3))
            .withDerivative(x -> 2 * x - 1)
            .build();
        assertEquals(4, nr.findRoot(10), TOLERANCE);
        assertEquals(-3, nr.findRoot(-10), TOLERANCE);
        // Inflection point when derivative is zero => x = 1/2
        assertEquals(4, nr.findRoot(.51), TOLERANCE);
        assertEquals(-3, nr.findRoot(.49), TOLERANCE);
    }

    @Test
    public void failToConverge() throws Exception 
    {
        try
        {
            NewtonRaphson nr = NewtonRaphson.builder()
                .withFunction(x -> (x - 4) * (x + 3))
                .withDerivative(x -> 2 * x - 1)
                .build();
            // Inflection point when derivative is zero => x = 1/2
            nr.findRoot(.5);
            fail("Expected non-convergence");
        }
        catch(ArithmeticException ex)
        {
        }
    }

    @Test
    public void failToConverge_iterations() throws Exception 
    {
        try
        {
            NewtonRaphson nr = NewtonRaphson.builder()
                .withFunction(x -> x * x)
                .withDerivative(x -> 1) // Wrong on purpose
                .build();
            nr.findRoot(Integer.MAX_VALUE);
            fail("Expected non-convergence");
        }
        catch(IllegalArgumentException ex)
        {
        }
    }

    @Test
    public void tolerance() throws Exception {
        final double tolerance = TOLERANCE/1000;
        NewtonRaphson nr = NewtonRaphson.builder()
            .withFunction(x -> x * x)
            .withDerivative(x -> 2 * x)
            .withTolerance(tolerance)
            .build();
        assertEquals(4, nr.inverse(16, 16), tolerance);
        assertEquals(15, nr.inverse(225, 225), tolerance);
        assertEquals(1.414_213_562_3, nr.inverse(2, 2), tolerance);
    }

}
