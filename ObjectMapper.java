import java.util.Hashtable;

public class ObjectMapper
{
	public static double[][] getObjectLocationArray()
	{
		double[][] object_array = new double[53][2];
		
		object_array[0][0] = 539080.628;
		object_array[0][1] = 5823155.422;
		
		object_array[1][0] = 539086.291;
		object_array[1][1] = 5823155.249;
	
		object_array[2][0] = 539079.935;
		object_array[2][1] = 5823148.063;
		
		object_array[3][0] = 539090.385;
		object_array[3][1] = 5823153.895;
		
		object_array[4][0] = 539087.633;
		object_array[4][1] = 5823149.767;
		
		object_array[5][0] = 539082.655;
		object_array[5][1] = 5823144.619;
		
		object_array[6][0] = 539104.556;
		object_array[6][1] = 5823157.319;
		
		object_array[7][0] = 539100.966;
		object_array[7][1] = 5823154.101;
		
		object_array[8][0] = 539095.087;
		object_array[8][1] = 5823150.731;
		
		object_array[9][0] = 539091.122;
		object_array[9][1] = 5823145.623;
		
		object_array[10][0] = 539086.722;
		object_array[10][1] = 5823141.810;
	
		object_array[11][0] = 539081.091;
		object_array[11][1] = 5823138.214;
		
		object_array[12][0] = 539117.104;
		object_array[12][1] = 5823156.268;
		
		object_array[13][0] = 539109.478;
		object_array[13][1] = 5823151.589;
		
		object_array[14][0] = 539100.519;
		object_array[14][1] = 5823146.578;
	
		object_array[15][0] = 539095.439;
		object_array[15][1] = 5823140.024;
		
		object_array[16][0] = 539089.617;
		object_array[16][1] = 5823137.041;
		
		object_array[17][0] = 539085.107;
		object_array[17][1] = 5823135.010;
		
		object_array[18][0] = 539079.116;
		object_array[18][1] = 5823131.827;
		
		object_array[19][0] = 539125.502;
		object_array[19][1] = 5823156.920;
		
		object_array[20][0] = 539117.698;
		object_array[20][1] = 5823150.523;
		
		object_array[21][0] = 539109.645;
		object_array[21][1] = 5823144.173;
		
		object_array[22][0] = 539102.208;
		object_array[22][1] = 5823139.490;
		
		object_array[23][0] = 539092.239;
		object_array[23][1] = 5823132.226;
		
		object_array[24][0] = 539081.452;
		object_array[24][1] = 5823122.121;
		
		object_array[25][0] = 539134.902;
		object_array[25][1] = 5823152.709;
		
		object_array[26][0] = 539128.926;
		object_array[26][1] = 5823148.620;
		
		object_array[27][0] = 539123.050;
		object_array[27][1] = 5823144.710;
		
		object_array[28][0] = 539117.007;
		object_array[28][1] = 5823140.705;
		
		object_array[29][0] = 539110.546;
		object_array[29][1] = 5823136.740;
		
		object_array[30][0] = 539103.747;
		object_array[30][1] = 5823130.232;
		
		object_array[31][0] = 539097.575;
		object_array[31][1] = 5823126.256;
		
		object_array[32][0] = 539091.528;
		object_array[32][1] = 5823121.257;
		
		object_array[33][0] = 539086.859;
		object_array[33][1] = 5823117.636;
		
		object_array[34][0] = 539136.754;
		object_array[34][1] = 5823146.868;
		
		object_array[35][0] = 539129.828;
		object_array[35][1] = 5823140.268;
		
		object_array[36][0] = 539124.284;
		object_array[36][1] = 5823135.999;
		
		object_array[37][0] = 539117.738;
		object_array[37][1] = 5823132.980;
		
		object_array[38][0] = 539111.835;
		object_array[38][1] = 5823127.564;
		
		object_array[39][0] = 539104.728;
		object_array[39][1] = 5823122.345;
		
		object_array[40][0] = 539100.296;
		object_array[40][1] = 5823119.568;
		
		object_array[41][0] = 539129.233;
		object_array[41][1] = 5823131.595;
	
		object_array[42][0] = 539124.573;
		object_array[42][1] = 5823128.099;
		
		object_array[43][0] = 539119.428;
		object_array[43][1] = 5823121.992;
		
		object_array[44][0] = 539112.131;
		object_array[44][1] = 5823117.652;
		
		object_array[45][0] = 539107.922;
		object_array[45][1] = 5823115.884;
		
		object_array[46][0] = 539134.526;
		object_array[46][1] = 5823127.206;
		
		object_array[47][0] = 539129.173;
		object_array[47][1] = 5823121.990;
		
		object_array[48][0] = 539130.481;
		object_array[48][1] = 5823116.210;
		
		object_array[49][0] = 539123.650;
		object_array[49][1] = 5823117.076;
		
		object_array[50][0] = 539132.594;
		object_array[50][1] = 5823164.449;
		
		object_array[51][0] = 539117.106;
		object_array[51][1] = 5823168.858;
		
		object_array[52][0] = 539101.650;
		object_array[52][1] = 5823165.038;
		
		return(object_array);
	}
	
