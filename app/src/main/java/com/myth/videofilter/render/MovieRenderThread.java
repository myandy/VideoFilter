package com.myth.videofilter.render;


import android.content.Context;
import android.util.Log;
import android.view.SurfaceHolder;

import com.myth.videofilter.ui.VideoFilterView;

public class MovieRenderThread extends RenderThread {

    protected static final String TAG = MovieRenderThread.class.getSimpleName();

    private VideoFilterView.ViewHandler mViewHandler;
    private boolean mNeedRefresh;

    public boolean isPlaying() {
        return isPlaying;
    }

    public void setPlaying(boolean playing) {
        isPlaying = playing;
    }

    private volatile boolean isPlaying = true;

    //whether paused by user
    private boolean isPaused = false;

    private MovieRenderer mMovieRenderer;

    private Context mContext;

    private boolean mFinish;

    public MovieRenderThread(Context context, VideoFilterView.ViewHandler viewHandler, SurfaceHolder surfaceHolder) {
        super(surfaceHolder);
        mContext = context;
        mViewHandler = viewHandler;
    }

    @Override
    public void start() {
        setName("MovieThread GL render");
        super.start();
        waitUntilReady();
    }

    public void manualPause() {
        isPaused = true;
        if (isPlaying) {
            isPlaying = false;
            mMovieRenderer.pause();
        }
    }

    public void manualResume() {
        isPaused = false;
        if (!isPlaying) {
            isPlaying = true;
            mMovieRenderer.resume();
        }
    }


    public void onPause() {
        if (isPlaying) {
            isPlaying = false;
            mMovieRenderer.pause();
        }
    }

    public void onResume() {
        if (!isPlaying && !isPaused && !mMovieRenderer.isFinish()) {
            isPlaying = true;
            mMovieRenderer.resume();
        }
    }

    @Override
    public void surfaceCreated() {
        super.surfaceCreated();
        Log.d(TAG, "surfaceCreated");
        if (mMovieRenderer == null) {
            mMovieRenderer = new MovieRenderer(mContext);
            mMovieRenderer.surfaceCreated();
        }
    }


    @Override
    public void surfaceChanged(int width, int height) {
        Log.d(TAG, "surfaceChanged");
        mMovieRenderer.surfaceChanged(width, height);
        mNeedRefresh = true;
    }

    @Override
    public void restart() {
        Log.d(TAG, "restart");
        isPlaying = true;
        mFinish = false;
        mMovieRenderer.restart();
    }

    @Override
    public void doFrame() {
        if (!mFinish) {
            if (mMovieRenderer.isFinish()) {
                Log.d(TAG, "restart:sendPlayCompleted");
                mMovieRenderer.pause();
                mViewHandler.sendPlayCompleted();
                mFinish = true;
                return;
            }
            if (!isPlaying) {
                if (mNeedRefresh) {
                    mMovieRenderer.doFrame();
                    mWindowSurface.swapBuffers();
                    mNeedRefresh = false;
                }
            } else {
                mMovieRenderer.doFrame();
                mWindowSurface.swapBuffers();
                mNeedRefresh = false;
            }
        }
    }

    @Override
    public void surfaceDestroy() {
        mMovieRenderer.surfaceDestroy();
        mMovieRenderer = null;
    }


    public int getFrame() {
        return mMovieRenderer.getFrame();
    }

    public void setNeedRestart() {
        mMovieRenderer.setNeedRestart();
    }

    public MovieRenderer getMovieRender() {
        return mMovieRenderer;
    }
}
