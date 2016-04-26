package com.example.sumit.memefy;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Random;

public class MainActivity extends AppCompatActivity {

    int SELECT_FILE = 1;
    Button genMemeButton;
    EditText topEditText;
    EditText bottomEditText;
    ImageView memeImage;
    Bitmap bm;
    Bitmap bm_original;
    Bitmap mutableBitmap;
    Bitmap savableBm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        memeImage = (ImageView)findViewById(R.id.meme_image_view);
        topEditText = (EditText)findViewById(R.id.topTextInput);
        bottomEditText = (EditText)findViewById(R.id.bottomTextInput);
        memeImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectImage();
            }
        });
        bm_original = ((BitmapDrawable)memeImage.getDrawable()).getBitmap();
        genMemeButton = (Button)findViewById(R.id.button_generate_meme);
        genMemeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                generateMeme();
            }
        });
    }

    public void generateMeme(){
        String text1 = topEditText.getText().toString();
        String text2 = bottomEditText.getText().toString();
        mutableBitmap = bm_original.copy(Bitmap.Config.ARGB_8888,true);
        Canvas canvas = new Canvas(mutableBitmap);
        Paint paint = new Paint();
        paint.setColor(getResources().getColor(R.color.textColor));
        paint.setTextSize(mutableBitmap.getHeight()/15);
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setShadowLayer(1f,0f,1f,R.color.shadow);
        canvas.drawText(text1,(int) mutableBitmap.getWidth()/2,(int) mutableBitmap.getHeight()/6,paint);
        canvas.drawText(text2,(int) mutableBitmap.getWidth()/2,(int) mutableBitmap.getHeight()*5/6,paint);
        memeImage.setImageBitmap(mutableBitmap);
        savableBm=mutableBitmap;
        mutableBitmap = bm_original.copy(Bitmap.Config.ARGB_8888,true);

    }

    public void selectImage(){
        final CharSequence[] items = { "Save Image", "Choose from Library",
                "Cancel" };

        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle("Options");
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
                if (items[item].equals("Save Image")) {
                    Toast.makeText(MainActivity.this, "Saving...", Toast.LENGTH_SHORT).show();
                    SaveImage(savableBm);
                } else if (items[item].equals("Choose from Library")) {
                    Intent intent = new Intent(
                            Intent.ACTION_PICK,
                            android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    intent.setType("image/*");
                    startActivityForResult(
                            Intent.createChooser(intent, "Select File"),
                            SELECT_FILE);
                } else if (items[item].equals("Cancel")) {
                    dialog.dismiss();
                }
            }
        });
        builder.show();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == SELECT_FILE)
                onSelectFromGalleryResult(data);
        }
    }


    @SuppressWarnings("deprecation")
    private void onSelectFromGalleryResult(Intent data) {
        Uri selectedImageUri = data.getData();
        String[] projection = { MediaStore.MediaColumns.DATA };
        Cursor cursor = managedQuery(selectedImageUri, projection, null, null,
                null);
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA);
        cursor.moveToFirst();

        String selectedImagePath = cursor.getString(column_index);

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(selectedImagePath, options);
        final int REQUIRED_SIZE = memeImage.getWidth();
        int scale = 1;
        while (options.outWidth / scale / 2 >= REQUIRED_SIZE
                && options.outHeight / scale / 2 >= REQUIRED_SIZE)
            scale *= 2;
        options.inSampleSize = scale;
        options.inJustDecodeBounds = false;
        bm = BitmapFactory.decodeFile(selectedImagePath, options);
        memeImage.setImageBitmap(bm);
        bm_original = ((BitmapDrawable)memeImage.getDrawable()).getBitmap();
    }

    private void SaveImage(Bitmap finalBitmap) {

        String root = Environment.getExternalStorageDirectory().toString();
        File myDir = new File(root + "/saved_images");
        myDir.mkdirs();
        Random generator = new Random();
        int n = 10000;
        n = generator.nextInt(n);
        String fname = "Image-"+ n +".jpg";
        File file = new File (myDir, fname);
        if (file.exists ()) file.delete ();
        try {
            FileOutputStream out = new FileOutputStream(file);
            finalBitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);
            out.flush();
            out.close();

        } catch (Exception e) {
            e.printStackTrace();
        }

        Intent mediaScanIntent = new Intent(
                Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        Uri contentUri = Uri.fromFile(myDir);
        mediaScanIntent.setData(contentUri);
        this.sendBroadcast(mediaScanIntent);
    }
}

