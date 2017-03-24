package com.myth.videofilter.filter.advance;

import android.content.Context;

import com.myth.videofilter.filter.base.OpenGlUtils;

public class B612ResponsibleFilter extends B612BaseFilter {


    public B612ResponsibleFilter(Context context) {
        super(context);
    }

    @Override
    protected int getInputTexture() {
        return OpenGlUtils.loadTexture(mContext, "filter/responsible_new.png");
    }

}
