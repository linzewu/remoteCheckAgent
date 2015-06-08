package com.xs.rca.task;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.rmi.RemoteException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import org.apache.axis2.AxisFault;
import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.xs.rca.entity.Codes;
import com.xs.rca.entity.Event;
import com.xs.rca.entity.SBRZ;
import com.xs.rca.manager.CheckedInfoManger;
import com.xs.rca.manager.EventManger;
import com.xs.rca.manager.ImageDBManger;
import com.xs.rca.util.BeanXMLUtil;
import com.xs.rca.util.RCAConstant;
import com.xs.rca.ws.client.TmriJaxRpcOutAccessServiceStub;
import com.xs.rca.ws.client.TmriJaxRpcOutAccessServiceStub.QueryObjectOutResponse;
import com.xs.rca.ws.client.TmriJaxRpcOutAccessServiceStub.WriteObjectOutResponse;

@Component("CheckedInfoTaskJob")
public class CheckedInfoTaskJob {

	private static Logger logger = Logger.getLogger(CheckedInfoTaskJob.class);

	private Properties properties = null;

	private TmriJaxRpcOutAccessServiceStub tro = null;

	private String jkxlh = null;

	private List<Codes> codes = null;

	private String jyjgbh = null;

	@Resource(name = "checkedInfoManger")
	private CheckedInfoManger checkedInfoManger;

	@Resource(name = "imageDBManger")
	private ImageDBManger imageDBManger;

	@Resource(name = "eventManger")
	private EventManger eventManger;

	public CheckedInfoTaskJob() {
		try {
			tro = new TmriJaxRpcOutAccessServiceStub();
			InputStream in = this.getClass().getClassLoader()
					.getResourceAsStream("rca.properties");
			properties = new Properties();
			properties.load(in);
			jkxlh = properties.getProperty("jkxlh");
			jyjgbh = properties.getProperty("jyjgbh");
		} catch (AxisFault e) {
			logger.error("链接专网查验平台失败", e);
		} catch (IOException e) {
			logger.error("加载rca配置文件出错", e);
		}
	}

	@PostConstruct
	public void intJob() {
		codes = eventManger.getCodes();
		logger.debug("code:" + codes);
	}

	private Document write(TmriJaxRpcOutAccessServiceStub.WriteObjectOut woo,
			Object o) throws Exception {
		try {
			Document xml = BeanXMLUtil.bean2xml(o, "vehispara");
			logger.debug("bo:" + xml.asXML());
			woo.setUTF8XmlDoc(xml.asXML());
			WriteObjectOutResponse wor = tro.writeObjectOut(woo);
			String response = wor.getWriteObjectOutReturn();
			response = URLDecoder.decode(response, "utf-8");
			Document document = DocumentHelper.parseText(response);
			Element root = document.getRootElement();
			Element head = root.element("head");
			Element code = head.element("code");
			Element message = head.element("message");

			SBRZ sbrz = new SBRZ();
			sbrz.setSbsj(new Date());
			sbrz.setJkbid(Integer.parseInt(xml.getRootElement()
					.element("vehispara").element("id").getText()));
			sbrz.setJylsh(xml.getRootElement().element("vehispara")
					.element("jylsh").getText());
			sbrz.setJkbmc("JYDLXX");
			sbrz.setMessage(message.getText());
			sbrz.setCode(code.getText());
			sbrz.setXml(document.asXML());
			eventManger.saveLog(sbrz);
			logger.debug("response BO:" + document.asXML());

			return document;
		} catch (NoSuchMethodException e) {
			logger.error("bean2xml转换异常", e);
			throw e;
		} catch (UnsupportedEncodingException e) {
			logger.error("xmlencoding异常", e);
			throw e;
		} catch (RemoteException e) {
			logger.error("远程连接异常", e);
			throw e;
		} catch (DocumentException e) {
			logger.error("response parseText异常", e);
			throw e;
		}
	}

