package jp.ac.ritsumei.scrambledegg.room;

import jp.ac.ritsumei.scrambledegg.R;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

public class ResultActivity extends Activity{
	TextView resultText1;
	TextView resultText2;
	
	Button endButton;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// タイトルを非表示にします。
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		// splash.xmlをViewに指定します。
		setContentView(R.layout.result);
		
		Intent intent = getIntent();
		
		int myFinalEggs = intent.getIntExtra("myFinalEggs", 0);
		int enemyFinalEggs = intent.getIntExtra("enemyFinalEggs", 0);
		
		resultText1 = (TextView) findViewById(R.id.resultText1);

		resultText2 = (TextView) findViewById(R.id.resultText2);
		
		resultText1.setText(myFinalEggs + " - " + enemyFinalEggs);
		
		if (myFinalEggs > enemyFinalEggs) {
			resultText2.setText("WIN!: Your team cooked bigger Scrambled Egg!!");
		} else if(myFinalEggs < enemyFinalEggs) {
			resultText2.setText("LOSE: Your team cooked smaller Scrambled Egg...");
		} else {
			resultText2.setText("DRAW");
		}
		
		endButton = (Button)findViewById(R.id.button);
		endButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				startActivity(new Intent(ResultActivity.this,TitleActivity.class));
			}
		});
	}
}
