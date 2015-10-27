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
	// �뾶
	private float mRotationRadius = 150;
	// СԲ�뾶
	private float mCircleRadius = 18;
	// СԲ����ɫ
	private int[] mCircleColors;
	// ��Բ��СԲ��ת��ʱ��
	private long mRotationDuration = 1200;
	// �ڶ����ֶ���ִ�е���ʱ�� һ ѡװ���� 2 �ۺ϶�������ɢ����
	private long mSplashDuration = 1200;
	// ����ı�����ɫ
	private int mSplashBgColor = Color.WHITE;
	// ��������ʱ��
	// ����Բ�뾶
	private float mHoleRadius = 0f;
	// ��ǰ��Բ����ת�Ƕ�
	private float mCurrentRotationAngle = 0f;
	// ��ǰ��Բ�İ뾶
	private float mCurrentRotationRadius = mRotationRadius;

	// ����
	private Paint mPaint = new Paint();
	// ���Ʊ����Ļ���
	private Paint mPaintBackground = new Paint();
	// ��Ļ�����ĵ������
	private float mCentX;
	private float mCentY;
	// ��Ļ�Խ��ߵ�һ��
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
		// ��ʼ������
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
		// view��ʾ��ʱ������һ��
		// ��ʼ�����ĵ�
		mCentX = w / 2;
		mCentY = h / 2;
		// �Խ��ߵ�һ��
		mDiagonalDist = (float) Math.sqrt(w * w + h * h) / 2;
	}

	// ����������--�����������������
	public void SplashDisappear() {
		// ��ȡ����һ������
		RotationState rs = (RotationState) mState;
		rs.cancle();
		// ��ʼ�ڶ�������
		mState = new MergingState();
		// ���µ���ondraw����
		invalidate();
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		// ���ģʽ--ģ��ģʽ
		// �������һЩ�򵥵��¼��ַ�
		if (mState == null) {
			// ��һ��ִ�ж���
			mState = new RotationState();
		}
		mState.drawState(canvas);
	}

	// ��ת����
	private class RotationState extends SplashState {

		private ValueAnimator mAnimator;

		public RotationState() {
			// ��ֵ����1200ms�� ����ĳ��ʱ�̵�ǰ�ĽǶ���0-2pai��360��/1200*��ǰʱ��
			mAnimator = ValueAnimator.ofFloat(0, (float) Math.PI * 2);// 0-2pai
			mAnimator.setInterpolator(new LinearInterpolator());// ���ȼ���
			mAnimator.setDuration(mRotationDuration);
			mAnimator.addUpdateListener(new AnimatorUpdateListener() {

				@Override
				public void onAnimationUpdate(ValueAnimator animation) {
					// �õ�ĳ��ʱ�̵ĽǶ�
					mCurrentRotationAngle = (Float) animation
							.getAnimatedValue();
					// �иı���ػ� �����ondraw
					invalidate();
				}
			});
			mAnimator.setRepeatCount(ValueAnimator.INFINITE);// ������ת
			mAnimator.start();
		}

		@Override
		public void drawState(Canvas canvas) {
			// ִ����ת����
			// ����СԲ
			// 1 ��ջ���
			drawBackground(canvas);
			// 2 ����СԲ
			drawCircle(canvas);
		}

		// ֹͣ��ת
		public void cancle() {
			mAnimator.cancel();
		}

	}

	// �ۺ϶���
	private class MergingState extends SplashState {
		private ValueAnimator mAnimator;

		public MergingState() {
			// ��ֵ����1200ms�� ����ĳ��ʱ�̵�ǰ�İ뾶��mRotationRadius-0/1200*��ǰʱ��
			mAnimator = ValueAnimator.ofFloat(mRotationRadius, 0);
			mAnimator.setInterpolator(new OvershootInterpolator(10));// ����һ�����ٵ�Ч�����������ⵯһ��
			mAnimator.setDuration(mRotationDuration / 2);
			mAnimator.addUpdateListener(new AnimatorUpdateListener() {

				@Override
				public void onAnimationUpdate(ValueAnimator animation) {
					// �õ�ĳ��ʱ�̵ĽǶ�
					mCurrentRotationRadius = (Float) animation
							.getAnimatedValue();
					// �иı���ػ� �����ondraw
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
			// ִ�оۺ϶���
			// ����СԲ
			// 1 ��ջ���
			drawBackground(canvas);
			// 2 ����СԲ
			drawCircle(canvas);
		}

	}

	// ��ɢ����
	private class ExpandState extends SplashState {
		private ValueAnimator mAnimator;

		public ExpandState() {
			// ��ֵ����1200ms�� ����ĳ��ʱ�̵�ǰ�Ŀ���Բ�뾶��mRotationRadius-0/1200*��ǰʱ��
			mAnimator = ValueAnimator.ofFloat(mDiagonalDist, 0);
			mAnimator.setDuration(mRotationDuration);
			mAnimator.addUpdateListener(new AnimatorUpdateListener() {

				@Override
				public void onAnimationUpdate(ValueAnimator animation) {
					// �õ�ĳ��ʱ�̵Ŀ���Բ�İ뾶
					mHoleRadius = (Float) animation.getAnimatedValue();
					// �иı���ػ� �����ondraw
					invalidate();
				}
			});
			// mAnimator.setRepeatCount(ValueAnimator.INFINITE);// ������ת
			mAnimator.reverse();// ����������
		}

		@Override
		public void drawState(Canvas canvas) {
			// ִ����ɢ����
			// ���ƿ���Բ
			// 1 ��ջ���
			drawBackground(canvas);
			// // 2 ����СԲ
			// drawCircle(canvas);
		}
	}

	private void drawBackground(Canvas canvas) {
		if (mHoleRadius > 0f) {
			// ���ƿ���Բ
			// ���ǳ���Ļ��� ���ƵĻ��ʵĿ�ȱ�С
			// ���ʵĿ��
			float strokeWidth = mDiagonalDist - mHoleRadius;
			mPaintBackground.setStrokeWidth(strokeWidth);// ���û��ʵĿ��
			float radius = mHoleRadius + strokeWidth / 2;
			canvas.drawCircle(mCentX, mCentY, radius, mPaintBackground);
		} else {
			canvas.drawColor(mSplashBgColor);
		}
	}

	private void drawCircle(Canvas canvas) {
		// ����СԲ
		float rotationAngle = (float) (2 * Math.PI / mCircleColors.length);

		for (int i = 0; i < mCircleColors.length; i++) {
			/*
			 * x=r*cos(a)+mCentX; y=r*sin(a)+mCentY; a=��ת�Ƕ�+����Ƕ�*i
			 */
			double a = (mCurrentRotationAngle + rotationAngle * i);
			float x = (float) (mCurrentRotationRadius * Math.cos(a) + mCentX);
			float y = (float) (mCurrentRotationRadius * Math.sin(a) + mCentY);
			mPaint.setColor(mCircleColors[i]);
			canvas.drawCircle(x, y, mCircleRadius, mPaint);
		}
	}
}