	private Document write(Event event, Map data) throws Exception {
		try {
			TmriJaxRpcOutAccessServiceStub.WriteObjectOut woo = new TmriJaxRpcOutAccessServiceStub.WriteObjectOut();
			String jkid = event.getEvent();
			woo.setJkid(jkid);
			woo.setXtlb(RCAConstant.XTLB);
			woo.setJkxlh(jkxlh);
			Document xml = BeanXMLUtil.map2xml(data, "vehispara");
			woo.setUTF8XmlDoc(xml.asXML());
			WriteObjectOutResponse wor = tro.writeObjectOut(woo);
			String response = wor.getWriteObjectOutReturn();
			response = URLDecoder.decode(response, "utf-8");
			Document document = DocumentHelper.parseText(response);
			Element root = document.getRootElement();
			Element head = root.element("head");
			Element code = head.element("code");
			Element message = head.element("message");
			logger.info(document.asXML());
			SBRZ sbrz = new SBRZ();
			sbrz.setSbsj(new Date());

			logger.debug("xml:" + xml.asXML());
			sbrz.setJylsh((String) data.get("jylsh"));
			if (event.getCheckItem() == null) {
				sbrz.setJkbmc(jkid);
			} else {
				sbrz.setJkbmc(jkid + "_" + event.getCheckItem());
			}

			sbrz.setMessage(message.getText());
			sbrz.setCode(code.getText());
			sbrz.setXml(document.asXML());
			if (!"18C63".equals(jkid)) {
				if (xml.asXML().length() < 8000) {
					sbrz.setBo(URLDecoder.decode(xml.asXML(),"UTF-8"));
				}
			}
			eventManger.saveLog(sbrz);

			return document;
		} catch (UnsupportedEncodingException e) {
			logger.error("xmlencoding异常", e);
			throw e;
		} catch (RemoteException e) {
			logger.error("远程连接异常", e);
			throw e;
		} catch (DocumentException e) {
			logger.error("response parseText异常", e);
			throw e;
		}
	}

//	@Scheduled(fixedDelay = 2000)
//	public void scanEventJob() throws Exception {
//		List<Event> list = (List<Event>) eventManger.getEvents();
//		for (Event e : list) {
//			String viewName = "V" + e.getEvent();
//			String checkItem = e.getCheckItem();
//			if (RCAConstant.V18C63.equals(viewName)) {
//				uploadImage(e);
//				continue;
//			}
//			if ((RCAConstant.V18C53.equals(viewName)
//					|| RCAConstant.V18C80.equals(viewName)
//					|| RCAConstant.V18C54.equals(viewName)
//					|| RCAConstant.V18C81.equals(viewName) || RCAConstant.V18C64
//						.equals(viewName))
//					&& checkItem != null
//					&& !"".equals(checkItem)) {
//				viewName += "_" + checkItem;
//			}
//
//			List<Map> datas = (List<Map>) checkedInfoManger.getViewData(
//					viewName, e.getLsh());
//
//			if (datas == null || datas.isEmpty()) {
//				e.setState(2);
//				e.setMessage("该事件无法找到视图：" + viewName + " 条件为RegistNo ="
//						+ e.getLsh() + "对应的数据。请检查上传的数据是否已经产生！");
//				eventManger.update(e);
//				continue;
//			}
//
//			for (Map data : datas) {
//				// 数据转换
//				convertData(viewName, data);
//				Document document = this.write(e, data);
//				Element root = document.getRootElement();
//				Element head = root.element("head");
//				Element code = head.element("code");
//				Element message = head.element("message");
//				if ("1".equals(code.getText())) {
//					System.out.println("delete event id" + e.getId());
//					eventManger.delete(e);
//					logger.info("viewName:"+viewName);
//					if(viewName.equals(RCAConstant.V18C51)){
//						saveRegistVehcle(data, document);
//					}
//					
////					String item=(String)data.get("jyxm");
////					String registNo=(String)data.get("RegistNo");
////					
////					if(viewName.equals(RCAConstant.V18C58)&&RCAConstant.ITEM_F1.equals(item)){
////						
////						String line = eventManger.getDetLineSelect(registNo);
////						if(RCAConstant.ROAD.equals(line)){
////							createRoadEvent(registNo);
////						}
////					}
////					
//				} else {
//					e.setState(2);
//					e.setMessage(message.getText());
//					eventManger.update(e);
//				}
//			}
//		}
//	}
//	
//	
//	private void createRoadEvent(String code){
//			Event e=new Event();
//			e.setEvent("18C55");
//			e.setCreateDate(new Date());
//			e.setLsh(code);
//			e.setState(0);
//			e.setCheckItem("R");
//			eventManger.save(e);
//	}
//	
//	private void saveRegistVehcle(Map data,Document document){
//		logger.info("begin save RegistVehcle");
//		RegistVehcle rv=new RegistVehcle();
//		rv.setClsbdh((String)data.get("clsbdh"));
//		rv.setHphm((String)data.get("hphm"));
//		rv.setHpzl((String)data.get("hpzl"));
//		rv.setJcxdh((String)data.get("jcxdh"));
//		rv.setJycs(String.valueOf(data.get("jycs")));
//		rv.setJylsh((String)data.get("jylsh"));
//		rv.setCreateTime(new Date());
//		rv.setXml(document.asXML());
//		eventManger.save(rv);
//		logger.info("end save RegistVehcle");
//	}
//
//	public Map<String, String> getCodes(String type) {
//		if (codes == null) {
//			return null;
//		}
//		Map<String, String> map = new HashMap<String, String>();
//		for (Codes code : codes) {
//			if (type.equals(code.getType())) {
//				map.put(code.getKey(), code.getValue());
//			}
//		}
//		return map;
//	}
//
//	private void convertData(String viewName, Map data) {
//		Set<String> set = data.keySet();
//		for (String key : set) {
//			boolean isConvert = SpecialConvert(viewName, key, data);
//			if (isConvert) {
//				continue;
//			}
//			Map<String, String> code = getCodes(key);
//			if (code != null && !code.isEmpty()) {
//				String oldValue = (String) data.get(key);
//
//				if (oldValue == null) {
//					data.put(key, "");
//					continue;
//				}
//
//				String newValue = code.get(oldValue);
//				if (newValue != null) {
//					data.put(key, newValue);
//				} else {
//					data.put(key, "");
//				}
//			}
//		}
//	}
//
//	public boolean SpecialConvert(String viewName, String key, Map map) {
//		boolean isConvert = false;
//		Object o = map.get(key);
//		if (o instanceof String) {
//			String value = (String) o;
//			switch (key) {
//			case "jyjgbh":
//				map.put(key, jyjgbh);
//				isConvert = true;
//				break;
//			case "cwkc":
//				if (value != null && !value.equals("")) {
//					map.put(key,value.split("/")[0]);
//					isConvert = true;
//					break;
//				}
//			case "cwkk":
//				if (value != null && !value.equals("")) {
//					map.put(key, value.split("/")[1]);
//					isConvert = true;
//					break;
//				}
//			case "cwkg":
//				if (value != null && !value.equals("")) {
//					map.put(key, value.split("/")[2]);
//					isConvert = true;
//					break;
//				}
//			case "qlj":
//				if (value != null && !value.equals("")) {
//					map.put(key, value.split("/")[0]);
//					isConvert = true;
//					break;
//				}
//			case "hlj":
//				if (value != null && !value.equals("")) {
//					String v[] = value.split("/");
//					if (v.length > 1) {
//						map.put(key, v[1]);
//						isConvert = true;
//						break;
//					}
//				}
//			case "jyxm":
//				convertJYXM(viewName,key,value,map);
//				isConvert = true;
//				break;
//				
//			case "fjx":
//				convertJYXM(viewName,key,value,map);
//				isConvert = true;
//				break;
//			}
//		}
//		return isConvert;
//	}
//	
//	
//	private void convertJYXM(String viewName,String key ,String value,Map map){
//		
//		if (RCAConstant.V18C51.equals(viewName)
//				|| RCAConstant.V18C66.equals(viewName)||RCAConstant.V18C65.equals(viewName)) {
//			Map jyxmMap = getCodes(key);
//			char[] charArray = value.toCharArray();
//			String newValue = "";
//			for (char c : charArray) {
//				String item = String.valueOf(c);
//				// 如果是灯光项目
//				if (item.equals("m")) {
//					logger.info(item + "eq m");
//					item = item + map.get("qzdz");
//					logger.info(item);
//				}
//				if (item.equals("j")) {
//					String zs = (String) map.get("zs");
//					if (zs != null) {
//						newValue += ",B" + zs.trim();
//					}
//				} else {
//					String xm = (String) jyxmMap.get(item);
//					if (xm != null) {
//						newValue += "," + jyxmMap.get(item);
//					}
//				}
//			}
//			if (newValue.length() > 0) {
//				newValue = newValue.substring(1, newValue.length());
//				// 如果是路试
//				if ("路试".equals(map.get("jcxdh"))) {
//					newValue = newValue + ",R1,R2";
//				}
//			}
//			logger.debug("jyxm:" + newValue);
//			map.put(key, newValue);
//		}
//		
//	}
//
//	public void uploadImage(Event e) {
//
//		String jyxm = e.getCheckItem();
//		String zpzl = e.getZpzl();
//		Map zpMap = imageDBManger.getImage(e.getLsh(), zpzl, jyxm);
//		if (zpMap != null) {
//			zpMap.put("jyxm", jyxm);
//			zpMap.put("zpzl", zpzl);
//			zpMap.put("jyjgbh", jyjgbh);
//			try {
//				Document document = this.write(e, zpMap);
//				Element root = document.getRootElement();
//				Element head = root.element("head");
//				Element code = head.element("code");
//				Element message = head.element("message");
//				if ("1".equals(code.getText())) {
//					eventManger.delete(e);
//				} else {
//					e.setState(2);
//					e.setMessage(message.getText());
//					eventManger.update(e);
//				}
//			} catch (Exception e1) {
//				e1.printStackTrace();
//			}
//		}
//	}
	
