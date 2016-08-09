<%@ page language="java" contentType="text/html; charset=utf-8"
    pageEncoding="utf-8"%>



<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
<title>盐城市交警支队车辆管理所机动车安检视频监控程序</title>
<style type="text/css">
body {
	background-color:#96C3E4;
	color:#222248;
	font-size:12px;
	font-family:Arial, Helvetica, sans-serif;
}
td {
	color:#222248;
	font-size:12px;
	font-family:Arial, Helvetica, sans-serif;
}
ul {
	margin-left:5px;
	padding: 0px;
	white-space:nowrap;
}
li {
	display:inline-block;
	list-style-type:none;
	vertical-align:middle;
	height:30px;
}
#Container {
	width: 980px;
	background-color:#f5f5f5;
	padding-top:0px;
	position: absolute;
 left: expression((body.clientWidth-980)/2);
	height:100%;
	top:0;
}
#TopLogo {
	width: 100%;
	padding-top:10px;
	height:50px;
	font-size:24px;
}
#TopMenu {
	width: 100%;
	height:50px;
	padding:0;
}
#MainBody {
	width: 100%;
 height:expression(body.clientHeight-50-50-28);
}
#BodyLeft {
	width: 300px;
	height:100%;
	float: left;
	border-right:1px solid #CCC;
	padding:10px;
}
#BodyRight {
	width: 679px;
	height:100%;
	float: right;
	padding:10px;
}
#Foot {
	width: 100%;
	height:22px;
	background-color:#E2E7EB;
	padding-top:5px;
}
#OperatLogTitle {
	width:100%;
	height:22px;
	padding-top:3px;
	padding-left:5px;
	font-weight:bold;
	background-color:#EBEBEB;
}
#OCXBody {
	width:600px;
	height:450px;
	margin-bottom:5px;
}
#OperatLogBody {
	width:100%;
	height:150px;
	overflow:auto;
	padding:5px;
	border:1px solid #EBEBEB;
}
.normalinput {
	width:100px;
	vertical-align:middle;
	margin-right:10px;
}
.longinput {
	width:150px;
	vertical-align:middle;
	margin-right:10px;
}
.distanceleft {
	padding-left:10px;
}
.normalbtn {
	background-color: #3FF;
	border:1px solid #0CF;
	vertical-align:middle;
	height:22px;
	padding-top:2px;
}
.smallocxdiv{float:left;display:; width:1px; height:1px;}
</style>
<script type="text/javascript">
//全局变量定义
var m_iNowChanNo = -1;                           //当前通道号
var m_iLoginUserId = -1;                         //注册设备用户ID
var m_iChannelNum = -1; 						 //模拟通道总数
var m_iIPChannelNum = -1;                        //IP通道总数
var m_bDVRControl = null;						 //OCX控件对象
var m_iProtocolType = 0;                         //协议类型，0 – TCP， 1 - UDP
var m_iStreamType = 0;                           //码流类型，0 表示主码流， 1 表示子码流
var m_iPlay = 0;                                 //当前是否正在预览
var m_iRecord = 0;                               //当前是否正在录像
var m_iTalk = 0;                                 //当前是否正在对讲 
var m_iVoice = 0;                                //当前是否打开声音
var m_iAutoPTZ = 0;                              //当前云台是否正在自转
var m_iPTZSpeed = 4;                             //云台速度
var m_szDeviceType = 0;                          //设备类型
var m_iPlayback = 0;                             //是否回放状态 
var m_iDownload = 0;                             //是否下载状态 
/*************************************************
  Function:    	onload
  Description:	页面加载完后判断系统日期是否在1971-2037范围
  Input:        无
  Output:      	无
  Return:		无
*************************************************/
window.onload = function()
{ 
	var myDate = new Date();
	var iYear = myDate.getFullYear();        
	if(iYear < 1971 || iYear > 2037)
	{
		alert("为了正常使用本软件，请将系统日期年限设置在1971-2037范围内！");
		//parent.location.href = "../login.php";
	}
	if(document.getElementById("HIKOBJECT1").object == null)
	{
		alert("请先下载控件并注册！");
		m_bDVRControl = null;
	}
	else
	{
		m_bDVRControl = document.getElementById("HIKOBJECT1");
		ChangeStatus(1);
		ArrangeWindow(4);
	}
} 
//document.oncontextmenu = rightclick;
/*************************************************
  Function:    	rightclick
  Description:	网页禁用右键
  Input:        无
  Output:      	无
  Return:		bool:   true false
*************************************************/
function rightclick()
{
	return false;
}
/*************************************************
  Function:    	rightclick
  Description:	网页禁用右键
  Input:        无
  Output:      	无
  Return:		bool:   true false
*************************************************/
function ButtonPress(sKey)
{
	try
	{
		switch (sKey)
		{
			case "LoginDev":
			{
				var szDevIp = document.getElementById("DeviceIP").value; 
				var szDevPort = document.getElementById("DevicePort").value; 
				var szDevUser = document.getElementById("DeviceUsername").value; 
				var szDevPwd = document.getElementById("DevicePasswd").value; 
				m_iLoginUserId = m_bDVRControl.Login(szDevIp,szDevPort,szDevUser,szDevPwd);
				if(m_iLoginUserId == -1)
				{
                                                                                var dRet = m_bDVRControl.GetLastError();
                                                                                LogMessage("注册失败，错误号：" + dRet);
				}
				else
				{
					LogMessage("注册成功！");
					for(var i = 2; i <= 4; i ++)
					{
						document.getElementById("HIKOBJECT" + i).SetUserID(m_iLoginUserId);
					}
				}
				break;
			}
			case "LogoutDev":
			{
				if(m_bDVRControl.Logout())
				{
					LogMessage("注销成功！");
				}
				else
				{
					LogMessage("注销失败！");
				}
				break;
			}
			case "getDevName":
			{
				var szDecName = m_bDVRControl.GetServerName();
				//szDecName = szDecName.replace(/\s/g,"&nbsp;"); 
				if(szDecName == "")
				{
					LogMessage("获取名称失败！");
					szDecName = "Embedded Net DVR";	
				}
				else
				{
					LogMessage("获取名称成功！");	
				}
				document.getElementById("DeviceName").value = szDecName; 
				break;
			}
			case "getDevChan":
			{
				szServerInfo = m_bDVRControl.GetServerInfo();
				var xmlDoc = new ActiveXObject("Microsoft.XMLDOM");
				xmlDoc.async="false"
				xmlDoc.loadXML(szServerInfo)
				m_iChannelNum = parseInt(xmlDoc.documentElement.childNodes[0].childNodes[0].nodeValue);
                m_iIPChannelNum = parseInt(xmlDoc.documentElement.childNodes[8].childNodes[0].nodeValue);
                m_szDeviceType = xmlDoc.documentElement.childNodes[1].childNodes[0].nodeValue;
				//m_iChannelNum = parseInt(iChannelNum);

				if(m_iChannelNum < 1)
				{
					LogMessage("获取模拟通道失败！");
				}
				else
				{
					LogMessage("获取模拟通道成功！");	
					document.getElementById("ChannelList").length = 0; //先清空下拉列表
					for(var i = 0; i < m_iChannelNum; i ++)
					{
						var szChannelName = m_bDVRControl.GetChannelName(i);
						if(szChannelName == "")
						{
							szChannelName = "通道" + (i + 1);
						}
						document.getElementById("ChannelList").options.add(new Option(szChannelName,i)); 
					}
				}
                if (m_iIPChannelNum < 1) {
                    LogMessage("获取IP通道失败！");
                }
                else {
                    LogMessage("获取IP通道成功！");

                    if (m_iIPChannelNum >= 64) {
                             LogMessage("IP通道个数大于等于64，" + "通道号取值从0开始！");
                             m_iIPChanStart = 0;
                        }

                    else{
                             LogMessage("如果设备有IP通道，IP通道号取值从32开始！");
                             m_iIPChanStart = 32;
                    }

                    m_bDVRControl.GetIPParaCfg();
                    szIPChanInfo = m_bDVRControl.GetIPCConfig();
                    var xmlDoc = new ActiveXObject("Microsoft.XMLDOM");
                    xmlDoc.async = "false"
                    xmlDoc.loadXML(szIPChanInfo)
                    for (var i = m_iChannelNum; i < m_iChannelNum+m_iIPChannelNum; i++) {                   
                        m_iIPConnet = parseInt(xmlDoc.documentElement.childNodes[i].childNodes[3].childNodes[0].nodeValue);
                        if (m_iIPConnet == 1) {
                            LogMessage("获取IP通道" + (i-m_iChannelNum+1) + "成功！");                            
                            var szChannelName = m_bDVRControl.GetChannelName(i+m_iIPChanStart );
                            if (szChannelName == "") {
                                szChannelName = "IP通道" + (i-m_iChannelNum + 1);
                            }
                            document.getElementById("ChannelList").options.add(new Option(szChannelName, i));
                        }
                    }
                }

				break;
			}

case "Preview:start":
{
       LogMessage(m_szDeviceType);
       m_iNowChanNo = parseInt(document.getElementById("ChannelList").value)
       if(m_iNowChanNo>=m_iChannelNum)
       {           
            m_iNowChanNo=m_iNowChanNo-m_iChannelNum+ m_iIPChanStart;
       } 

       if (m_iNowChanNo > -1) {

            if (m_iPlay == 1) {

                m_bDVRControl.StopRealPlay();
                m_iPlay = 0;

            }


            var bRet = m_bDVRControl.StartRealPlay(m_iNowChanNo, m_iProtocolType, m_iStreamType);

            if (bRet) {

                LogMessage("预览通道" + (m_iNowChanNo + 1) + "成功！");

                m_iPlay = 1;

            }

            else {

                LogMessage("预览通道" + (m_iNowChanNo + 1) + "失败！");

                var dRet = m_bDVRControl.GetLastError();
                LogMessage("预览失败，错误号：" + dRet);

            }

        }

        else {

            LogMessage("请选择通道号！");

        }

        break;

    }
			case "Preview:stop":
			{
				
				if(m_bDVRControl.StopRealPlay())
				{
					LogMessage("停止预览成功！");
					m_iPlay = 0;
				}
				else
				{
					LogMessage("停止预览失败！");
				}
				break;
			}
			case "CatPic:bmp":
			{
				if(m_iPlay == 1)
				{
					if(m_bDVRControl.BMPCapturePicture("C:/OCXBMPCaptureFiles",1))
					{
						LogMessage("抓BMP图成功！");
					}
					else
					{
						LogMessage("抓BMP图失败！");
					}
				}
				else
				{
					LogMessage("请先预览！");
				}
				break;
			}
			case "CatPic:jpeg":
			{
				if(m_iPlay == 1)
				{
					if(m_bDVRControl.JPEGCapturePicture((m_iNowChanNo + 1),2,0,"F:/work/document/test/dir",1))
					{
						LogMessage("抓JPEG图成功！");
					}
					else
					{
						LogMessage("抓JPEG图失败！");
					}
				}
				else
				{
					LogMessage("请先预览！");
				}
				break;
			}
			case "Record:start":
			{
				if(m_iPlay == 1)
				{
					if(m_iRecord == 0)
					{
						if(m_bDVRControl.StartRecord("C:/OCXRecordFiles"))
						{
							LogMessage("开始录像成功！");
							m_iRecord = 1;
						}
						else
						{
							LogMessage("开始录像失败！");
						}
					}
				}
				else
				{
					LogMessage("请先预览！");
				}
				break;
			}
			case "Record:stop":
			{
				if(m_iRecord == 1)
				{
					if(m_bDVRControl.StopRecord(1))
					{
						LogMessage("停止录像成功！");
						m_iRecord = 0;
					}
					else
					{
						LogMessage("停止录像失败！");
					}
				}
				break;
			}
			case "talk:start":
			{
				if(m_iLoginUserId > -1)
				{
					if(m_iTalk == 0)
					{
						if(m_bDVRControl.StartTalk(1))
						{
							LogMessage("开始对讲成功！");
							m_iTalk = 1;
						}
						else
						{
							LogMessage("开始对讲失败！");
						}
					}
				}
				else
				{
					LogMessage("请注册设备！");
				}
				break;
			}
			case "talk:stop":
			{
				if(m_iTalk == 1)
				{
					if(m_bDVRControl.StopTalk())
					{
						LogMessage("停止对讲成功！");
						m_iTalk = 0;
					}
					else
					{
						LogMessage("停止对讲失败！");
					}
				}
				break;
			}
			case "voice:start":
			{
				if(m_iPlay == 1)
				{
					if(m_iVoice == 0)
					{
						if(m_bDVRControl.OpenSound(1))
						{
							LogMessage("打开声音成功！");
							m_iVoice = 1;
						}
						else
						{
							LogMessage("打开声音失败！");
						}
					}
				}
				else
				{
					LogMessage("请先预览！");
				}
				break;
			}
			case "voice:stop":
			{
				if(m_iVoice == 1)
				{
					if(m_bDVRControl.CloseSound(1))
					{
						LogMessage("关闭声音成功！");
						m_iVoice = 0;
					}
					else
					{
						LogMessage("关闭声音失败！");
					}
				}
				break;
            }
        case "playback:start":
            {
                if (m_iIPChannelNum >= 64) {

                    LogMessage("IP通道个数大于等于64，" + "IP通道号取值从0开始！");

                    m_iIPChanStart = 0;

                }

                else {
                    LogMessage("如果设备有IP通道，IP通道号取值从32开始！");
                    m_iIPChanStart = 32;
                }

                m_iNowChanNo = parseInt(document.getElementById("ChannelList").value)
                if (m_iNowChanNo >= m_iChannelNum) {
                    m_iNowChanNo = m_iNowChanNo - m_iChannelNum + m_iIPChanStart;
                }

                if (m_iLoginUserId > -1) {

                    if (m_iPlayback == 1) {
                        m_bDVRControl.StopPlayBack();
                        m_iPlayback = 0;
                    } 

                    // m_iNowChanNo = parseInt(document.getElementById("ChannelList").value);
                    if (m_iPlayback == 0) {
                        if (m_bDVRControl.PlayBackByTime(m_iNowChanNo, "2014-1-14 11:10:00", "2014-1-14 11:35:50")) {
                            LogMessage("开始时间回放成功，起止时间：2014-1-14 11:10:00 ~ 2014-1-14 11:35:50！");
                        }
                        m_iPlayback = 1;
                    }
                }
                else {
                    LogMessage("请注册设备！");
                }
                break;
            }
        case "playback:stop":
            {
                if (m_bDVRControl.StopPlayBack()) {
                    LogMessage("停止回放成功！");
                    m_iPlayback = 0;
                }
                else {
                    LogMessage("停止回放失败！");
                }
                break;
            }
        case "downloadfile:start":
            {
                if (m_iLoginUserId > -1) {
                    if (m_iIPChannelNum >= 64) {
                        LogMessage("IP通道个数大于等于64，" + "通道号取值从0开始！");
                        m_iIPChanStart = 0;

                    }
                    else {
                        LogMessage("如果设备有IP通道，IP通道号取值从32开始！");
                        m_iIPChanStart = 32;
                    }
                    m_iNowChanNo = parseInt(document.getElementById("ChannelList").value)
                    if (m_iNowChanNo >= m_iChannelNum) {
                        m_iNowChanNo = m_iNowChanNo - m_iChannelNum + m_iIPChanStart;
                    }

                    if (m_iDownload == 1) {
                        m_bDVRControl.StopDownLoadFile();
                        m_iDownload = 0;
                    }

                    if (m_iDownload == 0) {
                        //m_iNowChanNo = parseInt(document.getElementById("ChannelList").value);
                        LogMessage("查找录像文件，通道号: " + (m_iNowChanNo + 1) + "，起止时间：2014-1-14 11:10:00, 2014-1-14 11:35:50")

                        szFileInfo = m_bDVRControl.SearchRemoteRecordFile(m_iNowChanNo, 0, "2014-1-14 11:10:00", "2014-1-14 11:35:50", false, false, "");
                        if (szFileInfo == " ") {
                            var dRet = m_bDVRControl.GetLastError();
                            LogMessage("搜索录像文件失败！错误号：" + dRet);
                            break;
                        }
                        else if (szFileInfo == "null") {
                            LogMessage("没有搜索到录像文件！");
                            break;
                        }
                        else {
                            LogMessage("搜索录像文件成功！");
                            var xmlDoc = new ActiveXObject("Microsoft.XMLDOM");
                            xmlDoc.async = "false";
                            xmlDoc.loadXML(szFileInfo);
                            var szFileName = xmlDoc.documentElement.childNodes[0].childNodes[2].childNodes[0].nodeValue;

                            if (m_bDVRControl.DownLoadByFileName(szFileName, "D:\\")) {
                                LogMessage("下载查找到的第一个录像文件" + szFileName + "下载成功！");
                                m_iDownload = 1;
                            }
                            else {
                                LogMessage("开始下载失败！");
                            }
                        }
                        break;
                    }
                }

                else {
                    LogMessage("请注册设备！");
                }

                break;

            }
        case "downloadtime:start":
            {
                if (m_iIPChannelNum >= 64) {

                    LogMessage("IP通道个数大于等于64，" + "IP通道号取值从0开始！");

                    m_iIPChanStart = 0;

                }

                else {
                    LogMessage("如果设备有IP通道，IP通道号取值从32开始！");
                    m_iIPChanStart = 32;
                }

                m_iNowChanNo = parseInt(document.getElementById("ChannelList").value)
                if (m_iNowChanNo >= m_iChannelNum) {
                    m_iNowChanNo = m_iNowChanNo - m_iChannelNum + m_iIPChanStart;
                }

                if (m_iLoginUserId > -1) {

                    if (m_iDownload == 1) {
                        m_bDVRControl.StopDownLoadFile();
                        m_iDownload = 0;
                    }
                    
                    // m_iNowChanNo = parseInt(document.getElementById("ChannelList").value);
                    if (m_iDownload == 0) {
                        if (m_bDVRControl.DownLoadByTime(m_iNowChanNo, "2014-1-14 11:10:00", "2014-1-14 11:35:50","D:\\")) {
                            LogMessage("开始按时间下载成功，起止时间：2014-1-14 11:10:00 ~ 2014-1-14 11:35:50！");
                        }
                        m_iDownload = 1;
                    }
                }
                else {
                    LogMessage("请注册设备！");
                }
                break;
        }

        case "downloadfile:stop":
        {
                if (m_bDVRControl.StopDownLoadFile()) {
                     LogMessage("停止下载成功！");
                     m_iDownload = 0;
                }
                else {
                     LogMessage("停止下载失败！");
                }
         
                break;
         }

			case "PTZ:stop":
			{
				if(m_iPlay == 1)
				{
					if(m_bDVRControl.PTZCtrlStop(10,m_iPTZSpeed))
					{
						LogMessage("停止PTZ成功！");
						m_iAutoPTZ = 0;
					}
					else
					{
						LogMessage("停止PTZ失败！");
					}
				}
				break;
			}
			case "PTZ:leftup":
			{
				if(m_iPlay == 1)
				{
					if(m_iAutoPTZ == 1)
					{
						m_bDVRControl.PTZCtrlStop(10,m_iPTZSpeed);
						m_iAutoPTZ = 0;
					}
					if(m_bDVRControl.PTZCtrlStart(13,m_iPTZSpeed))
					{
						LogMessage("PTZ左上成功！");
					}
					else
					{
						LogMessage("PTZ左上失败！");
					}
				}
				else
				{
					LogMessage("请先预览！");
				}
				break;
			}	
			case "PTZ:rightup":
			{
				if(m_iPlay == 1)
				{
					if(m_iAutoPTZ == 1)
					{
						m_bDVRControl.PTZCtrlStop(10,m_iPTZSpeed);
						m_iAutoPTZ = 0;
					}
					if(m_bDVRControl.PTZCtrlStart(14,m_iPTZSpeed))
					{
						LogMessage("PTZ右上成功！");
					}
					else
					{
						LogMessage("PTZ右上失败！");
					}
				}
				else
				{
					LogMessage("请先预览！");
				}
				break;
			}	
			case "PTZ:up":
			{
				if(m_iPlay == 1)
				{
					if(m_iAutoPTZ == 1)
					{
						m_bDVRControl.PTZCtrlStop(10,m_iPTZSpeed);
						m_iAutoPTZ = 0;
					}
					if(m_bDVRControl.PTZCtrlStart(0,m_iPTZSpeed))
					{
						LogMessage("PTZ上成功！");
					}
					else
					{
						LogMessage("PTZ上失败！");
					}
				}
				else
				{
					LogMessage("请先预览！");
				}
				break;
			}	
			case "PTZ:left":
			{
				if(m_iPlay == 1)
				{
					if(m_iAutoPTZ == 1)
					{
						m_bDVRControl.PTZCtrlStop(10,m_iPTZSpeed);
						m_iAutoPTZ = 0;
					}
					if(m_bDVRControl.PTZCtrlStart(2,m_iPTZSpeed))
					{
						LogMessage("PTZ向左成功！");
					}
					else
					{
						LogMessage("PTZ向左失败！");
					}
				}
				else
				{
					LogMessage("请先预览！");
				}
				break;
			}	
			case "PTZ:right":
			{
				if(m_iPlay == 1)
				{
					if(m_iAutoPTZ == 1)
					{
						m_bDVRControl.PTZCtrlStop(10,m_iPTZSpeed);
						m_iAutoPTZ = 0;
					}
					if(m_bDVRControl.PTZCtrlStart(3,m_iPTZSpeed))
					{
						LogMessage("PTZ向右成功！");
					}
					else
					{
						LogMessage("PTZ向右失败！");
					}
				}
				else
				{
					LogMessage("请先预览！");
				}
				break;
			}	
			case "PTZ:rightdown":
			{
				if(m_iPlay == 1)
				{
					if(m_iAutoPTZ == 1)
					{
						m_bDVRControl.PTZCtrlStop(10,m_iPTZSpeed);
						m_iAutoPTZ = 0;
					}
					if(m_bDVRControl.PTZCtrlStart(16,m_iPTZSpeed))
					{
						LogMessage("PTZ右下成功！");
					}
					else
					{
						LogMessage("PTZ右下失败！");
					}
				}
				else
				{
					LogMessage("请先预览！");
				}
				break;
			}	
			case "PTZ:leftdown":
			{
				if(m_iPlay == 1)
				{
					if(m_iAutoPTZ == 1)
					{
						m_bDVRControl.PTZCtrlStop(10,m_iPTZSpeed);
						m_iAutoPTZ = 0;
					}
					if(m_bDVRControl.PTZCtrlStart(15,m_iPTZSpeed))
					{
						LogMessage("PTZ左下成功！");
					}
					else
					{
						LogMessage("PTZ左下失败！");
					}
				}
				else
				{
					LogMessage("请先预览！");
				}
				break;
			}	
			case "PTZ:down":
			{
				if(m_iPlay == 1)
				{
					if(m_iAutoPTZ == 1)
					{
						m_bDVRControl.PTZCtrlStop(10,m_iPTZSpeed);
						m_iAutoPTZ = 0;
					}
					if(m_bDVRControl.PTZCtrlStart(1,m_iPTZSpeed))
					{
						LogMessage("PTZ向下成功！");
					}
					else
					{
						LogMessage("PTZ向下失败！");
					}
				}
				else
				{
					LogMessage("请先预览！");
				}
				break;
			}	
			case "PTZ:auto":
			{
				if(m_iPlay == 1)
				{
					if(m_bDVRControl.PTZCtrlStart(10,m_iPTZSpeed))
					{
						LogMessage("PTZ自转成功！");
						m_iAutoPTZ = 1;
					}
					else
					{
						LogMessage("PTZ自转失败！");
					}
				}
				else
				{
					LogMessage("请先预览！");
				}
				break;
			}		
			case "zoom:in":
			{
				if(m_iPlay == 1)
				{
					if(m_iAutoPTZ == 1)
					{
						m_bDVRControl.PTZCtrlStop(10,m_iPTZSpeed);
						m_iAutoPTZ = 0;
					}
					if(m_bDVRControl.PTZCtrlStart(4,m_iPTZSpeed))
					{
						LogMessage("焦距拉近成功！");
					}
					else
					{
						LogMessage("焦距拉近失败！");
					}
				}
				else
				{
					LogMessage("请先预览！");
				}
				break;
			}	
			case "zoom:out":
			{
				if(m_iPlay == 1)
				{
					if(m_iAutoPTZ == 1)
					{
						m_bDVRControl.PTZCtrlStop(10,m_iPTZSpeed);
						m_iAutoPTZ = 0;
					}
					if(m_bDVRControl.PTZCtrlStart(5,m_iPTZSpeed))
					{
						LogMessage("焦距拉远成功！");
					}
					else
					{
						LogMessage("焦距拉远失败！");
					}
				}
				else
				{
					LogMessage("请先预览！");
				}
				break;
			}
			case "focus:in":
			{
				if(m_iPlay == 1)
				{
					if(m_iAutoPTZ == 1)
					{
						m_bDVRControl.PTZCtrlStop(10,m_iPTZSpeed);
						m_iAutoPTZ = 0;
					}
					if(m_bDVRControl.PTZCtrlStart(6,m_iPTZSpeed))
					{
						LogMessage("聚焦拉近成功！");
					}
					else
					{
						LogMessage("聚焦拉近失败！");
					}
				}
				else
				{
					LogMessage("请先预览！");
				}
				break;
			}	
			case "focus:out":
			{
				if(m_iPlay == 1)
				{
					if(m_iAutoPTZ == 1)
					{
						m_bDVRControl.PTZCtrlStop(10,m_iPTZSpeed);
						m_iAutoPTZ = 0;
					}
					if(m_bDVRControl.PTZCtrlStart(7,m_iPTZSpeed))
					{
						LogMessage("聚焦拉远成功！");
					}
					else
					{
						LogMessage("聚焦拉远失败！");
					}
				}
				else
				{
					LogMessage("请先预览！");
				}
				break;
			}
			case "iris:in":
			{
				if(m_iPlay == 1)
				{
					if(m_iAutoPTZ == 1)
					{
						m_bDVRControl.PTZCtrlStop(10,m_iPTZSpeed);
						m_iAutoPTZ = 0;
					}
					if(m_bDVRControl.PTZCtrlStart(8,m_iPTZSpeed))
					{
						LogMessage("光圈大成功！");
					}
					else
					{
						LogMessage("光圈大失败！");
					}
				}
				else
				{
					LogMessage("请先预览！");
				}
				break;
			}	
			case "iris:out":
			{
				if(m_iPlay == 1)
				{
					if(m_iAutoPTZ == 1)
					{
						m_bDVRControl.PTZCtrlStop(10,m_iPTZSpeed);
						m_iAutoPTZ = 0;
					}
					if(m_bDVRControl.PTZCtrlStart(9,m_iPTZSpeed))
					{
						LogMessage("光圈小成功！");
					}
					else
					{
						LogMessage("光圈小失败！");
					}
				}
				else
				{
					LogMessage("请先预览！");
				}
				break;
			}
			case "getImagePar":
			{
				if(m_iPlay == 1)
				{
					var szXmlInfo = m_bDVRControl.GetVideoEffect();
					if(szXmlInfo != "")
					{
						var xmlDoc = new ActiveXObject("Microsoft.XMLDOM");
						xmlDoc.async="false"
						xmlDoc.loadXML(szXmlInfo)	
						document.getElementById("PicLight").value = xmlDoc.documentElement.childNodes[0].childNodes[0].nodeValue;
						document.getElementById("PicContrast").value = xmlDoc.documentElement.childNodes[1].childNodes[0].nodeValue;
						document.getElementById("PicSaturation").value = xmlDoc.documentElement.childNodes[2].childNodes[0].nodeValue;
						document.getElementById("PicTonal").value = xmlDoc.documentElement.childNodes[3].childNodes[0].nodeValue;
						LogMessage("获取图像参数成功！");
					}
					else
					{
						LogMessage("获取图像参数失败！");
					}
				}
				else
				{
					LogMessage("请先预览！");
				}
				break;
			}		
			case "setImagePar":
			{
				if(m_iPlay == 1)
				{
					var iL = parseInt(document.getElementById("PicLight").value);
					var iC = parseInt(document.getElementById("PicContrast").value);
					var iS = parseInt(document.getElementById("PicSaturation").value);
					var iT = parseInt(document.getElementById("PicTonal").value);
					var bRet = m_bDVRControl.SetVideoEffect(iL,iC,iS,iT);
					if(bRet)
					{
						LogMessage("设置图像参数成功！");
					}
					else
					{
						LogMessage("设置图像参数失败！");
					}
				}
				else
				{
					LogMessage("请先预览！");
				}
				break;
			}
			case "setPreset":
			{
				if(m_iPlay == 1)
				{
					var iPreset = parseInt(document.getElementById("Preset").value);
					var bRet = m_bDVRControl.PTZCtrlSetPreset(iPreset);
					if(bRet)
					{
						LogMessage("设置预置点成功！");
					}
					else
					{
						LogMessage("设置预置点失败！");
					}
				}
				else
				{
					LogMessage("请先预览！");
				}
				break;
			}
			case "goPreset":
			{
				if(m_iPlay == 1)
				{
					var iPreset = parseInt(document.getElementById("Preset").value);
					var bRet = m_bDVRControl.PTZCtrlGotoPreset(iPreset);
					if(bRet)
					{
						LogMessage("调用预置点成功！");
					}
					else
					{
						LogMessage("调用预置点成功！");
					}
				}
				else
				{
					LogMessage("请先预览！");
				}
				break;
			}
			default:
			{
				//Record:start   setPreset
				break;
			}
		}		//switch  
	}
	catch(err)
	{
		alert(err);
	}
}
/*************************************************
  Function:    	LogMessage
  Description:	写执行结果日志
  Input:        msg:日志
  Output:      	无
  Return:		无
*************************************************/
function LogMessage(msg)
{
	var myDate = new Date(); 
	var szNowTime = myDate.toLocaleString( );                   //获取日期与时间
	document.getElementById("OperatLogBody").innerHTML = szNowTime + " --> " + msg + "<br>" + document.getElementById("OperatLogBody").innerHTML;
}
</script>
<script type="text/javascript" for="HIKOBJECT1" event="SelectWindow()">
	ChangeStatus(1);
