find src -name *.java > sources.txt
mkdir -p bin

javac -d bin @sources.txt -classpath lib/commons-logging-1.2.jar:lib/log4j-1.2.17.jar
cp src/log4j.xml bin/
