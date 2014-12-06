test:: envExceptionTest
envExceptionTest::
	java RunEngineTest

test:: RunEngineTest
RunEngineTest::
	javac $@.java
	java -DHYSPIRIT=$(HYSPIRIT) $@

test:: RunProgramTest
RunProgramTest::
	javac $@.java
	java -DHYSPIRIT=$(HYSPIRIT) $@
