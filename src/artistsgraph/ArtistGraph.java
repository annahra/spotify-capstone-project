package artistsgraph;

import util.GraphLoader;
import org.graphstream.graph.*;
import org.graphstream.graph.implementations.*;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.graphstream.*;

public class ArtistGraph{
	private static List<Node> userArt = new LinkedList<>();
	private static Graph g;
	
	public static List<Node> bfs(){
		List<Node> sugg = new LinkedList<>();
		
		for(Node node : userArt) {
			List<Node> neigh = new LinkedList<>();
			Iterator<Node> it = node.getNeighborNodeIterator();
			while(it.hasNext()) {
				Edge e = node.getEdgeBetween(it.next());
				if(e.hasAttribute("ui.class")) {
//					System.out.println("--------------------");
//					System.out.println((String)node.getAttribute("name"));
//					System.out.println((String)it.next().getAttribute("name"));
//					System.out.println("--------------------");
					it.next().addAttribute("ui.class", "sugg");
				}
			}
		}
		
		return sugg;
	}
	
	private static void getArtists(Node user, String[] artists, Graph g){
		for(int i=0;i<artists.length;i++) {
			Node n = g.getNode(artists[i]);
			n.addAttribute("ui.class", "userlikes");
			
//			System.out.println("-----------------------");
//			System.out.println((String)n.getAttribute("name")+":");
//			Iterator<Node> nIt = n.getNeighborNodeIterator();
//			while(nIt.hasNext()) {
//				System.out.println((String)nIt.next().getAttribute("name"));
//			}
//			System.out.println("-----------------------");
			
			
			if(!userArt.contains(n)) {userArt.add(n);}
			
			Edge e = g.addEdge("User likes "+artists[i], user, n);
			e.addAttribute("ui.class", "userlikes");
		}
	}
	
	public static void main(String args[]) {
		
		String[] artists = {"Vansire","Alvvays","Khruangbin","blink-182","San Holo","Rage Against The Machine"};
		
		g = new SingleGraph("Graph of Artists");
		//g.addAttribute("ui.stylesheet", "graph { fill-color: red; }");
		g.addAttribute("ui.stylesheet", styleSheet);
		g.addAttribute("ui.quality");
		
		//String file = "data/artists-small.txt";
		String file = "data/artists.txt";
		//String file = "data/artists-1600.txt";
		
		System.out.println("Loading graph....");
		GraphLoader.loadGraph(file, g);
		System.out.println("DONE");
		
		Node n = g.addNode("user");
		getArtists(n, artists,g);
		
		g.display();
		bfs();
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
			+ "}"
			+ ""
			+ "edge.priority{"
			+ "	fill-color: red;"
			+ "}"
			+ ""
			+ "node.sugg{"
			+ "	fill-color: red;}";
	
}