package com.myth.videofilter.render;


import android.content.Context;
import android.graphics.SurfaceTexture;
import android.media.MediaPlayer;
import android.text.TextUtils;
import android.util.Log;
import android.view.Surface;

import com.myth.videofilter.filter.advance.B612BaseFilter;
import com.myth.videofilter.filter.VideoFilter;
import com.myth.videofilter.filter.base.GPUImageFilter;
import com.myth.videofilter.filter.helper.FilterTypeHelper;
import com.myth.videofilter.filter.helper.MagicFilterType;
import com.myth.videofilter.utils.ConfigUtils;

import java.util.LinkedList;


public class MovieRenderer implements SurfaceTexture.OnFrameAvailableListener, IMovieRenderer {

    private static final String TAG = MovieRenderer.class.getSimpleName();

    private VideoFilter mVideoFilter;

    private float[] mVideoTextureTransform = new float[16];
    private MediaPlayer mVideoPlayer;
    private SurfaceTexture mVideoTexture;
    private boolean mFrameAvailable = false;

    private volatile boolean mIsPlaying = true;

    private int mVideoPos;
    private boolean mHasSetSize;

    public int getFrame() {
        return mFrame;
    }

    private volatile int mFrame = 0;

    private boolean mIsRecordType;


    private Context mContext;


    private int mWidth, mHeight;

    public void setFilter(GPUImageFilter filter) {
        mFilter = filter;
    }

    private GPUImageFilter mFilter;

    public void setNeedRestart() {
        mNeedRestart = true;
    }

    //need be set true when change images
    private boolean mNeedRestart = false;


    public void setOnProgressListener(OnProgressListener onProgressListener) {
        mOnProgressListener = onProgressListener;
    }

    private OnProgressListener mOnProgressListener;

    private final LinkedList<Runnable> mRunOnDraw;

    public MovieRenderer(Context context) {
        mContext = context;
        mRunOnDraw = new LinkedList<>();
    }

    public void pause() {
        mIsPlaying = false;
        if (mVideoPlayer != null && mVideoPlayer.isPlaying()) {
            mVideoPlayer.pause();
        }
    }

    public void resume() {
        mIsPlaying = true;
        playVideo(false);
    }


    public void restart() {
        if (mNeedRestart) {
            mFrame = 0;
            mIsPlaying = true;
            surfaceCreated();
            surfaceChanged(mWidth, mHeight);
            mNeedRestart = false;
        } else {
            mFrame = 0;
            mIsPlaying = true;
            playVideo(true);
        }
    }

    public boolean isFinish() {
        return mIsFinish;
    }

    private boolean mIsFinish;

    private void playVideo(boolean start) {
        if (TextUtils.isEmpty(ConfigUtils.getInstance().getVideoPath())) {
            return;
        }
        try {
            if (mVideoPlayer == null) {
                mVideoPlayer = new MediaPlayer();
            }
            if (start) {
                mIsFinish = false;
                mVideoPlayer.reset();
                mVideoPlayer.setDataSource(ConfigUtils.getInstance().getVideoPath());
                if (mIsRecordType) {
                    mVideoPlayer.setLooping(false);
                    mVideoPlayer.setVolume(0, 0);
                }
                Surface surface = new Surface(mVideoTexture);
                mVideoPlayer.setSurface(surface);
                surface.release();
                Log.d(TAG, "time: resume" + mVideoPos);
                mVideoPlayer.setOnPreparedListener(new PrepareListener(mVideoPos));
                mVideoPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer mp) {
                        mIsFinish = true;
                    }
                });
                mVideoPlayer.prepare();//缓冲
            }
            if (!mVideoPlayer.isPlaying()) {
                mVideoPlayer.start();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public void seekTo(int frame) {
        mFrame = frame;
    }

    private final class PrepareListener implements MediaPlayer.OnPreparedListener {
        private int position;

        public PrepareListener(int position) {
            this.position = position;
        }

        public void onPrepared(MediaPlayer mp) {
            mVideoPlayer.start();
            if (position > 0) mVideoPlayer.seekTo(position);
        }
    }

    public void setFilter(final MagicFilterType type) {
        mRunOnDraw.add(new Runnable() {
            @Override
            public void run() {
                if (mFilter != null)
                    mFilter.destroy();
                ConfigUtils.getInstance().setMagicFilterType(type);
                mFilter = FilterTypeHelper.getFilter(mContext);
                if (mFilter != null) {
                    mFilter.init();
                    mFilter.onDisplaySizeChanged(mWidth, mHeight);
                }
            }
        });
    }

    public void setFilterStrength(float strength) {
        if (mFilter instanceof B612BaseFilter) {
            ((B612BaseFilter) mFilter).setStrength(strength);
        }
    }


    @Override
    public void surfaceCreated() {
        Log.d(TAG, "surfaceCreated");

        mVideoFilter = new VideoFilter(mContext);
        mVideoFilter.init();
        mVideoFilter.getInitTextureId();
        mVideoTexture = new SurfaceTexture(mVideoFilter.getTextureId());

        mFilter = FilterTypeHelper.getFilter(mContext);
        if (mFilter != null) {
            mFilter.init();
            mFilter.getInitTextureId();
        }
    }

    @Override
    public void surfaceChanged(int width, int height) {
        Log.d(TAG, "surfaceChanged");
        this.mWidth = width;
        this.mHeight = height;
        mVideoFilter.onDisplaySizeChanged(width, height);
        mVideoFilter.initFrameBuffers(width, height);
        mVideoTexture.setOnFrameAvailableListener(this);

        if (mFilter != null) {
            mFilter.onDisplaySizeChanged(width, height);
        }
        if (mIsPlaying) {
            playVideo(true);
        }
    }

    protected void runPendingOnDrawTasks() {
        while (!mRunOnDraw.isEmpty()) {
            mRunOnDraw.removeFirst().run();
        }
    }

    @Override
    public void doFrame() {
        runPendingOnDrawTasks();
        synchronized (this) {
            if (mFrameAvailable) {
                mVideoTexture.updateTexImage();
                if (!mHasSetSize) {
                    mVideoTexture.getTransformMatrix(mVideoTextureTransform);
                    mVideoFilter.setTextureTransformMatrix(mVideoTextureTransform);
                    mHasSetSize = true;
                }

                mFrameAvailable = false;
            }
        }

        mVideoFilter.setSize(mVideoPlayer.getVideoWidth(), mVideoPlayer.getVideoHeight());

        Log.d(TAG, "doFrame:" + System.currentTimeMillis() + ":" + mFrame);

        if (mFilter != null) {
            int id = mVideoFilter.onDrawToTexture();
            mFilter.onDrawFrame(id);
        } else {
            mVideoFilter.onDrawFrame();
        }

        if (mOnProgressListener != null) {
            mOnProgressListener.onProgress(1.0f * mVideoPlayer.getCurrentPosition() / mVideoPlayer.getDuration());
        }
    }


    @Override
    public void surfaceDestroy() {
        if (mVideoPlayer != null) {
            mVideoPlayer.release();
            mVideoPlayer = null;
        }
        if (mVideoFilter != null) {
            mVideoFilter.destroy();
            mVideoTexture.release();
        }
    }

    @Override
    public void onFrameAvailable(SurfaceTexture surfaceTexture) {
        synchronized (this) {
            Log.d(TAG, "onFrameAvailable");
            mFrameAvailable = true;
        }
    }


    public void setIsRecordType(boolean mIsRecordType) {
        this.mIsRecordType = mIsRecordType;
    }

    public interface OnProgressListener {
        void onProgress(float progress);
    }
}
