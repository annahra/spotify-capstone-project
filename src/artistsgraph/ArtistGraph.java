package artistsgraph;

import util.GraphLoader;
import org.graphstream.graph.*;
import org.graphstream.graph.implementations.*;

import java.util.LinkedList;

import org.graphstream.*;

public class ArtistGraph{
	
	private static void getArtists(Node user, String[] artists, Graph g){
		for(int i=0;i<artists.length;i++) {
			Node n = g.getNode(artists[i]);
			n.addAttribute("ui.class", "userlikes");
			Edge e = g.addEdge("User likes "+artists[i], user, n);
			e.addAttribute("ui.class", "userlikes");
		}
	}
	
	public static void main(String args[]) {
		
		String[] artists = {"Vansire","Alvvays","Khruangbin","blink-182","San Holo","Rage Against The Machine"};
		
		Graph g = new SingleGraph("Graph of Artists");
		//g.addAttribute("ui.stylesheet", "graph { fill-color: red; }");
		g.addAttribute("ui.stylesheet", styleSheet);
		g.addAttribute("ui.quality");
		
		//String file = "data/artists-small.txt";
		//String file = "data/artists.txt";
		String file = "data/artists-1600.txt";
		
		System.out.println("Loading graph....");
		GraphLoader.loadGraph(file, g);
		System.out.println("DONE");
		
		Node n = g.addNode("user");
		getArtists(n, artists,g);
		
		
		
		g.display();
	}
	
	private static String styleSheet = ""
			+ "node#user{"
			+ "	size: 20px;"
			+ "	fill-color: blue;"
			+ "}"
			+ ""
			+ "node.userlikes{"
			+ "	size: 15px;"
			+ "	fill-color: green;"
			+ "}"
			+ ""
			+ "edge.userlikes{"
			+ "	fill-color: blue;"
			+ "}";
	
}