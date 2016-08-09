package com.xs.rca.manager;

import java.io.InputStream;
import java.sql.Blob;
import java.sql.SQLException;
import java.util.Map;

import javax.annotation.Resource;

import org.hibernate.Hibernate;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.transform.Transformers;
import org.springframework.context.annotation.Scope;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;
import org.springframework.stereotype.Service;

import sun.misc.BASE64Encoder;

@Scope("prototype")
@Service("imageDBManger")
public class ImageDBManger extends HibernateDaoSupport {

	@Resource(name = "sessionFactory")
	public void setBaseSessionFactory(SessionFactory sessionFactory) {
		super.setSessionFactory(sessionFactory);
	}

	public Map getImage(final String lsh, final String zpzl, final String jyxm) {

		final String sql = "select j.jclsh as jylsh,j.zp as zp,j.jycs as jycs , j.hphm as hphm ,j.hpzl as hpzl , j.clsbdh as clsbdh, j.pssj as pssj  from jdczpb j where jclsh=:jclsh and zpzl=:zpzl and jyxm=:jyxm order by pssj desc";
		Map zpMap = (Map) this.getHibernateTemplate().execute(
				new HibernateCallback<Object>() {
					@Override
					public Object doInHibernate(Session session)
							throws HibernateException, SQLException {
						return session
								.createSQLQuery(sql)
								.addScalar("zp", Hibernate.BLOB)
								.addScalar("jylsh", Hibernate.STRING)
								.addScalar("jycs", Hibernate.STRING)
								.addScalar("hphm", Hibernate.STRING)
								.addScalar("hpzl", Hibernate.STRING)
								.addScalar("clsbdh", Hibernate.STRING)
								.addScalar("pssj", Hibernate.STRING)
								.setString("jclsh", lsh)
								.setString("zpzl", zpzl)
								.setString("jyxm", jyxm)
								.setFirstResult(0)
								.setResultTransformer(
										Transformers.ALIAS_TO_ENTITY_MAP)
								.setMaxResults(1).uniqueResult();
					}
				});
		if (zpMap != null) {
			String imageCode = "";
			Blob imgObj = (Blob) zpMap.get("zp");

			if (imgObj != null) {
				BASE64Encoder encode = new BASE64Encoder();
				try {
					InputStream is = imgObj.getBinaryStream();
					byte[] b = new byte[ is.available()];
					is.read(b);
					imageCode = encode.encode(b);
				} catch (Exception e) {
					logger.error(e);
				}
			}
			zpMap.put("zp",imageCode);
		}
		return zpMap;
	}
}
