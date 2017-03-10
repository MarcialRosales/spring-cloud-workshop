package io.pivotal.demo.cups.cloud.cf;

import java.util.Map;

import org.springframework.cloud.cloudfoundry.CloudFoundryServiceInfoCreator;
import org.springframework.cloud.cloudfoundry.Tags;

import io.pivotal.demo.cups.cloud.WebServiceInfo;

public class WebServiceInfoCreator extends CloudFoundryServiceInfoCreator<WebServiceInfo> {

	@SuppressWarnings("unused")
	private static final String WEB_SERVICE = "WebService";
	
	public WebServiceInfoCreator() {
		super(new Tags(WEB_SERVICE));		
	}

	@Override
	public boolean accept(Map<String, Object> serviceData) {
		String tag = getStringFromCredentials(getCredentials(serviceData), "tag");
		return super.accept(serviceData) || WEB_SERVICE.equals(tag);
	}

	@Override
	public WebServiceInfo createServiceInfo(Map<String, Object> serviceData) {
		String id = (String) serviceData.get("name");
		String url = getStringFromCredentials(getCredentials(serviceData), "uri", "url");
		return new WebServiceInfo(id, url);
	}
}
