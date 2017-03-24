package com.myth.videofilter.filter.helper;


import android.content.Context;

import com.myth.videofilter.R;
import com.myth.videofilter.filter.advance.B612AdoreFilter;
import com.myth.videofilter.filter.advance.B612HeartFilter;
import com.myth.videofilter.filter.advance.B612PerfumeFilter;
import com.myth.videofilter.filter.advance.B612ResponsibleFilter;
import com.myth.videofilter.filter.base.GPUImageFilter;
import com.myth.videofilter.utils.ConfigUtils;

public class FilterTypeHelper {

    public static GPUImageFilter getFilter(Context context) {
        MagicFilterType filterType = ConfigUtils.getInstance().getMagicFilterType();
        return getFilter(filterType, context);
    }

    private static GPUImageFilter getFilter(MagicFilterType filterType, Context context) {
        switch (filterType) {
            case NONE:
                return null;
            case ADORE:
                return new B612AdoreFilter(context);
            case HEART:
                return new B612HeartFilter(context);
            case PERFUME:
                return new B612PerfumeFilter(context);
            case RESPONSIBLE:
                return new B612ResponsibleFilter(context);
            default:
                return null;
        }
    }


    public static int FilterType2Name(MagicFilterType filterType) {
        switch (filterType) {
            case NONE:
                return R.string.filter_none;
            case ADORE:
                return R.string.filter_adore;
            case HEART:
                return R.string.filter_heart;
            case PERFUME:
                return R.string.filter_perfume;
            case RESPONSIBLE:
                return R.string.filter_responsible;
            default:
                return R.string.filter_none;
        }
    }

    public static int FilterType2Color(MagicFilterType filterType) {
        switch (filterType) {
            case NONE:
                return R.color.filter_category_greenish_dummy;

            default:
                return R.color.filter_category_greenish_normal;
        }
    }

    public static int FilterType2Thumb(MagicFilterType filterType) {
        switch (filterType) {
            case NONE:
                return R.drawable.filter_thumb_original;
            default:
                return R.drawable.filter_thumb_original;
        }
    }
}
