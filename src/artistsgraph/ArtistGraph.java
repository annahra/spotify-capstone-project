package artistsgraph;

import util.GraphLoader;
import org.graphstream.graph.*;
import org.graphstream.graph.implementations.*;

public class ArtistGraph{
	public static void main(String args[]) {
		Graph g = new SingleGraph("Graph of Artists");
		
		//String file = "data/artists-small.txt";
		String file = "data/artists.txt";
		
		System.out.println("Loading graph....");
		GraphLoader.loadGraph(file, g);
		System.out.println("DONE");
		
		g.display();
	}
}