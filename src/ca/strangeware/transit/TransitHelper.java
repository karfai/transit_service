package ca.strangeware.transit;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

public class TransitHelper {
	public interface Callback {
		void onAnswer(Intent i);
	}
	
	private Callback _callback;
	private BroadcastReceiver _receiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			_callback.onAnswer(intent);
		}
	};
	private Activity _activity;
		
	public TransitHelper(Callback c) {
		_callback = c;
	}
	
	public static final String NEARBY_INTENT = "ca.strangeware.transit.answers.nearby";
	public static final String ARRIVALS_INTENT = "ca.strangeware.transit.answers.arrivals";
	
	public void register(Activity act, String[] intents) {
		_activity = act;
		for (String k : intents) {
			IntentFilter filt = new IntentFilter(k);
			_activity.registerReceiver(_receiver, filt);
		}
	}
	
	public void unregister() {
		if (null != _activity) {
			_activity.unregisterReceiver(_receiver);
		}
	}
}
