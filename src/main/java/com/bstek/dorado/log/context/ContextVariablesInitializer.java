package com.bstek.dorado.log.context;

import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang.StringUtils;

import com.bstek.dorado.core.Configure;
import com.bstek.dorado.core.el.ContextVarsInitializer;
import com.bstek.dorado.data.entity.EntityEnhancer;
import com.bstek.dorado.data.entity.EntityUtils;
import com.bstek.dorado.data.type.EntityDataType;
import com.bstek.dorado.data.type.SimpleDataType;
import com.bstek.dorado.data.type.property.PropertyDef;
import com.bstek.dorado.log.Constants;

public class ContextVariablesInitializer implements ContextVarsInitializer{

	public void initializeContext(Map<String, Object> contextVarsMap)
			throws Exception {
		contextVarsMap.put("log", this);
		
	}
	
	public String modifyInfo(Object entity) {
		StringBuilder retVal = new StringBuilder();
		EntityEnhancer entityEnhancer = EntityUtils.getEntityEnhancer(entity);
		if (entityEnhancer != null) {
			Map<String, Object> oldValues = entityEnhancer.getOldValues();
			EntityDataType dataType =entityEnhancer.getDataType();
			if (oldValues != null && dataType != null) {
				for (Entry<String, Object> entry : oldValues.entrySet()) {
					String property = entry.getKey();
					Object oldValue = entry.getValue();
					Object newValue = EntityUtils.getValue(entity, property);
					PropertyDef df = dataType.getPropertyDef(property);
					if (df != null && (df.getDataType() == null || df.getDataType() instanceof SimpleDataType)) {
						 if (isChanged(newValue, oldValue)) {
								String label = df.getLabel();
								String dataTypeId = "String";
								if (StringUtils.isEmpty(label)) {
									label = property;
								}
								if (df.getDataType() != null) {
									dataTypeId = df.getDataType().getId();
								}
								String format = Configure.getString("log.format" + dataTypeId);
								if (StringUtils.isEmpty(format)) {
									format = Configure.getString(Constants.DEFAULT_FORMAT);
								}
								retVal.append(String.format(format, label, oldValue, newValue));
						}
								
					}
					
				}
			}
		}
		return retVal.toString();
	}
	
	
	private boolean isChanged(Object newValue, Object oldValue){
		if(oldValue != null || newValue != null) {
			if(!(oldValue != null && newValue !=null && oldValue.equals(newValue))) {
				return true;
			}
		}
		return false;
	} 
	
}