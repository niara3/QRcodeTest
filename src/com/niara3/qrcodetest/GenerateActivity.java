package com.niara3.qrcodetest;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
 
import com.google.zxing.BarcodeFormat;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.RGBLuminanceSource;
import com.google.zxing.ReaderException;
import com.google.zxing.Result;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.qrcode.QRCodeWriter;
 
public class GenerateActivity extends FragmentActivity {
 
    private static final int REQUEST_CAMERA = 100;

	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_generate);
        findViewById(R.id.button).setOnClickListener(onClickListener);
    }

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item != null) {
			switch (item.getItemId()) {
			case R.id.action_generate:
				//startActivity(new Intent(this, SampleQrActivity.class));
				startActivityForResult(new Intent(MediaStore.ACTION_IMAGE_CAPTURE), REQUEST_CAMERA);
				return true;
			default:
				break;
			}
		}
		return false;
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
	    if (requestCode == REQUEST_CAMERA && resultCode == RESULT_OK) {
	    	if (data == null) {
	    		return;
	    	}
	        Bitmap bitmap = (Bitmap) data.getExtras().get("data");
	        int width = bitmap.getWidth();
	        int height = bitmap.getHeight();
	        int pixels[] = new int[width * height];

	        bitmap.getPixels(pixels, 0, width, 0, 0, width, height);
	        RGBLuminanceSource source = new RGBLuminanceSource(width, height, pixels);
	        BinaryBitmap bb = new BinaryBitmap(new HybridBinarizer(source));
            MultiFormatReader multiFormatReader = new MultiFormatReader();
            try {
            	Result rawResult = multiFormatReader.decode(bb);
                Toast.makeText(getApplicationContext(), rawResult.getText(), Toast.LENGTH_LONG)
                        .show();
            } catch (ReaderException re) {
                Toast.makeText(getApplicationContext(), "read error: " + re.getMessage(),
                        Toast.LENGTH_LONG).show();
            }

            ImageView imageView = (ImageView) findViewById(R.id.result_view);
            imageView.setImageBitmap(bitmap);
	    }
	}

	private OnClickListener onClickListener = new OnClickListener() {
        @Override
        public void onClick(View view) {
            // EditText から文字を取得
            EditText editText = (EditText) findViewById(R.id.edit_text);
            String contents = editText.getText().toString();
            // 非同期でエンコードする
            Bundle bundle = new Bundle();
            bundle.putString("contents", contents);
            getSupportLoaderManager().initLoader(0, bundle, callbacks);
        }
    };
     
    private LoaderCallbacks<Bitmap> callbacks = new LoaderCallbacks<Bitmap>() {
        @Override
        public Loader<Bitmap> onCreateLoader(int id, Bundle bundle) {
            EncodeTaskLoader loader = new EncodeTaskLoader(
                    getApplicationContext(), bundle.getString("contents"));
            loader.forceLoad();
            return loader;
        }
        @Override
        public void onLoaderReset(Loader<Bitmap> loader) {
        }
        @Override
        public void onLoadFinished(Loader<Bitmap> loader, Bitmap bitmap) {
            getSupportLoaderManager().destroyLoader(0);
            if (bitmap == null) {
                // エンコード失敗
                Toast.makeText(getApplicationContext(), "Error.", Toast.LENGTH_SHORT).show();
            } else {
                // エンコード成功
                ImageView imageView = (ImageView) findViewById(R.id.result_view);
                imageView.setImageBitmap(bitmap);
            }
        }
    };
     
    public static class EncodeTaskLoader extends AsyncTaskLoader<Bitmap> {
        private String mContents;
        public EncodeTaskLoader(Context context, String contents) {
            super(context);
            mContents = contents;
        }
        @Override
        public Bitmap loadInBackground() {
            try {
                // エンコード結果を返す
                return encode(mContents);
            } catch (Exception e) {
                // 何らかのエラーが発生したとき
                return null;
            }
        }
        private Bitmap encode(String contents) throws Exception {
            QRCodeWriter writer = new QRCodeWriter();
            // エンコード
            BitMatrix bm = null;
            bm = writer.encode(mContents, BarcodeFormat.QR_CODE, 100, 100);
            // ピクセルを作る
            int width = bm.getWidth();
            int height = bm.getHeight();
            int[] pixels = new int[width * height];
            // データがあるところだけ黒にする
            for (int y = 0; y < height; y++) {
                int offset = y * width;
                for (int x = 0; x < width; x++) {
                    pixels[offset + x] = bm.get(x, y) ? Color.BLACK : Color.WHITE;
                }
            }
            Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            bitmap.setPixels(pixels, 0, width, 0, 0, width, height);
            return bitmap;
        }
    }
}
