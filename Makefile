AJA=$(HOME)/devel/Apriorie-Java-API

SOURCES+=engines/HyPSQLEngine.java
SOURCES+=engines/HyPDatalogEngine.java
SOURCES+=engines/HyFVPDatalogEngine.java
SOURCES+=engines/HyPOOLEngine.java
diff::
	for i in $(SOURCES); do \
	echo $$i; \
	diff $(AJA)/src/apriorie/$$i src/hyspirit/$$i; \
	done
