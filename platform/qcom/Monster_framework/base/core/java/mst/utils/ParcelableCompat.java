package mst.utils;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Helper for accessing features in {@link android.os.Parcelable}
 * introduced after API level 4 in a backwards compatible fashion.
 */
public class ParcelableCompat {

    /**
     * Factory method for {@link Parcelable.Creator}.
     *
     * @param callbacks Creator callbacks implementation.
     * @return New creator.
     */
    public static <T> Parcelable.Creator<T> newCreator(
            ParcelableCompatCreatorCallbacks<T> callbacks) {
        return new CompatCreator<T>(callbacks);
    }

    static class CompatCreator<T> implements Parcelable.Creator<T> {
        final ParcelableCompatCreatorCallbacks<T> mCallbacks;

        public CompatCreator(ParcelableCompatCreatorCallbacks<T> callbacks) {
            mCallbacks = callbacks;
        }

        @Override
        public T createFromParcel(Parcel source) {
            return mCallbacks.createFromParcel(source, null);
        }

        @Override
        public T[] newArray(int size) {
            return mCallbacks.newArray(size);
        }
    }
}
