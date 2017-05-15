# Make file for Java

# Java compiler
JAVAC = javac

# Java compiler flags
JAVAFLAGS = -g -classpath ./src/ -d ./class

# Creating a .class file
COMPILE = $(JAVAC) $(JAVAFLAGS)

CLASS_FILES = src/SinglesOnly.class

all: $(CLASS_FILES)

%.class : %.java
	$(COMPILE) $<
