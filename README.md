# Instructions

To be able to compile and run:
* Java JDK 8 (javac and java commands).
* make and curl commands.
* Connected to the internet (to download the corpus)
* Alot of RAM (~8 GB) to be able to preprocess the data.

## Build and preprocess
Run "make init" command. This should take about 5 minutes.

## Build and fast preprocess
If you want to use a pre made n-gram file, rum "make init-fast".

## Run the prediction program
Run "make run". The program should take ~10 sec to start.

## Run the prediction program with grammar check enabled
Run "make run-grammar". The program should take ~10 sec to start.

## Generate results
There are three types of results that can be generated: 
"make eval-nogrammar"
"make eval-grammar"
"make eval-userlearning"
This takes about 20 mins to complete and the output is in the res/evaluation folder, where each type of result get an own result output folder.

