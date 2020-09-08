package main.tweet;

import java.util.ArrayList;

/*
 * Class User :
 * Contient toute les infos suivante sur un utilisateur :
 * - nom
 * - liste des tweets qu'il a rt
 */
public class User {
	
	private String name;
	private ArrayList<Tweet> retweets;
	
	// Constructeur
	public User(String name) {
		this.name = name;
		retweets = new ArrayList<Tweet>();
	}
	
	/*
	 * Getters & Setters
	 */
	public String getName() {
		return name;
	}

	public ArrayList<Tweet> getRetweets() {
		return retweets;
	}

	public void setRetweets(ArrayList<Tweet> retweets) {
		this.retweets = retweets;
	}
}
