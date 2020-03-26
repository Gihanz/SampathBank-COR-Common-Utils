package biz.nable.sb.cor.common.utility;

public enum RecordStatusEnum {
	ACTIVE(0, "Active"), REJECTED(1, "Rejected"), NEW_PENDING(2, "Create Request subjected to approval"),
	MODIFY_PENDING(3, "Modify Request subjected to approval"),
	DELETE_PENDING(4, "Delete Request subjected to approval"), DELETED(5, "Permanently Deleted"),
	SUSPENDED(6, "Suspended");

	private final int value;

	private final String reasonPhrase;

	RecordStatusEnum(int value, String reasonPhrase) {
		this.value = value;
		this.reasonPhrase = reasonPhrase;
	}

	/**
	 * Return the integer value of this status code.
	 */
	public int value() {
		return this.value;
	}

	/**
	 * Return the reason phrase of this status code.
	 */
	public String getReasonPhrase() {
		return this.reasonPhrase;
	}
}
