package main.tweet;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/*
 * Class Base de Tweet : 
 * Singleton qui contient tout les tweets et tout les utilisateurs
 */
public class TweetBase {

	private static TweetBase tweetBase = new TweetBase();
	private static ArrayList<Tweet> tweets;
	private static ArrayList<Tweet> filtredTweets;
	private static ArrayList<User> users;
	private static ArrayList<User> filtredUsers;

	// Constructor
	private TweetBase() {
		tweets = new ArrayList<Tweet>();
		users = new ArrayList<User>();
	}
	
	/*
	 * Getters & Setters
	 */
	public ArrayList<Tweet> getFiltredTweets() {
		return filtredTweets;
	}
	
	public ArrayList<Tweet> getTweets() {
		return tweets;
	}
	
	public ArrayList<User> getUsers() {
		return users;
	}
	
	public ArrayList<User> getFiltredUsers() {
		return filtredUsers;
	}

	public static TweetBase getInstance()
	{
		return tweetBase;
	}
	
	// Recherche tout les Tweet correspondant a la recherche
	public static ArrayList<Tweet> searchTweets(String search)
	{
		ArrayList<String> u = new ArrayList<String>();
		
		filtredTweets = new ArrayList<Tweet>();
		filtredUsers = new ArrayList<User>();
		
		Pattern p = Pattern.compile(search);
		
		for(Tweet t : tweets)
		{
			Matcher m = p.matcher(t.getUser()+t.getContent()+t.getRetweet());
			if(m.find())
			{
				filtredTweets.add(t);
				if(!u.contains(t.getUser()))
				{
					u.add(t.getUser());
					filtredUsers.add(new User(t.getUser()));
				}
				if(!u.contains(t.getRetweet()) && !t.getRetweet().equals("null"))
				{
					u.add(t.getRetweet());
					filtredUsers.add(new User(t.getRetweet()));
				}
				if(!t.getRetweet().equals("null"))
					setRetweetFromFiltredUser(t.getRetweet(), t);
			}
		}
		return filtredTweets;
	}
	
	// ajoute le tweet t dans la base de retweet de tout les utilisateurs qui l'ont retweeté
	public static void setRetweetFromUser(String name, Tweet t)
	{
		for(User u : users)
		{
			if(u.getName().equals(name))
			{
				u.getRetweets().add(t);
			}
		}
	}
	
	// Comme setRetweetFromUser mais avec la liste des utilisateurs filtré
	public static void setRetweetFromFiltredUser(String name, Tweet t)
	{
		for(User u : filtredUsers)
		{
			if(u.getName().equals(name))
			{
				u.getRetweets().add(t);
			}
		}
	}
	
	// Renvoi tout les tweet d'un utilisateur passé en paramètre
	public static ArrayList<Tweet> getTweetsFromUser(String user)
	{
		ArrayList<Tweet> tweetList = new ArrayList<Tweet>();
		
		for(Tweet t : tweets)
		{
			if(t.getUser() == user)
			{
				tweetList.add(t);
			}
		}
		
		return tweetList;
	}
}
