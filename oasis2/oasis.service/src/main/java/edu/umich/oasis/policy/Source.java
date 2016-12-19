package edu.umich.oasis.policy;

import android.content.ComponentName;
import android.content.res.Resources;
import android.content.res.XmlResourceParser;
import android.util.Log;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;

import edu.umich.oasis.helpers.Utils;

/**
 * Created by jpaupore on 1/14/16.
 */
public class Source {
    private static final String TAG = "OASIS.Policy";
    private static final boolean localLOGV = Log.isLoggable(TAG, Log.VERBOSE);
    private static final boolean localLOGD = Log.isLoggable(TAG, Log.DEBUG);

    private final ComponentName sourceName;
    private final String sourceLabel;
    private final Policy policy;

    public Source(String currentPackage, XmlResourceParser parser, Resources resources)
            throws XmlPullParserException, IOException {
        parser.require(XmlPullParser.START_TAG, "", "source");
        int depth = parser.getDepth();

        String sourceTag = parser.getAttributeValue(Utils.OASIS_NAMESPACE, "name");
        if (sourceTag == null) {
            throw new PolicyParseException("Missing oasis:name attribute on source");
        }
        sourceName = new ComponentName(currentPackage, sourceTag);

        String label = parser.getAttributeValue(Utils.OASIS_NAMESPACE, "label");
        if (label == null) {
            label = sourceName.flattenToString();
        }
        sourceLabel = label;

        Policy policy = null;
        while (parser.nextTag() != XmlPullParser.END_TAG) {
            switch (parser.getName()) {
                case "policy":
                    if (policy != null) {
                        throw new PolicyParseException("Multiple policies for one source");
                    }
                    policy = new Policy(this, parser, resources);
                    break;
                default:
                    Log.w(TAG, String.format("Unknown <source> element <%s>", parser.getName()));
                    Utils.skip(parser);
                    break;
            }
        }

        this.policy = (policy != null) ? policy : new Policy(this);

        Utils.skip(parser, depth);
        parser.require(XmlPullParser.END_TAG, "", "source");
    }

    public ComponentName getSourceName() {
        return sourceName;
    }

    public String getSourceLabel() {
        return sourceLabel;
    }

    public Policy getPolicy() {
        return policy;
    }

    @Override
    public String toString() {
        return String.format("%s (%s)", sourceLabel, sourceName.flattenToString());
    }

    @Override
    public boolean equals(Object obj) {
        return (obj != null) && (obj instanceof Source)
                && sourceName.equals(((Source)obj).sourceName);
    }

    @Override
    public int hashCode() {
        return sourceName.hashCode();
    }
}
