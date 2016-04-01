package com.dgreenhalgh.android.largemargebackground

import android.app.Activity
import android.content.res.AssetManager
import android.media.AudioManager
import android.media.SoundPool
import android.os.Bundle
import android.util.Log
import org.phoenixframework.channels.Socket
import java.io.IOException


class MainActivity : Activity() {

    private val TAG = "MainActivity"
    private val SOUNDS_FOLDER = "short" // TODO: Add attributions

    lateinit var assetManager: AssetManager
    lateinit var soundPool: SoundPool

    var sounds: MutableList<Sound> = arrayListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main);

        var socket = connectToSocket()
        joinChannel(socket)

        assetManager = assets
        soundPool = SoundPool(10, AudioManager.STREAM_MUSIC, 0)
        loadShortSounds()
    }

    private fun connectToSocket(): Socket {
        var socket = Socket("ws://large-marge-server.herokuapp.com/socket/websocket?vsn=1.0.0")
        socket.connect()

        return socket
    }

    private fun joinChannel(socket: Socket) {
        var channel = socket.chan("largemarge:events", null)

        channel.join()
                .receive("ignore", { System.out.println("IGNORE") })
                .receive("ok", { System.out.println("JOINED with " + it.toString()) })

        channel.on("start", {
            playSoundsSerially()
            System.out.println("NEW MESSAGE: " + it.toString())
        })

        channel.onClose { System.out.println("CLOSED: " + it.toString()) }

        channel.onError { System.out.println("ERROR: " + it) }
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