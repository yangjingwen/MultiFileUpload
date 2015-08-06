package com.yjw.uploadfile;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.concurrent.ExecutionException;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.ContentBody;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;

import com.yjw.upload.AsyncUploadTask;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
/**
 * 批量文件上传
 * @author yjw
 * 20150804
 * 博客地址：http://blog.csdn.net/muyi_amen
 */
public class MainActivity extends Activity {

	private final static String TAG = "YJW";
	private Button uploadBtn;
	private Button fileBtn;
	private TextView showFileNameTxt;
	private ProgressDialog dialog;
	
	private List<String> filesPath = new ArrayList<String>();
	private List<FormFile> filesList = new ArrayList<FormFile>();
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		uploadBtn = (Button) findViewById(R.id.upload);
		fileBtn = (Button) findViewById(R.id.file);
		showFileNameTxt = (TextView) findViewById(R.id.show_filename);
		dialog = new ProgressDialog(this);
		fileBtn.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent intent = new Intent();
				if (Build.VERSION.SDK_INT < 19) {
					intent.setAction(Intent.ACTION_GET_CONTENT);
				}else{
					//由于Intent.ACTION_OPEN_DOCUMENT的版本是4.4以上的内容
					//如果客户使用的不是4.4以上的版本，因为前面有判断，所以根本不会走else，
					//也就不会出现任何因为这句代码引发的错误
					intent.setAction(Intent.ACTION_OPEN_DOCUMENT);
				}
				intent.setType("image/*");
				startActivityForResult(intent, 1);
			}
		});
		uploadBtn.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if (filesList == null || filesList.isEmpty()){
					Toast.makeText(MainActivity.this, "请先选择上传文件", Toast.LENGTH_SHORT).show();
					return;
				}
//				dialog.show();
				//1、HTTP协议的方式
//				MultiUploadThread uploadThread = new MultiUploadThread(filesList, handler);
//				new Thread(uploadThread).start();
				//2、HttpClient方式
