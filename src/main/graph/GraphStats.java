package main.graph;

import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.algorithm.Toolkit;

public class GraphStats {

	private int volume;
	private int ordre;
	private double diametre;
	private double degre;
	
	public GraphStats(Graph graph)
	{
		int volume = 0;
		int ordre = 0;
		for(Node n : graph)
		{
			volume += n.getDegree();
			ordre++;
		}
		this.volume = volume;
		this.ordre = ordre;
		degre = Toolkit.averageDegree(graph);
		diametre = Toolkit.diameter(graph);
	}

	public int getVolume() {
		return volume;
	}

	public int getOrdre() {
		return ordre;
	}

	public double getDiametre() {
		return diametre;
	}

	public double getDegre() {
		int i = (int)(degre*100);
		degre = i/100.;
		return degre;
	}

}
