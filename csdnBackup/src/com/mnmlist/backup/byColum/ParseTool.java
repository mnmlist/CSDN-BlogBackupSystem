package com.mnmlist.backup.byColum;

import java.util.List;

import javax.management.Attribute;
import javax.management.AttributeList;

import org.htmlparser.Node;
import org.htmlparser.NodeFilter;
import org.htmlparser.Parser;
import org.htmlparser.filters.AndFilter;
import org.htmlparser.filters.HasAttributeFilter;
import org.htmlparser.filters.TagNameFilter;
import org.htmlparser.tags.ImageTag;
import org.htmlparser.tags.LinkTag;
import org.htmlparser.tags.Span;
import org.htmlparser.util.NodeList;
import org.htmlparser.util.ParserException;

/**
 * @author mnmlist@163.com
 * @blog http://blog.csdn.net/mnmlist
 * @version v1.0
 */
public class ParseTool {
	/*
	 * @param url htmlҳ��url
	 * 
	 * @param tagNameFilter tagName of a filter
	 *
	 * @param attributeKey ��ǩ��
	 *
	 * @param attributeValue ��ǩֵ
	 * 
	 * @return ����ĳ�������ӽڵ�,�����ǩ��Ϊblog_list��ֱ�ӷ��ر�ǩ��Ϊblog_list��Ӧ�����н��
	 */
	public static NodeList getNodeList(String url, String tagNameFilter,
			String attributeKey, String attributeValue) {
		Parser parser = ParserInstance.getParserInstance(url);
		NodeFilter andFilter = new AndFilter(new TagNameFilter(tagNameFilter),
				new HasAttributeFilter(attributeKey, attributeValue));
		NodeList list = null;
		try {
			list = parser.extractAllNodesThatMatch(andFilter);
		} catch (ParserException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		NodeList nlist = null;
		if (list.size() > 0)
			if(attributeValue.equals("blog_list"))
			return list;
			nlist = list.elementAt(0).getChildren();
		return nlist;
	}
	/*
	 * @param nlist ���½��
	 */
	public static void parseTitle(NodeList nlist,BlogInfo blogInfo) {
		Node tit = null;
		int count = nlist.size();
		for (int i = 0; i < count; i++) {
			tit = nlist.elementAt(i);
			if (tit instanceof Span) {
				Span span = (Span) tit;
				if (span.getAttribute("class") != null
						&& span.getAttribute("class").equalsIgnoreCase(
								"link_title")) {
					LinkTag link = (LinkTag) span.childAt(0);
					String title = link.getLinkText();
					/* ���ļ����в�������ַ��滻��������ַ� */
					title = title.trim();
					title = title.replaceAll("[\\?/:*|<>\"]","_");
					blogInfo.setArticleTitle(title);
				}
			} else {
				NodeList slist = tit.getChildren();
				if (slist != null && slist.size() > 0) {
					parseTitle(slist,blogInfo);
				}
			}
		}
	}
	/*
	 * @param nlist ר���ĵ���ҳ���
	 */
	public static void parseColumsTitle(NodeList nlist,BlogInfo blogInfo) {
		Node tit = null;
		int count = nlist.size();
		for (int i = 0; i < count; i++) {
			tit = nlist.elementAt(i);
			if (tit instanceof Span) {
				Span span = (Span) tit;
				Node node = span.childAt(0);
				String title = node.getText();
				/* ���ļ����в�������ַ��滻��������ַ� */
				title = title.trim();
				title = title.replaceAll("[\\?/:*|<>\"]", "_");
				title=title.replaceAll("/", "//");
				blogInfo.setArticleTitle(title);
			} else {
				NodeList slist = tit.getChildren();
				if (slist != null && slist.size() > 0) {
					parseTitle(slist,blogInfo);
				}
			}
		}
	}

	/*
	 * @param nlist HTML���ĵ��ӱ�ǩ����
	 * 
	 * @param index ��������ͼƬ�ĸ����Լ���ǰ��ͼƬ��
	 * 
	 * @return ��ǰ��ͼƬ��
	 */
	public static int parseImg(NodeList nlist, int index,BlogInfo blogInfo) {
		String path=blogInfo.getNewArticlePath();
		Node img = null;
		int count = nlist.size();
		for (int i = 0; i < count; i++) {
			img = nlist.elementAt(i);
			if (img instanceof ImageTag) {
				ImageTag imgtag = (ImageTag) img;
				if (!imgtag.isEndTag()) {
					/* ��ͼƬ��URLӳ��ɱ���·�� */
					blogInfo.getImageResourceList().add(new Attribute("" + index,
							new String(imgtag.extractImageLocn().getBytes())));
					imgtag.setImageURL(path + "_files/" + index + ".gif");
					/* ��������·������ */
					index++;
				}
			} else {
				NodeList slist = img.getChildren();
				if (slist != null && slist.size() > 0) {
					index = ParseTool.parseImg(slist, index,blogInfo);
				}
			}
		}
		return index;
	}
	/*
	 * @param nlist HTMLÿ�·ݴ浵���ӱ�ǩ����
	 */
	public static boolean parsePerArticle(NodeList nlist,BlogInfo blogInfo) {
		boolean flag=false;
		int count = nlist.size();
		Node atls=null;
		String string="http://blog.csdn.net";
		for (int i = 0; i < count; i++) {
			atls = nlist.elementAt(i);
			if (atls instanceof LinkTag) {
				LinkTag link = (LinkTag) atls;
				String urlString=link.extractLink();
				String urlText = link.getLinkText().replaceAll("[\\?/:*|<>\"]","_");
				if(urlString.startsWith(string))
				{
					blogInfo.getColumArticleList().add(new Attribute(urlText, urlString));
					flag=true;
					break;
				}
			}else {
				NodeList slist = atls.getChildren();
				if (slist != null && slist.size() > 0) {
					flag=parsePerArticle(slist,blogInfo);
					if(flag)
						break;
				}
			}
		}
		return flag;
	}

	/*
	 * @param nlist HTML��ҳ��ʾ��ǩ���ӱ�ǩ����
	 */
	public static void parsePage(NodeList nlist,BlogInfo blogInfo) {// from parseMonth
		Node pg = null;
		int count = nlist.size();
		for (int i = 0; i < count; i++) {
			pg = nlist.elementAt(i);
			if (pg instanceof LinkTag) {
				LinkTag lt = (LinkTag) pg;
				if (lt.getLinkText().equalsIgnoreCase("��һҳ")) {
					String url = "http://blog.csdn.net" + lt.extractLink();
					NodeList titleList = getNodeList(url, "div", "class", "blog_list");
					int size=titleList.size();
					for(int j=0;j<size;j++)
					{
						parsePerArticle(titleList.elementAt(j).getChildren(),blogInfo);
					}
					NodeList fenYeList = getNodeList(url, "div", "class", "page_nav");
					if (fenYeList != null)
						parsePage(fenYeList,blogInfo);
					else
						break;
				}
			}
		}
	}

	/*
	 * @param filepath ���ش浵��·��
	 * 
	 * @param url ���汾�´浵����ҳ��URL
	 * 
	 * @param articles ���汾�´浵������
	 */
	public static void parseColums(String filepath, String url,
			AttributeList articles,BlogInfo blogInfo) {
		NodeList titleList = getNodeList(url, "div", "class", "blog_list");
		int size=titleList.size();
		for(int i=0;i<size;i++)
		{
			parsePerArticle(titleList.elementAt(i).getChildren(),blogInfo);
		}
		
		NodeList fenYeList = getNodeList(url, "div", "class", "page_nav");
		if (fenYeList != null)
			parsePage(fenYeList,blogInfo);
		/* ��һ�㣬����ᱻ��Ϊ�Ƕ�����Ϊ */
		List<Attribute> li =blogInfo.getColumArticleList().asList();
		for (int i = 0; i < li.size(); i++) {
			String titleName=li.get(i).getName();
			HandleTool.handleHtml(titleName, (String) li.get(i).getValue(),blogInfo);
			try {
				/* ��һ�㣬����ᱻ��Ϊ�Ƕ�����Ϊ */
				Thread.sleep(500);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

}
