//Author: Amshar Basheer
//Project Name: MAD_A2
//File Name: SelfieSketch.java
//Date: 3/6/15
//Description: this android app allows users to take multiple photos and navigate between them,
//  sketch finger paintings on the photos, save photos on their device, and add textual comments
//  on the photos.

package basheer.mad_a2;

import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Paint;
import android.widget.Button;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.ArrayList;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.widget.Toast;
import android.widget.RadioButton;


public class SelfieSketch extends ActionBarActivity {

    private final int REQUEST_CODE = 2015;
    private Button btnNext;
    private Button btnPrev;
    private List<Bitmap> lsPicList;
    private int nCurrPic = -1;
    private DrawingView drawView;
    private Matrix picMatrix;
    private final float SCALE_FACTOR = 0.47f;
    static RadioButton rbFingerPaint;
    static RadioButton rbEmbedText;
    private String mCurrentPhotoPath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_selfie_sketch);

        //making reference to next and prev buttons
        btnNext = (Button) findViewById(R.id.btnNext);
        btnPrev = (Button) findViewById(R.id.btnPrev);

        //initially next and prev buttons invisible
        btnNext.setVisibility(View.INVISIBLE);
        btnPrev.setVisibility(View.INVISIBLE);

        //initialize pic list (list of Bitmaps)
        lsPicList = new ArrayList<Bitmap>();

        //making reference to DrawingView object
        drawView = (DrawingView)findViewById(R.id.drawing);

        //initialize matrix
        picMatrix = new Matrix();

        //make references to radio buttons and make embed text selected by default at start of app
        rbEmbedText = (RadioButton) findViewById(R.id.rbEmbedText);
        rbFingerPaint = (RadioButton) findViewById(R.id.rbFingerPaint);
        rbEmbedText.setChecked(true);

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_selfie_sketch, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    //event handler for Take Picture button: calls the method below with REQUEST_CODE
    public void onTakePicBtnClick(View v) {

        takePic(REQUEST_CODE);
    }

    //makes intent and starts camera app
    public void takePic(int actionCode){
        //make Intent
        Intent takePicIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        // Ensure that there's a camera activity to handle the intent
        if (takePicIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            }
            catch (IOException ex) {
                // Error occurred while creating the File

            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                takePicIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(photoFile));

                //call to start camera app
                startActivityForResult(takePicIntent, actionCode);
            }
        }

    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = image.getAbsolutePath();
        return image;
    }


    //automatically called upon return from camera app
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){

        if (requestCode == REQUEST_CODE && resultCode == RESULT_OK){
            //call method to get and display the pic
            setPic();
        }

    }

    //upon return from camera app the above method will call this one if everything ok
    //  this method deals with the pic taken
    private void setPic() {
        // Get the dimensions of the View
       // int targetW = drawView.getWidth();
       // int targetH = drawView.getHeight();

        // Get the dimensions of the bitmap
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(mCurrentPhotoPath, bmOptions);

        //int photoW = bmOptions.outWidth;
        //int photoH = bmOptions.outHeight;

        // Determine how much to scale down the image
       // int scaleFactor = Math.min(photoW/targetW, photoH/targetH);

        // Decode the image file into a Bitmap sized to fill the View
        bmOptions.inJustDecodeBounds = false;
        //setting inSampleSize using previously calculated value of scaleFactor (3) that was done
        //using commented out code above before made canvas smaller so could shrink canvas width
        //to fit previously calculated value (3) -- otherwise would recalc value and still would have
        //empty white space on sides of image on canvas
        bmOptions.inSampleSize = 3;
        bmOptions.inPurgeable = true;

        Bitmap bitmap = BitmapFactory.decodeFile(mCurrentPhotoPath, bmOptions);

        //reset matrix so don't re-do scaling
        picMatrix.reset();

        //need to make sure previous pic is up to date in pic list
        if (nCurrPic >= 0) {
            lsPicList.set(nCurrPic, loadBitmapFromView(drawView));
        }

        //clear display
        drawView.clearCanvas();

        //Setting scale so multiplies height and width by 0.47
        //  because 0.47 most closely approximates scale factor needed to fill DrawingView
        //  width (just using same factor for height so doesn't distort image)
        picMatrix.setScale(SCALE_FACTOR,SCALE_FACTOR);

        drawView.drawPic(bitmap, picMatrix, new Paint());

        //add pic to list
        lsPicList.add(loadBitmapFromView(drawView));

        //set current pic to the most recently added pic that was just displayed (last in list)
        nCurrPic = lsPicList.size() - 1;

        //ensure next button invisible (since at end of list)
        btnNext.setVisibility(View.INVISIBLE);

        //ensure prev button visible as long as have at least 2 pics (i.e. not the first pic taken)
        if (lsPicList.size() > 1){
            btnPrev.setVisibility(View.VISIBLE);
        }
    }

    //event handler for prev button click used in navigating through pics
    public void onPrevBtnClick (View v){

        //check if there is a prev pic in list
        if (lsPicList.get(nCurrPic - 1) != null){
            //before move from current pic re-save it so up to date in pic list
            lsPicList.set(nCurrPic, loadBitmapFromView(drawView));

            //decrement current pic
            --nCurrPic;

            //clear display
            drawView.clearCanvas();

            //reset matrix so don't re-do scaling
            picMatrix.reset();

            //display the new current pic
            drawView.drawPic(lsPicList.get(nCurrPic), picMatrix, new Paint());

            //ensure next button visible
            btnNext.setVisibility(View.VISIBLE);

            //if reached start of list then make prev button invisible
            if (nCurrPic == 0){
                btnPrev.setVisibility(View.INVISIBLE);
            }
        }

    }

    //event handler for next button click used in navigating through pics
    public void onNextBtnClick (View v){

        //check if there is a next pic in list
        if (lsPicList.get(nCurrPic + 1) != null){
            //before move from current pic re-save it so up to date in pic list
            lsPicList.set(nCurrPic, loadBitmapFromView(drawView));

            //increment current pic
            ++nCurrPic;

            //clear display
            drawView.clearCanvas();

            //reset matrix so don't re-do scaling
            picMatrix.reset();

            //display the new current pic
            drawView.drawPic(lsPicList.get(nCurrPic), picMatrix, new Paint());

            //ensure prev button visible
            btnPrev.setVisibility(View.VISIBLE);

            //if reached end of list then make next button invisible
            if (nCurrPic == (lsPicList.size() - 1)){
                btnNext.setVisibility(View.INVISIBLE);
            }
        }

    }

    //event handler for save button click: used to save photo to device gallery
    public void onSaveBtnClick (View v){
        //the following code is modified code borrowed from the drawing app tutorial by Sue Smith
        //http://code.tutsplus.com/tutorials/android-sdk-create-a-drawing-app-essential-functionality--mobile-19328

        //bring up dialog prompting user if they would like to save or not
        AlertDialog.Builder saveDialog = new AlertDialog.Builder(this);
        saveDialog.setTitle("Save Picture");
        saveDialog.setMessage("Save picture to device Gallery?");

        //listener for if they select "Yes"
        saveDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener(){
            public void onClick(DialogInterface dialog, int which){
                //user selected to save photo

                //ensure DrawingCache is enabled on the custom View
                drawView.setDrawingCacheEnabled(true);

                // Create an image file name
                String timeStamp = new SimpleDateFormat( "yyyyMMdd_HHmmss" ). format( new Date());

                //save the image using insertImage method
                String imgSaved = MediaStore.Images.Media.insertImage(
                        getContentResolver(), drawView.getDrawingCache(),
                        timeStamp+".png", "SelfieSketchPhoto");

                //give user feedback based on if successful or not
                if(imgSaved != null){
                    Toast savedToast = Toast.makeText(getApplicationContext(),
                            "Picture saved to Gallery!", Toast.LENGTH_SHORT);
                    savedToast.show();
                    drawView.destroyDrawingCache();
                }
                else{
                    Toast unsavedToast = Toast.makeText(getApplicationContext(),
                            "Oops! Picture could not be saved.", Toast.LENGTH_SHORT);
                    unsavedToast.show();
                }
            }
        });

        //listener for if they select "No"
        saveDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener(){
            public void onClick(DialogInterface dialog, int which){
                dialog.cancel();
            }
        });
        saveDialog.show();

    }

    //Borrowed from stackoverflow -- this method can load a bitmap from a view
    //   I used this method to generate current bitmaps of the custom drawView so pic in pic list
    //   are up to date including finger painting plus pics
    // http://stackoverflow.com/questions/11100428/add-text-to-image-in-android-programmatically
    public Bitmap loadBitmapFromView(View v) {
        Bitmap b = Bitmap.createBitmap(v.getWidth(), v.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(b);
        v.layout(v.getLeft(), v.getTop(), v.getRight(), v.getBottom());
        v.draw(c);
        return b;
    }



}


