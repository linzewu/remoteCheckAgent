package com.xs.rca.util;

import java.util.List;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import com.xs.rca.entity.Codes;
import com.xs.rca.manager.CheckedInfoManger;

/**
 * Application Lifecycle Listener implementation class RCAListener
 *
 */
public class RCAListener implements ServletContextListener {
	
	protected static Log log = LogFactory.getLog(RCAListener.class);
	
	private WebApplicationContext wac;

    public RCAListener() {
    }

    public void contextInitialized(ServletContextEvent contextEvent) {
    }

    public void contextDestroyed(ServletContextEvent arg0) {
    }
	
}
