package com.iit.tankbattle;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Random;

import org.anddev.andengine.audio.music.Music;
import org.anddev.andengine.audio.music.MusicFactory;
import org.anddev.andengine.audio.sound.Sound;
import org.anddev.andengine.audio.sound.SoundFactory;
import org.anddev.andengine.engine.Engine;
import org.anddev.andengine.engine.camera.Camera;
import org.anddev.andengine.engine.handler.IUpdateHandler;
import org.anddev.andengine.engine.handler.timer.ITimerCallback;
import org.anddev.andengine.engine.handler.timer.TimerHandler;
import org.anddev.andengine.engine.options.EngineOptions;
import org.anddev.andengine.engine.options.EngineOptions.ScreenOrientation;
import org.anddev.andengine.engine.options.resolutionpolicy.RatioResolutionPolicy;
import org.anddev.andengine.entity.modifier.MoveModifier;
import org.anddev.andengine.entity.modifier.MoveXModifier;
import org.anddev.andengine.entity.scene.CameraScene;
import org.anddev.andengine.entity.scene.Scene;
import org.anddev.andengine.entity.scene.Scene.IOnSceneTouchListener;
import org.anddev.andengine.entity.scene.background.AutoParallaxBackground;
import org.anddev.andengine.entity.scene.background.ColorBackground;
import org.anddev.andengine.entity.scene.background.ParallaxBackground.ParallaxEntity;
import org.anddev.andengine.entity.sprite.Sprite;
import org.anddev.andengine.entity.text.ChangeableText;
import org.anddev.andengine.entity.util.FPSLogger;
import org.anddev.andengine.input.touch.TouchEvent;
import org.anddev.andengine.opengl.font.Font;
import org.anddev.andengine.opengl.font.FontFactory;
import org.anddev.andengine.opengl.texture.TextureOptions;
import org.anddev.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlas;
import org.anddev.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlasTextureRegionFactory;
import org.anddev.andengine.opengl.texture.region.TextureRegion;
import org.anddev.andengine.ui.activity.BaseGameActivity;





