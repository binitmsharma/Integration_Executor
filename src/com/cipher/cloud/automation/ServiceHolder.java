package com.cipher.cloud.automation;

public class ServiceHolder {

	private String serviceName;
	private boolean isChecked;
	private boolean isEnabled;
	private String provisioned;

	public ServiceHolder() {

	}

	public ServiceHolder(String name, boolean checked, boolean enabled,
			String provisioned) {
		this.serviceName = name;
		this.isChecked = checked;
		this.isEnabled = enabled;
		this.provisioned = provisioned;
	}

	public String getServiceName() {
		return serviceName;
	}

	public void setServiceName(String serviceName) {
		this.serviceName = serviceName;
	}

	public boolean isChecked() {
		return isChecked;
	}

	public void setChecked(boolean isChecked) {
		this.isChecked = isChecked;
	}

	public boolean isEnabled() {
		return isEnabled;
	}

	public void setEnabled(boolean isEnabled) {
		this.isEnabled = isEnabled;
	}

	public String getProvisioned() {
		return provisioned;
	}

	public void setProvisioned(String provisioned) {
		this.provisioned = provisioned;
	}

	@Override
	public String toString() {
		return this.serviceName;
	}
}
