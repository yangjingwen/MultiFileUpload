package com.yjw.uploadfile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.os.Handler;
import android.os.Message;

/**
 * 优惠管理的批量上传功能，上传到优惠的文件夹下
 * @author yjw
 *
 */
public class MultiUploadThread implements Runnable{
	private FormFile[] formFile;
	private Handler mHandler;

	/**
	 * 
	 * @param formFile
	 * @param mHandler
	 */
	public MultiUploadThread(FormFile[] formFile, Handler mHandler) {
		super();
		this.formFile = formFile;
		this.mHandler = mHandler;
	}
	
	public MultiUploadThread(List<FormFile> formFile, Handler mHandler) {
		super();
		if (formFile == null || formFile.isEmpty()) {
			throw new NullPointerException("param formFile is null or empty");
		}
		FormFile[] tempFile = new FormFile[formFile.size()];
		this.formFile = formFile.toArray(tempFile);
		this.mHandler = mHandler;
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
//		for (FormFile file:formFile) {
			String result = uploadFile(formFile);
			Message msg= mHandler.obtainMessage();
			if (msg != null) {
				msg.obj = result;
				msg.arg1 = 11;
				mHandler.sendMessage(msg);
			}
//		}
	}
	
	/**
     * 上传图片到服务器
     * 
     * @param formFiles FormFile数组。
     */
    public String uploadFile(FormFile[] formFiles) {
        try {
            //请求普通信息
            Map<String, String> params = new HashMap<String, String>();
            params.put("method", "upload");
            //如果是本机使用10.0.2.2
            String result = SocketHttpRequester.post("http://10.0.2.2:8080/WebAppProject/main.do", params, formFiles,mHandler);
            return result;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

}
