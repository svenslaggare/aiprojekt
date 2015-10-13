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

## Generate results
There are four types of results that can be runned:
"make eval-bigdump"
"make eval-nogrammar"
"make eval-grammar"
"make eval-userlearning"
This takes about 1 hour to complete.

