import java.io.*;
import javax.servlet.*;
import javax.servlet.http.*;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.*;
import org.json.simple.*;
import java.util.ArrayList;
import java.util.List;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Iterator;
import org.json.JSONObject;
import org.json.JSONArray;

/**
 *
 * @author Lahiru Lakshitha
 */

public class GameLogic extends HttpServlet 
{
   
    private int currentPlayers; 
    private int toPlay; 
    public String PlayerName;
    public int PlayerId;
	public int points; 
    public String[] cards;
    public static boolean gameFull = false;
    private final int players = 4;
    private String trumpSuit; 
    private int trickLeader; 
    private int winner;
    private static ArrayList<String> cardList;
    private static List<String> cardTypes;
    private String[] cardsStr;
    private int[] score; 
     
    
    
    @Override
    public void init() throws ServletException {
        currentPlayers = 0;
        toPlay = 1;
        cardsStr = new String[4];
        score = new int[4];
        trickLeader = 1;
        
        getcardPack();
    }

    static void getcardPack() {
        cardList = new ArrayList<String> ();
        cardTypes = new ArrayList<String>();
        
        cardTypes.add("1");
        cardTypes.add("0");
        cardTypes.add("2");
        cardTypes.add("3");
       
        int i, j;
        for(i = 0; i < 4; i++) {
            for(j = 1; j < 14; j++) {
                String cardType = cardTypes.get(i);
                cardList.add(cardType + "_" + j + ".png");
            }
        }
        
        shuffle();
    }	
	
	/*  public static String[] getCards()
    {
    	int suit = 0;
    	int value = 1;
    	
    	String[] cards = new String[54];
    	
    	for(int i = 0; i<54; i++)
    	{
    		cards[i] =  suit + "_" + value + ".png";
    		if(value == 13)
    		{
    			value = 0;
    			suit++;
    		}
    		value++;
    	}
    	
    	return cards;
    } */

	
 @Override
    protected void doPost(HttpServletRequest request,
	HttpServletResponse resp) throws ServletException, 
	IOException {
		
        HttpSession session = request.getSession();
        String playedCard = request.getParameter("card");
        
		
        if(session.getAttribute("status").equals(Status.Play)) {
            if(isCardAvailable(playedCard, (List<String>) session.getAttribute("cards"))) {
                if((int) (Integer)session.getAttribute("position") != 1) {
                    if(isCardValid(playedCard, (List<String>) session.getAttribute("cards"))) {
						
                        markCardAsPlayed(playedCard, session);
                        
                        toPlay = toPlay % players + 1;
                    }
                } else {
                    markCardAsPlayed(playedCard, session);
                    
                    toPlay = toPlay % players + 1;
                }
            } else {
                
            }
        
            
            if((int) (Integer) session.getAttribute("position") == 4) {
                updateScore();
            }
        } else {
            
        }
    }

	public String addPlayer(String name)
    {
    	this.PlayerName = name;
    	this.PlayerId = players;
    	
    	JSONObject innerObject, mainObject;
    	JSONArray jArray = new JSONArray();
   
    	for(int i = 0; i<13; i++)
    	{
    		innerObject = new JSONObject();
			innerObject.put("image",AllCards[13*(PlayerId-1)+i]);
			jArray.add(innerObject);
    		cards[i] = AllCards[13*(PlayerId-1)+i];
    	}
		
		mainObject = new JSONObject();
		mainObject.put("cards",jArray);
		
    	return mainObject.toString().replace("\\","");
    }

