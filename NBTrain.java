/**
 * This is the Assignment 1a of 10605
 * @author Hao Wang (haow2)
 */
import java.util.*;
import java.io.*;

public class NBTrain {
	
    private Map<String, Integer> mapInstanceY;
	private long countInstancesY;
	private Map<String, Integer> mapTokenYW;
	private Map<String, Integer> mapTokenY;
	private Set<String> setWord;
	private Set<String> setLabel;

    private final String LABEL_ENDS_WITH = "CAT";
	
	// Constructor
	public NBTrain () {
		mapInstanceY = new HashMap<String, Integer>();
		mapTokenYW = new HashMap<String, Integer>();
		mapTokenY = new HashMap<String, Integer>();
		setWord = new HashSet<String>();
		setLabel = new HashSet<String>();
		countInstancesY = 0;
	}

	private void streamReadTrain(String fileName) throws Exception {
		// Read training data
        File inFile = new File(fileName);
        
        // If file doesnt exists, then create it
        if (!inFile.exists()) {
            System.err.println("No file called: " + fileName);
            System.exit(-1);
        }

        // Read string from the input file
        BufferedReader br = null;
        String currLine;
        
        br = new BufferedReader(new FileReader(inFile));

        while ((currLine = br.readLine()) != null) {
            String[] ss = currLine.split("\t");
            if (ss.length != 2) {
            	continue;
            }

            String[] labels = ss[0].trim().split(",");
            // Traverse all labels
            for (int i = 0; i < labels.length; i++) {
            	String label = labels[i];
            	if (label.length() == 0) {
            		continue;
            	}

                // System.out.println("label: " + label);

                // Only remain selected labels for 1a
                if (!label.endsWith(LABEL_ENDS_WITH)) {
                    continue;
                }

        		// Check unique class
        		if (!setLabel.contains(label)) {
        			setLabel.add(label);
        		}
            	
            	// Increment #(Y=y) by 1
            	if (mapInstanceY.containsKey(label)) {
            		mapInstanceY.put(label, mapInstanceY.get(label) + 1);
            	} else {
            		mapInstanceY.put(label, 1);
            	}

            	// Increment #(Y=*) by 1
            	countInstancesY++;

            	String[] words = ss[1].split("\\s+");
            	int countWord = 0;
            	for (int j = 0; j < words.length; j++) {
            		String word = words[j].replaceAll("\\W", "");
            		if (word.length() == 0) {
            			continue;
            		}

                    // System.out.println("word: " + word);

            		// Increment #(Y=y,W=wj) by 1
        			String key = label + "\t" + word;
        			if (mapTokenYW.containsKey(key)) {
        				mapTokenYW.put(key, mapTokenYW.get(key) + 1);
        			} else {
        				mapTokenYW.put(key, 1);
        			}
        			countWord++;
            	}

            	// Increment #(Y=y,W=*) by countWord
            	if (mapTokenY.containsKey(label)) {
            		mapTokenY.put(label, mapTokenY.get(label) + countWord);
            	} else {
            		mapTokenY.put(label, countWord);
            	}
            }
        }
	}

	private void streamReadTest(String fileName) throws Exception {
		// Read test data
        File inFile = new File(fileName);
        
        // If file doesnt exists, then create it
        if (!inFile.exists()) {
            System.err.println("No file called: " + fileName);
            System.exit(-1);
        }

        // Read string from the input file
        BufferedReader br = null;
        String currLine;
        
        br = new BufferedReader(new FileReader(inFile));

        while ((currLine = br.readLine()) != null) {
            String[] ss = currLine.split("\t");
            if (ss.length != 2) {
            	continue;
            }

            String[] words = ss[1].trim().split("\\s+");
        	for (int j = 0; j < words.length; j++) {
        		String word = words[j].replaceAll("\\W", "");
        		if (word.length() == 0) {
        			continue;
        		}

        		// Check unique word
        		if (!setWord.contains(word)) {
        			setWord.add(word);
        		}
        	}
        }
	}

	private void smoothing(String fileName) throws Exception {
		// Read test data
        File inFile = new File(fileName);
        
        // If file doesnt exists, then create it
        if (!inFile.exists()) {
            System.err.println("No file called: " + fileName);
            System.exit(-1);
        }

        // Read string from the input file
        BufferedReader br = null;
        String currLine;
        
        br = new BufferedReader(new FileReader(inFile));

        long correct = 0;
        long overall = 0;
        while ((currLine = br.readLine()) != null) {
            String[] ss = currLine.split("\t");
            if (ss.length != 2) {
            	continue;
            }

            String[] labels = ss[0].trim().split(",");
            String maxLabel = "WRONG";
           	double maxP = 0.0;
            boolean maxHasInit = false;

            // Traverse all labels in the setLabel
            for (String label: setLabel) {
            	double currP = Math.log((1.0 + mapInstanceY.get(label)) / (0.0 + countInstancesY + setLabel.size()));
            	String[] words = ss[1].trim().split("\\s+");
	        	
                for (int j = 0; j < words.length; j++) {
	        		String word = words[j].replaceAll("\\W", "");
	        		if (word.length() == 0) {
	        			continue;
	        		}

	        		String key = label + "\t" + word;
	        		double countXY = 0.0;
	        		if (mapTokenYW.containsKey(key)) {
	        			countXY += mapTokenYW.get(key);
	        		}
	        		currP += Math.log((1.0 + countXY) / (0.0 + mapTokenY.get(label) + setWord.size()));
	        	}
                
                if (!maxHasInit) {
                    maxP = currP;
                    maxLabel = label;
                    maxHasInit = true;
                } else if (currP > maxP) {
                    maxP = currP;
                    maxLabel = label;
                }
                // System.out.println("boolean: " + (currP > maxP));
                // System.out.println("currLabel: " + label + "\tcurrP: " + currP);
            }
            // System.out.println("maxLabel: " + maxLabel + "\tmaxP: " + maxP);

            String printLine = "[";
            boolean hasOneLegalLabel = false;
            for (int i = 0; i < labels.length; i++) {
            	String label = labels[i];

            	printLine += label;
            	if (i != labels.length - 1) {
            		printLine += ", ";
            	}
            	if (label.length() == 0) {
            		continue;
            	}
                if (!label.endsWith(LABEL_ENDS_WITH)) {
                    continue;
                } else {
                    hasOneLegalLabel = true;
                }

            	if (label.equals(maxLabel)) {
            		correct++;
            	}
            }

            if (hasOneLegalLabel == false) {
                correct++;
            }

            overall++;
            printLine += "]\t" + maxLabel + "\t" + maxP;
            System.out.println(printLine);
        }
        System.out.println("Percent correct: " + correct + "/" + overall + "=" + (1.0 * correct) / overall);
	}

	public static void main(String[] args) throws Exception {
		 if (args == null || args.length != 2) {
		 	System.err.println("Illegal input args");
            System.exit(-1);
		 }

		 NBTrain train = new NBTrain();
		 train.streamReadTrain(args[0]);
		 train.streamReadTest(args[1]);
		 train.smoothing(args[1]);
         // System.out.println(train.mapInstanceY.size() + "\t" + train.countInstancesY + "\t" + train.mapTokenYW.size() + "\t" + train.mapTokenY.size() + "\t" + train.setWord.size() + "\t" + train.setLabel.size());

         // System.out.println(train.setWord.size() + "\t" + train.setLabel.size());
	}
}