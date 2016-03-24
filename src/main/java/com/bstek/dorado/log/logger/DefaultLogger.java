package com.bstek.dorado.log.logger;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.springframework.transaction.annotation.Transactional;

import com.bstek.dorado.dao.BeanUtils;
import com.bstek.dorado.dao.FieldUtils;
import com.bstek.dorado.dao.GenricTypeUtils;
import com.bstek.dorado.dao.hibernate.HibernateUtils;
import com.bstek.dorado.data.entity.EntityState;
import com.bstek.dorado.data.entity.EntityUtils;
import com.bstek.dorado.log.annotation.LogDefinition;
import com.bstek.dorado.log.context.ContextHandler;
import com.bstek.dorado.log.context.provider.ContextProvider;
import com.bstek.dorado.log.model.LogInfo;
import com.bstek.dorado.web.DoradoContext;

/**
 *@author Kevin.yang
 *@since 2015年7月20日
 */
public class DefaultLogger implements Logger {

	protected ContextHandler contextHandler;
	private String sessionFactoryName;
	
	@SuppressWarnings("rawtypes")
	@Override
	@Transactional
	public void log() {
		Object obj = contextHandler.get(ContextProvider.TARGET);
		LogDefinition log = (LogDefinition) contextHandler.get(ContextProvider.LOG_DEFINITION);
		if (obj instanceof Collection) {
			Collection target = (Collection) obj;
			for (Object item : target) {
				setContext(log, item);
				doLog(log);
				doChildernLog(item, log);
			}
		} else if (obj != null) {
			setContext(log, obj);
			doLog(log);
			doChildernLog(obj, log);
		} else {
			doLog(log);
		}

	}
	
	protected void doChildernLog(Object parent, LogDefinition log) {
		if (log.isRecursive()) {
			List<Field> fields = FieldUtils.getFields(parent.getClass());
			for (Field field : fields) {
				Object value = BeanUtils.getFieldValue(parent, field);
				Class<?> pt = field.getType();
				if (!EntityUtils.isSimpleValue(value)) {
					if (Collection.class.isAssignableFrom(pt)) {
						Class<?> gt = GenricTypeUtils.getGenricType(field);
						if (gt != null && HibernateUtils.isEntityClass(gt)) {
							contextHandler.set(ContextProvider.TARGET, value);
							log();
						}
					} else if(HibernateUtils.isEntityClass(pt)) {
						contextHandler.set(ContextProvider.TARGET, value);
						log();
					}
				}
			}
		}
	}

	protected void doLog(LogDefinition log) {
		boolean disabled = contextHandler.compile(log.getDisabled());
		if (!disabled) {
			Object logInfo = getLogInfo(log);
			HibernateUtils.getSessionFactory(sessionFactoryName)
				.getCurrentSession().save(logInfo);
			
		}
		
	}

	protected Object getLogInfo(LogDefinition log) {
		LogInfo logInfo = new LogInfo();
		logInfo.setId(UUID.randomUUID().toString());
		logInfo.setCategory((String) contextHandler.compile(log.getCategory()));
		logInfo.setDesc((String) contextHandler.compile(log.getDesc()));
		logInfo.setIP((String) contextHandler.compile(DoradoContext.getCurrent().getRequest().getRemoteAddr()));
		logInfo.setModule((String) contextHandler.compile(log.getModule()));
		logInfo.setOperation((String) contextHandler.compile(log.getOperation()));
		logInfo.setOperationDate(new Date());
		logInfo.setOperationUser((String) contextHandler.compile(log.getOperationUser()));
		logInfo.setSource(DoradoContext.getCurrent().getRequest().getHeader("Referer"));
		logInfo.setTitle((String) contextHandler.compile(log.getTitle()));
		
		return logInfo;
	}

	protected void setContext(LogDefinition log, Object entity) {
		EntityState state = EntityUtils.getState(entity);
		contextHandler.set(log.getVar(), entity);
		contextHandler.set(ContextProvider.ENTITY_STATE, state.name());
	}

	public void setContextHandler(ContextHandler contextHandler) {
		this.contextHandler = contextHandler;
	}

	public void setSessionFactoryName(String sessionFactoryName) {
		this.sessionFactoryName = sessionFactoryName;
	}

}
