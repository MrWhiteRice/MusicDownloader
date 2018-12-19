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
		
		handler = new Handler();
		handler.removeCallbacks(UIUpdate);
		handler.postDelayed(UIUpdate, 100);
	}
	
	private Runnable UIUpdate = new Runnable()
	{
		@Override
		public void run()
		{
			if(MainActivity.mp != null)
			{
				UpdatePlayIcon();
				UpdateLoopIcon();
				UpdateSeekBar();
				UpdateShuffleIcon();
				UpdateDetails();
			}
			
			handler.postDelayed(this, 100);
		}
	};
	
	private void UpdatePlayIcon()
	{
		if(MainActivity.mp.mediaPlayer.isPlaying())
		{
			playButton.setImageResource(R.drawable.pause);
		}
		else
		{
			playButton.setImageResource(R.drawable.play);
		}
	}
	
	private void UpdateShuffleIcon()
	{
		if(MainActivity.mp.shuffle)
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
		switch(MainActivity.mp.selectedLoop)
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
		if(MainActivity.mp.GetNewSong())
		{
			seekBar.setProgress(0);
			seekBar.setMax(MainActivity.mp.mediaPlayer.getDuration());
			MainActivity.mp.SetNewSong(false);
		}
		
		if(!MainActivity.mp.GetUserInput())
		{
			seekBar.setProgress(MainActivity.mp.mediaPlayer.getCurrentPosition());
		}
		
		elapsedTime.setText(MainActivity.mp.GetTime(MainActivity.mp.mediaPlayer.getCurrentPosition()));
		totalTime.setText(MainActivity.mp.GetTime(MainActivity.mp.mediaPlayer.getDuration()));
	}
	
	private void UpdateDetails()
	{
		songName.setText(""+MainActivity.mp.GetSong(MainActivity.mp.GetCurrentIndex()));
		artistName.setText(""+MainActivity.mp.GetCurrentIndex());
	}
	
	@Override
	public void onClick(View view)
	{
		switch(view.getId())
		{
			case R.id.PlayButton:
				MainActivity.mp.PlayToggle();
				break;
			case R.id.NextButton:
				MainActivity.mp.NextSong(true);
				break;
			case R.id.PrevButton:
				MainActivity.mp.PrevSong();
				break;
			case R.id.LoopButton:
				MainActivity.mp.Loop();
				break;
			case R.id.ShuffleButton:
				MainActivity.mp.Shuffle();
				break;
			case R.id.BackButton:
				finish();
				break;
		}
	}
	
	@Override
	public void onProgressChanged(SeekBar seekBar, int i, boolean b)
	{
		if(MainActivity.mp.GetUserInput())
		{
			seekBar.setProgress(i);
			MainActivity.mp.mediaPlayer.seekTo(i);
			MainActivity.mp.SetUserInput(false);
		}
	}
	
	@Override
	public void onStartTrackingTouch(SeekBar seekBar)
	{
		MainActivity.mp.SetUserInput(true);
	}
	
	@Override
	public void onStopTrackingTouch(SeekBar seekBar)
	{
		MainActivity.mp.SetUserInput(false);
	}
}
