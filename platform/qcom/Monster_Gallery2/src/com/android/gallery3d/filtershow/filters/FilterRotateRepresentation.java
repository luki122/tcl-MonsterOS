/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.gallery3d.filtershow.filters;

import android.util.JsonReader;
import android.util.JsonWriter;
import android.util.Log;

import com.android.gallery3d.R;
import com.android.gallery3d.filtershow.editors.EditorRotate;
import com.android.gallery3d.filtershow.editors.ImageOnlyEditor;

import java.io.IOException;

public class FilterRotateRepresentation extends FilterRepresentation {
    public static final String SERIALIZATION_NAME = "ROTATION";
    public static final String SERIALIZATION_ROTATE_VALUE = "value";
    // TCL ShenQianfeng Begin on 2016.08.25
    public static final String SERIALIZATION_IS_CCW_VALUE = "is_ccw";
    // TCL ShenQianfeng End on 2016.08.25
    
    private static final String TAG = FilterRotateRepresentation.class.getSimpleName();

    private Rotation mRotation; //original not static, ShenQianfeng modify this to static on 2016.08.25
    
    // TCL ShenQianfeng Begin on 2016.08.25
    private boolean mIsCounterClockwise;
    // TCL ShenQianfeng End on 2016.08.25

    public enum Rotation {
        //ZERO(0), NINETY(90), ONE_EIGHTY(180), TWO_SEVENTY(270);
        ZERO(0), NINETY(90), NEGATIVE_NINETY(-90);
        
        private final int mValue;

        private Rotation(int value) {
            mValue = value;
        }

        public int value() {
            return mValue;
        }

        public static Rotation fromValue(int value) {
            switch (value) {
                case 0:
                    return ZERO;
                case 90:
                    return NINETY;
                    // TCL ShenQianfeng Begin on 2016.08.25
                case -90:
                    return NEGATIVE_NINETY;
                    // TCL ShenQianfeng End on 2016.08.25
                    /*
                case 180:
                    return ONE_EIGHTY;
                case 270:
                    return TWO_SEVENTY;
                    */
                default:
                    return null;
            }
        }
    }

    public FilterRotateRepresentation(Rotation rotation) {
        super(SERIALIZATION_NAME);
        setSerializationName(SERIALIZATION_NAME);
        setShowParameterValue(false);
        setFilterClass(FilterRotateRepresentation.class);
        setFilterType(FilterRepresentation.TYPE_GEOMETRY);
        setSupportsPartialRendering(true);
        setTextId(R.string.rotate);
        setEditorId(ImageOnlyEditor.ID);
        setRotation(rotation);
    }

    public FilterRotateRepresentation(FilterRotateRepresentation r) {
        this(r.getRotation());
        setName(r.getName());
        // TCL ShenQianfeng Begin on 2016.08.25
        setIsCounterClockwise(r.isCounterClockwise());
        // TCL ShenQianfeng End on 2016.08.25
    }
    
    // TCL ShenQianfeng Begin on 2016.08.25

    public FilterRotateRepresentation(boolean ccw) {
        this(getNil());
        mIsCounterClockwise = ccw;
    }

    // TCL ShenQianfeng End on 2016.08.25
    
    public void setIsCounterClockwise(boolean isCCW) {
        mIsCounterClockwise = isCCW;
    }

    public FilterRotateRepresentation() {
        this(getNil());
    }

    public Rotation getRotation() {
        // TCL ShenQianfeng Begin on 2016.08.25
        // Original:
        // return mRotation;
        // Modify To:
        if(mIsCounterClockwise) {
            return Rotation.NEGATIVE_NINETY;
        } 
        return Rotation.NINETY;
        // TCL ShenQianfeng End on 2016.08.25
        
    }
    
    // TCL ShenQianfeng Begin on 2016.08.25
    
    public boolean isCounterClockwise() {
        return mIsCounterClockwise;
    }
    
    public void rotate() {
        if(mIsCounterClockwise) {
            rotateCCW();
        } else {
            rotateCW();
        }
    }
    
    /*
    public void rotateTo() {
        switch(mRotation) {
        case ZERO:
            mRotation = Rotation.ZERO;
            break;
        case NINETY:
            mRotation = Rotation.NINETY;
            break;
        case ONE_EIGHTY:
            mRotation = Rotation.ONE_EIGHTY;
            break;
        case TWO_SEVENTY:
            mRotation = Rotation.TWO_SEVENTY;
            break;
        }
    }
    */
    
