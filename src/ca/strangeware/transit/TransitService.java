package ca.strangeware.transit;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Process;
import android.util.Log;

public class TransitService extends Service {
	private static final String API = "http://octranspo-api.heroku.com/";
	
	private static final String ACTION_NEARBY = "ca.strangeware.transit.nearby";
	private static final String ACTION_ARRIVALS = "ca.strangeware.transit.arrivals";
	private static final String ACTION_ARRIVALS_WATCH = "ca.strangeware.transit.arrivals.watch";
	
	private static final int WHAT_UNKNOWN = 0;
	private static final int WHAT_NEARBY= 1;
	private static final int WHAT_ARRIVALS = 2;
	private static final int WHAT_ARRIVALS_WATCH = 3;	
	
	private Looper _looper;
	private OurHandler _handler;
	private HashMap<String, Integer> _whats = new HashMap<String, Integer>(){{
		put(ACTION_NEARBY, WHAT_NEARBY);
		put(ACTION_ARRIVALS, WHAT_ARRIVALS);
		put(ACTION_ARRIVALS_WATCH, WHAT_ARRIVALS_WATCH);
	}};
		
	private final class OurHandler extends Handler {
		private HttpClient _client;
		public OurHandler(Looper l) {
			super(l);
			_client = new DefaultHttpClient();
		}
		
		@Override
		public void handleMessage(Message m) {
			try {
				switch (m.what) {
				case WHAT_NEARBY:
					nearby(m.getData());
					break;
				case WHAT_ARRIVALS:
					arrivals(m.getData());
					break;
				case WHAT_ARRIVALS_WATCH:
					//nearbyStopsRepeat();
					break;
				default:
					return;
				}
			} catch (ClientProtocolException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (JSONException e){
				e.printStackTrace();
			}
		}
		
		private JSONArray getArray(String url) throws JSONException, IOException {
			String endpoint = TransitService.API + url;
			HttpGet get = new HttpGet(endpoint);
			
			Log.d("Starting", "Starting the request");
			HttpResponse response = _client.execute(get);
			BufferedReader reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
			Log.d("Received Data", "Data received");
			
			return new JSONArray(reader.readLine());			
		}
		
		public void nearby(Bundle args) throws ClientProtocolException, IOException, JSONException {
			String url = String.format("stops_nearby/%f/%f", args.getDouble("location.lat"), args.getDouble("location.lon"));
			Log.d("Service", "URL: " + url);
			JSONArray json = getArray(url);
			for(int i = 0, len = json.length(); i < len; i++) {
				Bundle extras = new Bundle();
				Intent intent = new Intent("ca.strangeware.transit.answers.nearby");
				
				JSONObject o = json.getJSONObject(i);
				JSONObject stop = o.getJSONObject("stop");

				extras.putString("request_id", args.getString("request_id"));
				extras.putInt("distance", o.getInt("distance"));

				extras.putInt("stop.number", stop.getInt("number"));
				extras.putString("stop.name", stop.getString("name"));
				extras.putDouble("stop.lat", stop.getDouble("lat"));
				extras.putDouble("stop.lon", stop.getDouble("lon"));	
				
				intent.putExtras(extras);
				Log.d("Nearby", "Broadcasting!");
				sendBroadcast(intent);
			}
		}
		
		public void arrivals(Bundle args) throws JSONException, IOException  {
			String url = String.format("arrivals/%d", args.getInt("stop.number"));
			JSONArray json = getArray(url);
			for (int i = 0, len = json.length(); i < len; i++) {
				Bundle extras = new Bundle();
				Intent intent = new Intent("ca.strangeware.transit.answers.arrivals");
				JSONObject o = json.getJSONObject(i);
				JSONObject stop = o.getJSONObject("stop");
				JSONObject trip = o.getJSONObject("trip");
				
				extras.putString("request_id", args.getString("request_id"));
				
				extras.putInt("arrival", o.getInt("arrival"));
				extras.putInt("departure", o.getInt("departure"));
				
				extras.putInt("stop.number", stop.getInt("number"));
				extras.putString("stop.name", stop.getString("name"));
				
				extras.putInt("trip.route", trip.getInt("route"));
				extras.putString("trip.headsign", trip.getString("headsign"));
				
				intent.putExtras(extras);
				sendBroadcast(intent);
			}
		}
		
		public void nearbyStopsRepeat() throws IOException, ClientProtocolException, JSONException{
		}
	}
	
	public TransitService() {
	}

	@Override
	public void onCreate() {
		HandlerThread th = new HandlerThread("ServiceStartArguments", Process.THREAD_PRIORITY_BACKGROUND);
		th.start();
		
		_looper = th.getLooper();
		_handler = new OurHandler(_looper);
	}
	
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}
	
	private int makeWhatFromAction(String act) {
		int rv = WHAT_UNKNOWN;
		if ( _whats.containsKey(act) ) {
			rv = _whats.get(act);
		}
		return rv;
	}
	
	@Override
	public int onStartCommand(Intent i, int flags, int startId) {
		Message m = _handler.obtainMessage();
		m.what = makeWhatFromAction(i.getAction());
		m.setData(i.getExtras());
		_handler.sendMessage(m);
		return START_STICKY;
	}
}