</script>
<script type="text/javascript" for="HIKOBJECT2" event="SelectWindow()">
	ChangeStatus(2);
</script>
<script type="text/javascript" for="HIKOBJECT3" event="SelectWindow()">
	ChangeStatus(3);
</script>
<script type="text/javascript" for="HIKOBJECT4" event="SelectWindow()">
	ChangeStatus(4);
</script>
</head>
<body>
<div id="Container">
  <div id="TopLogo" align="center">HIKVISION V2.3控件网页DEMO</div>
  <div id="TopMenu">
    <table width="100%" cellspacing="1" cellpadding="0" border="0" bgcolor="#96C3E4">
      <tr height="22">
        <td bgcolor="#ebebeb" class="distanceleft">登录信息</td>
      </tr>
      <tr bgcolor="#f5f5f5" height="30">
        <td class="distanceleft"> IP:
          <input type="text" id="DeviceIP" value="172.6.22.98" class="normalinput">
          port:
          <input type="text" id="DevicePort" value="8000" class="normalinput">
          user:
          <input type="text" id="DeviceUsername" value="admin" class="normalinput">
          pwd:
          <input type="text" id="DevicePasswd" value="12345" class="normalinput">
          <button class="normalbtn" onClick="ButtonPress('LoginDev')">注册</button>
          <button class="normalbtn" onClick="ButtonPress('LogoutDev')" style="margin-left:20px;">注销</button></td>
      </tr>
    </table>
  </div>
  <div id="MainBody">
    <div id="BodyLeft">
      <ul>
        <li>设备名称：
          <input type="text" name="DeviceName" id="DeviceName" class="longinput" readonly>
          <button class="normalbtn" onClick="ButtonPress('getDevName')">获取</button>
        </li>
        <li>通道列表：
          <select name="ChannelList" id="ChannelList" class="longinput">
            <!-- <option value="0">Camera 01</option>
                <option value="1">Camera 02</option>-->
          </select>
          <button class="normalbtn" onClick="ButtonPress('getDevChan')" >获取</button>
        </li>
        <li>
          <button class="normalbtn" onClick="ButtonPress('Preview:start')" style=" margin-left:60px;">&Delta;开始预览</button>
          <button class="normalbtn" onClick="ButtonPress('Preview:stop')" style=" margin-left:13px;">&nabla;停止预览</button>
        </li>
        <li>
          <button class="normalbtn" onClick="ButtonPress('CatPic:bmp')" style=" margin-left:60px;">抓BMP图</button>
          <button class="normalbtn" onClick="ButtonPress('CatPic:jpeg')" style=" margin-left:28px;">抓JPEG图</button>
        </li>
        <li>
          <button class="normalbtn" onClick="ButtonPress('Record:start')" style=" margin-left:60px;">开始录像</button>
          <button class="normalbtn" onClick="ButtonPress('Record:stop')" style=" margin-left:43px;">停止录像</button>
        </li>
        <li>
          <button class="normalbtn" onClick="ButtonPress('talk:start')" style=" margin-left:60px;">开始对讲</button>
          <button class="normalbtn" onClick="ButtonPress('talk:stop')" style=" margin-left:43px;">停止对讲</button>
        </li>
        <li>
          <button class="normalbtn" onClick="ButtonPress('voice:start')" style=" margin-left:60px;">打开声音</button>
          <button class="normalbtn" onClick="ButtonPress('voice:stop')" style=" margin-left:43px;">关闭声音</button>
        </li>
        <li>
          <button class="normalbtn" onClick="ButtonPress('playback:start')" style=" margin-left:60px;">按时间回放</button>
          <button class="normalbtn" onClick="ButtonPress('playback:stop')" style=" margin-left:25px;">停止回放</button>
        </li>
        <li>
          <button class="normalbtn" onClick="ButtonPress('downloadfile:start')" style=" margin-left:60px;">按文件下载</button>
          <button class="normalbtn" onClick="ButtonPress('downloadtime:start')" style=" margin-left:25px;">按时间下载</button>
        </li>
        <li>
          <button class="normalbtn" onClick="ButtonPress('downloadfile:stop')" style=" margin-left:60px;">停止下载</button>
        </li>
        <li> 云台控制 </li>
        <li>
          <table width="275" height="90" border="0" cellspacing="0" cellpadding="0">
            <tr>
              <td><table width="120" height="90" border="0" cellspacing="0" cellpadding="0">
                  <tr  align="center">
                    <td><button class="normalbtn" onClick="ButtonPress('PTZ:leftup')" style="width:30px;" >左上</button></td>
                    <td><button class="normalbtn" onClick="ButtonPress('PTZ:up')" style="width:30px;">上</button></td>
                    <td><button class="normalbtn" onClick="ButtonPress('PTZ:rightup')" style="width:30px;">右上</button></td>
                  </tr>
                  <tr  align="center">
                    <td><button class="normalbtn" onClick="ButtonPress('PTZ:left')" style="width:30px;" >左</button></td>
                    <td><button class="normalbtn" onClick="ButtonPress('PTZ:auto')" style="width:30px;">自转</button></td>
                    <td><button class="normalbtn" onClick="ButtonPress('PTZ:right')" style="width:30px;">右</button></td>
                  </tr>
                  <tr  align="center">
                    <td><button class="normalbtn" onClick="ButtonPress('PTZ:leftdown')" style="width:30px;" >左下</button></td>
                    <td><button class="normalbtn" onClick="ButtonPress('PTZ:down')" style="width:30px;">下</button></td>
                    <td><button class="normalbtn" onClick="ButtonPress('PTZ:rightdown')" style="width:30px;">右下</button></td>
                  </tr>
                </table></td>
              <td><button class="normalbtn" onClick="ButtonPress('PTZ:stop')" style="width:30px; background-color:#C00">停止</button></td>
              <td><table width="120" height="90" border="0" cellspacing="0" cellpadding="0">
                  <tr  align="center">
                    <td><button class="normalbtn" onClick="ButtonPress('zoom:in')" style="width:30px;" >+</button></td>
                    <td>焦距</td>
                    <td><button class="normalbtn" onClick="ButtonPress('zoom:out')" style="width:30px;">-</button></td>
                  </tr>
                  <tr  align="center">
                    <td><button class="normalbtn" onClick="ButtonPress('focus:in')" style="width:30px;" >+</button></td>
                    <td>焦点</td>
                    <td><button class="normalbtn" onClick="ButtonPress('focus:out')" style="width:30px;">-</button></td>
                  </tr>
                  <tr  align="center">
                    <td><button class="normalbtn" onClick="ButtonPress('iris:in')" style="width:30px;" >+</button></td>
                    <td>光圈</td>
                    <td><button class="normalbtn" onClick="ButtonPress('iris:out')" style="width:30px;">-</button></td>
                  </tr>
                </table></td>
            </tr>
          </table>
        </li>
        <li> 预置点：
          <select name="Preset" id="Preset"  class="longinput"  style="width:124px;">
            <option value="1">1</option>
            <option value="2">2</option>
            <option value="3">3</option>
            <option value="4">4</option>
            <option value="5">5</option>
            <option value="6">6</option>
            <option value="7">7</option>
            <option value="8">8</option>
            <option value="9">9</option>
            <option value="10">10</option>
          </select>
          <button class="normalbtn" onClick="ButtonPress('setPreset')">设置</button>
          <button class="normalbtn" onClick="ButtonPress('goPreset')" style="margin-left:10px;">调用</button>
        </li>
        <li> </li>
        <li> 图像参数 </li>
        <li> 亮&nbsp;&nbsp;&nbsp;&nbsp;度：
          <select name="PicLight" id="PicLight"  class="longinput">
            <option value="1">1</option>
            <option value="2">2</option>
            <option value="3">3</option>
            <option value="4">4</option>
            <option value="5">5</option>
            <option value="6">6</option>
            <option value="7">7</option>
            <option value="8">8</option>
            <option value="9">9</option>
            <option value="10">10</option>
          </select>
        </li>
        <li> 对比度：
          <select name="PicContrast" id="PicContrast"  class="longinput">
            <option value="1">1</option>
            <option value="2">2</option>
            <option value="3">3</option>
            <option value="4">4</option>
            <option value="5">5</option>
            <option value="6">6</option>
            <option value="7">7</option>
            <option value="8">8</option>
            <option value="9">9</option>
            <option value="10">10</option>
          </select>
          <button class="normalbtn" onClick="ButtonPress('getImagePar')">获取</button>
        </li>
        <li> 饱和度：
          <select name="PicSaturation" id="PicSaturation"  class="longinput">
            <option value="1">1</option>
            <option value="2">2</option>
            <option value="3">3</option>
            <option value="4">4</option>
            <option value="5">5</option>
            <option value="6">6</option>
            <option value="7">7</option>
            <option value="8">8</option>
            <option value="9">9</option>
            <option value="10">10</option>
          </select>
          <button class="normalbtn" onClick="ButtonPress('setImagePar')">设置</button>
        </li>
        <li> 色&nbsp;&nbsp;&nbsp;&nbsp;调：
          <select name="PicTonal" id="PicTonal"  class="longinput">
            <option value="1">1</option>
            <option value="2">2</option>
            <option value="3">3</option>
            <option value="4">4</option>
            <option value="5">5</option>
            <option value="6">6</option>
            <option value="7">7</option>
            <option value="8">8</option>
            <option value="9">9</option>
            <option value="10">10</option>
          </select>
        </li>
      </ul>
    </div>
    <div id="BodyRight" align="center">
      <div id="OCXBody">
        <div class="smallocxdiv" id="NetPlayOCX1">
          <object classid="CLSID:CAFCF48D-8E34-4490-8154-026191D73924" codebase="../codebase/NetVideoActiveX23.cab#version=2,3,19,1" standby="Waiting..." id="HIKOBJECT1" width="100%" height="100%" name="HIKOBJECT1" ></object>
        </div>
        <div class="smallocxdiv" id="NetPlayOCX2">
          <object classid="CLSID:CAFCF48D-8E34-4490-8154-026191D73924" standby="Waiting..." id="HIKOBJECT2" width="100%" height="100%" name="HIKOBJECT2" ></object>
        </div>
        <div class="smallocxdiv" id="NetPlayOCX3">
          <object classid="CLSID:CAFCF48D-8E34-4490-8154-026191D73924" standby="Waiting..." id="HIKOBJECT3" width="100%" height="100%" name="HIKOBJECT3" ></object>
        </div>
        <div class="smallocxdiv" id="NetPlayOCX4">
          <object classid="CLSID:CAFCF48D-8E34-4490-8154-026191D73924" standby="Waiting..." id="HIKOBJECT4" width="100%" height="100%" name="HIKOBJECT4" ></object>
        </div>
        <div style="float:right; display:none;"></div>
      </div>
      <div id="OperatLogTitle" align="left">日志（运行结果）<span style="margin-left:170px; cursor:pointer" onClick="ArrangeWindow(1)">一画面</span>
      	<span style="margin-left:20px; cursor:pointer" onClick="ArrangeWindow(4)">四画面</span>
      </div>
      <div id="OperatLogBody" align="left"></div>
    </div>
  </div>
  <div id="Foot" align="center">版权所有：杭州海康威视数字技术股份有限公司</div>
