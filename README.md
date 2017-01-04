#Markov Chain Text Generator

This program implements a Markov Chain to create a paragraph of text from an input. Its output is fresh text generated based on the probability of each word following the words before it.

###Details
The chain is implemented mostly through HashMaps and ArrayLists. It has a memory depth of 3 words, so each chosen word is based only on the three before it.

###How to execute
Call MarkovChain.java. Input a .txt file containing your sample book or books; I've included manybooks.txt, a collection of classic works from Project Gutenberg. The input should look like
    ```
    [terminal] MarkovChain < manybooks.txt
    ```