	public static double[] getOffsetArray()
	{
		double[] offset_array = new double[30];
		offset_array[0]       = .4;
		offset_array[1]       = -.4;
		offset_array[2]       = .4;
		offset_array[3]       = -1.;
		offset_array[4]       = .3;
		offset_array[5]       = -.3;
		offset_array[6]       = .4;
		offset_array[7]       = -.6;
		offset_array[8]       = .6;
		offset_array[9]       = -.6;
		offset_array[10]      = .6;
		offset_array[11]      = -.6;
		offset_array[12]      = .4;
		offset_array[13]      = -.4;
		offset_array[14]      = .4;
		offset_array[15]      = -.5;
		offset_array[16]      = .5;
		offset_array[17]      = -.5;
		offset_array[18]      = .5;
		offset_array[19]      = -.5;
		offset_array[20]      = .7;
		offset_array[21]      = -.4;
		offset_array[22]      = .4;
		offset_array[23]      = -.4;
		offset_array[24]       = .5;
		offset_array[25]       = -.5;
		offset_array[26]       = .5;
		offset_array[27]       = -.4;
		offset_array[28]       = .4;
		offset_array[29]       = -.6;
		return(offset_array);
	}
	
	public static int[][] getLineArray()
	{
		int[][] line_array = new int[30][2];
		
		line_array[0][0] = 0;
		line_array[0][1] = 8400;
		
		line_array[1][0] = 12950;
		line_array[1][1] = 22430;
		
		line_array[2][0] = 28300;
		line_array[2][1] = 36300;
		
		line_array[3][0] = 40000;
		line_array[3][1] = 49700;
		
		line_array[4][0] = 56000;
		line_array[4][1] = 63300;
		
		line_array[5][0] = 70000;
		line_array[5][1] = 77500;
		
		line_array[6][0] = 82000;
		line_array[6][1] = 91300;
		
		line_array[7][0] = 97000;
		line_array[7][1] = 105200;
		
		line_array[8][0] = 110000;
		line_array[8][1] = 119000;
		
		line_array[9][0] = 124000;
		line_array[9][1] = 132700;
		
		line_array[10][0] = 137500;
		line_array[10][1] = 146800;
		
		line_array[11][0] = 151000;
		line_array[11][1] = 160500;
		
		line_array[12][0] = 165500;
		line_array[12][1] = 174000;
		
		line_array[13][0] = 179000;
		line_array[13][1] = 188000;
		
		line_array[14][0] = 193000;
		line_array[14][1] = 201500;
		
		line_array[15][0] = 206500;
		line_array[15][1] = 215600;
		
		line_array[16][0] = 220000;
		line_array[16][1] = 229300;
		
		line_array[17][0] = 233400;
		line_array[17][1] = 243000;
		
		line_array[18][0] = 247000;
		line_array[18][1] = 256300;
		
		line_array[19][0] = 261000;
		line_array[19][1] = 270300;
		
		line_array[20][0] = 275000;
		line_array[20][1] = 284000;
		
		line_array[21][0] = 289000;
		line_array[21][1] = 298000;
		
		line_array[22][0] = 303000;
		line_array[22][1] = 312000;
		
		line_array[23][0] = 317000;
		line_array[23][1] = 325500;
		
		line_array[24][0] = 331500;
		line_array[24][1] = 339000;
		
		line_array[25][0] = 344000;
		line_array[25][1] = 352800;
		
		line_array[26][0] = 357000;
		line_array[26][1] = 366500;
		
		line_array[27][0] = 371000;
		line_array[27][1] = 380000;
		
		line_array[28][0] = 385000;
		line_array[28][1] = 394500;
		
		line_array[29][0] = 399000;
		line_array[29][1] = 409650;
		
		return(line_array);
	}
	
	public static int[][] getUnclippedLineArray()
	{
		int[][] line_array = new int[30][2];
		
		line_array[0][0] = 0;
		line_array[0][1] = 10400;
		
		line_array[1][0] = 10400;
		line_array[1][1] = 23850;
		
		line_array[2][0] = 23850;
		line_array[2][1] = 37150;
		
		line_array[3][0] = 37150;
		line_array[3][1] = 51500;
		
		line_array[4][0] = 51500;
		line_array[4][1] = 65000;
		
		line_array[5][0] = 65000;
		line_array[5][1] = 79500;
		
		line_array[6][0] = 79500;
		line_array[6][1] = 93000;
		
		line_array[7][0] = 93000;
		line_array[7][1] = 107500;
		
		line_array[8][0] = 107500;
		line_array[8][1] = 120500;
		
		line_array[9][0] = 120500;
		line_array[9][1] = 134750;
		
		line_array[10][0] = 134750;
		line_array[10][1] = 148500;
		
		line_array[11][0] = 148500;
		line_array[11][1] = 162500;
		
		line_array[12][0] = 162500;
		line_array[12][1] = 176000;
		
		line_array[13][0] = 176000;
		line_array[13][1] = 190500;
		
		line_array[14][0] = 190500;
		line_array[14][1] = 203500;
		
		line_array[15][0] = 203500;
		line_array[15][1] = 218000;
		
		line_array[16][0] = 218000;
		line_array[16][1] = 231000;
		
		line_array[17][0] = 231000;
		line_array[17][1] = 245000;
		
		line_array[18][0] = 245000;
		line_array[18][1] = 258500;
		
		line_array[19][0] = 258500;
		line_array[19][1] = 272500;
		
		line_array[20][0] = 272500;
		line_array[20][1] = 286500;
		
		line_array[21][0] = 286500;
		line_array[21][1] = 300000;
		
		line_array[22][0] = 300000;
		line_array[22][1] = 313500;
		
		line_array[23][0] = 313500;
		line_array[23][1] = 327000;
		
		line_array[24][0] = 327000;
		line_array[24][1] = 341000;
		
		line_array[25][0] = 341000;
		line_array[25][1] = 355000;
		
		line_array[26][0] = 355000;
		line_array[26][1] = 368500;
		
		line_array[27][0] = 368500;
		line_array[27][1] = 382000;
		
		line_array[28][0] = 382000;
		line_array[28][1] = 395000;
		
		line_array[29][0] = 395000;
		line_array[29][1] = 409650;
		
		return(line_array);
	}
}