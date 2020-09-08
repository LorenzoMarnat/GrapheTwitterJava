package main.reader;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import main.tweet.Tweet;
import main.tweet.TweetBase;
import main.tweet.User;


/*
 * Permet de lire un fichier CSV
 */
public class CSVReader {
	private String filepath;
	private String sep;
	
	/*
	 * Constructeur
	 * filepath : chemin du fichier csv
	 * sep : caractère de séparation
	 */
	public CSVReader(String filepath, String sep){
		this.filepath = filepath;
		this.sep = sep;
	}

	/*
	 * Lit les données depuis un fichier CSV et les ajoute a la base de tweet
	 */
	public void readCSV() {
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		BufferedReader csvReader = null;
		String line = "";
		
		ArrayList<String> users = new ArrayList<String>();
		
		try {
			// On lit tout le fichier
			csvReader = new BufferedReader(new FileReader(filepath));
			while((line = csvReader.readLine()) != null) {
				// on split le tweet pour avoir toutes les infos
				String[] tweetInfo = line.split(sep);
				Date date = null;
				date = dateFormat.parse(tweetInfo[2]);
				Tweet t;
				// s'il n'y a pas de rt, on met une valeur "null"
				if(tweetInfo.length == 5)
					t = new Tweet(tweetInfo[0], tweetInfo[1], date, tweetInfo[3], tweetInfo[4]);
				else
					t = new Tweet(tweetInfo[0], tweetInfo[1], date, tweetInfo[3], "null");
				
				TweetBase.getInstance().getTweets().add(t);
				
				// On met a jour la base d'utilisateur si l'utilisateur n'existe pas
				if(!users.contains(tweetInfo[1]))
				{
					users.add(tweetInfo[1]);
					TweetBase.getInstance().getUsers().add(new User(tweetInfo[1]));
				}
				if(!users.contains(t.getRetweet()) && !t.getRetweet().equals("null"))
				{
					users.add(t.getRetweet());
					TweetBase.getInstance().getUsers().add(new User(t.getRetweet()));
				}
				if(!t.getRetweet().equals("null"))
					TweetBase.setRetweetFromUser(tweetInfo[4], t);
				
			}
			csvReader.close();
			
		} catch(FileNotFoundException e) {
			e.printStackTrace();
		} catch(IOException e) {
			e.printStackTrace();
		} catch (ParseException e) {
			e.printStackTrace();
		}
	}
}
