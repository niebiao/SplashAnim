package com.example.splashanima;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.widget.FrameLayout;

public class MainActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		fLayout = new FrameLayout(this);
		contentView = new ContentView(this);
		fLayout.addView(contentView);
		splashView = new SplashView(this);
		fLayout.addView(splashView);
		setContentView(fLayout);

		startLoad();
	}

	private Handler handler = new Handler();
	private FrameLayout fLayout;
	private ContentView contentView;
	private SplashView splashView;

	private void startLoad() {
		handler.postDelayed(new Runnable() {

			@Override
			public void run() {
				splashView.SplashDisappear();
			}
		}, 5000); // —” ±5√Î

	}

}
