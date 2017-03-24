package com.myth.videofilter.filter.advance;


import android.content.Context;

import com.myth.videofilter.filter.base.OpenGlUtils;

public class B612AdoreFilter extends B612BaseFilter {


    public B612AdoreFilter(Context context) {
        super(context);
    }

    @Override
    protected int getInputTexture() {
        return OpenGlUtils.loadTexture(mContext, "filter/adore_new.png");
    }


}
