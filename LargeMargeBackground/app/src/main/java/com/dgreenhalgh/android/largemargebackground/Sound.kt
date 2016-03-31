package com.dgreenhalgh.android.largemargebackground

class Sound {

    var assetPath = ""
    var name = ""
    var id: Int

    constructor(assetPath: String) {
        this.assetPath = assetPath

        var components = assetPath.split("/")
        var filename = components[components.size - 1]
        name = filename.replace(".wav", "")
        id = -1
    }


}