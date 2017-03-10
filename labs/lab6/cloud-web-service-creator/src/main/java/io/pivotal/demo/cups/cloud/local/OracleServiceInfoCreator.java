package io.pivotal.demo.cups.cloud.local;

import org.springframework.cloud.localconfig.LocalConfigServiceInfoCreator;
import org.springframework.cloud.service.UriBasedServiceData;
import org.springframework.cloud.service.common.OracleServiceInfo;

public class OracleServiceInfoCreator extends LocalConfigServiceInfoCreator<OracleServiceInfo> {

	public OracleServiceInfoCreator() {
		super(OracleServiceInfo.ORACLE_SCHEME);
	}

	@Override
	public boolean accept(UriBasedServiceData serviceData) {
		String uriString = serviceData.getUri();
		return uriString.startsWith("jdbc:oracle:");
	}

	@Override
	public OracleServiceInfo createServiceInfo(String id, String uri) {
		return new OracleServiceInfo(id, uri);
	}
}