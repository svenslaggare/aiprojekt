CLASSPATH=libraries/commons-math3-3.5.jar:libraries/stanford-postagger.jar

init: build download-data preprocess-data

run:
	java -classpath $(CLASSPATH):bin/ aiprojekt.PredictionGUI

build:
	mkdir -p bin
	javac -classpath $(CLASSPATH) src/aiprojekt/*.java -d bin

download-data:
	wget https://dl.dropboxusercontent.com/u/4940720/big_dump_logs.zip
	unzip big_dump_logs.zip -d res/chatlogs

preprocess-data:
	java -Xmx8g -classpath $(CLASSPATH):bin/ aiprojekt.PreProcessor
