public class QuadrantMapper
{
	public static int[] getQuadrantSetIndex(int id)
	{
		int index[];
		
	    if(id < 0 || id > 29)  // Not a recognized id.
	    {
	    	index    = new int[1];
	    	index[0] = -1;
	    	return index;
	    }
	    else
	    {
	        if(id == 0)
	    	{ 
	    	    index    = new int[1];
	    	    index[0] = -1;
	    	    return index;
	    	}
	        else if(id == 1)
	        {
	        	index = new int[3];
		    	index[0] = 0;
		    	index[1] = 2;
		    	index[2] = 5;
		    	return index;   	
	        }
	        else if(id == 2)
	        {
	        	index    = new int[3];
		        index[0] = 0;
		        index[1] = 2;
		        index[2] = 6;
		        return index;    	
	        }
	        else if(id == 3)
	        {
	        	index    = new int[3];
		        index[0] = 0;
		        index[1] = 2;
		        index[2] = 7;
		        return index;    	
	        }
	        else if(id == 4)
	        {
	        	index    = new int[2];
		        index[0] = 0;
		        index[1] = 4;
		        return index;    	
	        }
	        else if(id == 5)
	        {
	        	index    = new int[3];
		        index[0] = 0;
		        index[1] = 4;
		        index[2] = 5;
		        return index;    	
	        }
	        else if(id == 6)
	        {
	        	index    = new int[3];
		        index[0] = 0;
		        index[1] = 4;
		        index[2] = 6;
		        return index;    	
	        }
	        else if(id == 7)
	        {
	        	index    = new int[3];
		        index[0] = 0;
		        index[1] = 4;
		        index[2] = 6;
		        return index;    	
	        }
	        else if(id == 8)
	        {
	        	index    = new int[2];
		        index[0] = 0;
		        index[1] = 6;
		        return index;    	
	        }
	        else if(id == 9)
	        {
	        	index    = new int[2];
		        index[0] = 0;
		        index[1] = 7;
		        return index;    	
	        }
	        else if(id == 10)
	        {
	        	index    = new int[3];
		        index[0] = 1;
		        index[1] = 3;
		        index[2] = 4;
		        return index;    	
	        }
	        else if(id == 11)
	        {
	        	index    = new int[3];
		        index[0] = 1;
		        index[1] = 3;
		        index[2] = 6;
		        return index;    	
	        }
	        else if(id == 12)
	        {
	        	index    = new int[3];
		        index[0] = 1;
		        index[1] = 3;
		        index[2] = 7;
		        return index;    	
	        }
	        else if(id == 13)
	        {
	        	index    = new int[3];
		        index[0] = 1;
		        index[1] = 4;
		        index[2] = 5;
		        return index;    	
	        }
	        else if(id == 14)
	        {
	        	index    = new int[3];
		        index[0] = 1;
		        index[1] = 4;
		        index[2] = 6;
		        return index;    	
	        }
	        else if(id == 15)
	        {
	        	index    = new int[2];
		        index[0] = 0;
		        index[1] = 7;
		        return index;    	
	        }
	        else if(id == 16)
	        {
	        	index    = new int[3];
		        index[0] = 1;
		        index[1] = 5;
		        index[2] = 7;
		        return index;    	
	        }
	        else if(id == 17)
	        {
	        	index    = new int[2];
		        index[0] = 1;
		        index[1] = 6;
		        return index;    	
	        }
	        else if(id == 18)
	        {
	        	index    = new int[3];
		        index[0] = 1;
		        index[1] = 6;
		        index[2] = 7;
		        return index;    	
	        }
	        else if(id == 19)
	        {
	        	index    = new int[2];
		        index[0] = 1;
		        index[1] = 7;
		        return index;    	
	        }	
	        else if(id == 20)
	        {
	        	index    = new int[2];
		        index[0] = 2;
		        index[1] = 3;
		        return index;    	
	        }
	        else if(id == 21)
	        {
	        	index    = new int[3];
		        index[0] = 2;
		        index[1] = 3;
		        index[2] = 6;
		        return index;    	
	        }
	        else if(id == 22)
	        {
	        	index    = new int[3];
		        index[0] = 2;
		        index[1] = 3;
		        index[2] = 7;
		        return index;    	
	        }
	        else if(id == 23)
	        {
	        	index    = new int[2];
		        index[0] = 2;
		        index[1] = 5;
		        return index;    	
	        }
	        else if(id == 24)
	        {
	        	index    = new int[3];
		        index[0] = 2;
		        index[1] = 5;
		        index[2] = 7;
		        return index;    	
	        }
	        else if(id == 25)
	        {
	        	index    = new int[2];
		        index[0] = 3;
		        index[1] = 4;
		        return index;    	
	        }
	        else if(id == 26)
	        {
	        	index    = new int[3];
		        index[0] = 3;
		        index[1] = 4;
		        index[2] = 6;
		        return index;    	
	        }
	        else if(id == 27)
	        {
	        	index    = new int[3];
		        index[0] = 3;
		        index[1] = 4;
		        index[2] = 7;
		        return index;    	
	        }
	        else if(id == 28)
	        {
	        	index    = new int[2];
		        index[0] = 3;
		        index[1] = 7;
		        return index;    	
	        }
	        else if(id == 29)
	        {
	        	index    = new int[2];
		        index[0] = 4;
		        index[1] = 5;
		        return index;    	
	        }
	        else
	        {
	        	index = new int[1];
	            index[0] = -1;
	            return index;
	        }
	    }
	       
	 }
	    	

