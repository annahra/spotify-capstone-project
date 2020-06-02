package util;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.graphstream.graph.*;
import org.graphstream.graph.implementations.*;

public class GraphLoader{
	
	
	/**
	 * Loads a file of artists into a graph
	 * 
	 * The first line of the file contains all of the information about a given node:
	 * artist name, acousticness, danceability, duration_ms, energy, instrumentalness,
	 * liveness, loudness, speechiness, tempo, valence, popularity, key, node, count 
	 * 
	 * @param filename The file containing the graph
	 * @param graph The graph to be loaded
	 */
	public static void loadGraph(String filename, Graph graph)
	{
		BufferedReader reader = null;
        try {
            String nextLine;
            reader = new BufferedReader(new FileReader(filename));
            nextLine = reader.readLine();
            if (nextLine == null) {
            	reader.close();
            	throw new IOException("Graph file is empty!");
            }
         
            while ((nextLine = reader.readLine()) != null) {
            	//regex splits comma separated values while ignoring commas in double quotes (positive lookahead)
            	String[] input = nextLine.split(",(?=(?:(?:[^\"]*\"){2})*[^\"]*$)");
            	
            	Node n = graph.addNode(input[0]);
            	n.addAttribute("name", input[0]);
            	
            	addAttributes(n,input);
            	
            }
            reader.close();
            
            addEdges(graph);
        } catch (IOException e) {
            System.err.println("Problem loading graph file: " + filename);
            e.printStackTrace();
        }
	}
	
