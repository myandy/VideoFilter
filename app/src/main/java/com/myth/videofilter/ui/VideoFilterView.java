package com.myth.videofilter.ui;

import android.content.Context;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.Toast;

import com.myth.videofilter.render.MovieRenderThread;
import com.myth.videofilter.render.MovieRenderer;
import com.myth.videofilter.render.RecordRenderThread;
import com.myth.videofilter.utils.ConfigUtils;
import com.myth.videofilter.utils.VideoInfoUtils;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.Timer;
import java.util.TimerTask;


public class VideoFilterView extends SurfaceView implements SurfaceHolder.Callback {

    private static final String TAG = VideoFilterView.class.getSimpleName();

    private RecordRenderThread mRecordThread;

    private MovieRenderThread mMovieThread;

    private Context mContext;


    private Timer mTimer = new Timer();

    private boolean mInit;

    //do not change,no support for not looping
    private boolean mIsLooping = true;

    private OnSaveProgress onSaveProgress;


    public void setNeedRestart() {
        mNeedRestart = true;
        if (mMovieThread != null) {
            mMovieThread.setNeedRestart();
        }
    }

    private boolean mNeedRestart;

    private String mOutputTempleFile = Environment.getExternalStorageDirectory().getPath() + "/output_temp.mp4";
    private String mOutputFile = Environment.getExternalStorageDirectory().getPath() + "/output.mp4";


    public VideoFilterView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;

        getHolder().addCallback(this);
    }

    public void handleSaveCompleted() {
        Log.d(TAG, "onCompleted");

        VideoInfoUtils.combineVideo(ConfigUtils.getInstance().getVideoPath(), mOutputTempleFile, mOutputFile);
        Toast.makeText(getContext(), "视频保存在：" + mOutputFile, Toast.LENGTH_SHORT).show();
    }

    public void resume() {
        if (mMovieThread != null) {
            if (mNeedRestart) {
                restart();
                mNeedRestart = false;
            } else {
                mMovieThread.manualResume();
            }
        }
    }

    public void pause() {
        if (mMovieThread != null) {
            mMovieThread.manualPause();
        }
    }

    public void onPause() {
        if (mMovieThread != null) {
            mMovieThread.onPause();
        }
    }

    public void onResume() {
        if (mMovieThread != null) {
            mMovieThread.onResume();
        }
    }

    public void onDestroy() {
        mTimer.cancel();
        mTimerTask.cancel();
        if (mMovieThread != null) {
            mMovieThread.sendQuit();
        }
        if (mRecordThread != null) {
            mRecordThread.sendQuit();
        }
    }

    /**
     * Handles messages sent from the render thread to the UI thread.
     * <p>
     * The object is created on the UI thread, and all handlers run there.
     */
    public static class ViewHandler extends Handler {
        private static final int MSG_SAVE_COMPLETED = 0;

        private static final int MSG_PLAY_COMPLETED = 1;

        private static final int MSG_SAVE_PROGRESS = 2;

        // Weak reference to the Activity; only access this from the UI thread.
        private WeakReference<VideoFilterView> mFilmViewWeakReference;

        public ViewHandler(VideoFilterView activity) {
            mFilmViewWeakReference = new WeakReference<VideoFilterView>(activity);
        }

        public void sendSaveCompleted() {
            sendMessage(obtainMessage(MSG_SAVE_COMPLETED));
        }

        public void sendSaveProgress(int progress) {
            sendMessage(obtainMessage(MSG_SAVE_PROGRESS, progress, 0));
        }

        public void sendPlayCompleted() {
            sendMessage(obtainMessage(MSG_PLAY_COMPLETED));
        }

        @Override  // runs on UI thread
        public void handleMessage(Message msg) {
            int what = msg.what;
            //Log.d(TAG, "ActivityHandler [" + this + "]: what=" + what);

            VideoFilterView activity = mFilmViewWeakReference.get();
            if (activity == null) {
                Log.w(TAG, "ActivityHandler.handleMessage: activity is null");
                return;
            }

            switch (what) {
                case MSG_SAVE_COMPLETED:
                    activity.handleSaveCompleted();
                    break;
                case MSG_PLAY_COMPLETED:
                    activity.handlerPlayCompleted();
                    break;
                case MSG_SAVE_PROGRESS:
                    activity.handlerSaveProgress(msg.arg1);
                    break;
                default:
                    throw new RuntimeException("unknown msg " + what);
            }
        }
    }


    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        Log.d(TAG, "surfaceCreated");
        if (mMovieThread == null || mMovieThread.getSurfaceHolder() == null || !mMovieThread.getSurfaceHolder().getSurface().isValid()) {
            mMovieThread = new MovieRenderThread(mContext, new ViewHandler(this), getHolder());
            mMovieThread.start();
        }
        mMovieThread.sendSurfaceCreated();
        if (mRecordThread == null) {
            mRecordThread = new RecordRenderThread(mContext, new ViewHandler(this));
            mRecordThread.setOutputTempleFile(new File(mOutputTempleFile));
        }
    }


    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        Log.d(TAG, "surfaceChanged");
        mMovieThread.sendSurfaceChanged(width, height);

        if (!mInit) {
            mTimer.schedule(mTimerTask, 0, ConfigUtils.getInstance().getFrameInterval());
            mInit = true;
        }
    }

    private void handlerPlayCompleted() {
        if (mIsLooping) {
            mMovieThread.sendRestart();
        }
    }

    public void restart() {
        mMovieThread.sendRestart();
    }

    private void handlerSaveProgress(int progress) {
        if (onSaveProgress != null) {
            onSaveProgress.onProgress(progress);
        }
    }

    private TimerTask mTimerTask = new TimerTask() {
        @Override
        public void run() {
            mMovieThread.sendDoFrame();
            mRecordThread.doFrameControl(System.nanoTime());
        }
    };

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        Log.d(TAG, "surfaceDestroyed");
    }


    public MovieRenderer getMovieRender() {
        return mMovieThread.getMovieRender();
    }

    public void startRecord() {
        mRecordThread.startRecord();
    }

    public void stopRecord() {
        mRecordThread.stopRecord();
    }


    public void setOnSaveProgress(OnSaveProgress onSaveProgress) {
        this.onSaveProgress = onSaveProgress;
    }


    public interface OnSaveProgress {
        void onProgress(int progress);
    }
}
