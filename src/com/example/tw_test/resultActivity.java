package com.example.tw_test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import oauth.signpost.OAuthConsumer;

import twitter4j.Paging;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.auth.AccessToken;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class resultActivity extends Activity {
    
	
	static ListView listview;
	static String oauth_token, oauth_token_secret, user_id, amount, consumer_key, consumer_secret;
	Twitter twitter;
	
	@Override
	protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);
        
        Intent i=getIntent(); 
        user_id = i.getStringExtra(MainActivity.USER_ID); 
		amount = i.getStringExtra(MainActivity.POSTS_AMOUNT);
		twitter = (Twitter)getIntent().getSerializableExtra("TWITTER");

		
		listview = (ListView)findViewById(R.id.result_list);
		
		Entity entity = new Entity(twitter, Long.valueOf(user_id).longValue(), Integer.parseInt(amount));
		
		GetPosts info = new GetPosts(resultActivity.this, listview);
		info.execute(entity);
		
	}
}


class GetPosts extends AsyncTask<Entity, Void, List<twitter4j.Status>>{

	Context c;
	ListView listview;
	GetPosts(Context context, ListView Listview){
		c = context;
		listview = Listview;
	}
	

	@Override
	protected List<twitter4j.Status> doInBackground(Entity... params) {
		// TODO Auto-generated method stub
		List<twitter4j.Status> statuses = null;
		Entity entity = params[0];
		Paging paging = new Paging();
		paging.count(entity.amount);
		try {
			statuses = entity.twitter.getUserTimeline(entity.id,paging);
			Log.wtf("~~~~~~~~~~~~~", statuses.get(0)+"");
			
			
		} catch (TwitterException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		return statuses;
	}
	
	protected void onPostExecute(List<twitter4j.Status> posts) {
		// TODO Auto-generated method stub
		BarAdapter adapter = new BarAdapter(c, posts);
		listview.setAdapter(adapter);
	}

	
}

//bar adapter for list view
@SuppressLint("ViewHolder") class BarAdapter extends BaseAdapter
{
	List<twitter4j.Status> posts = null;
	private Context context;
	
	BarAdapter(Context c, List<twitter4j.Status> POSTS){
		context = c;
		posts = POSTS;
	}
	
	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return posts.size();
	}

	@Override
	public Object getItem(int arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View row=inflater.inflate(R.layout.single_row, parent, false);
		TextView id = (TextView)row.findViewById(R.id.id);
		TextView date = (TextView)row.findViewById(R.id.date);
		TextView message = (TextView)row.findViewById(R.id.message);
		
		
		id.setText(posts.get(position).getId()+"");
		date.setText(posts.get(position).getText());
		message.setText(posts.get(position).getCreatedAt()+"");
		
		return row;
	}
	
}

class Entity{
	Twitter twitter;
	long id;
	int amount;
	Entity(Twitter tw, long ID, int Amount){
		twitter = tw;
		id = ID;
		amount =Amount;
	}
	
}