/*
 * Copyright 2005 PayPal, Inc. All Rights Reserved.
 */

package com.paypal.sdk.core.nvp;

import java.io.File;
import java.net.URL;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import javax.xml.namespace.QName;

import org.apache.commons.httpclient.*;
import org.apache.commons.httpclient.protocol.*;
import org.apache.commons.httpclient.params.*;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.paypal.sdk.core.APICallerBase;
import com.paypal.sdk.core.Constants;
import com.paypal.sdk.exceptions.FatalException;
import com.paypal.sdk.exceptions.PayPalException;
import com.paypal.sdk.exceptions.TransactionException;
import com.paypal.sdk.exceptions.WarningException;
import com.paypal.sdk.profiles.APIProfile;
import com.paypal.sdk.profiles.CertificateAPIProfile;
import com.paypal.sdk.profiles.SignatureAPIProfile;
import com.paypal.sdk.util.MessageResources;
import com.paypal.sdk.util.SDKResources;
import com.paypal.sdk.util.Util;

/**
 * NVP API caller class.
 */
public class NVPAPICaller extends APICallerBase
{
	private static Log log = LogFactory.getLog(NVPAPICaller.class);

	/**
	 * PayPal NVP endopoint URL
	 */
	private URL url;

	/**
	 * PayPal NVP security header
	 */
	private String header;

	/**
	 * HTTP connection manager parameters
	 */
	private HttpConnectionManagerParams params;

	/**
	 * HTTPS protocol handler
	 */
	private Protocol myhttps;

	/**
	 * Maximum number of retries
	 */
	private int maximumRetries;

	/**
	 * PayPal NVP service name
	 */
	private static final QName service = new QName("PayPalAPI");

	static
	{
		readEndpoints(Constants.NVP_ENDPOINTS);
	}

	/**
	 * This method validates the API profile for a NVP connection.
	 *
	 * @param _profile          the profile object set by the merchant or from samples
	 * @return                  void
	 * @throws PayPalException  if an exception occurs.
	 */
	protected void validateProfile(APIProfile _profile) throws PayPalException
	{
		List errors = new ArrayList();
		if (Util.isEmpty(_profile.getEnvironment()))
		{
			errors.add(MessageResources.getMessage("API_ENVIRONMENT_EMPTY"));
		}
		if (_profile instanceof CertificateAPIProfile)
		{
			if (Util.isEmpty(_profile.getCertificateFile()))
			{
				errors.add(MessageResources.getMessage("API_CERTIFICATE_FILE_EMPTY"));
			}
			else
			{
				File file = new File(_profile.getCertificateFile());
				if (!file.exists())
				{
					errors.add(MessageResources.getMessage("API_CERTIFICATE_FILE_MISSING"));
				}
			}
			if ((_profile.getPrivateKeyPassword() == null))
			{
				errors.add(MessageResources.getMessage("API_PRIVATE_KEY_PASSWORD_EMPTY"));
			}
		}
		if (!errors.isEmpty())
		{
			StringBuffer msg = new StringBuffer(MessageResources.getMessage("PROFILE_INVALID"));
			Iterator iterator = errors.iterator();
			while (iterator.hasNext())
			{
				msg.append("\n" + ((String) iterator.next()));
			}
			throw new TransactionException(msg.toString());
		}
	}

	/**
	 * This method creates a SSL connection to URL taken from the paypal-endpoints.xml. It takes
	 * appropriate URL based on the type of authentication preferred Certificate or 3 Token method.
	 *
	 * @param _profile          the profile object set by the merchant or from samples
	 * @return                  void
	 * @throws PayPalException  if an exception occurs.
	 */
	public final synchronized void setupConnection(APIProfile _profile) throws PayPalException
	{
		super.setupConnection(_profile);

		// Retrieve the endpoint URL
		String endpointUrl = this.getEndpointUrl(_profile, service);

		if (Util.isEmpty(endpointUrl))
			throw new TransactionException(MessageFormat.format(MessageResources
					.getMessage("ENDPOINT_NOT_FOUND"), new Object[] {
				_profile.getEnvironment(), service.getLocalPart()}));

		try
		{
			url = new URL(endpointUrl);
		}
		catch (Exception e)
		{
			throw new TransactionException(MessageFormat.format(MessageResources
					.getMessage("ENDPOINT_INVALID"), new Object[] {endpointUrl}));
		}

		if (log.isDebugEnabled())
		{
			log.debug(MessageFormat.format(MessageResources.getMessage("CONNECTION_OPEN"),
					new Object[] {service.getLocalPart(), endpointUrl}));
		}

		// Create the security header
		NVPEncoder encoder = new NVPEncoder();
		if (!Util.isEmpty(_profile.getAPIUsername()))
		{
			encoder.add("USER", _profile.getAPIUsername());
		}
		if (!Util.isEmpty(_profile.getAPIPassword()))
		{
			encoder.add("PWD", _profile.getAPIPassword());
		}
		if (!Util.isEmpty(_profile.getSubject()))
		{
			encoder.add("SUBJECT", _profile.getSubject());
		}
		if ((_profile instanceof SignatureAPIProfile) && !Util.isEmpty(_profile.getSignature()))
		{
			encoder.add("SIGNATURE", _profile.getSignature());
		}
		encoder.add("VERSION", Constants.DEFAULT_API_VERSION);
		encoder.add("SOURCE", Constants.API_SOURCE);
		header = encoder.encode();

		// Set the connection parameters
		params = new HttpConnectionManagerParams();
		if (_profile.getTimeout() > 0)
		{
			params.setConnectionTimeout(_profile.getTimeout());
		}
		maximumRetries = _profile.getMaximumRetries();

		// Set the HTTPS protocol handler
		myhttps = new Protocol("https", new NVPSSLSocketFactory(String.valueOf(this.hashCode())), 443);
	} // setupConnection

