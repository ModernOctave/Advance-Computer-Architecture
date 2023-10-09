package emulatorinterface.translator;

public class InvalidInstructionException extends Exception 
{
	private static final long serialVersionUID = 1L;

	//TODO need an efficient way to figure out
	//how to exit on fatal and how to flag an error.
	String message;
	boolean fatal;
	
	public InvalidInstructionException(String message, boolean fatal) 
	{
		super();
		this.message = message;
		this.fatal = fatal;
	}
}
