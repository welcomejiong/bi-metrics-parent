import static org.junit.Assert.assertTrue;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.reflect.FieldUtils;
import org.corps.bi.metrics.Dau;
import org.corps.bi.metrics.Install;
import org.corps.bi.metrics.converter.DauConverter;
import org.corps.bi.metrics.converter.InstallConverter;
import org.corps.bi.metrics.protobuf.InstallProto;
import org.corps.bi.tools.util.JSONUtils;
import org.junit.Test;

public class TestDefault {
	
	@Test
	public void testField() throws Exception {
		Dau dau=new Dau();
		Field userIdField=FieldUtils.getField(Dau.class, "userId",true);
		assertTrue("userId is or not exist!","userId".equals(userIdField.getName()));
		FieldUtils.writeField(userIdField, dau, "12345", true);
		assertTrue("userId is 12345?", "12345".equals(dau.getUserId()));
	}
	
	@Test
	public void testInstallConverter() throws Exception {
		int i=1;
		String ds="2020-04-21";
		String time="19:00:00";
		Install entity=new Install();
		entity.setClientId(i+"");
		
		entity.setAffiliate("affiliate_"+i);
		entity.setCreative("100reative_"+i);
		entity.setInstallDate(ds);
		entity.setInstallTime(time);
		entity.setFamily("family_"+i);
		entity.setFromUid("f_u_id-"+i);
		entity.setGenus("genus_"+i);
		entity.setSource("source_"+i);
		entity.setUserId("userid_"+i);
		entity.setRoleid("roleid_"+i);

		Map<String,String> extraMap=new HashMap<String,String>();
		extraMap.put("username", "username_"+i);
		extraMap.put("password", "password_"+i);
		entity.setExtra(JSONUtils.toJSON(extraMap));
		
		InstallConverter installConverter=new InstallConverter(entity);
		
		InstallProto installProto=installConverter.copyTo();
		System.out.println(installProto.toString());
		
		InstallConverter install2Converter=new InstallConverter(installProto.toByteArray());
		System.out.println(install2Converter.toString());
	}
	
	
	
	@Test
	public void testDauConverter() throws Exception {
		int i=2;
		String ds="2020-04-21";
		String time="19:00:00";
		Dau entity=new Dau();
		entity.setClientId(i+"");
		
		entity.setAffiliate("affiliate_"+i);
		entity.setCreative("100reative_"+i);
		entity.setDauDate(ds);
		entity.setDauTime(time);
		entity.setFamily("family_"+i);
		entity.setFromUid("f_u_id-"+i);
		entity.setGenus("genus_"+i);
		entity.setSource("source_"+i);
		entity.setUserId("userid_"+i);
		entity.setRoleid("roleid_"+i);

		Map<String,String> extraMap=new HashMap<String,String>();
		extraMap.put("username", "username_"+i);
		extraMap.put("password", "password_"+i);
		entity.setExtra(JSONUtils.toJSON(extraMap));
		
		DauConverter converter=new DauConverter(entity);
		
		System.out.println(DauConverter.class.getName()+"["+converter.copyTo().toString()+"]");
	}

}
