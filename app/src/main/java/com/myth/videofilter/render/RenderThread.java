package com.myth.videofilter.render;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;

import com.myth.videofilter.encoder.gles.EglCore;
import com.myth.videofilter.encoder.gles.WindowSurface;

import java.lang.ref.WeakReference;


public abstract class RenderThread extends Thread implements IMovieRenderer {


    protected static final String TAG = RenderThread.class.getSimpleName();

    private volatile RenderHandler mHandler;

    // Used to wait for the thread to resume.
    private Object mStartLock = new Object();
    private boolean mReady = false;


    private EglCore mEglCore;

    public void setSurfaceHolder(SurfaceHolder surfaceHolder) {
        mSurfaceHolder = surfaceHolder;
    }

    public SurfaceHolder getSurfaceHolder() {
        return mSurfaceHolder;
    }

    private volatile SurfaceHolder mSurfaceHolder;

    protected WindowSurface mWindowSurface;

    public RenderThread(SurfaceHolder surfaceHolder) {
        mSurfaceHolder = surfaceHolder;
    }

    /**
     * Thread entry point.
     * <p>
     * The thread should not be started until the Surface associated with the SurfaceHolder
     * has been created.  That way we don't have to wait for a separate "surface created"
     * message to arrive.
     */
    @Override
    public void run() {
        Looper.prepare();
        mHandler = new RenderHandler(this);
        mEglCore = new EglCore(null, EglCore.FLAG_RECORDABLE);
        synchronized (mStartLock) {
            mReady = true;
            mStartLock.notify();    // signal waitUntilReady()
        }

        Looper.loop();

        Log.d(TAG, "looper sendQuit");
        releaseGl();
        mEglCore.makeNothingCurrent();
        mEglCore.release();
        synchronized (mStartLock) {
            mReady = false;
        }
    }

    /**
     * Waits until the render thread is ready to receive messages.
     * <p>
     * Call from the UI thread.
     */
    public void waitUntilReady() {
        synchronized (mStartLock) {
            while (!mReady) {
                try {
                    mStartLock.wait();
                } catch (InterruptedException ie) { /* not expected */ }
            }
        }
    }

    /**
     * Returns the render thread's Handler.  This may be called from any thread.
     */
    public RenderHandler getHandler() {
        return mHandler;
    }

    /**
     * Prepares the surface.
     */
    public void surfaceCreated() {
        Surface surface = mSurfaceHolder.getSurface();
        prepareGl(surface);
    }


    protected void releaseGl() {

    }


    protected void prepareGl(Surface surface) {
        mWindowSurface = new WindowSurface(mEglCore, surface, false);
        mWindowSurface.makeCurrent();
    }


    /**
     * Shuts everything down.
     */
    public void quit() {
        Log.d(TAG, "sendQuit");
        Looper.myLooper().quit();
    }


    /**
     * Sends the "surface created" message.
     * <p>
     * Call from UI thread.
     */
    public void sendSurfaceCreated() {
        mHandler.sendMessage(mHandler.obtainMessage(RenderHandler.MSG_SURFACE_CREATED));
    }

    /**
     * Sends the "surface changed" message, forwarding what we got from the SurfaceHolder.
     * <p>
     * Call from UI thread.
     */
    public void sendSurfaceChanged(int width, int height) {
        // ignore format
        mHandler.sendMessage(mHandler.obtainMessage(RenderHandler.MSG_SURFACE_CHANGED, width, height));
    }

    /**
     * Sends the "do frame" message, forwarding the Choreographer event.
     * <p>
     * Call from UI thread.
     */
    public void sendDoFrame() {
        mHandler.sendMessage(mHandler.obtainMessage(RenderHandler.MSG_DO_FRAME));
    }

    /**
     * Call from render thread.
     */
    protected void restart() {

    }

    public void sendRestart() {
        mHandler.sendMessage(mHandler.obtainMessage(RenderHandler.MSG_RESTART));
    }

    /**
     * Sends the "sendQuit" message, which tells the render thread to halt.
     * <p>
     * Call from UI thread.
     */
    public void sendQuit() {
        mHandler.sendMessage(mHandler.obtainMessage(RenderHandler.MSG_QUIT));
    }

    /**
     * Handler for RenderThread.  Used for messages sent from the UI thread to the render thread.
     * <p>
     * The object is created on the render thread, and the various "send" methods are called
     * from the UI thread.
     */
    public static class RenderHandler extends Handler {
        private static final int MSG_SURFACE_CREATED = 0;
        private static final int MSG_SURFACE_CHANGED = 1;
        private static final int MSG_DO_FRAME = 2;
        private static final int MSG_RESTART = 3;
        private static final int MSG_QUIT = 4;

        // This shouldn't need to be a weak ref, since we'll go away when the Looper quits,
        // but no real harm in it.
        private WeakReference<RenderThread> mWeakRenderThread;

        /**
         * Call from render thread.
         */
        public RenderHandler(RenderThread rt) {
            mWeakRenderThread = new WeakReference<RenderThread>(rt);
        }

        @Override  // runs on RenderThread
        public void handleMessage(Message msg) {
            int what = msg.what;
            //Log.d(TAG, "RenderHandler [" + this + "]: what=" + what);

            RenderThread renderThread = mWeakRenderThread.get();
            if (renderThread == null) {
                Log.w(TAG, "RenderHandler.handleMessage: weak ref is null");
                return;
            }
            switch (what) {
                case MSG_SURFACE_CREATED:
                    renderThread.surfaceCreated();
                    break;
                case MSG_SURFACE_CHANGED:
                    renderThread.surfaceChanged(msg.arg1, msg.arg2);
                    break;
                case MSG_DO_FRAME:
                    renderThread.doFrame();
                    break;
                case MSG_RESTART:
                    renderThread.restart();
                    break;
                case MSG_QUIT:
                    renderThread.quit();
                    break;
                default:
                    throw new RuntimeException("unknown message " + what);
            }
        }
    }

}
