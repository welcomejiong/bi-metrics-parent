package org.corps.bi.recording.exception;

public class TrackingException extends RuntimeException{
	

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public TrackingException(String msg) {
	    super(msg);
	  }

	  public TrackingException(String msg, Throwable th) {
	    super(msg, th);
	  }

	  public TrackingException(Throwable th) {
	    super(th);
	  }
}
