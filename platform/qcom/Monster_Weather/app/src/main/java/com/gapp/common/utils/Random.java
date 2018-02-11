package com.gapp.common.utils;

/**
 * User : user
 * Date : 2016-08-11
 * Time : 17:12
 */
public class Random {

    private static final java.util.Random RANDOM = new java.util.Random(System.currentTimeMillis());

    private static boolean[] sLastInt = new boolean[50];

    static {
        for (int i = sLastInt.length - 1; i >= 0; i--)
            sLastInt[i] = false;
    }

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

    public static int getRandomIntOffset(int lower, int upper, int offset) {
        final int size = sLastInt.length;
        final int delta = upper - lower;

        int random = getRandomInt(size);
        int index = 0;
        while (sLastInt[random]) {
            random++;
            if (random >= sLastInt.length)
                random -= sLastInt.length;
            index++;
            if (index == sLastInt.length) {
                for (int i = size - 1; i >= 0; i--)
                    sLastInt[i] = false;
            }
        }
        sLastInt[random] = true;
        int distance = delta * random / size;
        return distance;
    }

}
