package com.myth.videofilter.filter.advance;


import android.content.Context;

import com.myth.videofilter.filter.base.OpenGlUtils;

public class B612HeartFilter extends B612BaseFilter {


    public B612HeartFilter(Context context) {
        super(context);
    }

    @Override
    protected int getInputTexture() {
        return OpenGlUtils.loadTexture(mContext, "filter/heart_new.png");
    }


}
