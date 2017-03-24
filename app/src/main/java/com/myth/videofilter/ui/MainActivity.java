package com.myth.videofilter.ui;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.graphics.Point;
import android.media.MediaFormat;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.Toast;

import com.myth.videofilter.R;
import com.myth.videofilter.filter.helper.MagicFilterType;
import com.myth.videofilter.utils.ConfigUtils;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {


    private VideoFilterView mVideoFilterView;
    private View mFilterLayout;
    private FilterAdapter mAdapter;

    private RecyclerView mRecyclerView;
    private boolean mIsPlaying = true;
    private ImageView mIVController;

    private SeekBar mSBFilter;
    private SeekBar mSBVideo;
    private View mSavingLayout;
    private View mControllerLayout;
    private ProgressBar mProgressBar;

    private static final String VIDEO_PATH = "/sdcard/test.mp4";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ConfigUtils.getInstance().setVideoPath(VIDEO_PATH);
        MediaFormat format = ConfigUtils.getInstance().getMediaFormat();
        if (format == null) {
            Toast.makeText(this, "视频解析错误", Toast.LENGTH_SHORT).show();
            finish();
        }
        int videoFrameRate = format.getInteger(MediaFormat.KEY_FRAME_RATE);
        int frameInterval = 1000 / videoFrameRate;
        ConfigUtils.getInstance().setFrameInterval(frameInterval);

        mVideoFilterView = (VideoFilterView) findViewById(R.id.video_filter_view);
        Point screenSize = new Point();
        getWindowManager().getDefaultDisplay().getSize(screenSize);
        ViewGroup.LayoutParams params = mVideoFilterView.getLayoutParams();
        params.width = screenSize.x;
        params.height = screenSize.x;


        mVideoFilterView.setLayoutParams(params);
        mVideoFilterView.setOnSaveProgress(new VideoFilterView.OnSaveProgress() {
            @Override
            public void onProgress(int progress) {
                mProgressBar.setProgress(progress);
            }
        });

        mRecyclerView = (RecyclerView) findViewById(R.id.rv_filter);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        mRecyclerView.setLayoutManager(linearLayoutManager);

        mAdapter = new FilterAdapter(this);
        mRecyclerView.setAdapter(mAdapter);
        mAdapter.setOnFilterChangeListener(new FilterAdapter.onFilterChangeListener() {
            @Override
            public void onFilterChanged(MagicFilterType filterType) {
                mVideoFilterView.getMovieRender().setFilter(filterType);
            }

            @Override
            public void onNoChanged(int pos) {
                mSBFilter.setVisibility(mSBFilter.getVisibility() == View.VISIBLE ? View.INVISIBLE : View.VISIBLE);
            }
        });
        mSBFilter = (SeekBar) findViewById(R.id.filter_strength);
        mSBFilter.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                mVideoFilterView.getMovieRender().setFilterStrength(seekBar.getProgress() * 1f / 100);
                mVideoFilterView.setNeedRestart();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                Toast.makeText(MainActivity.this, seekBar.getProgress() + "", Toast.LENGTH_SHORT).show();
            }
        });

        mSavingLayout = findViewById(R.id.layout_saving);
        mControllerLayout = findViewById(R.id.layout_controller);
        mFilterLayout = findViewById(R.id.layout_filter);

        findViewById(R.id.filter).setOnClickListener(this);
        findViewById(R.id.save).setOnClickListener(this);
        findViewById(R.id.cancel).setOnClickListener(this);
        findViewById(R.id.iv_filter_close).setOnClickListener(this);
        mProgressBar = (ProgressBar) findViewById(R.id.progress);

        mIVController = (ImageView) findViewById(R.id.control);
        mIVController.setOnClickListener(this);
        mIVController.setSelected(true);


    }

    @Override
    protected void onResume() {
        super.onResume();
        mVideoFilterView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mVideoFilterView.onPause();
    }

    private void controlPlaying(boolean isPlaying) {
        if (isPlaying) {
            mVideoFilterView.resume();
        } else {
            mVideoFilterView.pause();
        }
        mIsPlaying = isPlaying;
        mIVController.setSelected(mIsPlaying);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.control:
                controlPlaying(!mIsPlaying);
                break;
            case R.id.save:
                if (mIsPlaying) {
                    controlPlaying(false);
                }
                mProgressBar.setProgress(0);
                mVideoFilterView.startRecord();
                mControllerLayout.setVisibility(View.GONE);
                mSavingLayout.setVisibility(View.VISIBLE);
                break;
            case R.id.cancel:
                mVideoFilterView.stopRecord();
                mControllerLayout.setVisibility(View.VISIBLE);
                mSavingLayout.setVisibility(View.GONE);
                break;
            case R.id.filter:
                showFilters();
                break;
            case R.id.iv_filter_close:
                hideFilters();
                mSBFilter.setVisibility(View.INVISIBLE);
                break;
        }
    }

    private void showFilters() {
        ObjectAnimator animator = ObjectAnimator.ofFloat(mFilterLayout, "translationY", mFilterLayout.getHeight(), 0);
        animator.setDuration(200);
        animator.addListener(new Animator.AnimatorListener() {

            @Override
            public void onAnimationStart(Animator animation) {
                mFilterLayout.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {

            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }
        });
        animator.start();
    }

    private void hideFilters() {
        ObjectAnimator animator = ObjectAnimator.ofFloat(mFilterLayout, "translationY", 0, mFilterLayout.getHeight());
        animator.setDuration(200);
        animator.addListener(new Animator.AnimatorListener() {

            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                mFilterLayout.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }
        });
        animator.start();
    }
}
