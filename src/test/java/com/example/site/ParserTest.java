//package com.example.site;
//
//import org.junit.jupiter.api.Test;
//
//import static org.junit.jupiter.api.Assertions.assertFalse;
//import static org.junit.jupiter.api.Assertions.assertTrue;
//
//class ParserTest {
//
//    @Test
//    void testValidExpressions() {
//        assertTrue(new Parser("(2+1)*2").parseExpr());
//        assertTrue(new Parser("((2+1)-(3-4))").parseExpr());
//        assertTrue(new Parser("(2)").parseExpr());
//        assertTrue(new Parser("-2").parseExpr());
//        assertTrue(new Parser("-(2+3)").parseExpr());
//        assertTrue(new Parser("2 + /* comment */ 3").parseExpr());
//        assertTrue(new Parser("2 + 3 // end of line comment").parseExpr());
//        assertTrue(new Parser("/* multi-line\ncomment */ 2 + 3").parseExpr());
//    }
//
//    @Test
//    void testInvalidExpressions() {
//        assertFalse(new Parser("((").parseExpr());
//        assertFalse(new Parser("(2)+").parseExpr());
//        assertFalse(new Parser("2 + / 3").parseExpr());
//        assertFalse(new Parser(")2 + 3(").parseExpr());
//    }
//
//    @Test
//    void testUnaryMinus() {
//        assertTrue(new Parser("-2").parseExpr());
//        assertTrue(new Parser("-(2)").parseExpr());
//        assertTrue(new Parser("-(2+3)").parseExpr());
//        assertTrue(new Parser("-(2+3)*4").parseExpr());
//    }
//}
