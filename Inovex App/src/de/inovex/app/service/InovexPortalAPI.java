package de.inovex.app.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.HTTP;
import org.xml.sax.SAXException;

public class InovexPortalAPI {
	public class Employee {
		public String givenName;
		public String familyName;
		public String symbol;
		public String lob;
		public String location;
		public String photoMD5;
		public String photoUrl;
		public String numberMobile;
		public String numberWork;
		public String numberHome;
		public String emailAddress;
		public String skills;

		@Override
		public String toString() {
			return "Employee [givenName=" + givenName + ", familyName="
					+ familyName + ", symbol=" + symbol + ", lob=" + lob
					+ ", location=" + location + ", photoMD5=" + photoMD5
					+ ", photoUrl=" + photoUrl + ", numberMobile="
					+ numberMobile + ", numberWork=" + numberWork
					+ ", numberHome=" + numberHome + ", emailAddress="
					+ emailAddress + ", skills=" + skills + "]";
		}
	}

	public class MySSLSocketFactory extends SSLSocketFactory {
		SSLContext sslContext = SSLContext.getInstance("TLS");

		public MySSLSocketFactory(KeyStore truststore) throws NoSuchAlgorithmException, KeyManagementException, KeyStoreException, UnrecoverableKeyException {
			super(truststore);

			TrustManager tm = new X509TrustManager() {
				@Override
				public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
				}

				@Override
				public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
				}

				@Override
				public X509Certificate[] getAcceptedIssuers() {
					return null;
				}
			};

			sslContext.init(null, new TrustManager[] { tm }, null);
		}

		@Override
		public Socket createSocket() throws IOException {
			return sslContext.getSocketFactory().createSocket();
		}

		@Override
		public Socket createSocket(Socket socket, String host, int port, boolean autoClose) throws IOException, UnknownHostException {
			return sslContext.getSocketFactory().createSocket(socket, host, port, autoClose);
		}
	}

	private final DefaultHttpClient httpClient;

	public InovexPortalAPI(String user, String pwd) {
		httpClient = getNewHttpClient();
		httpClient.getCredentialsProvider().setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(user, pwd));
		HttpParams params = httpClient.getParams();
		HttpConnectionParams.setConnectionTimeout(params, 10000);
	}

	private String emptyToNull(String group) {
		return group.length()==0?null:group;
	}

	public List<Employee> getAllEmployees() throws ClientProtocolException, IOException, IllegalStateException, SAXException, ParserConfigurationException {
		List<Employee> employees = new ArrayList<Employee>();

		HttpGet get = new HttpGet("https://portal.inovex.de/mitarbeiter/Lists/Mitarbeiter/Mitarbeiter.aspx");
		HttpResponse resp = httpClient.execute(get);
		if (resp.getStatusLine().getStatusCode() == 401) {
			// http auth fehlgeschlagen
			//TODO throw new HttpAuthorizationRequiredException();
		}

		StringBuilder total = new StringBuilder();
		BufferedReader rd = new BufferedReader(new InputStreamReader(resp.getEntity().getContent()));
		int chr;
		while ((chr = rd.read()) != -1) {
			total.append((char) chr);
		}

		// einzelne mitarbeiter per regex finden und exportieren
		Pattern p = Pattern.compile("<TR[^>]*><TD Class=\"ms-vb-user\"><table cellpadding=0 cellspacing=0 border=\"0\"><tr><td><a ONCLICK=\"GoToLink\\(this\\);return false;\" href=\"/mitarbeiter/_layouts/userdisp.aspx\\?ID=[^\"]+\"><IMG width=\"62\" height=\"62\" border=\"0\" SRC=\"([^\"]+)\" ALT=\"[^\"]*\"[ ]?>[ ]?</a></td></tr><tr><td class=\"ms-descriptiontext\"><table cellpadding=0 cellspacing=0 dir=\"\"><tr><td style=\"padding-right: 3px;\">.*?</td><td style=\"padding: 1px 0px 0px 0px;\" class=\"ms-vb\"><A ONCLICK=\"GoToLink\\(this\\);return false;\" HREF=\"/mitarbeiter/_layouts/userdisp.aspx\\?ID=[^\"]+\">([^<]+)</A></td></tr></table></td></tr></table></TD><TD Class=\"ms-vb-icon\"><a onfocus=\"OnLink\\(this\\)\" href=\"/mitarbeiter/Lists/Mitarbeiter/DispForm.aspx\\?ID=[^\"]+\" ONCLICK=\"GoToLink\\(this\\);return false;\" target=\"_self\"><IMG BORDER=0 ALT=\"\" title=\"\" SRC=\"/_layouts/images/icgen.gif\"></A></TD><TD Class=\"ms-vb2\">([^<]*)</TD><TD Class=\"ms-vb2\">([^<]*)</TD><TD Class=\"ms-vb2\"><A HREF=\"[^\"]*\">([^<]*)</A></TD><TD Class=\"ms-vb2\">([^<]*)</TD><TD Class=\"ms-vb2\">([^<]*)</TD><TD Class=\"ms-vb2\"><A HREF=\"[^\"]*\">([^<]*)</A></TD><TD Class=\"ms-vb2\"><NOBR>[^<]*</NOBR></TD><TD Class=\"ms-vb2\">[^<]*</TD></TR>", Pattern.CASE_INSENSITIVE);
		Matcher m = p.matcher(total);
		while (m.find()) {
			Employee emp = new Employee();
			emp.photoUrl = m.group(1);
			String fullname = m.group(2);
			emp.givenName = fullname.substring(0, fullname.lastIndexOf(' '));
			emp.familyName = fullname.substring(fullname.lastIndexOf(' ')+1);
			emp.symbol = emptyToNull(m.group(4));
			emp.lob = emptyToNull(m.group(5));
			emp.numberMobile = emptyToNull(m.group(6));
			emp.numberWork = emptyToNull(m.group(7));
			emp.location = emptyToNull(m.group(8));

			employees.add(emp);
		}


		return employees;
	}

	public DefaultHttpClient getNewHttpClient() {
		try {
			KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
			trustStore.load(null, null);

			SSLSocketFactory sf = new MySSLSocketFactory(trustStore);
			sf.setHostnameVerifier(SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);

			HttpParams params = new BasicHttpParams();
			HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
			HttpProtocolParams.setContentCharset(params, HTTP.UTF_8);

			SchemeRegistry registry = new SchemeRegistry();
			registry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
			registry.register(new Scheme("https", sf, 443));

			ClientConnectionManager ccm = new ThreadSafeClientConnManager(params, registry);

			return new DefaultHttpClient(ccm, params);
		} catch (Exception e) {
			return new DefaultHttpClient();
		}
	}
}
