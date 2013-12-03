package jp.ac.ritsumei.scrambledegg;

import java.util.Timer;
import java.util.TimerTask;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

public class GameInfomationGetter extends Service {

	private final String URI = "http://10.0.2.2:8080/ScrambledEgg/MainScrambledEggServlet";
	private HttpClient httpClient;
	private Timer infoGetTimer;
	
	
	/* (non-Javadoc)
	 * @see android.app.Service#onBind(android.content.Intent)
	 */
	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}
	
	
	/* (non-Javadoc)
	 * @see android.app.Service#onCreate()
	 */
	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();
		
		GetInfoTimerTask task = new GetInfoTimerTask();
		infoGetTimer.schedule(task, 0, 5000);//5秒サイクルで実行
	}

	/* (non-Javadoc)
	 * @see android.app.Service#onDestroy()
	 */
	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		
		infoGetTimer.cancel();
	}
	
	
	private class GetInfoTimerTask extends TimerTask{

		/* (non-Javadoc)
		 * @see java.util.TimerTask#run()
		 */
		@Override
		public void run() {
			httpClient  = new DefaultHttpClient();
			HttpGet request = new HttpGet(URI);
			HttpResponse httpResponse = null;
			
			try {
			    httpResponse = httpClient.execute(request);
			    Log.i("egg",""+httpResponse.getStatusLine().getStatusCode());
			} catch (Exception e) {
				e.printStackTrace();
			    Log.d("HttpSampleActivity", "Error Execute");
			}	
		}
	}
}
