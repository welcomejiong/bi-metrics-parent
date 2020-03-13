import static org.junit.Assert.assertTrue;

import java.lang.reflect.Field;

import org.apache.commons.lang3.reflect.FieldUtils;
import org.corps.bi.metrics.Dau;
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

}
