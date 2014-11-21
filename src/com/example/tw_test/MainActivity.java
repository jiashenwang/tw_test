package com.example.tw_test;

import java.io.Serializable;

import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.User;
import twitter4j.auth.AccessToken;
import twitter4j.auth.RequestToken;
import twitter4j.conf.Configuration;
import twitter4j.conf.ConfigurationBuilder;
import android.support.v7.app.ActionBarActivity;
import android.text.Html;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.ActivityInfo;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Parcelable;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends ActionBarActivity {
	final static String TWITTER_CONSUMER_KEY = "D3Fsen8SZ3yurMgpaOG0gnwws";
	final static String TWITTER_CONSUMER_SECRET = "07sgrXhPY1iL8scZ5UlhdwIYFRvYq2fCWG0F8cE5IADPYTrN25";
	//final static String TWITTER_CALLBACK_URL = "tw://com.example.tw_test.MainActivity";
	public static final String  OAUTH_CALLBACK_SCHEME   = "mytwitterapp";
	public static final String  OAUTH_CALLBACK_HOST     = "oauth";
	public static final String  TWITTER_CALLBACK_URL      = OAUTH_CALLBACK_SCHEME + "://" + OAUTH_CALLBACK_HOST;
	
	static String PREFERENCE_NAME = "twitter_oauth";
    public static final String PREF_KEY_OAUTH_TOKEN = "oauth_token";
    public static final String PREF_KEY_OAUTH_SECRET = "oauth_token_secret";
	public static final String USER_ID = "user_id";
	public static final String POSTS_AMOUNT = "amount";
    static final String PREF_KEY_TWITTER_LOGIN = "isTwitterLogedIn";
    

    // Twitter oauth urls
    static final String URL_TWITTER_AUTH = "auth_url";
    static final String URL_TWITTER_OAUTH_VERIFIER = "oauth_verifier";
    static final String URL_TWITTER_OAUTH_TOKEN = "oauth_token";
	
    private User user;
    private AccessToken accessToken;
    private static Twitter twitter;
    private static RequestToken requestToken;
	Button login, logout, display;
	TextView welcome;
	NumberPicker np;
	private ConnectionDetector cd;
	private static SharedPreferences mSharedPreferences;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        cd = new ConnectionDetector(getApplicationContext());
        
        // Check if Internet present
        if (!cd.isConnectingToInternet()) {
            // Internet Connection is not present
        	Toast.makeText(getApplicationContext(), "Internet Connection Error", Toast.LENGTH_SHORT).show();
            // stop executing code by return
            return;
        }
        if(TWITTER_CONSUMER_KEY.trim().length() == 0 || TWITTER_CONSUMER_SECRET.trim().length() == 0){
            // Internet Connection is not present
        	Toast.makeText(getApplicationContext(), "Please set your twitter oauth tokens first!", Toast.LENGTH_SHORT).show();
            // stop executing code by return
            return;
        }
        
        login = (Button) findViewById(R.id.login);
        logout = (Button) findViewById(R.id.logout);
        display = (Button) findViewById(R.id.view_posts);
        welcome = (TextView) findViewById(R.id.welcome);
        np = (NumberPicker) findViewById(R.id.number_picker);
        np.setMaxValue(100);
        np.setMinValue(1);
        
        display.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
                
               	Intent intent = new Intent(getBaseContext(), resultActivity.class);
            	intent.putExtra(USER_ID, accessToken.getUserId()+"");
            	intent.putExtra(POSTS_AMOUNT, np.getValue()+"");
            	intent.putExtra("TWITTER", (Serializable)twitter);
            	startActivity(intent);
            	
			}
		});
        
        // Shared Preferences
        mSharedPreferences = getApplicationContext().getSharedPreferences("MyPref", 0);
        
        /**
         * Twitter login button click event will call loginToTwitter() function
         * */
        login.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                // Call login twitter function
                loginToTwitter();
            }
        });
        
        /**
         * Button click event for logout from twitter
         * */
        logout.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                // Call logout twitter function
                logoutFromTwitter();
            }
        });
        

        
        if (!isTwitterLoggedInAlready()) {
            Uri uri = getIntent().getData();
            if (uri != null && uri.toString().startsWith(TWITTER_CALLBACK_URL)) {

                // oAuth verifier
                String verifier = uri.getQueryParameter(URL_TWITTER_OAUTH_VERIFIER);
                new OAuthAccessTokenTask().execute(verifier);
            }
        }
        
    }
    
    private class OAuthAccessTokenTask extends AsyncTask<String, Void, Exception>
    {
       @Override
       protected Exception doInBackground(String... params) {
           Exception toReturn = null;

           try {
               accessToken = twitter.getOAuthAccessToken(requestToken, params[0]);
               user = twitter.showUser(accessToken.getUserId());

           }
           catch(TwitterException e) {
               Log.e(MainActivity.class.getName(), "TwitterError: " + e.getErrorMessage());
               toReturn = e;
           }
           catch(Exception e) {
               Log.e(MainActivity.class.getName(), "Error: " + e.getMessage());
               toReturn = e;
           }

           return toReturn;
       }

       @Override
       protected void onPostExecute(Exception exception) {
           onRequestTokenRetrieved(exception);
       }
    }
    private void onRequestTokenRetrieved(Exception result) {
    	
        if (result != null) {
            Toast.makeText(
                    this, 
                    result.getMessage(), 
                    Toast.LENGTH_LONG
                    ).show();
        }

        else {
            try {
                // Shared Preferences
                Editor editor = mSharedPreferences.edit();

                // After getting access token, access token secret
                // store them in application preferences
                editor.putString(PREF_KEY_OAUTH_TOKEN, accessToken.getToken());
                editor.putString(PREF_KEY_OAUTH_SECRET, accessToken.getTokenSecret());
                // Store login status - true
                editor.putBoolean(PREF_KEY_TWITTER_LOGIN, true);
                editor.commit(); // save changes

                Log.e("Twitter OAuth Token", "> " + accessToken.getToken());

                login.setVisibility(View.GONE);

                logout.setVisibility(View.VISIBLE);
                display.setVisibility(View.VISIBLE);
                np.setVisibility(View.VISIBLE);

                // Getting user details from twitter
                String username = user.getName();

                // Displaying in xml ui
                welcome.setText(Html.fromHtml("<b>Welcome " + username + "</b>"));
            }
            catch (Exception ex) {
                // Check log for login errors
                Log.e("Twitter Login Error", "> " + ex.getMessage());
                ex.printStackTrace();
            }
        }
    }
    
    public class ConnectionDetector {
        
        private Context _context;
         
        public ConnectionDetector(Context context){
            this._context = context;
        }
     
        public boolean isConnectingToInternet(){
            ConnectivityManager connectivity = (ConnectivityManager) _context.getSystemService(Context.CONNECTIVITY_SERVICE);
              if (connectivity != null) 
              {
                  NetworkInfo[] info = connectivity.getAllNetworkInfo();
                  if (info != null) 
                      for (int i = 0; i < info.length; i++) 
                          if (info[i].getState() == NetworkInfo.State.CONNECTED)
                          {
                              return true;
                          }
     
              }
              return false;
        }
    }
    
    private boolean isTwitterLoggedInAlready() {
        // return twitter login status from Shared Preferences
        return mSharedPreferences.getBoolean(PREF_KEY_TWITTER_LOGIN, false);
    }
    
    private void loginToTwitter() {
        // Check if already logged in
        if (!isTwitterLoggedInAlready()) {
            ConfigurationBuilder builder = new ConfigurationBuilder();
            builder.setOAuthConsumerKey(TWITTER_CONSUMER_KEY);
            builder.setOAuthConsumerSecret(TWITTER_CONSUMER_SECRET);
            Configuration configuration = builder.build();

            TwitterFactory factory = new TwitterFactory(configuration);
            twitter = factory.getInstance();

                Thread thread = new Thread(new Runnable(){
                    @Override
                    public void run() {
                        try {
                            requestToken = twitter.getOAuthRequestToken(TWITTER_CALLBACK_URL);
                            MainActivity.this.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(requestToken.getAuthenticationURL())));

                        } catch (Exception e) {
                            e.printStackTrace();
                            //Toast.makeText(getApplicationContext(), "Already Logged into twitter", Toast.LENGTH_LONG).show();
                        }
                    }
                });
                thread.start();         
        } else {
            // user already logged into twitter
            Toast.makeText(getApplicationContext(), "Already Logged into twitter", Toast.LENGTH_LONG).show();
        }
    }
    
    /**
     * Function to logout from twitter
     * It will just clear the application shared preferences
     * */
    private void logoutFromTwitter() {
        // Clear the shared preferences
        Editor e = mSharedPreferences.edit();
        e.remove(PREF_KEY_OAUTH_TOKEN);
        e.remove(PREF_KEY_OAUTH_SECRET);
        e.remove(PREF_KEY_TWITTER_LOGIN);
        e.commit();
        twitter = null;

        logout.setVisibility(View.GONE);
        welcome.setText("");
        welcome.setVisibility(View.GONE);
        display.setVisibility(View.INVISIBLE);
        np.setVisibility(View.INVISIBLE);

        login.setVisibility(View.VISIBLE);
    }
    
    protected void onResume() {
        super.onResume();
    }
}
