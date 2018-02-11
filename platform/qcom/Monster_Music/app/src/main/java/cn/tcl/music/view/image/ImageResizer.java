package cn.tcl.music.view.image;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;

import java.io.FileDescriptor;

import cn.tcl.music.view.image.*;
import cn.tcl.music.common.cache.ImageCache;

public class ImageResizer extends cn.tcl.music.view.image.ImageWorker {
    private static final String TAG = "ImageResizer";
    protected int mImageWidth;
    protected int mImageHeight;

    /**
     * Initialize providing a single target image size (used for both width and height);
     *
     * @param context
     * @param imageWidth
     * @param imageHeight
     */
    public ImageResizer(Context context, int imageWidth, int imageHeight, int emptyImageResource) {
        super(context, emptyImageResource);
        setImageSize(imageWidth, imageHeight);
    }

    /**
     * Initialize providing a single target image size (used for both width and height);
     *
     * @param context
     * @param imageSize
     */
    public ImageResizer(Context context, int imageSize, int emptyImageResource) {
        super(context, emptyImageResource);
        setImageSize(imageSize);
    }

    /**
     * Set the target image width and height.
     *
     * @param width
     * @param height
     */
    public void setImageSize(int width, int height) {
        mImageWidth = width;
        mImageHeight = height;
    }

    /**
     * Set the target image size (width and height will be the same).
     *
     * @param size
     */
    public void setImageSize(int size) {
        setImageSize(size, size);
    }

    /**
     * The main processing method. This happens in a background task. In this case we are just
     * sampling down the bitmap and returning it from a resource.
     *
     * @param resId
     * @return
     */
    private Bitmap processBitmap(int resId, int imageWidth, int imageHeight) {
        return decodeSampledBitmapFromResource(mResources, resId, imageWidth,
                imageHeight, mCacheController.getImageCache());
    }

    @Override
    protected Bitmap processBitmap(Object data) {
        return processBitmap(Integer.parseInt(String.valueOf(data)), mImageWidth, mImageHeight);
    }

    @Override
    protected Bitmap processBitmap(Object data, int imageWidth, int imageHeight) {
        return processBitmap(Integer.parseInt(String.valueOf(data)), imageWidth, imageHeight);
    }

    @Override
    protected Bitmap processMosaicBitmap(Object[] dataArray, int numberOfTiles) {
        // Not implemented
        return null;
    }

    /**
     * Decode and sample down a bitmap from resources to the requested width and height.
     *
     * @param res The resources object containing the image data
     * @param resId The resource id of the image data
     * @param reqWidth The requested width of the resulting bitmap
     * @param reqHeight The requested height of the resulting bitmap
     * @param cache The ImageCache used to find candidate bitmaps for use with inBitmap
     * @return A bitmap sampled down from the original with the same aspect ratio and dimensions
     *         that are equal to or greater than the requested width and height
     */
    public static Bitmap decodeSampledBitmapFromResource(Resources res, int resId,
                                                         int reqWidth, int reqHeight, ImageCache cache) {

        // BEGIN_INCLUDE (read_bitmap_dimensions)
        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeResource(res, resId, options);

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);
        // END_INCLUDE (read_bitmap_dimensions)
        addInBitmapOptions(options, cache);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeResource(res, resId, options);
    }

    /**
     * Decode and sample down a bitmap from a file to the requested width and height.
     *
     * @param filename The full path of the file to decode
     * @param reqWidth The requested width of the resulting bitmap
     * @param reqHeight The requested height of the resulting bitmap
     * @param cache The ImageCache used to find candidate bitmaps for use with inBitmap
     * @return A bitmap sampled down from the original with the same aspect ratio and dimensions
     *         that are equal to or greater than the requested width and height
     */
    public static Bitmap decodeSampledBitmapFromFile(String filename,
                                                     int reqWidth, int reqHeight, ImageCache cache) {

        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(filename, options);

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);
        addInBitmapOptions(options, cache);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeFile(filename, options);
    }

    /**
     * Decode and sample down a bitmap from a file input stream to the requested width and height.
     *
     * @param fileDescriptor The file descriptor to read from
     * @param reqWidth The requested width of the resulting bitmap
     * @param reqHeight The requested height of the resulting bitmap
     * @param cache The ImageCache used to find candidate bitmaps for use with inBitmap
     * @return A bitmap sampled down from the original with the same aspect ratio and dimensions
     *         that are equal to or greater than the requested width and height
     */
    public static Bitmap decodeSampledBitmapFromDescriptor(
            FileDescriptor fileDescriptor, int reqWidth, int reqHeight, ImageCache cache) {

        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFileDescriptor(fileDescriptor, null, options);

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        addInBitmapOptions(options, cache);

        return BitmapFactory.decodeFileDescriptor(fileDescriptor, null, options);
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private static void addInBitmapOptions(BitmapFactory.Options options, ImageCache cache) {
        //BEGIN_INCLUDE(add_bitmap_options)
        // inBitmap only works with mutable bitmaps so force the decoder to
        // return mutable bitmaps.
        options.inMutable = true;

        if (cache != null) {
            // Try and find a bitmap to use for inBitmap
            Bitmap inBitmap = cache.getBitmapFromReusableSet(options);

            if (inBitmap != null) {
                options.inBitmap = inBitmap;
            }
        }
        //END_INCLUDE(add_bitmap_options)
    }


    public static int calculateInSampleSize(BitmapFactory.Options options,
                                            int reqWidth, int reqHeight)
    {
        // Calculate the largest inSampleSize value that is a power of 2 and keeps both
        // height and width larger than the requested height and width.
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSizeShift = 1;

        if (height > reqHeight || width > reqWidth)
        {
            while (((height >> inSampleSizeShift) > reqHeight)  && ((width >> inSampleSizeShift) > reqWidth))
                inSampleSizeShift++;

        }

        return (1 << (inSampleSizeShift - 1));
    }

}

