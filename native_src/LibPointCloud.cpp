#include "PointCloud.h"

PointCloud* m_point_cloud;

extern "C" {
    int getFour(){
        return 4;
    }
    
    void load(char* filename){
        m_point_cloud = new PointCloud(filename, true); // true = load from binary
//        m_point_cloud->TransposePointsAndFixColors();
    }
    
    void loadBundle(char* filename){
         m_point_cloud = new PointCloud(filename, false, true); // false,true = load from TEXT BUNDLE file
//         m_point_cloud->TransposePointsAndFixColors();
    }
    
    int getNumPoints(){
        return m_point_cloud->m_num_points;
    }
    
    double* getPointPositions(){
        return gsl_matrix_ptr(m_point_cloud->m_gsl_points, 0, 0);
    }
    
    double* getPointColors(){
        return gsl_matrix_ptr(m_point_cloud->m_gsl_colors, 0, 0);
    }
    
    void makeKdTree(){
        m_point_cloud->MakeKdTree();
    }
    
    void queryKdTree(float x, float y, float z, float radius){
        m_point_cloud->QueryKdTree(x,y,z,radius);
    }
    
    void makeSplat(float x, float y, float z, float radius){
        m_point_cloud->MakeSplat(x,y,z,radius);
    }
    
    int getVertexCount(){
        return m_point_cloud->CountVerticesOfLastGeometry();
    }
    
    float* getVertices(){
        return m_point_cloud->GetVerticesOfLastGeometry();
    }
    
    double* fitPlane(int n, double *pts){
        return m_point_cloud->FitPlaneToPoints(n, pts);
    }
}
