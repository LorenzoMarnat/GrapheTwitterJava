package main.graph;
import java.util.ArrayList;

import org.graphstream.algorithm.BetweennessCentrality;
import org.graphstream.graph.Edge;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.SingleGraph;
import org.graphstream.ui.view.Viewer;

import main.tweet.Tweet;
import main.tweet.TweetBase;
import main.tweet.User;

/*
 * Cette classe centralise toutes les op�rations li�es au graphe
 * Elle calcule les noeuds et ar�tes, g�re l'affichage de ceux-ci, calcule la centralit� et les communaut�es
 */
public class TweetGraph {
	
	private Graph graph;
	private ArrayList<User> users;
	private GraphStats graphStats;
	private boolean centralityCalculated;
	private Viewer v;
	
	// A l'instanciation, r�cup�re la liste des utilisateurs, ceux-ci composeront les noeuds du graphe
	public TweetGraph()
	{	
		users = new ArrayList<User>();
		users = TweetBase.getInstance().getUsers();
	}
	
	public Graph getGraph()
	{
		return graph;
	}
	
	public void setGraphStats(GraphStats graphStats) {
		this.graphStats = graphStats;
	}
	
	public GraphStats getGraphStats()
	{
		return graphStats;
	}
	
	
	/* 
	 * Cr�e un nouveau graphe (vide)
	 * On ajoute au graphe une feuille de style, indiquant comment afficher les noeuds
	 */
	private void newGraph(String name)
	{
		graph = new SingleGraph(name);
		// Acc�de � l'attribut "stylesheet" du graphe
		// Les noeuds ont: une taille dynamique, un nom cach�, une couleur dynamique (parmis vert,jaune,orange,rouge,violet) et une taille par d�faut de 8px
		graph.addAttribute("ui.stylesheet", "node {"
				+ "size-mode: dyn-size;	"
				+ "text-mode: hidden;"
				+ "fill-mode: dyn-plain;" 
				+ "fill-color: green,yellow,orange,red,purple;"
				+ "size: 8px;}");
		centralityCalculated = false;
	}
		
	/*
	 * Change la liste d'utilisateur
	 * Si on a effectu� une recherche (filtred == true), la liste d'utilisateur sera le resultat de la recherche
	 * Sinon c'est la liste enti�re des utilisateurs
	 */
	public void setUsers(Boolean filtred)
	{		
		users = new ArrayList<User>();

		if(filtred)
			users = TweetBase.getInstance().getFiltredUsers();
		else
			users = TweetBase.getInstance().getUsers();
	}	
	
	// Filtre les noeuds du graphe pour supprimer ceux avec un degr� inf�rieur � nb
	public void filterNodes(int nb)
	{
		int nbNodes = graph.getNodeCount();
		int i= 0;
		while(i < nbNodes)
		{
			Node n = graph.getNode(i);
			if(n.getDegree() < nb)
			{
				graph.removeNode(i);
				nbNodes--;
			}
			else
				i++;
		}
	}
	
	// Centralise les op�rations sur le graphe
	public void computeGraph(int nb, String name, boolean centrality)
	{
		// Cr�e un nouveau graphe
		newGraph(name);
		// D�finit ses noeuds (les utilisateurs)
		setNodes();
		// D�finit ses ar�tes (les retweets)
		setEdges();
		// Filtre le graphe
		filterNodes(nb);
		// D�finit les statistiques du graphe
		setStats();
		// Si la case "Centralit�" est coch�e, calcul la centralit� et adapte l'affichage
		if(centrality) 
		{
			setCentrality();
			setColorSize();
		}
	}
	
