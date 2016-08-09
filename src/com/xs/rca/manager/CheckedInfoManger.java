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
@Service("checkedInfoManger")
public class CheckedInfoManger extends HibernateDaoSupport {

	@Resource(name = "sessionFactory")
	public void setBaseSessionFactory(SessionFactory sessionFactory) {
		super.setSessionFactory(sessionFactory);
	}


	
	public List<?> getViewData(final String viewName,final String jylsh) {
		return this.getHibernateTemplate().execute(
				new HibernateCallback<List<?>>() {
					@Override
					public List<?> doInHibernate(Session session)
							throws HibernateException, SQLException {
						List<?> entitys = session
								.createSQLQuery("select * from "+viewName + "  where RegistNo=:jylsh")
								.setString("jylsh",jylsh)
								.setFirstResult(0)
								.setMaxResults(1)
								.setResultTransformer(
										Transformers.ALIAS_TO_ENTITY_MAP).list();
						return entitys;
					}
				});
	}
	
	
	public Map getRgjyxmjg(final String jylsh){
		
		return this.getHibernateTemplate().execute(new HibernateCallback<Map>() {

			@Override
			public Map doInHibernate(Session session) throws HibernateException, SQLException {
				
				List<?> entitys = session
						.createSQLQuery("select * from  OutSide_Result_Anjian_Bak  where RegistNo=:jylsh")
						.setString("jylsh",jylsh)
						.setFirstResult(0)
						.setMaxResults(1)
						.setResultTransformer(
								Transformers.ALIAS_TO_ENTITY_MAP).list();
				
				if(entitys!=null&&!entitys.isEmpty()){
					return (Map)entitys.get(0);
				}else{
					return null;
				}
			}
			
		});
	}
	
	public Map getYqsbjyjg(final String jylsh){
		
		return this.getHibernateTemplate().execute(new HibernateCallback<Map>() {

			@Override
			public Map doInHibernate(Session session) throws HibernateException, SQLException {
				
				List<?> entitys = session
						.createSQLQuery("select * from  DetReportResult_JS_AnJian  where RegistNo=:jylsh")
						.setString("jylsh",jylsh)
						.setFirstResult(0)
						.setMaxResults(1)
						.setResultTransformer(
								Transformers.ALIAS_TO_ENTITY_MAP).list();
				
				if(entitys!=null&&!entitys.isEmpty()){
					return (Map)entitys.get(0);
				}else{
					return null;
				}
			}
			
		});
	}
	
	/**
	 * 获取结果 标准限值
	 * 
	 * 参数 是报告号
	 * @param jylsh
	 * @return
	 */
	public Map getBzxz(final String jylsh){
		
		return this.getHibernateTemplate().execute(new HibernateCallback<Map>() {

			@Override
			public Map doInHibernate(Session session) throws HibernateException, SQLException {
				
				List<?> entitys = session
						.createSQLQuery("select * from  DetReportYQJC_LimitValue  where DocRegist=:jylsh")
						.setString("jylsh",jylsh)
						.setFirstResult(0)
						.setMaxResults(1)
						.setResultTransformer(
								Transformers.ALIAS_TO_ENTITY_MAP).list();
				
				if(entitys!=null&&!entitys.isEmpty()){
					return (Map)entitys.get(0);
				}else{
					return null;
				}
			}
			
		});
	}
	
}
