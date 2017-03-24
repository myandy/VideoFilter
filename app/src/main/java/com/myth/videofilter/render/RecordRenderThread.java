package com.myth.videofilter.render;


import android.content.Context;
import android.media.MediaFormat;
import android.opengl.EGL14;
import android.util.Log;

import com.myth.videofilter.encoder.TextureMovieEncoder;
import com.myth.videofilter.ui.VideoFilterView;
import com.myth.videofilter.utils.ConfigUtils;

import java.io.File;

public class RecordRenderThread extends TextureMovieEncoder {

    private static final String TAG = RecordRenderThread.class.getSimpleName();

    private VideoFilterView.ViewHandler mViewHandler;

    private volatile boolean isPlaying = false;

    private MovieRenderer mMovieRenderer;

    //this frame is faster than MovieRenderer's frame
    private int mFrame = 0;

    private boolean mRecordingEnabled = false;

    private int recordingStatus;

    private static final int RECORDING_OFF = 0;
    private static final int RECORDING_ON = 1;
    private static final int RECORDING_RESUMED = 2;

    private Context mContext;

    public void setOutputTempleFile(File outputTempleFile) {
        mOutputTempleFile = outputTempleFile;
    }

    private File mOutputTempleFile;

    private volatile boolean mIsCancelRecord;

    public RecordRenderThread(Context context, VideoFilterView.ViewHandler viewHandler) {
        mContext = context;
        mViewHandler = viewHandler;
    }

    public void onPause() {
        if (isPlaying && mRecordingEnabled) {
            isPlaying = false;
            mMovieRenderer.pause();
        }
    }

    public void onResume() {
        if (!isPlaying && mRecordingEnabled) {
            isPlaying = true;
            mMovieRenderer.resume();
        }
    }

    public void startRecord() {
        mFrame = 0;
        isPlaying = true;
        mRecordingEnabled = true;
    }

    public void stopRecord() {
        mIsCancelRecord = true;
        mRecordingEnabled = false;
        // doFrameControl has time delay ,do a manual stop
        doFrameControl(System.nanoTime());
    }


    /**
     * it is called after startRecording,MovieRenderer need to be create every time ,diff from MovieRenderThread,don't change it
     */
    @Override
    public void surfaceCreated() {
        Log.d(TAG, "surfaceCreated");
        mMovieRenderer = new MovieRenderer(mContext);
        mMovieRenderer.setIsRecordType(true);
        mMovieRenderer.surfaceCreated();
        mMovieRenderer.setOnProgressListener(new MovieRenderer.OnProgressListener() {
            @Override
            public void onProgress(float progress) {
                if (!mIsCancelRecord) {
                    mViewHandler.sendSaveProgress((int) (progress * 100));
                }
            }
        });

        // a new recorder start,last cancel is false
        mIsCancelRecord = false;
    }

    /**
     * it is called after surfaceCreated
     */
    @Override
    public void surfaceChanged(int width, int height) {
        Log.d(TAG, "surfaceChanged");
        mMovieRenderer.surfaceChanged(width, height);
    }


    @Override
    public void doFrame() {
        mMovieRenderer.doFrame();
    }

    @Override
    public void onCompleted() {
        if (!mIsCancelRecord) {
            mViewHandler.sendSaveCompleted();
        }
    }

    @Override
    public void surfaceDestroy() {
        mMovieRenderer.surfaceDestroy();
    }

    public void doFrameControl(long timeStampNanos) {
        if (!isPlaying) {
            return;
        }
        if (mRecordingEnabled && mMovieRenderer != null && mMovieRenderer.isFinish()) {
            mRecordingEnabled = false;
            Log.d(TAG, "doFrameControl stop:" + mFrame);
        }
        if (mRecordingEnabled) {
            switch (recordingStatus) {
                case RECORDING_OFF:

                    int width = ConfigUtils.getInstance().getMediaFormat().getInteger(MediaFormat.KEY_WIDTH);
                    int height = ConfigUtils.getInstance().getMediaFormat().getInteger(MediaFormat.KEY_HEIGHT);
                    startRecording(new TextureMovieEncoder.EncoderConfig(
                            mOutputTempleFile, width, height, 4000000, EGL14.eglGetCurrentContext()));
                    recordingStatus = RECORDING_ON;
                    break;
                case RECORDING_RESUMED:
                    updateSharedContext(EGL14.eglGetCurrentContext());
                    recordingStatus = RECORDING_ON;
                    break;
                case RECORDING_ON:
                    break;
                default:
                    throw new RuntimeException("unknown status " + recordingStatus);
            }
        } else {
            switch (recordingStatus) {
                case RECORDING_ON:
                case RECORDING_RESUMED:
                    Log.d(TAG, "StopRecording");
                    stopRecording(timeStampNanos, mIsCancelRecord);
                    recordingStatus = RECORDING_OFF;
                    break;
                case RECORDING_OFF:
                    break;
                default:
                    throw new RuntimeException("unknown status " + recordingStatus);
            }
        }
        if (mRecordingEnabled && !mIsCancelRecord) {
            Log.d(TAG, "doFrameControl:" + mFrame);
            frameAvailable(0, timeStampNanos, mFrame);
            mFrame++;
        }
    }
}
