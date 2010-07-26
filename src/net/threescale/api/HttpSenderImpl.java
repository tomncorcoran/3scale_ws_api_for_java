package net.threescale.api;

import java.io.*;
import java.net.*;
import java.util.logging.*;

import org.w3c.dom.html.*;

/**
 * Sends requests to a server using Http.
 */
public class HttpSenderImpl implements HttpSender {
	
	private Logger log = LogFactory.getLogger(this);
	private HttpConnectionFactory factory;
	
	public HttpSenderImpl() {
		this.factory = new HttpConnectionFactoryImpl();
	}

	public HttpSenderImpl(HttpConnectionFactory factory) {
		this.factory = factory;
	}

	/**
	 * Send a POST message.
	 * @param hostUrl	Url and parameters to send to the server.
	 * @param postData	Data to be POSTed.
	 * @return	Transaction data returned from the server.
	 * @throws ApiException Error information.
	 */
	public ApiStartResponse sendPostToServer(String hostUrl, String postData)
			throws ApiException {
		HttpURLConnection con = null;
		try {
			log.info("Connecting to: " + hostUrl);

			con = factory.openConnection(hostUrl);
			log.info("Connected");
			
			con.setRequestMethod("POST");
			con.setRequestProperty("Content-Type",
					"application/x-www-form-urlencoded");

			OutputStreamWriter out = new OutputStreamWriter(con
					.getOutputStream());
			out.write(postData.toString());
			out.close();
			log.info("Written Post data");
			
			return new ApiStartResponse(extractContent(con), con.getResponseCode());
		}
		catch (ApiException ex) {
			throw ex;
		}
		catch (Exception ex) {
			if (con != null) {
				try {
					throw new ApiException(con.getResponseCode(),
							getErrorMessage(con));
				} catch (IOException e) {
					throw new ApiException(999, e.getMessage());
				}
			} else {
				throw new ApiException(999, "Error connecting to server");
			}
		}
	}

	/**
	 * Send a DELETE message to the server.
	 * @param hostUrl Url and parameters to send to the server.
	 * @return Http Response code.
	 * @throws ApiException Error Information.
	 */
	public int sendDeleteToServer(String hostUrl) throws ApiException {
		HttpURLConnection con = null;

		try {
			URL url = new URL(hostUrl);
			con = (HttpURLConnection)url.openConnection();
			con.setDoInput(true);
			con.setRequestMethod("DELETE");

			con.getInputStream(); // Ensure we try to read something
			return con.getResponseCode();
		} 
		catch (Exception ex) {
			if (con != null) {
					try {
						throw new ApiException(con.getResponseCode(), getErrorMessage(con));
					} catch (IOException e) {
						throw new ApiException(999, e.getMessage());
					}
			}
			else {
				throw new ApiException(999, "Error connecting to server");
			}
		}
	}

	
	
	private String extractContent(HttpURLConnection con) throws IOException {
		assert(con != null);
		InputStream inputStream = con.getInputStream();
		assert(inputStream != null);
		BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
		String line = null;
		StringBuffer response = new StringBuffer();

		while ((line = reader.readLine()) != null) {
			response.append(line);
		}
		reader.close();
		return response.toString();
	}

	private String getErrorMessage(HttpURLConnection con) throws IOException {
		assert(con != null);
		
		StringBuffer errStream = new StringBuffer();
		InputStream errorStream = con.getErrorStream();
		assert(errorStream != null);
		
		BufferedReader in = new BufferedReader(new InputStreamReader(errorStream));
		String errres;
		while ((errres = in.readLine()) != null) {
			errStream.append(errres);
		}
		in.close();
		return (errStream.toString());
	}

	private class HttpConnectionFactoryImpl implements HttpConnectionFactory {

		public HttpURLConnection openConnection(String hostUrl) throws MalformedURLException, IOException {
			URL url = new URL(hostUrl);
			HttpURLConnection con = (HttpURLConnection) url.openConnection();
			con.setDoOutput(true);
			con.setDoInput(true);
			return con;
		}
		
	}
}