	@Scheduled(fixedDelay = 10000)
	public void test() throws RemoteException, UnsupportedEncodingException, DocumentException{
		
		System.out.println("begin"+ System.currentTimeMillis());
		
		long a=System.currentTimeMillis();
		
		
		TmriJaxRpcOutAccessServiceStub.QueryObjectOut qoo=new TmriJaxRpcOutAccessServiceStub.QueryObjectOut();
		qoo.setJkid("18C49");
		qoo.setXtlb(RCAConstant.XTLB);
		qoo.setJkxlh(jkxlh);
		
		Map data=new HashMap();
		
		data.put("hphm", "苏JZU202");
		data.put("hpzl", "02");
		data.put("clsbdh", "0079");
		data.put("jyjgbh", jyjgbh);
		
		System.out.println(jyjgbh);
		
		Document xml = BeanXMLUtil.map2xml(data, "QueryCondition");
		qoo.setUTF8XmlDoc(xml.asXML());
		
		QueryObjectOutResponse qoor = tro.queryObjectOut(qoo);
		
		
		String response = qoor.getQueryObjectOutReturn();
		response = URLDecoder.decode(response, "utf-8");
		Document document = DocumentHelper.parseText(response);
		Element root = document.getRootElement();
		Element head = root.element("head");
		Element code = head.element("code");
		Element message = head.element("message");
		
		long b=System.currentTimeMillis();
		
		System.out.println("总共耗时"+(a-b)/1000 );
		
		System.out.println(document.asXML());
	}

}
