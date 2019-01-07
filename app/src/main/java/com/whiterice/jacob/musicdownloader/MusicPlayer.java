package com.whiterice.jacob.musicdownloader;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.IBinder;
import android.os.PowerManager;
import android.util.Log;

import java.io.IOException;
import java.util.Random;

public class MusicPlayer extends Service implements MediaPlayer.OnPreparedListener, MediaPlayer.OnCompletionListener
{
	boolean userInput;
	boolean shuffle;
	
	IBinder binder = new LocalBinder();
	
	MediaPlayer mediaPlayer;
	AudioManager audioManager;
	
	int currentPosition = -1;
	int selectedLoop = 0;
	
	String audioStoragePath;
	String[] songsList;
	
	//System Methods
	public IBinder onBind(Intent intent) {
		return binder;
	}
	
	public class LocalBinder extends Binder
	{
		public MusicPlayer getMainInstance() {
			return MusicPlayer.this;
		}
	}
	
	public int onStartCommand(Intent intent, int flags, int startId)
	{
		audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
		
		Log.e("DONE!", "LOADED ALL MUSIC!");
		
		//probs return startId instead of start_sticky(1)?
		return START_STICKY;
	}
	
	@Override
	public void onCreate()
	{
		super.onCreate();
		
		InitSongs();
		CreateMediaPlayer();
	}
	
	void InitSongs()
	{
		songsList = MainActivity.getSongsList();
		audioStoragePath = MainActivity.getAudioStoragePath();
	}
	
	void CreateMediaPlayer()
	{
		if(mediaPlayer == null)
		{
			mediaPlayer = new MediaPlayer();
			mediaPlayer.setWakeMode(getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);
			
			mediaPlayer.setOnPreparedListener(this);
			mediaPlayer.setOnCompletionListener(this);
		}
		else
		{
			mediaPlayer.reset();
		}
	}
	
	void PlayToggle()
	{
		if(mediaPlayer.isPlaying())
		{
			mediaPlayer.pause();
		}
		else
		{
			mediaPlayer.start();
		}
	}
	
	public void SeekTo(int pos)
	{
		mediaPlayer.seekTo(pos);
	}
	
	void NextSong(boolean user)
	{
		if(shuffle)
		{
			PlaySong(RandomInt(0, songsList.length));
			return;
		}
		
		int p = currentPosition;
		p++;
		
		if(p > songsList.length-1)//end of list
		{
			if(selectedLoop == 0)//no looping
			{
				if(user)//did the player press the next button
				{
					p = 0;//loop
				}
				else//song just ended
				{
					return;
				}
			}
			else if(selectedLoop == 1)//list looping
			{
				p = 0;//loop
			}
			else if(selectedLoop == 2)//single looping
			{
				p = p-1;//loop song
			}
		}
		else if(selectedLoop == 2)
		{
			p = p-1;
		}
		
		PlaySong(p);
	}
	
	void PrevSong()
	{
		int p = currentPosition;
		p--;
		
		if(p < 0)
		{
			p = songsList.length-1;
		}
		
		PlaySong(p);
	}

	String GetSong(int index)
	{
		if(index >= 0)
		{
			return songsList[index];
		}
		
		return "";
	}
	
	void SetUserInput(boolean set)
	{
		userInput = set;
	}
	
	boolean GetUserInput()
	{
		return userInput;
	}
	
	int GetCurrentIndex()
	{
		return currentPosition;
	}
	
	void PlaySong(int index)
	{
		CreateMediaPlayer();
		
		if(index == -1)
		{
			RelaxResources(true);
			return;
		}
		
		try
		{
			//mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
			mediaPlayer.setDataSource(audioStoragePath + "/" + songsList[index]);
			mediaPlayer.prepareAsync();
			
			currentPosition = index;
		}
		catch(IOException e)
		{
		
		}
	}
	
	void Loop()
	{
		selectedLoop++;
		
		if(selectedLoop > 2)
		{
			selectedLoop = 0;
		}
	}
	
	int GetSelectedLoop()
	{
		return selectedLoop;
	}
	
	void Shuffle()
	{
		shuffle = !shuffle;
	}
	
	boolean GetShuffle()
	{
		return shuffle;
	}
	
	String GetTime(long ms)
	{
		int seconds = (int) (ms / 1000) % 60 ;
		int minutes = (int) ((ms/ (1000*60)) % 60);
		int hours   = (int) ((ms/ (1000*60*60)) % 24);
		
		String secs = "" + seconds;
		
		if(seconds < 10)
		{
			secs = "0" + seconds;
		}
		
		if(hours == 0)
		{
			return "" + minutes + ":" + secs;
		}
		else
		{
			return "" + hours + ":" + minutes + ":" + secs;
		}
	}
	
	int RandomInt(int min, int max)
	{
		final int random = new Random().nextInt((max - min) + 1) + min;
		
		return random;
	}
	
	boolean IsPlaying()
	{
		return mediaPlayer.isPlaying();
	}
	
	void RelaxResources(boolean releaseMediaPlayer)
	{
		CreateMediaPlayer();
		
		stopForeground(true);
		
		if(releaseMediaPlayer)
		{
			mediaPlayer.reset();
			mediaPlayer.release();
			mediaPlayer = null;
		}
	}
	
	void Play()
	{
		mediaPlayer.start();
	}
	
	@Override
	public void onPrepared(MediaPlayer mp)
	{
		mp.start();
	}
	
	@Override
	public void onCompletion(MediaPlayer mp)
	{
		if(mediaPlayer.getCurrentPosition() > 100)
		{
			if(!userInput)
			{
				NextSong(false);
			}
		}
	}
}