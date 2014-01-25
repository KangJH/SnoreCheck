package home.voice.snorecheck;

import java.io.File;

import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Parcelable;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.LocalBroadcastManager;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends Activity implements OnClickListener {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//mHandler = new Handler();
		setContentView(R.layout.activity_main);
		findViewById(R.id.play_btn).setOnClickListener(this);
		LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver, new IntentFilter(getResources().getString(R.string.communication_intent)));
		Intent intent = new Intent(this, SnoreCheckService.class);
		startService(intent);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		stopService(new Intent(this, SnoreCheckService.class));
		LocalBroadcastManager.getInstance(this).unregisterReceiver(mMessageReceiver);
	}

	private int mProgress = 0;
	private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				// TODO Auto-generated method stub
				String message = intent.getStringExtra("message");
				String strStatus = intent.getStringExtra("status");
				TextView status = (TextView) findViewById(R.id.status_text);
				if(message.equals("update")) {
					for(int i = 0; i < mProgress; i++) {
						strStatus = strStatus + ".";
					}
					mProgress++;
					if(mProgress > 3) {
						mProgress = 0;
					}
					status.setText(strStatus);
				}
			}
		};
	@Override
	public void onClick(View v) {
		switch(v.getId()) {
		case R.id.play_btn:
			Intent viewIntent = new Intent(Intent.ACTION_VIEW);
			viewIntent.setDataAndType(Uri.fromFile(getRecordingFolderPath()), "audio/*");
			startActivity(Intent.createChooser(viewIntent, null));
			break;
		default:
			break;
		}
		
	}
	
	public File getRecordingFolderPath() {
	    String filepath = Environment.getExternalStorageDirectory().getPath();
	    File file = new File(filepath, getResources().getString(R.string.audio_recorder_folder));
	    if (!file.exists()) {
	        file.mkdirs();
	    }
	    return file;
	}

}
