#pragma version(1)
#pragma rs java_package_name(com.android.renderscript_post_process)

rs_allocation gYUVInput;

     // Convert YUV to RGB, JFIF transform with fixed-point math
    // R = Y + 1.402 * (V - 128)
    // G = Y - 0.34414 * (U - 128) - 0.71414 * (V - 128)
    // B = Y + 1.772 * (U - 128)
uchar4 __attribute__((kernel)) convertRGB(int x ,int y){
    uchar Y = rsGetElementAtYuv_uchar_Y(gYUVInput, x, y);
    uchar U = rsGetElementAtYuv_uchar_U(gYUVInput, x, y);
    uchar V = rsGetElementAtYuv_uchar_V(gYUVInput, x, y);

    int4 curPixel;
    curPixel.r=Y + 1.402 * (V - 128);
    curPixel.g=Y - 0.34414 * (U - 128) - 0.71414 * (V - 128);
    curPixel.b=Y + 1.772 * (U - 128);
    curPixel.a=255;
    uchar4 out = convert_uchar4(clamp(curPixel, 0, 255));
    return out;
}
