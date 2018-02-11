package com.gapp.common.utils;

/**
 * User : user
 * Date : 2016-08-11
 * Time : 17:12
 */
public class RandomUtils {

    private static final java.util.Random RANDOM = new java.util.Random(System.currentTimeMillis());

    private boolean[] mLastInt;

    private final static int[] TYPES = new int[]{0, 1};

    private int mType = 0;

    private int mCounts = 0;
    private float mOffsetDelta;


    public static float getRandomFloat(float temp) {
        return RANDOM.nextFloat() * temp;
    }

    public static float getRandomFloat(float lower, float upper) {
        return getRandomFloat(upper - lower) + lower;
    }

    public static int getRandomInt(int lower, int upper) {
        return getRandomInt(upper - lower) + lower;
    }

    public static int getRandomInt(int temp) {
        return RANDOM.nextInt(temp);
    }

    public static boolean getRandomBoolean() {
        return RANDOM.nextBoolean();
    }


    public RandomUtils() {
        this(50);
    }

    public RandomUtils(int size) {
        mLastInt = new boolean[size];
    }

    public int getRandomIntOffset(int lower, int upper) {
        final int size = mLastInt.length;
        final int delta = upper - lower;

        mType++;
        mType &= 0x1;

        final int halfSize = size / 2;
        int random = getRandomInt(halfSize);// get random index
        if (mType == 1)
            random += halfSize;// get the real index, when random index in right
        if (mLastInt[random]) {// caculate the index when this index is used
            int start = 0;

            if (mType == 1) {
                if ((mCounts + 1) / 2 < halfSize) {
                    start = halfSize;
                }
            }

            for (int i = start; i < size; i++) {// get
                if (!mLastInt[i]) {
                    random = i;
                    break;
                }
            }
        }

        mLastInt[random] = true;
        mCounts++;
        int distance = (int) (delta * random / size + mOffsetDelta);

        if (mCounts == mLastInt.length) {
            mOffsetDelta = getRandomFloat(0, delta / size);
            for (int i = 0; i < mCounts; i++) {
                mLastInt[i] = false;
            }
            mCounts = 0;
        }
        return distance;
    }

}
