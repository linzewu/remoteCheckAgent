package com.xs.rca.task;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.persistence.criteria.CriteriaBuilder.Case;

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
import com.xs.rca.entity.RegistVehcle;
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
					sbrz.setBo(xml.asXML());
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

	@Scheduled(fixedDelay = 2000)
	public void scanEventJob() throws Exception {
		List<Event> list = (List<Event>) eventManger.getEvents();
		for (Event e : list) {
			try{
				if(e.getEvent().equals("18C49")){
					getVehInfo(e);
					continue;
				}
				String viewName = "V" + e.getEvent();
				String checkItem = e.getCheckItem();
				
				if (RCAConstant.V18C63.equals(viewName)) {
					uploadImage(e);
					continue;
				}
				if ((RCAConstant.V18C53.equals(viewName)
						|| RCAConstant.V18C80.equals(viewName)
						|| RCAConstant.V18C54.equals(viewName)
						|| RCAConstant.V18C81.equals(viewName) || RCAConstant.V18C64
							.equals(viewName))
						&& checkItem != null
						&& !"".equals(checkItem)) {
					viewName += "_" + checkItem;
				}

				List<Map> datas = (List<Map>) checkedInfoManger.getViewData(
						viewName, e.getLsh());

				if (datas == null || datas.isEmpty()) {
					e.setState(2);
					e.setMessage("该事件无法找到视图：" + viewName + " 条件为RegistNo ="
							+ e.getLsh() + "对应的数据。请检查上传的数据是否已经产生！");
					eventManger.update(e);
					continue;
				}

				for (Map data : datas) {
					// 数据转换
					convertData(viewName, data);
					
					if(viewName.equals(RCAConstant.V18C62)){
						//人工检验项目结果
						Map rgmap = this.checkedInfoManger.getRgjyxmjg(e.getLsh());
						String strDoc = rgjyxmjg2Xml(rgmap);
						data.put("rgjyjgs", strDoc);
						
						
						//仪器检测结果
						Map yqmap  =  this.checkedInfoManger.getYqsbjyjg(e.getLsh());
						
						if(yqmap!=null){
							 String bgh =(String)yqmap.get("DocRegist");
							 Map xzMap = this.checkedInfoManger.getBzxz(bgh);
							 
							 String yqDoc = yqjyxm2Xml(yqmap,xzMap);
							 
							 data.put("yqsbjyjgs", yqDoc);
							 
						}
					}
					Document document = this.write(e, data);
					Element root = document.getRootElement();
					Element head = root.element("head");
					Element code = head.element("code");
					Element message = head.element("message");
					if ("1".equals(code.getText())) {
						System.out.println("delete event id" + e.getId());
						eventManger.delete(e);
						logger.info("viewName:"+viewName);
						if(viewName.equals(RCAConstant.V18C51)){
							saveRegistVehcle(data, document);
						}
						
//						String item=(String)data.get("jyxm");
//						String registNo=(String)data.get("RegistNo");
//						
//						if(viewName.equals(RCAConstant.V18C58)&&RCAConstant.ITEM_F1.equals(item)){
//							
//							String line = eventManger.getDetLineSelect(registNo);
//							if(RCAConstant.ROAD.equals(line)){
//								createRoadEvent(registNo);
//							}
//						}
//						
					} else {
						e.setState(2);
						e.setMessage(message.getText());
						logger.error(document.asXML());
						eventManger.update(e);
					}
				}
			}catch(Exception et){
				e.setState(2);
				e.setMessage(et.getMessage());
				et.printStackTrace();
				logger.error("----");
				eventManger.update(e);
			}
		}
	}
	
	private String converRes(Object param){
		
		String res=(String)param;
		
		if(res==null||"".equals(res.trim())){
			return "";
		}
		
		Integer i=Integer.parseInt(res.trim());
		
		if(i==2){
			return "0";
		}
		
		if(i==1){
			return "2";
		}
		
		if(i==0){
			return "1";
		}
		
		return "";
	}
	
	private String yqjyxm2Xml(Map yqMap,Map xzMap){
		
		List yqList=new ArrayList();
		
		//序号
		Integer index=1;
		
		//取轴制动数据
		for(int i=1;i<=6;i++){
			
			String zdlValue = (String)yqMap.get("B"+i+"13");
			
			if(zdlValue==null||"-".equals(zdlValue.trim())||"".equals(zdlValue.trim())){
				continue;
			}
			
			Map<String,Object> zdlMap=new HashMap<String,Object>();
			Map<String,Object> bphlMap=new HashMap<String,Object>();
			
			
			String zdl="";
			String bphl="";
			
			switch (i) {
			case 1:
				zdl="一轴制动率";
				bphl="一轴不平衡率";
				break;
			case 2:
				zdl="二轴制动率";
				bphl="二轴不平衡率";
				break;
			case 3:
				zdl="三轴制动率";
				bphl="三轴不平衡率";
				break;
			case 4:
				zdl="四轴制动率";
				bphl="四轴不平衡率";
				break;
			case 5:
				zdl="五轴制动率";
				bphl="五轴不平衡率";
				break;
			case 6:
				zdl="六轴制动率";
				bphl="六轴不平衡率";
				break;
			}
			zdlMap.put("xh",index.toString());
			index++;
			zdlMap.put("yqjyxm", zdl);
			zdlMap.put("yqjyjg", zdlValue);
			zdlMap.put("yqbzxz", xzMap.get("B"+i+"13XZ"));
			zdlMap.put("yqjgpd", converRes(yqMap.get("B"+i+"14")));
			zdlMap.put("yqjybz", "");
			yqList.add(zdlMap);
			
			bphlMap.put("xh",index.toString());
			index++;
			bphlMap.put("yqjyxm", bphl);
			bphlMap.put("yqjyjg", yqMap.get("B"+i+"23"));
			bphlMap.put("yqbzxz", xzMap.get("B"+i+"23XZ"));
			bphlMap.put("yqjgpd", converRes(yqMap.get("B"+i+"24")));
			bphlMap.put("yqjybz", "");
			yqList.add(bphlMap);
		}
		
		//取驻车制动率的
		String zczdl=(String)yqMap.get("B813");
		if(zczdl!=null&&!"".equals(zczdl.trim())&&!"-".equals(zczdl.trim())){
			Map<String,Object> zczdlMap=new HashMap<String,Object>();
			zczdlMap.put("xh",index.toString());
			index++;
			zczdlMap.put("yqjyxm", "驻车制动率");
			zczdlMap.put("yqjyjg", zczdl);
			zczdlMap.put("yqbzxz", xzMap.get("B813XZ"));
			zczdlMap.put("yqjgpd", converRes(yqMap.get("B8Res")));
			zczdlMap.put("yqjybz", "");
			yqList.add(zczdlMap);
		}
		
		//整车制动率
		String zhczdl=(String)yqMap.get("B913");
		if(zhczdl!=null&&!"".equals(zhczdl.trim())&&!"-".equals(zhczdl.trim())){
			
			Map<String,Object> zhczdlMap=new HashMap<String,Object>();
			zhczdlMap.put("xh",index.toString());
			index++;
			zhczdlMap.put("yqjyxm", "整车制动率");
			zhczdlMap.put("yqjyjg", zhczdl);
			zhczdlMap.put("yqbzxz", xzMap.get("B913XZ"));
			zhczdlMap.put("yqjgpd", converRes(yqMap.get("B9Res")));
			zhczdlMap.put("yqjybz", "");
			yqList.add(zhczdlMap);
		}
		
		//灯光结果
		for(int i=1;i<=4;i++){
			
			String gq=(String)yqMap.get("H"+i+"01");
			
			if(gq==null||"".equals(gq.trim())||"-".equals(gq.trim())){
				continue;
			}
			
			String gqxm="";
			String pyxm="";
			
			switch (i) {
			case 1:
				gqxm="左外灯远光光强";
				pyxm="左外灯远光垂直偏移H";
				break;
			case 2:
				gqxm="左内灯远光光强";
				pyxm="左内灯远光垂直偏移H";
				break;
			case 3:
				gqxm="右内灯远光光强";
				pyxm="右内灯远光垂直偏移H";
				break;
			case 4:
				gqxm="右外灯远光光强";
				pyxm="右外灯远光垂直偏移H";
				break;
			}
			
			//光强	
			Map<String,Object> gqMap=new HashMap<String,Object>();
			gqMap.put("xh",index.toString());
			index++;
			gqMap.put("yqjyxm", gqxm);
			gqMap.put("yqjyjg", gq);
			gqMap.put("yqbzxz", xzMap.get("H"+i+"01XZ"));
			gqMap.put("yqjgpd", converRes(yqMap.get("H"+i+"02")));
			gqMap.put("yqjybz", "");
			yqList.add(gqMap);
			
			String czpy= (String)yqMap.get("H"+i+"15");
			
			
			if(czpy==null||"".equals(czpy.trim())||"-".equals(czpy.trim())){
				continue;
			}
			//垂直偏移
			Map<String,Object> pyMap=new HashMap<String,Object>();
			pyMap.put("xh",index.toString());
			index++;
			pyMap.put("yqjyxm", pyxm);
			pyMap.put("yqjyjg", yqMap.get("H"+i+"15"));
			pyMap.put("yqbzxz", xzMap.get("H"+i+"15XZ"));
			pyMap.put("yqjgpd", converRes(yqMap.get("H"+i+"13")));
			pyMap.put("yqjybz", "");
			yqList.add(pyMap);
		}
		
		//车速表
		String csbz=(String)yqMap.get("S01");
		if(csbz!=null&&!"".equals(csbz.trim())&&!"-".equals(csbz.trim())){
			
			Map<String,Object> csbzMap=new HashMap<String,Object>();
			csbzMap.put("xh",index.toString());
			index++;
			csbzMap.put("yqjyxm", "车速表检测值");
			csbzMap.put("yqjyjg", csbz);
			csbzMap.put("yqbzxz", xzMap.get("S01XZ"));
			csbzMap.put("yqjgpd", converRes(yqMap.get("SRes")));
			csbzMap.put("yqjybz", "");
			yqList.add(csbzMap);
		}
		
		
		//侧滑检测值
		String chjcz=(String)yqMap.get("A01");
		if(chjcz!=null&&!"".equals(chjcz.trim())&&!"-".equals(chjcz.trim())){
			
			Map<String,Object> chjczMap=new HashMap<String,Object>();
			chjczMap.put("xh",index.toString());
			index++;
			chjczMap.put("yqjyxm", "侧滑检测值");
			chjczMap.put("yqjyjg", chjcz);
			chjczMap.put("yqbzxz", xzMap.get("A01XZ"));
			chjczMap.put("yqjgpd", converRes(yqMap.get("ARes")));
			chjczMap.put("yqjybz", "");
			yqList.add(chjczMap);
		}
		
		Document d = BeanXMLUtil.list2xml(yqList, "yqsbjyjg");
		
//		StringBuffer strXML=new StringBuffer();
//		List<Element> elements = d.getRootElement().elements();
//		for(Element element :elements){
//			strXML.append(element.asXML());
//		}
		
		return d.asXML();
	}
	
	/**
	 * 人工检验项目 XML转换
	 * @param data
	 * @return
	 */
	private String rgjyxmjg2Xml(Map data){
		
		String res1 = (String)data.get("XM1Res");
		String res2 = (String)data.get("XM2Res");
		String res3 = (String)data.get("XM3Res");
		String res4 = (String)data.get("XM4Res");
		String res5 = (String)data.get("XM5Res");
		String res6 = (String)data.get("XM6Res");
		String res7 = (String)data.get("XM7Res");
		
		List<Map> res=new ArrayList<Map>();
		
		Integer index=1;
		
		if(res1!=null&&!res1.trim().equals("-")){
			Map<String,String> map=new HashMap<String,String>();
			map.put("xh", index.toString());
			map.put("rgjyxm", "车辆唯一性检查");
			if(res1.trim().equals("")){
				map.put("rgjgpd", "1");
			}else{
				map.put("rgjgpd","2");
			}
			map.put("rgjysm", getRgjysm(res1,data));
			
			map.put("rgjybz", "");
			index++;
			
			res.add(map);
		}
		
		if(res2!=null&&!res2.trim().equals("-")){
			Map<String,String> map=new HashMap<String,String>();
			map.put("xh", index.toString());
			map.put("rgjyxm", "车辆特征参数检查");
			if(res2.trim().equals("")){
				map.put("rgjgpd", "1");
			}else{
				map.put("rgjgpd","2");
			}
			map.put("rgjysm", getRgjysm(res2,data));
			map.put("rgjybz", "");
			index++;
			res.add(map);
		}
		
		if(res3!=null&&!res3.trim().equals("-")){
			Map<String,String> map=new HashMap<String,String>();
			map.put("xh", index.toString());
			map.put("rgjyxm", "车辆外观检查");
			if(res3.trim().equals("")){
				map.put("rgjgpd", "1");
			}else{
				map.put("rgjgpd","2");
			}
			map.put("rgjysm", getRgjysm(res3,data));
			map.put("rgjybz", "");
			index++;
			res.add(map);
		}
		
		if(res4!=null&&!res4.trim().equals("-")){
			Map<String,String> map=new HashMap<String,String>();
			map.put("xh", index.toString());
			map.put("rgjyxm", "安全装置检查");
			if(res4.trim().equals("")){
				map.put("rgjgpd", "1");
			}else{
				map.put("rgjgpd","2");
			}
			map.put("rgjysm", getRgjysm(res4,data));
			map.put("rgjybz", "");
			index++;
			res.add(map);
		}
		
		if(res5!=null&&!res5.trim().equals("-")){
			Map<String,String> map=new HashMap<String,String>();
			map.put("xh", index.toString());
			map.put("rgjyxm", "联网查询");
			if(res5.trim().equals("")){
				map.put("rgjgpd", "1");
			}else{
				map.put("rgjgpd","2");
			}
			map.put("rgjysm", getRgjysm(res5,data));
			map.put("rgjybz", "");
			index++;
			res.add(map);
		}
		
		
		if(res6!=null&&!res6.trim().equals("-")){
			Map<String,String> map=new HashMap<String,String>();
			map.put("xh", index.toString());
			map.put("rgjyxm", "底盘动态检验");
			if(res6.trim().equals("")){
				map.put("rgjgpd", "1");
			}else{
				map.put("rgjgpd","2");
			}
			map.put("rgjysm", getRgjysm(res6,data));
			map.put("rgjybz", "");
			index++;
			res.add(map);
		}
		
		
		if(res7!=null&&!res7.trim().equals("-")){
			Map<String,String> map=new HashMap<String,String>();
			map.put("xh", index.toString());
			map.put("rgjyxm", "车辆底盘部件检查");
			if(res7.trim().equals("")){
				map.put("rgjgpd", "1");
			}else{
				map.put("rgjgpd","2");
			}
			map.put("rgjysm", getRgjysm(res7,data));
			map.put("rgjybz", "");
			index++;
			res.add(map);
		}
		
		
		Document d = BeanXMLUtil.list2xml(res, "rgjyjg");
//		
//		StringBuffer strXML=new StringBuffer();
//		List<Element> elements = d.getRootElement().elements();
//		for(Element element :elements){
//			strXML.append(element.asXML());
//		}
		
		
		return d.asXML();
	}
	
	private String getRgjysm(String res,Map data){
		
		if(res.trim().equals("")){
			return "";
		}
		
		StringBuffer strRes=new StringBuffer("");
		String[] keys = res.trim().split(",");
		
		for(String key:keys){
			if(key.length()==1){
				key="A00"+key+"T";
			}
			if(key.length()==2){
				key="A0"+key+"T";
			}
			strRes.append(data.get(key)+",");
		}
		
		return strRes.toString();
	}
	
	
	private void createRoadEvent(String code){
			Event e=new Event();
			e.setEvent("18C55");
			e.setCreateDate(new Date());
			e.setLsh(code);
			e.setState(0);
			e.setCheckItem("R");
			eventManger.save(e);
	}
	
	private void saveRegistVehcle(Map data,Document document){
		logger.info("begin save RegistVehcle");
		RegistVehcle rv=new RegistVehcle();
		rv.setClsbdh((String)data.get("clsbdh"));
		rv.setHphm((String)data.get("hphm"));
		rv.setHpzl((String)data.get("hpzl"));
		rv.setJcxdh((String)data.get("jcxdh"));
		rv.setJycs(String.valueOf(data.get("jycs")));
		rv.setJylsh((String)data.get("jylsh"));
		rv.setCreateTime(new Date());
		rv.setXml(document.asXML());
		eventManger.save(rv);
		logger.info("end save RegistVehcle");
	}

	public Map<String, String> getCodes(String type) {
		if (codes == null) {
			return null;
		}
		Map<String, String> map = new HashMap<String, String>();
		for (Codes code : codes) {
			if (type.equals(code.getType())) {
				map.put(code.getKey(), code.getValue());
			}
		}
		return map;
	}

	private void convertData(String viewName, Map data) {
		Set<String> set = data.keySet();
		for (String key : set) {
			boolean isConvert = SpecialConvert(viewName, key, data);
			if (isConvert) {
				continue;
			}
			Map<String, String> code = getCodes(key);
			if (code != null && !code.isEmpty()) {
				String oldValue = (String) data.get(key);

				if (oldValue == null) {
					data.put(key, "");
					continue;
				}

				String newValue = code.get(oldValue);
				if (newValue != null) {
					data.put(key, newValue);
				} else {
					data.put(key, "");
				}
			}
		}
	}

	public boolean SpecialConvert(String viewName, String key, Map map) {
		boolean isConvert = false;
		Object o = map.get(key);
		if (o instanceof String) {
			String value = (String) o;
			switch (key) {
			case "jyjgbh":
				map.put(key, jyjgbh);
				isConvert = true;
				break;
			case "cwkc":
				if (value != null && !value.equals("")) {
					map.put(key,value.split("/")[0]);
					isConvert = true;
					break;
				}
			case "cwkk":
				if (value != null && !value.equals("")) {
					map.put(key, value.split("/")[1]);
					isConvert = true;
					break;
				}
			case "cwkg":
				if (value != null && !value.equals("")) {
					map.put(key, value.split("/")[2]);
					isConvert = true;
					break;
				}
			case "qlj":
				if (value != null && !value.equals("")) {
					map.put(key, value.split("/")[0]);
					isConvert = true;
					break;
				}
			case "hlj":
				if (value != null && !value.equals("")) {
					String v[] = value.split("/");
					if (v.length > 1) {
						map.put(key, v[1]);
						isConvert = true;
						break;
					}
				}
			case "jyxm":
				convertJYXM(viewName,key,value,map);
				isConvert = true;
				break;
				
			case "fjx":
				convertJYXM(viewName,key,value,map);
				isConvert = true;
				break;
			}
		}
		return isConvert;
	}
	
	
	private void convertJYXM(String viewName,String key ,String value,Map map){
		
		if (RCAConstant.V18C51.equals(viewName)
				|| RCAConstant.V18C66.equals(viewName)||RCAConstant.V18C65.equals(viewName)) {
			Map jyxmMap = getCodes(key);
			char[] charArray = value.toCharArray();
			String newValue = "";
			for (char c : charArray) {
				String item = String.valueOf(c);
				// 如果是灯光项目
				if (item.equals("m")) {
					logger.info(item + "eq m");
					item = item + map.get("qzdz");
					logger.info(item);
				}
				if (item.equals("j")) {
					String zs = (String) map.get("zs");
					if (zs != null) {
						newValue += ",B" + zs.trim();
					}
				} else {
					String xm = (String) jyxmMap.get(item);
					if (xm != null) {
						newValue += "," + jyxmMap.get(item);
					}
				}
			}
			if (newValue.length() > 0) {
				newValue = newValue.substring(1, newValue.length());
				// 如果是路试
				if ("路试".equals(map.get("jcxdh"))) {
					newValue = newValue + ",R1,R2";
				}
			}
			logger.debug("jyxm:" + newValue);
			map.put(key, newValue);
		}
		
	}

	public void uploadImage(Event e) {

		String jyxm = e.getCheckItem();
		String zpzl = e.getZpzl();
		Map zpMap = imageDBManger.getImage(e.getLsh(), zpzl, jyxm);
		if (zpMap != null) {
			zpMap.put("jyxm", jyxm);
			zpMap.put("zpzl", zpzl);
			zpMap.put("jyjgbh", jyjgbh);
			try {
				Document document = this.write(e, zpMap);
				Element root = document.getRootElement();
				Element head = root.element("head");
				Element code = head.element("code");
				Element message = head.element("message");
				if ("1".equals(code.getText())) {
					eventManger.delete(e);
				} else {
					e.setState(2);
					e.setMessage(message.getText());
					eventManger.update(e);
				}
			} catch (Exception e1) {
				e1.printStackTrace();
			}
		}else{
			e.setState(2);
			e.setMessage("无法找到该照片的数据");
			eventManger.update(e);
		}
		
	}
	
	public void getVehInfo(Event event) throws RemoteException, UnsupportedEncodingException, DocumentException{
		
		TmriJaxRpcOutAccessServiceStub.QueryObjectOut qoo=new TmriJaxRpcOutAccessServiceStub.QueryObjectOut();
		qoo.setJkid("18C49");
		qoo.setXtlb(RCAConstant.XTLB);
		qoo.setJkxlh(jkxlh);
		
		Map data=new HashMap();
		
		data.put("hphm", event.getHphm());
		data.put("hpzl", event.getHpzl());
		data.put("clsbdh", event.getClsbdh());
		data.put("jyjgbh", jyjgbh);
		
		
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
		
		SBRZ sbrz=new SBRZ();
		
		sbrz.setHmph(event.getHphm());
		
		sbrz.setHpzl(event.getHpzl());
		
		sbrz.setJkbmc("18C49");
		
		sbrz.setMessage(message.getText());
		sbrz.setXml(document.asXML());
		
		sbrz.setBo(xml.asXML());
		
		event.setState(2);
		
		eventManger.update(event);
		
		eventManger.saveLog(sbrz);
		
	}

}
