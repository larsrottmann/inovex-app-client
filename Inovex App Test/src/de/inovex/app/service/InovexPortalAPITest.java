package de.inovex.app.service;

import java.io.IOException;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.http.client.ClientProtocolException;
import org.xml.sax.SAXException;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.test.AndroidTestCase;
import de.inovex.app.service.InovexPortalAPI.Employee;

public class InovexPortalAPITest extends AndroidTestCase {
	private InovexPortalAPI api;

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
		String user = prefs.getString("username", "");
		String pwd = prefs.getString("password", "");
		api = new InovexPortalAPI(user, pwd);
	}

	public void testGetAllEmployees() throws ClientProtocolException, IllegalStateException, IOException, SAXException, ParserConfigurationException {
		List<Employee> employees = api.getAllEmployees();
		assertEquals(62, employees.size());

		Employee test = employees.get(3);
		assertEquals("Andreas", test.givenName);
		assertEquals("Friedel", test.familyName);
		assertEquals("afri", test.symbol);
		assertEquals("AD", test.lob);
		assertEquals("0173 3181 034", test.numberMobile);
		assertNull(test.numberWork);
		assertEquals("Pforzheim", test.location);
		assertEquals("Spring, SCRUM, PostgreSQL, maven, Linux, JSP, JSON, Java, Liferay, hibernate..., Eclipse, etc. (siehe XING)", test.skills);
	}
}
