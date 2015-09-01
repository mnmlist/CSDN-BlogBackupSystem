package com.mnmlist.backup.byTag;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URL;
import java.nio.ByteBuffer;
import java.util.Date;

import org.zefer.pd4ml.PD4Constants;
import org.zefer.pd4ml.PD4ML;
import org.zefer.pd4ml.PD4PageMark;

/**
 * @author mnmlist@163.com
 * @blog http://blog.csdn.net/mnmlist
 * @version v1.0
 */
public class FileTool {
	static String proxy_addr = null;
	static int proxy_port = 3128;
	/*
	 * @param url ��ҳ��URL
	 * 
	 * @param type ���ͣ�1Ϊ�ı���0Ϊ������
	 * 
	 * @return ���ݵ��ֽ�����
	 */
	public static byte[] getContent(String url, int type) {
		byte ret[] = null;
		try {
			HttpURLConnection conn = null;
			InputStream urlStream = null;
			URL surl=null;
			try {
				surl = new URL(url);
			} catch (Exception e1) {
				// TODO Auto-generated catch block
				System.out.println("url is not ilegal");
				e1.printStackTrace();
			}
			int j = -1;
			if (proxy_addr != null) {
				InetSocketAddress soA = new InetSocketAddress(
						InetAddress.getByName(proxy_addr), proxy_port);
				Proxy proxy = new Proxy(Proxy.Type.HTTP, soA);
				conn = (HttpURLConnection) surl.openConnection(proxy);
			} else {
				conn = (HttpURLConnection) surl.openConnection();
			}
			/* ���������һ��αװ��Mozilla�����������CSDN��ܾ����� */
			conn.setRequestProperty("User-Agent", "Mozilla/4.0");
			conn.connect();
			try {
				urlStream = conn.getInputStream();
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				System.out.println("file is not found.");
			}
			if (type == 1) {
				StringBuilder sBuilder = new StringBuilder();
				BufferedReader reader = new BufferedReader(
						new InputStreamReader(urlStream, "UTF-8"));
				char[] arr = new char[1024];
				while ((j = reader.read(arr)) != -1)
					sBuilder.append(arr, 0, j);
				ret = sBuilder.toString().getBytes();
			} else {
				/* CSDN�������ͼƬ������ */
				byte imgByte[] = new byte[1024];
				ByteBuffer buffer = ByteBuffer.allocate(5000000);
				while ((j = urlStream.read(imgByte)) != -1) {
					buffer.put(imgByte, 0, j);
				}
				ret = buffer.array();
			}
		} catch (Exception e) {
			e.printStackTrace();
			// ׷�ӳ�����־
		}
		return ret;
	}
	/*
	 * @param path �ļ�·��
	 * 
	 * @param content �ļ����ݵ��ֽ�����
	 * 
	 * @return �ɹ�����ʧ��
	 */
	public static boolean writeFile(String path, byte[] content) {
		try {
			FileOutputStream osw = new FileOutputStream(path);
			if(content!=null&&content.length>0)
				osw.write(content);
			osw.close();
		} catch (Exception e) {
			e.printStackTrace();
			// ׷�ӳ�����־
			return false;
		}
		return true;
	}

	/*
	 * @param path Ŀ¼·��
	 * 
	 * @return �ɹ�����ʧ��
	 */
	public static boolean makeDir(String path) {
		try {
			File fp = new File(path);
			if (!fp.exists()) {
				fp.mkdir();
			}
		} catch (Exception e) {
			e.printStackTrace();
			// ׷�ӳ�����־
			return false;
		}
		return true;
	}
	/**
	 * this method will change a html String to a pdf file named title.pdf
	 * @param contents html String 
	 * @param title the title of the .pdf file
	 * @throws Exception
	 */
	public static void generatePDF(String contents, String title)
			throws Exception {
		File saveFileName = new File(title + ".pdf");
		if(!saveFileName.exists()){
			saveFileName.createNewFile();
		}
		FileOutputStream fos = new FileOutputStream(saveFileName);
		PD4ML pd4ml = new PD4ML();
		//ҳü
		PD4PageMark headerMark = new PD4PageMark();
		headerMark.setAreaHeight(30);
//		headerMark.setInitialPageNumber(1);
//		headerMark.setPagesToSkip(1);
//		headerMark.setTitleAlignment(PD4PageMark.CENTER_ALIGN);
		headerMark.setHtmlTemplate("By mnmlist,"+new Date().toLocaleString()); // autocompute
		pd4ml.setPageHeader(headerMark);
		//ҳ��
		PD4PageMark footerMark = new PD4PageMark();
		footerMark.setAreaHeight(30);
		footerMark.setInitialPageNumber(1);
		footerMark.setPagesToSkip(1);
		footerMark.setPageNumberTemplate( "page: $[page]" );
		footerMark.setPageNumberAlignment( PD4PageMark.RIGHT_ALIGN);
		footerMark.setFont(footerMark.getFont());
		pd4ml.setPageFooter(footerMark);
		//ѡ��ֽ�Ŵ�С���ֿ�Ŀ¼�������
		pd4ml.setPageSize(PD4Constants.A4);
		pd4ml.useTTF("java:fonts", true);
        pd4ml.setDefaultTTFs("simhei", "Georgia", "consolas"); 
		pd4ml.enableDebugInfo();
		pd4ml.render(new StringReader(contents), fos);
		System.out.println("Success!");
	}
}

