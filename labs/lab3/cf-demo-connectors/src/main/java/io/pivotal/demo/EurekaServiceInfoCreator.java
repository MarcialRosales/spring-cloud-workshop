package io.pivotal.demo;

import java.util.Map;

public class EurekaServiceInfoCreator extends io.pivotal.spring.cloud.cloudfoundry.EurekaServiceInfoCreator {

	@Override
	public boolean accept(Map<String, Object> serviceData) {
		Map<String, Object> credentials = getCredentials(serviceData);
		return "eureka".equals(credentials.get("label"));
	}

}
