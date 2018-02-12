package com.myth.videofilter.filter;

import android.content.Context;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;

import com.myth.videofilter.R;
import com.myth.videofilter.filter.base.GPUImageFilter;
import com.myth.videofilter.filter.base.OpenGlUtils;
import com.myth.videofilter.filter.base.Rotation;
import com.myth.videofilter.filter.base.TextureRotationUtil;

import java.nio.FloatBuffer;


public class VideoFilter extends GPUImageFilter {

    private float[] mTextureTransformMatrix;
    private int mTextureTransformMatrixLocation;
    private boolean mHasSetSize;

    public VideoFilter(Context context) {
        super(context, OpenGlUtils.readShaderFromRawResource(context, R.raw.photomovie_default_vertex), OpenGlUtils.readShaderFromRawResource(context, R.raw.photomovie_fragment_sharder));
//        mGLTextureBuffer.put(TextureRotationUtil.getRotation(Rotation.NORMAL, false, true)).position(0);
    }

    public void setSize(int width, int height) {
        if (mHasSetSize) {
            return;
        }

        TextureRotationUtil.adjustSize(TextureRotationUtil.ScaleType.CENTER_INSIDE, width, height, mOutputWidth, mOutputHeight, Rotation.NORMAL.asInt(), false, true, mGLCubeBuffer, mGLTextureBuffer);
        mHasSetSize = true;
    }


    protected void onInit() {
        super.onInit();
        mTextureTransformMatrixLocation = GLES20.glGetUniformLocation(mGLProgId, "textureTransform");
    }

    @Override
    public void setTextureTransformMatrix(float[] mtx) {
        mTextureTransformMatrix = mtx;
    }

    @Override
    protected void onDrawArraysPre() {
        if (mTextureTransformMatrix != null) {
            GLES20.glUniformMatrix4fv(mTextureTransformMatrixLocation, 1, false, mTextureTransformMatrix, 0);
        }
    }

    @Override
    public int getInitTextureId() {
        mTextureId = OpenGlUtils.getExternalOESTextureID();
        return mTextureId;
    }

    @Override
    public int onDrawFrame(final int textureId, final FloatBuffer cubeBuffer,
                           final FloatBuffer textureBuffer) {
        GLES20.glUseProgram(mGLProgId);
        runPendingOnDrawTasks();
        if (!mIsInitialized) {
            return OpenGlUtils.NOT_INIT;
        }
        cubeBuffer.position(0);
        GLES20.glEnableVertexAttribArray(mGLAttribPosition);
        GLES20.glVertexAttribPointer(mGLAttribPosition, 2, GLES20.GL_FLOAT, false, 0, cubeBuffer);
        textureBuffer.position(0);
        GLES20.glVertexAttribPointer(mGLAttribTextureCoordinate, 2, GLES20.GL_FLOAT, false, 0,
                textureBuffer);
        GLES20.glEnableVertexAttribArray(mGLAttribTextureCoordinate);

        if (textureId != OpenGlUtils.NO_TEXTURE) {
            GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
            GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, textureId);
            GLES20.glUniform1i(mGLUniformTexture, 0);
        }
        onDrawArraysPre();
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);
        GLES20.glDisableVertexAttribArray(mGLAttribPosition);
        GLES20.glDisableVertexAttribArray(mGLAttribTextureCoordinate);
        onDrawArraysAfter();
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, 0);
        return OpenGlUtils.ON_DRAWN;
    }

}
