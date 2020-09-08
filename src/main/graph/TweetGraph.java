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
 * Cette classe centralise toutes les opérations liées au graphe
 * Elle calcule les noeuds et arêtes, gère l'affichage de ceux-ci, calcule la centralité et les communautées
 */
public class TweetGraph {
	
	private Graph graph;
	private ArrayList<User> users;
	private GraphStats graphStats;
	private boolean centralityCalculated;
	private Viewer v;
	
	// A l'instanciation, récupère la liste des utilisateurs, ceux-ci composeront les noeuds du graphe
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
	 * Crée un nouveau graphe (vide)
	 * On ajoute au graphe une feuille de style, indiquant comment afficher les noeuds
	 */
	private void newGraph(String name)
	{
		graph = new SingleGraph(name);
		// Accède à l'attribut "stylesheet" du graphe
		// Les noeuds ont: une taille dynamique, un nom caché, une couleur dynamique (parmis vert,jaune,orange,rouge,violet) et une taille par défaut de 8px
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
	 * Si on a effectué une recherche (filtred == true), la liste d'utilisateur sera le resultat de la recherche
	 * Sinon c'est la liste entière des utilisateurs
	 */
	public void setUsers(Boolean filtred)
	{		
		users = new ArrayList<User>();

		if(filtred)
			users = TweetBase.getInstance().getFiltredUsers();
		else
			users = TweetBase.getInstance().getUsers();
	}	
	
	// Filtre les noeuds du graphe pour supprimer ceux avec un degré inférieur à nb
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
	
	// Centralise les opérations sur le graphe
	public void computeGraph(int nb, String name, boolean centrality)
	{
		// Crée un nouveau graphe
		newGraph(name);
		// Définit ses noeuds (les utilisateurs)
		setNodes();
		// Définit ses arêtes (les retweets)
		setEdges();
		// Filtre le graphe
		filterNodes(nb);
		// Définit les statistiques du graphe
		setStats();
		// Si la case "Centralité" est cochée, calcul la centralité et adapte l'affichage
		if(centrality) 
		{
			setCentrality();
			setColorSize();
		}
	}
	
	// Affiche le graphe
	// Le booléen "hide" permet de cacher les noeuds sont liens (degré 0)
	// Le booléen "showCommunity" permet de simplifier l'affichage en communautées
	public void displayGraph(boolean hide, boolean showCommunity)
	{
		// show only nodes with a certain centrality, hide the others
		if(showCommunity) {
			if(!centralityCalculated)
			{	
				System.out.println("Calculez la centralité");
			}
			else
			{
				// On récupère la valeur de centralité de chaque noeud
				// Si elle est faible, on cache le noeud
				// Sinon elle est élevée, on augmente la taille du noeud proportionnellement à son degré
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
				// Modifie la stylesheet: l'attribut "text-mode" est passé à normal (on affiche le nom)
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
		
		// Affiche le graph dans une nouvelle fenêtre
		// Quitter la fenêtre du graphe ne ferme pas l'interface graphique
		v = graph.display();
		v.setCloseFramePolicy(Viewer.CloseFramePolicy.CLOSE_VIEWER);

	}
	
	// Modifie la taille et la couleur des noeuds en fonction de leur centralité
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
	
	// Le calcul de centralité est déjà compris dans la librairie externe
	// La centralité de chaque noeud est contenu dans l'attribut "Cb"
	public void setCentrality()
	{
		BetweennessCentrality bcb = new BetweennessCentrality();
		bcb.init(graph);
		bcb.compute();
		centralityCalculated = true;
	}
	
	// Définit les noeuds du graphe, chaque noeud correspond à un utilisateur unique
	public void setNodes()
	{
		for(User u : users)
		{
			// Ajoute un noeud au graphe, identifié par le pseudo
			graph.addNode(u.getName());
			// Récupère un noeud grâce à son id
			Node n = graph.getNode(u.getName());
			// Ajoute un attribut "tweets" contenant les retweets
			n.setAttribute("tweets", u.getRetweets());
			// Dans l'interface graphique, on affichera le nom du noeud
			n.addAttribute("ui.label", u.getName());
		}
	}
	
	// Définit les arêtes du graphe
	// Un arête est tracée entre A et B si A a retweeté B
	// Il ne peut y avoir qu'une arête entre A et B
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
	
	// Calcule les statistiques du graphe (degré moyen, volume, ordre, diametre)
	// Les stats sont contenues dans un objet GraphStats
	public void setStats()
	{
		graphStats = new GraphStats(graph);
	}

}
