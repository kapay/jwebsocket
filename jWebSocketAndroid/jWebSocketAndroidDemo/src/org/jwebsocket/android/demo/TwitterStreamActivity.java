// ---------------------------------------------------------------------------
// jWebSocket - Copyright (c) 2010 Innotrade GmbH
// ---------------------------------------------------------------------------
// This program is free software; you can redistribute it and/or modify it
// under the terms of the GNU Lesser General Public License as published by the
// Free Software Foundation; either version 3 of the License, or (at your
// option) any later version.
// This program is distributed in the hope that it will be useful, but WITHOUT
// ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
// FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
// for more details.
// You should have received a copy of the GNU Lesser General Public License
// along with this program; if not, see <http://www.gnu.org/licenses/lgpl.html>.
// ---------------------------------------------------------------------------
package org.jwebsocket.android.demo;

import android.app.ListActivity;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jwebsocket.android.demo.ImageThreadLoader.ImageLoadedListener;
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

    private ArrayList<Tweet> mTweets = null;
    private TweetAdapter mTweetAdapter;
    TwitterStreamSettingsActivity lSettingsDialog;
    private final int CAPACITY = 50;

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setContentView(R.layout.twitter_stream_activity);
        this.mTweets = new ArrayList<Tweet>(CAPACITY);
        this.mTweetAdapter = new TweetAdapter(this, R.layout.tweet_row, mTweets);
        setListAdapter(mTweetAdapter);

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
                if (lSettingsDialog == null) {
                    lSettingsDialog =
                            new TwitterStreamSettingsActivity(this, new TwitterStreamSettingsActivity.TwitterSettingsListener() {

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
                }
                lSettingsDialog.show();
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
        if (aToken.getNS().equals("org.jwebsocket.plugins.twitter")
                && aToken.getType().equals("event")
                && aToken.getString("name").equals("status")) {
            mTweets.add(0, new Tweet(aToken.getString("status"), aToken.getString("userImgURL"), aToken.getString("userName")));

            mTweetAdapter.notifyDataSetChanged();
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

    public class Tweet {

        private String tweet;
        private String userImgURL;
        private String userName;

        public Tweet(String tweet, String userImageUrl, String userName) {
            this.tweet = tweet;
            this.userImgURL = userImageUrl;
            this.userName = userName;
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

        /**
         * @return the userImgURL
         */
        public String getUserImgURL() {
            return userImgURL;
        }

        /**
         * @param userImgURL the userImgURL to set
         */
        public void setUserImgURL(String userImgURL) {
            this.userImgURL = userImgURL;
        }

        /**
         * @return the userName
         */
        public String getUserName() {
            return userName;
        }

        /**
         * @param userName the userName to set
         */
        public void setUserName(String userName) {
            this.userName = userName;
        }
    }

    public class TweetAdapter extends ArrayAdapter<Tweet> {

        private ArrayList<Tweet> tweets;
        private ImageThreadLoader imageLoader = new ImageThreadLoader();

        public TweetAdapter(Context context, int textViewResourceId, ArrayList<Tweet> tweets) {
            super(context, textViewResourceId, tweets);
            this.tweets = tweets;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View v = convertView;
            if (v == null) {
                LayoutInflater vi = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                v = vi.inflate(R.layout.tweet_row, null);
            }
            Tweet tweet = tweets.get(position);
            if (tweet != null) {
                TextView tweetText = (TextView) v.findViewById(R.id.tweetTxt);
                if (tweetText != null) {
                    tweetText.setText(tweet.getTweet());
                }
                final ImageView userImage = (ImageView) v.findViewById(R.id.userImg);
                Bitmap cachedImage = null;
                try {
                    cachedImage = imageLoader.loadImage(tweet.userImgURL, new ImageLoadedListener() {

                        public void imageLoaded(Bitmap imageBitmap) {
                            userImage.setImageBitmap(imageBitmap);
                            notifyDataSetChanged();
                        }
                    });
                } catch (MalformedURLException e) {
                }
                if (cachedImage != null) {
                    userImage.setImageBitmap(cachedImage);
                }


            }
            return v;
        }
    }
}