import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.Display;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends BaseGameActivity implements
		IOnSceneTouchListener {

	private Camera mCamera;

	// This one is for the font
	private BitmapTextureAtlas mFontTexture;
	private Font mFont;
	private ChangeableText score;
	private BitmapTextureAtlas mAutoParallaxBackgroundTexture;

	private TextureRegion mParallaxLayerBack;
	private TextureRegion mParallaxLayerMid;
	private TextureRegion mParallaxLayerFront;
	
	private AutoParallaxBackground autoParallaxBackground;
	// this one is for all other textures
	private BitmapTextureAtlas mBitmapTextureAtlas;
	private TextureRegion mPlayerTextureRegion;
	private TextureRegion mProjectileTextureRegion;
	private TextureRegion mTargetTextureRegion;
	private TextureRegion mPausedTextureRegion;
	private TextureRegion mWinTextureRegion;
	private TextureRegion mFailTextureRegion;
	
	// the main scene for the game
	private Scene mMainScene;
	private Sprite player;

	// win/fail sprites
	private Sprite winSprite;
	private Sprite failSprite;

	private LinkedList<Sprite> projectileLL;
	private LinkedList<Sprite> targetLL;
	private LinkedList<Sprite> projectilesToBeAdded;
	private LinkedList<Sprite> TargetsToBeAdded;
	private Sound shootingSound;
	private Sound gameOverSound;
	private Music backgroundMusic;
	private boolean runningFlag = false;
	private boolean pauseFlag = false;
	private CameraScene mPauseScene;
	private CameraScene mResultScene;
	private int hitCount;
	private final int maxScore = 0000;

	@Override
	public Engine onLoadEngine() {

		// getting the device's screen size
		final Display display = getWindowManager().getDefaultDisplay();
		int cameraWidth = display.getWidth();
		int cameraHeight = display.getHeight();

		// setting up the camera [AndEngine's camera , not the one you take
		// pictures with]
		mCamera = new Camera(0, 0, cameraWidth, cameraHeight);

		// Engine with varius options
		return new Engine(new EngineOptions(true, ScreenOrientation.LANDSCAPE,
				new RatioResolutionPolicy(cameraWidth, cameraHeight), mCamera)
				.setNeedsSound(true).setNeedsMusic(true));
	}

	@Override
	public void onLoadResources() {
		// prepare a container for the image
		mBitmapTextureAtlas = new BitmapTextureAtlas(512,512,
				TextureOptions.BILINEAR_PREMULTIPLYALPHA);
		// prepare a container for the font
		mFontTexture = new BitmapTextureAtlas(256, 256,
				TextureOptions.BILINEAR_PREMULTIPLYALPHA);
		mAutoParallaxBackgroundTexture = new BitmapTextureAtlas(1024, 1024, TextureOptions.DEFAULT);
		FontFactory.setAssetBasePath("font/");
		// setting assets path for easy access
		BitmapTextureAtlasTextureRegionFactory.setAssetBasePath("gfx/");
		
		
		//this.mParallaxLayerFront = BitmapTextureAtlasTextureRegionFactory.createFromAsset(this.mAutoParallaxBackgroundTexture, this, "parallax_background_layer_front.png", 0, 0);
		//this.mParallaxLayerBack = BitmapTextureAtlasTextureRegionFactory.createFromAsset(this.mAutoParallaxBackgroundTexture, this, "parallax_background_layer_back.png", 0,0);
		this.mParallaxLayerMid = BitmapTextureAtlasTextureRegionFactory.createFromAsset(this.mAutoParallaxBackgroundTexture, this, "parallax_background_layer_mid.png", 0,0);
		mEngine.getTextureManager().loadTexture(mAutoParallaxBackgroundTexture);
		
		// loading the image inside the container
		mPlayerTextureRegion = BitmapTextureAtlasTextureRegionFactory
				.createFromAsset(this.mBitmapTextureAtlas, this, "Player.png",
						0, 0);
		mProjectileTextureRegion = BitmapTextureAtlasTextureRegionFactory
				.createFromAsset(this.mBitmapTextureAtlas, this,
						"Projectile.png", 64, 0);
		mTargetTextureRegion = BitmapTextureAtlasTextureRegionFactory
				.createFromAsset(this.mBitmapTextureAtlas, this, "Target.png",
						128, 0);
		mPausedTextureRegion = BitmapTextureAtlasTextureRegionFactory
				.createFromAsset(this.mBitmapTextureAtlas, this, "paused.png",
						0, 64);
		mWinTextureRegion = BitmapTextureAtlasTextureRegionFactory
				.createFromAsset(this.mBitmapTextureAtlas, this, "win.png", 0,
						128);
		mFailTextureRegion = BitmapTextureAtlasTextureRegionFactory
				.createFromAsset(this.mBitmapTextureAtlas, this, "fail.png", 0,
						256);

		// preparing the font
		//mFont = new Font(mFontTexture, Typeface.create(Typeface.createFromAsset(getAssets(), "font/Flok.ttf"),
		//		Typeface.BOLD), 40, true, Color.BLACK);
		
		this.mFont = FontFactory.createFromAsset(this.mFontTexture, this, "Plok.ttf", 26, true, Color.WHITE);

		// loading textures in the engine
		mEngine.getTextureManager().loadTexture(mBitmapTextureAtlas);
		
		mEngine.getTextureManager().loadTexture(mFontTexture);
		mEngine.getFontManager().loadFont(mFont);
		//mEngine.getTextureManager().loadTextures(mAutoParallaxBackgroundTexture);
		SoundFactory.setAssetBasePath("mfx/");
		try {
			shootingSound = SoundFactory.createSoundFromAsset(mEngine
					.getSoundManager(), this, "shot.mp3");
			gameOverSound = SoundFactory.createSoundFromAsset(mEngine
					.getSoundManager(), this, "explosion.ogg");
		} catch (IllegalStateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		MusicFactory.setAssetBasePath("mfx/");

		try {
			backgroundMusic = MusicFactory.createMusicFromAsset(mEngine
					.getMusicManager(), this, "background_music.wav");
			backgroundMusic.setLooping(true);
		} catch (IllegalStateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	@Override
	public Scene onLoadScene() {
		mEngine.registerUpdateHandler(new FPSLogger());
		
		// creating a new scene for the pause menu
		mPauseScene = new CameraScene(mCamera);
		/* Make the label centered on the camera. */
		final int x = (int) (mCamera.getWidth() / 2 - mPausedTextureRegion
				.getWidth() / 2);
		final int y = (int) (mCamera.getHeight() / 2 - mPausedTextureRegion
				.getHeight() / 2);
		final Sprite pausedSprite = new Sprite(x, y, mPausedTextureRegion);
		mPauseScene.attachChild(pausedSprite);
		// makes the scene transparent
		mPauseScene.setBackgroundEnabled(true);

		// the results scene, for win/fail
		mResultScene = new CameraScene(mCamera);
		winSprite = new Sprite(x, y, mWinTextureRegion);
		failSprite = new Sprite(x, y, mFailTextureRegion);
		mResultScene.attachChild(winSprite);
		mResultScene.attachChild(failSprite);
		// makes the scene transparent
		mResultScene.setBackgroundEnabled(true);

		winSprite.setVisible(false);
		failSprite.setVisible(false);

		// set background color
		mMainScene = new Scene();
		
		autoParallaxBackground = new AutoParallaxBackground(0, 0, 0, 5);
		//autoParallaxBackground.attachParallaxEntity(new ParallaxEntity(30.0f, new Sprite(0, 0, this.mParallaxLayerBack)));
		autoParallaxBackground.attachParallaxEntity(new ParallaxEntity(20.0f, new Sprite(0,0, this.mParallaxLayerMid)));
		//autoParallaxBackground.attachParallaxEntity(new ParallaxEntity(30.0f, new Sprite(0,0, this.mParallaxLayerFront)));
				
		this.mEngine.getTextureManager().loadTextures(this.mAutoParallaxBackgroundTexture);	
		
		//mMainScene.setBackground(new ColorBackground(1f, 0.6274f, 0.8784f));
		mMainScene.setBackground(autoParallaxBackground);
		mMainScene.setOnSceneTouchListener(this);

		// set coordinates for the player
		final int PlayerX = (int)(mCamera.getWidth()-(this.mPlayerTextureRegion.getWidth()*2));
		final int PlayerY = (int) ((mCamera.getHeight() - mPlayerTextureRegion
				.getHeight()) / 2);

		// set the player on the scene
		player = new Sprite(PlayerX, PlayerY, mPlayerTextureRegion);
		player.setScale(1.5f);

		// initializing variables
		projectileLL = new LinkedList<Sprite>();
		targetLL = new LinkedList<Sprite>();
		projectilesToBeAdded = new LinkedList<Sprite>();
		TargetsToBeAdded = new LinkedList<Sprite>();

		// settings score to the value of the max score to make sure it appears
		// correctly on the screen
		score = new ChangeableText(0, 0, mFont, "SCORE: 0000");
		// repositioning the score later so we can use the score.getWidth()
		//score.setPosition(mCamera.getWidth() - score.getWidth() - 5, 5);
		score.setPosition(-60,330);
		score.setRotation(270);
		createSpriteSpawnTimeHandler();
		mMainScene.registerUpdateHandler(detect);

		// starting background music
		backgroundMusic.play();
		// runningFlag = true;

		restart();
		return mMainScene;
	}

	@Override
	public void onLoadComplete() {
	}

	// TimerHandler for collision detection and cleaning up
	IUpdateHandler detect = new IUpdateHandler() {
		@Override
		public void reset() {
		}

		@Override
		public void onUpdate(float pSecondsElapsed) {

			Iterator<Sprite> targets = targetLL.iterator();
			Sprite _target;
			boolean hit = false;

			// iterating over the targets
			while (targets.hasNext()) {
				_target = targets.next();

				// if target passed the left edge of the screen, then remove it
				// and call a fail
				if (_target.getX() >= mCamera.getWidth()) {
					removeSprite(_target, targets);
					gameOverSound.play();
					fail();
					break;
				}
				Iterator<Sprite> projectiles = projectileLL.iterator();
				Sprite _projectile;
				// iterating over all the projectiles (bullets)
				while (projectiles.hasNext()) {
					_projectile = projectiles.next();

					// in case the projectile left the screen
					if (_projectile.getX() <= 0
							|| _projectile.getY() >= mCamera.getHeight()
									+ _projectile.getHeight()
							|| _projectile.getY() <= -_projectile.getHeight()) {
						removeSprite(_projectile, projectiles);
						continue;
					}

					// if the targets collides with a projectile, remove the
					// projectile and set the hit flag to true
					if (_target.collidesWith(_projectile)) {
						removeSprite(_projectile, projectiles);
						hit = true;
						break;
					}
				}

				// if a projectile hit the target, remove the target, increment
				// the hit count, and update the score
				if (hit) {
					removeSprite(_target, targets);
					hit = false;
					hitCount++;
					score.setText("SCORE:"+String.valueOf(hitCount));
				}
			}

			// if max score , then we are done
//			if (hitCount >= maxScore) {
//				win();
//			}

			// a work around to avoid ConcurrentAccessException
			projectileLL.addAll(projectilesToBeAdded);
			projectilesToBeAdded.clear();

			targetLL.addAll(TargetsToBeAdded);
			TargetsToBeAdded.clear();
		}
	};

	/* safely detach the sprite from the scene and remove it from the iterator */
	public void removeSprite(final Sprite _sprite, Iterator<Sprite> it) {
		runOnUpdateThread(new Runnable() {

			@Override
			public void run() {
				mMainScene.detachChild(_sprite);
			}
		});
		it.remove();
	}

	@Override
	public boolean onSceneTouchEvent(Scene pScene, TouchEvent pSceneTouchEvent) {

		// if the user tapped the screen
		if (pSceneTouchEvent.getAction() == TouchEvent.ACTION_DOWN) {
			final float touchX = pSceneTouchEvent.getX();
			final float touchY = pSceneTouchEvent.getY();
			//Toast.makeText(getApplicationContext(), "Test",Toast.LENGTH_SHORT).show();
			shootProjectile(touchX, touchY);
			return true;
		}
		return false;
	}

	/* shoots a projectile from the player's position along the touched area */
	private void shootProjectile(final float pX, final float pY) {
		shootingSound.play();
		int offX = (int) (pX + (mCamera.getWidth()-player.getX()));
		int offXX = (int) (0-(pX - player.getX()));
		int offY = (int) (pY - player.getY());
		if (offX >= mCamera.getWidth())
			return;
		
		final Sprite projectile;
		// position the projectile on the player
		projectile = new Sprite(mCamera.getWidth()-(player.getWidth()*2)-40, mCamera.getHeight()/2-9,
				mProjectileTextureRegion.deepCopy());
		mMainScene.attachChild(projectile, 1);

		int realX = (int) (mCamera.getWidth() + projectile.getWidth() / 2.0f);
		float ratio = (float) offY / (float) offXX;
		int realY = (int) ((realX * ratio) + projectile.getY());//

		int offRealX = (int) (realX - (mCamera.getWidth()-projectile.getX()));
		int offRealY = (int) (realY - projectile.getY());
		float length = (float) Math.sqrt((offRealX * offRealX)
				+ (offRealY * offRealY));
		float velocity = 480.0f / 1.0f; // 480 pixels / 1 sec
		float realMoveDuration = length/ velocity;

		// defining a move modifier from the projectile's position to the
		// calculated one
		MoveModifier mod = new MoveModifier(realMoveDuration,
				 realX-(int)(player.getWidth()*3)+10, (mCamera.getWidth()-projectile.getX())-200,projectile.getY(),realY);
		projectile.registerEntityModifier(mod.deepCopy());

		projectilesToBeAdded.add(projectile);
		// plays a shooting sound
		
	}

	// adds a target at a random location and let it move along the x-axis
	public void addTarget() {
		Random rand = new Random();

		int x = 0 - mTargetTextureRegion.getWidth();
		int minY =mTargetTextureRegion.getHeight();
		int maxY = (int) (mCamera.getHeight() - mTargetTextureRegion
				.getHeight());
		int rangeY = maxY - minY;
		int y = rand.nextInt(rangeY) + minY;

		Sprite target = new Sprite(x, y, mTargetTextureRegion.deepCopy());
		mMainScene.attachChild(target);

		int minDuration = 3;
		int maxDuration =5; //7-(hitCount/100);
		int rangeDuration = maxDuration - minDuration;
		int actualDuration=4;
		if(hitCount>250)
		{
			actualDuration = (rand.nextInt(rangeDuration) + minDuration)-2;
		}
		else if(hitCount<250)
		{
			actualDuration = (rand.nextInt(rangeDuration) + minDuration)-(hitCount/100);
		}
		MoveXModifier mod = new MoveXModifier(actualDuration, target.getX(),
				mCamera.getWidth()+target.getWidth());
		target.registerEntityModifier(mod.deepCopy());

		TargetsToBeAdded.add(target);

	}

	// a Time Handler for spawning targets, triggers every 1 second
	private void createSpriteSpawnTimeHandler() {
		TimerHandler spriteTimerHandler;
		float mEffectSpawnDelay = 1f;

		spriteTimerHandler = new TimerHandler(mEffectSpawnDelay, true,
				new ITimerCallback() {

					@Override
					public void onTimePassed(TimerHandler pTimerHandler) {

						addTarget();
					}
				});

		getEngine().registerUpdateHandler(spriteTimerHandler);
	}

	/* to restart the game and clear the whole screen */
	public void restart() {

		runOnUpdateThread(new Runnable() {

			@Override
			// to safely detach and re-attach the sprites
			public void run() {
				mMainScene.detachChildren();
				mMainScene.attachChild(player, 0);
				mMainScene.attachChild(score);
			}
		});

		// resetting everything
		hitCount = 0;
		score.setText("SCORE:"+String.valueOf(hitCount));
		
		projectileLL.clear();
		projectilesToBeAdded.clear();
		TargetsToBeAdded.clear();
		targetLL.clear();
	}

	@Override
	// pauses the music and the game when the game goes to the background
	protected void onPause() {
		if (runningFlag) {
			pauseMusic();
			if (mEngine.isRunning()) {
				pauseGame();
				pauseFlag = true;
			}
		}
		super.onPause();
	}

	
	@Override
	public void onResumeGame() {
		super.onResumeGame();
		// shows this Toast when coming back to the game
		if (runningFlag) {
			if (pauseFlag) {
				pauseFlag = false;
				Toast.makeText(this, "Press Menu or Back Button to Resume",
						Toast.LENGTH_SHORT).show();
			} else {
				// in case the user clicks the home button while the game on the
				// resultScene
				resumeMusic();
				mEngine.stop();
			}
		} else {
			runningFlag = true;
		}
	}
	
	

	public void pauseMusic() {
		if (runningFlag)
			if (backgroundMusic.isPlaying())
				backgroundMusic.pause();
	}

	public void resumeMusic() {
		if (runningFlag)
			if (!backgroundMusic.isPlaying())
				backgroundMusic.resume();
	}

	public void fail() {
		if (mEngine.isRunning()) {
			winSprite.setVisible(false);
			//failSprite.setVisible(true);
			
			MainActivity.this.runOnUiThread(new Runnable() {
		         @Override
		         public void run() {
		        	 Typeface tf = Typeface.createFromAsset(getApplicationContext().getAssets(),
		 					"Plok.ttf");
		        	 Dialog d = new Dialog(MainActivity.this);
		 			d.setCancelable(false);
		  	       d.setContentView(R.layout.alertdialog);
		  	       d.setTitle("                -:-:-:-:♣ GAME OVER ♣:-:-:-:-");
			       d.getWindow().setLayout(getWindowManager().getDefaultDisplay().getWidth(),getWindowManager().getDefaultDisplay().getHeight());
		  		    //   d.setCancelable(false);
		  		       
		  		       String tankScore="0";
		  		       int ds;
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
		  				ds=Integer.parseInt(tankScore);
		  				String dscore = String.valueOf(hitCount);
		  				
		  		if(tankScore=="0"){
		  				try {
		  					FileOutputStream fOut = openFileOutput("tankscore", MODE_PRIVATE);
		  					fOut.write(dscore.getBytes());
		  	
		  					fOut.close();
		  					 
		  	
		  				} catch (Exception e) {
		  					// TODO Auto-generated catch block
		  	
		  				}
		  		}
		  		if(hitCount>ds){
		  			try {
		  				FileOutputStream fOut = openFileOutput("tankscore", MODE_PRIVATE);
		  				fOut.write(dscore.getBytes());
		  				tankScore=hitCount+"";
		  				fOut.close();
		  			
		  	
		  			} catch (Exception e) {
		  				// TODO Auto-generated catch block
		  	
		  			}
		  		}
		  		TextView bestScoreTV=(TextView)d.findViewById(R.id.bestScore);
		  		TextView scoreTV=(TextView)d.findViewById(R.id.score);
		  		TextView now=(TextView)d.findViewById(R.id.now); 
		  		TextView again=(TextView)d.findViewById(R.id.again);
		  		TextView bari=(TextView)d.findViewById(R.id.bari);
		  		TextView high=(TextView)d.findViewById(R.id.high);
		  		again.setOnClickListener(new OnClickListener() {
		  			
		  			@Override
		  			public void onClick(View v) {
		  				// TODO Auto-generated method stub
		  				Intent intents = getIntent();
		  			    finish();
		  			    
		  			    startActivity(intents);
		  			}
		  		});
		  		bari.setOnClickListener(new OnClickListener() {
		  			
		  			@Override
		  			public void onClick(View v) {
		  				// TODO Auto-generated method stub
		  				Intent dudhvatIntent=new Intent(MainActivity.this,Splash.class);
		  				finish();
		  				
		  				startActivity(dudhvatIntent);	
		  			}
		  		});
		  	
		  		now.setTypeface(tf);
		  		again.setTypeface(tf);
		  		again.setTextColor(Color.parseColor("#FFB405"));
		  		bari.setTextColor(Color.parseColor("#FFB405"));
		  		bari.setTypeface(tf);
		  		high.setTypeface(tf);
		  		       scoreTV.setText(hitCount+"");
		  		       scoreTV.setTypeface(tf);
		  		      
		  		       bestScoreTV.setText(tankScore);
		  		       bestScoreTV.setTypeface(tf);
		  		       Button homeButton=(Button)d.findViewById(R.id.home);
		  		       homeButton.setOnClickListener(new OnClickListener() {
		  					
		  					@Override
		  					public void onClick(View v) {
		  						// TODO Auto-generated method stub
		  						Intent splashIntent=new Intent(MainActivity.this,Splash.class);
		  						finish();
		  						startActivity(splashIntent);
		  					}
		  				});
		  		       Button retryButton=(Button)d.findViewById(R.id.retry);
		  		       retryButton.setOnClickListener(new OnClickListener() {
		  					
		  					@Override
		  					public void onClick(View v) {
		  						// TODO Auto-generated method stub
		  						
		  						Intent intent = getIntent();
		  					    finish();
		  					    
		  					    startActivity(intent);
		  					}
		  				});
		  		       
		  		  
		  	     d.show();  
		             
		         }
		        });
				
			
   
		
			mMainScene.setChildScene(mResultScene, false, true, true);
			mEngine.stop();
		}
	}

	public void win() {
		if (mEngine.isRunning()) {
			failSprite.setVisible(false);
			winSprite.setVisible(true);
			mMainScene.setChildScene(mResultScene, false, true, true);
			mEngine.stop();
		}
	}

	public void pauseGame() {
		if (runningFlag) {
			mMainScene.setChildScene(mPauseScene, false, true, true);
			mEngine.stop();
		}
	}
	
	public void unPauseGame(){
		mMainScene.clearChildScene();
	}

	@Override
	public boolean onKeyDown(final int pKeyCode, final KeyEvent pEvent) {
		// if menu button is pressed
		if (pKeyCode == KeyEvent.KEYCODE_MENU
				&& pEvent.getAction() == KeyEvent.ACTION_DOWN) {
			if (mEngine.isRunning() && backgroundMusic.isPlaying()) {
				pauseMusic();
				pauseFlag = true;
				pauseGame();
				Toast.makeText(this, " Press Menu or Back Button to Resume",
						Toast.LENGTH_SHORT).show();
			} else {
				if (!backgroundMusic.isPlaying()) {
					unPauseGame();
					pauseFlag = false;
					resumeMusic();
					mEngine.start();
				}
				return true;
			}
			// if back key was pressed
		} 
		return super.onKeyDown(pKeyCode, pEvent);
	}
	@Override
	public void onBackPressed() {
		// TODO Auto-generated method stub
		if (mEngine.isRunning() && backgroundMusic.isPlaying()) {
			pauseMusic();
			pauseFlag = true;
			pauseGame();
			Toast.makeText(this, " Press Back or Menu Button to Resume",
					Toast.LENGTH_SHORT).show();
		} else {
			if (!backgroundMusic.isPlaying()) {
				unPauseGame();
				pauseFlag = false;
				resumeMusic();
				mEngine.start();
			}
		
	}
		
	}}

// for time handler
// http://www.andengine.org/forums/tutorials/using-timer-s-sprite-spawn-example-t463.html