</div>
</body>
</html>
<script type="text/javascript">
/*************************************************
Function:		ArrangeWindow
Description:	画面分割为几个窗口
Input:			x : 窗口数目			
Output:			无
return:			无				
*************************************************/
function ArrangeWindow(x)
{
	var iMaxWidth = document.getElementById("OCXBody").offsetWidth;
	var iMaxHeight = document.getElementById("OCXBody").offsetHeight;
	for(var i = 1; i <= 4; i ++)
	{
		if(i <= x)
		{
			document.getElementById("NetPlayOCX" + i).style.display = "";
		}
		else
		{
			document.getElementById("NetPlayOCX" + i).style.display = "none";	
		}
	}
	var d = Math.sqrt(x);
	var iWidth = iMaxWidth/d;
	var iHight = iMaxHeight/d;
	for(var j = 1; j <= x; j ++)
	{
		document.getElementById("NetPlayOCX" + j).style.width = iWidth;
		document.getElementById("NetPlayOCX" + j).style.height = iHight;
	}
	if(x == 1)
	{

	}
	else if(x == 4)
	{
		
	}
	else
	{
		//	
	}
}
/*************************************************
Function:		ChangeStatus
Description:	选中窗口时，相应通道的状态显示
Input:			iWindowNum : 	选中窗口号		
Output:			无
return:			无				
*************************************************/
function ChangeStatus(iWindowNum)
{  
	m_bDVRControl = document.getElementById("HIKOBJECT" + iWindowNum);
	for(var i = 1; i <= 4; i ++)
	{
		if(i == iWindowNum)
		{
			document.getElementById("NetPlayOCX" + i).style.border = "1px solid #00F";
		}
		else
		{
			document.getElementById("NetPlayOCX" + i).style.border = "1px solid #EBEBEB";	
		}
	}
	LogMessage("当前选中窗口" + iWindowNum);
}
</script>