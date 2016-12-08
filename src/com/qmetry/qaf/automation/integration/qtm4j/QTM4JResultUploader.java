/*******************************************************************************
 * QMetry Automation Framework provides a powerful and versatile platform to author 
 * Automated Test Cases in Behavior Driven, Keyword Driven or Code Driven approach
 *                
 * Copyright 2016 Infostretch Corporation
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR
 * OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT
 * OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE
 *
 * You should have received a copy of the GNU General Public License along with this program in the name of LICENSE.txt in the root folder of the distribution. If not, see https://opensource.org/licenses/gpl-3.0.html
 *
 * See the NOTICE.TXT file in root folder of this source files distribution 
 * for additional information regarding copyright ownership and licenses
 * of other open source software / files used by QMetry Automation Framework.
 *
 * For any inquiry or need additional information, please contact support-qaf@infostretch.com
 *******************************************************************************/


package com.qmetry.qaf.automation.integration.qtm4j;

import java.io.File;
import java.io.IOException;

import javax.ws.rs.core.MediaType;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.ISuite;
import org.testng.ISuiteListener;

import com.qmetry.qaf.automation.core.ConfigurationManager;
import com.qmetry.qaf.automation.keys.ApplicationProperties;
import com.qmetry.qaf.automation.util.StringUtil;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import com.sun.jersey.core.header.FormDataContentDisposition;
import com.sun.jersey.multipart.FormDataMultiPart;
import com.sun.jersey.multipart.MultiPart;
import com.sun.jersey.multipart.file.FileDataBodyPart;
import com.sun.jersey.multipart.impl.MultiPartWriter;

/**
 * Use this class to upload QAF Results to QMetry Test Manager For Jira.
 * To enable integration with QTM4J, specify below properties.<br />
 * <b>integration.param.qtm4j.enabled</b>=true<br/>
 * <b>integration.param.qtm4j.apikey</b>="Your API KEY generated from qtm4j"<br>
 * <b>integration.param.qtm4j.baseurl</b>=QTM4J Endpoint url (default is
 * https://qtmcloud.qmetry.com/internal/importResults.do)
 * 
 * @author amit.bhoraniya
 */
public class QTM4JResultUploader implements ISuiteListener {

	public static final String INTEGRATION_EANBLED = "integration.param.qtm4j.enabled";
	public static final String API_KEY = "integration.param.qtm4j.apikey";
	public static final String API_ENDPOINT = "integration.param.qtm4j.baseurl";
	public static final String API_ENDPOINT_DEFAULT =
			"https://qtmcloud.qmetry.com/internal/importResults.do";

	private final static Log logger = LogFactory.getLog(QTM4JResultUploader.class);

	@Override
	public void onStart(ISuite suite) {
	}

	@Override
	public void onFinish(ISuite suite) {

		// If integration not enabled then return
		if (!isEnabled()) {
			logger.info("QTM4J Integration is not enabled. To enable it set "
					+ INTEGRATION_EANBLED + "=true");
			return;
		}

		logger.info("Start to upload result to QTM4J");
		try {
			String dirName = ApplicationProperties.JSON_REPORT_DIR.getStringVal();
			String zipFile = dirName + ".zip";
			File fileToUpload = new File(zipFile);
			try {

				logger.info("Compressing Result Directory");
				ZipUtils.zipDirectory(dirName, zipFile);

				ClientConfig cc = new DefaultClientConfig();
				cc.getClasses().add(MultiPartWriter.class);
				Client c = Client.create(cc);
				WebResource r = c.resource(ConfigurationManager.getBundle()
						.getString(API_ENDPOINT, API_ENDPOINT_DEFAULT));
				FileDataBodyPart fileDataBodyPart = new FileDataBodyPart("file",
						fileToUpload, MediaType.APPLICATION_OCTET_STREAM_TYPE);
				fileDataBodyPart.setContentDisposition(FormDataContentDisposition
						.name("file").fileName(fileToUpload.getName()).build());
				final MultiPart multiPart =
						new FormDataMultiPart()
								.field("apiKey",
										ConfigurationManager.getBundle()
												.getString(API_KEY, ""))
								.field("format", "qas/json")
								.field("testRunName", suite.getName())
								.field("filename", fileToUpload.getName(),
										MediaType.TEXT_PLAIN_TYPE)
								.bodyPart(fileDataBodyPart);
				multiPart.setMediaType(MediaType.MULTIPART_FORM_DATA_TYPE);
				ClientResponse response = r.type(MediaType.MULTIPART_FORM_DATA_TYPE)
						.post(ClientResponse.class, multiPart);
				logger.info(
						"Result Uploader Response:" + response.getEntity(String.class));
				c.destroy();
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				fileToUpload.delete();
			}
		} catch (Throwable e) {
			e.printStackTrace();
		}
		logger.info("Exiting QTM4J Result Uploader");
	}

	/**
	 * @return required properties is set or not to enable integration with
	 *         qtm4j
	 */
	public boolean isEnabled() {
		if (ConfigurationManager.getBundle().getBoolean(INTEGRATION_EANBLED, false)
				&& StringUtil.isNotBlank(
						ConfigurationManager.getBundle().getString(API_KEY, "")))
			return true;
		return false;
	}

}
