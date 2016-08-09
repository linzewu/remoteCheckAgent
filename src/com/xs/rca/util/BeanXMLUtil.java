package com.xs.rca.util;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URLEncoder;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;

public class BeanXMLUtil {
	
	 private static Logger logger = Logger.getLogger(BeanXMLUtil.class);

	public static Document bean2xml(Object bean, String element)
			throws 	NoSuchMethodException{

		Field[] fields = bean.getClass().getDeclaredFields();
		
		Document document=DocumentHelper.createDocument();
		document.setXMLEncoding("GBK");
		Element root = document.addElement("root");
		Element subelement = root.addElement(element);
		logger.debug("fields size"+fields.length);
		for (Field field : fields) {
			String fname = field.getName();
			String getMehod = "get" + fname.substring(0, 1).toUpperCase()
					+ fname.substring(1, fname.length());
			Method method = bean.getClass().getMethod(getMehod);
			Object value;
			try {
				value = method.invoke(bean);
				Element felement = subelement.addElement(fname);
				if(value!=null&&!"".equals(value)){
					felement.setText( URLEncoder.encode(value.toString(),"UTF-8"));
				}
			} catch (IllegalAccessException | IllegalArgumentException
					| InvocationTargetException | UnsupportedEncodingException e) {
				logger.error("bean2xml执行异常",e);
			}
		}
		logger.debug("subelement"+subelement.asXML());
		logger.debug("document"+document.asXML());
		return document;
	}
	
	
	public static Document map2xml(Map map ,String element){
		logger.debug("map:+"+map);
		Document document=DocumentHelper.createDocument();
		document.setXMLEncoding("GBK");
		Element root = document.addElement("root");
		Element subelement = root.addElement(element);
		Set<String> set = map.keySet();
		for(String key:set){
			Element felement = subelement.addElement(key);
			try {
				if(map.get(key)!=null){
					String val=map.get(key).toString().trim();
					if("rgjyjgs".equals(key)||"yqsbjyjgs".equals(key)){
						if(val!=null&&!"".equals(val.trim())){
							Document sd = DocumentHelper.parseText(val);
							List<Element> list = sd.getRootElement().elements();
							System.out.println("========list"+list.size());
							for(Element e:list){
								felement.add(e.detach());
							}
						}
					}else{
						felement.setText(URLEncoder.encode(val,"UTF-8"));
					}
					
				
				}
			} catch (Exception e) {
				logger.error("map2xml执行异常",e);
			}
		}
		logger.debug("document:"+document.asXML());
		return document;
	}
	
	public static Document list2xml(List<Map> data,String element){
		
		logger.debug("list:+"+data);
		Document document=DocumentHelper.createDocument();
		document.setXMLEncoding("GBK");
		Element  root =  document.addElement("root");
		for(Map map:data){
			Element  subElement = root.addElement(element);
			
			Set<String> keys = map.keySet();
			
			for(String key:keys){
				Element e = subElement.addElement(key);
				String val=(String)map.get(key);
				val=val==null?"":val;
				try {
					e.setText(URLEncoder.encode(val,"UTF-8"));
				} catch (UnsupportedEncodingException e1) {
					logger.error("map2xml执行异常",e1);
				}
			}
		}
		
		return document;
	}
	
	

	public static void main(String[] arg) {
		
		try {
			
			
			String a ="	<rgjyjg>				<xh>1</xh>				<rgjyxm>%e8%bd%a6%e8%be%86%e5%94%af%e4%b8%80%e6%80%a7%e6%a3%80%e6%9f%a5				</rgjyxm>				<rgjgpd>1</rgjgpd>				<rgjysm />				<rgjybz />			</rgjyjg>			<rgjyjg>			<xh>2</xh>				<rgjyxm>%e8%bd%a6%e8%be%86%e7%89%b9%e5%be%81%e5%8f%82%e6%95%b0%e6%a3%80%e6%9f%a5				</rgjyxm>				<rgjgpd>1</rgjgpd>				<rgjysm />				<rgjybz />			</rgjyjg>";
			Document document=DocumentHelper.createDocument();
			document.setXMLEncoding("GBK");
			Element root = document.addElement("root");
			
			System.out.println(((Element)root.elements().get(0)).elements().size());
			
			System.out.println(document.asXML());
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
