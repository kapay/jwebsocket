/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.jwebsocket.android.demo;

import android.app.ListActivity;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jwebsocket.api.WebSocketClientEvent;
import org.jwebsocket.api.WebSocketClientTokenListener;
import org.jwebsocket.api.WebSocketPacket;
import org.jwebsocket.kit.WebSocketException;
import org.jwebsocket.token.Token;
import org.jwebsocket.token.TokenFactory;

/**
 *
 * @author Prashant
 */
public class TwitterStreamActivity extends ListActivity implements
		WebSocketClientTokenListener {

    private ArrayList<Tweet> tweets = null;
    private TweetAdapter tweetAdapter;

    

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setContentView(R.layout.twitter_stream_activity);
        this.tweets = new ArrayList<Tweet>();
        this.tweetAdapter = new TweetAdapter(this,R.layout.tweet_row, tweets);
        setListAdapter(tweetAdapter);
        
    }

    @Override
    public boolean onCreateOptionsMenu(Menu aMenu) {
            MenuInflater lMenInfl = getMenuInflater();
            lMenInfl.inflate(R.menu.twitter_stream_menu, aMenu);
            return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
            // Handle item selection
            switch (item.getItemId()) {
            case R.id.twitterStreamSettings:
                TwitterStreamSettingsActivity settingsialog = new TwitterStreamSettingsActivity(this, new TwitterStreamSettingsActivity.TwitterSettingsListener() {

            public void setSettings(String keywords) {
                    //TODO:use the keywords specified to get the twitter stream
                    Token token = TokenFactory.createToken("org.jwebsocket.plugins.twitter", "setStream");
                    token.setString("keywords", keywords);
                    try {
                        JWC.sendToken(token);
                    } catch (WebSocketException ex) {
                        Logger.getLogger(TwitterStreamActivity.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            });
            settingsialog.show();
                return true;
            default:
                    return super.onOptionsItemSelected(item);
            }
    }


    @Override
    protected void onResume() {
            super.onResume();
            connect();
    }

    @Override
    protected void onPause() {
            super.onPause();
            disConnect();
    }

    private void connect() {
        try {
            JWC.addListener(this);
            JWC.open();
        } catch (WebSocketException ex) {

        }
    }

    private void disConnect() {
        try {
            JWC.removeListener(this);
            JWC.close();
        } catch (WebSocketException ex) {
                // TODO: log exception
        }
    }

    public void processToken(WebSocketClientEvent aEvent, Token aToken) {
        if(aToken.getNS().equals("org.jwebsocket.plugins.twitter") && aToken.getType().equals("event") && aToken.getString("name").equals("status")){
            tweetAdapter.add(new Tweet(aToken.getString("status")));
            tweetAdapter.notifyDataSetChanged();
        }
        //fillDemoTweets();
        //throw new UnsupportedOperationException("Not supported yet.");
    }

    public void processOpened(WebSocketClientEvent aEvent) {
        //throw new UnsupportedOperationException("Not supported yet.");
    }

    public void processPacket(WebSocketClientEvent aEvent, WebSocketPacket aPacket) {
        
        //throw new UnsupportedOperationException("Not supported yet.");
    }

    public void processClosed(WebSocketClientEvent aEvent) {
        //throw new UnsupportedOperationException("Not supported yet.");
    }

    public class Tweet
    {
        private String tweet;

        public Tweet(String tweet)
        {
            this.tweet = tweet;
        }
        /**
         * @return the tweet
         */
        public String getTweet() {
            return tweet;
        }

        /**
         * @param tweet
         */
        public void setTweet(String tweet) {
            this.tweet = tweet;
        }
    
    }

    public class TweetAdapter extends ArrayAdapter<Tweet>{
        private ArrayList<Tweet> tweets;

        public TweetAdapter(Context context, int textViewResourceId, ArrayList<Tweet> tweets) {
                super(context, textViewResourceId, tweets);
                this.tweets = tweets;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
                View v = convertView;
                if (v == null) {
                    LayoutInflater vi = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                    v = vi.inflate(R.layout.tweet_row, null);
                }
                Tweet tweet = tweets.get(position);
                if (tweet != null) {
                        TextView tweetText = (TextView) v.findViewById(R.id.tweetTxt);
                        if (tweetText != null) {
                              tweetText.setText("Tweet: "+tweet.getTweet());                            }

                }
                return v;
        }

    }

}
