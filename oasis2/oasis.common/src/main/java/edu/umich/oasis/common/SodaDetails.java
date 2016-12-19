package edu.umich.oasis.common;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import org.apache.commons.lang3.ObjectUtils;

import java.util.Collections;
import java.util.List;

/**
 * Created by jpaupore on 2/8/16.
 */
public class SodaDetails implements Parcelable {
    private static final String TAG = "OASIS.SodaDetails";
    private static final boolean localLOGV = Log.isLoggable(TAG, Log.VERBOSE);
    private static final boolean localLOGD = Log.isLoggable(TAG, Log.DEBUG);

    public SodaDescriptor descriptor;
    public String resultType;
    public List<ParamInfo> paramInfo;
    public TaintSet requiredTaints;
    public TaintSet optionalTaints;

    public SodaDetails() {

    }

    public SodaDetails(Parcel in) {
        readFromParcel(in);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public void copyFrom(SodaDetails other) {
        this.descriptor = other.descriptor;
        this.resultType = other.resultType;
        this.paramInfo = other.paramInfo;
        this.requiredTaints = other.requiredTaints;
        this.optionalTaints = other.optionalTaints;
    }

    public void readFromParcel(Parcel source) {
        final int length = source.readInt();
        final int endPos = source.dataPosition() + length;

        if (localLOGV) {
            Log.v(TAG, "Unparceling, length "+length);
        }

        if (source.dataPosition() < endPos) {
            descriptor = SodaDescriptor.readFromParcel(source);
            if (localLOGV) {
                Log.v(TAG, "Descriptor: "+descriptor);
            }
        }
        if (source.dataPosition() < endPos) {
            resultType = source.readString();
            if (localLOGV) {
                Log.v(TAG, "Result type: "+resultType);
            }
        }
        if (source.dataPosition() < endPos) {
            paramInfo = source.createTypedArrayList(ParamInfo.CREATOR);
            if (paramInfo != null) {
                if (localLOGV) {
                    Log.v(TAG, "Param info (size " + paramInfo.size() + "):");
                    for (ParamInfo pi : paramInfo) {
                        Log.v(TAG, "    "+pi);
                    }
                }
                paramInfo = Collections.unmodifiableList(paramInfo);
            } else if (localLOGV) {
                Log.v(TAG, "Param info (null)");
            }
        }
        if (source.dataPosition() < endPos) {
            requiredTaints = TaintSet.readFromParcel(source);
            if (localLOGV) {
                Log.v(TAG, "Required taints: "+requiredTaints);
            }
        }
        if (source.dataPosition() < endPos) {
            optionalTaints = TaintSet.readFromParcel(source);
            if (localLOGV) {
                Log.v(TAG, "Optional taints: "+optionalTaints);
            }
        }
        if (source.dataPosition() < endPos) {
            if (localLOGD) {
                Log.d(TAG, "Excess data at end of parcel");
            }
            source.setDataPosition(endPos);
        }
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        // This space will be filled in with the final size of the object, minus this header.
        final int headerPos = dest.dataPosition();
        dest.writeInt(0);
        final int startPos = dest.dataPosition();
        // Special handling for completely null objects - leave length at zero.
        if (ObjectUtils.firstNonNull(descriptor, resultType, paramInfo,
                requiredTaints, optionalTaints) == null) {
            if (localLOGV) {
                Log.v(TAG, "Null case, writing size 0");
            }
            return;
        }
        SodaDescriptor.writeToParcel(descriptor, dest, flags);
        dest.writeString(resultType);
        dest.writeTypedList(paramInfo);
        TaintSet.writeToParcel(requiredTaints, dest, flags);
        TaintSet.writeToParcel(optionalTaints, dest, flags);

        final int endPos = dest.dataPosition();
        dest.setDataPosition(headerPos);
        dest.writeInt(endPos - startPos);
        dest.setDataPosition(endPos);
        if (localLOGV) {
            Log.v(TAG, "Total size: " + (endPos - startPos));
        }
    }

    public static final Creator<SodaDetails> CREATOR = new Creator<SodaDetails>() {
        @Override
        public SodaDetails createFromParcel(Parcel in) {
            SodaDetails sd = new SodaDetails();
            sd.readFromParcel(in);
            return sd;
        }

        @Override
        public SodaDetails[] newArray(int size) {
            return new SodaDetails[size];
        }
    };
}
