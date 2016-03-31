package com.dgreenhalgh.android.largemargebackground

import android.app.Activity
import android.content.res.AssetManager
import android.media.AudioManager
import android.media.SoundPool
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import java.io.IOException


class MainActivity : Activity() {

    lateinit private var rootImageView: ImageView

    private val TAG = "MainActivity"
    private val SOUNDS_FOLDER = "short" // TODO: Add attributions

    lateinit var assetManager: AssetManager
    lateinit var soundPool: SoundPool

    var sounds: MutableList<Sound> = arrayListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main);

        rootImageView = findViewById(R.id.main_image) as ImageView
        rootImageView.setOnClickListener({ playSoundsSerially()})

        assetManager = assets
        soundPool = SoundPool(10, AudioManager.STREAM_MUSIC, 0)
        loadShortSounds()
    }

    private fun loadShortSounds() {
        var soundNames = emptyArray<String>()
        try {
            Log.i(TAG, "Found " + soundNames.size + " sounds")
            soundNames = assetManager.list(SOUNDS_FOLDER)
        } catch (ioe: IOException) {
            Log.e(TAG, "Could not list assets", ioe)
            return;
        }

        for (filename in soundNames) {
            try {
                var assetPath = SOUNDS_FOLDER + "/" + filename
                var sound = Sound(assetPath)
                load(sound)
                sounds.add(sound)
            } catch (ioe: IOException) {
                Log.e(TAG, "Could not load sound " + filename, ioe)
            }
        }
    }

    private fun load(sound: Sound) {
        var assetFileDescriptor = assetManager.openFd(sound.assetPath)
        sound.id = soundPool.load(assetFileDescriptor, 1)
    }

    private fun play(sound: Sound) {
        var soundId = sound.id;
        if (soundId == -1) {
            return;
        }

        soundPool.play(soundId, 1.0f, 1.0f, 1, 0, 1.0f)
    }

    private fun playSoundsSerially() {
        for (sound in sounds) {
            play(sound)
            Thread.sleep(1000)
        }
    }
}