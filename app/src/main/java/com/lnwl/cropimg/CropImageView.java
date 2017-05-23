package com.lnwl.cropimg;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.Region;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

/**
 * 底图不变，浮层缩放
 *
 * @author yanglonghui
 */
public class CropImageView extends View {

    //单点触摸的时候
    private float oldX = 0;
    private float oldY = 0;

    //默认的裁剪图片宽度与高度
    private final int defaultCropWidth = 200;
    private final int defaultCropHeight = 200;
    private int cropWidth = defaultCropWidth;
    private int cropHeight = defaultCropHeight;

    private final int EDGE_LT = 1;//左上
    private final int EDGE_RT = 2;//右上
    private final int EDGE_LB = 3;//左下
    private final int EDGE_RB = 4;//右下
    private final int EDGE_MOVE_IN = 5;//里面移动
    private final int EDGE_MOVE_OUT = 6;//外面移动
    private final int EDGE_NONE = 7;//外面移动

    public int currentEdge = EDGE_NONE;

    protected float oriRationWH = 0;//原始宽高比率

    protected Drawable mDrawable;//原图
    protected FloatDrawable mFloatDrawable;//浮层

    protected Rect mDrawableSrc = new Rect();
    protected Rect mDrawableDst = new Rect();
    protected Rect mDrawableFloat = new Rect();//浮层选择框，就是头像选择框

    protected boolean isFrist = true;

    private boolean isTouchInSquare = true;

    protected Context mContext;

    private int viewHegiht;

    public CropImageView(Context context) {
        super(context);
        init(context);
    }

