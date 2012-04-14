package ca.strangeware.transit;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Process;

public class TransitService extends Service {
	private Looper _looper;
	private OurHandler _handler;
	
	private final class OurHandler extends Handler {
		public OurHandler(Looper l) {
			super(l);
		}
		
		@Override
		public void handleMessage(Message m) {
			
		}
	}
	
	public TransitService() {
	}

	@Override
	public void onCreate() {
		HandlerThread th = new HandlerThread("ServiceStartArguments", Process.THREAD_PRIORITY_BACKGROUND);
		_looper = th.getLooper();
		_handler = new OurHandler(_looper);
	}
	
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}
	
	@Override
	public int onStartCommand(Intent i, int flags, int startId) {
		Message m = _handler.obtainMessage();
		// TODO: setup stuff
		_handler.sendMessage(m);
		return START_STICKY;
	}
}
