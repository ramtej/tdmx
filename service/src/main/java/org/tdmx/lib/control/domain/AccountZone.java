package org.tdmx.lib.control.domain;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * An AccountZone describes a Zone at a ServiceProvider and points to the Zone's
 * DatabasePartition.
 * 
 * The ServiceProvider may control the authorization state of a Zone independently
 * of the Account's authorization state.
 * 
 * @author Peter Klauser
 *
 */
@Entity
@Table(name="AccountZone")
public class AccountZone implements Serializable {

	public static final int MAX_ZONEAPEX_LEN = 255;
	public static final int MAX_SEGMENT_LEN = 16;
	
	private static final long serialVersionUID = -988419614813872556L;

	@Id
	@Column(length = MAX_ZONEAPEX_LEN)
	private String zoneApex;
	
	@Enumerated(EnumType.STRING)
	@Column(length = AuthorizationStatus.MAX_AUTHORIZATIONSTATUS_LEN, nullable = false)
	private AuthorizationStatus authorizationStatus;
	
	@Column(length = Account.MAX_ACCOUNTID_LEN, nullable = false)
	private String accountId;
	
	@Column(length = MAX_SEGMENT_LEN, nullable = false)
	private String segment;
	
	@Column(length = DatabasePartition.MAX_URL_LEN, nullable = false)
	private String zonePartitionId;
	
	public String getZoneApex() {
		return zoneApex;
	}

	public void setZoneApex(String zoneApex) {
		this.zoneApex = zoneApex;
	}

	public AuthorizationStatus getAuthorizationStatus() {
		return authorizationStatus;
	}

	public void setAuthorizationStatus(AuthorizationStatus authorizationStatus) {
		this.authorizationStatus = authorizationStatus;
	}

	public String getAccountId() {
		return accountId;
	}

	public void setAccountId(String accountId) {
		this.accountId = accountId;
	}

	public String getSegment() {
		return segment;
	}

	public void setSegment(String segment) {
		this.segment = segment;
	}

	public String getZonePartitionId() {
		return zonePartitionId;
	}

	public void setZonePartitionId(String zonePartitionId) {
		this.zonePartitionId = zonePartitionId;
	}

}
