package home.voice.snorecheck;

import java.util.Timer;
import java.util.TimerTask;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

public class SnoreCheckService extends Service {
	//private Activity		mActivity = null;
	private SoundMeter mSoundMeter = null;
	private TimerTask mTask = null;
    private Timer mTimer = null;
    private final static double THRESHHOLD_UP_LIMIT = 200;
    private final static double THRESHHOLD_DOWN_LIMIT = 100;
    private final static int 	SAMPLING_SPACE = 1000;
    private final static int	CHECK_PERIOD = 10000; 
    private final int 			MAX_CHECK_COUNT =  CHECK_PERIOD / SAMPLING_SPACE;
	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public void onStart(Intent intent, int startId) {
	    handleCommand(intent);
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
	    handleCommand(intent);
	    // We want this service to continue running until it is explicitly
	    // stopped, so return sticky.
	    return START_STICKY;
	}

	private void handleCommand(Intent intent) {
		// TODO Auto-generated method stub
		if(mSoundMeter == null) {
			mSoundMeter = new SoundMeter(this);
			mSoundMeter.startChecking();
			mTask = new TimerTask() {
	            @Override
	            public void run() {
	            	double dB = mSoundMeter.getAmplitude();
	            	Log.d("Test", "dB: " + dB);
	            	if(dB > 0) {
		            	if(mSoundMeter.isRecording()) {
		            		if(!checkSnoring(dB)) {
		            			mSoundMeter.stopRecording();
		            			mSoundMeter.startChecking();
				            	updateOSD("checking");
		            		} else {
		            			updateOSD("recording");
		            		}
		            	} else {
		            		if(checkSnoring(dB)) {
		            			mSoundMeter.stopChecking();
		            			mSoundMeter.startRecording();
		            			updateOSD("recording");
			            	} else {
			            		updateOSD("checking");
			            	}
		            	}
	            	}
	            }
	        };
	        mTimer = new Timer();
	        mTimer.schedule(mTask, 0, SAMPLING_SPACE);
		}
	}
	
    @Override
    public void onDestroy() { 
        super.onDestroy();
        if(mSoundMeter != null) {
			mSoundMeter.release();
			mTask.cancel();
			mTimer.cancel();
			mSoundMeter = null;
		}
    }
    
    private void updateOSD(String status) {
    	Intent intent = new Intent(getResources().getString(R.string.communication_intent));
    	  // You can also include some extra data.
    	  intent.putExtra("message", "update");
    	  intent.putExtra("status", status);
    	  LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }
    private int mCheckingCount = 0;
    //private boolean mLastCheckSnoringRet = false; 
    private double mdBArray[] = null;
    private boolean checkSnoring(double dB) {
    	boolean ret = false;
    	if(mdBArray == null) {
    		mdBArray = new double[MAX_CHECK_COUNT];
    	}
    	
    	if(mSoundMeter.isRecording()) {
    		ret = true;
    		mdBArray[mCheckingCount] = dB;
        	if(mCheckingCount == MAX_CHECK_COUNT - 1) {
        		mCheckingCount = 0;
        		int iUnderdBCount = 0;
        		for(int i = 0; i < MAX_CHECK_COUNT; i++) {
        			if(mdBArray[i] < THRESHHOLD_DOWN_LIMIT) {
        				iUnderdBCount++;
        			}
        		}
        		if(iUnderdBCount > MAX_CHECK_COUNT / 2) {
        			ret = false;
        		}
        	} else {
        		mCheckingCount++;
        	}
    	} else {
    		mdBArray[mCheckingCount] = dB;
        	if(mCheckingCount == MAX_CHECK_COUNT - 1) {
        		mCheckingCount = 0;
        		int iOverdBCount = 0;
        		for(int i = 0; i < MAX_CHECK_COUNT; i++) {
        			if(mdBArray[i] > THRESHHOLD_UP_LIMIT) {
        				iOverdBCount++;
        			}
        		}
        		if(iOverdBCount >= MAX_CHECK_COUNT / 4) {
        			ret = true;
        		}
        	} else {
        		mCheckingCount++;
        	}
    	}
    	return ret;
    }
    	
    
}
