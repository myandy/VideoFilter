package com.myth.videofilter.ui;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.myth.videofilter.R;
import com.myth.videofilter.filter.helper.FilterTypeHelper;
import com.myth.videofilter.filter.helper.MagicFilterType;
import com.myth.videofilter.utils.ConfigUtils;


public class FilterAdapter extends RecyclerView.Adapter<FilterAdapter.FilterHolder> {
    private MagicFilterType[] mFilters;
    private Context mContext;
    private int mSelected = 0;

    private final MagicFilterType[] types = new MagicFilterType[]{
            MagicFilterType.NONE,
            MagicFilterType.ADORE,
            MagicFilterType.HEART,
            MagicFilterType.PERFUME,
            MagicFilterType.RESPONSIBLE,
    };

    public FilterAdapter(Context context) {
        this.mFilters = types;
        this.mContext = context;
        for (int i = 0; i < types.length; i++) {
            if (types[i] == ConfigUtils.getInstance().getMagicFilterType()) {
                mSelected = i;
            }
        }
    }

    @Override
    public FilterHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.filter_item_layout,
                parent, false);
        FilterHolder viewHolder = new FilterHolder(view);
        viewHolder.mThumbImage = (ImageView) view
                .findViewById(R.id.filter_thumb_image);
        viewHolder.mFilterName = (TextView) view
                .findViewById(R.id.filter_thumb_name);
        viewHolder.mFilterRoot = (FrameLayout) view
                .findViewById(R.id.filter_root);
        viewHolder.mThumbSelected = (FrameLayout) view
                .findViewById(R.id.filter_thumb_selected);
        viewHolder.mThumbSelectedBg = view.
                findViewById(R.id.filter_thumb_selected_bg);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(FilterHolder holder, final int position) {
        holder.onBindView();
    }

    @Override
    public int getItemCount() {
        return mFilters == null ? 0 : mFilters.length;
    }


    class FilterHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        ImageView mThumbImage;
        TextView mFilterName;
        FrameLayout mThumbSelected;
        FrameLayout mFilterRoot;
        View mThumbSelectedBg;

        public FilterHolder(View itemView) {
            super(itemView);
        }

        void onBindView() {
            int position = getAdapterPosition();
            mThumbImage.setImageResource(FilterTypeHelper.FilterType2Thumb(mFilters[position]));
            mFilterName.setText(FilterTypeHelper.FilterType2Name(mFilters[position]));
            mFilterName.setBackgroundColor(mContext.getResources().getColor(
                    FilterTypeHelper.FilterType2Color(mFilters[position])));
            if (position == mSelected) {
                mThumbSelected.setVisibility(View.VISIBLE);
                mThumbSelectedBg.setBackgroundColor(mContext.getResources().getColor(
                        FilterTypeHelper.FilterType2Color(mFilters[position])));
                mThumbSelectedBg.setAlpha(0.7f);
            } else {
                mThumbSelected.setVisibility(View.GONE);
            }

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            int position = getAdapterPosition();
            if (mSelected == position) {
                if (mOnFilterChangeListener != null) {
                    mOnFilterChangeListener.onNoChanged(position);
                }
                return;
            }
            int lastSelected = mSelected;
            mSelected = position;
            notifyItemChanged(lastSelected);
            notifyItemChanged(position);
            if (mOnFilterChangeListener != null) {
                mOnFilterChangeListener.onFilterChanged(mFilters[position]);
            }
        }
    }

    public interface onFilterChangeListener {
        void onFilterChanged(MagicFilterType filterType);

        void onNoChanged(int pos);
    }

    private onFilterChangeListener mOnFilterChangeListener;

    public void setOnFilterChangeListener(onFilterChangeListener onFilterChangeListener) {
        this.mOnFilterChangeListener = onFilterChangeListener;
    }
}
