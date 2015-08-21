package com.mnmlist.backup.byTotal;

import org.htmlparser.Parser;
import org.htmlparser.util.ParserException;

/**
 * @author mnmlist@163.com
 * @blog http://blog.csdn.net/mnmlist
 * @version v1.0
 */
public class ParserInstance {
	/*
	 * @param inputURL url
	 * 
	 * @return Parser instance 
	 */
	public static Parser getParserInstance(String inputURL)
	{
		return Parser.createParser(new String(FileTool.getContent(inputURL, 1)), "UTF-8");
	}
	}
