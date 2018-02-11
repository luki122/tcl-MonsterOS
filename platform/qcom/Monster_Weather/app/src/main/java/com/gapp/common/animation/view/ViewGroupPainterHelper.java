package com.gapp.common.animation.view;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

import com.gapp.common.animation.IPainterView;
import com.gapp.common.animation.IServantConnecter;
import com.gapp.common.animation.ISprite;
import com.gapp.common.utils.BitmapManager;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Copyright (C) 2016 Tcl Corporation Limited
 * <p>
 * Created on 16-8-22.
 * $desc
 */
public class ViewGroupPainterHelper implements IPainterView {

    private final static String TAG = "ViewGroupPainterHelper";
    protected final static long RUNNING_TIME = 0;

    protected final static int STATE_NULL = 0;
    protected final static int STATE_START = 1;
    protected final static int STATE_RUNNING = 2;
    protected final static int STATE_READY = 4;

    protected ArrayList<ISprite> mSprites = new ArrayList<>(100);
    protected BitmapManager mBitmapManager;
    protected ArrayList<IServantConnecter> mSpriteConnecters = new ArrayList<>(10);
    protected ArrayList<IServantConnecter> mBackUpConnecters;
    protected ArrayList<ISprite> mBackUpSprites;
    protected ArrayList<IServantConnecter.IServant> mBackUpServants;
    protected OnRunningListener mOnRunningListener;

    private int mState = STATE_NULL;

    protected Lock mReadLock, mWirteLock;

    private Context mContext;

    private ViewGroup mViewGroup;

    public ViewGroupPainterHelper(ViewGroup vg) {
        mViewGroup = vg;
        mContext = vg.getContext();
    }


    @Override
    public void setZOrderOnTop(boolean isOnTop) {

    }

    public BitmapManager getBitmapManager() {
        return mBitmapManager;
    }

    @Override
    public int getHeight() {
        return mViewGroup.getWidth();
    }

    @Override
    public int getWidth() {
        return mViewGroup.getHeight();
    }


    /**
     * add the state to current state
     *
     * @param state
     */
    protected void addState(int state) {
        if (!hasState(state)) {
            mState |= state;
            runStateRunnable();
        }
    }


    private void runStateRunnable() {
        mStateRunnnable.run();
    }

    /**
     * remove the state from current state
     *
     * @param state
     */
    protected void removeState(int state) {
        if (hasState(state)) {
            mState ^= state;
            runStateRunnable();
        }
    }

    public boolean hasState(int state) {
        return (mState & state) == state;
    }

    protected void setState(int state) {
        mState = state;
    }

    public void addSprite(ISprite sprite) {
        mWirteLock.lock();
        if (!mSprites.contains(sprite)) {
            sprite.setUp(this);
            mSprites.add(sprite);
            mViewGroup.addView((View) sprite, indexOfSprite(sprite));
            if (sprite instanceof IServantConnecter.IServant) {
                for (IServantConnecter connecter : mSpriteConnecters) {
                    connecter.onServantStateChanged((IServantConnecter.IServant) sprite, IServantConnecter.SERVANT_STATE_ADD);
                }
            }
            mBackUpSprites = null;// clear
            mBackUpServants = null;
        }
        mWirteLock.unlock();
    }

    private int getZorder(int index) {
        View child = mViewGroup.getChildAt(index);
        if (child instanceof ISprite) {
            return ((ISprite) child).getZOrder();
        }
        return 0;
    }


    private int indexOfSprite(ISprite sprite) {
        final int order = sprite.getZOrder();
        int current;
        for (int i = mViewGroup.getChildCount() - 1; i >= 0; i--) {
            if (getZorder(i) < order) {
                return childIndex(i + 1);
            }
        }
        return childIndex(0);
    }

    private int childIndex(int index) {
        return index < mViewGroup.getChildCount() ? index : -1;
    }

    public void removeSprite(final ISprite sprite) {
        mWirteLock.lock();
        if (mSprites.remove(sprite)) {
            mViewGroup.removeView((View) sprite);
            sprite.tearDown();
            if (sprite instanceof IServantConnecter.IServant) {
                for (IServantConnecter connecter : mSpriteConnecters) {
                    connecter.onServantStateChanged((IServantConnecter.IServant) sprite, IServantConnecter.SERVANT_STATE_REMOVE);
                }
            }
            mBackUpSprites = null;
            mBackUpServants = null;
        }
        mWirteLock.unlock();
    }

    @Override
    public void clearSprites() {
        for (ISprite sprite : listAllSprites()) {
            removeSprite(sprite);
        }
    }

    public List<ISprite> listAllSprites() {
        List<ISprite> spriteList;
        mReadLock.lock();
        if (null == mBackUpSprites) {
            mBackUpSprites = (ArrayList<ISprite>) mSprites.clone();
        }
        spriteList = mBackUpSprites;
        mReadLock.unlock();
        return spriteList;
    }


    public List<IServantConnecter.IServant> listAllServants() {
        List<IServantConnecter.IServant> servants;
        List<ISprite> sprites = listAllSprites();
        mReadLock.lock();
        if (null == mBackUpServants) {
            mBackUpServants = new ArrayList<>(sprites.size());
            for (ISprite sprite : sprites) {
                if (sprite instanceof IServantConnecter.IServant) {
                    mBackUpServants.add((IServantConnecter.IServant) sprite);
                }
            }
        }
        servants = mBackUpServants;
        mReadLock.unlock();
        return servants;
    }


