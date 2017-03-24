package com.myth.videofilter.filter.advance;


import android.content.Context;

import com.myth.videofilter.filter.base.OpenGlUtils;

public class B612PerfumeFilter extends B612BaseFilter {


    public B612PerfumeFilter(Context context) {
        super(context);
    }

    @Override
    protected int getInputTexture() {
        return OpenGlUtils.loadTexture(mContext, "filter/perfume_new.png");
    }
}
