/*
 *  GeometricComponent.cpp
 *  sketchyserver
 *
 *  Created by Kathleen Tuite on 5/1/11.
 *  Copyright 2011 The University of Washington. All rights reserved.
 *
 */

#ifdef __APPLE__
    #include <OpenGL/glu.h>
#else
    #include <GLUT/glu.h>
#endif

#include "GeometricComponent.h"
#include <cstring>
#include <algorithm>

GeometricComponent::GeometricComponent(){
	m_db_id = 0;
	//m_file_prefix = (char*)malloc(L_tmpnam*sizeof(char));
	char* tmp = (char*)malloc(L_tmpnam*sizeof(char));
	tmpnam(tmp);
		tmp += 15;

	strcpy(m_file_prefix, tmp);
	Init();
}

GeometricComponent::GeometricComponent(int db_id, char* file_prefix){
    m_db_id = db_id;
    //m_file_prefix = (char*)malloc(100*sizeof(char)); 
    strcpy(m_file_prefix, file_prefix);
    Init();
}

void GeometricComponent::NewName(){
	char* tmp = (char*)malloc(L_tmpnam*sizeof(char));
	tmpnam(tmp);
		tmp += 15;

	strcpy(m_file_prefix, tmp);
	printf("Renaming geometric component to: %s\n", m_file_prefix);
	fflush(stdout);
}

void GeometricComponent::Init(){
    m_type = POLY;
    m_cylinder = NULL;
    m_vertical_plane = NULL; 
    m_quad = NULL;
    m_texture.bitmap = NULL;
    m_preview = 0;
    m_ok_to_edit = true;
    
    printf("  [New geometric component with id %d created] Type: %d, File prefix: %s\n", m_db_id, m_type, m_file_prefix);
    fflush(stdout);
}

void GeometricComponent::Draw(){
    glBegin(GL_TRIANGLES);
        for (int i = 0; i < m_triangle_vertices.size(); i++){
            glColor3fv(m_triangle_vertices[i].color);
            glVertex3fv(m_triangle_vertices[i].pos);
            //printf("%d: %f %f %f\n", i, m_triangle_vertices[i].pos[0], m_triangle_vertices[i].pos[1], m_triangle_vertices[i].pos[2]);
        }
    glEnd();
    
    glColor4f(.1, .1, .1, .5);
   
    for (int i = 0; i < m_triangle_vertices.size(); i+=3){
        glBegin(GL_LINE_LOOP);
        glVertex3fv(m_triangle_vertices[i+0].pos);
        glVertex3fv(m_triangle_vertices[i+1].pos);
        glVertex3fv(m_triangle_vertices[i+2].pos);
        glEnd();
    }

    printf("color type: %d %d %d and texture stuff: %d %d\n", m_texture.bitmap[0],  m_texture.bitmap[1],  m_texture.bitmap[2],m_texture.w,m_texture.h);
    
}

void GeometricComponent::BuildTriangles(){
    printf("[BuildTriangles]");
    if (m_type == QUAD){
        printf(" From QUAD\n");
        BuildTrianglesFromQuad();        
    }
    else if (m_type == POLY && m_triangle_vertices.size() > 0){
        printf(" From POINTS\n");
        BuildTrianglesFromPoints();
    }
    
    /*
    printf("triangle verts: \n");
    for ( int i = 0; i < m_triangle_vertices.size(); i++){
        for (int k = 0; k < 3; k++){
            printf("%.20f ", m_triangle_vertices[i].pos[k]);
        }
        printf("\n");
    }
    */
    fflush(stdout);
}

void GeometricComponent::BuildTrianglesFromQuad(){

    /*
    printf("behold, the corners of the quad:\n");
    for (int i = 0; i < 4; i++){
        for (int j = 0; j < 3; j++){
            printf("%.20f ", m_quad->corners[i][j]);
        }
        printf("\n");
    }
    */
    
    // single color/pixel texture
	if (m_texture.bitmap == NULL){
		m_texture.w = 1;
        m_texture.h = 1;
        m_texture.bitmap = (int*)calloc(4, sizeof(int));
	
        m_texture.bitmap[0] = 150;
        m_texture.bitmap[1] = 150;
        m_texture.bitmap[2] = 150;
        m_texture.bitmap[3] = 150;
	}
	
	// turn that quad into 2 triangles
    std::vector<int> mapping;
    mapping.push_back(0);
    mapping.push_back(1);
    mapping.push_back(2);
    mapping.push_back(2);
    mapping.push_back(3);
    mapping.push_back(0);


    for (int i = 0; i < mapping.size(); i++){
        tex_tri_vertex vert;
        
        for (int k = 0; k < 3; k++){
            vert.pos[k]= m_quad->corners[mapping[i]][k];
        }
        
        for (int k = 0; k < 2; k++){
            vert.tex[k] = 0;
        }
        
        for (int k = 0; k < 4; k++){
            vert.color[k] = m_texture.bitmap[k]/255.0;
        }
        
        m_triangle_vertices.push_back(vert);
    }
    
    m_triangle_vertices[1].tex[1] = 1.0;
    m_triangle_vertices[2].tex[0] = 1.0;
    m_triangle_vertices[2].tex[1] = 1.0;
    m_triangle_vertices[3].tex[0] = 1.0;
    m_triangle_vertices[3].tex[1] = 1.0;
    m_triangle_vertices[4].tex[0] = 1.0;

}

