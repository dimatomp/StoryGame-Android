package ru.ifmo.ctddev.games.messages;

import android.os.Parcel;
import android.os.Parcelable;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * Created by pva701 on 9/20/14.
 */
public class TreeMessage implements Parcelable {
    private int[] id;
    private int[] parent;
    private String[] name;
    private double[] progress;
    private int[] x;
    private int[] y;
    @JsonIgnore
    public static final Creator<TreeMessage> CREATOR = new Creator<TreeMessage>() {
        @Override
        public TreeMessage createFromParcel(Parcel source) {
            TreeMessage result = new TreeMessage();
            int len = source.readInt();
            source.readIntArray(result.id = new int[len]);
            source.readIntArray(result.parent = new int[len]);
            source.readStringArray(result.name = new String[len]);
            source.readDoubleArray(result.progress = new double[len]);
            source.readIntArray(result.x = new int[len]);
            source.readIntArray(result.y = new int[len]);
            return result;
        }

        @Override
        public TreeMessage[] newArray(int size) {
            return new TreeMessage[size];
        }
    };

    public TreeMessage() {
    }

    public int[] getId() {
        return id;
    }

    public void setId(int[] id) {
        this.id = id;
    }

    public int[] getParent() {
        return parent;
    }

    public void setParent(int[] parent) {
        this.parent = parent;
    }

    public String[] getName() {
        return name;
    }

    public void setName(String[] name) {
        this.name = name;
    }

    public double[] getProgress() {
        return progress;
    }

    public void setProgress(double[] progress) {
        this.progress = progress;
    }

    public int[] getX() {
        return x;
    }

    public void setX(int[] x) {
        this.x = x;
    }

    public int[] getY() {
        return y;
    }

    public void setY(int[] y) {
        this.y = y;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id.length);
        dest.writeIntArray(id);
        dest.writeIntArray(parent);
        dest.writeStringArray(name);
        dest.writeDoubleArray(progress);
        dest.writeIntArray(x);
        dest.writeIntArray(y);
    }
}