//				new Thread(multiThread).start();
				//3、带进度条的HttpClient上传
				String[] filesArray = new String[filesPath.size()];
				filesPath.toArray(filesArray);
				AsyncUploadTask uploadTask = new AsyncUploadTask(MainActivity.this);
				try {
					Hashtable<String, Object> param = new Hashtable<String, Object>();
					param.put("name", "zhangsan");
					uploadTask.setParamMap(param);
					String result = uploadTask.execute(filesArray).get();
					Log.d(TAG, "-->activity result=" + result);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (ExecutionException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});
	}
	
	Handler handler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			super.handleMessage(msg);
			dialog.dismiss();
			Log.d("yjw", "--->" + msg);
		}
		
	};
	
	@SuppressLint("NewApi")
	public static String getPathByUri(Context cxt,Uri uri){
		
		//判断手机系统是否是4.4或以上的sdk
		final boolean isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;
		//如果是4.4以上的系统并且选择的文件是4.4专有的最近的文件
		if (isKitKat && DocumentsContract.isDocumentUri(cxt, uri)) {
			// 如果是从外部储存卡选择的文件
			if (isExternalStorageDocument(uri)) {
				final String docId = DocumentsContract.getDocumentId(uri);
				final String[] split = docId.split(":");
				final String type = split[0];

				if ("primary".equalsIgnoreCase(type)) {
					return Environment.getExternalStorageDirectory() + "/"
							+ split[1];
				}

			}
			//如果是下载返回的路径
			else if (isDownloadsDocument(uri)) {
				final String id = DocumentsContract.getDocumentId(uri);
				final Uri contentUri = ContentUris.withAppendedId(
						Uri.parse("content://downloads/public_downloads"),
						Long.valueOf(id));

				return getDataColumn(cxt, contentUri, null, null);
			}
			//如果是选择的媒体的文件
			else if (isMediaDocument(uri)) {
				final String docId = DocumentsContract.getDocumentId(uri);
				final String[] split = docId.split(":");
				final String type = split[0];

				Uri contentUri = null;
				if ("image".equals(type)) {  //图片
					contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
				} else if ("video".equals(type)) {  //视频
					contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
				} else if ("audio".equals(type)) {  //音频
					contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
				}

				final String selection = "_id=?";
				final String[] selectionArgs = new String[] { split[1] };

				return getDataColumn(cxt, contentUri, selection,
						selectionArgs);
			}
		}else if ("content".equalsIgnoreCase(uri.getScheme())) {   //如果是低端4.2以下的手机文件uri格式
			if (isGooglePhotosUri(uri))
				return uri.getLastPathSegment();

			return getDataColumn(cxt, uri, null, null);
		}else if ("file".equalsIgnoreCase(uri.getScheme())) {   //如果是通过file转成的uri的格式
			return uri.getPath();
		}

		return null;
	}
	
	/**
	 * Get the value of the data column for this Uri. This is useful for
	 * MediaStore Uris, and other file-based ContentProviders.
	 * 
	 * @param context
	 *            The context.
	 * @param uri
	 *            The Uri to query.
	 * @param selection
	 *            (Optional) Filter used in the query.
	 * @param selectionArgs
	 *            (Optional) Selection arguments used in the query.
	 * @return The value of the _data column, which is typically a file path.
	 */
	public static String getDataColumn(Context context, Uri uri, String selection,
			String[] selectionArgs) {

		Cursor cursor = null;
		final String column = "_data";
		final String[] projection = { column };

		try {
			cursor = context.getContentResolver().query(uri, projection,
					selection, selectionArgs, null);
			if (cursor != null && cursor.moveToFirst()) {
				final int index = cursor.getColumnIndexOrThrow(column);
				return cursor.getString(index);
			}
		} finally {
			if (cursor != null)
				cursor.close();
		}
		return null;
	}
	
	/**
	 * @param uri
	 *            The Uri to check.
	 * @return Whether the Uri authority is ExternalStorageProvider.
	 */
	public static boolean isExternalStorageDocument(Uri uri) {
		return "com.android.externalstorage.documents".equals(uri
				.getAuthority());
	}

	/**
	 * @param uri
	 *            The Uri to check.
	 * @return Whether the Uri authority is DownloadsProvider.
	 */
	public static boolean isDownloadsDocument(Uri uri) {
		return "com.android.providers.downloads.documents".equals(uri
				.getAuthority());
	}

	/**
	 * @param uri
	 *            The Uri to check.
	 * @return Whether the Uri authority is MediaProvider.
	 */
	public static boolean isMediaDocument(Uri uri) {
		return "com.android.providers.media.documents".equals(uri
				.getAuthority());
	}

	/**
	 * @param uri
	 *            The Uri to check.
	 * @return Whether the Uri authority is Google Photos.
	 */
	public static boolean isGooglePhotosUri(Uri uri) {
		return "com.google.android.apps.photos.content".equals(uri
				.getAuthority());
	}
	
	@Override
	protected void onActivityResult(int arg0, int arg1, Intent arg2) {
		// TODO Auto-generated method stub
		super.onActivityResult(arg0, arg1, arg2);
		String path = getPathByUri(this,arg2.getData());
		System.out.println("-->path=" + path);
		showFileNameTxt.setText(showFileNameTxt.getText() + "\n" + path);
		filesPath.add(path);
		File file = new File(path);
		filesList.add(new FormFile(file.getName(), file, System.currentTimeMillis()+".png", "application/octet-stream"));
	}
	
	//网络请求需要开启新的线程，不能在主线程中操作
	Runnable multiThread = new Runnable() {
		
		@Override
		public void run() {
			try {
				multiUploadFile1();
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		public void multiUploadFile1 () throws UnsupportedEncodingException {
			//HttpClient对象
			HttpClient httpClient = new DefaultHttpClient();
			//采用POST的请求方式
			//这是上传服务地址http://10.0.2.2:8080/WebAppProject/main.do?method=upload2
			HttpPost httpPost = new HttpPost("http://10.0.2.2:8080/WebAppProject/main.do?method=upload2");
			//MultipartEntity对象，需要httpmime-4.1.1.jar文件。
			MultipartEntity multipartEntity = new MultipartEntity();
			//StringBody对象，参数
			StringBody param = new StringBody("参数内容");
			multipartEntity.addPart("param1",param);
			//filesPath为List<String>对象，里面存放的是需要上传的文件的地址
			for (String path:filesPath) {
				//FileBody对象，需要上传的文件
				ContentBody file = new FileBody( new File(path));
				multipartEntity.addPart("file",file);
			}
			//将MultipartEntity对象赋值给HttpPost
			httpPost.setEntity(multipartEntity);
			HttpResponse response = null;
			try {
				//执行请求，并返回结果HttpResponse
				response = httpClient.execute(httpPost);
			} catch (ClientProtocolException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			//上传成功后返回
			if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
				System.out.println("-->success");
			} else {
				System.out.println("-->failure");
			}
		}
	
};
	
	

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
}
