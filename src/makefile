JAVAC = javac
JAVACFLAGS =
SRC = com/client/UDPclient.java \
      com/company/ClientMain.java \

CLS = $(SRC:.java=.class)

all: $(CLS) 


.SUFFIXES : .class .java
.java.class:
	$(JAVAC) $(JAVACFLAGS) $<

