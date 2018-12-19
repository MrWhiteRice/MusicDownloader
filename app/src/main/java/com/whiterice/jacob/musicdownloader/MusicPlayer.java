package com.whiterice.jacob.musicdownloader;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.Environment;
import android.os.IBinder;
import android.os.PowerManager;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Random;

public class MusicPlayer extends Service implements MediaPlayer.OnPreparedListener, MediaPlayer.OnCompletionListener, MediaPlayer.OnErrorListener
{
	boolean userInput;
	boolean newSong;
	boolean shuffle;
	
	IBinder binder = new LocalBinder();
	
	MediaPlayer mediaPlayer;
	AudioManager audioManager;
	
	public PlayBackState state;
	
	float VOLUME_DUCK = 0.1f; //FIX VOLUME LATER
	float VOLUME_NORMAL = 1.0f; //FIX VOLUME LATER
	
	int currentPosition = -1;
	int selectedLoop = 0;
	
	MusicPlayer mService = this;
	
	String audioStoragePath;
	String[] songsList;
	
	//enums
	enum PlayBackState
	{
		STATE_PLAYING, STATE_PAUSED, STATE_STOPPED, STATE_BUFFERING
	}
	
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
			mediaPlayer.setWakeMode(mService.getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);
			
			mediaPlayer.setOnPreparedListener(this);
			mediaPlayer.setOnCompletionListener(this);
			mediaPlayer.setOnErrorListener(this);
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
					p = -1;//dont loop
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
	
	void SetNewSong(boolean set)
	{
		newSong = set;
	}
	
	boolean GetNewSong()
	{
		return newSong;
	}
	
	int GetCurrentIndex()
	{
		return currentPosition;
	}
	
	void PlaySong(int index)
	{
		CreateMediaPlayer();
		
		newSong = true;
		
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
	
	void Shuffle()
	{
		shuffle = !shuffle;
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
	
	/*public void play(QueueItem item, boolean reset)
	{
		mPlayOnFocusGain = true;
		
		String mediaId = item.getDescription().getMediaId();
		boolean mediaHasChanged = !TextUtils.equals(mediaId, mCurrentMediaId) || reset;
		if (mediaHasChanged)
		{
			mCurrentPosition = 0;
			mCurrentMediaId = mediaId;
		}
		
		if(state == PlayBackState.STATE_PAUSED && !mediaHasChanged && mediaPlayer != null)
		{
			// Begin playing if we were paused before and the media hasn't changed
			ConfigureMediaPlayerState();
		}
		else
		{
			// Recreate the media player
			state = PlayBackState.STATE_STOPPED;
			RelaxResources(false); // release everything except MediaPlayer
			
			// Get the source for the music track
			MediaMetadata track = mMusicProvider.getMusic(mediaId);
			String source = track.getString(MusicProvider.CUSTOM_METADATA_TRACK_SOURCE);
			
			try
			{
				CreateMediaPlayer();
				
				state = PlayBackState.STATE_BUFFERING;
				
				mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
				mediaPlayer.setDataSource(source);
				
				// Starts preparing the media player in the background. When
				// it's done, it will call our OnPreparedListener (that is,
				// the onPrepared() method on this class, since we set the
				// listener to 'this'). Until the media player is prepared,
				// we *cannot* call start() on it!
				mediaPlayer.prepareAsync();
				
				if (mCallback != null)
				{
					mCallback.onPlaybackStatusChanged(state);
				}
				
			}
			catch (IOException ex)
			{
			
			}
		}
	}*/
	
	void RelaxResources(boolean releaseMediaPlayer)
	{
		CreateMediaPlayer();
		
		mService.stopForeground(true);
		
		if(releaseMediaPlayer)
		{
			mediaPlayer.reset();
			mediaPlayer.release();
			mediaPlayer = null;
		}
	}
	
	/*void ConfigureMediaPlayerState()
	{
		if (mAudioFocus == AUDIO_NO_FOCUS_NO_DUCK)
		{
			// If we don't have audio focus and can't duck, we have to pause,
			if (state == PlayBackState.STATE_PLAYING)
			{
				pause();
			}
		}
		else
		{  // we have audio focus:
			if (mAudioFocus == AUDIO_NO_FOCUS_CAN_DUCK)
			{
				mediaPlayer.setVolume(VOLUME_DUCK, VOLUME_DUCK); // we'll be relatively quiet
			}
			else
			{
				if (mediaPlayer != null)
				{
					mediaPlayer.setVolume(VOLUME_NORMAL, VOLUME_NORMAL); // we can be loud again
				} // else do something for remote client.
			}
			
			// If we were playing when we lost focus, we need to resume playing.
			if (mPlayOnFocusGain)
			{
				if (mediaPlayer != null && !mediaPlayer.isPlaying())
				{
					if (currentPosition == mediaPlayer.getCurrentPosition())
					{
						mediaPlayer.start();
						state = PlayBackState.STATE_PLAYING;
					}
					else
					{
						mediaPlayer.seekTo(currentPosition);
						state = PlayBackState.STATE_BUFFERING;
					}
				}
				mPlayOnFocusGain = false;
			}
		}
		
		if (mCallback != null)
		{
			mCallback.onPlaybackStatusChanged(state);
		}
	}*/
	
	@Override
	public void onPrepared(MediaPlayer mp)
	{
		//ConfigureMediaPlayerState();
		mp.start();
	}
	
	@Override
	public void onCompletion(MediaPlayer mp)
	{
		if(mediaPlayer.getCurrentPosition() > 100)
		{
			NextSong(false);
		}
	}
	
	@Override
	public boolean onError(MediaPlayer mp, int what, int extra)
	{
		return false;
	}
}
