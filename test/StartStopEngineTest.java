//import hyspirit.engines.*;
//
//class StartStopEngineTest {
//    public static void main (String[] argv) {
//	HyInferenceEngine hy_pool = new HyPOOLEngine();
//	HyInferenceEngine hy_fvpd = new HyFVPDatalogEngine();
//	HyInferenceEngine hy_pd   = new HyPDatalogEngine();
//	HyInferenceEngine hy_psql = new HyPSQLEngine();
//	HyInferenceEngine hy_pra  = new HyPRAEngine();
//	
//	HyInferenceEngine[] engines = new HyInferenceEngine[5];
//	engines[0] = hy_pool;
//	engines[1] = hy_fvpd;
//	engines[2] = hy_pd;
//	engines[3] = hy_psql;
//	engines[4] = hy_pra;
//	hy_pool.addFile("test.pool");
//	hy_fvpd.addFile("test.fvpd");
//	hy_pd.addFile("test.pd");
//	hy_psql.addFile("test.psql");
//	hy_pra.addFile("test.pra");
//	
//	for (int i = 0; i < 0; i++) {
//	    System.out.println("StartStopEngineTest: run " + engines[i].cmd());
//	    engines[i].run();
//	}
//	for (int i = 0; i < 5; i++) {
//	    System.out.println("StartStopEngineTest: start " + engines[i].cmd());
//	    engines[i].start();
//	}
//	for (int i = 0; i < 5; i++) {
//	    System.out.println("StartStopEngineTest: echo " + engines[i].cmd());
//	    engines[i].setDebug();
//	    engines[i].echo("hello");
//	    String line;
//	    while ((line = engines[i].readLine()) != null) {
//		System.out.println(line);
//	    }
//	}
//	for (int i = 0; i < 0; i++) {
//	    System.out.println("StartStopEngineTest: run " + engines[i].cmd());
//	    engines[i].run();
//	}
//    }
//}