	/**
	 * Adds edges between certain nodes. Each node is categorized depending on the values
	 * of its features, each sub-categorization possibility is a different super-category.
	 * There are 128 in total for the subjective_classes super-category and 168 total for
	 * the objective_classes
	 * 
	 * @param graph The graph to be loaded
	 */
	private static void addEdges(Graph graph) {
		HashMap<String,LinkedList<Node>> submap = new HashMap<>();
		HashMap<String, LinkedList<Node>> objmap = new HashMap<>();
		
		for(Node n : graph.getEachNode()) {
			String key = (String)n.getAttribute("subjective_classes");
			if(submap.containsKey(key)){
				LinkedList<Node> nodes = submap.get(key);
				for(int idx=0; idx<nodes.size();idx++) {
					graph.addEdge( "sub "+(String)n.getAttribute("name")+" "+nodes.get(idx).getAttribute("name"), n, nodes.get(idx));
				}
			} else {
				LinkedList<Node> ll = new LinkedList<Node>();
				ll.add(n);
				submap.put(key, ll);
			}
			
			String objkey = (String)n.getAttribute("objective_classes");
			if(objmap.containsKey(objkey)) {
				LinkedList<Node> nodes = objmap.get(objkey);
				for(int idx=0;idx<nodes.size();idx++) {
					try {
						graph.addEdge("obj "+(String)n.getAttribute("name")+" "+nodes.get(idx).getAttribute("name"), n, nodes.get(idx));
					} catch(EdgeRejectedException e) {
						continue;
					}
				}
			} else {
				LinkedList<Node> ll = new LinkedList<Node>();
				ll.add(n);
				objmap.put(objkey, ll);
			}
		}
	}

	
	/**
	 * Adds a set of important attributes to a particular node to aid in
	 * creating edges between nodes. Each feature is split, so that nodes fall 
	 * under a certain category.
	 * 
	 * @param node The The node the attributes are being added to 
	 * @param input An array of strings corresponding to a line in the file
	 */
	private static void addAttributes(Node n, String[] input) {
		n.addAttribute("acousticness",input[1]);
		if(Float.parseFloat(input[1]) < 0.60) {
			n.addAttribute("acous_class", "A-");
		}
		else {
			n.addAttribute("acous_class", "A+");
		}
		
    	n.addAttribute("danceability",input[2]);
    	if(Float.parseFloat(input[2]) < 0.50) {
			n.addAttribute("dance_class", "D-");
		}
		else {
			n.addAttribute("dance_class", "D+");
		}
    	
    	n.addAttribute("duration_ms",input[3]);
    	
    	n.addAttribute("energy",input[4]);
    	if(Float.parseFloat(input[4]) < 0.50) {
			n.addAttribute("energy_class", "E-");
		}
		else {
			n.addAttribute("energy_class", "E+");
		}
    	
    	n.addAttribute("instrumentalness",input[5]);
    	if(Float.parseFloat(input[5]) < 0.085) {
			n.addAttribute("instr_class", "I-");
		}
		else {
			n.addAttribute("instr_class", "I+");
		}
    	
    	n.addAttribute("liveness",input[6]);
    	if(Float.parseFloat(input[6]) < 0.20) {
			n.addAttribute("live_class", "L-");
		}
		else {
			n.addAttribute("live_class", "L+");
		}
    	
    	n.addAttribute("loudness",input[7]);
    	
    	n.addAttribute("speechiness",input[8]);
    	if(Float.parseFloat(input[8]) < 0.50) {
			n.addAttribute("speech_class", "S-");
		}
		else {
			n.addAttribute("speech_class", "S+");
		}
    	
    	n.addAttribute("tempo",input[9]);
    	n.addAttribute("tempo_class", findTempo(Float.parseFloat(input[9])));
    	
    	n.addAttribute("valence",input[10]);
    	if(Float.parseFloat(input[10]) < 0.50) {
			n.addAttribute("val_class", "V-");
		}
		else {
			n.addAttribute("val_class", "V+");
		}
    	
    	n.addAttribute("popularity",input[11]);
    	
    	n.addAttribute("key",input[12]);
    	n.addAttribute("key_class", findKey(Integer.parseInt(input[12])));
    	
    	n.addAttribute("mode",input[13]);
    	n.addAttribute("mode_class", findMode(Integer.parseInt(input[13])));
    	
    	n.addAttribute("count",input[14]);
    	
    	n.addAttribute("subjective_classes", (String)n.getAttribute("acous_class")+n.getAttribute("dance_class")+n.getAttribute("energy_class")+n.getAttribute("instr_class")+n.getAttribute("live_class")+n.getAttribute("speech_class")+n.getAttribute("val_class"));
    	n.addAttribute("objective_classes", (String)n.getAttribute("tempo_class")+n.getAttribute("key_class")+n.getAttribute("mode_class"));
	}
	
	
	/**
	 * Determines if the current artist plays in the minor/major mode a
	 * majority of the time. 
	 * 
	 * @param i The integer read in from the file that corresponds to the mode
	 * @return The strings "minor" or "major"
	 */
	private static String findMode(int i) {
		if(i == 0) {return "Minor";}
		else if(i==1 ) {return "Major";}
		return "Not a valid mode";
	}
	
	
	/**
	 * Determines the key that the artist plays a majority of the time
	 * 
	 * @param i The integer read in from the file that corresponds to the key
	 * @return The String that corresponds to the key
	 */
	private static String findKey(int i) {
		if(i == 0) {
			return "C";
		} else if(i == 1) {
			return "C#/Db";
		} else if(i == 2) {
			return "D";
		} else if(i == 3) {
			return "D#/Eb";
		} else if(i == 4) {
			return "E";
		} else if(i == 5) {
			return "F";
		} else if(i == 6) {
			return "F#/Gb";
		} else if(i == 7) {
			return "G";
		} else if(i == 8) {
			return "G#/Ab";
		} else if(i == 9) {
			return "A";
		} else if(i == 10) {
			return "A#/Bb";
		} else if(i == 11) {
			return "B";
		}

		return "Invalid Key";
	}
	
	
	/**
	 * Determines the tempo that the artist plays a majority of the time
	 * 
	 * @param i The integer read in from the file that corresponds to the tempo
	 * @return The String that corresponds to the tempo
	 */
	private static String findTempo(Float num) {
		if(num >= 38 && num < 60) {
			return "Largo";
		} else if(num >= 60 && num < 76) {
			return "Adagio";
		} else if(num >= 76 && num < 108) {
			return "Adante";
		} else if(num >= 108 && num < 120) {
			return "Moderato";
		} else if(num >= 120 && num < 168) {
			return "Allegro";
		} else if(num >= 168 && num < 176) {
			return "Vivace";
		} else if(num >= 176 && num < 215) {
			return "Presto";
		}
		
		return "Tempo not within known ranges";
	}
	
}