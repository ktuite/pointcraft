package megamu.mesh;

import java.lang.reflect.Array;

public class IntArray {

	int[] data;
	int length;

	public IntArray(){
		this(1);
	}

	public IntArray( int l ){
		data = new int[l];
		length = 0;
	}

	public void add( int d ){
		if( length==data.length )
			data = (int[]) expand(data);
		data[length++] = d;
	}

	public int get( int i ){
		return data[i];
	}

	public boolean contains( int d ){
		for(int i=0; i<length; i++)
			if(data[i]==d)
				return true;
		return false;
	}
	
	public static int[] expand( int[] a) {
		int length = a.length;
		int newLength = 2 * length;
		int[] newArray = new int[newLength];
		for (int i = 0; i < length; i++)
			newArray[i] = a[i];
		return newArray;
	}

}