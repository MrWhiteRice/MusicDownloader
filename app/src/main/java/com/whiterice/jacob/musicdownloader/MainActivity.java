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
import android.support.annotation.NonNull;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;

public class MainActivity extends Activity implements View.OnClickListener
{
	//Class Variables
	public static MusicPlayer mp;

	//Object variables
	Button musicPlayerButton;
	
	//Other Variables
	boolean bound;
	
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.activity_main);
		
		//Init Objects
		musicPlayerButton = findViewById(R.id.MusicPlayerButton);
		
		//Set Up Listeners
		musicPlayerButton.setOnClickListener(this);
	}
	
	protected void onStart()
	{
		super.onStart();
		
		//Request Permissions
		if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
		{
			requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1000);
		}
		
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
				}
				else
				{
					Toast.makeText(this, "Permission Denied!", Toast.LENGTH_SHORT).show();
					finish();
				}
				break;
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
		startActivity(new Intent(this, Player.class));
		//mp.PlaySong(0);
	}
	
	static MusicPlayer getMusicPlayer()
	{
		return mp;
	}
}