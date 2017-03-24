package com.myth.videofilter.utils;

import android.media.MediaFormat;

import com.myth.videofilter.filter.helper.MagicFilterType;

/**
 * Created by AndyMao on 17-3-21.
 */
public class ConfigUtils {
    private static ConfigUtils mInstance;

    public static ConfigUtils getInstance() {
        if (mInstance == null) {
            synchronized (ConfigUtils.class) {
                if (mInstance == null) {
                    mInstance = new ConfigUtils();
                }
            }
        }
        return mInstance;
    }

    private ConfigUtils() {
    }

    private String mVideoPath;


    private MediaFormat mMediaFormat;

    public MediaFormat getMediaFormat() {
        return mMediaFormat;
    }

    public MagicFilterType getMagicFilterType() {
        return mMagicFilterType;
    }

    public void setMagicFilterType(MagicFilterType magicFilterType) {
        mMagicFilterType = magicFilterType;
    }

    private MagicFilterType mMagicFilterType = MagicFilterType.NONE;

    public int getFrameInterval() {
        return mFrameInterval;
    }

    public void setFrameInterval(int frameInterval) {
        mFrameInterval = frameInterval;
    }

    private int mFrameInterval;

    public String getVideoPath() {
        return mVideoPath;
    }

    public void setVideoPath(String videoPath) {
        mVideoPath = videoPath;
        mMediaFormat = VideoInfoUtils.getVideoInfo(mVideoPath);
    }


}