#ifndef GEOMETRIC_COMPONENT_H
#define GEOMETRIC_COMPONENT_H

/*
 *  GeometricComponent.h
 *  sketchyserver
 *
 *  Created by Kathleen Tuite on 5/1/11.
 *  Copyright 2011 The University of Washington. All rights reserved.
 *
 */

#include <stdlib.h>
#include <stdio.h>
#include <vector>
#include <fstream>

using namespace std;

enum PrimitiveType { POLY, VERT_PLANE, QUAD, CYLINDER };

struct texture{
	int w;
	int h;
	int *bitmap;
};

struct tex_tri_vertex {
    float pos[3];
    float tex[2];
    float color[4];
};

struct prim_cylinder{
    double radius;
    double top, bottom;
    double center[3];  
};

struct prim_vertical_plane{
    double line_m;
    double line_b;
    double min[3];
    double max[3];
    double center[3];
};

struct prim_quad{
    double a, b, c, d;
    double corners[4][3];
};

struct cylinder{
    double radius;
    double top, bottom;
    double center[3];
};

class GeometricComponent
{
public:
    int m_db_id;
    char m_file_prefix[100];
    char m_old_file_prefix[100];
    char m_actual_filename[100];
    bool m_preview;
    bool m_ok_to_edit;
    bool future1;
    bool future2;
    bool future3;
    bool future4;
    
    texture m_texture;
    std::vector<tex_tri_vertex> m_triangle_vertices;
    
    std::vector<int> m_origin_points;
    
    PrimitiveType m_type;
    prim_cylinder* m_cylinder;
    prim_vertical_plane* m_vertical_plane;
    prim_quad* m_quad;
    
	GeometricComponent();
	GeometricComponent(int db_id, char* file_prefix);
	void Init();
    void NewName();
	
	void Draw();
	
	void BuildTriangles();
	void BuildTrianglesFromPoints();
	
	void WriteFile(char* dir);
	
protected:
    void BuildTrianglesFromQuad();
    
    void WriteOriginPoints();
    void DumpClassToFile();
    void WriteTexture();
    void WritePlyFile();

};

#endif /* GEOMETRIC_COMPONENT_H */
