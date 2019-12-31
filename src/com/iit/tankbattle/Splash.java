package com.iit.tankbattle;

import java.io.FileInputStream;

import org.anddev.andengine.opengl.vertex.TextVertexBuffer;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class Splash extends Activity{
	Button startBtn;
	TextView score,scoreNum;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.activity_main);
		startBtn=(Button)findViewById(R.id.startButton);
		score=(TextView)findViewById(R.id.scoreTextView);
		scoreNum=(TextView)findViewById(R.id.scoreNumTextView);
		Typeface tf = Typeface.createFromAsset(getApplicationContext()
				.getAssets(), "Plok.ttf");
		
String tankScore = "0";
		
		try {
			FileInputStream fin = openFileInput("tankscore");

			int c;

			String temp = "";
			while ((c = fin.read()) != -1) {
				temp = temp + Character.toString((char) c);
			}

			tankScore = temp;

		} catch (Exception ex) {
		}
		scoreNum.setText(tankScore);
		score.setTypeface(tf);
		scoreNum.setTypeface(tf);
		startBtn.setTypeface(tf);
		startBtn.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
finish();
				Intent i=new Intent(Splash.this,MainActivity.class);
				startActivity(i);
			}
		});
		
	}

}