    @Override
    public void init() {
        ReentrantReadWriteLock lock = new ReentrantReadWriteLock(false);
        mReadLock = lock.readLock();
        mWirteLock = lock.writeLock();
        mBitmapManager = new BitmapManager(mContext);
        mBitmapManager.init();
    }

    public void start() {
        addState(STATE_START);
    }

    public void stop() {
        removeState(STATE_START);
    }

    @Override
    public void addServantConnnecter(final IServantConnecter connecter) {
        mWirteLock.lock();
        if (!mSpriteConnecters.contains(connecter)) {
            connecter.init();
            mSpriteConnecters.add(connecter);
            connecter.connectServants(listAllServants());
            if (connecter instanceof View) {
                mViewGroup.addView((View) connecter);
            }
        }
        mWirteLock.unlock();
    }

    @Override
    public void removeServantConnecter(final IServantConnecter connecter) {
        mWirteLock.lock();
        if (connecter instanceof View) {
            mViewGroup.removeView((View) connecter);
        }
        mSpriteConnecters.remove(connecter);
        connecter.recycle();
        mWirteLock.unlock();
    }

    @Override
    public void clearServantConnecters() {
        for (IServantConnecter connecter : listAllSpriteConnecters()) {
            removeServantConnecter(connecter);
        }
    }

    @Override
    public void getLocationOnScreen(int[] position) {

    }

    public List<IServantConnecter> listAllSpriteConnecters() {
        List<IServantConnecter> spriteConnecters;
        mReadLock.lock();
        if (null == mBackUpConnecters) {
            mBackUpConnecters = (ArrayList<IServantConnecter>) mSpriteConnecters.clone();
        }
        spriteConnecters = mBackUpConnecters;
        mReadLock.unlock();
        return spriteConnecters;
    }

    @Override
    public void setOnRunningListener(OnRunningListener l) {
        mOnRunningListener = l;
    }

    @Override
    public void recycle() {
        stop();
        for (IServantConnecter connecter : mSpriteConnecters) {
            connecter.recycle();
        }
        mSpriteConnecters.clear();
        mBitmapManager.recycle();
    }

    @Override
    public void onTrimMemory(int level) {
        for (IServantConnecter connecter : mSpriteConnecters) {
            connecter.onTrimMemory(level);
        }
    }

    private void caculateSprites(List<ISprite> sprites) {
        for (ISprite sprite : sprites) {
            sprite.running();
        }
        for (IServantConnecter connecter : listAllSpriteConnecters()) {
            connecter.running();
        }
    }

//    private Path mDirtyRects = new Path();
//    private RectF mSpritesRect = new RectF();
//    protected final void drawSprites(Canvas canvas, List<ISprite> sprites) {
//        canvas.save();
//        if (!mDirtyRects.isEmpty()) {
//            canvas.clipPath(mDirtyRects);
//        }
//        canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.ADD);
//        mDirtyRects.reset();
//        canvas.clipRect(mLocation);
//        for (ISprite sprite : mSprites) {
//            sprite.draw(canvas);
//            sprite.getLocation(mSpritesRect);
//            mDirtyRects.addRect(mSpritesRect, Path.Direction.CCW);
//        }
//        canvas.restore();
//    }


    private Runnable mCaculateRunnable = new Runnable() {


        @Override
        public void run() {
            List<ISprite> sprites = listAllSprites();
            long timeMills;
            timeMills = System.currentTimeMillis() + RUNNING_TIME;
            if (null != mOnRunningListener) {
                mOnRunningListener.onBeforeRunning();
                caculateSprites(sprites);
                mOnRunningListener.onAfterRunning();
            } else {
                caculateSprites(sprites);
            }

            timeMills = timeMills - System.currentTimeMillis();
            if (timeMills < 0) {
                timeMills = 0;
            }
            mViewGroup.postDelayed(this, timeMills);
        }
    };

    private void resumeSprites() {
        for (ISprite sprite : listAllSprites()) {
            sprite.resume();
        }
    }

    private void pauseSprites() {
        for (ISprite sprite : listAllSprites()) {
            sprite.pause();
        }
    }

    private void pauseConnectors() {
        for (IServantConnecter connecter : listAllSpriteConnecters())
            connecter.pause();
    }

    private void resumeConnectors() {
        for (IServantConnecter connecter : listAllSpriteConnecters())
            connecter.resume();
    }

    private Runnable mStateRunnnable = new Runnable() {
        @Override
        public void run() {
            if (hasState(STATE_START) && hasState(STATE_READY)) {// if state is ready and resume
                if (!hasState(STATE_RUNNING)) {// if state is not real running
                    addState(STATE_RUNNING);
                    onDrawingThreadStart();
                    resumeSprites();
                    resumeConnectors();
                    mViewGroup.removeCallbacks(mCaculateRunnable);
                    mViewGroup.post(mCaculateRunnable);
                }
            } else if (hasState(STATE_RUNNING)) {// if state is not ready or resume
                mViewGroup.removeCallbacks(mCaculateRunnable);
                pauseConnectors();
                pauseSprites();
                onDrawingThreadStoped();
                removeState(STATE_RUNNING);
            }
        }
    };

    protected void onDrawingThreadStart() {

    }

    protected void onDrawingThreadStoped() {

    }


    @Override
    public void runOnThread(Runnable runnable) {
        mViewGroup.post(runnable);
    }
}
