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

@Scope("prototype")
@Component("registVehcle")
@Entity
@Table(name = "TT_RegistVehcle")
public class RegistVehcle {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	private int id;
	
	@Column(name = "jylsh",length=50)
	private String jylsh;
	
	@Column(name = "jcxdh",length=50)
	private String jcxdh;
	
	@Column(name = "jycs",length=50)
	private String jycs;
	
	@Column(name = "hphm",length=50)
	private String hphm;
	
	@Column(name = "hpzl",length=50)
	private String hpzl;
	
	@Column(name = "clsbdh",length=50)
	private String clsbdh;
	
	@Column(name = "status")
	private int status;
	
	@Column(name = "xml" ,length=5000)
	private String xml;
	
	@Column(name = "createTime")
	private Date createTime;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getJylsh() {
		return jylsh;
	}

	public void setJylsh(String jylsh) {
		this.jylsh = jylsh;
	}

	public String getJcxdh() {
		return jcxdh;
	}

	public void setJcxdh(String jcxdh) {
		this.jcxdh = jcxdh;
	}

	public String getJycs() {
		return jycs;
	}

	public void setJycs(String jycs) {
		this.jycs = jycs;
	}

	public String getHphm() {
		return hphm;
	}

	public void setHphm(String hphm) {
		this.hphm = hphm;
	}

	public String getHpzl() {
		return hpzl;
	}

	public void setHpzl(String hpzl) {
		this.hpzl = hpzl;
	}

	public String getClsbdh() {
		return clsbdh;
	}

	public void setClsbdh(String clsbdh) {
		this.clsbdh = clsbdh;
	}

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	public String getXml() {
		return xml;
	}

	public void setXml(String xml) {
		this.xml = xml;
	}

	public Date getCreateTime() {
		return createTime;
	}

	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}
	
	
	
	
	



}
