/*
 * Copyright 2014 Google Inc. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.myth.videofilter.encoder.gles;

import android.opengl.GLES20;
import android.opengl.Matrix;
import android.util.Log;


public class GlUtil {


    private static final String TAG = GlUtil.class.getSimpleName();

    private GlUtil() {
    }


    /**
     * Checks to see if a GLES error has been raised.
     */
    public static void checkGlError(String op) {
        int error = GLES20.glGetError();
        if (error != GLES20.GL_NO_ERROR) {
            String msg = op + ": glError 0x" + Integer.toHexString(error);
            Log.e(TAG, msg);
            throw new RuntimeException(msg);
        }
    }


    public static void getMatrix1(float[] matrix, float scale) {
        float[] projection = new float[16];
        float[] camera = new float[16];
        Matrix.orthoM(projection, 0, -1, 1, -1, 1, 1, 3);
        Matrix.setLookAtM(camera, 0, 0, 0, 1, 0, 0, 0, 0, 1, 0);

        float[] mMatrixCurrent =
                {1, 0, 0, 0,
                        0, 1, 0, 0,
                        0, 0, 1, 0,
                        0, 0, 0, 1};
        Matrix.translateM(mMatrixCurrent, 0, (1 / scale - 1) / 2, (1 / scale - 1) / 2, 0);
        Matrix.scaleM(mMatrixCurrent, 0, scale, scale, 1);

        Matrix.multiplyMM(matrix, 0, camera, 0, mMatrixCurrent, 0);
        Matrix.multiplyMM(matrix, 0, projection, 0, matrix, 0);
    }


    public static void getMatrix(float[] matrix, float scale) {
        float[] projection = new float[16];
        float[] camera = new float[16];
        Matrix.orthoM(projection, 0, -1, 1, -1, 1, 1, 3);
        Matrix.setLookAtM(camera, 0, 0, 0, 1, 0, 0, 0, 0, 1, 0);

        float[] mMatrixCurrent =
                {1, 0, 0, 0,
                        0, 1, 0, 0,
                        0, 0, 1, 0,
                        0, 0, 0, 1};
        Matrix.translateM(mMatrixCurrent, 0, (scale - 1) / 2, (scale - 1) / 2, 0);
        Matrix.scaleM(mMatrixCurrent, 0, 1 / scale, 1 / scale, 1);

        Matrix.multiplyMM(matrix, 0, camera, 0, mMatrixCurrent, 0);
        Matrix.multiplyMM(matrix, 0, projection, 0, matrix, 0);
    }
}
