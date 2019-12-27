public class Sample
{
	public double x;
	public double y;
	public double intensity;
	public double distance;

	
	public Sample(double x, double y, double intensity)
	{
		this.x = x;
		this.y = y;
		this.intensity = intensity;
		this.distance = 0;
	}
	
	public void setDistance(double x_origin, double y_origin)
	{
	    distance  = Math.sqrt((x - x_origin) * (x - x_origin) + (y - y_origin) * (y - y_origin));
	}
}