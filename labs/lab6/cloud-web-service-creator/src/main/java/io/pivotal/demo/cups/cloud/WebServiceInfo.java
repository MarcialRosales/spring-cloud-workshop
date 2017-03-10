package io.pivotal.demo.cups.cloud;

import org.springframework.cloud.service.UriBasedServiceInfo;

public class WebServiceInfo extends UriBasedServiceInfo {

	public WebServiceInfo(String id, String uriString) {
		super(id, uriString);
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(id).append(" host:").append(getHost()).append(" port:").append(getPort())
			.append(" path:").append(getPath());
		return  sb.toString();
	}
}
