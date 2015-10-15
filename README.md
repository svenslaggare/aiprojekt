# Instructions

To be able to compile and run:
* Java JDK 8 (javac and java commands).
* make and curl commands.
* Connected to the internet (to download the corpus)
* Alot of RAM (~8 GB) to be able to preprocess the data.

## Build and preprocess
Run "make init" command. This should take about 5 minutes.

## Run the prediction program
Run "make run". The program should take ~10 sec to start.

## Run the prediction program with grammar check enabled
Run "make run-grammar". The program should take ~10 sec to start.

## Generate results
There are three types of results that can be runned:
"make eval-nogrammar"
"make eval-grammar"
"make eval-userlearning"
This takes about 30 mins to complete.

