package com.myth.videofilter.render;


public interface IMovieRenderer {
    void surfaceCreated();
    void surfaceChanged(int width, int height);
    void doFrame();
    void surfaceDestroy();
}