	// Affiche le graphe
	// Le bool�en "hide" permet de cacher les noeuds sont liens (degr� 0)
	// Le bool�en "showCommunity" permet de simplifier l'affichage en communaut�es
	public void displayGraph(boolean hide, boolean showCommunity)
	{
		// show only nodes with a certain centrality, hide the others
		if(showCommunity) {
			if(!centralityCalculated)
			{	
				System.out.println("Calculez la centralit�");
			}
			else
			{
				// On r�cup�re la valeur de centralit� de chaque noeud
				// Si elle est faible, on cache le noeud
				// Sinon elle est �lev�e, on augmente la taille du noeud proportionnellement � son degr�
				for(Node n : graph) {
					double centrality = n.getAttribute("Cb");
					if(centrality <= 100.0) {
						n.addAttribute("ui.hide");
						for(Edge e : n.getEachEdge()) {
							e.addAttribute("ui.hide");
						}
					}
					else {
						int currentSize = n.getAttribute("ui.size");
						n.setAttribute("ui.size",  currentSize + n.getDegree());
					}
				}
				// Modifie la stylesheet: l'attribut "text-mode" est pass� � normal (on affiche le nom)
				graph.setAttribute("ui.stylesheet", "node {"
						+ "size-mode: dyn-size;	"
						+ "text-mode: normal;"
						+ "fill-mode: dyn-plain;" 
						+ "fill-color: green,yellow,orange,red,purple;"
						+ "size: 8px;}");
			}
		}
		if(hide)
			for(Node n : graph)
				if(n.getDegree() < 1)
					n.addAttribute("ui.hide");
		
		// Affiche le graph dans une nouvelle fen�tre
		// Quitter la fen�tre du graphe ne ferme pas l'interface graphique
		v = graph.display();
		v.setCloseFramePolicy(Viewer.CloseFramePolicy.CLOSE_VIEWER);

	}
	
	// Modifie la taille et la couleur des noeuds en fonction de leur centralit�
	private void setColorSize() 
	{
		for(Node n : graph)
		{
			double cb = n.getAttribute("Cb");
			int log = 0;
			double color = 0;
			if(cb >= 1)
			{
				log = (int) Math.log10(cb);
				switch(log)
				{
				case 0: color = 0;break;
				case 1: color = 0.25;break;
				case 2: color = 0.5;break;
				case 3: color = 0.75;break;
				case 4: color = 1;break;
				default: color = 1;break;
				}
			}
			
			n.setAttribute("ui.color",color);
			n.setAttribute("ui.size", (8*log)+8);
		}
		
	}
	
	// Le calcul de centralit� est d�j� compris dans la librairie externe
	// La centralit� de chaque noeud est contenu dans l'attribut "Cb"
	public void setCentrality()
	{
		BetweennessCentrality bcb = new BetweennessCentrality();
		bcb.init(graph);
		bcb.compute();
		centralityCalculated = true;
	}
	
	// D�finit les noeuds du graphe, chaque noeud correspond � un utilisateur unique
	public void setNodes()
	{
		for(User u : users)
		{
			// Ajoute un noeud au graphe, identifi� par le pseudo
			graph.addNode(u.getName());
			// R�cup�re un noeud gr�ce � son id
			Node n = graph.getNode(u.getName());
			// Ajoute un attribut "tweets" contenant les retweets
			n.setAttribute("tweets", u.getRetweets());
			// Dans l'interface graphique, on affichera le nom du noeud
			n.addAttribute("ui.label", u.getName());
		}
	}
	
	// D�finit les ar�tes du graphe
	// Un ar�te est trac�e entre A et B si A a retweet� B
	// Il ne peut y avoir qu'une ar�te entre A et B
	public void setEdges() 
	{
		int i = 0;
		for(User u : users)
		{
			for(Tweet t : u.getRetweets())
			{
				Node n = graph.getNode(t.getUser());
				Node m = graph.getNode(t.getRetweet());
				
				if(n == null)
				{
					graph.addNode(t.getUser());
					n = graph.getNode(t.getUser());
					n.addAttribute("ui.label", t.getUser());
				}

				if(n != null && m != null && !n.hasEdgeBetween(m) && !t.getUser().equals(t.getRetweet()))
				{
					graph.addEdge(t.getUser()+t.getRetweet()+Integer.toString(++i), t.getUser(), t.getRetweet()).addAttribute("layout.weight", 5);
				}
			}
		}
	}
	
	// Calcule les statistiques du graphe (degr� moyen, volume, ordre, diametre)
	// Les stats sont contenues dans un objet GraphStats
	public void setStats()
	{
		graphStats = new GraphStats(graph);
	}

}
