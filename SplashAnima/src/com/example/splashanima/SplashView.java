package com.example.splashanima;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.view.animation.OvershootInterpolator;

public class SplashView extends View {
	// 半径
	private float mRotationRadius = 150;
	// 小圆半径
	private float mCircleRadius = 18;
	// 小圆的颜色
	private int[] mCircleColors;
	// 大圆和小圆旋转的时间
	private long mRotationDuration = 1200;
	// 第二部分动画执行的总时间 一 选装动画 2 聚合动画和扩散动画
	private long mSplashDuration = 1200;
	// 整体的背景颜色
	private int mSplashBgColor = Color.WHITE;
	// 动画动的时候
	// 空心圆半径
	private float mHoleRadius = 0f;
	// 当前大圆的旋转角度
	private float mCurrentRotationAngle = 0f;
	// 当前大圆的半径
	private float mCurrentRotationRadius = mRotationRadius;

	// 画笔
	private Paint mPaint = new Paint();
	// 绘制背景的画笔
	private Paint mPaintBackground = new Paint();
	// 屏幕正中心点的坐标
	private float mCentX;
	private float mCentY;
	// 屏幕对角线的一半
	private float mDiagonalDist;

	private SplashState mState = null;

	private abstract class SplashState {
		public abstract void drawState(Canvas canvas);
	}

	public SplashView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	public SplashView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public SplashView(Context context) {
		super(context);
		// 初始化数据
		init(context);
	}

	private void init(Context context) {
		mCircleColors = context.getResources().getIntArray(
				R.array.splash_circle_colors);
		mPaint.setAntiAlias(true);
		mPaintBackground.setAntiAlias(true);
		mPaintBackground.setStyle(Style.STROKE); //
		mPaintBackground.setColor(mSplashBgColor);
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);
		// view显示的时候会调用一次
		// 初始化中心点
		mCentX = w / 2;
		mCentY = h / 2;
		// 对角线的一半
		mDiagonalDist = (float) Math.sqrt(w * w + h * h) / 2;
	}

	// 进入主界面--开启后面的两个动画
	public void SplashDisappear() {
		// 先取消第一个动画
		RotationState rs = (RotationState) mState;
		rs.cancle();
		// 开始第二个动画
		mState = new MergingState();
		// 重新调用ondraw方法
		invalidate();
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		// 设计模式--模板模式
		// 这里就左一些简单的事件分发
		if (mState == null) {
			// 第一次执行动画
			mState = new RotationState();
		}
		mState.drawState(canvas);
	}

	// 旋转动画
	private class RotationState extends SplashState {

		private ValueAnimator mAnimator;

		public RotationState() {
			// 估值器，1200ms内 计算某个时刻但前的角度是0-2pai（360）/1200*当前时刻
			mAnimator = ValueAnimator.ofFloat(0, (float) Math.PI * 2);// 0-2pai
			mAnimator.setInterpolator(new LinearInterpolator());// 均匀计算
			mAnimator.setDuration(mRotationDuration);
			mAnimator.addUpdateListener(new AnimatorUpdateListener() {

				@Override
				public void onAnimationUpdate(ValueAnimator animation) {
					// 得到某个时刻的角度
					mCurrentRotationAngle = (Float) animation
							.getAnimatedValue();
					// 有改变就重绘 本类的ondraw
					invalidate();
				}
			});
			mAnimator.setRepeatCount(ValueAnimator.INFINITE);// 无穷旋转
			mAnimator.start();
		}

		@Override
		public void drawState(Canvas canvas) {
			// 执行旋转动画
			// 绘制小圆
			// 1 清空画板
			drawBackground(canvas);
			// 2 绘制小圆
			drawCircle(canvas);
		}

		// 停止旋转
		public void cancle() {
			mAnimator.cancel();
		}

	}

	// 聚合动画
	private class MergingState extends SplashState {
		private ValueAnimator mAnimator;

		public MergingState() {
			// 估值器，1200ms内 计算某个时刻但前的半径是mRotationRadius-0/1200*当前时刻
			mAnimator = ValueAnimator.ofFloat(mRotationRadius, 0);
			mAnimator.setInterpolator(new OvershootInterpolator(10));// 具有一个加速的效果，会先向外弹一点
			mAnimator.setDuration(mRotationDuration / 2);
			mAnimator.addUpdateListener(new AnimatorUpdateListener() {

				@Override
				public void onAnimationUpdate(ValueAnimator animation) {
					// 得到某个时刻的角度
					mCurrentRotationRadius = (Float) animation
							.getAnimatedValue();
					// 有改变就重绘 本类的ondraw
					invalidate();
				}
			});
			mAnimator.addListener(new AnimatorListenerAdapter() {
				@Override
				public void onAnimationEnd(Animator animation) {
					super.onAnimationEnd(animation);
					mState = new ExpandState();
					invalidate();
				}
			});
			mAnimator.start();
		}

		@Override
		public void drawState(Canvas canvas) {
			// 执行聚合动画
			// 绘制小圆
			// 1 清空画板
			drawBackground(canvas);
			// 2 绘制小圆
			drawCircle(canvas);
		}

	}

	// 扩散动画
	private class ExpandState extends SplashState {
		private ValueAnimator mAnimator;

		public ExpandState() {
			// 估值器，1200ms内 计算某个时刻但前的空心圆半径是mRotationRadius-0/1200*当前时刻
			mAnimator = ValueAnimator.ofFloat(mDiagonalDist, 0);
			mAnimator.setDuration(mRotationDuration);
			mAnimator.addUpdateListener(new AnimatorUpdateListener() {

				@Override
				public void onAnimationUpdate(ValueAnimator animation) {
					// 得到某个时刻的空心圆的半径
					mHoleRadius = (Float) animation.getAnimatedValue();
					// 有改变就重绘 本类的ondraw
					invalidate();
				}
			});
			// mAnimator.setRepeatCount(ValueAnimator.INFINITE);// 无穷旋转
			mAnimator.reverse();// 反过来计算
		}

		@Override
		public void drawState(Canvas canvas) {
			// 执行扩散动画
			// 绘制空心圆
			// 1 清空画板
			drawBackground(canvas);
			// // 2 绘制小圆
			// drawCircle(canvas);
		}
	}

	private void drawBackground(Canvas canvas) {
		if (mHoleRadius > 0f) {
			// 绘制空心圆
			// ，非常宽的画笔 绘制的画笔的宽度变小
			// 画笔的宽度
			float strokeWidth = mDiagonalDist - mHoleRadius;
			mPaintBackground.setStrokeWidth(strokeWidth);// 设置画笔的宽度
			float radius = mHoleRadius + strokeWidth / 2;
			canvas.drawCircle(mCentX, mCentY, radius, mPaintBackground);
		} else {
			canvas.drawColor(mSplashBgColor);
		}
	}

	private void drawCircle(Canvas canvas) {
		// 绘制小圆
		float rotationAngle = (float) (2 * Math.PI / mCircleColors.length);

		for (int i = 0; i < mCircleColors.length; i++) {
			/*
			 * x=r*cos(a)+mCentX; y=r*sin(a)+mCentY; a=旋转角度+间隔角度*i
			 */
			double a = (mCurrentRotationAngle + rotationAngle * i);
			float x = (float) (mCurrentRotationRadius * Math.cos(a) + mCentX);
			float y = (float) (mCurrentRotationRadius * Math.sin(a) + mCentY);
			mPaint.setColor(mCircleColors[i]);
			canvas.drawCircle(x, y, mCircleRadius, mPaint);
		}
	}
}
