package example.org;

import org.junit.*;
import static org.junit.Assert.*;

public class CalcTest{
    private Calc calc;

    @Before
    public void setUp(){
        calc = new Calc();
    }

    @Test
    public void testAdd(){
        assertEquals(calc.add(1, 1), 2);
    }

    @Test
    public void testMul(){
        assertEquals(calc.add(2, 2), 4);
    }
}