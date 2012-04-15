package ca.strangeware.transit;

import java.util.Date;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleAdapter;

public class TransitActivity extends Activity implements TransitHelper.Callback {
	public void onAnswer(Intent intent) {
		if (intent.getAction().equals(TransitHelper.NEARBY_INTENT)) {
			showStop(intent);
		} else if (intent.getAction().equals(TransitHelper.ARRIVALS_INTENT)) {
			showTrip(intent);
		}
	}
	
	private TransitHelper _helper;
	private ArrayList<Map<String, String>> _list = new ArrayList<Map<String,String>>();
	private SimpleAdapter _adapter;
	private ListView _listView;
	
	private void showStop(Intent intent) {
		Bundle extras = intent.getExtras();
		HashMap<String, String> item = new HashMap<String, String>();
		item.put("title", extras.getString("stop.name"));
		_list.add(item);
		_adapter.notifyDataSetChanged();
	}
	
	private int minutesFromNow(int secondsSinceMidnight) {
		Date now = new Date();
		int elapsedSinceMidnight = now.getHours() * 3600 + now.getMinutes() * 60 + now.getSeconds();
		return (secondsSinceMidnight - elapsedSinceMidnight) / 60;
	}
	
	private void showTrip(Intent intent) {
		Bundle extras = intent.getExtras();
		HashMap<String, String> item = new HashMap<String, String>();
		item.put("title", String.format("%d %s (%d minutes)",
				extras.getInt("trip.route"),
				extras.getString("trip.headsign"),
				minutesFromNow(extras.getInt("arrival"))));
		_list.add(item);
		_adapter.notifyDataSetChanged();
	}
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        _helper = new TransitHelper(this);
        _helper.register(this, new String[] { TransitHelper.ARRIVALS_INTENT });
        
        setContentView(R.layout.main);
        
        _adapter = new SimpleAdapter(this, _list, android.R.layout.simple_list_item_1, new String[] {"title"}, new int[] {android.R.id.text1});
        
        _listView = (ListView) findViewById(R.id.listView1);
        _listView.setAdapter(_adapter);
        
        registerListeners();
    }

	private void registerListeners() {
		Button b = (Button) findViewById(R.id.button1);
        b.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent i = new Intent("ca.strangeware.transit.arrivals");
				i.putExtra("request_id", UUID.randomUUID().toString());
				i.putExtra("stop.number", 3010);
				_list.clear();
				_adapter.notifyDataSetInvalidated();
				startService(i);
			}
		});
        
        b = (Button) findViewById(R.id.button2);
        b.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent i = new Intent("ca.strangeware.transit.nearby");
				i.putExtra("request_id", UUID.randomUUID().toString());
				i.putExtra("location.lat", 45.40412);
				i.putExtra("location.lon", -75.7405);
				_list.clear();
				_adapter.notifyDataSetInvalidated();
				startService(i);
			}
		});
	}
}