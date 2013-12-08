package jp.ac.ritsumei.scrambledegg;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;


public class GameInfoGetterService extends Service {

	private HttpClient httpClient;
	private Timer infoGetTimer;
	private HttpGet request;
	private HttpResponse httpResponse;
	
	private int roomID = 1;
	
	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}
	
	
	@Override
	public void onCreate() {
		super.onCreate();
		
		GetInfoTimerTask task = new GetInfoTimerTask();
		infoGetTimer = new Timer();
		infoGetTimer.schedule(task, 1000, 5000);//5秒サイクルで実行	
		
		httpClient  = new DefaultHttpClient();	
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		infoGetTimer.cancel();
		httpClient.getConnectionManager().shutdown();
	}
	
	
	private class GetInfoTimerTask extends TimerTask{

		@Override
		public void run() {
			httpResponse = null;
			
			try {
				//パラメータの設定(URLに付加する)
				ArrayList<NameValuePair> params = new ArrayList<NameValuePair>();
				params.add(new BasicNameValuePair("roomID", ""+roomID));
				String query = URLEncodedUtils.format(params, "UTF-8");
				
				request = new HttpGet(Constants.URI+"?"+query);
				httpResponse = httpClient.execute(request);
			    
			    //成功ならブロードキャスト
			    if(httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK){
			    	Intent getInfo = new Intent();
			    	getInfo.setAction(Constants.GET_DATA);
			    	getInfo.putExtra(Constants.DATA, EntityUtils.toString(httpResponse.getEntity()));
			    	sendBroadcast(getInfo);
			    }else{
			    	Log.e("service", "network err :"+ httpResponse.getStatusLine().getStatusCode());
			    }
			}catch (Exception e){
				e.printStackTrace();
			    Log.d("HttpSampleActivity", "Error Execute");
			}
		}
	}
}
