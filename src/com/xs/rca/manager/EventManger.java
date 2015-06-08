package com.xs.rca.manager;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.transform.Transformers;
import org.springframework.context.annotation.Scope;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;
import org.springframework.stereotype.Service;

import com.xs.rca.entity.Codes;
import com.xs.rca.entity.SBRZ;

@Scope("prototype")
@Service("eventManger")
public class EventManger extends HibernateDaoSupport {

	@Resource(name = "sessionFactory")
	public void setBaseSessionFactory(SessionFactory sessionFactory) {
		super.setSessionFactory(sessionFactory);
	}

	public List<?> queryEntity(final String entity) {

		List<?> entitys = this.getHibernateTemplate().execute(
				new HibernateCallback<List<?>>() {
					@Override
					public List<?> doInHibernate(Session session)
							throws HibernateException, SQLException {
						List<?> entitys = session
								.createQuery(
										"From " + entity
												+ " where rccysbzt='0'")
								.setFirstResult(0).setMaxResults(1).list();
						return entitys;
					}
				});
		return entitys;
	}

	public List<?> getEvents() {
		return this.getHibernateTemplate().execute(
				new HibernateCallback<List<?>>() {
					@Override
					public List<?> doInHibernate(Session session)
							throws HibernateException, SQLException {
						List<?> entitys = session
								.createQuery(
										" from Event where state=0 order by  id asc")
								.setFirstResult(0).setMaxResults(100).list();
						return entitys;
					}
				});
	}

	public Integer saveLog(SBRZ rz) {
		Integer sid = (Integer) this.getHibernateTemplate().save(rz);
		return sid;
	}

	public void update(Object entity) {
		this.getHibernateTemplate().update(entity);
	}

	public void delete(Object entity) {
		this.getHibernateTemplate().delete(entity);
	}

	public void save(Object entity) {
		this.getHibernateTemplate().save(entity);
	}

	public List<Codes> getCodes() {
		DetachedCriteria dc = DetachedCriteria.forClass(Codes.class);
		return this.getHibernateTemplate().findByCriteria(dc);
	}

	public String getDetLineSelect(final String code) {
		String line = this.getHibernateTemplate().execute(
				new HibernateCallback<String>() {
					@Override
					public String doInHibernate(Session session)
							throws HibernateException, SQLException {
						String sql = "SELECT DetLineSelect from registbak where regist_code=:code ";
						Object line = session.createSQLQuery(sql)
								.setString("code", code).uniqueResult();
						return (String) line;
					}
				});
		return line;
	}
		
	

}