   @Override
    protected void doGet(HttpServletRequest request, 
	HttpServletResponse response)
    throws ServletException, IOException {
		
        HttpSession session = request.getSession();
        
        if(session.isNew()) {
            session.setAttribute("position", ++currentPlayers);
            session.setAttribute("status", Status.Loading);
        }

        if(currentPlayers == players) {
            if(session.getAttribute("status").equals(Status.Loading)) {        
              
                dealCards(session);
            }

            if((int)(Integer)session.getAttribute("position") == toPlay) {
                session.setAttribute("status", Status.Play);
            } else {
                session.setAttribute("status", Status.Waiting);
            }
            
            if(session.getAttribute("status").equals(Status.Play)) {
                List<String> cards = (List<String>) session.getAttribute("cards");
                JSONArray cardArray = new JSONArray();
                for (String card : cards) {   
                    JSONObject tempCard = new JSONObject();
                    tempCard.put("image", + card);
                    cardArray.put(tempCard);
                }
                JSONObject json = new JSONObject();
                response.setContentType("text/event-stream");
                response.setCharacterEncoding("UTF-8");   
                PrintWriter out = response.getWriter();
                json.put("trumpSuit", trumpSuit);
                json.put("cards", cardArray);
                
                for(int i = 1; i < (int) (Integer) session.getAttribute("position"); i++) {
                    json.put("card" + i, cardsStr[i-1]);
                }
                json.put("showHand", true);
                json.put("showCards", true);
                json.put("message", "Play your card");
                out.write("data: " + json + "\n\n");
                out.close();
            } else if(session.getAttribute("status").equals(Status.Waiting)) {
                List<String> cards = (List<String>) session.getAttribute("cards");
                JSONArray cardArray = new JSONArray();
                for (String card : cards) {   
                    JSONObject tempCard = new JSONObject();
                    tempCard.put("image", card);
                    cardArray.put(tempCard);
                }
                JSONObject json = new JSONObject();
                response.setContentType("text/event-stream");
                response.setCharacterEncoding("UTF-8");   
                PrintWriter out = response.getWriter();
                json.put("trumpSuit", trumpSuit);
                json.put("cards", cardArray);
                int j = 1;
                for(int i = 1; i <= 4; i++) {
                    if(cardsStr[i-1] != null && i != (int) (Integer) session.getAttribute("position")) {
                        json.put("card" + j++, cardsStr[i-1]);
                    }
                }
                if(cardsStr[(int) (Integer) session.getAttribute("position") - 1] != null) {
                    json.put("mycard", cardsStr[(int) (Integer) session.getAttribute("position") - 1]);
                }
                json.put("showHand", true);
                json.put("showCards", true);
                json.put("message", "Wait for others to play");
                out.write("data: " + json + "\n\n");
                out.close(); 
            } else {
                
            }
        } else {
            JSONObject json = new JSONObject();
            response.setContentType("text/event-stream");
            response.setCharacterEncoding("UTF-8");   
            PrintWriter out = response.getWriter();
            json.put("cards", new JSONArray());
            json.put("showHand", false);
            json.put("showCards", false);
            if(currentPlayers == 1) {
                json.put("message", "Waiting for others to connect. Only 1 player connected");
            } else {
                json.put("message", "Waiting for others to connect. Only " + currentPlayers +  " players connected");
            }
            out.write("data: " + json + "\n\n");
            out.close();
        } 
    }
       
	   
	 public void destroy()
    {
      // do nothing.
	   /*return cards of new player*/
    }
	
	 /* public static String[] shuffle(String[] cards)
    {
    	String tmp;
    	int pos;
    	for(int i = 0; i<cards.length; i++)
    	{
    		pos = (int )(Math.random() * 54);;
    		tmp = cards[i];
    		cards[i] = cards[pos];
    		cards[pos] = tmp;
    	}
    	
    	return cards;
    } */



	private void dealCards(HttpSession session) {      
        List<String> cards = new ArrayList();
        ArrayList<String> cardList = getCardList();
        int start = 13*((int) (Integer) session.getAttribute("position")-1);
        int end = start + 12;
        
        for(int i = start; i <= end; i++) {
            cards.add(cardList.get(i));
        }
        
        if((int) (Integer) session.getAttribute("position") == 4) {
            trumpSuit = (cards.get(12).split("_"))[0];
        }
        
        
        session.setAttribute("cards", cards);
    }

