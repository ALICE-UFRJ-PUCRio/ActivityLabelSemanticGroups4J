# ActivityLabelSemanticGroups4J

Code used on my Master Thesis. Provides semantic similarity clustering of business process activity labels.

To Run the code you must have installed:

JavaSE-1.7

*** Instructions for running via command line ***

Execute the following command:

java -jar alsg.jar [Directory Path] [Level] [MinThreshold]

Example: java -jar alsg.jar /home/user/myAnalysis 2 0.25

Some explanations:

- You have to put a single text file named labels.txt with the activity labels, with one label per line.
- After running the results will be saved in a filled named output.txt
- Both labels.txt and output.txt will be created in the specified [Directory Path]

[Level] is an Integer value which specifies how many upper levels in Wordnet tree will be searched. The higher the value, the most abstract senses will be considered to match labels words.

[MinThreshold] is a Double value which limits the similarity value to be considered as minimum when matching similar activity labels.

