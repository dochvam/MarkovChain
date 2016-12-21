/**
 * An implementation of a MarkovChain for generating sentences based on the sentences in a text file.
 *
 * @author <a href="mailto:bg5087a@american.edu">Ben Goldstein</a>
 * @version 1.0
 */


import java.util.*;
import java.lang.StringBuilder;
import java.io.*;


public class MarkovChain {

     /**
     * Reads one text file in from StandardIn and builds it into a single large string.
     *
     * @throws Exception when StandardIn is empty
     * @return a string version of StandardIn
     */
	public static String readInText() throws Exception{
		StringBuilder builder = new StringBuilder();
		Scanner scan = new Scanner(System.in);

        if (!scan.hasNext()) throw new Exception();

		while (scan.hasNext()){
			builder.append(scan.next()+" ");
		}
		String returnable = builder.toString();

		return returnable;
	}

    /**
     * Converts a HashMap of items and their counts into a HashMap of those items and their probabilites.
     *      Does this by iterating through the map and dividing each number of occurances by the total count of possible occurences
     * 
     * @param hm is a HashMap with String keys and Integer values representing the number of times that the String occurred.
     * @return a HashMap of Strings and Floats giving probabilities of each String occurring.
     */
    public static HashMap<String, Float> computeProbabilities(HashMap<String, Integer> hm){

        int totalCount = 0;

        Iterator iter = hm.entrySet().iterator();
        while (iter.hasNext()){
            Map.Entry pair = (Map.Entry)iter.next();
            int x = (int) pair.getValue();
            totalCount += x;
        }

        HashMap<String, Float> probabilitySet = new HashMap<>();

        iter = hm.entrySet().iterator();
        while (iter.hasNext()){
            Map.Entry pair = (Map.Entry)iter.next();
            String entry = (String) pair.getKey();

            int x = (int) pair.getValue();

            float prob = (float) x / totalCount;

            probabilitySet.put(entry, prob);
            iter.remove();
        }

        return probabilitySet;
    }

     /**
     * Randomly selects a String from a HashMap that indicates the probability each word will be chosen. Non-deterministic.
     *
     * @param hm is a HashMap of Strings and Floats, as generated by computeProbabilities().
     * @return the randomly selected string.
     */
    public static String chooseWord(HashMap<String, Float> hm){
        if (hm == null) return "";
        float probSum = 0;
        double r = Math.random();
        Iterator probIter = hm.entrySet().iterator();
        String chosenWord = "";
        while (r > probSum){
            Map.Entry pair = (Map.Entry)probIter.next();
            float thisProb = (float) pair.getValue();
            chosenWord = (String) pair.getKey();
            probSum += thisProb;
        }
        return chosenWord;
    }

    /**
     * For printing out generated sentences. Takes an ArrayList of Strings and formats it as one big String, 
     *      with punctuation correctly placed.
     *
     * @param sentence The ArrayList you want to reformat
     *
     * @return the String format of your ArrayList.
     */
    public static String turnToSentence(ArrayList<String> sentence){

        int howManyWords = sentence.size();

        StringBuilder strngSent = new StringBuilder();

        strngSent.append(sentence.get(0));

        for (int i = 1; i<howManyWords; i++){
            String temp = sentence.get(i);
            if (temp.equals("\n")) break;
            else if (temp.equals(",") || temp.equals("(") || temp.equals(")") || temp.equals(";") 
                    || temp.equals(":") || temp.equals("-") || temp.equals(".")){
                strngSent.append(temp);
            }
            else strngSent.append(" " + temp);
        }

        return strngSent.toString();
    }


