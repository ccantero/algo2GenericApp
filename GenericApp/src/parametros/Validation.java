package parametros;

public class Validation
{
	private int minSize;
	private boolean hasMinSize = false;
	
	private int maxSize;
	private boolean hasMaxSize = false;
	
	private boolean nullable;
	private boolean hasNullable = false;
	
	private boolean numeric;
	private boolean hasNumeric = false;
	
	private boolean exists;
	private boolean hasExists = false;
	
	
	public boolean hasMinSize()
	{
		return this.hasMinSize;
	}
	public boolean hasMaxSize()
	{
		return this.hasMaxSize;
	}
	public boolean hasNullable()
	{
		return this.hasNullable;
	}
	public boolean hasNumeric()
	{
		return this.hasNumeric;
	}
	public boolean hasExists()
	{
		return this.hasExists;
	}
	
	//getters y setters
	public int getMinSize() {
		return minSize;
	}
	public void setMinSize(int minSize) {
		this.minSize = minSize;
		this.hasMinSize = true;
	}
	public int getMaxSize() {
		return maxSize;
	}
	public void setMaxSize(int maxSize) {
		this.maxSize = maxSize;
		this.hasMaxSize = true;
	}
	public boolean isNullable() {
		return nullable;
	}
	public void setNullable(boolean nullable) {
		this.nullable = nullable;
		this.hasNullable = true;
	}
	public boolean isNumeric() {
		return numeric;
	}
	public void setNumeric(boolean numeric) {
		this.numeric = numeric;
		this.hasNumeric = true;
	}
	public boolean isExists() {
		return exists;
	}
	public void setExists(boolean exists) {
		this.exists = exists;
		this.hasExists = true;
	}
	
	
}