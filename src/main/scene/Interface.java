package main.scene;


import java.util.Date;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Separator;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import main.graph.TweetGraph;
import main.tweet.Tweet;
import main.tweet.TweetBase;

// Gère toute l'interface Javafx
public class Interface {

	private Interface intrface;
	private static Scene scene;
	
	public Interface getInterface()
	{
		return intrface;
	}
	
	private Interface()
	{
		intrface = new Interface();
	}
	
	public static void launchInterface(Stage stage)
	{
		stage.setTitle("Graphe");
		scene = buildScene();
		stage.setScene(scene);
		stage.sizeToScene();
		stage.show();
	}
	
	// Construit la scène de l'interface
	@SuppressWarnings("unchecked")
	public static Scene buildScene()
	{		
		TweetGraph tweetGraph = new TweetGraph();
		
		GridPane grid = new GridPane();

		MenuBar menuBar = new MenuBar();
		Menu actions = new Menu("Actions");
		menuBar.getMenus().addAll(actions);
		MenuItem itemQuit = new MenuItem("Quitter");
		actions.getItems().addAll(itemQuit);

		// Quitte l'interface
		itemQuit.setText("Quitter");
		itemQuit.setOnAction((ActionEvent t) ->
		{
			Platform.exit();
		});
	    
		Separator separator = new Separator();
		
		// On construit une table contenant tous les tweets
		TableView<Tweet> tableTweet = new TableView<Tweet>();
		TableColumn<Tweet,String> cId = new TableColumn<Tweet,String>("Id");
		TableColumn<Tweet,String> cUser = new TableColumn<Tweet,String>("User");
		TableColumn<Tweet,Date> cDate = new TableColumn<Tweet,Date>("Date");
		TableColumn<Tweet,String> cContent = new TableColumn<Tweet,String>("Content");
		TableColumn<Tweet,String> cRetweet = new TableColumn<Tweet,String>("Retweet");
		
		cId.setCellValueFactory(new PropertyValueFactory<>("id"));
		cUser.setCellValueFactory(new PropertyValueFactory<>("user"));
		cDate.setCellValueFactory(new PropertyValueFactory<>("date"));
		cContent.setCellValueFactory(new PropertyValueFactory<>("content"));
		cRetweet.setCellValueFactory(new PropertyValueFactory<>("retweet"));
		
		tableTweet.getColumns().addAll(cId,cUser,cDate,cContent,cRetweet);
		tableTweet.setItems(getTweets(false));
		
		
		// Permet d'effectuer des recherches dans les tweets
		// La recherche concerne les noms d'utilisateurs, le contenu des tweets et le nom du retweet
		// Quand une recherche est effectuée, on filtre le graphe pour n'afficher que les resultats de la recherche
		
		Text textSearch = new Text();
		textSearch.setText("Chercher dans les tweets : ");
		TextField textFieldSearch = new TextField();
			
		Button search = new Button("Search");
		search.setText("Rechercher");
		search.setOnAction((ActionEvent e) ->
		{
			try
			{
				TweetBase.searchTweets(textFieldSearch.getText());
				tableTweet.setItems(getTweets(true));
				tweetGraph.setUsers(true);
			}
			catch(Exception ex)
			{
				tableTweet.setItems(getTweets(false));
				tweetGraph.setUsers(false);
			}
		});
		
		
		grid.add(menuBar,0,0);
		grid.add(textSearch, 0, 1);
		grid.add(textFieldSearch, 1, 1);
		grid.add(search, 2, 1);
		
		GridPane graphGrid = new GridPane();
		Text degre = new Text();
		Text volume = new Text();
		Text ordre = new Text();
		Text diametre = new Text();
		
		degre.setText("Degré moyen : ");
		volume.setText("Volume : ");
		ordre.setText("Ordre : ");
		diametre.setText("Diametre : ");
		
		// Permet de cacher les noeuds en dessous d'un certain degré, par défaut la valeur est à zéro (aucun noeud caché)
		Text filter = new Text();
		filter.setText("Degré minimal (entier) pour lequel les noeuds seront affichés : ");
		TextField graphFilter = new TextField();
		
		CheckBox checkCentrality = new CheckBox("Calculer centralité");
		
		CheckBox checkHide = new CheckBox("Cacher les noeuds de degré 0");
		
		CheckBox checkCommunity = new CheckBox("Regrouper par communautés");
		
		Button displayGraph = new Button("DisplayGraph");
		displayGraph.setText("Display graphe");
		displayGraph.setOnAction((ActionEvent e) ->
		{
			boolean centrality = false;
			if(checkCentrality.isSelected())
				centrality = true;
			
			try
			{
				tweetGraph.computeGraph(Integer.parseInt(graphFilter.getText()),"Filtred graph",centrality);
			}
			catch(Exception ex)
			{
				tweetGraph.computeGraph(0,"Simple graph",centrality);
			}
			
			degre.setText("Degré moyen : "+Double.toString(tweetGraph.getGraphStats().getDegre()));
			volume.setText("Volume : "+Integer.toString(tweetGraph.getGraphStats().getVolume()));
			ordre.setText("Ordre : "+Integer.toString(tweetGraph.getGraphStats().getOrdre()));
			diametre.setText("Diametre : "+Double.toString(tweetGraph.getGraphStats().getDiametre()));
			
			boolean showCommunity = checkCommunity.isSelected();
			
			boolean hide = false;
			if(checkHide.isSelected())
				hide = true;
			
			try
			{
				tweetGraph.displayGraph(hide, showCommunity);
			}
			catch(Exception ex)
			{
				System.out.println("Créez un graphe avant de l'afficher");
			}
			
		});
		
		graphGrid.add(degre,0,0);
		graphGrid.add(volume,0,1);
		graphGrid.add(ordre,1,0);
		graphGrid.add(diametre,1,1);
		graphGrid.add(separator,0,2);
		graphGrid.add(filter,0,3);
		graphGrid.add(graphFilter,1,3);
		graphGrid.add(checkCentrality,2,4);
		graphGrid.add(displayGraph,0,4);
		graphGrid.add(checkHide,2,5);
		graphGrid.add(checkCommunity, 2, 3);
		
		BorderPane root = new BorderPane();
		root.setTop(grid);
		root.setCenter(tableTweet);
		root.setBottom(graphGrid);
		Scene scene = new Scene(root,1000,700);
		return scene;
	}
	
	// Renvoit la liste de tweets, soit filtrée soit entière
	private static ObservableList<Tweet> getTweets(Boolean filtred)
	{
		ObservableList<Tweet> list;
		if(filtred)
			list = FXCollections.observableArrayList(TweetBase.getInstance().getFiltredTweets());
		else
			list = FXCollections.observableArrayList(TweetBase.getInstance().getTweets());
		return list;
	}
	
}
