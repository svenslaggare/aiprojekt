CLASSPATH=libraries/commons-math3-3.5.jar:libraries/stanford-postagger.jar
EVALUATOR=java -Xmx4g -classpath $(CLASSPATH):bin/ aiprojekt.Evaluator

init: build download-data preprocess-data

run:
	java -classpath $(CLASSPATH):bin/ aiprojekt.PredictionGUI

build:
	mkdir -p bin
	javac -classpath $(CLASSPATH) src/aiprojekt/*.java -d bin

download-data:
	curl "https://dl.dropboxusercontent.com/u/4940720/big_dump_logs.zip" -o "big_dump_logs.zip"
	unzip big_dump_logs.zip -d res/chatlogs

preprocess-data:
	java -Xmx8g -classpath $(CLASSPATH):bin/ aiprojekt.PreProcessor

clean-output:
	rm -fr res/evaluation/output
	mkdir res/evaluation/output

clean-testing:
	rm -fr res/evaluation/user_input_testing
	mkdir res/evaluation/user_input_testing

eval-bigdump: clean-output clean-testing
	cp "res/evaluation/2006-12-01-#ubuntu.txt" "res/evaluation/user_input_testing/2006-12-01-#ubuntu.txt"
	cp "res/evaluation/2006-12-02-#ubuntu.txt" "res/evaluation/user_input_testing/2006-12-02-#ubuntu.txt"
	cp "res/evaluation/2006-12-03-#ubuntu.txt" "res/evaluation/user_input_testing/2006-12-03-#ubuntu.txt"
	cp "res/evaluation/2006-12-04-#ubuntu.txt" "res/evaluation/user_input_testing/2006-12-04-#ubuntu.txt"
	cp "res/evaluation/2006-12-05-#ubuntu.txt" "res/evaluation/user_input_testing/2006-12-05-#ubuntu.txt"
	cp "res/evaluation/2006-12-06-#ubuntu.txt" "res/evaluation/user_input_testing/2006-12-06-#ubuntu.txt"
	cp "res/evaluation/2006-12-07-#ubuntu.txt" "res/evaluation/user_input_testing/2006-12-07-#ubuntu.txt"
	cp "res/evaluation/2006-12-08-#ubuntu.txt" "res/evaluation/user_input_testing/2006-12-08-#ubuntu.txt"
	cp "res/evaluation/2006-12-09-#ubuntu.txt" "res/evaluation/user_input_testing/2006-12-09-#ubuntu.txt"
	$(EVALUATOR) bigdump
	cp -avr "res/evaluation/output" "res/evaluation/bigdump-output"

eval-nogrammar: clean-output clean-testing
	cp "res/evaluation/2006-12-01-#ubuntu-short.txt" "res/evaluation/user_input_testing/2006-12-01-#ubuntu.txt"
	$(EVALUATOR) nogrammar
	cp -avr "res/evaluation/output" "res/evaluation/nogrammar-output"

eval-grammar: clean-output clean-testing
	cp "res/evaluation/2006-12-01-#ubuntu-short.txt" "res/evaluation/user_input_testing/2006-12-01-#ubuntu.txt"
	$(EVALUATOR) grammar
	cp -avr "res/evaluation/output" "res/evaluation/grammar-output"

eval-userlearning: clean-output clean-testing
	cp "res/evaluation/2006-12-01-#ubuntu.txt" "res/evaluation/user_input_testing/2006-12-01-#ubuntu.txt"
	cp "res/evaluation/2006-12-02-#ubuntu.txt" "res/evaluation/user_input_testing/2006-12-02-#ubuntu.txt"
	cp "res/evaluation/2006-12-03-#ubuntu.txt" "res/evaluation/user_input_testing/2006-12-03-#ubuntu.txt"
	cp "res/evaluation/2006-12-04-#ubuntu.txt" "res/evaluation/user_input_testing/2006-12-04-#ubuntu.txt"
	cp "res/evaluation/2006-12-05-#ubuntu.txt" "res/evaluation/user_input_testing/2006-12-05-#ubuntu.txt"
	cp "res/evaluation/2006-12-06-#ubuntu.txt" "res/evaluation/user_input_testing/2006-12-06-#ubuntu.txt"
	cp "res/evaluation/2006-12-07-#ubuntu.txt" "res/evaluation/user_input_testing/2006-12-07-#ubuntu.txt"
	cp "res/evaluation/2006-12-08-#ubuntu.txt" "res/evaluation/user_input_testing/2006-12-08-#ubuntu.txt"
	cp "res/evaluation/2006-12-09-#ubuntu.txt" "res/evaluation/user_input_testing/2006-12-09-#ubuntu.txt"
	cp "res/evaluation/2006-12-10-#ubuntu.txt" "res/evaluation/user_input_testing/2006-12-10-#ubuntu.txt"
	cp "res/evaluation/2006-12-11-#ubuntu.txt" "res/evaluation/user_input_testing/2006-12-11-#ubuntu.txt"
	cp "res/evaluation/2006-12-12-#ubuntu.txt" "res/evaluation/user_input_testing/2006-12-12-#ubuntu.txt"
	cp "res/evaluation/2006-12-13-#ubuntu.txt" "res/evaluation/user_input_testing/2006-12-13-#ubuntu.txt"
	cp "res/evaluation/2006-12-14-#ubuntu.txt" "res/evaluation/user_input_testing/2006-12-14-#ubuntu.txt"
	cp "res/evaluation/2006-12-15-#ubuntu.txt" "res/evaluation/user_input_testing/2006-12-15-#ubuntu.txt"
	cp "res/evaluation/2006-12-16-#ubuntu.txt" "res/evaluation/user_input_testing/2006-12-16-#ubuntu.txt"
	cp "res/evaluation/2006-12-17-#ubuntu.txt" "res/evaluation/user_input_testing/2006-12-17-#ubuntu.txt"
	cp "res/evaluation/2006-12-18-#ubuntu.txt" "res/evaluation/user_input_testing/2006-12-18-#ubuntu.txt"
	cp "res/evaluation/2006-12-19-#ubuntu.txt" "res/evaluation/user_input_testing/2006-12-19-#ubuntu.txt"
	cp "res/evaluation/2006-12-20-#ubuntu.txt" "res/evaluation/user_input_testing/2006-12-20-#ubuntu.txt"
	$(EVALUATOR) userlearning
	cp -avr "res/evaluation/output" "res/evaluation/userlearning-output"

eval: eval-bigdump eval-nogrammar eval-grammar eval-userlearning
