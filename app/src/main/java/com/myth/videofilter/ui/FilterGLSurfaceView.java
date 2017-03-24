package com.myth.videofilter.ui;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;

import com.myth.videofilter.filter.base.GPUImageFilter;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * Created by AndyMao on 17-3-22.
 */

public class FilterGLSurfaceView extends GLSurfaceView implements GLSurfaceView.Renderer {

    private SurfaceTexture surfaceTexture;

    private GPUImageFilter mFilter;

    private int surfaceWidth, surfaceHeight;

    public FilterGLSurfaceView(Context context) {
        super(context);
    }

    public FilterGLSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setEGLConfigChooser(false);
        setEGLContextClientVersion(2);
        setRenderer(this);
        setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        GLES20.glDisable(GL10.GL_DITHER);
        GLES20.glClearColor(0, 0, 0, 0);
        GLES20.glEnable(GL10.GL_CULL_FACE);
        GLES20.glEnable(GL10.GL_DEPTH_TEST);
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        GLES20.glViewport(0, 0, width, height);
        surfaceWidth = width;
        surfaceHeight = height;
        onFilterChanged();
    }

    @Override
    public void onDrawFrame(GL10 gl) {

    }


    public void setFilter(final GPUImageFilter filter) {
        queueEvent(new Runnable() {
            @Override
            public void run() {
                if (mFilter != null)
                    mFilter.destroy();
                mFilter = filter;
                if (mFilter != null)
                    mFilter.init();
                onFilterChanged();
            }
        });
        requestRender();
    }

    private void onFilterChanged() {
        if (mFilter != null) {
            mFilter.onDisplaySizeChanged(surfaceWidth, surfaceHeight);
        }
    }

}
