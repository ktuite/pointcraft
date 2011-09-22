#include "PointCloud.h"

PointCloud* m_point_cloud;

extern "C" {
    int getFour(){
        return 4;
    }
    
    void load(char* filename){
        m_point_cloud = new PointCloud(filename, true); // true = load from binary
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
}
