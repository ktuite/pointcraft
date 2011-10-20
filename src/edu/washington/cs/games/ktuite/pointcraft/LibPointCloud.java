package edu.washington.cs.games.ktuite.pointcraft;

import com.sun.jna.Native;
import com.sun.jna.Pointer;

public class LibPointCloud {
	static {
        Native.register("pointcloud");
    }
	
	public static native int getFour();
	public static native void load(String filename);
	public static native void loadBundle(String filename);
	public static native int getNumPoints();
	public static native Pointer getPointPositions();
	public static native Pointer getPointColors();
	public static native void makeKdTree();
	public static native int queryKdTree(float x, float y, float z, float radius);
	
	public static native void makeSplat(float x, float y, float z, float radius);
	public static native int getVertexCount();
	public static native Pointer getVertices();
}
