package megamu.mesh;

public class LinkedIndex {

	LinkedArray array;
	int index;
	int[] links;
	int linkCount;

	public LinkedIndex(LinkedArray a, int i) {
		array = a;
		index = i;
		links = new int[1];
		linkCount = 0;
	}

	public void linkTo(int i) {
		if (links.length == linkCount)
			links = expand(links);
		links[linkCount++] = i;
	}

	public boolean linked(int i) {
		for (int j = 0; j < linkCount; j++)
			if (links[j] == i)
				return true;
		return false;
	}

	public int[] getLinks() {
		return links;
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