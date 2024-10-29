package com.example.site;



import org.testng.annotations.Test;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertTrue;

class ParserTest {

    @Test
    public void testValidExpressions() {
        assertTrue(new Parser("(2+1)*2").parseExpr());
        assertTrue(new Parser("((2+1)-(3-4))").parseExpr());
        assertTrue(new Parser("(2)").parseExpr());
        assertTrue(new Parser("-2").parseExpr());
        assertTrue(new Parser("-(2+3)").parseExpr());
        assertTrue(new Parser("2 + /* comment */ 3").parseExpr());
        assertTrue(new Parser("2 + 3 // end of line comment").parseExpr());
        assertTrue(new Parser("/* multi-line\ncomment */ 2 + 3").parseExpr());
    }

    @Test
    public void testInvalidExpressions() {
        assertFalse(new Parser("((").parseExpr());
        assertFalse(new Parser("(2)+").parseExpr());
        assertFalse(new Parser("2 + / 3").parseExpr());
        assertFalse(new Parser(")2 + 3(").parseExpr());
    }

    @Test
    public void testUnaryMinus() {
        assertTrue(new Parser("-2").parseExpr());
        assertTrue(new Parser("-(2)").parseExpr());
        assertTrue(new Parser("-(2+3)").parseExpr());
        assertTrue(new Parser("-(2+3)*4").parseExpr());
    }
}