void GeometricComponent::BuildTrianglesFromPoints(){
    int samp1 = m_texture.w;
    int samp2 = m_texture.h;
    
    if (m_texture.bitmap == NULL)
        m_texture.bitmap = (int*)calloc(4*samp1*samp2, sizeof(int));
    
    std::vector<tex_tri_vertex> orig_points = m_triangle_vertices;
    m_triangle_vertices.clear();
    
    for (int i = 0; i < samp1 - 1; i++){
        for (int j = 0; j < samp2 - 1; j++){
            
            int i1 = i*samp2 + j;
            int i2 = (i+1)*samp2 + j;
            int i3 = i1 + 1;
            int i4 = i2 + 1;
            
            m_triangle_vertices.push_back(orig_points[i1]);
            m_triangle_vertices.push_back(orig_points[i2]);
            m_triangle_vertices.push_back(orig_points[i3]);
            
            m_triangle_vertices.push_back(orig_points[i4]);
            m_triangle_vertices.push_back(orig_points[i3]);
            m_triangle_vertices.push_back(orig_points[i2]);
            
            for (int k = 0; k < 4; k++)
                m_texture.bitmap[(i*samp2 + j)*4 + k] = orig_points[i1].color[k]*255;
        }
    }
}

void GeometricComponent::WriteFile(char* dir){
    if (m_db_id != 0 || m_file_prefix == "")
        return;

    char filename[100];
    sprintf(filename, "users/%s/geom-%s.txt", dir, m_file_prefix);
    
    printf("[GeometricComponent::WriteFile] ***WRITING FILE*** file: %s\n", filename);
    fflush(stdout);
    
    int num_polygons = 0;
    
    FILE *fp;
    fp = fopen(filename, "w");
    fprintf(fp, "%s\n", m_file_prefix);
    fprintf(fp, "%d\n", m_db_id);
    fprintf(fp, "-1\n"); // a dummy commit id
    fprintf(fp, "%d\n", m_type);
    
    printf("file opened!\n");
    
    switch (m_type){
        case QUAD:
            printf(" case: quad\n");
            num_polygons = 1;
            fprintf(fp, "%d\n", num_polygons);
            fprintf(fp, "%f %f %f\n", m_triangle_vertices[0].color[0], m_triangle_vertices[0].color[1], m_triangle_vertices[0].color[2]); // color
            for (int c = 0; c < 4; c++){
                for (int k = 0; k < 3; k++){
                    fprintf(fp, "%.20f ", m_quad->corners[c][k]);
                } 
            }
            fprintf(fp, "\n");
            fclose(fp);
        break;
        
        case VERT_PLANE:
            printf(" case: vertical plane\n");
            fflush(stdout);
            if (m_vertical_plane == NULL){
                printf("the vert plane is null, that's not good. size of m_triangle_verts: %d\n", m_triangle_vertices.size());
                fflush(stdout);
                return;
            }
            num_polygons = 1;
            fprintf(fp, "%d\n", num_polygons);
            fprintf(fp, "%f %f %f\n", m_triangle_vertices[0].color[0], m_triangle_vertices[0].color[1], m_triangle_vertices[0].color[2]); // color
            fprintf(fp, "%.20f %.20f %.20f ", m_vertical_plane->min[0], m_vertical_plane->min[1], m_vertical_plane->min[2]);
            fprintf(fp, "%.20f %.20f %.20f ", m_vertical_plane->min[0], m_vertical_plane->max[1], m_vertical_plane->min[2]);
            fprintf(fp, "%.20f %.20f %.20f ", m_vertical_plane->max[0], m_vertical_plane->max[1], m_vertical_plane->max[2]);
            fprintf(fp, "%.20f %.20f %.20f ", m_vertical_plane->max[0], m_vertical_plane->min[1], m_vertical_plane->max[2]);
            fprintf(fp, "\n");
            fclose(fp);
        
        break;
        
        case POLY:
            printf(" case: polygon\n");
            fflush(stdout);
            num_polygons = m_triangle_vertices.size()/3; 
            for (int i = 0; i < num_polygons; i++){
                for (int v = 0; v < 3; v++){
                    for (int k = 0; k < 3; k++){
                        fprintf(fp, "%.20f ", m_triangle_vertices[i*3 + v].pos[k]);
                    }
                }
                fprintf(fp, "\n");
            }
            fclose(fp);
        
        break;
        
        default:
            printf("Unsupported primitive type, geometry not getting saved.\n");
            break;
    }
    
    WriteOriginPoints();
    DumpClassToFile();
    //WriteTexture();
    WritePlyFile();
    
}

