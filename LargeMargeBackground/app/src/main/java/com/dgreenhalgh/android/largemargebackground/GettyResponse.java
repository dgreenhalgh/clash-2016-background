package com.dgreenhalgh.android.largemargebackground;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class GettyResponse {

    @SerializedName("images")
    List<GettyImage> imageList;

    public GettyImage getFirstImage() {
        return imageList.get(0);
    }
}
