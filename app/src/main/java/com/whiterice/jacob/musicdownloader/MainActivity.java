package com.whiterice.jacob.musicdownloader;

import android.Manifest;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.io.File;
import java.util.Arrays;

public class MainActivity extends Activity implements View.OnClickListener
{
	//Class Variables
	public static MusicPlayer mp;

	//Object variables
	Button musicPlayerButton;
	
	LinearLayout linearLayout;
	
	//Other Variables
	boolean bound;
	
	public static String[] songsList;
	public static String audioStoragePath;
	
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.activity_main);
		
		//Init Objects
		musicPlayerButton = findViewById(R.id.MusicPlayerButton);
		linearLayout = findViewById(R.id.LinearLayout);
		
		//Set Up Listeners
		musicPlayerButton.setOnClickListener(this);
		
		//Init Methods
		InitSongs();
	}
	
	protected void onStart()
	{
		super.onStart();
		
		//Request Permissions
		if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
		{
			requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1000);
		}
		else
		{
			Init();
		}
	}
	
	ServiceConnection connection = new ServiceConnection()
	{
		@Override
		public void onServiceConnected(ComponentName name, IBinder service)
		{
			bound = true;
			MusicPlayer.LocalBinder mLocalBinder = (MusicPlayer.LocalBinder)service;
			mp = mLocalBinder.getMainInstance();
		}
		
		@Override
		public void onServiceDisconnected(ComponentName name)
		{
			bound = false;
			mp = null;
		}
	};
	
	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults)
	{
		switch(requestCode)
		{
			case 1000:
				if(grantResults[0] == PackageManager.PERMISSION_GRANTED)
				{
					Toast.makeText(this, "Permission Granted!", Toast.LENGTH_SHORT).show();
					Init();
				}
				else
				{
					Toast.makeText(this, "Permission Denied!", Toast.LENGTH_SHORT).show();
					finish();
				}
				break;
		}
	}
	
	public static String[] getSongsList()
	{
		return songsList;
	}
	
	public static String getAudioStoragePath()
	{
		return audioStoragePath;
	}
	
	void Init()
	{
		Intent intent = new Intent(this, MusicPlayer.class);
		bindService(intent, connection, BIND_AUTO_CREATE);
	}
	
	void InitSongs()
	{
		Log.e("DONE!", "LOADED ALL MUSIC!");
		
		audioStoragePath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Music";
		File[] f = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/Music").listFiles();
		
		songsList = new String[f.length];
		
		for(int x = 0; x < f.length; x++)
		{
			songsList[x] = f[x].getName();
		}
		
		Arrays.sort(songsList);
		
		CreateList(songsList);
	}
	
	void CreateList(String[] list)
	{
		for(int x = 0; x < list.length; x++)
		{
			Button myButton = new Button(this);
			myButton.setContentDescription("Padoru" + x);
			myButton.setText(list[x].replace(".mp3", ""));
			myButton.setOnClickListener(this);
			
			LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
			linearLayout.addView(myButton, lp);
		}
	}
	
	@Override
	protected void onStop()
	{
		super.onStop();
		if(bound)
		{
			unbindService(connection);
			bound = false;
		}
	}
	
	@Override
	public void onClick(View v)
	{
		if(v.getContentDescription() != null)
		{
			String bitchId = v.getContentDescription().toString();
			if(bitchId.contains("Padoru"))
			{
				int id = Integer.parseInt(bitchId.replace("Padoru", ""));
				getMusicPlayer().PlaySong(id);
			}
			
			startActivity(new Intent(this, Player.class));
		}

		switch(v.getId())
		{
			case R.id.MusicPlayerButton:
				startActivity(new Intent(this, Player.class));
				break;
		}
	}
	
	static MusicPlayer getMusicPlayer()
	{
		return mp;
	}
}