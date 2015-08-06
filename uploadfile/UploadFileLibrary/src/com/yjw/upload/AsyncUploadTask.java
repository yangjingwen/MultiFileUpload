package com.yjw.upload;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.Hashtable;
import java.util.Set;

import org.apache.http.HttpEntity;
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

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
/**
 * 文件上传：支持单文件和批量文件上传
 * uploadTask.execute(filesArray) filesArray调用入参，为需要上传的文件路径。字符串数组类型
 * 例子:
 * 	AsyncUploadTask uploadTask = new AsyncUploadTask(MainActivity.this);
 *	请求参数，即服务端接口所需入参
	Hashtable<String, Object> param = new Hashtable<String, Object>();
	param.put("name", "zhangsan");
	param.put("age", "18");
	将请求参数赋值给Task
	uploadTask.setParamMap(param);
	开始上传。filesArray上传路劲字符串数组
	返回结果result为null，表示上传失败
	String result = uploadTask.execute(filesArray).get();
 * @author yang.jingwen
 * 20150804
 */
public class AsyncUploadTask extends AsyncTask<String, Long, String>{

//	private final static String TAG = "YJW";
	private final static String UPLOAD_URL = "http://10.0.2.2:8080/WebAppProject/main.do?method=upload";
	private Context context;
	private ProgressBar progressBar;
	private TextView progressTxt;
	private Dialog progressDialog;
	private Hashtable<String, Object> paramMap;
	
	private long totalSize;
	/**
	 * 设置请求参数
	 * @param paramMap 请求参数
	 */
	public void setParamMap(Hashtable<String, Object> paramMap) {
		this.paramMap = paramMap;
	}

	public AsyncUploadTask(Context context) {
		super();
		this.context = context;
		initProgressBarDialog();
	}

	@Override
	protected String doInBackground(String... params) {
		try {
			return uploadFile(params);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	@Override
	protected void onPreExecute() {
		super.onPreExecute();
		showProgress();
	}

	@Override
	protected void onPostExecute(String result) {
		super.onPostExecute(result);
		if (result == null) {
			progressTxt.setText("上传失败，请重试");
		}
//		Log.d(TAG, "-->onPostExecute result=" + result);
	}

	@Override
	protected void onProgressUpdate(Long... values) {
		super.onProgressUpdate(values);
		updateProgressValue(values[0]);
	}
	
	//设置请求参数
	private HttpEntity setParams(String... files) throws UnsupportedEncodingException {
		//MultipartEntity对象，需要httpmime-4.1.1.jar文件。
		MultipartEntity multipartEntity = new MultipartEntity();
		//StringBody对象，参数
		if (paramMap != null) {
			Set<String> keySet = paramMap.keySet();
			for(String paramName:keySet) {
				Object paramValue = paramMap.get(paramName);
				StringBody param = new StringBody(paramValue.toString());
				
				multipartEntity.addPart(paramName,param);
			}
		}
		//files需要上传文件地址
		for (String path:files) {
			//FileBody对象，需要上传的文件
			ContentBody file = new FileBody( new File(path));
			multipartEntity.addPart("file",file);
		}
		//将MultipartEntity对象赋值给HttpPost
		totalSize = multipartEntity.getContentLength();
		progressBar.setMax((int)totalSize);
		//重写HttpEntity,获取上传进度
		UploadProgressEntity uploadProgressEntity = new UploadProgressEntity(multipartEntity, 
				new UploadProgressEntity.ProgressListener() {
			@Override
			public void transferred(long transferedBytes) {
				publishProgress(transferedBytes);
			}
		});
		
		return uploadProgressEntity;
	}
	
	public String uploadFile(String... files) throws ClientProtocolException, IOException {
		//HttpClient对象
		HttpClient httpClient = new DefaultHttpClient();
		//采用POST的请求方式
		//这是上传服务地址http://10.0.2.2:8080/WebAppProject/main.do?method=upload2
		HttpPost httpPost = new HttpPost(UPLOAD_URL);
		HttpEntity httpEntity = setParams(files);
		httpPost.setEntity(httpEntity);
		HttpResponse response = null;
		//执行请求，并返回结果HttpResponse
		response = httpClient.execute(httpPost);
		
		if (response != null && response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
			try {
//				Log.d(TAG, "-->success");
				InputStream is = response.getEntity().getContent();
				byte[] buffer = new byte[1024];
				int len = 0;
				StringBuffer result = new StringBuffer();
				while((len = is.read(buffer)) != -1) {
					result.append(new String(buffer, 0, len));
				}
				is.close();
				return result.toString();
			} catch (IllegalStateException e) {
				e.printStackTrace();
			}
				catch (IOException e) {
				e.printStackTrace();
			}
		}
//		Log.d(TAG, "-->upload fail");
		return null;
	}
	
	private void initProgressBarDialog() {
		AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(context);
		View view = LayoutInflater.from(context).inflate(R.layout.upload_dialog_main, null);
		progressBar = (ProgressBar) view.findViewById(R.id.upload_progressbar);
		progressTxt = (TextView) view.findViewById(R.id.upload_progress_txt);
		dialogBuilder.setView(view);
		progressDialog = dialogBuilder.create();
	}
	
	private void showProgress() {
		if (progressDialog != null){
			progressDialog.show();
		}
	}
	
	private void updateProgressValue(long progress) {
		
		progressTxt.setText("已完成:"+(int)(100*progress/totalSize) + "%");
		progressBar.setProgress((int)progress);
	}
	
	/**
	 * 隐藏上传进度条
	 */
	public void cancelProgressDialog() {
		if (progressDialog != null){
			progressDialog.dismiss();
		}
	}
	

}