	// This function assumes the indices are passed in order of increasing value and they are either doublets or triplets.
	public static int getQuadrantSetID(int ...index)
	{
		// Find out if we have a doublet or triplet and initialize indices.
		int first_index = index[0];
		int second_index = index[1];
		int third_index = -1;
		boolean isTriplet = false;
		if(index.length == 3)
		{
			third_index = index[2];
			isTriplet   = true;
		}
		
		// Get an id for sets of quadrants that might contain the center of a cell with a polygon.
		// Return 0 for all other sets.
		if(first_index == 0)
		{
			if(second_index == 2)
			{
				if(!isTriplet)
					return(0);  // All containing sets including 0 and 2 are triplets.
				else if(third_index == 5)
				    return(1);	
				else if(third_index == 6)
				    return(2);   	
				else if(third_index == 7)
				    return(3);
				else
					return(0);	
				
			}
		    if(second_index == 4)
		    {
		        if(!isTriplet)
		        	return(4);
		        else
		        {
		        	if(third_index == 5)
		        	    return(5);    	
		        	else if(third_index == 6)
		        	    return(6);	
		        	else if(third_index ==7)
		        	    return(7);
		        	else
		        	    return(4);  // Return the id for the doublet if the third index is irrelevant.
		        }
		    }
		    else if(second_index == 6)
		        return(8);  // The triplet is already accounted for.
		    else if(second_index == 7)
		        return(9);  // The triplet is already accounted for.
		    else
		    	return(0); // Return 0 since the first two quadrants cannot contain the center of the cell by themselves. 	
		}
		else if(first_index == 1)
		{
		    if(second_index == 3)
		    {
		    	if(!isTriplet)
		        	return(0);  // Any containing sets with these two quadrants must be a triplet.
		        else
		        {
		        	if(third_index == 4)
	        	        return(10);    	
	        	    else if(third_index == 6)
	        	        return(11);	
	        	    else if(third_index == 7)
	        	    	return(12);
	        	else
	        	    return(9);	
		        }
		    }
		    else if(second_index == 4)
		    {
		    	if(!isTriplet)
		        	return(12);
		        else
		        {
		        	if(third_index == 5)
	        	        return(13);    	
	        	    else if(third_index == 6)
	        	        return(14);	
	        	    else
	        	        return(12);	
		        }	
		    }
		    else if(second_index == 5)
		    {
		    	if(!isTriplet)
		        	return(15);
		        else
		        {
		        	if(third_index == 7)
	        	        return(16);    	
	        	    else
	        	        return(15);	
		        }	
		    }
		    else if(second_index == 6)
		    {
		    	if(!isTriplet)
		        	return(17);
		        else
		        {
		        	if(third_index == 7)
	        	        return(18);    	
	        	    else
	        	        return(17);	
		        }	
		    }
		    else if(second_index == 7)
		    {
		        return(19);	// The triplet with these two quadrants is already accounted for.
		    }
		    else
		    	return(0);  // No containing sets with the first two quadrants.
		}
		else if(first_index == 2)
		{
			if(second_index == 3)
		    {
				if(!isTriplet)
		        	return(20);
		        else
		        {
		        	if(third_index == 6)
	        	        return(21);    	
	        	    else if(third_index == 7)
	        	        return(22);	
	        	    else
	        	        return(20);	 // Return the id for the doublet since the third quadrant is irrelevant.
		        }	
		    }
			else if(second_index == 5)
		    {
				if(!isTriplet)
		        	return(23);
		        else
		        {
		        	if(third_index == 7)
	        	        return(24);    		
	        	    else
	        	        return(23);		
		        }	
		    }
			else
				return(0); // No containing sets with the first two quadrants.
		}
		else if(first_index == 3)
		{
			if(second_index == 4)
		    {
				if(!isTriplet)
		        	return(25);
		        else
		        {
		        	if(third_index == 6)
	        	        return(26);    	
	        	    else if(third_index == 7)
	        	        return(27);	
	        	    else
	        	        return(25);	 // Return the id for the doublet since the third quadrant is irrelevant.   	
		        }	
		    }
			else if(second_index == 7)
				return(28);  // Triplet already accounted for.
			else
				return(0);
		}
		else if(first_index == 4)
		{
			if(second_index == 5)
			    return(29);  
		    else
			    return(0);	// No other containing sets that start with quadrant 4.
		}
		else
			return(0);  // The first index was not less than five so there can be no containing polygon.
	}
	
}