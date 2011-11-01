#include <gsl/gsl_linalg.h>
#include <gsl/gsl_blas.h>
#include <gsl/gsl_sort_vector.h>
#include <stdlib.h>
#include <vector>
#include <map>
#include <ANN/ANN.h> 
#include "GeometricComponent.h"

#ifdef __APPLE__
    #include <OpenGL/glu.h>
#else
    #include <GLUT/glu.h>
#endif

using namespace std;

enum { PT_DRAW, PT_DELETE, PT_MARK, PT_MARK2 };
enum { VERTICAL_PLANE, ROOF_PLANE };

struct point {
    float x;
    float y;
    float z;
    int r;
    int g;
    int b;
	float u;
	float v;
}; 

struct structure_ptr {
	int idx;
	int flash_idx;
	int type;
};

struct plane{
    double a, b, c, d;
    double line_m;
    double line_b;
    double min[3];
    double max[3];
    double w, h;
    double center[3];
    int geom_id;
    int samp1, samp2;
};

#define BRANCH_FACTOR 5

struct adaptive_node {
    int pt_idx;
    int level;
    int parent_node;
};

struct adaptive_structure {
    int n;
    adaptive_node nodes[];
};

int compare_adaptive_nodes(const adaptive_node *a, const adaptive_node *b);

class PointCloud
{
public:
    PointCloud(char *filename, bool from_binary = 0, bool from_bundle = 0); 
    
    void LoadBinaryPointCloud(char *filename);
    void ReadBundleFile(char *filename);
    void WriteBinaryPointCloud(char *filename);
    void LoadLevels(char *filename);
    void WriteLevels(char *filename);
    
    void ClusterPoints();
    void ChangePointVisibility(int v);
    
    void MarkPointsFromFile(char *filename);
    bool MarkPointsFromString(char *pointString);
	bool MarkPointsFromBinaryStream(int *buffer, int size);
	
    bool DeletePointsFromString(char *pointString);
    void DeletePointsFromFile(char *filename);
    void UnmarkMarkedPoints();
    void MarkMarkedPointsForDrawing();

    void GroundPlane();
    void GroundPlaneOfDrawablePoints();

    void PlaneFloodFill();
    void PlaneFloodFillConnected(bool boundedByStroke = false);
    void PlaneSampleTextureTriangles(GeometricComponent* geom, plane pl, int idx = -1);
    void PlaneSampleTextureTrianglesMarkedPoints(GeometricComponent* geom, plane pl, int idx = -1);
    
    void PlaneFloodFillWindows();
    void MoreWindowProcessing();
    void WindowFromMarkedPoints();
    void SegmentWindowPoints();
    
    void ColorFloodFillConnected();
    void MakeTree();
    
    void CylinderFloodFillConnected();
    void MakeCylinder();
    
    void FitVerticalFeature();
	
	void SetMarkedPointPlane(double a, double b, double c, double d);
	void MakePlane();
	void MakeGroundAlignedPlane(double a, double b, double c, double d);
	void UpdateGroundAlignedPlane(int idx, double int_x, double int_z);
	
	void MakeWallFromFloorPlan(double *buffer, int num_points);
	void SampleTextureForGeom(GeometricComponent* geom);
	
	void GhettoTriangulateMarkedPoints();
	
	void MakeRoof();
	void ReflectRoof(GeometricComponent *geom, point roof_top1, point roof_top2, point roof_bottom1, point roof_bottom2);
	void ReflectPoint(float *x, float *y, double line_m, double line_b);
	
	void MakeStairs();
	
	void ClusterMarkedPoints();
	void MarkConnectedMarkedPoints();
	
	/*
    void TreeFloodFill();
    void GhettoTriangulateMarkedPoints();
    void FitCylinderToMarkedPoints();
    int find2DMeanVarianceOfMarkedPoints(double*, double*, double*); 
    void FindMinMaxOfMarkedPoints(double*, double*);
    void MakeKdTreeOfMarkedPoints(ANNkd_tree* kd_3d, ANNpointArray ann_points, gsl_matrix * colors, int num_marked_points);
    void groundAlignedTriangulate(double a, double b, double c, double d);
    void groundAlignedTriangulate2(float min_x, float min_y, float max_x, float max_y);
    void WindowFromMarkedPoints();
    void WallFromMarkedPoints();
    void PlaneFromMarkedPoints();
    */
    void DrawPoints(float size = 1);
    void DrawPrimitives();
	 
    GLUquadricObj *m_quadratic;
	 
	// Util
	void UtilCopyUserMarkedPoints(gsl_matrix *A, double *mean);
	void UtilCopyMarkedPoints(gsl_matrix *A, double *mean);
	void UtilCopyUserMarkedPoints2D(gsl_matrix *A, double *mean);
    float UtilColorDiff(int r1, int g1, int b1, int r2, int g2, int b2);
    
    void SetBasePointIndices();
    
    int m_num_points;
    bool m_has_normals; 
    
    gsl_matrix *m_gsl_points;	// basic storage of points 3xN
    gsl_matrix *m_gsl_normals;	// basic storage of point normals
    gsl_matrix *m_gsl_colors;	// basic storage of point colors

    int *m_draw;					// if coloring points differently, what to color them
    bool *m_delete;					// keep track of which points are not to be deleted
	structure_ptr *m_point_lookup;	// a way to see which geometry a point came from (limit 1 per point...)
	
	std::vector<int> m_user_marked_points;	// points marked by the user
	std::vector<int> m_marked_points;		// actual marked points, probably a super-set of user_marked points
	plane m_marked_point_plane;
	plane m_ground_plane;
	
	std::vector<GeometricComponent*> m_geometry;
	
    std::vector<int> m_planes_changed;
    
    adaptive_node *m_cluster_tree;
    adaptive_structure *m_cluster_structure;
    double m_bundle_version;
    
    int m_point_budget_base;
    int m_point_budget_extra;
    
    ANNpointArray m_ann_points;
    ANNkd_tree* m_kd_3d;
    ANNpoint m_query_pt;
    
    void MakeKdTree();
    int QueryKdTree(float x, float y, float z, float radius);
    void MakeSplat(float x, float y, float z, float radius);
    int CountVerticesOfLastGeometry();
    float* GetVerticesOfLastGeometry();
    double* FitPlaneToPoints(int n, double *pts);
    
    void TransposePointsAndFixColors();
    
	// all this below is crap
    /*
	std::vector<plane> m_vertical_planes;
	std::vector<cylinder> m_vertical_columns;
    std::vector<quad> m_quads;
    

	GeometricModel *m_model;
    */
};
