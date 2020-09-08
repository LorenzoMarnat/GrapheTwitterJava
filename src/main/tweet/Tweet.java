package main.tweet;

import java.util.Date;

/*
 * Class Tweet :
 * Contient toutes les informations d'un tweet, a savoir :
 * - id
 * - nom d'utilisateur
 * - date de publication du tweet
 * - contenu du tweet
 * - utilisateur retweeté dans le cas d'une réponse a un tweet
 */
public class Tweet {

	private String id;
	private String user;
	private Date date;
	private String content;
	private String retweet;
	
	public Tweet(String id, String user, Date date, String content, String retweet) {
		this.id = id;
		this.user = user;
		this.date = date;
		this.content = content;
		this.retweet = retweet;
	}

	/*
	 * Getters & Setters
	 */
	public String getId() {
		return id;
	}

	public String getUser() {
		return user;
	}

	public String getContent() {
		return content;
	}
	
	public Date getDate() {
		return date;
	}
	
	public String getRetweet() {
		return retweet;
	}
	
	@Override
	public String toString() {
		return id + "	" + user + "	" + date + "	" + content + "	" + retweet;
	}
}
