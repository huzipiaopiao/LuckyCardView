package com.teaanddogdog.luckycardviewhelper;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Build;
import android.support.annotation.DrawableRes;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;

/**
 * @author banbury
 * @version v1.0
 * @created 2020/1/8_09:27.
 * @description 一个帮助任何view实现刮刮乐效果的帮助类；
 */

public class LuckyCardViewHelper {
    protected Context mContext;
    /**
     * 当项目的minSdkVersion小于23时，view要是FrameLayout，其他类型的View会导致遮挡层无效
     * 当项目的minSdkVersion大于等于23时，view可以是任何View；
     */
    protected View mView;
    protected Drawable mForeWallDrawable;
    protected Bitmap mForeWallBitmap;
    protected Canvas mForeWallCanvas;
    protected Paint mFingerPaint;                       // 触摸的画笔
    protected Path mFingerPath = new Path();            // 画笔路径
    protected boolean readyToPlay;                      // 是否初始化完成
    protected boolean complete;                         // 是否完成抽奖
    protected int x = 0;                                // 当前触点X坐标
    protected int y = 0;                                // 当前触点Y坐标
    protected LuckyCardViewHelperListener mLuckyCardViewHelperListener;           // 刮刮乐的监听器
    protected int completePercent = 50;                 // 抬手后自动显示结果的百分比

    /**
     *
     * @param view 需要变成刮刮乐的view（建议是一个FrameLayout对象）
     * @param foregroundRes 刮刮乐涂层的图片资料引用
     * @param completePercent 抬手时，检测到刮出百分之多少后，自动显示全部
     * @param listener 监听器
     * @return
     */
    public LuckyCardViewHelper init(View view, @DrawableRes int foregroundRes, int completePercent, LuckyCardViewHelperListener listener) {
        if (view == null) {
            throw new IllegalStateException("context cant null!");
        }
        this.mContext = view.getContext();
        this.mView = view;
        this.mForeWallDrawable = mContext.getResources().getDrawable(foregroundRes);
        this.completePercent = completePercent;
        this.mLuckyCardViewHelperListener = listener;
        mFingerPaint = getFingerPaint();
        view.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                if (Build.VERSION.SDK_INT >= 16) {
                    mView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                }
                int width = mView.getMeasuredWidth();
                int height = mView.getMeasuredHeight();
                mForeWallBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
                mForeWallCanvas = new Canvas(mForeWallBitmap);
                mForeWallDrawable.setBounds(0, 0, width, height);
                mForeWallDrawable.draw(mForeWallCanvas);
                readyToPlay = setTheViewForeground();
            }
        });

        view.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (!readyToPlay || complete) {
                    return false;
                }
                int currX = (int) event.getX();
                int currY = (int) event.getY();
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN: {
                        mFingerPath.reset();
                        x = currX;
                        y = currY;
                        mFingerPath.moveTo(x, y);
                        break;
                    }
                    case MotionEvent.ACTION_MOVE: {
                        // 二次贝塞尔，实现平滑曲线；previousX, previousY为操作点，cX, cY为终点
                        mFingerPath.quadTo(x, y, currX, currY);
                        x = currX;
                        y = currY;

                        // 将path绘制到topBitmap涂层上
                        mForeWallCanvas.drawPath(mFingerPath, mFingerPaint);
                        // 重绘View，再非UI线程中调用
                        mView.postInvalidate();
                        break;
                    }
                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_CANCEL: {
                        if (!complete) {
                            new ComputeTask(mForeWallBitmap).execute();
                        }
                        break;
                    }
                }
                return true;    // 事件执行完毕
            }
        });
        return this;
    }

    protected boolean setTheViewForeground() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            mView.setForeground(new BitmapDrawable(mForeWallBitmap));
            return true;
        } else if (mView instanceof FrameLayout) {
            ((FrameLayout) mView).setForeground(new BitmapDrawable(mForeWallBitmap));
            return true;
        }
        return false;
    }

    /**
     * 初始化手指画笔
     *
     * @return 画笔
     */
    protected Paint getFingerPaint() {
        Paint paint = new Paint();
        // 初始化画笔
        paint.setFlags(Paint.ANTI_ALIAS_FLAG);     // 消除锯齿
        paint.setAntiAlias(true);                  // 设置是否使用抗锯齿功能，会消耗较大资源，绘制图形速度会变慢
        paint.setDither(true);                     // 设定是否使用图像抖动处理，会使绘制出来的图片颜色更加平滑和饱满，图像更加清晰
        paint.setStyle(Paint.Style.STROKE);        // 画笔样式
        paint.setStrokeWidth(80);                  // 当画笔样式为STROKE或FILL_OR_STROKE时，设置笔刷的粗细度
        paint.setStrokeCap(Paint.Cap.ROUND);       // 当画笔样式为STROKE或FILL_OR_STROKE时，设置笔刷的图形样式
        paint.setStrokeJoin(Paint.Join.ROUND);     // 结合处的样子
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_IN));     // 设置图形重叠时的处理方式
        paint.setAlpha(0);                         // 设置绘制图形的透明度
        return paint;
    }


    protected void processResult(int transparentPercent) {
        if (mLuckyCardViewHelperListener != null) {
            mLuckyCardViewHelperListener.onHandUp(mView,transparentPercent);
        }

        if (transparentPercent >= completePercent) {
            complete = true;
            clearCoverage();
            if (mLuckyCardViewHelperListener != null) {
                mLuckyCardViewHelperListener.onComplete(mView);
            }
        }
    }

    /**
     * 清除剩余灰色涂层
     */
    protected void clearCoverage() {
        mFingerPath.reset();
        // 将涂层位图颜色设置为透明
        mForeWallBitmap.eraseColor(0);
        // 重绘
        mView.postInvalidate();
    }

    public LuckyCardViewHelper setLuckyCardViewHelperListener(LuckyCardViewHelperListener luckyCardViewHelperListener) {
        mLuckyCardViewHelperListener = luckyCardViewHelperListener;
        return this;
    }

    public void reset() {
        complete = false;
        mFingerPath.reset();
        if (mForeWallCanvas != null) {
            mForeWallDrawable.draw(mForeWallCanvas);
        }
        mView.postInvalidate();
    }

    public class ComputeTask extends AsyncTask<Integer, Void, Integer> {
        Bitmap bitmap;

        ComputeTask(Bitmap bitmap) {
            this.bitmap = bitmap;
        }

        @Override
        protected Integer doInBackground(Integer... params) {
            int w = bitmap.getWidth();
            int h = bitmap.getHeight();
            float wipeArea = 0;
            float totalArea = w * h;
            int[] mPixels = new int[w * h];
            //获取bitmap的所有像素信息
            bitmap.getPixels(mPixels, 0, w, 0, 0, w, h);
            for (int mPixel : mPixels) {
                if (mPixel == 0) {
                    wipeArea++;
                }
            }
            //计算已扫区域所占的比例
            int percent = 0;
            if (wipeArea > 0 && totalArea > 0) {
                percent = (int) (wipeArea * 100 / totalArea);
            }
            return percent;
        }

        @Override
        protected void onPostExecute(Integer integer) {
            super.onPostExecute(integer);
            processResult(integer);
        }
    }

    public interface LuckyCardViewHelperListener {

        /**
         * 没完成前，抬手后，回调（如果已完成，则不会回调）
         * @param view
         * @param scrapedPercent 已刮百分比
         */
        void onHandUp(View view, int scrapedPercent);
        /**
         * 检测到刮完后，回调
         * @param view
         */
        void onComplete(View view);

    }
}
