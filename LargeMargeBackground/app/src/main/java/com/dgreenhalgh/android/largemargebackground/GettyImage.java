package com.dgreenhalgh.android.largemargebackground;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class GettyImage {

    @SerializedName("display_sizes")
    List<GettyDisplaySize> displaySizeList;

    public GettyDisplaySize getLargestDisplaySize() {
        return displaySizeList.get(0);
    }
}
