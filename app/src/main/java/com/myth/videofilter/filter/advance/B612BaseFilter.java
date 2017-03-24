package com.myth.videofilter.filter.advance;

import android.content.Context;
import android.opengl.GLES20;

import com.myth.videofilter.filter.base.GPUImageFilter;

public abstract class B612BaseFilter extends GPUImageFilter {

    private int[] inputTextureHandles = {-1};
    private int[] inputTextureUniformLocations = {-1};
    private int mGLStrengthLocation;

    public B612BaseFilter(Context context) {
        super(context, NO_FILTER_VERTEX_SHADER, FRAGMENT_SHADER);
    }

    public void setStrength(float strength) {
        this.strength = strength;
    }

    private float strength = 1.0f;

    private static String FRAGMENT_SHADER = "varying highp vec2 textureCoordinate;\n \n uniform sampler2D inputImageTexture;\n uniform sampler2D inputImageTexture2; // lookup texture\n uniform float strength; \n void main()\n {\n     lowp vec4 textureColor = texture2D(inputImageTexture, textureCoordinate);\n     \n     mediump float blueColor = textureColor.b * 63.0;\n     \n     mediump vec2 quad1;\n     quad1.y = floor(blueColor/8.0); \n     quad1.x = floor(blueColor) - (quad1.y * 8.0);\n     \n     mediump vec2 quad2;\n     quad2.y = floor(ceil(blueColor)/7.999); \n     quad2.x = ceil(blueColor) - (quad2.y * 8.0);\n     \n     highp vec2 texPos1;\n     texPos1.x = (quad1.x * 0.125) + 0.5/512.0 + ((0.125 - 1.0/512.0) * textureColor.r);\n     texPos1.y = (quad1.y * 0.125) + 0.5/512.0 + ((0.125 - 1.0/512.0) * textureColor.g);\n     \n     highp vec2 texPos2;\n     texPos2.x = (quad2.x * 0.125) + 0.5/512.0 + ((0.125 - 1.0/512.0) * textureColor.r);\n     texPos2.y = (quad2.y * 0.125) + 0.5/512.0 + ((0.125 - 1.0/512.0) * textureColor.g);\n     \n     lowp vec4 newColor1 = texture2D(inputImageTexture2, texPos1);\n     lowp vec4 newColor2 = texture2D(inputImageTexture2, texPos2);\n     \n     lowp vec4 newColor = mix(newColor1, newColor2, fract(blueColor));\n   vec4 newColor3;\n" +
            "newColor3=mix(textureColor,newColor,strength);\n   gl_FragColor = vec4(newColor3.rgb, textureColor.w);\n  }";


    public void onDestroy() {
        super.onDestroy();
        GLES20.glDeleteTextures(1, inputTextureHandles, 0);
        this.inputTextureHandles[0] = -1;
    }

    protected void onDrawArraysAfter() {
        if (this.inputTextureHandles[0] != -1) {
            GLES20.glActiveTexture(GLES20.GL_TEXTURE3);
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
            GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        }
    }

    protected void onDrawArraysPre() {
        if (this.inputTextureHandles[0] != -1) {
            GLES20.glActiveTexture(GLES20.GL_TEXTURE3);
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, inputTextureHandles[0]);
            GLES20.glUniform1i(inputTextureUniformLocations[0], 3);
        }
        GLES20.glUniform1f(mGLStrengthLocation, strength);
    }

    protected void onInit() {
        super.onInit();
        for (int i = 0; i < inputTextureUniformLocations.length; i++) {
            inputTextureUniformLocations[i] = GLES20.glGetUniformLocation(getProgram(), "inputImageTexture" + (2 + i));
        }
        mGLStrengthLocation = GLES20.glGetUniformLocation(mGLProgId,
                "strength");
    }

    protected abstract int getInputTexture();

    protected void onInitialized() {
        super.onInitialized();
        runOnDraw(new Runnable() {
            public void run() {
                inputTextureHandles[0] = getInputTexture();
            }
        });

    }

}
