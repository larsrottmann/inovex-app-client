package de.inovex.app.service;

import java.io.IOException;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.http.client.ClientProtocolException;
import org.xml.sax.SAXException;

import android.test.AndroidTestCase;
import de.inovex.app.service.InovexPortalAPI.Employee;

public class InovexPortalAPITest extends AndroidTestCase {
	private InovexPortalAPI api;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		api = new InovexPortalAPI("jogehring", "xT93qb");
	}

	public void testGetAllEmployees() throws ClientProtocolException, IllegalStateException, IOException, SAXException, ParserConfigurationException {
		List<Employee> employees = api.getAllEmployees();
		assertEquals(68, employees.size());

		Employee test = employees.get(0);
		assertEquals("Alexander", test.givenName);
		assertEquals("Gabert", test.familyName);
		assertEquals("alga", test.symbol);
		assertEquals("BI", test.lob);
		assertEquals("0173 3181 028", test.numberMobile);
		assertNull(test.numberWork);
		assertEquals("Pforzheim", test.location);
	}
}
