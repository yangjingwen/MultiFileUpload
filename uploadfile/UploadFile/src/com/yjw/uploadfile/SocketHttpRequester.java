package com.yjw.uploadfile;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.URL;
import java.util.List;
import java.util.Map;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

/**
 * �ϴ��ļ���������
 * 
 * @author yjw
 *
 */
public class SocketHttpRequester {
    /**
     * ֱ��ͨ��HTTPЭ���ύ���ݵ�������,ʵ����������ύ����:
     *   <FORM METHOD=POST ACTION="http://yjc-pc:8080/WebAppProject/main.do" enctype="multipart/form-data">
            <INPUT TYPE="text" NAME="name">
            <INPUT TYPE="text" NAME="id">
            <input type="file" name="imagefile"/>
            <input type="file" name="zip"/>
         </FORM>
     * @param path �ϴ�·��(ע������ʹ��localhost��127.0.0.1������·�����ԣ���Ϊ����ָ���ֻ�ģ�����������ʹ��http://www.iteye.cn��http://192.168.1.101:8083������·������)
     * @param params ������� keyΪ������,valueΪ����ֵ
     * @param file �ϴ��ļ�
     */
	public static String post(String path, Map<String, String> params, FormFile[] files,Handler handler) throws Exception{     
        try {
		final String BOUNDARY = "---------------------------7da2137580612"; //���ݷָ���
        final String endline = "--" + BOUNDARY + "--\r\n";//���ݽ�����־
        int fileDataLength = 0;	//�ļ�����
        for(FormFile uploadFile : files){//�õ��ļ��������ݵ��ܳ���
            StringBuilder fileExplain = new StringBuilder();
             fileExplain.append("--");
             fileExplain.append(BOUNDARY);
             fileExplain.append("\r\n");
             fileExplain.append("Content-Disposition: form-data;name=\""+ uploadFile.getParameterName()+"\";filename=\""+ uploadFile.getFilname() + "\"\r\n");
             fileExplain.append("Content-Type: "+ uploadFile.getContentType()+"\r\n\r\n");
             fileExplain.append("\r\n");
             fileDataLength += fileExplain.length();
            if(uploadFile.getInStream()!=null){
            	if (uploadFile.getFile() != null) {
            		fileDataLength += uploadFile.getFile().length();
            	} else {
            		fileDataLength += uploadFile.getFileSize();
            	}
             }else{
                 fileDataLength += uploadFile.getData().length;
             }
        }
        StringBuilder textEntity = new StringBuilder();
        for (Map.Entry<String, String> entry : params.entrySet()) {//�����ı����Ͳ�����ʵ������
            textEntity.append("--");
            textEntity.append(BOUNDARY);
            textEntity.append("\r\n");
            textEntity.append("Content-Disposition: form-data; name=\""+ entry.getKey() + "\"\r\n\r\n");
            textEntity.append(entry.getValue());
            textEntity.append("\r\n");
        }
        //���㴫�����������ʵ�������ܳ���
        int dataLength = textEntity.toString() .getBytes().length + fileDataLength +  endline.getBytes().length;
        
        URL url = new URL(path);
        int port = url.getPort()==-1 ? 80 : url.getPort();
        Socket socket = new Socket(InetAddress.getByName(url.getHost()), port);   
        Log.i("hbgz", "socket connected is " + socket.isConnected());
        OutputStream outStream = socket.getOutputStream();
        //�������HTTP����ͷ�ķ���
        String requestmethod = "POST "+ url.getPath()+" HTTP/1.1\r\n";
        outStream.write(requestmethod.getBytes());
        String accept = "Accept: image/gif, image/jpeg, image/pjpeg, image/pjpeg, application/x-shockwave-flash, application/xaml+xml, application/vnd.ms-xpsdocument, application/x-ms-xbap, application/x-ms-application, application/vnd.ms-excel, application/vnd.ms-powerpoint, application/msword, */*\r\n";
        outStream.write(accept.getBytes());
        String language = "Accept-Language: zh-CN\r\n";
        outStream.write(language.getBytes());
        String contenttype = "Content-Type: multipart/form-data; boundary="+ BOUNDARY+ "\r\n";
        outStream.write(contenttype.getBytes());
        String contentlength = "Content-Length: "+ dataLength + "\r\n";
        outStream.write(contentlength.getBytes());
        String alive = "Connection: Keep-Alive\r\n";
        outStream.write(alive.getBytes());
        String host = "Host: "+ url.getHost() +":"+ port +"\r\n";
        outStream.write(host.getBytes());
        //д��HTTP����ͷ�����HTTPЭ����дһ���س�����
        outStream.write("\r\n".getBytes());
        //�������ı����͵�ʵ�����ݷ��ͳ���
        outStream.write(textEntity.toString().getBytes());
        int lenTotal = 0;
        //�������ļ����͵�ʵ�����ݷ��ͳ���
        for(FormFile uploadFile : files){
            StringBuilder fileEntity = new StringBuilder();
             fileEntity.append("--");
             fileEntity.append(BOUNDARY);
             fileEntity.append("\r\n");
             fileEntity.append("Content-Disposition: form-data;name=\""+ uploadFile.getParameterName()+"\";filename=\""+ uploadFile.getFilname() + "\"\r\n");
             fileEntity.append("Content-Type: "+ uploadFile.getContentType()+"\r\n\r\n");
             outStream.write(fileEntity.toString().getBytes());
             InputStream is = uploadFile.getInStream();
             if(is!=null) {
                 byte[] buffer = new byte[1024];
                 int len = 0;
	                 while((len = is.read(buffer, 0, 1024))!=-1){
	                     outStream.write(buffer, 0, len);
	                     lenTotal += len;	//ÿ���ϴ��ĳ���
	                     Message message = new Message();
	                     message.arg1 = 11;
	                     message.obj = lenTotal;
	                     handler.sendMessage(message);
	                 }
	                 is.close();   
             }else{
                 outStream.write(uploadFile.getData(), 0, uploadFile.getData().length);
             }
             outStream.write("\r\n".getBytes());
        }
        //���淢�����ݽ�����־����ʾ�����Ѿ�����
        outStream.write(endline.getBytes());
        
        BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        String str = "";
        boolean requestCodeSuccess = false;
        boolean uploadSuccess = false;
        int indexResult = 1;
        while((str = reader.readLine()) != null) {
        	Log.d("yjw", "upload--->str=" + str);
        	if (indexResult == 1) {
        		if (str.indexOf("200") > 0) {
            		requestCodeSuccess = true;
            	}
        	}
        	if (indexResult == 6) {
        		if ("true".equals(str.trim())) {
            		uploadSuccess = true;
            	}
        	}
        	
        	if (requestCodeSuccess && uploadSuccess) {
        		
        		outStream.flush();
    	        if(null != socket && socket.isConnected())
    	        {
	    	        socket.shutdownInput();
	    	        socket.shutdownOutput();
    	        }
    	        outStream.close();
    	        reader.close();
    	        socket.close();
        		return str.trim();
        	} else if (indexResult == 6) {
        		outStream.flush();
    	        if(null != socket && socket.isConnected())
    	        {
	    	        socket.shutdownInput();
	    	        socket.shutdownOutput();
    	        }
    	        outStream.close();
    	        reader.close();
    	        socket.close();
        		return str.trim();
        	}
        	++indexResult;
        }
        outStream.flush();
       
        if(null != socket && socket.isConnected())
        {
	        socket.shutdownInput();
	        socket.shutdownOutput();
        }
        outStream.close();
        reader.close();
        socket.close();
        return null;
        } catch(Exception e) {
        	e.printStackTrace();
        	 return null;
        }
    }
    
    /**
     * �ύ���ݵ�������
     * @param path �ϴ�·��(ע������ʹ��localhost��127.0.0.1������·�����ԣ���Ϊ����ָ���ֻ�ģ�����������ʹ��http://www.itcast.cn��http://192.168.1.10:8080������·������)
     * @param params ������� keyΪ������,valueΪ����ֵ
     * @param file �ϴ��ļ�
     */
    public static String post(String path, Map<String, String> params, FormFile file,Handler handler) throws Exception{
       return post(path, params, new FormFile[]{file},handler);
    }
    
}