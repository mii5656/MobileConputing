package jp.ac.ritsumei.scrambledegg;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;

public class MainActivity extends Activity {

	
	private final String URI = "http://10.0.2.2:8080/ScrambledEgg/MainScrambledEggServlet";
	private HttpClient httpClient;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		GetMessageTask getMessageTask = new GetMessageTask();
		getMessageTask.execute(URI);
		
	}

	
	private class GetMessageTask extends AsyncTask<String, Integer, Integer> {
		 @Override
		    protected Integer doInBackground(String... contents) {
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
		        return 1;
		    }
	}
}