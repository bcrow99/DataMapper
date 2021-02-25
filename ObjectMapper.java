import java.util.Hashtable;

public class ObjectMapper
{
	// Maps objects to iso quadrants.
	public static int[][] getIsoArray()
	{
		int[][] iso_array = new int[54][2];
		
		iso_array[1][0] = 1;
		iso_array[1][1] = 24;
		
		iso_array[2][0] = 2;
		iso_array[2][1] = 24;
		
		iso_array[3][0] = 1;
		iso_array[3][1] = 27;
		
		iso_array[4][0] = 5;
		iso_array[4][1] = 25;
		
		iso_array[5][0] = 4;
		iso_array[5][1] = 26;
		
		iso_array[6][0] = 1;
		iso_array[6][1] = 28;
		
		iso_array[7][0] = 14;
		iso_array[7][1] = 23;
		
		iso_array[8][0] = 12;
		iso_array[8][1] = 25;
		
		iso_array[9][0] = 9;
		iso_array[9][1] = 26;
		
		iso_array[10][0] = 8;
		iso_array[10][1] = 29;
		
		iso_array[11][0] = 4;
		iso_array[11][1] = 31;
		
		iso_array[12][0] = 2;
		iso_array[12][1] = 32;
		
		iso_array[13][0] = 19;
		iso_array[13][1] = 24;
		
		iso_array[14][0] = 15;
		iso_array[14][1] = 25;
		
		iso_array[15][0] = 12;
		iso_array[15][1] = 29;
		
		iso_array[16][0] = 9;
		iso_array[16][1] = 31;
		
		iso_array[17][0] = 6;
		iso_array[17][1] = 32;
		
		iso_array[18][0] = 3;
		iso_array[18][1] = 33;
		
		iso_array[19][0] = 0;
		iso_array[19][1] = 34;
		
		iso_array[20][0] = 23;
		iso_array[20][1] = 23;
		
		iso_array[21][0] = 19;
		iso_array[21][1] = 26;
		
		iso_array[22][0] = 15;
		iso_array[22][1] = 28;
		
		iso_array[23][0] = 12;
		iso_array[23][1] = 31;
		
		iso_array[24][0] = 8;
		iso_array[24][1] = 34;
		
		iso_array[26][0] = 27;
		iso_array[26][1] = 23;
		
		iso_array[27][0] = 24;
		iso_array[27][1] = 26;
		
		iso_array[28][0] = 21;
		iso_array[28][1] = 27;
		
		iso_array[29][0] = 19;
		iso_array[29][1] = 31;
		
		iso_array[30][0] = 16;
		iso_array[30][1] = 32;
		
		iso_array[31][0] = 12;
		iso_array[31][1] = 33;
		
		iso_array[32][0] = 8;
		iso_array[32][1] = 36;
		
		iso_array[35][0] = 27;
		iso_array[35][1] = 25;
		
		iso_array[36][0] = 24;
		iso_array[36][1] = 30;
		
		iso_array[37][0] = 22;
		iso_array[37][1] = 31;
		
		iso_array[38][0] = 17;
		iso_array[38][1] = 32;
		
		iso_array[39][0] = 14;
		iso_array[39][1] = 35;
		
		iso_array[42][0] = 24;
		iso_array[42][1] = 34;
		
		iso_array[43][0] = 21;
		iso_array[43][1] = 34;
		
		iso_array[47][0] = 26;
		iso_array[47][1] = 36;
		
		iso_array[51][0] = 26;
		iso_array[51][1] = 17;  
		
		iso_array[52][0] = 19;
		iso_array[52][1] = 15;  
		
		iso_array[53][0] = 11;
		iso_array[53][1] = 16;  
		return iso_array;
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
	
	public static int getInitRow(int object_id)
	{
		int init_row[] = new int [54];
		for(int i = 0; i < 54; i++)
			init_row[i] = 0;
		init_row[1]  = 1;
		init_row[2]  = 4;
		init_row[3]  = 1;
		init_row[4]  = 4;
		init_row[5]  = 4;
		init_row[6]  = 2;
		init_row[7]  = 13;
		init_row[8]  = 10;
		init_row[9]  = 9;
		init_row[10] = 4;
		init_row[11] = 4;
		init_row[12] = 3;
		init_row[13] = 17;
		init_row[14] = 17;
		init_row[15] = 9;
		init_row[16] = 9;
		init_row[17] = 4;
		init_row[18] = 3;
		init_row[19] = 1;
		init_row[20] = 23;
		init_row[21] = 17;
		init_row[22] = 17;
		init_row[23] = 9;
		init_row[24] = 17;
		init_row[25] = 0;
		init_row[26] = 26;
		init_row[27] = 25;
		init_row[28] = 21;
		init_row[29] = 17;
		init_row[30] = 17;
		init_row[31] = 11;
		init_row[32] = 8;
		init_row[33] = 0;
		init_row[34] = 0;
		init_row[35] = 27;
		init_row[36] = 27;
		init_row[37] = 17;
		init_row[38] = 17;
		init_row[39] = 14;
		init_row[40] = 0;
		init_row[41] = 0;
		init_row[42] = 26;
		init_row[43] = 17;
		init_row[44] = 0;
		init_row[45] = 0;
		init_row[46] = 0;
		init_row[47] = 27;
		init_row[48] = 0;
		init_row[49] = 0;
		init_row[50] = 0;
		init_row[51] = 27;
		init_row[52] = 18;
		init_row[53] = 13;
		return init_row[object_id];
	}
	
	public static Hashtable getObjectTable()
	{
		Hashtable table = new Hashtable();
		
		//Possible.
		int key        = 1;
		int[][] object = new int[2][2];
		object[0][0]   = 5000;
		object[0][1]   = 5785;
		object[1][0]   = 18600;
		object[1][1]   = 19380;
		table.put(key,  object);
		
		
		//Possible.  
	    key          = 2;
	    object       = new int[5][2];
	    
		// This is no where near our known objects.
		// A lucky accident that I found an interesting 
		// signal in the blind data space worth exploring.
		// Extremely smooth background helps pick out
		// low intensity signal.
		object[0][0] = 47850;
		object[0][1] = 49370;
		object[1][0] = 57400;
		object[1][1] = 59050;
		object[2][0] = 75355;
		object[2][1] = 76800;
		object[3][0] = 85055;
		object[3][1] = 86630;
		object[4][0] = 103040;
		object[4][1] = 104440;
		
	    /*
		object[0][0] = 17550;
		object[0][1] = 18365;
		object[1][0] = 33475;
		object[1][1] = 34300;
		object[2][0] = 44925;
		object[2][1] = 45800;
		*/
		//object[3][0] = 61435;
		//object[3][1] = 61690;
		
		table.put(key,  object);
	
		//Possible.
		key          = 3;
		object       = new int[2][2];
		object[0][0] = 6000;
		object[0][1] = 6700;
		object[1][0] = 17650;
		object[1][1] = 18330;
		table.put(key,  object);
		
		//Possible. Not a good signal but a constellation of signals that 
		//resembles the iso plot around objects 4 and 5 and 10.
		key          = 4;
		object       = new int[5][2];
		object[0][0] = 42540;
		object[0][1] = 45400;
		object[1][0] = 61550;
		object[1][1] = 63750;
		object[2][0] = 70200;
		object[2][1] = 72685;
		object[3][0] = 89250;
		object[3][1] = 91500;
		object[4][0] = 97700;
		object[4][1] = 100350;
		table.put(key,  object);
		
		//Associating the same data segment with three objects.
		key          = 5;
		object       = new int[5][2];
		object[0][0] = 42540;
		object[0][1] = 45400;
		object[1][0] = 61550;
		object[1][1] = 63750;
		object[2][0] = 70200;
		object[2][1] = 72685;
		object[3][0] = 89250;
		object[3][1] = 91500;
		object[4][0] = 97700;
		object[4][1] = 100350;
		table.put(key,  object);
		
		//Probable.  Nice smooth image with narrow range.
		//Part of a constellation of signals with 11 and 17.
		key          = 6;
		object       = new int[3][2];
		object[0][0] = 17660;
		object[0][1] = 18560;
		object[1][0] = 33290;
		object[1][1] = 34200;
		object[2][0] = 45050;
		object[2][1] = 45990;
		table.put(key,  object);		

		//Possible.  Narrow range of intensities.
		key          = 7;
		object       = new int[4][2];
		object[0][0] = 170050;
		object[0][1] = 171350;
		object[1][0] = 184050;
		object[1][1] = 185400;
		object[2][0] = 197600;
		object[2][1] = 198935;
		object[3][0] = 211800;
		object[3][1] = 213045;
		table.put(key,  object);
		
		// Probable.
		key          = 8;
		object       = new int[5][2];
		object[0][0] = 115500;
		object[0][1] = 117335;
		object[1][0] = 127300;
		object[1][1] = 129170;
		object[2][0] = 143380;
		object[2][1] = 145250;
		object[3][0] = 155075;
		object[3][1] = 157005;
		object[4][0] = 170800;
		object[4][1] = 172700;
		table.put(key,  object);
		
			
		//Undetermined.  This should include the location
		// of object 9 but there is no obvious signal.
		key          = 9;
		object       = new int[4][2];
		object[0][0] = 113730;
		object[0][1] = 115960;
		object[1][0] = 128700;
		object[1][1] = 130900;
		object[2][0] = 141570;
		object[2][1] = 143800;
		object[3][0] = 156640;
		object[3][1] = 158740;
		table.put(key,  object);
		
		
		
		//Not a good signal, but a constellation of signals that
		//resembles the iso plot around 4, 5, and 10.
		key          = 10;
		object       = new int[5][2];
		object[0][0] = 42540;
		object[0][1] = 45400;
		object[1][0] = 61550;
		object[1][1] = 63750;
		object[2][0] = 70200;
		object[2][1] = 72685;
		object[3][0] = 89250;
		object[3][1] = 91500;
		object[4][0] = 97700;
		object[4][1] = 100350;
		table.put(key,  object);
		
		
		//Possible.  Part of a constellation of signals along 
		//with 6 and 17 that resembles part of the iso plot.
		//Not a good signal.
		key          = 11;
		object       = new int[3][2];
		object[0][0] = 44050;
		object[0][1] = 45300;
		object[1][0] = 61500;
		object[1][1] = 62750;
		object[2][0] = 71500;
		object[2][1] = 72730;
		table.put(key,  object);
	
		//Possible.  Consistent with object 18.
		key          = 12;
		object       = new int[5][2];
		object[0][0] = 5800;
		object[0][1] = 7250;
		object[1][0] = 17120;
		object[1][1] = 18600;
		object[2][0] = 33300;
		object[2][1] = 34700;
		object[3][0] = 44580;
		object[3][1] = 46000;
		object[4][0] = 60800;
		object[4][1] = 62320;
		table.put(key,  object);

	
		//Probable.
		key          = 13;
		object       = new int[6][2];
		object[0][0] = 226055;
		object[0][1] = 228255;
		object[1][0] = 237200;
		object[1][1] = 239410;
		object[2][0] = 253200;
		object[2][1] = 255295;
		object[3][0] = 264800;
		object[3][1] = 267000;
		object[4][0] = 280800;
		object[4][1] = 283050;
		object[5][0] = 292300;
		object[5][1] = 294500;
		table.put(key,  object);
	
		
		// More closely cropped segment.
	    /*
		key          = 13;
		object       = new int[4][2];
		object[0][0] = 226500; 
		object[0][1] = 227870;
		object[1][0] = 237600;  
		object[1][1] = 239050; 
		object[2][0] = 254000;
		object[2][1] = 254920;
		object[3][0] = 265200;
		object[3][1] = 266600;
		table.put(key,  object);
		*/
		
		// Two possible signals near location
		// of object 14.
		key          = 14;
		object       = new int[4][2];
		object[0][0] = 169050;
		object[0][1] = 171260;
		object[1][0] = 184130;
		object[1][1] = 186350;
		object[2][0] = 196600;
		object[2][1] = 198900;
		object[3][0] = 211835;
		object[3][1] = 214100;
		table.put(key,  object);
		
	
		//Undetermined. This is the segment associated with object 16.
		/*
		key          = 15;
		object       = new int[3][2];
		object[0][0] = 116135;
		object[0][1] = 116955;
		object[1][0] = 127850;
		object[1][1] = 128680;
		object[2][0] = 144000;
		object[2][1] = 144870;
		table.put(key,  object);		
		*/
		
		key          = 15;
		object       = new int[4][2];
		//object[0][0] = 113730;
		//object[0][1] = 115960;
		//object[0][0] = 128700;
		//object[0][1] = 130900;
		//object[0][0] = 141570;
		//object[0][1] = 143800;
		//object[0][0] = 156640;
		//object[0][1] = 158740;
		object[0][0] = 169150;
		object[0][1] = 171250;
		object[1][0] = 184085;
		object[1][1] = 186385;
		object[2][0] = 196720;
		object[2][1] = 198875;
		object[3][0] = 211815;
		object[3][1] = 214095;
		table.put(key,  object);
		
		// Probable, consistent with object 32.	
	
		key          = 16;
		object       = new int[4][2];
		object[0][0] = 115365;
		object[0][1] = 117500;
		object[1][0] = 127030;
		object[1][1] = 129300;
		object[2][0] = 143220;
		object[2][1] = 145400;
		object[3][0] = 154940;
		object[3][1] = 157140;
		table.put(key,  object);
		
	    /*
		key          = 16;
		object       = new int[4][2];
		object[0][0] = 116965;
		object[0][1] = 118715;
		object[1][0] = 125400;
		object[1][1] = 127965;
		object[2][0] = 144720;
		object[2][1] = 146800;
		object[3][0] = 153500;
		object[3][1] = 155840;
		table.put(key,  object);
		*/
	
		
		//Possible.
		key = 17;
		object       = new int[3][2];
		object[0][0] = 44300;
		object[0][1] = 45085;
		object[1][0] = 61700;
		object[1][1] = 62530;
		object[2][0] = 71730;
		object[2][1] = 72590;
		table.put(key,  object);
		
		
		
		
		//Possible.
		key = 18;
		object       = new int[4][2];
		object[0][0] = 34150;
		object[0][1] = 35180;
		object[1][0] = 44000;
		object[1][1] = 45095;
		object[2][0] = 61720;
		object[2][1] = 62800;
		object[3][0] = 71445;
		object[3][1] = 72570;
		table.put(key,  object);
		
		
		
		//Possible.
		key = 19;
		object       = new int[1][2];
		object[0][0] = 8000;
		object[0][1] = 8500;
		table.put(key,  object);
		
		//Possible.
		key = 20;
		object       = new int[3][2];
		//object[0][0] = 306865;
		//object[0][1] = 307880;
		object[0][0] = 322525;
		object[0][1] = 322475;
		object[1][0] = 334540;
		object[1][1] = 335500;
		object[2][0] = 349720;
		object[2][1] = 350700;
		table.put(key,  object);

		//Probable.
	    key          = 21;
		object       = new int[5][2];
		object[0][0] = 226500;
		object[0][1] = 227780;
		object[1][0] = 237750;
		object[1][1] = 239070;
		object[2][0] = 253550;
		object[2][1] = 254810;
		object[3][0] = 265300;
		object[3][1] = 266540;
		object[4][0] = 281230;
		object[4][1] = 282565;
		table.put(key,  object);

		//Duplicate of object 21 segment.
	    key          = 22;
	    object       = new int[4][2];
		object[0][0] = 226570;
		object[0][1] = 227770;
		object[1][0] = 237760;
		object[1][1] = 239020;
		object[2][0] = 253600;
		object[2][1] = 254780;
		object[3][0] = 265300;
		object[3][1] = 266510;
		table.put(key,  object);

		// Duplicate of object 8 (or whatever object it is).
		key = 23;
		object       = new int[4][2];
		object[0][0] = 115705;
		object[0][1] = 117900;
		object[1][0] = 126750;
		object[1][1] = 128900;
		object[2][0] = 143600;
		object[2][1] = 145750;
		object[3][0] = 154600;
		object[3][1] = 156900;
		//object[4][0] = 171000;
		//object[4][1] = 173250;
		table.put(key,  object);
		
		// Yet another duplicate of object 38.
		// Location is way off.
		key = 24;
		object       = new int[4][2];
		object[0][0] = 227860;
		object[0][1] = 228700;
		object[1][0] = 236700;
		object[1][1] = 237350;
		object[2][0] = 254900;
		object[2][1] = 255770;
		object[3][0] = 264280;
		object[3][1] = 265200;
		table.put(key,  object);
		
		// Object 25 is not in the regular data set.
		
		//Probable.  Undersampled.
		key          = 26;
	    object       = new int[4][2];
		object[0][0] = 347850;
		object[0][1] = 349425;
		object[1][0] = 363100;
		object[1][1] = 364620;
		object[2][0] = 375350;
		object[2][1] = 376920;
		object[3][0] = 390545;
		object[3][1] = 392100;
		table.put(key,  object);
		
		//Possible.
		key = 27;
		object       = new int[4][2];
		object[0][0] = 336250;
		object[0][1] = 336830;
		object[1][0] = 348355;
		object[1][1] = 348975;
		object[2][0] = 363550;
		object[2][1] = 364150;
		object[3][0] = 375950;
		object[3][1] = 376400;
		table.put(key,  object);
		
		//Possible.
		key = 28;
		object       = new int[4][2];
		object[0][0] = 281465;
		object[0][1] = 282360;
		object[1][0] = 293050;
		object[1][1] = 293910;
		object[2][0] = 308820;
		object[2][1] = 309675;
		object[3][0] = 320540;
		object[3][1] = 321435;
		table.put(key,  object);
		
		//Duplicate of 38, more closely cropped.
		key          = 29;
		object       = new int[4][2];
		object[0][0] = 226905;
		object[0][1] = 227780;
		object[1][0] = 237900;
		object[1][1] = 238560;
		object[2][0] = 253975;
		object[2][1] = 254810;
		object[3][0] = 265320;
		object[3][1] = 266200;
		table.put(key,  object);
		
		//Even more closely cropped version of same data.
		key          = 30;
		object       = new int[3][2];
		object[0][0] = 227900;
		object[0][1] = 228565;
		object[1][0] = 236995;
		object[1][1] = 237675;
		object[2][0] = 254900;
		object[2][1] = 255640;
		table.put(key,  object);
		
		//Possible.
		key          = 31;
		object       = new int[3][2];
		object[0][0] = 145615;
		object[0][1] = 146550;
		object[1][0] = 153830;
		object[1][1] = 154745;
		object[2][0] = 173080;
		object[2][1] = 174000;
		table.put(key,  object);
		
		//Possible.  Consistent with position of object 16.
		key          = 32;
		object       = new int[3][2];
		object[0][0] = 98095;
		object[0][1] = 98980;
		object[1][0] = 118215;
		object[1][1] = 118800;
		object[2][0] = 125415;
		object[2][1] = 126325;
		table.put(key,  object);
		
		// Objects 33 and 34 are not in the regular data set.
		
		//Possible.
		key          = 35;
		object       = new int[3][2];
		object[0][0] = 363580;
		object[0][1] = 364605;
		object[1][0] = 375450;
		object[1][1] = 376555;
		object[2][0] = 390900;
		object[2][1] = 392100;
		table.put(key,  object);
		
		//Probable.
		key          = 36;
		object       = new int[4][2];
		object[0][0] = 363605;
		object[0][1] = 364700;
		object[1][0] = 375200;
		object[1][1] = 376450;
		object[2][0] = 391000;
		object[2][1] = 392220;
		object[3][0] = 402705;
		object[3][1] = 403960;
		table.put(key,  object);
		
		//Probable.
		key          = 37;
		object       = new int[4][2];
		object[0][0] = 226275;
		object[0][1] = 228100;
		object[1][0] = 237300;
		object[1][1] = 239235;
		object[2][0] = 253400;
		object[2][1] = 255150;
		object[3][0] = 264900;
		object[3][1] = 266800;
		table.put(key,  object);
		
		//Probable.
	    key          = 38;
		object       = new int[4][2];
		object[0][0] = 227755;
		object[0][1] = 228765;
		object[1][0] = 236760;
		object[1][1] = 237775;
		object[2][0] = 254760;
		object[2][1] = 255850;
		object[3][0] = 264200;
		object[3][1] = 265320;
		table.put(key,  object);
		
		// Possible.  Undersampled.
		key          = 39;
		object       = new int[4][2];
		object[0][0] = 181090;
		object[0][1] = 182960;
		object[1][0] = 200000;
		object[1][1] = 201650;
		object[2][0] = 208900;
		object[2][1] = 210610;
		object[3][0] = 227500;
		object[3][1] = 229000;
		table.put(key,  object);
		
		//Objects 40 and 41 are not in the regular data set.
		
		//Possible. Irregular sampling.
		key          = 42;
		object       = new int[5][2];
		object[0][0] = 348305;
		object[0][1] = 351430;
		object[1][0] = 361060;
		object[1][1] = 364100;
		object[2][0] = 375950;
		object[2][1] = 379000;
		object[3][0] = 388325;
		object[3][1] = 391595;
		object[4][0] = 403400;
		object[4][1] = 406510;
		
		table.put(key,  object);
		
		//This is too far from the known location to object 43.
		key          = 43;
		object       = new int[4][2];
		object[0][0] = 236000;
		object[0][1] = 239900;
		object[1][0] = 254600;
		object[1][1] = 256270;
		object[2][0] = 252700;
		object[2][1] = 256070;
		object[3][0] = 263670;
		object[3][1] = 267500;
		table.put(key,  object);
		
		//Objects 44, 45 and 46 are not in the regular data set.
		
		//Probable.  Irregular data sampling--both curved flight line
		//and undersampling but still manifests obvious wave form.
		key          = 47;
		object       = new int[3][2];
		object[0][0] = 366355;
		object[0][1] = 367060;
		object[1][0] = 370900;
		object[1][1] = 373835;
		object[2][0] = 393455;
		object[2][1] = 394760;
		table.put(key,  object);
		
		//Objects 48, 49, and 50 are not in the regular data set.
		
		//Probable.
		key          = 51;
		
		
		
		object       = new int[3][2];
		object[0][0] = 362200;
		object[0][1] = 363100;
		object[1][0] = 376870;
		object[1][1] = 377810;
		object[2][0] = 389635;
		object[2][1] = 390620;
		table.put(key,  object);		
		
		 // Possible.
		 key          = 52;
		 object       = new int[3][2];
		 object[0][0] = 240600;
		 object[0][1] = 241500;
		 object[1][0] = 250950;
		 object[1][1] = 252000;
		 object[2][0] = 268170;
		 object[2][1] = 269100;
		 table.put(key,  object);
		
		 
		 // Possible.
		 key          = 53;
		 object       = new int[4][2];
		 object[0][0] = 169400;
		 object[0][1] = 170655;
		 object[1][0] = 184750;
		 object[1][1] = 185975;
		 object[2][0] = 197000;
		 object[2][1] = 198315;
		 object[3][0] = 212435;
		 object[3][1] = 213675;
		 table.put(key,  object);
	
		 
		 
		 
		 return table;
		
	}
}