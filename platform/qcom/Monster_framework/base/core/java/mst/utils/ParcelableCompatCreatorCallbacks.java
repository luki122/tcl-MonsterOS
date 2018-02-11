package mst.utils;

import android.os.Parcel;
import android.os.Parcelable;


/**
 * Callbacks a {@link Parcelable} creator should implement.
 */
public interface ParcelableCompatCreatorCallbacks<T> {

    /**
     * Create a new instance of the Parcelable class, instantiating it
     * from the given Parcel whose data had previously been written by
     * {@link Parcelable#writeToParcel Parcelable.writeToParcel()} and
     * using the given ClassLoader.
     *
     * @param in The Parcel to read the object's data from.
     * @param loader The ClassLoader that this object is being created in.
     * @return Returns a new instance of the Parcelable class.
     */
    public T createFromParcel(Parcel in, ClassLoader loader);

    /**
     * Create a new array of the Parcelable class.
     *
     * @param size Size of the array.
     * @return Returns an array of the Parcelable class, with every entry
     *         initialized to null.
     */
    public T[] newArray(int size);
}
