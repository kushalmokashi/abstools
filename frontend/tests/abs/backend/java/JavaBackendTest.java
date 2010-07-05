package abs.backend.java;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.io.PrintWriter;

import junit.framework.Assert;

import org.junit.Test;

import abs.frontend.ast.Model;
import abs.frontend.parser.Main;

public class JavaBackendTest {
    
    @Test
    public void testEmptyBlock() {
        assertValid("{ }");
    }

    @Test
    public void testEmptyClass() {
        assertEqual("class A { }", "class A implements ABSClassType { }");
    }
    
    @Test
    public void testEmptyInterface() {
        assertEqual("interface A { }", "interface A extends ABSInterfaceType { }");
    }
    
    @Test
    public void testInterfaceExtend() {
        assertEqual("interface A { } interface B extends A { }", 
                "interface A extends ABSInterfaceType { } "+
                "interface B extends ABSInterfaceType, A { }");
    }

    @Test
    public void testClassOneInterface() {
        assertEqual("interface I { } class C implements I { }", 
                    "interface I extends ABSInterfaceType { } "+
                    "class C implements ABSClassType, I { }");
    }

    @Test
    public void testVarDeclInterface() {
        assertValid("interface I { } { I x; }");
    }

    @Test
    public void testEmptyStmt() {
        assertValid("{ ; }");
    }
    
    @Test
    public void testNullLit() {
        assertValid("interface I { } { I i; i = null; }");
    }

    @Test
    public void testBoolLit() {
        assertValid("data Bool {  } { Bool b; b = True; b = False; }");
    }

    @Test
    public void testBoolNeg() {
        assertValid("data Bool {  } { Bool b; b = ~True; }");
    }
    
    @Test
    public void testBoolAnd() {
        assertValid("data Bool {  } { Bool b; b = True && False; }");
    }

    @Test
    public void testBoolOr() {
        assertValid("data Bool {  } { Bool b; b = True || False; }");
    }

    @Test
    public void testBoolEq() {
        assertValid("data Bool {  } { Bool b; b = True == False; }");
    }

    @Test
    public void testBoolNotEq() {
        assertValid("data Bool {  } { Bool b; b = True != False; }");
    }
    
    
    @Test
    public void testIntLit() {
        assertValid("data Int { } { Int i; i = 5; }");
    }

    @Test
    public void testNegativeIntLit() {
        assertValid("data Int { } { Int i; i = -7; }");
    }

    @Test
    public void testLongIntLit() {
        assertValid("data Int { } { Int i; i = 534023840238420394820394823094; }");
    }
    
    @Test
    public void testIntAddOps() {
        assertValid("data Int { } { Int i; i = 5 + -7; }");
    }
    
    @Test
    public void testIntSubtractOps() {
        assertValid("data Int { } { Int i; i = 7 - 5; }");
    }

    @Test
    public void testIntMultiplyOps() {
        assertValid("data Int { } { Int i; i = 7 * 5; }");
    }

    @Test
    public void testIntDivideOps() {
        assertValid("data Int { } { Int i; i = 7 / 5; }");
    }

    @Test
    public void testIntModOps() {
        assertValid("data Int { } { Int i; i = 7 % 5; }");
    }
    
    @Test
    public void testIntCompareOps() {
        assertValid("data Int { } data Bool { } { Bool b; b = 7 == 5; }");
        assertValid("data Int { } data Bool { } { Bool b; b = 7 != 5; }");
        assertValid("data Int { } data Bool { } { Bool b; b = 7 > 5; }");
        assertValid("data Int { } data Bool { } { Bool b; b = 7 < 5; }");
        assertValid("data Int { } data Bool { } { Bool b; b = 7 >= 5; }");
        assertValid("data Int { } data Bool { } { Bool b; b = 7 <= 5; }");
    }

    
    @Test
    public void testStringLit() {
//        assertValid("data String { } { String s; s = \"Test\"; }");
    }
    

    void assertEqual(String absCode, String javaCode) {
        assertEqual(absCode, javaCode,null);
    }
    
    void assertValid(String absCode) {
        assertValidJava(getJavaCode(absCode));
    }
    
    void assertValidJava(String javaCode) {
        File tmpFile;
        try {
            tmpFile = getTempFile(javaCode);
            JavaCompiler.compile("-classpath","bin", "-d", "gen/test", tmpFile.getAbsolutePath());
        } catch (Exception e) {
            Assert.fail(e.getMessage());
        }
    }
    
    String getJavaCode(String absCode) {
        try {
        InputStream in = getInputStream(absCode);
        Model model = null;
        try {
            model = Main.parse(in);
        } catch (Exception e) {
            Assert.fail(e.getMessage());
            return null;
        }
        
        if (model.hasErrors()) {
            Assert.fail(model.getErrors().getFirst().getMsgString());
            return null;
        }
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        model.generateJava(new PrintStream(out));
        String res = out.toString();
        res = res.replace('\n', ' ');
        res = res.replaceAll("[ ]+", " ");
        res = res.trim();
        return res;
        } catch (NumberFormatException e) {
            Assert.fail(e.getMessage());
            return null;
        }
    }
    
    void assertEqual(String absCode, String javaCode, String pkg) {
        try {
            StringBuffer expectedJavaCode = new StringBuffer();
            if (pkg != null) {
                expectedJavaCode.append("package "+pkg+"; ");
            }
            
            expectedJavaCode.append(JavaBackendConstants.LIB_IMPORT_STATEMENT+" ");
            expectedJavaCode.append(javaCode);
            String generatedJavaCode = getJavaCode(absCode);
            Assert.assertEquals(expectedJavaCode.toString(), generatedJavaCode);
            
            assertValidJava(generatedJavaCode);
            
        } catch (Exception e) {
            Assert.fail(e.getMessage());
        }
    }

    private InputStream getInputStream(String absCode) {
        return new ByteArrayInputStream(absCode.getBytes());
    }
    
    private static File getTempFile(String testCode) throws IOException {
        File tmpFile = File.createTempFile("abs", "test");
        PrintWriter p = new PrintWriter(new FileOutputStream(tmpFile));
        p.print(testCode);
        p.close();
        tmpFile.deleteOnExit();
        
        return tmpFile;
    }
    
}
