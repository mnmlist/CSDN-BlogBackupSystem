package com.mnmlist.backup.byTag;

import java.io.File;
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

import com.mnmlist.backup.lib.*;
/**
 * @author mnmlist@163.com
 * @blog http://blog.csdn.net/mnmlist
 * @version v1.0
 */
/**
 * @author Sting
 *
 */
/**
 * @author Sting
 *
 */
/**
 * @author Sting
 *
 */
/**
 * @author Sting
 *
 */
public class ParseTool
{
	/*
	 * @param url htmlҳ��url
	 * 
	 * @param tagNameFilter tagName of a filter
	 * 
	 * @param attributeKey ��ǩ��
	 * 
	 * @param attributeValue ��ǩֵ
	 * 
	 * @return ����ĳ�������ӽڵ�,�����ǩ��Ϊarticle_title��ֱ�ӷ��ر�ǩ��Ϊarticle_title��Ӧ�����н��
	 */
	public static NodeList getNodeList(String url, String tagNameFilter,
			String attributeKey, String attributeValue)
	{
		Parser parser = ParserInstance.getParserInstance(url);
		NodeFilter andFilter = new AndFilter(new TagNameFilter(tagNameFilter),
				new HasAttributeFilter(attributeKey, attributeValue));
		NodeList list = null;
		try
		{
			list = parser.extractAllNodesThatMatch(andFilter);
		} catch (ParserException e)
		{
			e.printStackTrace();
		}
		NodeList nlist = null;
		//System.out.println(list.size());
		if (list.size() > 0)
		{
			if (attributeValue.equals("article_title"))
				return list;
			nlist = list.elementAt(0).getChildren();
		}
		return nlist;
	}
	
	
	/**
	 * @param url htmlҳ��url
	 * @param tagNameFilter tagName of a filter
	 * @param attributeKey ��ǩ��
	 * @param attributeValue ��ǩֵ
	 * @return �������±�ǩ������ӽڵ�
	 * ������ȡtagName��Ӧ����ַ,�������±�ǩ��ר��ʹ�õ���<div id="panel_Category">,���Ի�
	 * ��ò�ֹһ��Ԫ�أ�������Ҫ�ر�����һ��
	 */
	public static NodeList getNodeLists(String url, String tagNameFilter,
			String attributeKey, String attributeValue)
	{
		Parser parser = ParserInstance.getParserInstance(url);
		NodeFilter andFilter = new AndFilter(new TagNameFilter(tagNameFilter),
				new HasAttributeFilter(attributeKey, attributeValue));
		NodeList list = null;
		try
		{
			list = parser.extractAllNodesThatMatch(andFilter);
		} catch (ParserException e)
		{
			e.printStackTrace();
		}
		NodeList nlist = null;
		if (list.size() > 0)
		{
			nlist = choooseOneNodeList(list);//������ȡtagName��Ӧ����ַ,�������±�ǩ��ר��ʹ�õ���<div id="panel_Category">,���Ի�
			//��ò�ֹһ��Ԫ�أ�������Ҫ�ر�����һ��
		}
		return nlist;
	}
	
	/**
	 * @param list ���ܰ���ר���Ͳ��ͷ������ֽڵ㣬���Ի���Ҫ��ѡ��
	 * @return ĳ�����ͷ������ڵ���ַ
	 */
	public static NodeList choooseOneNodeList(NodeList list)
	{
		NodeList nlist = null;
		int listSize=list.size();
		for(int i=0;i<listSize;i++)
		{
			Node node=list.elementAt(i);
			NodeList childrenList=node.getChildren();
			//����ͨ��node.getChildren()��ȡnode�������ӽڵ�
			//Ȼ������toString()��ӡ�����е��ӽڵ㣬Ȼ��������ķ��������Ӧ�Ľڵ��ֵ
			String string=childrenList.elementAt(1).getFirstChild().getFirstChild().toPlainTextString();
			if(string.equals("���·���"))//���˵�����ר��
				return node.getChildren();
		}
		return nlist;
	}
	/*
	 * @param urlString ĳ���͵�url
	 * 
	 * @param tagString �������µ�ĳ�����
	 * 
	 * @return ĳ����Ӧ��url
	 */
	public static String getUrlFromTag(String urlString, String tagString,
			BlogInfo blogInfo)
	{
		NodeList nList = ParseTool.getNodeLists(urlString, "div", "id",
				"panel_Category");
		NodeFilter andFilter = new AndFilter(new TagNameFilter("ul"),
				new HasAttributeFilter("class", "panel_body"));
		nList = nList.extractAllNodesThatMatch(andFilter);
		nList = nList.elementAt(0).getChildren();
		boolean flag = ParseTool.parseUrl(nList, tagString, blogInfo);
		if (flag)
			return blogInfo.getTagCorrespondedURL();
		return null;
	}

	/*
	 * @param nlist ����Ŀ¼���ӱ�ǩ����
	 * 
	 * @param tag �������µ�ĳ�����
	 */
	public static boolean parseUrl(NodeList nList, String tag, BlogInfo blogInfo)
	{
		int size = nList.size();
		boolean flag = false;
		for (int j = 0; j < size; j++)
		{
			Node atls = nList.elementAt(j);
			if (atls instanceof LinkTag)
			{
				LinkTag link = (LinkTag) atls;
				if (link.getLinkText().equals(tag))
				{
					blogInfo.setTagCorrespondedURL(link.extractLink());// ���tag��Ӧ��url
					flag = true;
					break;
				}
			} else
			{
				NodeList slist = atls.getChildren();
				if (slist != null && slist.size() > 0)
				{
					flag = parseUrl(slist, tag, blogInfo);
					if (flag)
						break;
				}
			}
		}
		return flag;
	}

