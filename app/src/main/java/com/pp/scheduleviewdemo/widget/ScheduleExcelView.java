package com.pp.scheduleviewdemo.widget;


import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.support.annotation.ColorInt;
import android.support.annotation.ColorRes;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Scroller;

import com.pp.scheduleviewdemo.R;
import com.pp.scheduleviewdemo.util.SizeUtil;

import java.util.ArrayList;
import java.util.List;


/**
 * excel view
 *
 * @author qing
 */
public class ScheduleExcelView extends View {

    private static final String TAG = "ExcelView";
    protected final List<ExcelTitle> mColumTitle = new ArrayList<>();
    protected final List<ExcelTitle> mRowTitle = new ArrayList<>();
    private Paint mTitlePaint, mBackPaint, mLinePaint;
    private int[][] mSpans;
    private PorterDuffXfermode xfermode;
    private GestureDetector mScheduleGestureDetector, mScrollGestureDetector;
    private Scroller mScroller;
    private @ColorInt
    int columTitleColor,
            rowTitleColor,
            lineColor,
            columTitleBackColor,
            scheduleColor,
            dividerColor;
    private float columTitleTextSize, rowTitleTextSize;
    private float spanPercent;

    public ScheduleExcelView(Context context) {
        this(context, null);
    }

    public ScheduleExcelView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ScheduleExcelView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        // 关闭硬件加速
        setLayerType(View.LAYER_TYPE_SOFTWARE, null);

        TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.ScheduleExcelView, defStyleAttr, 0);
        columTitleColor = array.getColor(R.styleable.ScheduleExcelView_columTitleTextColor, Color.BLACK);
        rowTitleColor = array.getColor(R.styleable.ScheduleExcelView_rowTitleTextColor, Color.BLACK);
        lineColor = array.getColor(R.styleable.ScheduleExcelView_lineColor, Color.GRAY);
        columTitleBackColor = array.getColor(R.styleable.ScheduleExcelView_columTitleBackground, Color.WHITE);
        scheduleColor = array.getColor(R.styleable.ScheduleExcelView_scheduleColor, Color.RED);
        dividerColor = array.getColor(R.styleable.ScheduleExcelView_dividerColor, Color.BLACK);
        columTitleTextSize = array.getDimensionPixelSize(R.styleable.ScheduleExcelView_columTitleTextSize, 18);
        rowTitleTextSize = array.getDimensionPixelSize(R.styleable.ScheduleExcelView_rowTitleTextSize, 18);
        float percent = array.getFloat(R.styleable.ScheduleExcelView_spanPercent, 0.5f);
        spanPercent = percent < 0 ? 0 : percent > 1 ? 1 : percent;
        array.recycle();
        init();
    }

    private void init() {
        // 初始化画笔
        mTitlePaint = new Paint();
        mTitlePaint.setAntiAlias(true);
        mTitlePaint.setTextAlign(Paint.Align.CENTER);

        mLinePaint = new Paint();
        mLinePaint.setColor(lineColor);
        mLinePaint.setAntiAlias(true);
        mLinePaint.setStrokeWidth(2);

        mBackPaint = new Paint();
        mBackPaint.setColor(columTitleBackColor);
        mBackPaint.setAntiAlias(true);

        setSpanSize(new SimpleSpanSize());

        setDefauleTitle();

        xfermode = new PorterDuffXfermode(PorterDuff.Mode.SRC_IN);

        //　计划手势检测
        mScheduleGestureDetector = new GestureDetector(onScheduleGestureListener);
        // 滑动检测
        mScrollGestureDetector = new GestureDetector(onScrollGestureListener);
        //　粘性滑动
        mScroller = new Scroller(getContext());
    }

    private void setDefauleTitle() {
        clearColumTitle()
                .addColumTitle("S")
                .addColumTitle("M")
                .addColumTitle("T")
                .addColumTitle("W")
                .addColumTitle("T")
                .addColumTitle("F")
                .addColumTitle("S");

        String[] rowTitle = new String[]{"00:00", "00:30", "01:00", "01:30", "02:00", "02:30", "03:00", "03:30", "04:00", "04:30", "05:00", "05:30", "06:00",
                "06:30", "07:00", "07:30", "08:00", "08:30", "09:00", "09:30", "10:00", "10:30", "11:00", "11:30", "12:00"};
        clearRowTitle();
        for (String row : rowTitle) {
            addRowTitle(row);
        }

        commit();
    }

    public void commit() {
        setupSpan();
        invalidate();
    }

    public void setupSpan() {
        mSpans = new int[getSpanColumCount()][getSpanRowCount()];
    }

    public ScheduleExcelView clearColumTitle() {
        mColumTitle.clear();
        return this;
    }

    public ScheduleExcelView clearRowTitle() {
        mRowTitle.clear();
        return this;
    }

    public ExcelTitle newTitle() {
        return new ExcelTitle();
    }

    public void addColumTitle(ExcelTitle title) {
        mColumTitle.add(title);
    }

    public void addRowTitle(ExcelTitle title) {
        mRowTitle.add(title);
    }

    public ScheduleExcelView addColumTitle(String title) {
        mColumTitle.add(new ExcelTitle(title, columTitleColor, columTitleTextSize));
        return this;
    }

    public ScheduleExcelView addRowTitle(String title) {
        mRowTitle.add(new ExcelTitle(title, rowTitleColor, rowTitleTextSize));
        return this;
    }

    /**
     * 要求刷新计划表
     */
    public void callRefrshSchedule(OnScheduleSpanLoading onScheduleSpanLoading) {
        if (null != onScheduleSpanLoading) {
            onScheduleSpanLoading.onLoadStart(mSpans);
        }
        for (int i = 0; i < mSpans.length; i++) {
            for (int j = 0; j < mSpans[i].length; j++) {
                if (null != onScheduleSpanLoading) {
                    setSpanStatus(i, j, onScheduleSpanLoading.getSchedule(i, j));
                }
            }
        }
        if (null != onScheduleSpanLoading) {
            onScheduleSpanLoading.onLoadFinish(mSpans);
        }
        invalidate();
    }

    public int getColumCount() {
        return getSpanColumCount() + 1;
    }

    public int getRowCount() {
        return getSpanRowCount() + 1;
    }

    public int getSpanColumCount() {
        return mColumTitle.size();
    }

    public int getSpanRowCount() {
        return mRowTitle.size();
    }

    private int getDefautSize(int size, int measureSpec) {
        int measureMode = MeasureSpec.getMode(measureSpec);
        int measureSize = MeasureSpec.getSize(measureSpec);
        int result = size;
        switch (measureMode) {
            case MeasureSpec.EXACTLY:
            case MeasureSpec.AT_MOST:
                result = measureSize;
                break;
            case MeasureSpec.UNSPECIFIED:
                result = size > 0 ? size : measureSize / 2;
                break;
            default:
                break;
        }
        return result;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int width = getDefautSize(getSuggestedMinimumWidth(), widthMeasureSpec);
        int height = getDefautSize(getSuggestedMinimumHeight(), heightMeasureSpec);
        setMeasuredDimension(width, height);
    }

    @Override
    protected void onDraw(Canvas canvas) {

        // 绘制标题
        drawRowTitle(canvas);
        // 绘制横线
        drawHorizontaLine(canvas);
        drawVerticalLine(canvas);

        // 绘制单元格区域
        drawSpan(canvas);

        // 在最后绘制,达到悬浮效果
        drawColumTitle(canvas);
    }

    private void drawSpan(Canvas canvas) {
        float spanWidth = mSpanSize.getSpanWidth(this, getColumCount());
        float spanHeight = mSpanSize.getSpanHeight(this, getRowCount());
        float left = getPaddingLeft();
        float top = getPaddingTop();
        RectF tempSpanBound = new RectF(left, top, left + spanWidth, top + spanHeight);
        Paint paint = new Paint();
        paint.setColor(scheduleColor);
        int startRow = -1, endRow = -1;
        RectF startSpanBound = null, endSpanBound = null;
        int scrollerY = getScrollY();
        // 列
        for (int i = 0; i < mSpans.length; i++) {
            tempSpanBound.offset(spanWidth, 0);
            // 行
            for (int j = 0; j < mSpans[i].length + 1; j++) {
                tempSpanBound.offset(0, spanHeight);
                // 单元格是否计划
                boolean schedule = hasSpanSchedule(i, j);

                if (schedule && j < mSpans[i].length) {
                    // 记录这一段计划的开始行
                    if (startRow == -1) {
                        startRow = j;
                        startSpanBound = new RectF(tempSpanBound);
                    }
                } else {
                    // 这一段计划的结束
                    if (startRow != -1) {
                        // 记录这一段计划的结束行
                        endRow = j - 1;
                        endSpanBound = new RectF(tempSpanBound);
                    }
                }
                if (tempSpanBound.bottom - getScrollY() < top + spanHeight) {
                    if (startRow != -1) {
                        startSpanBound.top = top + getScrollY();
                        startSpanBound.bottom = startSpanBound.top + spanHeight;
                    }
                    if (endRow != -1) {
                        startRow = -1;
                        endRow = -1;
                        startSpanBound = null;
                        endSpanBound = null;
                    }
                    continue;
                }

                if (startRow != -1 && endRow != -1) {

                    onDrawSpan(canvas, paint, startRow == endRow, startSpanBound, endSpanBound);
                    // 绘制完当前计划段,重置开始行,重新记录
                    startRow = -1;
                    endRow = -1;
                    startSpanBound = null;
                    endSpanBound = null;
                }
            }
            startRow = -1;
            endRow = -1;
            startSpanBound = null;
            endSpanBound = null;
            tempSpanBound.set(tempSpanBound.left, top, tempSpanBound.right, top + spanHeight);
        }
    }

    private void onDrawSpan(Canvas canvas, Paint paint, boolean same, RectF startSpanBound, RectF endSpanBound) {
        if (same) {
            float scheduleWidth = startSpanBound.width() * spanPercent;
            float left = startSpanBound.left + (startSpanBound.width() - scheduleWidth) * 0.5f;
            float top = startSpanBound.top;
            float right = left + scheduleWidth;
            float bottom = top + startSpanBound.height();
            canvas.drawRoundRect(new RectF(left,top,right,bottom), (right - left) * 0.5f, (bottom - top) * 0.5f, paint);
        } else if (endSpanBound.top - startSpanBound.top < startSpanBound.width() * spanPercent) {
            if (endSpanBound.top - startSpanBound.top > mSpanSize.getSpanHeight(this, getRowCount())) {
                float scheduleWidth = endSpanBound.width() * spanPercent;
                float left = startSpanBound.left + (startSpanBound.width() - scheduleWidth) * 0.5f;
                float bottom = endSpanBound.top;
                float top = bottom - endSpanBound.height();
                float right = left + scheduleWidth;
                canvas.drawRoundRect(new RectF(left,top,right,bottom), (right - left) * 0.5f, (bottom - top) * 0.5f, paint);
            }
        } else {
            float spanWidth = mSpanSize.getSpanWidth(this, getColumCount());
            float strokeWidth = spanWidth * spanPercent;
            paint.setStrokeWidth(strokeWidth);
            paint.setStrokeCap(Paint.Cap.ROUND);

            float startX = startSpanBound.centerX();
            float stopX = startX;
            float startY = startSpanBound.top;
            float stopY = endSpanBound.top;
            canvas.drawLine(startX, startY + strokeWidth * 0.5f, stopX, stopY - strokeWidth * 0.5f, paint);
        }
    }


    /**
     * 绘制纵向横线
     *
     * @param canvas
     */
    private void drawVerticalLine(Canvas canvas) {
        int columCount = getColumCount();
        int rowCount = getRowCount();
        float spanWidth = mSpanSize.getSpanWidth(this, columCount);
        float spanHeight = mSpanSize.getSpanHeight(this, rowCount);
        // 参照点
        float referX = 0 + getPaddingLeft();
        float startX, stopX;
        float startY = getPaddingTop() + spanHeight + getScrollY();
        float stopY = startY + getSpanRowCount() * spanHeight - getScrollY();
        mLinePaint.setColor(lineColor);
        for (int i = 0; i <= columCount; i++) {
            startX = stopX = referX + spanWidth * i;
            canvas.drawLine(startX, startY, stopX, stopY, mLinePaint);
        }

    }

    /**
     * 绘制横向横线
     *
     * @param canvas
     */
    private void drawHorizontaLine(Canvas canvas) {
        int rowCount = getRowCount();
        int columCount = getColumCount();
        float spanHeight = mSpanSize.getSpanHeight(this, rowCount);
        float spanWidth = mSpanSize.getSpanWidth(this, columCount);
        // 参照点
        float referY = 0 + getPaddingTop() + spanHeight;
        float startY, stopY;
        float startX = getPaddingLeft();
        float stopX = startX + columCount * spanWidth;
        for (int i = 0; i <= getSpanRowCount(); i++) {
            startY = stopY = referY + spanHeight * i;
            if (startY - getScrollY() < referY) {
                continue;
            }

            ExcelTitle excelTitle = null;
            if (i < mRowTitle.size()) {
                excelTitle = mRowTitle.get(i);
            }
            if (null == excelTitle || !excelTitle.isDivider()) {
                mLinePaint.setColor(lineColor);
                mLinePaint.setStrokeWidth(2);
                canvas.drawLine(startX, startY, stopX, stopY, mLinePaint);
            } else {
                mLinePaint.setStrokeWidth(3);
                mLinePaint.setColor(excelTitle.getDividerColor() == 0 ? dividerColor : excelTitle.getDividerColor());
                canvas.drawLine(startX, startY, stopX, stopY, mLinePaint);
            }
        }
    }

    private void drawRowTitle(Canvas canvas) {
        float spanHeight = mSpanSize.getSpanHeight(this, getRowCount());
        float startY = getPaddingTop();
        float startX = getPaddingLeft();
        RectF tempBound = new RectF(startX, startY, startX + mSpanSize.getSpanWidth(this, getColumCount()), startY + spanHeight);
        for (int i = 0; i < mRowTitle.size(); i++) {
            tempBound.offset(0, spanHeight);
            if (tempBound.bottom - getScrollY() < startY + spanHeight) {
                continue;
            }
            ExcelTitle excelTitle = this.mRowTitle.get(i);
            String title = excelTitle.getTitle();
            if (!TextUtils.isEmpty(title)) {

                mTitlePaint.setColor(excelTitle.getTextColor() == 0 ? rowTitleColor : excelTitle.getTextColor());
                mTitlePaint.setTextSize(excelTitle.getTextSize() == 0 ? rowTitleTextSize : excelTitle.getTextSize());
                Paint.FontMetrics fontMetrics = mTitlePaint.getFontMetrics();
                float textH = fontMetrics.bottom - fontMetrics.top;
                float r = fontMetrics.bottom / textH;

                float baseline = tempBound.centerY() + (textH * 0.5f - textH * r);
                canvas.drawText(title, tempBound.centerX(), baseline, mTitlePaint);
            }
        }
    }

    private void drawColumTitle(Canvas canvas) {

        float spanWidth = mSpanSize.getSpanWidth(this, getColumCount());
        float spanHeight = mSpanSize.getSpanHeight(this, getRowCount());
        float startX = getPaddingLeft() + spanWidth;
        float startY = getScrollY() + getPaddingTop();
        mBackPaint.setColor(columTitleBackColor);
        // 绘制背景
        canvas.drawRect(getPaddingLeft(), startY, startX + spanWidth * getColumCount(), startY + spanHeight, mBackPaint);
        mLinePaint.setColor(lineColor);
        canvas.drawLine(getPaddingLeft(), startY, startX + spanWidth * getColumCount(), startY, mLinePaint);
        mLinePaint.setColor(dividerColor);
        canvas.drawLine(getPaddingLeft(), startY + spanHeight, startX + spanWidth * getColumCount(), startY + spanHeight, mLinePaint);

        RectF tempBound = new RectF(startX, startY, startX + spanWidth, startY + spanHeight);

        mLinePaint.setColor(lineColor);
        canvas.drawLine(getPaddingLeft(), tempBound.top, getPaddingLeft(), tempBound.bottom, mLinePaint);
        for (int i = 0; i < mColumTitle.size(); i++) {
            ExcelTitle excelTitle = this.mColumTitle.get(i);
            String title = excelTitle.getTitle();
            if (!TextUtils.isEmpty(title)) {

                mTitlePaint.setColor(excelTitle.getTextColor() == 0 ? columTitleColor : excelTitle.getTextColor());
                mTitlePaint.setTextSize(excelTitle.getTextSize() == 0 ? columTitleTextSize : excelTitle.getTextSize());
                Paint.FontMetrics fontMetrics = mTitlePaint.getFontMetrics();
                float textH = fontMetrics.bottom - fontMetrics.top;
                float r = fontMetrics.bottom / textH;

                float baseline = tempBound.centerY() + (textH * 0.5f - textH * r);
                canvas.drawText(title, tempBound.centerX(), baseline, mTitlePaint);
            }
            canvas.drawLine(tempBound.left, tempBound.top, tempBound.left, tempBound.bottom, mLinePaint);
            tempBound.offset(spanWidth, 0);
        }
        canvas.drawLine(tempBound.left, tempBound.top, tempBound.left, tempBound.bottom, mLinePaint);
    }

    /**
     * 根据x坐标获取对应的列(相对于整个计划表)
     *
     * @param x
     * @return
     */
    protected int getColumByX(float x) {
        return (int) (x / mSpanSize.getSpanWidth(ScheduleExcelView.this, getColumCount()));
    }

    /**
     * 根据x坐标获取对应的列(相对于整个计划表)
     *
     * @param y
     * @return
     */
    protected int getRowByY(float y) {
        return (int) (y / mSpanSize.getSpanHeight(ScheduleExcelView.this, getRowCount()));
    }

    /**
     * 列是否合法
     *
     * @param colum
     * @return
     */
    public boolean legalSpanColum(int colum) {
        return 0 <= colum && colum < mSpans.length;
    }

    /**
     * 行/列是否合法
     *
     * @param row
     * @param colum
     * @return
     */
    public boolean legalSpanColumAndRow(int colum, int row) {
        return legalSpanColum(colum) && 0 <= row && row < mSpans[colum].length;
    }

    /**
     * 根据传入位置和状态设置span状态
     *
     * @param row
     * @param colum
     * @param status
     */
    public void setSpanStatus(int colum, int row, boolean status) {
        if (0 <= colum && colum < mSpans.length) {
            if (0 <= row && row < mSpans[colum].length) {
                mSpans[colum][row] = status ? 1 : 0;
            }
        }
    }

    /**
     * 根据传入范围,设置部分span状态
     *
     * @param startColum
     * @param startRow
     * @param endColum
     * @param endRow
     * @param status
     */
    public void setSpanStatus(int startColum, int startRow, int endColum, int endRow, boolean status) {
        for (int i = startColum; i <= endColum; i++) {
            for (int j = startRow; j <= endRow; j++) {
                setSpanStatus(i, j, status);
            }
        }
    }

    /**
     * 根据传入状态,设置整个计划
     *
     * @param status
     */
    public void setSpanStatus(boolean status) {
        for (int i = 0; i < mSpans.length; i++) {
            for (int j = 0; j < mSpans[i].length; j++) {
                setSpanStatus(i, j, status);
            }
        }
    }

    /**
     * 是否存在计划
     *
     * @return
     */
    public boolean hasSchedule() {
        for (int i = 0; i < mSpans.length; i++) {
            if (hasColumSchedule(i)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 判断对应行是否有计划
     *
     * @param row
     * @return
     */
    public boolean hasRowSchedule(int row) {
        for (int i = 0; i < mSpans.length; i++) {
            for (int j = 0; j < mSpans[i].length; j++) {
                if (mSpans[i][row] != 0) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean hasSpanSchedule(int colum, int row) {
        if (0 <= colum && colum < mSpans.length) {
            if (0 <= row && row < mSpans[colum].length) {
                if (mSpans[colum][row] != 0) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 判断传入的列是否有计划
     *
     * @param colum
     * @return
     */
    public boolean hasColumSchedule(int colum) {
        if (0 <= colum && colum < mSpans.length) {
            for (int i = 0; i < mSpans[colum].length; i++) {
                if (mSpans[colum][i] != 0) {
                    return true;
                }
            }
        }
        return false;
    }

    /***
     * 根据传入的参数,自行判断开始和结束值
     * @param downColum
     * @param downRow
     * @param curColum
     * @param curRow
     * @return 返回一个 rect -- left 表示开始列,top 表示开始行,right 表示结束列,bottom 表示结束行
     */
    private Rect getSpanRangeRect(int downColum, int downRow, int curColum, int curRow) {
        int startRow, startColum, endRow, endColum;
        if (downRow > curRow) {
            startRow = curRow;
            endRow = downRow;
        } else {
            startRow = downRow;
            endRow = curRow;
        }

        if (downColum > curColum) {
            startColum = curColum;
            endColum = downColum;
        } else {
            startColum = downColum;
            endColum = curColum;
        }
        return new Rect(startColum, startRow, endColum, endRow);
    }

    private int getFlingMaxY() {
        return (int) (mSpanSize.getSpanHeight(this, getRowCount()) * getRowCount() - (getHeight() - getPaddingTop() - getPaddingBottom()));
    }

    protected void reFreshSchedule() {
        invalidate();
        if (null != onScheduleChangeListener) {
            onScheduleChangeListener.onScheduleChange(mSpans);
        }
    }

    GestureDetector.OnGestureListener onScrollGestureListener = new GestureDetector.SimpleOnGestureListener() {

        @Override
        public boolean onDown(MotionEvent e) {
            // 只处理多指触摸
//            return e.getPointerCount() > 1;
            return mutilPointer;
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            if (getScrollY() < 0 || getScrollY() > getFlingMaxY()) {
                return false;
            }
            scrollBy(0, (int) distanceY);
            return true;
        }
    };

    GestureDetector.OnGestureListener onScheduleGestureListener = new GestureDetector.SimpleOnGestureListener() {

        private boolean downInVerticalTitle;
        private boolean downInHorizontalTitle;
        private int spanDownColum;
        private int spanDownRow;
        private int lastSpanColum;
        private int lastSpanRow;
        private boolean scrollStatus;

        /**
         * 恢复默认
         */
        public void reset() {

            spanDownColum = -1;
            spanDownRow = -1;
            lastSpanColum = -1;
            lastSpanRow = -1;
            scrollStatus = false;
            downInHorizontalTitle = false;
            downInVerticalTitle = false;
        }

        @Override
        public boolean onDown(MotionEvent e) {
            reset();
            //  是否按在第一列(相对于整个表)
            downInVerticalTitle = getColumByX(e.getX() - getPaddingLeft()) == 0;
            // 是否按在第一行(相对于整个表)
            downInHorizontalTitle = getRowByY(e.getY() - getPaddingTop()) == 0;
            // 计算按下的列(相对于计划区)
            lastSpanColum = spanDownColum = getColumByX(getScrollX() + e.getX() - getPaddingLeft()) - 1;
            // 计算按下的行(相对于计划区)
            lastSpanRow = spanDownRow = getRowByY(getScrollY() + e.getY() - getPaddingTop()) - 1;

            // 只处理单指触摸
            return 1 == e.getPointerCount();
        }

        @Override
        public boolean onSingleTapUp(MotionEvent e) {

            // 点击在左上角
            if (downInHorizontalTitle && downInVerticalTitle) {
                setSpanStatus(!hasSchedule());
                reFreshSchedule();
                return true;
                // 点击在时间上
            } else if (downInVerticalTitle) {
                setSpanStatus(0, spanDownRow, mSpans.length, spanDownRow, !hasRowSchedule(spanDownRow));
                reFreshSchedule();
                return true;
                // 点击在 日期上
            } else if (downInHorizontalTitle) {
                setSpanStatus(spanDownColum, 0, spanDownColum, mSpans[spanDownColum].length, !hasColumSchedule(spanDownColum));
                reFreshSchedule();
                return true;
            }

            if (!legalSpanColumAndRow(spanDownColum, spanDownRow)) {
                return false;
            }
            int downStatus = mSpans[spanDownColum][spanDownRow];
            setSpanStatus(spanDownColum, spanDownRow, downStatus == 0);
            reFreshSchedule();
            return true;
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            if (!legalSpanColumAndRow(spanDownColum, spanDownRow)) {
                return false;
            }

            int downStatus = mSpans[spanDownColum][spanDownRow];
            if (!scrollStatus) {
                setSpanStatus(spanDownColum, spanDownRow, downStatus == 0);
                reFreshSchedule();
                scrollStatus = true;
            } else {
                int colum = getColumByX(getScrollX() + e2.getX() - getPaddingLeft()) - 1;
                int row = getRowByY(getScrollY() + e2.getY() - getPaddingTop()) - 1;
                if (!legalSpanColumAndRow(colum, row)) {
                    return false;
                }
                if (lastSpanRow == row && lastSpanColum == colum) {
                    return false;
                }

                // 按下单元格与当前单元格范围
                Rect rangeRect = getSpanRangeRect(spanDownColum, spanDownRow, colum, row);

                setSpanStatus(rangeRect.left, rangeRect.top, rangeRect.right, rangeRect.bottom, downStatus != 0);
                reFreshSchedule();
                lastSpanColum = colum;
                lastSpanRow = row;
            }
            return true;
        }
    };

    /**
     * 是否多指
     */
    boolean mutilPointer = false;

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int action = event.getAction() & MotionEvent.ACTION_MASK;
        switch (action) {
            // 第一个触摸点按下
            case MotionEvent.ACTION_DOWN:
                mutilPointer = false;
                break;
            // 非第一个手指按下
            case MotionEvent.ACTION_POINTER_DOWN:
                mutilPointer = true;
                break;
            default:
                break;
        }
        boolean result;
        int pointerCount = event.getPointerCount();
        if (1 == pointerCount && !mutilPointer) {
            result = mScheduleGestureDetector.onTouchEvent(event);
            // 多指触摸,处理滑动事件
        } else {
            result = mScrollGestureDetector.onTouchEvent(event);
        }

        if (action == MotionEvent.ACTION_UP /*&& !result*/) {
            mutilPointer = false;
            int dy = 0;
            if (getScrollY() < 0) {
                dy = 0 - getScrollY();
            } else if (getScrollY() > getFlingMaxY()) {
                dy = getFlingMaxY() - getScrollY();
            }
            if (dy != 0) {
                mScroller.startScroll(getScrollX(), getScrollY(), 0 - getScrollX(), dy, 300);
                invalidate();
            }
        }
        return result || super.onTouchEvent(event);
    }

    @Override
    public void computeScroll() {
        super.computeScroll();
        if (mScroller.computeScrollOffset()) {
            scrollTo(mScroller.getCurrX(), mScroller.getCurrY());
            postInvalidate();
        }
    }

    public float getExcelWidth() {
        return (getWidth() - getPaddingLeft() - getPaddingRight()) * 1.0f;
    }

    public float getExcelHeight() {
        return (getHeight() - getPaddingTop() - getPaddingBottom()) * 1.0f;
    }

    private SpanSize mSpanSize;

    public void setSpanSize(SpanSize spanSize) {
        this.mSpanSize = spanSize;
        invalidate();
    }

    public void setColumTitleColor(int columTitleColor) {
        this.columTitleColor = columTitleColor;
    }

    public void setRowTitleColor(int rowTitleColor) {
        this.rowTitleColor = rowTitleColor;
    }

    public void setLineColor(int lineColor) {
        this.lineColor = lineColor;
    }

    public void setColumTitleBackColor(int columTitleBackColor) {
        this.columTitleBackColor = columTitleBackColor;
    }

    public void setScheduleColor(int scheduleColor) {
        this.scheduleColor = scheduleColor;
    }

    class SimpleSpanSize implements SpanSize {

        @Override
        public float getSpanWidth(ScheduleExcelView view, int columCount) {
            return view.getExcelWidth() / columCount;
        }

        @Override
        public float getSpanHeight(ScheduleExcelView view, int rowCount) {
            return view.getExcelHeight() / rowCount;
        }
    }

    public interface SpanSize {
        /**
         * 获取单元格宽度
         *
         * @param view
         * @param columCount
         * @return
         */
        float getSpanWidth(ScheduleExcelView view, int columCount);

        /**
         * 获取单元格高度
         *
         * @param view
         * @param rowCount
         * @return
         */
        float getSpanHeight(ScheduleExcelView view, int rowCount);

    }

    private OnScheduleChangeListener onScheduleChangeListener;

    public void setOnScheduleChangeListener(OnScheduleChangeListener onScheduleChangeListener) {
        this.onScheduleChangeListener = onScheduleChangeListener;
    }

    public class ExcelTitle {
        private String title;
        private @ColorInt
        int textColor;
        private float textSize;
        private boolean divider;
        private @ColorInt
        int dividerColor;

        public ExcelTitle() {
        }

        public ExcelTitle(String title, int textColor, float textSize) {
            this.title = title;
            this.textColor = textColor;
            this.textSize = textSize;
        }

        public ExcelTitle setTitle(String title) {
            this.title = title;
            return this;
        }

        public ExcelTitle setTextColor(@ColorRes int textColor) {
            this.textColor = getResources().getColor(textColor);
            return this;
        }

        public ExcelTitle setTextSize(float textSize) {
            this.textSize = SizeUtil.sp2px(getContext(), textSize);
            return this;
        }

        public String getTitle() {
            return title;
        }

        public int getTextColor() {
            return textColor;
        }

        public float getTextSize() {
            return textSize;
        }

        public boolean isDivider() {
            return divider;
        }

        public ExcelTitle setDivider(boolean divider) {
            this.divider = divider;
            return this;
        }

        public int getDividerColor() {
            return dividerColor;
        }

        public ExcelTitle setDividerColor(@ColorRes int dividerColor) {
            this.dividerColor = getResources().getColor(dividerColor);
            return this;
        }
    }

    /**
     * 计划发生变化监听
     */
    public interface OnScheduleChangeListener {
        /**
         * 计划监听
         *
         * @param schedule
         */
        void onScheduleChange(int[][] schedule);
    }

    /**
     * 计划单元格初始化
     */
    public static abstract class OnScheduleSpanLoading {

        /**
         * 开始初始化
         *
         * @param schedule
         */
        public void onLoadStart(int[][] schedule) {
        }

        /**
         * 单元格初始化完成
         *
         * @param schedule
         */
        public void onLoadFinish(int[][] schedule) {
        }

        /**
         * 获取对应位置计划
         *
         * @param colum
         * @param row
         * @return 0表示不计划, 否则计划
         */
        public abstract boolean getSchedule(int colum, int row);
    }
}
