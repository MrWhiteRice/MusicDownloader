package com.whiterice.jacob.musicdownloader;

import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;

public class Player extends AppCompatActivity implements View.OnClickListener, SeekBar.OnSeekBarChangeListener
{
	Handler handler;
	MusicPlayer mp;
	
	ImageButton playButton;
	ImageButton nextButton;
	ImageButton prevButton;
	ImageButton loopButton;
	ImageButton shuffleButton;
	Button backButton;
	SeekBar seekBar;
	TextView elapsedTime;
	TextView totalTime;
	TextView songName;
	TextView artistName;
	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		//init
		super.onCreate(savedInstanceState);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.activity_player);
		
		//init objects
		playButton = findViewById(R.id.PlayButton);
		nextButton = findViewById(R.id.NextButton);
		prevButton = findViewById(R.id.PrevButton);
		loopButton = findViewById(R.id.LoopButton);
		backButton = findViewById(R.id.BackButton);
		shuffleButton = findViewById(R.id.ShuffleButton);
		
		seekBar = findViewById(R.id.SeekBarMusic);
		seekBar.getProgressDrawable().setColorFilter(getResources().getColor(R.color.blue), PorterDuff.Mode.SRC_IN);
		
		elapsedTime = findViewById(R.id.ElapsedTime);
		totalTime = findViewById(R.id.TotalTime);
		songName = findViewById(R.id.SongName);
		artistName = findViewById(R.id.ArtistName);
		
		//listeners
		playButton.setOnClickListener(this);
		nextButton.setOnClickListener(this);
		prevButton.setOnClickListener(this);
		loopButton.setOnClickListener(this);
		backButton.setOnClickListener(this);
		shuffleButton.setOnClickListener(this);
		seekBar.setOnSeekBarChangeListener(this);
		
		mp = MainActivity.getMusicPlayer();
		
		handler = new Handler();
		handler.removeCallbacks(UIUpdate);
		handler.postDelayed(UIUpdate, 100);
	}
	
	private Runnable UIUpdate = new Runnable()
	{
		@Override
		public void run()
		{
			//if(mp.mediaPlayer != null)
			//{
				UpdatePlayIcon();
				UpdateLoopIcon();
				UpdateSeekBar();
				UpdateShuffleIcon();
				UpdateDetails();
			//}
			
			handler.postDelayed(this, 100);
		}
	};
	
	private void UpdatePlayIcon()
	{
		if(mp.mediaPlayer != null)
		{
			if(mp.IsPlaying())
			{
				playButton.setImageResource(R.drawable.pause);
			}
			else
			{
				playButton.setImageResource(R.drawable.play);
			}
		}
		else
		{
			playButton.setImageResource(R.drawable.play);
		}
	}
	
	private void UpdateShuffleIcon()
	{
		if(mp.GetShuffle())
		{
			shuffleButton.setColorFilter(Color.parseColor(getResources().getString(0+R.color.blue)));
		}
		else
		{
			shuffleButton.setColorFilter(null);
		}
	}
	
	private void UpdateLoopIcon()
	{
		switch(mp.GetSelectedLoop())
		{
			case 0://no loop
				loopButton.setColorFilter(null);
				loopButton.setImageResource(R.drawable.loop);
				break;
			case 1://loop all
				loopButton.setImageResource(R.drawable.loop);
				loopButton.setColorFilter(Color.parseColor(getResources().getString(0+R.color.blue)));
				break;
			case 2://repeat single
				loopButton.setImageResource(R.drawable.repeat);
				loopButton.setColorFilter(Color.parseColor(getResources().getString(0+R.color.green)));
				break;
		}
	}
	
	private void UpdateSeekBar()
	{
		if(mp.mediaPlayer != null)
		{
			if(!mp.GetUserInput())
			{
				seekBar.setProgress(mp.mediaPlayer.getCurrentPosition());
				elapsedTime.setText(mp.GetTime(mp.mediaPlayer.getCurrentPosition()));
				
				seekBar.setMax(mp.mediaPlayer.getDuration());
				totalTime.setText(mp.GetTime(mp.mediaPlayer.getDuration()));
			}
		}
	}
	
	private void UpdateDetails()
	{
		songName.setText(""+mp.GetSong(mp.GetCurrentIndex()));
		artistName.setText(""+mp.GetCurrentIndex());
	}
	
	@Override
	public void onClick(View view)
	{
		switch(view.getId())
		{
			case R.id.PlayButton:
				mp.PlayToggle();
				break;
			case R.id.NextButton:
				mp.NextSong(true);
				break;
			case R.id.PrevButton:
				mp.PrevSong();
				break;
			case R.id.LoopButton:
				mp.Loop();
				break;
			case R.id.ShuffleButton:
				mp.Shuffle();
				break;
			case R.id.BackButton:
				finish();
				break;
		}
	}
	
	@Override
	public void onProgressChanged(SeekBar seekBar, int i, boolean b)
	{
		if(mp.GetUserInput())
		{
			seekBar.setProgress(i);
			elapsedTime.setText(mp.GetTime(mp.mediaPlayer.getCurrentPosition()));
			mp.SeekTo(i);
			mp.Play();
		}
	}
	
	@Override
	public void onStartTrackingTouch(SeekBar seekBar)
	{
		mp.SetUserInput(true);
	}
	
	@Override
	public void onStopTrackingTouch(SeekBar seekBar)
	{
		mp.SetUserInput(false);
	}
}
