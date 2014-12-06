//import apriorie.knowledgeBase.HyKB;
//import apriorie.engines.*;
//
//class KBTest {
//    
//    public static void test1 () {
//	System.out.println("test1: load doc to KB and retrieve");
//        HyAnalysisEngine hy_data2kb = new HyText2KBEngine();
//	hy_data2kb.kb("HyKB");
//	hy_data2kb.context("KBTest.java");
//	hy_data2kb.runAndWait("KBtest.java");
//	
//	HyInferenceEngine hy_psql = new HyPSQLEngine();
//	hy_psql.kb("HyKB");
//	//hy_psql.run(System.err, "CREATE TABLE term(term, context);");
//	//System.err.println("TODO: Pass CREATE TABLE (term,context); to the engine and catch the error.");
//	//hy_psql.run("SELECT * FROM term;");
//	String line;
//	while ((line = hy_psql.readLine()) != null) {
//	    System.out.println(line);
//	}
//    }
//    public static void test2 () {
//	System.out.println("test2: retrieve from KB");
//	HyEngine hy_psql = new HyPSQLEngine();
//	hy_psql.setVerbose();
//	hy_psql.kb("HyKB");
//	hy_psql.run(System.err, "CREATE TABLE term(term, context);");
//	hy_psql.run(System.out, "SELECT * FROM term;");
//	hy_psql.run("SELECT * FROM term;");
//	String line;
//	while ((line = hy_psql.readLine()) != null) {
//	    System.out.println(line);
//	}
//    }
//    public static void test3 () {
//	System.err.println("test3");
//	HyKB kb = new HyKB("news");
//	kb.addDocument("XYZ");
//	kb.addDocument("KBTest.java");
//	kb.inferenceEngine().runProgram(System.out, "SELECT * FROM term;");
//    }
//    
//    public static void main (String[] argv) {
//	//test1();
//	//test2();
//	test3();
//    }
//}
