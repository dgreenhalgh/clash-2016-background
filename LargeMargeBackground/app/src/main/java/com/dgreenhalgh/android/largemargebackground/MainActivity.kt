package com.dgreenhalgh.android.largemargebackground

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.app.Activity
import android.content.Context
import android.content.res.AssetManager
import android.media.AudioManager
import android.media.SoundPool
import android.os.Bundle
import android.util.Log
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.squareup.picasso.Picasso
import okhttp3.OkHttpClient
import org.phoenixframework.channels.Socket
import retrofit2.Retrofit
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import java.io.IOException
import okhttp3.logging.HttpLoggingInterceptor
import rx.Observable
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper

class MainActivity : Activity() {

    private val TAG = "MainActivity"
    private val SOUNDS_FOLDER = "short"

    private lateinit var backgroundView: LinearLayout
    private lateinit var backgroundImageView: ImageView
    private lateinit var signpostTextView: TextView
    private lateinit var curtainLeftImageView: ImageView
    private lateinit var curtainRightImageView: ImageView

    private lateinit var assetManager: AssetManager
    private lateinit var soundPool: SoundPool

    private lateinit var retrofit: Retrofit
    private lateinit var gettyService: GettyService

    private lateinit var openCurtainLeftAnimator: ObjectAnimator
    private lateinit var openCurtainRightAnimator: ObjectAnimator
    private var openCurtainsAnimatorSet = AnimatorSet()

    private lateinit var closeCurtainLeftAnimator: ObjectAnimator
    private lateinit var closeCurtainRightAnimator: ObjectAnimator
    private var closeCurtainsAnimatorSet = AnimatorSet()

    var sounds: MutableList<Sound> = arrayListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main);

        backgroundView = findViewById(R.id.root) as LinearLayout
        backgroundImageView = findViewById(R.id.background_image) as ImageView
        signpostTextView = findViewById(R.id.signpost_text) as TextView
        curtainLeftImageView = findViewById(R.id.curtain_left) as ImageView
        curtainRightImageView = findViewById(R.id.curtain_right) as ImageView

        backgroundImageView.setOnClickListener {
            closeem() // TODO: handle on reset
        }

        initOpenAnimators()

        initRetrofit()
        gettyService = retrofit.create(GettyService::class.java)

        var socket = connectToSocket()
        joinChannel(socket)

        assetManager = assets
        soundPool = SoundPool(10, AudioManager.STREAM_MUSIC, 0)
        loadShortSounds()
    }

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase))
    }

    private fun initRetrofit() {
        var interceptor = HttpLoggingInterceptor();
        interceptor.level = HttpLoggingInterceptor.Level.BODY

        var client = OkHttpClient.Builder()
                .addInterceptor(interceptor)
                .build()

        retrofit = Retrofit.Builder()
                .baseUrl("https://api.gettyimages.com:443/v3/")
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .build()
    }

    private fun setLocationSignpostText(location: String) {
        Observable.just("")
                .subscribeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    signpostTextView.text = location

                }
    }

    private fun loadBackgroundImage(query: String) {
        gettyService.listGettyImages("display_set", 1, query, "none")
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .map(GettyResponse::imageList)
                .map { it[0] }
                .map { it.largestDisplaySize }
                .map { it.uri }
                .subscribe({
                    changeBackgroundImage(it)
                    animateCurtainsOpen()
                    Log.i(TAG, "Changing background image")
                }, { it -> Log.e(TAG, "Error downloading image metadata", it) })
    }

    private fun changeBackgroundImage(uri: String) {
        Picasso.with(this).load(uri).into(backgroundImageView)
    }

    private fun animateCurtainsOpen() {
        openCurtainsAnimatorSet.start()
    }

    private fun initOpenAnimators() {
        openCurtainLeftAnimator = initOpenCurtainLeftAnimator()
        openCurtainRightAnimator = initOpenCurtainRightAnimator()
        openCurtainsAnimatorSet
                .play(openCurtainLeftAnimator)
                .with(openCurtainRightAnimator)
    }

    private fun initOpenCurtainLeftAnimator(): ObjectAnimator {
        var curtainLeftAnimator = ObjectAnimator
                .ofFloat(curtainLeftImageView, "x", 0f, -1000f)
                .setDuration(3000)
        curtainLeftAnimator.interpolator = AccelerateDecelerateInterpolator()

        return curtainLeftAnimator
    }

    private fun initOpenCurtainRightAnimator(): ObjectAnimator {
        var curtainRightStart: Float = curtainRightImageView.left.toFloat()
        var curtainRightEnd: Float = backgroundView.right.toFloat()
        var curtainRightAnimator = ObjectAnimator
                .ofFloat(curtainRightImageView, "x", curtainRightStart, curtainRightEnd)
                .setDuration(3000)
        curtainRightAnimator.interpolator = AccelerateDecelerateInterpolator()

        return curtainRightAnimator
    }

    private fun animateCurtainsClosed() {
        initCloseAnimators()
        closeCurtainsAnimatorSet.start()
    }

    private fun initCloseAnimators() {
        closeCurtainLeftAnimator = initCloseCurtainLeftAnimator()
        closeCurtainRightAnimator = initCloseCurtainRightAnimator()
        closeCurtainsAnimatorSet
                .play(closeCurtainLeftAnimator)
                .with(closeCurtainRightAnimator)
    }

    private fun initCloseCurtainLeftAnimator(): ObjectAnimator {
        var curtainLeftAnimator = ObjectAnimator
                .ofFloat(curtainLeftImageView, "x", -1000f, 0f)
                .setDuration(3000)

        curtainLeftAnimator.interpolator = AccelerateDecelerateInterpolator()

        return curtainLeftAnimator
    }

    private fun initCloseCurtainRightAnimator(): ObjectAnimator {
        var curtainRightAnimator = ObjectAnimator
                .ofFloat(curtainRightImageView, "x", 1600f, 800f)
                .setDuration(3000)

        curtainRightAnimator.interpolator = AccelerateDecelerateInterpolator()

        return curtainRightAnimator
    }

    private fun connectToSocket(): Socket {
        var socket = Socket("ws://large-marge-server.herokuapp.com/socket/websocket?vsn=1.0.0")
        socket.connect()

        return socket
    }

    private fun closeem() {
        Observable.just("")
                .subscribeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    animateCurtainsClosed()
        }
    }

    private fun joinChannel(socket: Socket) {
        var channel = socket.chan("largemarge:events", null)

        channel.join()
                .receive("ignore", { Log.e(TAG, "IGNORE") })
                .receive("ok", { Log.i(TAG, "JOINED with " + it.toString()) })

        channel.on("start", {
            initOpenAnimators()
            playSoundsSerially()
            Log.i(TAG, "NEW MESSAGE: " + it.toString())
        })

        // TODO: on restart close curtains

        channel.on("location", {
            Log.i(TAG, "NEW LOCATION" + it.toString())
            var query = it.payload.get("location").toString()
            Log.i(TAG, "LOCATION QUERY " + query)
            setLocationSignpostText(query)
            loadBackgroundImage(query)
        })

        channel.onClose { Log.i(TAG, "CLOSED: " + it.toString()) }

        channel.onError { Log.e(TAG, "ERROR: " + it) }
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