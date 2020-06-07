package artistsgraph;

import util.GraphLoader;
import util.GraphLoaderArtists;

import org.graphstream.graph.*;
import org.graphstream.graph.implementations.*;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.graphstream.*;

public class ArtistGraph{
	private static List<Node> userArt = new LinkedList<>();
	private static Graph g;
	
	public static List<Node> bfs(Node user){
		List<Node> sugg = new LinkedList<>();
		
		Iterator bfsIt = user.getBreadthFirstIterator();
		while(bfsIt.hasNext()) {
			Node currNode = (Node) bfsIt.next();
			if(!userArt.contains(currNode) && !currNode.equals(user)) {
				sugg.add(currNode);
				currNode.addAttribute("ui.class", "sugg");
			}
			if(sugg.size()==20) {break;}
		}
		
//		for(Node node : userArt) {
//			Iterator<Node> it = node.getNeighborNodeIterator();
//			while(it.hasNext()) {
//				Edge e = node.getEdgeBetween(it.next());
//				Node n = it.next();
//				System.out.println(n);
//				
////				if(e.hasAttribute("ui.class")) {
////					System.out.println("--------------------");
////					System.out.println((String)node.getAttribute("name"));
////					System.out.println((String)it.next().getAttribute("name"));
////					System.out.println("--------------------");
////					it.next().addAttribute("ui.class", "sugg");
////				}
//			}
//		}
		
		for(Node n : sugg) {
			System.out.println((String)n.getAttribute("name"));
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
		//GraphLoader.loadGraph(file, g);
		GraphLoaderArtists.loadGraph(file, g);
		System.out.println("DONE");
		
		Node user = g.addNode("user");
		getArtists(user, artists,g);
		//System.out.println("Displaying graph.....");
		//g.display();
		System.out.println("Performing bfs.....");
		bfs(user);
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