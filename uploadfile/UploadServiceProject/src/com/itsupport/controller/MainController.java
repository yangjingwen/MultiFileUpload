package com.itsupport.controller;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Set;

import javax.print.attribute.standard.Finishings;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.multipart.MultipartRequest;
import org.springframework.web.servlet.ModelAndView;

import com.sun.org.apache.commons.collections.MultiMap;


@Controller
@RequestMapping("/main.do")
public class MainController {

	@RequestMapping(params="method=upload")
	public void uploadFile(MultipartHttpServletRequest request,HttpServletResponse response) {
		System.out.println("--->uploadFile");
		//1、此种方式不需要知道input的name值----下面方法2的“file”值，方法1不需要知道是多少
		MultiValueMap<String,MultipartFile> multiMap = request.getMultiFileMap();
		String name = request.getParameter("name");
		System.out.println("--->name=" + name);
		Set<String> keys = multiMap.keySet();
		for (String key:keys) {
			List<MultipartFile> mutiFile = multiMap.get(key);
				writeFileToDisk(mutiFile);
		}
		
		//2、request.getFiles("file")
		//其中的"file"是<input type="file" name="file" />中的name值
		//或者是android代码中HTTP协议上传的FormFile.parameterName值
		//或者是android代码中的HttpClient中的multipartEntity.addPart("file",file);
//		List<MultipartFile> fileList = request.getFiles("file");
//		writeFileToDisk(fileList);
		
		//跳转的网页，对应success.jsp
		try {
			response.getWriter().print("{name:zhangsan}");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private void writeFileToDisk(List<MultipartFile> fileList) {
		for (MultipartFile file : fileList) {
			InputStream is = null;
			FileOutputStream fos = null;
			try {
			System.out.println("--->"+file.getSize());
				is =  file.getInputStream();
				fos = new FileOutputStream("D:/a/"+System.currentTimeMillis()+".png");
				
				byte[] buffer = new byte[1024];
				int len=0;
				while ((len = is.read(buffer)) != -1) {
					fos.write(buffer, 0, len);
				} 
				fos.flush();
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				try {
					if (fos != null) {
						fos.close();
					}
					if (is != null) {
						is.close();
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
				
			}
		}
	}
}