	/*
	 * @param nlist HTML���ĵ��ӱ�ǩ����
	 * 
	 * @param index ��������ͼƬ�ĸ����Լ���ǰ��ͼƬ��
	 * 
	 * @return ��ǰ��ͼƬ��
	 */
	public static int parseImg(NodeList nlist, int index,
			final BlogInfo blogInfo)
	{
		String path = blogInfo.getNewArticlePath();
		Node img = null;
		int count = nlist.size();
		for (int i = 0; i < count; i++)
		{
			img = nlist.elementAt(i);
			if (img instanceof ImageTag)
			{
				ImageTag imgtag = (ImageTag) img;
				if (!imgtag.isEndTag())
				{
					/* ��ͼƬ��URLӳ��ɱ���·�� */
					blogInfo.getImageResourceList().add(
							new Attribute("" + index, new String(imgtag
									.extractImageLocn().getBytes())));
					imgtag.setImageURL(path + "_files/" + index + ".gif");
					/* ��������·������ */
					index++;
				}
			} else
			{
				NodeList slist = img.getChildren();
				if (slist != null && slist.size() > 0)
				{
					index = ParseTool.parseImg(slist, index, blogInfo);
				}
			}
		}
		return index;
	}

	/*
	 * @param nlist HTMLÿ�·ݴ浵���ӱ�ǩ����
	 * 
	 * @param index ����
	 * 
	 * @return ����
	 */
	public static void parsePerArticle(NodeList nlist, BlogInfo blogInfo)
	{
		Node atl = null;
		int count = nlist.size();
		for (int i = 1; i < count; i += 2)
		{
			atl = nlist.elementAt(i);
			if (atl instanceof Span)
			{
				Span span = (Span) atl;
				if (span.getAttribute("class") != null
						&& span.getAttribute("class").equalsIgnoreCase(
								"link_title"))
				{
					LinkTag link = (LinkTag) span.childAt(0);
					String urlDescripString = link.getLinkText().trim()
							.replaceAll("[\\?/:*|<>\"]", "_");
					blogInfo.getColumArticleList()
							.add(new Attribute(urlDescripString,
									"http://blog.csdn.net" + link.extractLink()));
				}
			} else
			{
				NodeList slist = atl.getChildren();
				if (slist != null && slist.size() > 0)
				{
					parsePerArticle(slist, blogInfo);
				}
			}
		}
	}

	/*
	 * @param nlist HTML��ҳ��ʾ��ǩ���ӱ�ǩ����
	 * 
	 * @param index ����
	 * 
	 * @return ����
	 */
	public static void parsePage(NodeList nlist, BlogInfo blogInfo)
	{// from parseMonth
		Node pg = null;
		int count = nlist.size();
		for (int i = 0; i < count; i++)
		{
			pg = nlist.elementAt(i);
			if (pg instanceof LinkTag)
			{
				LinkTag lt = (LinkTag) pg;
				if (lt.getLinkText().equalsIgnoreCase("��һҳ"))
				{
					String url = "http://blog.csdn.net" + lt.extractLink();
					NodeList titleList = getNodeList(url, "div", "id",
							"article_list");
					parsePerArticle(titleList, blogInfo);
					NodeList fenYeList = getNodeList(url, "div", "id",
							"papelist");
					if (fenYeList != null)
						parsePage(fenYeList, blogInfo);
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
	 * 
	 * @return ��
	 */
	public static void parseColums(String filepath, String url,
			AttributeList articles, BlogInfo blogInfo)
	{
		NodeList titleList = getNodeList(url, "div", "id", "article_list");// list
		int size = titleList.size();
		for (int i = 1; i < size; i += 2)
		{
			NodeList sList = titleList.elementAt(i).getChildren();
			sList = sList.elementAt(1).getChildren();// article title���ӽڵ�
			parsePerArticle(sList, blogInfo);
		}
		NodeList fenYeList = getNodeList(url, "div", "id", "papelist");
		if (fenYeList != null)
			parsePage(fenYeList, blogInfo);
		StringBuilder sb = new StringBuilder();
		/* ����meta��Ϣ */
		sb.append("<html>\r\n");
		sb.append("<head><title>" + filepath.replace('/', ' ')
				+ "</title></head>\r\n<body <font face=\"microsoft yahei\"> >\r\n");
		String htmlString=null;
		/* ��һ�㣬����ᱻ��Ϊ�Ƕ�����Ϊ */
		List<Attribute> li = blogInfo.getColumArticleList().asList();
		for (int i = 0; i < li.size(); i++)
		{
			String titleName = li.get(i).getName();
			htmlString=HandleTool.handleHtml(titleName, (String) li.get(i).getValue(),
					blogInfo);
			sb.append(htmlString);
			try
			{
				/* ��һ�㣬����ᱻ��Ϊ�Ƕ�����Ϊ */
				Thread.sleep(500);
			} catch (Exception e)
			{
				e.printStackTrace();
			}
		}
		sb.append("</body>\r\n</html>");
		String fileNameString =filepath.replace('/', '_');
		htmlString = sb.toString();
		//�������·ݵ����±�����.html�ļ���
		FileTool.writeFile(filepath+"/"+fileNameString + ".html", htmlString.getBytes());
		//�������·ݵĲ�������ת����pdf�ļ�
		try
		{
			//��pdf�ļ����ϵ��û���Ŀ¼��
			File file=new File(filepath);
			file=file.getParentFile();
			//FileTool.makeDir(file.getPath()+"/pdf");
			fileNameString=file.getPath()+"/"+fileNameString;
			FileTool.generatePDF(htmlString,fileNameString);
		} catch (Exception e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