    public CropImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public CropImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);

    }

    private void init(Context context) {
        this.mContext = context;
        try {
            if (android.os.Build.VERSION.SDK_INT >= 11) {
                this.setLayerType(LAYER_TYPE_SOFTWARE, null);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        mFloatDrawable = new FloatDrawable(context);
    }

    public void setDrawable(Drawable mDrawable, int cropWidth, int cropHeight, int viewHegiht) {
        this.mDrawable = mDrawable;
        this.cropWidth = cropWidth;
        this.cropHeight = cropHeight;
        this.isFrist = true;

        this.viewHegiht = viewHegiht;

        invalidate();
    }

    boolean isSearch = true;


    private int mY;

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                oldX = event.getX();
                oldY = event.getY();
                currentEdge = getTouchEdge((int) oldX, (int) oldY);
                isTouchInSquare = mDrawableFloat.contains((int) event.getX(), (int) event.getY());
                break;

            case MotionEvent.ACTION_UP:

                if (isSearch) {
                    getCropImage();
                } else {

                    if (mDrawableDst.top >= 0) {
                        mDrawableDst.offset(0, -mDrawableDst.top);
                        mDrawableFloat.offset(0, -mDrawableDst.top);

                        invalidate();
                    }

                    if (mDrawableDst.bottom < viewHegiht) {
                        mDrawableDst.offset(0, viewHegiht - mDrawableDst.bottom);
                        mDrawableFloat.offset(0, viewHegiht - mDrawableDst.bottom);

                        invalidate();
                    }
                }

                break;

            case MotionEvent.ACTION_MOVE:
                int dx = (int) (event.getX() - oldX);
                int dy = (int) (event.getY() - oldY);

                oldX = event.getX();
                oldY = event.getY();

                if (!(dx == 0 && dy == 0)) {
//                    System.out.println(currentEdge);
                    switch (currentEdge) {
                        case EDGE_LT:
                            mDrawableFloat.set(mDrawableFloat.left + dx, mDrawableFloat.top + dy, mDrawableFloat.right, mDrawableFloat.bottom);
                            isSearch = true;
                            break;

                        case EDGE_RT:
                            mDrawableFloat.set(mDrawableFloat.left, mDrawableFloat.top + dy, mDrawableFloat.right + dx, mDrawableFloat.bottom);
                            isSearch = true;
                            break;

                        case EDGE_LB:
                            mDrawableFloat.set(mDrawableFloat.left + dx, mDrawableFloat.top, mDrawableFloat.right, mDrawableFloat.bottom + dy);
                            isSearch = true;
                            break;

                        case EDGE_RB:
                            mDrawableFloat.set(mDrawableFloat.left, mDrawableFloat.top, mDrawableFloat.right + dx, mDrawableFloat.bottom + dy);
                            isSearch = true;
                            break;

                        case EDGE_MOVE_IN:
//                            System.out.println("isTouchInSquare : " + isTouchInSquare);
                            if (isTouchInSquare) {
//                                System.out.println((int) dx + "    " + (int) dy);
                                mDrawableFloat.offset((int) dx, (int) dy);
                            }
                            isSearch = true;
                            break;

                        // 移动图片
                        case EDGE_MOVE_OUT:

//                            System.out.println("dy : " + (int) dy + "  mDrawableDst.top : " + mDrawableDst.top + "  mDrawableDst.bottom : " + mDrawableDst.bottom);

                            if ((int) dy > 0) {

                                if (mDrawableDst.top <= 0) {
                                    mDrawableDst.offset(0, (int) dy);
                                    mDrawableFloat.offset(0, (int) dy);
                                }

                            } else {

                                if (mDrawableDst.bottom > viewHegiht) {
                                    mDrawableDst.offset(0, (int) dy);
                                    mDrawableFloat.offset(0, (int) dy);
                                }
                            }

                            isSearch = false;

                            break;
                    }

                    mDrawableFloat.sort();
                    invalidate();
                }
                break;
        }

        return true;
    }

    public int getTouchEdge(int eventX, int eventY) {
        if (mFloatDrawable.getBounds().left <= eventX && eventX < (mFloatDrawable.getBounds().left + mFloatDrawable.getCirleWidth())
                && mFloatDrawable.getBounds().top <= eventY && eventY < (mFloatDrawable.getBounds().top + mFloatDrawable.getCirleHeight())) {
            return EDGE_LT;//左上
        } else if ((mFloatDrawable.getBounds().right - mFloatDrawable.getCirleWidth()) <= eventX && eventX < mFloatDrawable.getBounds().right
                && mFloatDrawable.getBounds().top <= eventY && eventY < (mFloatDrawable.getBounds().top + mFloatDrawable.getCirleHeight())) {
            return EDGE_RT;//右上
        } else if (mFloatDrawable.getBounds().left <= eventX && eventX < (mFloatDrawable.getBounds().left + mFloatDrawable.getCirleWidth())
                && (mFloatDrawable.getBounds().bottom - mFloatDrawable.getCirleHeight()) <= eventY && eventY < mFloatDrawable.getBounds().bottom) {
            return EDGE_LB;//左下
        } else if ((mFloatDrawable.getBounds().right - mFloatDrawable.getCirleWidth()) <= eventX && eventX < mFloatDrawable.getBounds().right
                && (mFloatDrawable.getBounds().bottom - mFloatDrawable.getCirleHeight()) <= eventY && eventY < mFloatDrawable.getBounds().bottom) {
            return EDGE_RB;//右下
        } else if (mFloatDrawable.getBounds().contains(eventX, eventY)) {
            return EDGE_MOVE_IN;//里面移动
        }
        return EDGE_MOVE_OUT;
    }

    @Override
    protected void onDraw(Canvas canvas) {
//		super.onDraw(canvas);
        if (mDrawable == null) {
            return; // couldn't resolve the URI
        }

        if (mDrawable.getIntrinsicWidth() == 0 || mDrawable.getIntrinsicHeight() == 0) {
            return;     // nothing to draw (empty bounds)
        }

        configureBounds();

        mDrawable.draw(canvas);
        canvas.save();
        canvas.clipRect(mDrawableFloat, Region.Op.DIFFERENCE);
        canvas.drawColor(Color.parseColor("#55000000"));
        canvas.restore();
        mFloatDrawable.draw(canvas);
    }


    protected void configureBounds() {
        if (isFrist) {
            oriRationWH = ((float) mDrawable.getIntrinsicWidth()) / ((float) mDrawable.getIntrinsicHeight());

            final float scale = mContext.getResources().getDisplayMetrics().density;
            int w = Math.min(getWidth(), (int) (mDrawable.getIntrinsicWidth() * scale + 0.5f));
            int h = (int) (w / oriRationWH);

//            int left = (getWidth() - w) / 2;
//            int top = (getHeight() - h) / 2;
            int left = 0;
            int top = 0;
            int right = left + w;
            int bottom = top + h;

            mDrawableSrc.set(left, top, right, bottom);
            mDrawableDst.set(mDrawableSrc);

            int floatWidth = dipTopx(mContext, cropWidth);
            int floatHeight = dipTopx(mContext, cropHeight);

            if (floatWidth > getWidth()) {
                floatWidth = getWidth();
                floatHeight = cropHeight * floatWidth / cropWidth;
            }

            if (floatHeight > getHeight()) {
                floatHeight = getHeight();
                floatWidth = cropWidth * floatHeight / cropHeight;
            }

            int floatLeft = (getWidth() - floatWidth) / 2;
            int floatTop = (getHeight() - floatHeight) / 2;
            mDrawableFloat.set(floatLeft, floatTop, floatLeft + floatWidth, floatTop + floatHeight);

            isFrist = false;
        }

        mDrawable.setBounds(mDrawableDst);
        mFloatDrawable.setBounds(mDrawableFloat);
    }

    protected void checkBounds() {
        int newLeft = mDrawableFloat.left;
        int newTop = mDrawableFloat.top;

        boolean isChange = false;
        if (mDrawableFloat.left < getLeft()) {
            newLeft = getLeft();
            isChange = true;
        }

        if (mDrawableFloat.top < getTop()) {
            newTop = getTop();
            isChange = true;
        }

        if (mDrawableFloat.right > getRight()) {
            newLeft = getRight() - mDrawableFloat.width();
            isChange = true;
        }

        if (mDrawableFloat.bottom > getBottom()) {
            newTop = getBottom() - mDrawableFloat.height();
            isChange = true;
        }

        mDrawableFloat.offsetTo(newLeft, newTop);
        if (isChange) {
            invalidate();
        }
    }

    public Bitmap getCropImage() {

//        System.out.println(getWidth() + "   " + getHeight());

        Bitmap tmpBitmap = Bitmap.createBitmap(getWidth(), getHeight(), Config.RGB_565);
        Canvas canvas = new Canvas(tmpBitmap);
        mDrawable.draw(canvas);

        Matrix matrix = new Matrix();
        float scale = (float) (mDrawableSrc.width()) / (float) (mDrawableDst.width());
        matrix.postScale(scale, scale);

//        System.out.println(mDrawableDst.left + "  " + mDrawableDst.top + "  " + mDrawableDst.width() + "  " + mDrawableDst.height());
//        System.out.println(mDrawableFloat.left + "  " + mDrawableFloat.top + "  " + mDrawableFloat.width() + "  " + mDrawableFloat.height());

        if (mDrawableFloat.top > 0 && mDrawableFloat.top + mDrawableFloat.height() <= tmpBitmap.getHeight() && mDrawableFloat.left + mDrawableFloat.width() <= tmpBitmap.getWidth()) {

//             y + height must be <= bitmap.height()
//            x + width must be <= bitmap.width()
//            createBitmap(Bitmap source, int x, int y, int width, int height,Matrix m, boolean filter)
            Bitmap ret = Bitmap.createBitmap(tmpBitmap, mDrawableFloat.left, mDrawableFloat.top, mDrawableFloat.width(), mDrawableFloat.height(), matrix, true);
            tmpBitmap.recycle();
            tmpBitmap = null;

//            System.out.println(mDrawableFloat.left + "  " + mDrawableFloat.top + "  " + mDrawableFloat.width() + "  " + mDrawableFloat.height());

            int m_left = Math.abs(mDrawableFloat.left);
            int m_top = Math.abs(mDrawableDst.top) + Math.abs(mDrawableFloat.top);
            int m_width = mDrawableFloat.width();
            int m_height = mDrawableFloat.height();

            System.out.println("m_left : " + m_left + "  m_top : " + m_top + "  m_width : " + m_width + "  m_height : " + m_height);


            return ret;
        }

        return null;
    }

    public int dipTopx(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

//    @Override
//    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
//        setMeasuredDimension(1020, 2000);
//    }
}
