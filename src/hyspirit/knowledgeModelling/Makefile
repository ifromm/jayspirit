all::
	perl -w make-program POOL > HyPOOLProgram.java
	perl -w make-program FVPDatalog > HyFVPDatalogProgram.java
	perl -w make-program PDatalog > HyPDatalogProgram.java
	perl -w make-program PSQL > HyPSQLProgram.java
	perl -w make-program PRA > HyPRAProgram.java

SWQW::
	diff HyPOOLProgram.java HyFVPDatalogProgram.java
	diff HyPOOLProgram.java HyPDatalogProgram.java
	diff HyPOOLProgram.java HyPSQLProgram.java
	diff HyPOOLProgram.java HyPRAProgram.java
