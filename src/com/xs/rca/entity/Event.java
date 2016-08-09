package com.xs.rca.entity;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;


/**
 * 事件表
 */
@Scope("prototype")
@Component("events")
@Entity
@Table(name = "CheckEvents")
public class Event {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	private int id;
	
	@Column(name = "lsh",length=17)
	private String lsh;
	
	@Column(name = "checkEvent",length=64)
	private String event;
	
	@Column(name = "checkItem",length=64)
	private String checkItem;
	
	@Column(name = "createDate")
	private Date createDate;
	
	@Column(name = "state")
	private int state;
	
	@Column(name = "message",length=4000)
	private String message;
	
	@Column(name = "zpzl",length=10)
	private String zpzl;
	
	
	@Column(name = "hphm")
	private String hphm;
	
	@Column(name = "hpzl")
	private String hpzl;
	
	@Column(name = "clsbdh")
	private String clsbdh;
	
	
	
	

	
	public String getHphm() {
		return hphm;
	}

	public String getHpzl() {
		return hpzl;
	}

	public String getClsbdh() {
		return clsbdh;
	}

	public void setHphm(String hphm) {
		this.hphm = hphm;
	}

	public void setHpzl(String hpzl) {
		this.hpzl = hpzl;
	}

	public void setClsbdh(String clsbdh) {
		this.clsbdh = clsbdh;
	}

	public String getZpzl() {
		return zpzl;
	}

	public void setZpzl(String zpzl) {
		this.zpzl = zpzl;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getLsh() {
		return lsh;
	}

	public void setLsh(String lsh) {
		this.lsh = lsh;
	}

	public String getEvent() {
		return event;
	}

	public void setEvent(String event) {
		this.event = event;
	}

	public String getCheckItem() {
		return checkItem;
	}

	public void setCheckItem(String checkItem) {
		this.checkItem = checkItem;
	}

	public Date getCreateDate() {
		return createDate;
	}

	public void setCreateDate(Date createDate) {
		this.createDate = createDate;
	}

	public int getState() {
		return state;
	}

	public void setState(int state) {
		this.state = state;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}
	

}
