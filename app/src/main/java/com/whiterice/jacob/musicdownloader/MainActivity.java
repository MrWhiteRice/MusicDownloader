package com.whiterice.jacob.musicdownloader;

import android.Manifest;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;

public class MainActivity extends Activity implements View.OnClickListener
{
	//Class Variables
	MusicPlayer mp;

	//Object variables
	Button musicPlayerButton;
	
	//Other Variables
	boolean bound;
	
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.activity_main);
		
		//Request Permissions
		if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
		{
			requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1000);
		}
		
		//Init Objects
		musicPlayerButton = findViewById(R.id.MusicPlayerButton);
		
		//Set Up Listeners
		musicPlayerButton.setOnClickListener(this);
	}
	
	protected void onStart()
	{
		super.onStart();
		
		Intent intent = new Intent(this, MusicPlayer.class);
		bindService(intent, connection, BIND_AUTO_CREATE);
	}
	
	ServiceConnection connection = new ServiceConnection()
	{
		@Override
		public void onServiceConnected(ComponentName name, IBinder service)
		{
			bound = true;
			MusicPlayer.LocalBinder mLocalBinder = (MusicPlayer.LocalBinder)service;
			mp= mLocalBinder.getMainInstance();
		}
		
		@Override
		public void onServiceDisconnected(ComponentName name)
		{
			bound = false;
			mp = null;
		}
	};
	
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
		//mp.PlaySong(0);
		mp.PlaySong(0);
	}
}