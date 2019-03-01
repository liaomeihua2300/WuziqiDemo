package com.imooc.wuziqi;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class WuziqiPanel extends View{
    private int mPanelWidth;
    private float mLineHeigth;
    private int MAX_LINE=10;
    private int MAX_COUNT_IN_LINE=5;
    private Paint mPaint=new Paint();
    private Bitmap mWhitePiece;
    private Bitmap mBlackPiece;
    private float ratioPieceOfLineHeigth = 3*1.0f/4;
    private List<Point> mWhiteArray=new ArrayList<>();
    private List<Point> mBlackArray=new ArrayList<>();
    //白棋先手、当前白棋
    private boolean mIsWhite=true;
    private boolean mIsGameOver;
    private boolean mIsWhiteWinner;
    public WuziqiPanel(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        setBackgroundColor(0x44ff0000);
        init();
    }
    private void init(){
        mPaint.setColor(0x88000000);
        mPaint.setAntiAlias(true);
        mPaint.setDither(true);
        mPaint.setStyle(Paint.Style.STROKE);

        mWhitePiece= BitmapFactory.decodeResource(getResources(),R.drawable.stone_w2);
        mBlackPiece=BitmapFactory.decodeResource(getResources(),R.drawable.stone_b1);
    }
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int widthSize=MeasureSpec.getSize(widthMeasureSpec);
        int widthModel=MeasureSpec.getMode(widthMeasureSpec);
        int heigthSize=MeasureSpec.getSize(heightMeasureSpec);
        int heigthModel=MeasureSpec.getMode(heightMeasureSpec);

        int width=Math.min(widthSize,heigthSize);
        if(widthModel == MeasureSpec.UNSPECIFIED){
            width=heigthSize;
        }else if(heigthModel == MeasureSpec.UNSPECIFIED){
            width=widthSize;
        }
        setMeasuredDimension(width,width);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {   //控件大小改变时调用 ，初始化时会被调用 ，获取控件宽、高
        super.onSizeChanged(w, h, oldw, oldh);
        mPanelWidth=w;
        mLineHeigth=mPanelWidth*1.0f/MAX_LINE;

        int pieceWidth=(int)(mLineHeigth*ratioPieceOfLineHeigth);

        mWhitePiece= Bitmap.createScaledBitmap(mWhitePiece,pieceWidth,pieceWidth,false);
        mBlackPiece= Bitmap.createScaledBitmap(mBlackPiece,pieceWidth,pieceWidth,false);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if(mIsGameOver){
            return false;
        }
        int action=event.getAction();
        if(action == MotionEvent.ACTION_UP){
            int x=(int)event.getX();
            int y =(int)event.getY();
            Point p=getValidePoint(x,y);
            if(mWhiteArray.contains(p) || mBlackArray.contains(p)){
                return false;
            }
            if(mIsWhite){
                mWhiteArray.add(p);
            }else{
                mBlackArray.add(p);
            }
            invalidate();  //重绘
            mIsWhite=!mIsWhite;
        }
        return true;
    }

    private Point getValidePoint(int x, int y) {
        return new Point((int)(x/mLineHeigth),(int)(y/mLineHeigth));
    }
    private void drawPeices(Canvas canvas){
        for(int i =0 ,n=mWhiteArray.size(); i < n ; i ++){
            Point whitePoint=mWhiteArray.get(i);
            canvas.drawBitmap(mWhitePiece,
                    (whitePoint.x+(1-ratioPieceOfLineHeigth)/2)*mLineHeigth,
                    (whitePoint.y+(1-ratioPieceOfLineHeigth)/2)*mLineHeigth,null);
        }
        for(int i =0 ,n=mBlackArray.size(); i <n ;i ++){
            Point blackPoint=mBlackArray.get(i);
            canvas.drawBitmap(mBlackPiece,
                    (blackPoint.x+(1-ratioPieceOfLineHeigth)/2)*mLineHeigth,
                    (blackPoint.y+(1-ratioPieceOfLineHeigth)/2)*mLineHeigth,null);
        }
    }
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        drawBoard(canvas);
        drawPeices(canvas);
        checkGameOver();
    }

    private void checkGameOver() {
       boolean whiteWin= checkFiveInLine(mWhiteArray);
       boolean blackWin= checkFiveInLine(mBlackArray);
       if(whiteWin || blackWin) {
           String text=whiteWin?"白棋胜利":"黑棋胜利";
           Toast.makeText(getContext(),text,Toast.LENGTH_SHORT).show();
           mIsGameOver = true;
           mIsWhiteWinner=whiteWin;
       }
    }

    private boolean checkFiveInLine(List<Point> points) {
        for(Point point: points){
            int x =point.x;
            int y =point.y;
            boolean win= checkHorizontal(x,y,points) ;
            if(win) return true;
            win=checkVertical(x,y,points);
            if(win)return true;
            win=checkLeftDiagonal(x,y,points);
            if(win) return true;
            win=checkRightDiagonal(x,y,points);
            if(win) return true;
        }
        return false;
    }

    /**
     * 判断x、y横向是否有相邻的五个
     * @param x
     * @param y
     * @param points
     * @return
     */
    private boolean checkHorizontal(int x, int y, List<Point> points) {
        int count=1;
        for(int i=1;i < MAX_COUNT_IN_LINE; i++){
            if(points.contains(new Point(x-i,y))){
                count++;
            }else{
                count=1;
                break;
            }
        }
        if(count == MAX_COUNT_IN_LINE) {
            return true;
        }
        for(int i=1;i < MAX_COUNT_IN_LINE; i++){
            if(points.contains(new Point(x+i,y))){
                count++;
            }else{
                break;
            }
        }
        return false;
    }
    /**
     * 判断x、y纵向是否有相邻的五个
     * @param x
     * @param y
     * @param points
     * @return
     */
    private boolean checkVertical(int x, int y, List<Point> points) {
        int count=1;
        for(int i=1;i < MAX_COUNT_IN_LINE; i++){
            if(points.contains(new Point(x,y-i))){
                count++;
            }else{
                count=1;
                break;
            }
        }
        if(count == MAX_COUNT_IN_LINE) {
            return true;
        }
        for(int i=1;i < MAX_COUNT_IN_LINE; i++){
            if(points.contains(new Point(x,y+i))){
                count++;
            }else{
                break;
            }
        }
        return false;
    }

    /**
     * 判断x、y纵向是否有相邻的五个
     * @param x
     * @param y
     * @param points
     * @return
     */
    private boolean checkRightDiagonal(int x, int y, List<Point> points) {
        int count=1;
        for(int i=1;i < MAX_COUNT_IN_LINE; i++){
            if(points.contains(new Point(x-i,y-i))){
                count++;
            }else{
                break;
            }
        }
        if(count == MAX_COUNT_IN_LINE) {
            return true;
        }
        for(int i=1;i < MAX_COUNT_IN_LINE; i++){
            if(points.contains(new Point(x+i,y+i))){
                count++;
            }else{
                break;
            }
        }
        return false;
    }
    /**
     * 判断x、y纵向是否有相邻的五个
     * @param x
     * @param y
     * @param points
     * @return
     */
    private boolean checkLeftDiagonal(int x, int y, List<Point> points) {
        int count=1;
        for(int i=1;i < MAX_COUNT_IN_LINE; i++){
            if(points.contains(new Point(x-i,y+i))){
                count++;
            }else{
                break;
            }
        }
        if(count == MAX_COUNT_IN_LINE) {
            return true;
        }
        for(int i=1;i < MAX_COUNT_IN_LINE; i++){
            if(points.contains(new Point(x+i,y-i))){
                count++;
            }else{
                break;
            }
        }
        return false;
    }

    private void drawBoard(Canvas canvas){
        int w=mPanelWidth;
        float lineHeight=mLineHeigth;
        for(int i =0 ; i <MAX_LINE ; i++){
            int startX=(int)lineHeight/2;
            int endX=(int)(w-lineHeight/2);

            int y = (int)((0.5+i)*lineHeight);
            canvas.drawLine(startX,y,endX,y,mPaint);

        }
        for(int i =0 ; i <MAX_LINE ; i++){
            int startY=(int)lineHeight/2;
            int endY=(int)(w-lineHeight/2);

            int x=(int)((0.5+i)*lineHeight);
            canvas.drawLine(x,startY,x,endY,mPaint);

        }
    }
}
