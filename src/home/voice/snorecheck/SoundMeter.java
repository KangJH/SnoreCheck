package home.voice.snorecheck;

import java.io.File;
import java.io.IOException;

import android.content.Context;
import android.media.MediaRecorder;
import android.os.Environment;
import android.util.Log;

public class SoundMeter extends Object {
	private MediaRecorder mRecorder = null;
	private Context mContext = null;
	private static final String AUDIO_RECORDER_FILE_EXT_3GP = ".3gp";
    //private static final String AUDIO_RECORDER_FILE_EXT_MP4 = ".mp4";
    //private static final String AUDIO_RECORDER_FOLDER = "AudioRecorder";
    private static final int TYPE_NONE = 0;
    private static final int TYPE_CHECKING = 1;
    private static final int TYPE_RECORDING = 2;
	private int mCurrentType = TYPE_NONE;
    public SoundMeter(Context context) {
    	mContext = context;
		if (mRecorder == null) {
            mRecorder = new MediaRecorder();
		}
	}
	
	public void release() {
		if (mRecorder != null) {
        	mRecorder.stop();    
        	mRecorder.reset();
            mRecorder.release();
            mRecorder = null;
        }
	}
    public void startChecking() {
        if (mRecorder != null) {
        	setConfig(TYPE_CHECKING);
            mRecorder.start();
        }
    }

    public void stopChecking() {
        if (mRecorder != null) {
        	mRecorder.stop();
        	mRecorder.reset();
        }
    }
    
    public void startRecording() {
    	if (mRecorder != null) {
        	setConfig(TYPE_RECORDING);
            mRecorder.start();
        }
    }

    public void stopRecording() {
        if (mRecorder != null) {
        	mRecorder.stop();
        	mRecorder.reset();
        }
    }
    
    public double getAmplitude() {
        if (mRecorder != null)
        	return  mRecorder.getMaxAmplitude();
        else
            return 0;
    }
    
    public boolean isRecording() {
    	boolean ret = false;
    	if(mCurrentType == TYPE_RECORDING) {
    		ret = true;
    	}
    	return ret;
    }

    private void setConfig(int type) {
    	mCurrentType = type;
    	mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
        if(mCurrentType == TYPE_CHECKING) {
        	mRecorder.setOutputFile("/dev/null"); 
        } else {
        	mRecorder.setOutputFile(getFilename());
        }
        mRecorder.setOnErrorListener(errorListener);
        mRecorder.setOnInfoListener(infoListener);
        try {
			mRecorder.prepare();
		} catch (IllegalStateException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
    }

    private MediaRecorder.OnErrorListener errorListener = new MediaRecorder.OnErrorListener() {
        @Override
        public void onError(MediaRecorder mr, int what, int extra) {
            //Toast.makeText(mActivity, "Error: " + what + ", " + extra, Toast.LENGTH_SHORT).show();
        	Log.e("SnoreCheck", "Error: " + what + ", " + extra);
        }
    };

    private MediaRecorder.OnInfoListener infoListener = new MediaRecorder.OnInfoListener() {
        @Override
        public void onInfo(MediaRecorder mr, int what, int extra) {
            //Toast.makeText(mActivity, "Warning: " + what + ", " + extra, Toast.LENGTH_SHORT).show();
        	Log.e("SnoreCheck", "Warning: " + what + ", " + extra);
        }
    };
    
    private String getFilename() {
        String filepath = Environment.getExternalStorageDirectory().getPath();
        File file = new File(filepath, mContext.getResources().getString(R.string.audio_recorder_folder));
        if (!file.exists()) {
            file.mkdirs();
        }
        return (file.getAbsolutePath() + "/" + System.currentTimeMillis() + AUDIO_RECORDER_FILE_EXT_3GP);
    }
    
    /*public String getRecordingFolderPath() {
        String filepath = Environment.getExternalStorageDirectory().getPath();
        File file = new File(filepath, mContext.getResources().getString(R.string.audio_recorder_folder));
        if (!file.exists()) {
            file.mkdirs();
        }
        return file.getAbsolutePath();
    }*/
}
