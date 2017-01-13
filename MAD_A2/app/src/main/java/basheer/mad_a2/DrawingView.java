//Author: Amshar Basheer
//Project Name: MAD_A2
//File Name: DrawingView.java
//Date: 3/6/15
//Description: this file contains the code for the DrawingView class which codes a custom view.
//  This class contains the canvas and deals with actions related to the canvas including
//  drawing the photos, lines, and textual comments.

package basheer.mad_a2;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.View;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.view.MotionEvent;
import android.graphics.PorterDuff;
import android.widget.EditText;



/*
 * Note: this class was borrowed from the tutorial found at the following URL:
 * http://code.tutsplus.com/tutorials/android-sdk-create-a-drawing-app-interface-creation--mobile-19021
 * The last 4 methods were added to the tutorial code.
 * Also the Handler and Runnable and related code dealing with a long press was also added.
 */
public class DrawingView extends View {

    //drawing path
    private Path drawPath;
    //drawing and canvas paint
    private Paint drawPaint, canvasPaint;
    //initial color
    private int paintColor = 0xFF660000;
    //canvas
    private Canvas drawCanvas;
    //canvas bitmap
    private Bitmap canvasBitmap;
    private float touchX;
    private float touchY;
    private String sTextComment;


    public DrawingView(Context context, AttributeSet attrs){
        super(context, attrs);
        setupDrawing();
    }

    private void setupDrawing(){

        drawPath = new Path();
        drawPaint = new Paint();

        drawPaint.setColor(paintColor);

        drawPaint.setAntiAlias(true);
        drawPaint.setStrokeWidth(20);
        drawPaint.setStyle(Paint.Style.STROKE);
        drawPaint.setStrokeJoin(Paint.Join.ROUND);
        drawPaint.setStrokeCap(Paint.Cap.ROUND);

        canvasPaint = new Paint(Paint.DITHER_FLAG);

    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        //view given size

        super.onSizeChanged(w, h, oldw, oldh);

        canvasBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        drawCanvas = new Canvas(canvasBitmap);

    }

    @Override
    protected void onDraw(Canvas canvas) {
        //draw view

        canvas.drawBitmap(canvasBitmap, 0, 0, canvasPaint);
        canvas.drawPath(drawPath, drawPaint);
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        //detect user touch

        touchX = event.getX();
        touchY = event.getY();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (SelfieSketch.rbEmbedText.isChecked())
                {
                    askForTextEmbed();
                }
                else if (SelfieSketch.rbFingerPaint.isChecked())
                {
                    drawPath.moveTo(touchX, touchY);
                }
                break;
            case MotionEvent.ACTION_MOVE:
                if (SelfieSketch.rbFingerPaint.isChecked())
                {
                    drawPath.lineTo(touchX, touchY);
                }
                break;
            case MotionEvent.ACTION_UP:
                drawCanvas.drawPath(drawPath, drawPaint);
                drawPath.reset();
                break;
            default:
                return false;
        }

        invalidate();
        return true;
    }

    //used to clear display
    public void clearCanvas(){

        drawCanvas.drawColor(0, PorterDuff.Mode.CLEAR);
        invalidate();
    }

    //used to display picture
    public void drawPic(Bitmap bitmap, Matrix matrix, Paint paint){

        drawCanvas.drawBitmap(bitmap, matrix, paint);
    }

    //askForTextEmbed() is called when the user touches their finger down on the canvas
    // A dialog will open allowing the user to enter a textual comment to embed in the picture
    //code was borrowed from: http://stackoverflow.com/questions/10903754/input-text-dialog-android
    public void askForTextEmbed(){
        sTextComment = "";

        //bring up dialog prompting user if they would like to add textual comment
        AlertDialog.Builder promptDialog = new AlertDialog.Builder(getContext());
        promptDialog.setTitle("Add Textual Comment");
        promptDialog.setMessage("Add textual comment to photo?");

        // Set up the input
        final EditText input = new EditText(getContext());
        promptDialog.setView(input);

        //listener for if they select "OK"
        promptDialog.setPositiveButton("OK", new DialogInterface.OnClickListener(){
            public void onClick(DialogInterface dialog, int which){
                //user selected to add textual comment

                //get textual comment from EditText into sTextComment string
                sTextComment = input.getText().toString();
                //if not empty then call writeTextOnCanvas with the string as parameter
                if (sTextComment != "") {
                    writeTextOnCanvas(sTextComment);
                }
            }
        });

        //listener for if they select "Cancel"
        promptDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener(){
            public void onClick(DialogInterface dialog, int which){
                dialog.cancel();
            }
        });
        promptDialog.show();

    }


    //the following method is modified code borrowed from:
    // http://stackoverflow.com/questions/11100428/add-text-to-image-in-android-programmatically
    // writeTextOnCanvas (String text) takes in a string as a parameter and draws it on the canvas
    // at the location touched. This method is called from the askForTextEmbed() method
    private void writeTextOnCanvas(String text) {

        //make typeface -- here choosing Arial bold font
        Typeface tf = Typeface.create("Arial", Typeface.BOLD);

        //make paint and set some attributes
        Paint paint = new Paint();
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.BLUE);
        paint.setTypeface(tf);
        paint.setTextAlign(Paint.Align.LEFT);
        paint.setTextSize(25);

        //make a rectangle and find smallest size of rectangle that encloses the text
        Rect textRect = new Rect();
        paint.getTextBounds(text, 0, text.length(), textRect);


        //If the text is bigger than the canvas , reduce the font size
        if(textRect.width() >= (drawCanvas.getWidth() - 4)) {
            //the padding on either sides is considered as 4, so as to appropriately fit in the text
            paint.setTextSize(15);        //Scaling needs to be used for different dpi's
        }

        //draw the text on the canvas and the point touched
        drawCanvas.drawText(text, touchX, touchY, paint);

    }




}
