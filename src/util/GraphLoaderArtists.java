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
import java.util.ListIterator;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.graphstream.graph.*;
import org.graphstream.graph.implementations.*;
public class GraphLoaderArtists{
	private static String[] subjCat = {"AD","AE","AI","AL","AS","AV",
										"DE","DI","DL","DS","DV",
										"EI","EL","ES","EV",
										"IL","IS","IV",
										"LS","LV",
										"SV",
										"DEV","AVS", "subjective_classes"};
	private static String[] objCat = {"MK","MKT"};
	private static HashMap<String,LinkedList<Node>> submap = new HashMap<>();
	private static HashMap<String, LinkedList<Node>> objmap = new HashMap<>();
	
	
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
            	
            	addAttr(n,input);
            	
            }
            reader.close();
            
            addEdges(graph);
            //evalEdges(graph);
        } catch (IOException e) {
            System.err.println("Problem loading graph file: " + filename);
            e.printStackTrace();
        }
	}
	
	private static void addEdges(Graph graph) {
		
		int maincount = 0;
		for(Node n : graph.getEachNode()) {
			
			String attr = n.getAttribute("subjective_classes");
			implementAddEdges(attr, n, graph, submap);
			System.out.println("sub map size "+submap.size());
			
			
			String objkey = (String)n.getAttribute("MKT");
			implementAddEdges(objkey,n,graph, objmap);
			
			System.out.println("obj map size "+objmap.size());
			
			System.out.println("iteration: "+maincount);
			maincount+=1;
		}
		

//		int max = 0;
//		int min = 1000000;
//		String minatt = "";
//		String attr = "";
//		for(String sub : submap.keySet()) {
//			int currCount = submap.get(sub).size();
//			if(max == 0) {
//				max = currCount;
//				min = currCount;
//				attr = sub;
//				minatt = sub;
//			}
//			else {
//				if(currCount > max) {
//					max = currCount;
//					attr = sub;
//				}
//				else {
//					if(currCount < min) {
//						min = currCount;
//						minatt = sub;
//					}
//				}
//			}
//		}
//		System.out.println("Hashmap size: "+submap.size());
//		System.out.println("Max: "+attr+"= "+max);
//		System.out.println("Min: "+minatt+"= "+min);
//		System.out.println("Nodes in graph: "+ graph.getNodeCount());
	}
	
	
	private static void implementAddEdges(String attr, Node n, Graph graph, HashMap<String,LinkedList<Node>> map) {
		
		if(map.containsKey(attr)) {
			LinkedList<Node> nodes = map.get(attr);
			//System.out.println("Size of "+attr+": "+nodes.size());
			for(int idx=0;idx<nodes.size();idx++) {

				if(!n.hasEdgeBetween(nodes.get(idx)) && !nodes.get(idx).getAttribute("name").equals(n.getAttribute("name"))) {
					Edge e = graph.addEdge((String)n.getAttribute("name")+" "+nodes.get(idx).getAttribute("name"), n, nodes.get(idx));
					e.addAttribute("weight", 1);
					
				} else if(!nodes.get(idx).getAttribute("name").equals(n.getAttribute("name"))){
					Edge e = n.getEdgeBetween(nodes.get(idx));
					e.addAttribute("weight",(Integer)e.getAttribute("weight")+1);
					
				}				
				
				if(!map.get(attr).contains(n)) {
					map.get(attr).add(n);
				}
			}
		} else {
			LinkedList<Node> ll = new LinkedList<Node>();
			ll.add(n);
			map.put(attr, ll);
		}
	}
	
	private static void evalEdges(Graph g) {
		
		System.out.println("Edge count: "+g.getEdgeCount());
		for(int k=0;k<15;k++) {
			for(Edge e : g.getEachEdge()) {
				if( (Integer)e.getAttribute("weight") < 2) {
					g.removeEdge(e);
				}
			}
		}
		
		System.out.println("Edge count: "+g.getEdgeCount());
	}
	
	private static void categorizeObjAttr(Node n) {
		n.addAttribute("MK", (String)n.getAttribute("mode_class")+n.getAttribute("key_class"));
		n.addAttribute("MKT", (String)n.getAttribute("mode_class")+n.getAttribute("key_class")+n.getAttribute("tempo_class"));
	}
	
	private static void categorizeSubAttr(Node n) {
		n.addAttribute(subjCat[0], (String)n.getAttribute("acous_class")+n.getAttribute("dance_class"));
		n.addAttribute(subjCat[1], (String)n.getAttribute("acous_class")+n.getAttribute("energy_class"));
		n.addAttribute(subjCat[2], (String)n.getAttribute("acous_class")+n.getAttribute("instr_class"));
		n.addAttribute(subjCat[3], (String)n.getAttribute("acous_class")+n.getAttribute("live_class"));
		n.addAttribute(subjCat[4], (String)n.getAttribute("acous_class")+n.getAttribute("speech_class"));
		n.addAttribute(subjCat[5], (String)n.getAttribute("acous_class")+n.getAttribute("val_class"));
		
		n.addAttribute(subjCat[6], (String)n.getAttribute("dance_class")+n.getAttribute("energy_class"));
		n.addAttribute(subjCat[7], (String)n.getAttribute("dance_class")+n.getAttribute("instr_class"));
		n.addAttribute(subjCat[8], (String)n.getAttribute("dance_class")+n.getAttribute("live_class"));
		n.addAttribute(subjCat[9], (String)n.getAttribute("dance_class")+n.getAttribute("speech_class"));
		n.addAttribute(subjCat[10], (String)n.getAttribute("dance_class")+n.getAttribute("val_class"));
		
		n.addAttribute(subjCat[11], (String)n.getAttribute("energy_class")+n.getAttribute("instr_class"));
		n.addAttribute(subjCat[12], (String)n.getAttribute("energy_class")+n.getAttribute("live_class"));
		n.addAttribute(subjCat[13], (String)n.getAttribute("energy_class")+n.getAttribute("speech_class"));
		n.addAttribute(subjCat[14], (String)n.getAttribute("energy_class")+n.getAttribute("val_class"));
		
		n.addAttribute(subjCat[15], (String)n.getAttribute("instr_class")+n.getAttribute("live_class"));
		n.addAttribute(subjCat[16], (String)n.getAttribute("instr_class")+n.getAttribute("speech_class"));
		n.addAttribute(subjCat[17], (String)n.getAttribute("instr_class")+n.getAttribute("val_class"));
		
		n.addAttribute(subjCat[18], (String)n.getAttribute("live_class")+n.getAttribute("speech_class"));
		n.addAttribute(subjCat[19], (String)n.getAttribute("live_class")+n.getAttribute("val_class"));
		
		n.addAttribute(subjCat[20], (String)n.getAttribute("speech_class")+n.getAttribute("val_class"));
		
		n.addAttribute(subjCat[21], (String)n.getAttribute("dance_class")+n.getAttribute("energy_class")+n.getAttribute("val_class"));
		n.addAttribute(subjCat[22], (String)n.getAttribute("acous_class")+n.getAttribute("val_class")+n.getAttribute("speech_class"));
		n.addAttribute("subjective_classes", (String)n.getAttribute("acous_class")+n.getAttribute("dance_class")+n.getAttribute("energy_class")+n.getAttribute("instr_class")+n.getAttribute("live_class")+n.getAttribute("speech_class")+n.getAttribute("val_class"));
	}
	
	private static void addAttr(Node n, String[] vals) {
		acousticAttr(n, Float.parseFloat(vals[1]));
		danceAttr(n, Float.parseFloat(vals[2]));
		energyAttr(n, Float.parseFloat(vals[4]));
		instrAttr(n, Float.parseFloat(vals[5]));
		liveAttr(n, Float.parseFloat(vals[6]));
		speechAttr(n, Float.parseFloat(vals[8]));
		findTempo(n, Float.parseFloat(vals[9]));
		valAttr(n, Float.parseFloat(vals[10]));
		findKey(n,Integer.parseInt(vals[12]));
		findMode(n,Integer.parseInt(vals[13]));
		
		categorizeSubAttr(n);
		categorizeObjAttr(n);
	}
	
	/**
	 * Determines if the current artist plays in the minor/major mode a
	 * majority of the time. 
	 * 
	 * @param i The integer read in from the file that corresponds to the mode
	 * @return The strings "minor" or "major"
	 */
	private static void findMode(Node n, int i) {
		if(i == 0) {n.addAttribute("mode_class", "Minor");}
		else if(i==1 ) {n.addAttribute("mode_class", "Major");}
		
	}
	
	
	/**
	 * Determines the key that the artist plays a majority of the time
	 * 
	 * @param i The integer read in from the file that corresponds to the key
	 * @return The String that corresponds to the key
	 */
	private static void findKey(Node n,int i) {
		if(i == 0) {
			n.addAttribute("key_class", "C");
		} else if(i == 1) {
			n.addAttribute("key_class", "C#/Db");
		} else if(i == 2) {
			n.addAttribute("key_class", "D");
		} else if(i == 3) {
			n.addAttribute("key_class", "D#/Eb");
		} else if(i == 4) {
			n.addAttribute("key_class", "E");
		} else if(i == 5) {
			n.addAttribute("key_class", "F");
		} else if(i == 6) {
			n.addAttribute("key_class", "F#/Gb");
		} else if(i == 7) {
			n.addAttribute("key_class", "G");
		} else if(i == 8) {
			n.addAttribute("key_class", "G#/Ab");
		} else if(i == 9) {
			n.addAttribute("key_class", "A");
		} else if(i == 10) {
			n.addAttribute("key_class", "A#/Bb");
		} else if(i == 11) {
			n.addAttribute("key_class", "B");
		}
	}
	
	/**
	 * Determines the tempo that the artist plays a majority of the time
	 * 
	 * @param i The integer read in from the file that corresponds to the tempo
	 * @return The String that corresponds to the tempo
	 */
	private static void findTempo(Node n, Float num) {
		if(num >= 38 && num < 60) {
			n.addAttribute("tempo_class", "Largo");
		} else if(num >= 60 && num < 76) {
			n.addAttribute("tempo_class", "Adagio");
		} else if(num >= 76 && num < 108) {
			n.addAttribute("tempo_class", "Adante");
		} else if(num >= 108 && num < 120) {
			n.addAttribute("tempo_class", "Moderato");
		} else if(num >= 120 && num < 168) {
			n.addAttribute("tempo_class", "Allegro");
		} else if(num >= 168 && num < 176) {
			n.addAttribute("tempo_class", "Vivace");
		} else if(num >= 176 && num < 215) {
			n.addAttribute("tempo_class", "Presto");
		}
		
	}
	
	private static void acousticAttr(Node n, float val) {
		if(val < 0.10) {n.addAttribute("acous_class", "Ac1");}
		else if(val >= 0.10 && val < 0.20) {n.addAttribute("acous_class", "Ac2");}
		else if(val >= 0.20 && val < 0.30) {n.addAttribute("acous_class", "Ac3");}
		else if(val >= 0.30 && val < 0.40) {n.addAttribute("acous_class", "Ac4");}
		else if(val >= 0.40 && val < 0.50) {n.addAttribute("acous_class", "Ac5");}
		else if(val >= 0.50 && val < 0.60) {n.addAttribute("acous_class", "Ac6");}
		else if(val >= 0.60 && val < 0.70) {n.addAttribute("acous_class", "Ac7");}
		else if(val >= 0.70 && val < 0.80) {n.addAttribute("acous_class", "Ac8");}
		else if(val >= 0.80 && val < 0.90) {n.addAttribute("acous_class", "Ac9");}
		else {n.addAttribute("acous_class", "Ac10");}
	}
	
	private static void danceAttr(Node n, float val) {
		if(val < 0.10) {n.addAttribute("dance_class", "Da1");}
		else if(val >= 0.10 && val < 0.20) {n.addAttribute("dance_class", "Da2");}
		else if(val >= 0.20 && val < 0.30) {n.addAttribute("dance_class", "Da3");}
		else if(val >= 0.30 && val < 0.40) {n.addAttribute("dance_class", "Da4");}
		else if(val >= 0.40 && val < 0.50) {n.addAttribute("dance_class", "Da5");}
		else if(val >= 0.50 && val < 0.60) {n.addAttribute("dance_class", "Da6");}
		else if(val >= 0.60 && val < 0.70) {n.addAttribute("dance_class", "Da7");}
		else if(val >= 0.70 && val < 0.80) {n.addAttribute("dance_class", "Da8");}
		else if(val >= 0.80 && val < 0.90) {n.addAttribute("dance_class", "Da9");}
		else {n.addAttribute("dance_class", "Da10");}
	}
	
	private static void energyAttr(Node n, float val) {
		if(val < 0.10) {n.addAttribute("energy_class", "En1");}
		else if(val >= 0.10 && val < 0.20) {n.addAttribute("energy_class", "En2");}
		else if(val >= 0.20 && val < 0.30) {n.addAttribute("energy_class", "En3");}
		else if(val >= 0.30 && val < 0.40) {n.addAttribute("energy_class", "En4");}
		else if(val >= 0.40 && val < 0.50) {n.addAttribute("energy_class", "En5");}
		else if(val >= 0.50 && val < 0.60) {n.addAttribute("energy_class", "En6");}
		else if(val >= 0.60 && val < 0.70) {n.addAttribute("energy_class", "En7");}
		else if(val >= 0.70 && val < 0.80) {n.addAttribute("energy_class", "En8");}
		else if(val >= 0.80 && val < 0.90) {n.addAttribute("energy_class", "En9");}
		else {n.addAttribute("energy_class", "En10");}
	}
	
	private static void instrAttr(Node n, float val) {
		if(val < 0.0050) {n.addAttribute("instr_class", "Ins1");}
		else if(val >= 0.0050 && val < 0.010) {n.addAttribute("instr_class", "Ins2");}
		else if(val >= 0.010 && val < 0.0150) {n.addAttribute("instr_class", "Ins3");}
		else if(val >= 0.0150 && val < 0.020) {n.addAttribute("instr_class", "Ins4");}
		else if(val >= 0.020 && val < 0.0250) {n.addAttribute("instr_class", "Ins5");}
		else if(val >= 0.0250 && val < 0.030) {n.addAttribute("instr_class", "Ins6");}
		else if(val >= 0.030 && val < 0.0350) {n.addAttribute("instr_class", "Ins7");}
		else if(val >= 0.0350 && val < 0.040) {n.addAttribute("instr_class", "Ins8");}
		else if(val >= 0.040 && val < 0.0450) {n.addAttribute("instr_class", "Ins9");}
		else if(val >= 0.0450 && val < 0.050) {n.addAttribute("instr_class", "Ins10");}
		else if(val >= 0.050 && val < 0.10) {n.addAttribute("instr_class", "Ins11");}
		else if(val >= 0.10 && val < 0.40) {n.addAttribute("instr_class", "Ins12");}
		else if(val >= 0.40 && val < 0.750) {n.addAttribute("instr_class", "Ins13");}
		else {n.addAttribute("instr_class", "Ins14");}
	}
	
	private static void liveAttr(Node n, float val) {
		if(val < 0.050) {n.addAttribute("live_class", "Li1");}
		else if(val >= 0.050 && val < 0.10) {n.addAttribute("live_class", "Li2");}
		else if(val >= 0.10 && val < 0.150) {n.addAttribute("live_class", "Li3");}
		else if(val >= 0.150 && val < 0.20) {n.addAttribute("live_class", "Li4");}
		else if(val >= 0.20 && val < 0.250) {n.addAttribute("live_class", "Li5");}
		else if(val >= 0.250 && val < 0.30) {n.addAttribute("live_class", "Li6");}
		else if(val >= 0.30 && val < 0.350) {n.addAttribute("live_class", "Li7");}
		else {n.addAttribute("live_class", "Li8");}
	}
	
	private static void speechAttr(Node n, float val) {
		if(val < 0.0050) {n.addAttribute("speech_class", "Spe1");}
		else if(val >= 0.0050 && val < 0.010) {n.addAttribute("speech_class", "Spe2");}
		else if(val >= 0.010 && val < 0.0150) {n.addAttribute("speech_class", "Spe3");}
		else if(val >= 0.0150 && val < 0.020) {n.addAttribute("speech_class", "Spe4");}
		else if(val >= 0.020 && val < 0.0250) {n.addAttribute("speech_class", "Spe5");}
		else if(val >= 0.0250 && val < 0.030) {n.addAttribute("speech_class", "Spe6");}
		else if(val >= 0.030 && val < 0.0350) {n.addAttribute("speech_class", "Spe7");}
		else if(val >= 0.0350 && val < 0.040) {n.addAttribute("speech_class", "Spe8");}
		else if(val >= 0.040 && val < 0.0450) {n.addAttribute("speech_class", "Spe9");}
		else if(val >= 0.0450 && val < 0.050) {n.addAttribute("speech_class", "Spe10");}
		else if(val >= 0.050 && val < 0.0750) {n.addAttribute("speech_class", "Spe11");}
		else if(val >= 0.0750 && val < 0.10) {n.addAttribute("speech_class", "Spe12");}
		else {n.addAttribute("speech_class", "Spe13");}
	}
	
	private static void valAttr(Node n, float val) {
		if(val < 0.10) {n.addAttribute("val_class", "Val1");}
		else if(val >= 0.10 && val < 0.20) {n.addAttribute("val_class", "Val2");}
		else if(val >= 0.20 && val < 0.30) {n.addAttribute("val_class", "Val3");}
		else if(val >= 0.30 && val < 0.40) {n.addAttribute("val_class", "Val4");}
		else if(val >= 0.40 && val < 0.50) {n.addAttribute("val_class", "Val5");}
		else if(val >= 0.50 && val < 0.60) {n.addAttribute("val_class", "Val6");}
		else if(val >= 0.60 && val < 0.70) {n.addAttribute("val_class", "Val7");}
		else if(val >= 0.70 && val < 0.80) {n.addAttribute("val_class", "Val8");}
		else if(val >= 0.80 && val < 0.90) {n.addAttribute("val_class", "Val9");}
		else {n.addAttribute("val_class", "Val10");}
	}
	
}