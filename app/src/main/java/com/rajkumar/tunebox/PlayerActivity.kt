package com.rajkumar.tunebox

import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.media.MediaPlayer
import android.os.Bundle
import android.os.IBinder
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.rajkumar.tunebox.databinding.ActivityPlayerBinding

class PlayerActivity : AppCompatActivity(),ServiceConnection {
    companion object{
        lateinit var musicListPA:ArrayList<Music>
        var songPosition: Int = 0

        var isPlaying : Boolean = false
        var musicService: MusicService? = null
    }
    private lateinit var binding: ActivityPlayerBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(R.style.coolPink)
        binding = ActivityPlayerBinding.inflate(layoutInflater)
        setContentView(binding.root)
        // for starting service
        val intent = Intent(this,MusicService::class.java)
        bindService(intent,this, BIND_AUTO_CREATE)
        startService(intent)

        initializedLayout()
        binding.playPauseBtnPA.setOnClickListener {
            if(isPlaying){
                 pauseMusic()
            }
            else{
                playMusic()
            }
        }
        binding.previousBtnPA.setOnClickListener { prevNextMusic(increment = false)  }
        binding.netBtnPA.setOnClickListener { prevNextMusic(increment = true) }
    }
    private fun setLayout(){
        Glide.with(this)
            .load(musicListPA[songPosition].artUri)
            .apply(RequestOptions().placeholder(R.drawable.music_2__icon).centerCrop())
            .into(binding.songImgPA)
        binding.songNamePA.text = musicListPA[songPosition].title
    }
    private fun createMediaPlayer(){
        try {
            if(musicService!!.mediaPlayer==null) musicService!!.mediaPlayer = MediaPlayer()
            musicService!!.mediaPlayer!!.reset()
            musicService!!.mediaPlayer!!.setDataSource(musicListPA[songPosition].path)
            musicService!!.mediaPlayer!!.prepare()
            musicService!!.mediaPlayer!!.start()
            isPlaying = true
            binding.playPauseBtnPA.setIconResource(R.drawable.pause_icon)
        }catch (e:Exception){return}
    }
    private fun initializedLayout(){
        songPosition= intent.getIntExtra("index",0)
        when(intent.getStringExtra("class")){
            "MusicAdapter" ->{
                musicListPA = ArrayList()
                musicListPA.addAll(MainActivity.MusicListMA)
                setLayout()

            }
            "MainActivity" ->{
                musicListPA = ArrayList()
                musicListPA.addAll(MainActivity.MusicListMA)
                musicListPA.shuffle()
                setLayout()


            }
        }
    }
    private fun playMusic(){
        binding.playPauseBtnPA.setIconResource(R.drawable.pause_icon)
        isPlaying = true
        musicService!!.mediaPlayer!!.start()
    }
    private fun pauseMusic(){
        binding.playPauseBtnPA.setIconResource(R.drawable.play_icon)
        isPlaying=false
        musicService!!.mediaPlayer!!.pause()
    }
    private fun prevNextMusic(increment:Boolean){
        if(increment){
            setSongPosition(increment = true)
            setLayout()
            createMediaPlayer()
        }
        else{
           setSongPosition(increment = false)
            setLayout()
            createMediaPlayer()

        }
    }
    private fun setSongPosition(increment: Boolean){
    if(increment){
        if(musicListPA.size-1 == songPosition){
            songPosition =0;
        }
        else{
            ++songPosition
        }
    }
        else{
        if(songPosition==0){
            songPosition = musicListPA.size-1
        }
        else{
            --songPosition
        }

    }
    }

    override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
    val binder = service as MusicService.MyBinder
        musicService = binder.currentService()
        createMediaPlayer()
    }

    override fun onServiceDisconnected(p0: ComponentName?) {
        musicService = null
    }
}