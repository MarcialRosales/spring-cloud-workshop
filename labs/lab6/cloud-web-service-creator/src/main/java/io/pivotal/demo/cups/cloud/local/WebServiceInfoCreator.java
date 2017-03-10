package io.pivotal.demo.cups.cloud.local;

import org.springframework.cloud.localconfig.LocalConfigServiceInfoCreator;

import io.pivotal.demo.cups.cloud.WebServiceInfo;

public class WebServiceInfoCreator extends LocalConfigServiceInfoCreator<WebServiceInfo> {

	@SuppressWarnings("unused")
	private static final String WEB_SERVICE = "WebService";
	
	public WebServiceInfoCreator() {
		super("http");		
	}


	@Override
	public WebServiceInfo createServiceInfo(String id, String uri) {
		return new WebServiceInfo(id, uri);
	}
}