void GeometricComponent::WriteOriginPoints(){
    char filename[100];
    sprintf(filename, "modelfiles/%s-origin-points.txt", m_file_prefix);
    printf("[GeometricComponent::WriteOriginPoints] this many origin points for this file: %d %s\n", m_origin_points.size(), m_file_prefix);

    sort (m_origin_points.begin(), m_origin_points.end());

    FILE *fp;
    fp = fopen(filename, "w");
    for (int i = 0; i < m_origin_points.size(); i++){
        fprintf(fp, "%d\n", m_origin_points[i]);
    }
    fclose(fp);
}

void GeometricComponent::DumpClassToFile(){
    char filename[100];
    sprintf(filename, "modelfiles/%s-class.txt", m_file_prefix);
    
    ofstream fout(filename,ios::binary);
    if (!fout) {
        printf("Unable to open for writing.\n");
        return;
    }

    fout.write( (char*) this, sizeof (*this) );
    fout.write( (char*) m_texture.bitmap, m_texture.h * m_texture.w * 4 * sizeof(int) );
    
    tex_tri_vertex* triangles = (tex_tri_vertex*)malloc(m_triangle_vertices.size()*sizeof(tex_tri_vertex));
    for (int i = 0; i < m_triangle_vertices.size(); i++){
        memcpy(&(triangles[i]), &(m_triangle_vertices[i]), sizeof(tex_tri_vertex));
    }
    
    fout.write( (char*) triangles, m_triangle_vertices.size()*sizeof(tex_tri_vertex) );
    
    fout.close();
}

void GeometricComponent::WriteTexture(){
    char filename[100];
    sprintf(filename, "modelfiles/%s-texture.txt", m_file_prefix);

    ofstream fout(filename,ios::binary);
    if (!fout) {
        printf("Unable to open for writing.\n");
        return;
    }

    int tex_size[2];
    tex_size[0] = m_texture.w;
    tex_size[1] = m_texture.h;
    fout.write( (char*) tex_size, sizeof (tex_size) );
    fout.write( (char*) m_texture.bitmap, m_texture.h * m_texture.w * 4 * sizeof(int) );    
    fout.close();
}

void GeometricComponent::WritePlyFile(){
    char filename[100];
    sprintf(filename, "modelfiles/%s.ply", m_file_prefix);
    
    
	int num_points = m_triangle_vertices.size();
	int num_faces = num_points / 3;
	
	
	FILE *fp;
	fp = fopen(filename, "w");
	fprintf(fp, "ply\n");
	fprintf(fp, "format ascii 1.0\n");
	fprintf(fp, "element vertex %d\n", num_points);
	fprintf(fp, "property float x\n");
	fprintf(fp, "property float y\n");
	fprintf(fp, "property float z\n");
	fprintf(fp, "property uchar diffuse_red\n");
	fprintf(fp, "property uchar diffuse_green\n");
	fprintf(fp, "property uchar diffuse_blue\n");
	fprintf(fp, "element face %d\n", num_faces);
	fprintf(fp, "property list uchar int vertex_index\n");
	fprintf(fp, "end_header\n");
	
	// print out the points of the triangles
	for (int i = 0; i < num_points; i++){
	   for (int k = 0; k < 3; k++)
	       fprintf(fp, "%.10f ", m_triangle_vertices[i].pos[k]);
	   for (int k = 0; k < 3; k++)
	       fprintf(fp, "%d ", int(m_triangle_vertices[i].color[k]*255.0));
	   fprintf(fp, "\n");
	}

	// print out which triangles are made out of which points
	for (int i = 0; i < num_faces; i++)
		fprintf(fp, "3 %d %d %d\n", 3*i + 0, 3*i + 1, 3*i + 2);

	fclose(fp);
	printf("done saving!! %s\n", filename);
}

