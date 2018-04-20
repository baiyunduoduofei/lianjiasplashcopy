package com.zhouyunfei.lianjiasplashcopy;

import android.animation.Animator;
import android.animation.TimeInterpolator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathMeasure;
import android.graphics.PorterDuff;
import android.graphics.RectF;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.DecelerateInterpolator;

/**
 * Created by zyf on 2018/4/18.
 */

public class HouseView extends View {
    private Context mContext;
    private Paint mPaint;
    private int mViewHeight;
    private int mViewWidth;
    private State mCurrentState;//当前状态

    private float mLineWidth;//线宽
    private float mLitleCircleR;//小圆半径
    private float mDropCircleR;//水滴圆半径
    private float mDropOutsidePoint;//水滴圆外点与圆心的距离
    private float mDropDistance;//水滴移动距离
    private float mHouseWidth;//房子宽度


    private float mLineWidthRate = 18;//布局最小边与线宽的比值
    private float mLitleCircleRRate = 36;//布局最小边与小圆半径的比值
    private float mDropCircleRRate = 15;//布局最小边与水滴圆半径的比值
    private float mDropOutsidePointRate = 10;//布局最小边与（水滴圆外点与圆心的距离）的比值
    private float mDropDistanceRate = 1.2f;//布局最小边与水滴移动距离的比值
    private float mHouseWidthRate = 3;//布局最小边与房子宽度的比值
    private ValueAnimator.AnimatorUpdateListener mAnimatorUpdateListener;//用于更新动画进度
    private Animator.AnimatorListener mAnimatorListener;//用于更新动画各阶段状态
    private float mAnimatorValue;//当前动画执行进度
    private ValueAnimator mDropCircleAnimator;//水滴下落动画
    private ValueAnimator mHouseDrowAnimator;//房子动画
    private ValueAnimator mCircleShakeAnimator;//小圆晃动动画
    private long mDefaultDuration = 3000;//动画周期
    private Path mDropPath;//水滴绘画路径
    private Path mHousePath;//房子路径
    private Path mHouseTmpPath;//房子临时路径

    private PathMeasure mMeasure;//用于截取house路径
    private boolean isShow = true;


    private AnimationListener mListener;//动画回调


    public HouseView(Context context) {
        super(context);
        init(context);
    }

