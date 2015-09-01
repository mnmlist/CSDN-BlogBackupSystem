package com.mnmlist.backup.byTotal;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.StringReader;
import java.util.Date;
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
import org.zefer.pd4ml.PD4Constants;
import org.zefer.pd4ml.PD4ML;
import org.zefer.pd4ml.PD4PageMark;

/**
 * @author mnmlist@163.com
 * @blog http://blog.csdn.net/mnmlist
 * @version v1.0
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
	 * @return ����ĳ�������ӽڵ�
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
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		NodeList nlist = null;
		if (list.size() > 0)
			nlist = list.elementAt(0).getChildren();
		return nlist;
	}

	/*
	 * @param nlist ���½��
	 */
	public static void parseTitle(NodeList nlist, BlogInfo blogInfo)
	{
		Node tit = null;
		int count = nlist.size();
		for (int i = 0; i < count; i++)
		{
			tit = nlist.elementAt(i);
			if (tit instanceof Span)
			{
				Span span = (Span) tit;
				if (span.getAttribute("class") != null
						&& span.getAttribute("class").equalsIgnoreCase(
								"link_title"))
				{
					LinkTag link = (LinkTag) span.childAt(0);
					String title = link.getLinkText();
					/* ���ļ����в�������ַ��滻��������ַ� */
					title = title.trim();
					title = title.replaceAll("[\\?/:*|<>\"]", "_");
					blogInfo.setArticleTitle(title);
					// Spider.articleTitle=title;
					// Spider.imageResourceList.add(new Attribute("title",
					// title));
				}
			} else
			{
				NodeList slist = tit.getChildren();
				if (slist != null && slist.size() > 0)
				{
					parseTitle(slist, blogInfo);
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
	public static int parseImg(NodeList nlist, int index, BlogInfo blogInfo)
	{
		String title = blogInfo.getArticleTitle();
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
					imgtag.setImageURL(title + "_files/" + index + ".gif");
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
	 * @param nlist HTML�·ݴ浵���ӱ�ǩ����
	 */
	public static void parseMonthArticle(NodeList nlist, BlogInfo blogInfo)
	{
		Node atls = null;
		int count = nlist.size();
		for (int i = 0; i < count; i++)
		{
			atls = nlist.elementAt(i);
			if (atls instanceof LinkTag)
			{
				LinkTag link = (LinkTag) atls;
				blogInfo.getMonthIndexList().add(
						new Attribute(link.getLinkText(), link.extractLink()));
			} else
			{
				NodeList slist = atls.getChildren();
				if (slist != null && slist.size() > 0)
				{
					parseMonthArticle(slist, blogInfo);
				}
			}
		}

	}

	/*
	 * @param nlist HTMLÿ�·ݴ浵���ӱ�ǩ����
	 */
	public static void parsePerArticle(NodeList nlist, BlogInfo blogInfo)
	{
		Node atl = null;
		int count = nlist.size();
		for (int i = 0; i < count; i++)
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
					blogInfo.getMonthArticleList()
							.add(new Attribute(link.getLinkText(),
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
	 * @param nlist HTML������Ϣ��ǩ���ӱ�ǩ����
	 * 
	 * @return ��������
	 */
	public static String parseAuthor(NodeList nlist)
	{
		Node aut = null;
		String author = null;
		int count = nlist.size();
		for (int i = 0; i < count; i++)
		{
			aut = nlist.elementAt(i);
			if (aut instanceof LinkTag)
			{
				LinkTag link = (LinkTag) aut;
				return link.getLinkText();
			} else
			{
				NodeList slist = aut.getChildren();
				if (slist != null && slist.size() > 0)
				{
					return ParseTool.parseAuthor(slist);
				}
			}
		}
		return author;
	}

	/*
	 * @param filepath ���ش浵��·��
	 * 
	 * @param url ���汾�´浵����ҳ��URL
	 * 
	 * @param articles ���汾�´浵������
	 */
	public static void parseMonth(String filepath, String url,
			AttributeList articles, BlogInfo blogInfo)
	{
		NodeList titleList = getNodeList(url, "div", "id", "article_list");
		StringBuilder sb = new StringBuilder();
		/* ����meta��Ϣ */
		sb.append("<html>\r\n");
		sb.append("<head><title>" + filepath.replace('/', ' ')
				+ "</title></head>\r\n<body <font face=\"microsoft yahei\"> >\r\n");
		String articleString = null;
		parsePerArticle(titleList, blogInfo);
		NodeList fenYeList = getNodeList(url, "div", "id", "papelist");
		if (fenYeList != null)
			parsePage(fenYeList, blogInfo);
		FileTool.makeDir(filepath);// bloger name//month Index
		List<Attribute> li = blogInfo.getMonthArticleList().asList();
		//�ֱ𱣴�ÿһƪ�������²��������·ݵ����½Ἧ���Ϊ.html�ļ���.pdf�ļ�
		for (int i = 0; i < li.size(); i++)
		{
			articleString = HandleTool.handleHtml(filepath, (String) li.get(i)
					.getValue(), articles, blogInfo);
			sb.append(articleString);
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
		String htmlString = sb.toString();
		//�������·ݵ����±�����.html�ļ���
		FileTool.writeFile(filepath+"/"+fileNameString + ".html", htmlString.getBytes());
		//�������·ݵĲ�������ת����pdf�ļ�
		try
		{
			//��pdf�ļ����ϵ��û���Ŀ¼�µ�pdf��Ŀ¼��
			File file=new File(filepath);
			file=file.getParentFile();
			fileNameString=file.getPath()+"/pdf/"+fileNameString;
			FileTool.generatePDF(htmlString,fileNameString);
		} catch (Exception e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// ��html�ļ�ת��Ϊpdf�ļ�
		blogInfo.getMonthArticleList().clear();
	}
}