    private boolean isCardAvailable(String playedCard, List<String> cards) {
        for (String tempCard : cards) {   
            if(tempCard.equals(playedCard)) {
                return true;
            }
        }
        return false;
    }

    private void markCardAsPlayed(String playedCard, HttpSession session) {
        List<String> list = (List<String>) session.getAttribute("cards");
  
        for (Iterator<String> iter = list.listIterator(); iter.hasNext(); ) {
            String card = iter.next();
            if (card.equals(playedCard)) {
                iter.remove();
            }
        }
        
        
        cardsStr[(int) (Integer) session.getAttribute("position") - 1] = playedCard;
        
        
        session.setAttribute("status", Status.Waiting);
    }

    private void updateScore() {
        int winnerIndex = trickLeader; 
        String leaderCardType = cardsStr[trickLeader-1].split("_")[0];
        int leaderCardValue = Integer.parseInt((cardsStr[trickLeader-1].split("_")[1]).replace(".png", ""));
        boolean trumpPlayed = false;
        int trumpPlayedValue = 0;
        for(int i = 0; i < 4; i++) {System.out.println(i+"...");
            String currentCardtype = cardsStr[i].split("_")[0];
            int currentCardValue = Integer.parseInt(cardsStr[i].split("_")[1].replace(".png", ""));
            if(currentCardtype.equals(trumpSuit)) {
                if(trumpPlayed) {
                    if(trumpPlayedValue < currentCardValue) {
                        trumpPlayedValue = currentCardValue;
                        winnerIndex = i+1;
                    }
                } else {
                    trumpPlayed = true;
                    trumpPlayedValue = currentCardValue;
                    winnerIndex = i+1; 
                }
            } else if(currentCardtype.equals(leaderCardType) && trumpPlayed == false) {
                if(leaderCardValue < currentCardValue) {
                    winnerIndex = i+1;
                }
            }
        }
        score[winnerIndex]++;
       
        System.out.println(winnerIndex);
    }

    private void findWinner() {
        int largest = score[0], index = 0;
        for (int i = 1; i < score.length; i++) {
          if ( score[i] > largest ) {
              largest = score[i];
              index = i;
           }
        }
        winner = 1+index;
    }

    private boolean isCardValid(String playedCard, List<String> cards) {
        String playedCardType = playedCard.split("_")[0];
        String trickLeaderCardType = (cardsStr[trickLeader-1].split("_"))[0];
        System.out.println(playedCardType + " " + trickLeaderCardType);
        if(!playedCardType.equals(trickLeaderCardType)) {
           
            for (Iterator<String> iter = cards.listIterator(); iter.hasNext(); ) {
                String cardType = (iter.next().split("_"))[0];
                if(cardType.equals(trickLeaderCardType)) {
                    return false;
                }
            }
            return true;
        } else {
            return true;
        }
    }


    
    public String cardPlayed(String card)
    {
        JSONObject innerObject, mainObject;
    	JSONArray jArray = new JSONArray();
   
    	for(int i = 0; i<13; i++)
    	{
    		innerObject = new JSONObject();
    		if(!(card.replace("\"","")).equals(cards[i]) && !cards[i].equals(""))
    		{
			    innerObject.put("image",cards[i]);
			    jArray.add(innerObject);
			}    
			else
			    cards[i] = "";
			
    	}
		
		mainObject = new JSONObject();
		mainObject.put("cards",jArray);
		
		
    	return players+"";
    	
    }
  
    private static void shuffle() {
        
        for (int i = 0; i < cardList.size(); i++) {
            int swapIndex = (int) (Math.random() * 52);
            String temp = cardList.get(swapIndex);
            cardList.set(swapIndex, cardList.get(i));
            cardList.set(i, temp);
        }
    }

    public static ArrayList<String> getCardList() {
        return cardList;
    }

}