	/**
	 * This method invokes all the API calls depending on the request string sent in as parameter
	 * in this method which has to be in NVP format.
	 *
	 * @param payload              Payload string
	 * @return String              Response String from the server in the NVP format.
	 * @exception PayPalException  If exception occurs
	 */
	public final String call(String payload) throws PayPalException
	{
		if (url == null) throw new WarningException(MessageResources.getMessage("NO_PROFILE_SET"));

		PostMethod httppost = null;
		try
		{
			SimpleHttpConnectionManager manager = new SimpleHttpConnectionManager();
			manager.setParams(params);

			HttpClient httpclient = new HttpClient(manager);
			httpclient.getHostConfiguration().setHost(url.getHost(), 443, myhttps);

			// Set the connection time
			int timeout = params.getConnectionTimeout();
			if (timeout != 0)
			{
				httpclient.setConnectionTimeout(timeout);
			}

			// Set the retry handler
			httpclient.getParams().setParameter(HttpMethodParams.RETRY_HANDLER,
					new DefaultHttpMethodRetryHandler(maximumRetries, false));

			// Set the proxy
			if ("true".equalsIgnoreCase((String)System.getProperties().get("https.proxySet")))
			{
				String host=(String) System.getProperties().get("https.proxyHost");
				int port=Integer.parseInt((String)System.getProperties().get("https.proxyPort"));
				httpclient.getHostConfiguration().setProxy(host,port);
				String username= (String) System.getProperties().get("https.proxyUser");
				String password=(String) System.getProperties().get("https.proxyPassword");
				if(username!=null&&password!=null)
				{
					Credentials userCredential = new UsernamePasswordCredentials(username,password);
					httpclient.getState().setProxyCredentials( new AuthScope(host,port),userCredential);
				}
			}

			httppost = new PostMethod(url.getPath());
			Date startTime = new Date();

			StringBuffer request = new StringBuffer(payload);
			request.append("&");
			request.append(header);
			httppost.setRequestEntity(new StringRequestEntity(request.toString(), "text/html", "UTF-8"));

			if (log.isInfoEnabled())
			{
				NVPDecoder decoder = new NVPDecoder();
				String requestmask = request.toString();
				decoder.decode(requestmask);
				if (!Util.isEmpty(decoder.get("PWD")))
				{
					requestmask = requestmask.replaceAll(decoder.get("PWD"), "******");
				}
				if (!Util.isEmpty(decoder.get("SIGNATURE")))
				{
					requestmask = requestmask.replaceAll(decoder.get("SIGNATURE"), "**********");
				}
				if (!Util.isEmpty(decoder.get("CVV2")))
				{
					requestmask = requestmask.replaceAll(decoder.get("CVV2"), "****");
				}
				if (!Util.isEmpty(decoder.get("ACCT")))
				{
					requestmask = requestmask.replaceAll(decoder.get("ACCT"), "****************");
				}
				log.info(MessageFormat.format(MessageResources.getMessage("TRANSACTION_SENT"),
						new Object[] {requestmask}));
			}

			int result = httpclient.executeMethod(httppost);
			String response = httppost.getResponseBodyAsString();

			// Log the transaction result
			if (log.isInfoEnabled())
			{
				Date endTime = new Date();
				log.info(MessageFormat.format(
						MessageResources.getMessage("TRANSACTION_RESULT"), new Object[] {
							response, String.valueOf(result),
							new Long(endTime.getTime() - startTime.getTime())}));
			}
			return response;
		}
		catch (Exception e)
		{
			throw new FatalException(MessageResources.getMessage("TRANSACTION_FAILED"), e);
		}
		finally
		{
			httppost.releaseConnection();
		}
	} // call
}