 	public static void main(String args[]){

        int howMany = 1;
        int ppLength = 10;

        if (args.length > 0) howMany = Integer.parseInt(args[0]);
        if (args.length > 1) ppLength = Integer.parseInt(args[1]);

 		String fullText = "";

        boolean done = false;

        //get the whole board; catch when empty. I did this to try to read in multiple .txt files. I don't think it worked.
        while (!done){
            try {
                fullText += readInText();
            } catch (Exception ignored) {
                done = true;
            }
        }

        // Below, we convert our text file into an array of ArrayLists, each holding the separate words of a single sentence.
        // I did words.remove(0) to address an issue I was having where every sentence would begin with an empty string, which I think
        // was an artifact of the splitting process.

        String[] fullTextByWords = fullText.split("\\s+|(?=\\p{Punct})|(?<=,\\p{Punct})");
        System.out.println(fullTextByWords.length);

    	String[] sentences = fullText.split("[\\.\\!\\?]");
    	ArrayList<String>[] wordsBySen =  (ArrayList<String>[])new ArrayList[sentences.length]; //is there a better way?

    	for (int i = 0; i<sentences.length; i++){
    		ArrayList<String> words = new ArrayList<String>(Arrays.asList(sentences[i].split("\\s+|(?=\\p{Punct})|(?<=,\\p{Punct})")));
    		if (words.size() > 0) words.remove(0);
    		words.add("\n");
    		wordsBySen[i] = words;
    		// if (i<100) System.out.println(words);
    	}



        //Now we create a HashMap of the every first word of a sentence and how often it shows up in that position.

    	HashMap<String, Integer> sentenceStarterCounts = new HashMap<>();

    	for (int i = 0; i<wordsBySen.length; i++){
    		String thisWord = "";
    		boolean noGoodWord = true;
    		int count = 0;
    		while (noGoodWord){
    			thisWord = wordsBySen[i].get(count);
    			if (thisWord == ""){
    				System.out.println("caught one!");
    				count++;
    			} else noGoodWord = false;
    		}
    		if (sentenceStarterCounts.containsKey(thisWord)){
    			sentenceStarterCounts.put(thisWord, sentenceStarterCounts.get(thisWord)+1);
    		} else sentenceStarterCounts.put(thisWord, 1);
    	}
    	// System.out.println(sentenceStarterCounts);
    	// System.out.println("Number of diff starters: " + sentenceStarterCounts.size());



        // This next giant block is a series of creating counts and numbers of words that follow things we've already counted.
        // First, we look at every word that follows a sentence starter (basically every second word) and how often it does so.
        // Then we do this with every word following a pair of words above. Then we do this with every triple that occurs in the document
        // and the word that follows it. Tuples and Triples are stored as ArrayLists of Strings of length 2 and 3, respectively.
        // I couldn't figure out how to code this once and have it do it for any ArrayList length, so I manually coded each step.


    	HashMap<String, HashMap<String, Integer>> wordsFollowingSingles = new HashMap<>();
    	HashMap<ArrayList<String>, HashMap<String, Integer>> wordsFollowingPairs = new HashMap<>();
    	HashMap<ArrayList<String>, HashMap<String, Integer>> wordsFollowingTriples = new HashMap<>();

        for (int i = 0; i<wordsBySen.length; i++){
            ArrayList<String> sentence = wordsBySen[i];
            boolean noGoodWord = true;
            int counter = 0;
            if (counter > sentence.size()-3) noGoodWord = false;
            while(noGoodWord){
                String s = sentence.get(counter);
                if (sentenceStarterCounts.containsKey(s)){
                    // System.out.println(s);
                    noGoodWord = false;
                    String s2 = sentence.get(counter+1);
                    if (wordsFollowingSingles.containsKey(s)){
                        HashMap<String, Integer> hm = wordsFollowingSingles.get(s);
                        if (hm.containsKey(s2)){
                            hm.put(s2, hm.get(s2)+1);
                        } else hm.put(s2, 1);
                        wordsFollowingSingles.put(s, hm);
                    } else {
                        HashMap<String, Integer> hm = new HashMap<>();
                        hm.put(s2, 1);
                        wordsFollowingSingles.put(s, hm);
                    }

                    ArrayList<String> tempTuple = new ArrayList<>();
                    tempTuple.add(s);
                    tempTuple.add(s2);
                    String s3 = sentence.get(counter+2);

                    if (wordsFollowingPairs.containsKey(tempTuple)){
                        HashMap<String, Integer> hm = wordsFollowingPairs.get(tempTuple);
                        if (hm.containsKey(s3)) hm.put(s3, hm.get(s3) + 1);
                        else hm.put(s3, 1);
                        wordsFollowingPairs.put(tempTuple, hm);
                    } else {
                        HashMap<String, Integer> hm = new HashMap<>();
                        hm.put(s3, 1);
                        wordsFollowingPairs.put(tempTuple, hm);
                    }
                }
                counter++;
            }
        }

        for (int i = 0; i<fullTextByWords.length-3; i++){
            ArrayList<String> tempTriple = new ArrayList<>();
            tempTriple.add(fullTextByWords[i]);

            // if (!tempTriple.get(0).equals(".")){

            tempTriple.add(fullTextByWords[i+1]);
            tempTriple.add(fullTextByWords[i+2]);

            String s4 = fullTextByWords[i+3];

            if (wordsFollowingTriples.containsKey(tempTriple)){
                HashMap<String, Integer> hm = wordsFollowingTriples.get(tempTriple);
                if (hm.containsKey(s4)) hm.put(s4, hm.get(s4) + 1);
                else hm.put(s4,1);
                wordsFollowingTriples.put(tempTriple, hm);
            } else {
                HashMap<String, Integer> hm = new HashMap<>();
                hm.put(s4, 1);
                wordsFollowingTriples.put(tempTriple, hm);
            }
            // }
        }


        // Now we have 4 HashMaps. The first stores sentence starters and their counts. The next 3 store words or
        // lists of words, paired with the HashMaps that describe what follows them. We will now create a HashMap storing 
        // probability (rather than absolute frequency) for every String, Integer HashMap. By writing a method that computes this,
        // the only thing I had to code here was the iteration and creation of the parallel HashMaps.


        HashMap<String, Float> sentenceStarterProbabilities = computeProbabilities(sentenceStarterCounts);

        HashMap<String, HashMap<String, Float>> secondWordProbabilities = new HashMap<>();
        HashMap<ArrayList<String>, HashMap<String, Float>> thirdWordProbabilities = new HashMap<>();
        HashMap<ArrayList<String>, HashMap<String, Float>> fourthOrMoreProbabilities = new HashMap<>();

        Iterator iter2nd = wordsFollowingSingles.entrySet().iterator();
        while (iter2nd.hasNext()){
            Map.Entry pair = (Map.Entry) iter2nd.next();
            HashMap<String, Integer> thisWordCounts = (HashMap<String, Integer>) pair.getValue();
            HashMap<String, Float> thisWordProbs = computeProbabilities(thisWordCounts);
            String thisWord = (String) pair.getKey();
            secondWordProbabilities.put(thisWord, thisWordProbs);
        }

        Iterator iter3rd = wordsFollowingPairs.entrySet().iterator();
        while (iter3rd.hasNext()){
            Map.Entry pair = (Map.Entry) iter3rd.next();
            HashMap<String, Integer> thisWordCounts = (HashMap<String, Integer>) pair.getValue();
            HashMap<String, Float> thisWordProbs = computeProbabilities(thisWordCounts);
            ArrayList<String> theseWords = (ArrayList<String>) pair.getKey();
            thirdWordProbabilities.put(theseWords, thisWordProbs);
        }

        Iterator iter4th = wordsFollowingTriples.entrySet().iterator();
        while (iter4th.hasNext()){
            Map.Entry pair = (Map.Entry) iter4th.next();
            HashMap<String, Integer> thisWordCounts = (HashMap<String, Integer>) pair.getValue();
            HashMap<String, Float> thisWordProbs = computeProbabilities(thisWordCounts);
            ArrayList<String> theseWords = (ArrayList<String>) pair.getKey();
            fourthOrMoreProbabilities.put(theseWords, thisWordProbs);
        }


        // depending on howMany sentences we want, we'll do this a number of times. We start by choosing a starter
        // word. Then we choose a word from the words that can follow that one, then from the words that can follow
        // that pair. Once we have 3 words, we do this over and over again on the last 3 words until we hit a period
        // (marking the end of a sentence). Then we format it and print it out.

        for (int i = 0; i < howMany; i++){

            ArrayList<String> mySentence = new ArrayList<>();

            String firstWord = chooseWord(sentenceStarterProbabilities);
            mySentence.add(firstWord);

            String secondWord = chooseWord(secondWordProbabilities.get(firstWord));
            mySentence.add(secondWord);

            ArrayList<String> upToThreeWords = new ArrayList<>();
            upToThreeWords.add(firstWord);
            upToThreeWords.add(secondWord);

            String thirdWord = chooseWord(thirdWordProbabilities.get(upToThreeWords));
            upToThreeWords.add(thirdWord);
            mySentence.add(thirdWord);

            String nextWord = "~";
            int sentenceCount = 0;

            // while (!nextWord.equals(".") && !nextWord.equals("!") && !nextWord.equals("\n") && !nextWord.equals("!") && nextWord != ""){
            while (sentenceCount < ppLength){
                nextWord = chooseWord(fourthOrMoreProbabilities.get(upToThreeWords));
                upToThreeWords.remove(0);
                upToThreeWords.add(nextWord);
                mySentence.add(nextWord);

                if (nextWord.equals(".")) sentenceCount++;

                if (mySentence.size() > 2000) break;
            }

            System.out.println(turnToSentence(mySentence) + '\n');

        }
	}
}