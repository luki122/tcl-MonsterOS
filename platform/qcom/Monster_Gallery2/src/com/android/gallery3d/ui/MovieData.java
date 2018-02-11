package com.android.gallery3d.ui;

import android.graphics.Movie;

public class MovieData{
    public String mPath = null;
    public Movie mMovie = null;
    public float mScale = 0.0f;

    public float mDrawLeft = 0;
    public float mDrawTop = 0;
    public int mIndex = -1;
    
    public MovieData reset() {
        this.mPath = null;
        this.mMovie = null;
        this.mScale = 0.0f;
        this.mDrawLeft = 0;
        this.mDrawTop = 0;
        this.mIndex = -1;
        return this;
    }
    
}
