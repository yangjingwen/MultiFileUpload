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
 * �ļ��ϴ���֧�ֵ��ļ��������ļ��ϴ�
 * uploadTask.execute(filesArray) filesArray������Σ�Ϊ��Ҫ�ϴ����ļ�·�����ַ�����������
 * ����:
 * 	AsyncUploadTask uploadTask = new AsyncUploadTask(MainActivity.this);
 *	���������������˽ӿ��������
	Hashtable<String, Object> param = new Hashtable<String, Object>();
	param.put("name", "zhangsan");
	param.put("age", "18");
	�����������ֵ��Task
	uploadTask.setParamMap(param);
	��ʼ�ϴ���filesArray�ϴ�·���ַ�������
	���ؽ��resultΪnull����ʾ�ϴ�ʧ��
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
	 * �����������
	 * @param paramMap �������
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
			progressTxt.setText("�ϴ�ʧ�ܣ�������");
		}
//		Log.d(TAG, "-->onPostExecute result=" + result);
	}

	@Override
	protected void onProgressUpdate(Long... values) {
		super.onProgressUpdate(values);
		updateProgressValue(values[0]);
	}
	
	//�����������
	private HttpEntity setParams(String... files) throws UnsupportedEncodingException {
		//MultipartEntity������Ҫhttpmime-4.1.1.jar�ļ���
		MultipartEntity multipartEntity = new MultipartEntity();
		//StringBody���󣬲���
		if (paramMap != null) {
			Set<String> keySet = paramMap.keySet();
			for(String paramName:keySet) {
				Object paramValue = paramMap.get(paramName);
				StringBody param = new StringBody(paramValue.toString());
				
				multipartEntity.addPart(paramName,param);
			}
		}
		//files��Ҫ�ϴ��ļ���ַ
		for (String path:files) {
			//FileBody������Ҫ�ϴ����ļ�
			ContentBody file = new FileBody( new File(path));
			multipartEntity.addPart("file",file);
		}
		//��MultipartEntity����ֵ��HttpPost
		totalSize = multipartEntity.getContentLength();
		progressBar.setMax((int)totalSize);
		//��дHttpEntity,��ȡ�ϴ�����
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
		//HttpClient����
		HttpClient httpClient = new DefaultHttpClient();
		//����POST������ʽ
		//�����ϴ������ַhttp://10.0.2.2:8080/WebAppProject/main.do?method=upload2
		HttpPost httpPost = new HttpPost(UPLOAD_URL);
		HttpEntity httpEntity = setParams(files);
		httpPost.setEntity(httpEntity);
		HttpResponse response = null;
		//ִ�����󣬲����ؽ��HttpResponse
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
		
		progressTxt.setText("�����:"+(int)(100*progress/totalSize) + "%");
		progressBar.setProgress((int)progress);
	}
	
	/**
	 * �����ϴ�������
	 */
	public void cancelProgressDialog() {
		if (progressDialog != null){
			progressDialog.dismiss();
		}
	}
	

}