    public void rotateCCW() {
        mRotation = Rotation.NEGATIVE_NINETY;
        /*
        switch(mRotation) {
        case ZERO:
            mRotation = Rotation.TWO_SEVENTY; //NINETY
            break;
        case NINETY:
            mRotation = Rotation.ZERO; //ONE_EIGHTY
            break;
        
        case ONE_EIGHTY:
            mRotation = Rotation.NINETY; //TWO_SEVENTY
            break;
        case TWO_SEVENTY:
            mRotation = Rotation.ONE_EIGHTY; //ONE_EIGHTY;
            break;
        }
         */
    }
    // TCL ShenQianfeng End on 2016.08.25

    public void rotateCW() {
        mRotation = Rotation.NINETY;
        /*
        switch(mRotation) {
            case ZERO:
                mRotation = Rotation.NINETY;
                break;
            case NINETY:
                mRotation = Rotation.ONE_EIGHTY;
                break;
            case ONE_EIGHTY:
                mRotation = Rotation.TWO_SEVENTY;
                break;
            case TWO_SEVENTY:
                mRotation = Rotation.ZERO;
                break;
        }
        */
    }

    public void set(FilterRotateRepresentation r) {
        mRotation = r.mRotation;
        // TCL ShenQianfeng Begin on 2016.08.25
        mIsCounterClockwise = r.mIsCounterClockwise;
        // TCL ShenQianfeng End on 2016.08.25
    }

    public void setRotation(Rotation rotation) {
        if (rotation == null) {
            throw new IllegalArgumentException("Argument to setRotation is null");
        }
        mRotation = rotation;
    }

    @Override
    public boolean allowsSingleInstanceOnly() {
        // TCL ShenQianfeng Begin on 2016.08.25
        // Original:
        // return true;
        // Modify To:
        return false;
        // TCL ShenQianfeng End on 2016.08.25
    }

    @Override
    public FilterRepresentation copy() {
        return new FilterRotateRepresentation(this);
    }

    @Override
    protected void copyAllParameters(FilterRepresentation representation) {
        if (!(representation instanceof FilterRotateRepresentation)) {
            throw new IllegalArgumentException("calling copyAllParameters with incompatible types!");
        }
        super.copyAllParameters(representation);
        representation.useParametersFrom(this);
    }

    @Override
    public void useParametersFrom(FilterRepresentation a) {
        if (!(a instanceof FilterRotateRepresentation)) {
            throw new IllegalArgumentException("calling useParametersFrom with incompatible types!");
        }
        setRotation(((FilterRotateRepresentation) a).getRotation());
        // TCL ShenQianfeng Begin on 2016.08.25
        setIsCounterClockwise(((FilterRotateRepresentation) a).isCounterClockwise());
        // TCL ShenQianfeng End on 2016.08.25
        
    }

    @Override
    public boolean isNil() {
        return mRotation == getNil();
    }

    public static Rotation getNil() {
        return Rotation.ZERO;
    }

    @Override
    public void serializeRepresentation(JsonWriter writer) throws IOException {
        writer.beginObject();
        writer.name(SERIALIZATION_ROTATE_VALUE).value(mRotation.value());
        // TCL ShenQianfeng Begin on 2016.08.25
        writer.name(SERIALIZATION_IS_CCW_VALUE).value(mIsCounterClockwise ? 1 : 0);
        // TCL ShenQianfeng End on 2016.08.25
        writer.endObject();
    }

    @Override
    public boolean equals(FilterRepresentation rep) {
        if (!(rep instanceof FilterRotateRepresentation)) {
            return false;
        }
        FilterRotateRepresentation rotate = (FilterRotateRepresentation) rep;
        if (rotate.mRotation.value() != mRotation.value()) {
            return false;
        }
        // TCL ShenQianfeng Begin on 2016.08.25
        if(mRotation != rotate.getRotation()) {
            return false;
        }
        // TCL ShenQianfeng End on 2016.08.25
        return true;
    }

    @Override
    public void deSerializeRepresentation(JsonReader reader) throws IOException {
        boolean unset = true;
        reader.beginObject();
        while (reader.hasNext()) {
            String name = reader.nextName();
            if (SERIALIZATION_ROTATE_VALUE.equals(name)) {
                Rotation r = Rotation.fromValue(reader.nextInt());
                if (r != null) {
                    setRotation(r);
                    unset = false;
                }
            } 
            // TCL ShenQianfeng Begin on 2016.08.25
            else if(SERIALIZATION_IS_CCW_VALUE.equals(name)) {
                mIsCounterClockwise = 1 == reader.nextInt();
            }
            // TCL ShenQianfeng End on 2016.08.25
            else {
                reader.skipValue();
            }
        }
        if (unset) {
            Log.w(TAG, "WARNING: bad value when deserializing " + SERIALIZATION_NAME);
        }
        reader.endObject();
    }
}