    public HouseView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }


    /***
     * 设置动画回调
     * @param animatorListener
     */
    public void setAnimatorListener(AnimationListener animatorListener) {
        this.mListener = animatorListener;
    }

    /***
     * 开启动画
     */
    public void startAnimation() {
        mPaint.setStyle(Paint.Style.FILL);
        mCurrentState = State.WATER_DROP;
        mHouseTmpPath.reset();
        mDropCircleAnimator.start();
        if (null != mListener) {
            mListener.onStart();
        }
    }

    public void startAnimation(long dropDuration, long houseDuration, long circleDuration) {
        if (null != mDropCircleAnimator) {
            mDropCircleAnimator.setDuration(dropDuration);
        }
        if (null != mHouseDrowAnimator) {
            mHouseDrowAnimator.setDuration(houseDuration);
        }
        if (null != mCircleShakeAnimator) {
            mCircleShakeAnimator.setDuration(circleDuration);
        }
        startAnimation();
    }


    public static enum State {
        END,//结束状态
        WATER_DROP,//水滴滴落
        HOUSE_DRAW,//房子绘制
        CIRCLE_SHAKE//中心小圆点晃动

    }


    /**
     * 初始化
     *
     * @param context
     */
    private void init(Context context) {
        this.mContext = context;
        mPaint = new Paint();
        mPaint.setColor(Color.GREEN);
        mPaint.setStrokeCap(Paint.Cap.ROUND);
        mPaint.setStrokeJoin(Paint.Join.ROUND);//圆弧
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setAntiAlias(true);
        mPaint.setDither(true);
        mDropPath = new Path();
        mHousePath = new Path();
        mHouseTmpPath = new Path();
        mMeasure = new PathMeasure();
        initListener();
        initAnimator();
    }

    /**
     * 初始化动画
     */
    private void initAnimator() {
        //初始动画
        mDropCircleAnimator = ValueAnimator.ofFloat(0, 1).setDuration(mDefaultDuration);
        mHouseDrowAnimator = ValueAnimator.ofFloat(0, 1).setDuration(mDefaultDuration);
        mCircleShakeAnimator = ValueAnimator.ofFloat(0, 1).setDuration(mDefaultDuration);
        //设置插值器
        mDropCircleAnimator.setInterpolator(new DropInterpolator());
        mHouseDrowAnimator.setInterpolator(new DecelerateInterpolator());
        //设置进度监听
        mDropCircleAnimator.addUpdateListener(mAnimatorUpdateListener);
        mHouseDrowAnimator.addUpdateListener(mAnimatorUpdateListener);
        mCircleShakeAnimator.addUpdateListener(mAnimatorUpdateListener);
        //设置动画监听
        mDropCircleAnimator.addListener(mAnimatorListener);
        mHouseDrowAnimator.addListener(mAnimatorListener);
        mCircleShakeAnimator.addListener(mAnimatorListener);
    }


    /***
     * 初始化监听
     */

    private void initListener() {
        mAnimatorUpdateListener = new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                mAnimatorValue = (float) animation.getAnimatedValue();
                invalidate();
            }
        };


        mAnimatorListener = new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                changeAnimationState();
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        };
    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.translate(mViewWidth / 2, 0);
        switch (mCurrentState) {
            case WATER_DROP:
                drawDrop(canvas);
                break;
            case HOUSE_DRAW:
                drawHouse(canvas);
                break;
            case CIRCLE_SHAKE:
                drawCircle(canvas);
                break;
            case END:
                drawEnd(canvas);
                break;
        }

    }


    /**
     * 画水滴动画
     *
     * @param canvas
     */
    private void drawDrop(Canvas canvas) {
        mDropPath.reset();
        RectF oval = new RectF((int) ((mHouseWidth * 0.5) - mDropCircleR * (1 - mAnimatorValue)),
                (int) (mDropDistance * mAnimatorValue),
                (int) (mDropCircleR * (1 - mAnimatorValue) + (mHouseWidth * 0.5)),
                (int) (mDropCircleR * (1 - mAnimatorValue) * 2 + mDropDistance * mAnimatorValue));
        mDropPath.addArc(oval, -180, 180);
        mDropPath.lineTo((int) (mHouseWidth * 0.5), mDropCircleR * (1 - mAnimatorValue) + mDropDistance * mAnimatorValue + mDropOutsidePoint * (1 - mAnimatorValue));
        canvas.drawPath(mDropPath, mPaint);
    }

    /***
     * 画房子动画
     * @param canvas
     */
    private void drawHouse(Canvas canvas) {
        mMeasure.getSegment(0, mMeasure.getLength() * mAnimatorValue, mHouseTmpPath, true);
        canvas.drawPath(mHouseTmpPath, mPaint);

    }

    /**
     * 画小圆动画
     *
     * @param canvas
     */
    private void drawCircle(Canvas canvas) {
        mPaint.setStyle(Paint.Style.STROKE);
        canvas.drawPath(mHousePath, mPaint);
        mPaint.setStyle(Paint.Style.FILL);
        canvas.drawCircle(mHouseWidth * 0.5f, getLitleCicleY(), mLitleCircleR, mPaint);
    }

    /**
     * 绘画结束状态
     *
     * @param canvas
     */
    private void drawEnd(Canvas canvas) {
        if (isShow) {
            mPaint.setStyle(Paint.Style.STROKE);
            canvas.drawPath(mHousePath, mPaint);
            mPaint.setStyle(Paint.Style.FILL);
            canvas.drawCircle(mHouseWidth * 0.5f, mViewHeight - mLineWidth - mHouseWidth * 0.3f, mLitleCircleR, mPaint);
        }
    }


    /**
     * 获取Y 坐标
     *
     * @return
     */
    private float getLitleCicleY() {
        if (mAnimatorValue <= 0.5) {
            return mViewHeight - mLineWidth - mHouseWidth * 0.6f + mAnimatorValue / 0.5f * mHouseWidth * 0.3f;
        } else if (mAnimatorValue <= 0.75) {
            return mViewHeight - mLineWidth - mHouseWidth * 0.3f - (mAnimatorValue - 0.5f) / 0.25f * mHouseWidth * 0.1f;
        } else {
            return mViewHeight - mLineWidth - mHouseWidth * 0.4f + (mAnimatorValue - 0.75f) / 0.25f * mHouseWidth * 0.1f;
        }
    }


    /***
     * 更改动画状态
     */
    private void changeAnimationState() {
        switch (mCurrentState) {
            case WATER_DROP:
                mCurrentState = State.HOUSE_DRAW;
                mPaint.setStrokeWidth(mLineWidth);
                mPaint.setStyle(Paint.Style.STROKE);
                mHouseDrowAnimator.start();
                break;
            case HOUSE_DRAW:
                mCurrentState = State.CIRCLE_SHAKE;
                mCircleShakeAnimator.start();
                break;
            case CIRCLE_SHAKE:
                mPaint.setStyle(Paint.Style.STROKE);
                mCurrentState = State.END;
                if (null != mListener) {
                    mListener.onEnd();
                }
                break;
        }

        if (null != mListener) {
            mListener.onStateChange(mCurrentState);
        }
    }


    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        updateBySize(w, h, oldw, oldh);
    }

    private void updateBySize(int w, int h, int oldw, int oldh) {
        mViewHeight = h;
        mViewWidth = w;
        int small = mViewHeight < mLineWidth ? mViewHeight : mViewWidth;
        mLineWidth = small / mLineWidthRate;
        mPaint.setStrokeWidth(mLineWidth);
        mLitleCircleR = small / mLitleCircleRRate;
        mDropCircleR = small / mDropCircleRRate;
        mDropOutsidePoint = small / mDropOutsidePointRate;
        mHouseWidth = small / mHouseWidthRate;
        mDropDistance = small / mDropDistanceRate;
        buidHousePath();
    }

    /**
     * 构造house路径
     */
    private void buidHousePath() {
        mHousePath.moveTo((int) (mHouseWidth * 0.5), mViewHeight - mLineWidth);//初始点
        mHousePath.lineTo(-(int) (mHouseWidth * 0.5), mViewHeight - mLineWidth);
        mHousePath.lineTo(-(int) (mHouseWidth * 0.5), (int) (mViewHeight - mLineWidth - mHouseWidth * 0.8));
        mHousePath.lineTo(0, (int) (mViewHeight - mLineWidth - mHouseWidth * 1.2));
        mHousePath.lineTo((int) (mHouseWidth * 0.5), (int) (mViewHeight - mLineWidth - mHouseWidth * 0.8));
        mHousePath.lineTo((int) (mHouseWidth * 0.5), (int) (mViewHeight - mLineWidth - mHouseWidth * 0.6));
        mMeasure.setPath(mHousePath, false);
    }


    class DropInterpolator implements TimeInterpolator {

        @Override
        public float getInterpolation(float input) {
            if (input <= 0.6) {
                return input / 0.6f;
            } else if (input <= 0.8) {
                return 1 - ((input - 0.6f) / 0.2f) * 0.4f;
            } else {
                return 0.6f + ((input - 0.8f) / 0.2f) * 0.4f;
            }


        }
    }

    /***
     * 动画回调
     */
    interface AnimationListener {
        void onStart();

        void onEnd();

        void onStateChange(State state);
    }
}
