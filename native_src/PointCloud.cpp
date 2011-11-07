#include "PointCloud.h"

#include <sys/mman.h>
#include <sys/stat.h>
#include <fcntl.h>
#include <fstream>


PointCloud::PointCloud(char *filename, bool from_binary, bool from_bundle){
    printf("[PointCloud] Creating a new Point Cloud from file %s\n", filename);
    
    if (from_binary){
        printf("[PointCloud] Loading from BINARY file\n");
        LoadBinaryPointCloud(filename);
        return;
    }
    else if (from_bundle){
        printf("[PointCloud] Loading from TEXT BUNDLE File\n");
        ReadBundleFile(filename);
        return;
    }
    else {
        printf("[PointCloud] Parsing a text file... this shall take a little while.\n");
    }
	
	//m_model = new GeometricModel();
	
    m_num_points = 0; 
    bool has_normals = false; 
    int num_fields = 9; // position and color and normal
    
    char prompt1[256], prompt2[256], prompt3[256];
    int eof;
    
    FILE *fp;
    fp = fopen(filename, "r");
    
    
    if (fp == NULL) {
        printf("failed to open %s!\n", filename);
        //return NULL;
    }
    
    eof = fscanf(fp, "%s", prompt1);
    if (eof == EOF || strcmp(prompt1, "ply") != 0) {
        printf("expect \"ply\" flag\n");
        //return NULL;
    }

    eof = fscanf(fp, "%s%s%s", prompt1, prompt2, prompt3);
    if (eof == EOF || strcmp(prompt1, "format") != 0 || strcmp(prompt2, "ascii") != 0 || strcmp(prompt3, "1.0") != 0) {
        printf("expect \"format ascii 1.0\"\n");
        //return NULL;
    }

    eof = fscanf(fp, "%s%s%d", prompt1, prompt2, &m_num_points);
    if (eof == EOF || strcmp(prompt1, "element") != 0 || strcmp(prompt2, "vertex") != 0) {
        printf("expect \"element vertex\"\n");
        //return NULL;
    }
    printf("NUM VERTICES: %d\n", m_num_points);

    if (m_num_points <= 0) {
        printf("invalid number of vertices\n");
        //return NULL;
    }

    /* Position */
    eof = fscanf(fp, "%s%s%s", prompt1, prompt2, prompt3);
    if (eof == EOF || strcmp(prompt1, "property") != 0 || strcmp(prompt2, "float") != 0 || strcmp(prompt3, "x") != 0) {
        printf("expect \"property float x\"\n");
        //return NULL;
    }

    eof = fscanf(fp, "%s%s%s", prompt1, prompt2, prompt3);
    if (eof == EOF || strcmp(prompt1, "property") != 0 || strcmp(prompt2, "float") != 0 || strcmp(prompt3, "y") != 0) {
        printf("expect \"property float y\"\n");
        //return NULL;
    }

    eof = fscanf(fp, "%s%s%s", prompt1, prompt2, prompt3);
    if (eof == EOF || strcmp(prompt1, "property") != 0 || strcmp(prompt2, "float") != 0 || strcmp(prompt3, "z") != 0) {
        printf("expect \"property float z\"\n");
        //return NULL;
    }

    /* Normal */
    eof = fscanf(fp, "%s%s%s", prompt1, prompt2, prompt3);
    if (eof == EOF || strcmp(prompt1, "property") != 0 || strcmp(prompt2, "float") != 0 || strcmp(prompt3, "nx") != 0) {
        printf("expect \"property float nx\"\n");
        //return NULL;
    }

    eof = fscanf(fp, "%s%s%s", prompt1, prompt2, prompt3);
    if (eof == EOF || strcmp(prompt1, "property") != 0 || strcmp(prompt2, "float") != 0 || strcmp(prompt3, "ny") != 0) {
        printf("expect \"property float ny\"\n");
        //return NULL;
    }

    eof = fscanf(fp, "%s%s%s", prompt1, prompt2, prompt3);
    if (eof == EOF || strcmp(prompt1, "property") != 0 || strcmp(prompt2, "float") != 0 || strcmp(prompt3, "nz") != 0) {
        printf("expect \"property float nz\"\n");
        //return NULL;
    }
    
    has_normals = true;


    /* Color */
    eof = fscanf(fp, "%s%s%s", prompt1, prompt2, prompt3);
    if (eof == EOF || strcmp(prompt1, "property") != 0 || strcmp(prompt2, "uchar") != 0 || strcmp(prompt3, "diffuse_red") != 0) {
        printf("expect \"property uchar diffuse_red\"\n");
        //return NULL;
    }

    eof = fscanf(fp, "%s%s%s", prompt1, prompt2, prompt3);
    if (eof == EOF || strcmp(prompt1, "property") != 0 || strcmp(prompt2, "uchar") != 0 || strcmp(prompt3, "diffuse_green") != 0) {
        printf("expect \"property uchar diffuse_green\"\n");
        //return NULL;
    }

    eof = fscanf(fp, "%s%s%s", prompt1, prompt2, prompt3);
    if (eof == EOF || strcmp(prompt1, "property") != 0 || strcmp(prompt2, "uchar") != 0 || strcmp(prompt3, "diffuse_blue") != 0) {
        printf("expect \"property uchar diffuse_blue\"\n");
        //return NULL;
    }

    eof = fscanf(fp, "%s", prompt1);
    if (eof == EOF || strcmp(prompt1, "end_header") != 0) {
        printf("expect \"end_header\"\n");
        //return NULL;
    }
    
    // alloc point array
    printf("number of points: %d, number of fields %d\n", m_num_points, num_fields);
    
    m_gsl_points = gsl_matrix_calloc(3, m_num_points);
    m_gsl_colors = gsl_matrix_calloc(3, m_num_points);
    m_gsl_normals = gsl_matrix_calloc(3, m_num_points);

    
    for (int i = 0; i < m_num_points; i++) {
        double x,y,z;
        double nx, ny, nz;
        int r,g,b;

        /* Read position */
        eof = fscanf(fp, "%lf %lf %lf", &x, &y, &z);
        if (eof == EOF) {
            printf("fail to load vertex %d\n",i);
            //return NULL;
        }

        gsl_matrix_set(m_gsl_points, 0, i, x);
        gsl_matrix_set(m_gsl_points, 1, i, y);
        gsl_matrix_set(m_gsl_points, 2, i, z);


        /* Read normal */
        eof = fscanf(fp, "%lf %lf %lf", &nx, &ny, &nz);
        if (eof == EOF) {
            printf("fail to load vertex normal %d\n",i);
            //return NULL;
        }

        gsl_matrix_set(m_gsl_normals, 0, i, nx);
        gsl_matrix_set(m_gsl_normals, 1, i, ny);
        gsl_matrix_set(m_gsl_normals, 2, i, nz);

        /* Read color */
        eof = fscanf(fp, "%d %d %d", &r, &g, &b);
        if (eof == EOF) {
            printf("fail to load vertex color %d\n",i);
            //return NULL;
        }
        
        gsl_matrix_set(m_gsl_colors, 0, i, r);
        gsl_matrix_set(m_gsl_colors, 1, i, g);
        gsl_matrix_set(m_gsl_colors, 2, i, b);
    }
    
    m_draw = (int*) calloc(m_num_points, sizeof(int));
	m_delete = (bool*) calloc(m_num_points, sizeof(bool));
	m_point_lookup = (structure_ptr*) calloc(m_num_points, sizeof(structure_ptr));
    for (int i = 0; i < m_num_points; i++){
		m_point_lookup[i].idx = -1;
        m_point_lookup[i].flash_idx = -1;
	}
	
    fclose(fp);
}

void PointCloud::LoadBinaryPointCloud(char *filename){
    m_num_points = 0; 
    bool has_normals = false; 
    int num_fields = 6; // position and color and normal
    
    FILE *fp;
    fp = fopen(filename, "r");
    fread(&m_num_points, sizeof(int), 1, fp);
    
    printf("Loading binary data: number of points: %d\n", m_num_points);
    
    m_gsl_points = gsl_matrix_calloc(3, m_num_points);
    m_gsl_colors = gsl_matrix_calloc(3, m_num_points);
    //m_gsl_normals = gsl_matrix_calloc(3, m_num_points);
    
    int success;
    success = gsl_matrix_fread(fp, m_gsl_points);
    printf("loaded points successfully? %d\n", success);
    fflush(stdout);
    success = gsl_matrix_fread(fp, m_gsl_colors);
    printf("loaded m_gsl_colors successfully? %d\n", success);
    fflush(stdout);
    //success = gsl_matrix_fread(fp, m_gsl_normals);
    //printf("loaded m_gsl_normals successfully? %d\n", success);
    //fflush(stdout);
    
    m_draw = (int*) calloc(m_num_points, sizeof(int));
    m_delete = (bool*) calloc(m_num_points, sizeof(bool));
	m_point_lookup = (structure_ptr*) calloc(m_num_points, sizeof(structure_ptr));
    for (int i = 0; i < m_num_points; i++){
		m_point_lookup[i].idx = -1;
        m_point_lookup[i].flash_idx = -1;
	}

    fclose(fp);
}

void PointCloud::ReadBundleFile(char *filename)
{
    FILE *f;
    f = fopen(filename, "r");
    
    printf("[TexViewerApp::ReadBundleFile] Reading file...\n");

    if (f == NULL) {
        printf("Error opening file for reading\n");
        return;
    }

    int num_images, num_points;
    bool binary = false;
    int include_obs = 1;
    bool features_coalesced = false;

    char first_line[256];
    fgets(first_line, 256, f);
    if (first_line[0] == '#') {
        double version;
        sscanf(first_line, "# Bundle file v%lf", &version);

        m_bundle_version = version;
        printf("[ReadBundleFile] Bundle version: %0.3f\n", version);

        /* Check if the file is in binary format */
        int len = strlen(first_line);
        int start = len - strlen("(binary)\n");
        if (strcmp(first_line + start, "(binary)\n") == 0) {
            binary = true;
            printf("[ReadBundleFile] File is in binary format\n");
        }
        
        if (m_bundle_version >= 0.4) {
            int coalesced = 0;
            include_obs = 1;

            if (!binary) {
                fscanf(f, "%d %d %d", &num_images, &num_points, &coalesced);
            } else {
                fread(&num_images, sizeof(int), 1, f);
                fread(&num_points, sizeof(int), 1, f);
                fread(&coalesced, sizeof(int), 1, f);
            }
            
            if (m_bundle_version >= 0.5) {
                if (!binary) {
                    fscanf(f, "%d", &include_obs);
                } else {
                    fread(&include_obs, sizeof(int), 1, f);
                }
            }

            if (coalesced)
                features_coalesced = true;
        } else {
            if (!binary) {
                fscanf(f, "%d %d\n", &num_images, &num_points);
            } else {
                fread(&num_images, sizeof(int), 1, f);
                fread(&num_points, sizeof(int), 1, f);
            }
        }
    } else if (first_line[0] == 'v') {
        double version;
        sscanf(first_line, "v%lf", &version);
        m_bundle_version = version;
        printf("[ReadBundleFile] Bundle version: %0.3f\n", version);

        fscanf(f, "%d %d\n", &num_images, &num_points);
    } else {
        m_bundle_version = 0.1;
        printf("[ReadBundleFile] Bundle version: %0.3f\n", m_bundle_version);
        sscanf(first_line, "%d %d\n", &num_images, &num_points);
    }

    printf("[ReadBundleFile::ReadBundleFile] Reading %d images and %d points...\n", num_images, num_points);
    m_num_points = num_points;
    fflush(stdout);

    /* Read cameras */
    for (int i = 0; i < num_images; i++) {
        double focal_length;
        double R[9];
        double t[3];
        double k[2] = { 0.0, 0.0 };
        int player_id = -1;
        int w, h;
              
                    printf("CAMERA %d, %f\n", i, m_bundle_version);
            fflush(stdout);
                    
        if (m_bundle_version >= 0.4) {
            char name[512];
            double focal_est;
            


            if (!binary) {
                fscanf(f, "%s %d %d %lf %d\n", 
                       name, &w, &h, &focal_est, &player_id); // &w, &h);
            } else {
                // fscanf(f, "%s %d %d %lf %d\n", 
                //        name, &w, &h, &focal_est, &player_id); // &w, &h);
                
                int name_len;
                fread(&name_len, sizeof(int), 1, f);
                fread(name, sizeof(char), name_len, f);
                name[name_len] = 0;
                fread(&w, sizeof(int), 1, f);
                fread(&h, sizeof(int), 1, f);
                fread(&focal_est, sizeof(double), 1, f);
                fread(&player_id, sizeof(int), 1, f);
            }

        }
        
        /* Focal length */
        if (m_bundle_version > 0.1) {
            if (!binary) {
                fscanf(f, "%lf %lf %lf\n", &focal_length, k+0, k+1);
            } else {
                fread(&focal_length, sizeof(double), 1, f);
                fread(k, sizeof(double), 2, f);
            }
        } else {
            fscanf(f, "%lf\n", &focal_length);
        }

        /* Rotation and translation */
        if (!binary) {
            fscanf(f, "%lf %lf %lf\n%lf %lf %lf\n%lf %lf %lf\n", 
                   R+0, R+1, R+2, R+3, R+4, R+5, R+6, R+7, R+8);
            fscanf(f, "%lf %lf %lf\n", t+0, t+1, t+2);
        } else {
            fread(R, sizeof(double), 9, f);
            fread(t, sizeof(double), 3, f);
        }

        if (focal_length > 100.0) {
            /* only if camera info is good */
            /*
            Camera cd;

            cd.m_width = w;
            cd.m_height = h;
            cd.m_focal = focal_length;
            cd.m_k[0] = k[0];
            cd.m_k[1] = k[1];
            memcpy(cd.m_R, R, sizeof(double) * 9);
            memcpy(cd.m_t, t, sizeof(double) * 3);
            */

        }
    }

    m_gsl_points = gsl_matrix_calloc(3, m_num_points);
    m_gsl_colors = gsl_matrix_calloc(3, m_num_points);


    int num_min_views_points = 0;
    for (int i = 0; i < num_points; i++) {
        double pos[3];
        int color[3];
        if (i % 500 == 0){
            printf("reading point %d\n", i);
            fflush(stdout);
        }

        /* Player ID */
        int player_id; // unused here
        if (m_bundle_version >= 0.4) {
            if (!binary)
                fscanf(f, "%d\n", &(player_id));
            else
                fread(&(player_id), sizeof(int), 1, f);
        }

        /* Position and color */
        if (!binary) {
            fscanf(f, "%lf %lf %lf\n", 
                   pos + 0, pos + 1, pos + 2);
            fscanf(f, "%d %d %d\n", 
                   color + 0, color + 1, color + 2);
        } else {
            fread(pos, sizeof(double), 3, f);
            // fread(pt.m_color, sizeof(float), 3, f);
            fread(color, sizeof(unsigned char), 3, f);
        }

        for (int k = 0; k < 3; k++){ 
            gsl_matrix_set(m_gsl_points, k, i, pos[k]);
            gsl_matrix_set(m_gsl_colors, k, i, double(color[k]));
        }


        int num_visible;

        if (!binary) {
            fscanf(f, "%d", &num_visible);
            // pt.m_num_vis = num_visible;
        } else {
            fread(&num_visible, sizeof(int), 1, f);
        }
        
        if (num_visible >=3)
            num_min_views_points++;

        // pt.m_views.resize(num_visible);
        for (int j = 0; j < num_visible; j++) {
            int view, key;

            if (!binary) {
                fscanf(f, "%d %d", &view, &key);
            } else {
                fread(&view, sizeof(int), 1, f);
                fread(&key, sizeof(int), 1, f);
            }
            
            //pt.m_views.push_back(ImageKey(view, key));
            
            if (m_bundle_version >= 0.3 && include_obs) {
                float x, y;
                if (!binary) {
                    fscanf(f, "%f %f", &x, &y);
                } else {
                    fread(&x, sizeof(float), 1, f);
                    fread(&y, sizeof(float), 1, f);
                }
            }
        }
        
    }


    fclose(f);
    printf("[ReadBundleFile] %d / %d points visible to more than 2 cameras\n", 
           num_min_views_points, num_points);
}


void PointCloud::TransposePointsAndFixColors(){
    // transpose points so instead of [xxx,yyy,zzz] its [xyz, xyz, xyz]
    gsl_matrix *m_gsl_points_fixed = gsl_matrix_calloc(m_num_points,3);
    gsl_matrix_transpose_memcpy(m_gsl_points_fixed, m_gsl_points);
    gsl_matrix_free(m_gsl_points);
    m_gsl_points = m_gsl_points_fixed;
    
    // make colors be in range 0-1 isntead of 0-255
    gsl_matrix_scale(m_gsl_colors, 1/255.0);
    
    // transpose colors, too
    gsl_matrix *m_gsl_colors_fixed = gsl_matrix_calloc(m_num_points,3);
    gsl_matrix_transpose_memcpy(m_gsl_colors_fixed, m_gsl_colors);
    gsl_matrix_free(m_gsl_colors);
    m_gsl_colors = m_gsl_colors_fixed;
}

void PointCloud::SetBasePointIndices(){
    for (int i = 0; i < m_num_points; i++){
        m_point_lookup[i].flash_idx = -1;
	}
	
    for (int h = 0; h < m_point_budget_base; h++){
        int i = m_cluster_tree[h].pt_idx;
        m_point_lookup[i].flash_idx = h;
    }
}

void PointCloud::WriteBinaryPointCloud(char *filename){
    FILE *fp;
    fp = fopen(filename, "w");
    fwrite(&m_num_points, sizeof(int), 1, fp);
    
    printf("Saving binary data: number of points: %d\n", m_num_points);
    
    int success;
    gsl_matrix_fwrite(fp, m_gsl_points);
    gsl_matrix_fwrite(fp, m_gsl_colors);
    //gsl_matrix_fwrite(fp, m_gsl_normals);

    fclose(fp);
}

void PointCloud::LoadLevels(char *filename){
    printf("[LoadLevels] %s\n", filename);
    m_cluster_structure = (adaptive_structure*)malloc(sizeof(int) + m_num_points*sizeof(adaptive_node));
    
    int fd;
    struct stat sb;
    fd = open(filename, O_RDONLY);
    fstat(fd, &sb);
    printf("this file has a size! it is this big: %d\n\n", int(sb.st_size));

    m_cluster_structure = (adaptive_structure*)mmap(NULL, sb.st_size, PROT_READ, MAP_PRIVATE, fd, 0);
    close(fd);
    
    printf("number loaded: %d... and also parent of random thing: %d\n", m_cluster_structure->n, m_cluster_structure->nodes[m_num_points/20].parent_node);

    m_cluster_tree = m_cluster_structure->nodes;
    fflush(stdout);
}

void PointCloud::WriteLevels(char *filename){
    printf("[WriteLevels] %s\n", filename);
    fflush(stdout);
    ofstream fout(filename, ios::binary);
    fout.write( (char*) &m_num_points, sizeof(int));
    fout.write( (char*) m_cluster_tree, sizeof(adaptive_node)*m_num_points );
    fout.close();
}

int compare_adaptive_nodes(const void *an, const void *bn){
    adaptive_node *a = (adaptive_node*)an;
    adaptive_node *b = (adaptive_node*)bn;
    int temp = b->level - a->level;
    if (temp > 0)
        return 1;
    else if (temp < 0)
        return -1;
    else
        return 0;
}

int compare_double_floats(const void *an, const void *bn){
    float *a = (float*)an;
    float *b = (float*)bn;
    float temp = *(a+1) - *(b+1);
    if (temp > 0)
        return 1;
    else if (temp < 0)
        return -1;
    else
        return 0;
}

void PointCloud::MakeKdTree(){
    printf("[PointCloud::MakeKdTree] \n");
    
    int old_time = clock();
    float time_diff = 0;
    
    m_query_pt = annAllocPt(3);
    
    int num_points = m_num_points;
    m_ann_points = annAllocPts(num_points, 3); 
    for (int i = 0; i < num_points; i++){
        for (int k = 0; k < 3; k++){
            m_ann_points[i][k] = gsl_matrix_get(m_gsl_points, k, i);
        }
    }
    
    time_diff = (clock() - old_time) / 1000000.0;
    old_time = clock();
    printf("     Time to copy points into ann array: %f\n", time_diff);
    
    m_kd_3d = new ANNkd_tree(m_ann_points, num_points, 3);
    time_diff = (clock() - old_time) / 1000000.0;
    old_time = clock();
    printf("     Time to build ann tree: %f\n", time_diff);
    fflush(stdout);
}

int PointCloud::QueryKdTree(float x, float y, float z, float radius){
    m_query_pt[0] = x;
    m_query_pt[1] = y;
    m_query_pt[2] = z;
    int neighbors = m_kd_3d->annkFRSearch(m_query_pt, radius*radius, 0);
    return neighbors;
}

double* PointCloud::QueryKdTreeGetCenter(float x, float y, float z, float radius){
    m_query_pt[0] = x;
    m_query_pt[1] = y;
    m_query_pt[2] = z;
    
     double* center = (double*)malloc(3*sizeof(double));
     for (int k = 0; k < 3; k++)
        center[k] = 0;
    
    int max_neighbors = 100;
    ANNidxArray nnIdx = new ANNidx[max_neighbors];
    ANNdistArray dists = new ANNdist[max_neighbors];
    
    int neighbors = m_kd_3d->annkFRSearch(m_query_pt, radius*radius, max_neighbors, nnIdx, dists);
    double weights = 0;
    for (int j = 0; j < min(neighbors, max_neighbors); j++){
        weights += dists[j];
        for (int k = 0; k < 3; k++){
            center[k] += gsl_matrix_get(m_gsl_points, k, nnIdx[j])*dists[j];
        }
    }
    
    for (int k = 0; k < 3; k++)
        center[k] /= weights; 
       
    return center;
}

void PointCloud::MakeSplat(float x, float y, float z, float radius){
    m_query_pt[0] = x;
    m_query_pt[1] = y;
    m_query_pt[2] = z;
    
    m_user_marked_points.clear();
    int max_neighbors = 10000;
    ANNidxArray nnIdx = new ANNidx[max_neighbors];
    ANNdistArray dists = new ANNdist[max_neighbors];
    
    int neighbors = m_kd_3d->annkFRSearch(m_query_pt, radius*radius, max_neighbors, nnIdx, dists, 0);

    for (int j = 0; j < min(neighbors, max_neighbors); j++){
        m_user_marked_points.push_back(nnIdx[j]);
    }
    printf("%d points marked\n", m_user_marked_points.size());
    fflush(stdout);
    PlaneFloodFillConnected(true);
    GhettoTriangulateMarkedPoints();
}

int PointCloud::CountVerticesOfLastGeometry(){ 
    if(m_geometry.size() == 0)
        return 0;   
    
    GeometricComponent* geom = m_geometry.back();
    int num_verts = int( geom->m_triangle_vertices.size() );
    //printf("number of vertices: %d\n", num_verts);
    fflush(stdout);
    
    return num_verts;
}

float* PointCloud::GetVerticesOfLastGeometry(){  
     
    GeometricComponent* geom = m_geometry.back();
    int num_verts = geom->m_triangle_vertices.size();
    
    float* tri = (float*)malloc(num_verts*sizeof(float)*3);
    for (int i = 0; i < num_verts; i++){
       for(int k = 0; k < 3; k++)
            tri[i*3 + k] = geom->m_triangle_vertices[i].pos[k];
    }
    
    return tri;
}

double* PointCloud::FitPlaneToPoints(int n, double *pts){
    // fit a plane to the points made by some pellets in java
    gsl_matrix *sample_points = gsl_matrix_calloc(n, 3);
    
    double mean[3];
    for (int k = 0; k < 3; k++)
		mean[k] = 0;
		
    for (int i = 0; i < n; i++){
        for (int k = 0; k < 3; k++){
            gsl_matrix_set(sample_points, i, k, pts[i*3 + k]);
            mean[k] += pts[i*3 + k];
        }
    }
    
    for (int k = 0; k < 3; k++)
		mean[k] /= n;
		
    for (int i = 0; i < n; i++){
        for (int k = 0; k < 3; k++){
            gsl_matrix_set(sample_points, i, k, gsl_matrix_get(sample_points, i, k) - mean[k]);
        }
    }

    printf("about to run SVD\n");
    fflush(stdout);
    
    gsl_vector *S = gsl_vector_calloc(3);
    gsl_matrix *V = gsl_matrix_calloc(3,3);
    gsl_vector *work = gsl_vector_calloc(3);

    // run SVD!!! singular value decomposition! 
    int res = gsl_linalg_SV_decomp(sample_points, V, S, work);
    
    // now that we have SVD, let's get the plane normal and do some plane math
    double a = gsl_matrix_get(V, 0, 2);
    double b = gsl_matrix_get(V, 1, 2);
    double c = gsl_matrix_get(V, 2, 2);
    
    printf("plane normal: %f %f %f\n", a,b,c);
    double d = -1 * (a * mean[0] + b * mean[1] + c * mean[2]);
    fflush(stdout);
    
    double* plane = (double*)malloc(4 * sizeof(double));
    plane[0] = a;
    plane[1] = b;
    plane[2] = c;
    plane[3] = d;
    
    return plane;
}

double* PointCloud::FitLineToPoints(int n, double *pts){
    // fit a plane to the points made by some pellets in java
    gsl_matrix *sample_points = gsl_matrix_calloc(n, 3);
    
    double mean[3];
    for (int k = 0; k < 3; k++)
		mean[k] = 0;
		
    for (int i = 0; i < n; i++){
        for (int k = 0; k < 3; k++){
            gsl_matrix_set(sample_points, i, k, pts[i*3 + k]);
            mean[k] += pts[i*3 + k];
        }
    }
    
    for (int k = 0; k < 3; k++)
		mean[k] /= n;
		
    for (int i = 0; i < n; i++){
        for (int k = 0; k < 3; k++){
            gsl_matrix_set(sample_points, i, k, gsl_matrix_get(sample_points, i, k) - mean[k]);
        }
    }


    double* line_guts = (double*)malloc(6 * sizeof(double));

    if (n >= 3){
        printf("about to run SVD\n");
        fflush(stdout);
        
        gsl_vector *S = gsl_vector_calloc(3);
        gsl_matrix *V = gsl_matrix_calloc(3,3);
        gsl_vector *work = gsl_vector_calloc(3);
    
        // run SVD!!! singular value decomposition! 
        int res = gsl_linalg_SV_decomp(sample_points, V, S, work);
        
        // now that we have SVD, let's get the direction of the line
        double a = gsl_matrix_get(V, 0, 0);
        double b = gsl_matrix_get(V, 1, 0);
        double c = gsl_matrix_get(V, 2, 0);
        
        
        
        line_guts[0] = a;
        line_guts[1] = b;
        line_guts[2] = c;
    }
    else if (n == 2){
        for (int k = 0; k < 3; k++)
            line_guts[k] = pts[1*3 + k] - pts[0*3 + k];
    }

    line_guts[3] = mean[0];
    line_guts[4] = mean[1];
    line_guts[5] = mean[2];
    
    return line_guts;
    
}

void PointCloud::ClusterPoints(){
    printf("[PointCloud::ClusterPoints] \n");
    
    int old_time = clock();
    float time_diff = 0;
    
    int num_points = m_num_points;
    ANNpointArray ann_points = annAllocPts(num_points, 3); 
    for (int i = 0; i < num_points; i++){
        for (int k = 0; k < 3; k++){
            ann_points[i][k] = gsl_matrix_get(m_gsl_points, k, i);
        }
    }
    
    time_diff = (clock() - old_time) / 1000000.0;
    old_time = clock();
    printf("     Time to copy points into ann array: %f\n", time_diff);
    
    ANNkd_tree* kd_3d = new ANNkd_tree(ann_points, num_points, 3);
    time_diff = (clock() - old_time) / 1000000.0;
    old_time = clock();
    printf("     Time to build ann tree: %f\n", time_diff);
    
    // step 1, find avg distance between neighboring points
    double avg_distance = 0;
    ANNpoint queryPt = annAllocPt(3);
    ANNidxArray nnIdx = new ANNidx[BRANCH_FACTOR];
    ANNdistArray dists = new ANNdist[BRANCH_FACTOR];
    
    int i, j, k;
    double dist;
    for (i = 0; i < num_points; i++){
        for (k = 0; k < 3; k++)
            queryPt[k] = ann_points[i][k];
        
        kd_3d->annkSearch(queryPt, BRANCH_FACTOR, nnIdx, dists, 0);
        dist = 0;
        for (j = 0; j < BRANCH_FACTOR; j++)
            avg_distance += dists[j];
        dist /= BRANCH_FACTOR;
        avg_distance += dist;
        
        if (i%1000000 == 0)
            printf("i = %d\n", i);
        
    }
    avg_distance /= num_points;


    printf("average distance: %.20f\n", avg_distance);
    time_diff = (clock() - old_time) / 1000000.0;
    old_time = clock();
    printf("     Time to loop through all %d points %f\n", num_points, time_diff);
    fflush(stdout);
    
    // step 2, start building the tree
    double radius = avg_distance * 2;
    adaptive_node *adapt = (adaptive_node*)calloc(num_points, sizeof(adaptive_node));
    m_cluster_tree = adapt;
    int *marked_points = (int*)malloc(num_points*sizeof(int));
    for (int i = 0; i < num_points; i++){
        m_cluster_tree[i].pt_idx = i;
        m_cluster_tree[i].level = -1;
        m_cluster_tree[i].parent_node = -1;
        //for (int j = 0; j < BRANCH_FACTOR; j++){
        //    m_cluster_tree[i].children[j] = -1;
        //}
        marked_points[i] = i;
    }
    
    ANNpointArray new_ann_points;
    
    int unclustered_nodes = num_points;
    int lowest_level = 0;
    
    while (unclustered_nodes > 5){
        printf("number of unclustered nodes left this time: %d\n", unclustered_nodes);
        unclustered_nodes = 0;
        int points_in_this_level = 0;
        
        for (int i = 0; i < num_points; i++){
            if (m_cluster_tree[i].level == -1){
                // this point is open for clustering, turning into a node, attaching children
                m_cluster_tree[i].level = lowest_level + 1;
                unclustered_nodes++;
                
                for (int k = 0; k < 3; k++)
                    queryPt[k] = ann_points[i][k];

                int neighbors = kd_3d->annkFRSearch(queryPt, radius, BRANCH_FACTOR, nnIdx, dists, 0);
                
                for (int j = 0; j < min(neighbors, BRANCH_FACTOR); j++){
                    int idx = marked_points[nnIdx[j]];
                    if (m_cluster_tree[idx].level == -1){
                        m_cluster_tree[idx].level = lowest_level;
                        m_cluster_tree[idx].parent_node = i;
                        //m_cluster_tree[i].children[j] = idx;
                        //m_cluster_tree[i].children2.push_back(idx);
                        points_in_this_level ++;
                    }
                }
            }   
        }
        lowest_level ++;
        printf("after most things, lowest level: %d\n", lowest_level);
        delete kd_3d;
        free(marked_points);
        new_ann_points = annAllocPts(unclustered_nodes, 3);
        marked_points = (int*)malloc(unclustered_nodes*sizeof(int));
        int h = 0;
        for (int i = 0; i < num_points; i++){
            if (m_cluster_tree[i].level == lowest_level){
                m_cluster_tree[i].level = -1;
                for (int k = 0; k < 3; k++){
                    new_ann_points[h][k] = ann_points[i][k];
                }
                marked_points[h] = i;
                h++;
            }
        }
        kd_3d = new ANNkd_tree(new_ann_points, unclustered_nodes, 3);
        radius *= 2.0;
        //m_nodes_in_each_level.push_back(points_in_this_level);
    }
    
    //m_top_level = m_cluster_tree[0].level;
    //m_max_level = lowest_level;
    
    printf(" about to qsort \n");
    fflush(stdout);
    // next step: rearrange so pts clustered by level and in descending order...
    qsort( m_cluster_tree+1, num_points-1, sizeof(adaptive_node), compare_adaptive_nodes );
    printf("done running qsort\n");
    fflush(stdout);
    
    int *reverse_map = (int*)malloc(sizeof(int)*num_points);

    for (int i = 0; i < num_points; i++){
        reverse_map[ m_cluster_tree[i].pt_idx ] = i;
    }
    printf("done building reverse map\n");
    fflush(stdout);

    for (int i = 0; i < num_points; i++){
        m_cluster_tree[i].parent_node = reverse_map[ m_cluster_tree[i].parent_node ];
        if (m_cluster_tree[i].parent_node > i){
            printf("**** there's a problem wiht parent ordering!!!! %d (and parent: %d) ****\n", i,m_cluster_tree[i].parent_node);
            printf(" my level: %d\n", m_cluster_tree[i].level);
        }
    }
    
    fflush(stdout);
}

void PointCloud::ChangePointVisibility(int v){
    for (int i = 0; i < m_num_points; i++){
        m_draw[i] = v;
    }
}

void PointCloud::MarkPointsFromFile(char *filename){
    int point_idx;
    
    FILE *fp;
    fp = fopen(filename, "r");
    
    if (fp == NULL) {
        printf("failed to open %s!\n", filename);
    }
    else {
        printf("- opened file %s for reading\n", filename);
    }
    
    while ( fscanf(fp, "%d,", &point_idx) != EOF){
        if (point_idx < m_num_points && !m_delete[point_idx]){
            m_user_marked_points.push_back(point_idx);        }
    }
	
    fclose(fp);
	printf("[PointCloud::MarkPointsFromFile] user points now marked: %d\n", m_user_marked_points.size());
}

bool PointCloud::MarkPointsFromString(char *pointString){
    printf("[PointCloud::MarkPointsFromString] start of function\n");
    fflush(stdout);
    int point_idx;
    int sn;
    bool terminated = false;
    
    while( pointString[0] != ',' )
        pointString++;
    pointString++;

    while ( sscanf(pointString, "%d%n,", &point_idx, &sn) != EOF){
        pointString += (sn + 1);
        if (point_idx == -1){
            terminated = true;
            break;
        }
        if (point_idx < m_num_points && !m_delete[point_idx]){
            m_user_marked_points.push_back(point_idx);
        }
    }
    if (!terminated){
        // undo marking of last point
        m_user_marked_points.pop_back();
    }
    
	printf("[PointCloud::MarkPointsFromString] user points now marked: %d, last point index: %d\n", m_user_marked_points.size(), m_user_marked_points.back());
	fflush(stdout);
    return terminated;
}

bool PointCloud::MarkPointsFromBinaryStream(int *buffer, int size){
    printf("[PointCloud::MarkPointsFromBinaryStream] start of function\n");
    fflush(stdout);
    
    size /= 4;
    
    bool terminate = false;
    
    for (int i = 0; i < size; i++){
        printf("%d ", buffer[i]);
        if (buffer[i] < 0){
            terminate = true;
            break;
        }
        m_user_marked_points.push_back(buffer[i]);
    }
    
    printf("[PointCloud::MarkPointsFromString] user points now marked: %d... terminated = %d\n", m_user_marked_points.size(), terminate);
	fflush(stdout);
	
	return terminate;
}

bool PointCloud::DeletePointsFromString(char *pointString){
    int point_idx;
    int sn;
	int n = 0;
    bool terminated = false;
    
    while( pointString[0] != ',' )
        pointString++;
    pointString++;

    while ( sscanf(pointString, "%d%n,", &point_idx, &sn) != EOF){
        pointString += (sn + 1);
        if (point_idx == -1){
            terminated = true;
            break;
        }
        if (point_idx < m_num_points){
            m_delete[point_idx] = true;
			n++;
        }
    }
    if (!terminated){
        // undo marking of last point
        m_delete[point_idx] = false;
		n--;
    }
    
	printf("[PointCloud::DeletePointsFromString] points deleted: %d\n", n);
    return terminated;
}

void PointCloud::DeletePointsFromFile(char *filename){
    int point_idx;
    int n = 0;
    FILE *fp;
    fp = fopen(filename, "r");
    
    
    if (fp == NULL) {
        printf("failed to open %s!\n", filename);
    }
    else {
        printf("- opened file %s for reading\n", filename);
    }
    
    while ( fscanf(fp, "%d,", &point_idx) != EOF){
        m_delete[point_idx] = true;
		n++;
    }
    
    fclose(fp);
	printf("[PointCloud::DeletePointsFromFile] points deleted: %d\n", n);
}

void PointCloud::UnmarkMarkedPoints(){
	m_user_marked_points.clear();
    m_marked_points.clear();
    
    for (int i = 0; i < m_num_points; i++)
        if (0 && m_delete[i] == false)  
            m_draw[i] = 0;
}

void PointCloud::MarkMarkedPointsForDrawing(){
    for (int h = 0; h < m_marked_points.size(); h++){
        int i = m_marked_points[h];
        if (m_delete[i] == false) {
            m_draw[i] = 1;
        }
    }
    
    for (int h = 0; h < m_user_marked_points.size(); h++){
        int i = m_user_marked_points[h];
        if (m_delete[i] == false) {
            m_draw[i] = 2;
        }
    }
}



void PointCloud::DrawPoints(float size){
    float x, y, z;
    int r,g,b;

/*
    glPointSize(size);
    
    glBegin(GL_POINTS);    
	for (int i = 0; i < m_num_points; i++){
            x = gsl_matrix_get(m_gsl_points, 0, i);
            y = gsl_matrix_get(m_gsl_points, 1, i);
            z = gsl_matrix_get(m_gsl_points, 2, i);
            
        if (m_draw[i] == 0){ 
            r = gsl_matrix_get(m_gsl_colors, 0, i);
            g = gsl_matrix_get(m_gsl_colors, 1, i);
            b = gsl_matrix_get(m_gsl_colors, 2, i);
            
            glColor4f(r/255.0, g/255.0, b/255.0, 0.8);
            glVertex3f(x, y, z);
        }
        else if (m_draw[i] == 1){
            r = 30;
            g = 0;
            b = 255.0;
            glColor4f(r/255.0, g/255.0, b/255.0, 0.7);
            glVertex3f(x, y, z);
        }
        else if (m_draw[i] == 2){
            r = 255.0;
            g = 0;
            b = 255.0;
            glColor4f(r/255.0, g/255.0, b/255.0, .7);
            glVertex3f(x, y, z);
        }
    }
    glEnd();
    
    
    glPointSize(6 + size);
    
    glBegin(GL_POINTS);    
	for (int i = 0; i < m_num_points; i++){
            x = gsl_matrix_get(m_gsl_points, 0, i);
            y = gsl_matrix_get(m_gsl_points, 1, i);
            z = gsl_matrix_get(m_gsl_points, 2, i);
            
        if (m_draw[i] == 2){
            r = 255.0;
            g = 0;
            b = 255.0;
            glColor4f(r/255.0, g/255.0, b/255.0, 1.0);
            glVertex3f(x, y, z);
        }
    }
    glEnd();
*/  

}

void PointCloud::DrawPrimitives(){
    /*
	m_quadratic = gluNewQuadric();			// Create A Pointer To The Quadric Object ( NEW )
	gluQuadricNormals(m_quadratic, GLU_NONE);	// Create Smooth Normals ( NEW )
	gluQuadricTexture(m_quadratic, GL_TRUE);	
	glMatrixMode(GL_MODELVIEW);
	
	glLineWidth(1);
	    
    for (int i = 0; i < m_geometry.size(); i++){
        m_geometry[i]->Draw();
    }
    */
}

void PointCloud::UtilCopyUserMarkedPoints(gsl_matrix *A, double *mean){
	printf("[PointCloud::UtilCopyUserMarkedPoints] with %d user-marked points\n", m_user_marked_points.size());
	
	for (int k = 0; k < 3; k++)
		mean[k] = 0;
	
	for (int h = 0; h < m_user_marked_points.size(); h++){
		int i = m_user_marked_points[h];
		for (int k = 0; k < 3; k++){
			mean[k] += gsl_matrix_get(m_gsl_points, k, i);
		}
	}
	
	for (int k = 0; k < 3; k++)
		mean[k] /= m_user_marked_points.size();
	
	for (int h = 0; h < m_user_marked_points.size(); h++){
		int i = m_user_marked_points[h];
		for (int k = 0; k < 3; k++){
			gsl_matrix_set(A, h, k, gsl_matrix_get(m_gsl_points, k, i) - mean[k]);
		}
	}
}

void PointCloud::UtilCopyMarkedPoints(gsl_matrix *A, double *mean){
	printf("[PointCloud::UtilCopyMarkedPoints] with %d user-marked points\n", m_marked_points.size());
	
	for (int k = 0; k < 3; k++)
		mean[k] = 0;
	
	for (int h = 0; h < m_marked_points.size(); h++){
		int i = m_marked_points[h];
		for (int k = 0; k < 3; k++){
			mean[k] += gsl_matrix_get(m_gsl_points, k, i);
		}
	}
	
	for (int k = 0; k < 3; k++)
		mean[k] /= m_marked_points.size();
	
	for (int h = 0; h < m_marked_points.size(); h++){
		int i = m_marked_points[h];
		for (int k = 0; k < 3; k++){
			gsl_matrix_set(A, h, k, gsl_matrix_get(m_gsl_points, k, i) - mean[k]);
		}
	}
}

void PointCloud::UtilCopyUserMarkedPoints2D(gsl_matrix *A, double *mean){
	printf("[PointCloud::UtilCopyUserMarkedPoints2D] with %d user-marked points\n", m_user_marked_points.size());
	
	for (int k = 0; k < 2; k++)
		mean[k] = 0;
	
	for (int h = 0; h < m_user_marked_points.size(); h++){
		int i = m_user_marked_points[h];
        mean[0] += gsl_matrix_get(m_gsl_points, 0, i);
        mean[1] += gsl_matrix_get(m_gsl_points, 2, i);
	}
	
	for (int k = 0; k < 2; k++)
		mean[k] /= m_user_marked_points.size();
	
	for (int h = 0; h < m_user_marked_points.size(); h++){
		int i = m_user_marked_points[h];
        gsl_matrix_set(A, h, 0, gsl_matrix_get(m_gsl_points, 0, i) - mean[0]);
        gsl_matrix_set(A, h, 1, gsl_matrix_get(m_gsl_points, 2, i) - mean[1]);
	}
	
	printf("[PointCloud::UtilCopyUserMarkedPoints2D] finished\n");
}

float PointCloud::UtilColorDiff(int r1, int g1, int b1, int r2, int g2, int b2){
    float diff = ( (r1 - r2)*(r1 - r2) + (b1 - b2)*(b1 - b2) + (g1 - g2)*(g1 - g2) );
    diff /= 5000;
    //printf("%3d %3d %3d\n%3d %3d %3d\n   diff: %f\n", r1, g1, b1, r2, g2, b2,diff);
    return diff;
}

void PointCloud::GroundPlane(){
    printf("[PointCloud::GroundPlane]\n");
    fflush(stdout);
    
    double min_x = DBL_MAX;
    double max_x = -1 * DBL_MAX;
    double min_z = DBL_MAX;
    double max_z = -1 * DBL_MAX;
    
    float *heights = (float*)malloc(sizeof(float)*m_num_points*2);
     
    for (int i = 0; i < m_num_points; i++){
        heights[i*2 + 0] = float(i);
        heights[i*2 + 1] = gsl_matrix_get(m_gsl_points, 1, i);
        
        double local_x = gsl_matrix_get(m_gsl_points, 0, i);
        double local_z = gsl_matrix_get(m_gsl_points, 2, i);
        if (local_x > max_x)
            max_x = local_x;
        if (local_x < min_x)
            min_x = local_x;
        if (local_z > max_z)
            max_z = local_z;
        if (local_z < min_z)
            min_z = local_z;
    }
    
    qsort(heights, m_num_points, sizeof(float)*2, compare_double_floats);
    
    printf("sorting takes a while...\n");
    fflush(stdout);
    
    int num_ground_samples = min(6000, m_num_points);
    int skip = 500;
    if (num_ground_samples < skip*5){
        skip = 0;
    }
    
    double mean[3];
    for (int k = 0; k < 3; k++)
		mean[k] = 0;
    
    
    gsl_matrix *sample_points = gsl_matrix_calloc(num_ground_samples, 3);
    for (int h = skip; h < num_ground_samples+skip; h++){
        int i = h - skip;
        int j = int(heights[h*2 + 0]);
        
        for (int k = 0; k < 3; k++){
            gsl_matrix_set(sample_points, i, k, gsl_matrix_get(m_gsl_points, k, j));
            mean[k] += gsl_matrix_get(m_gsl_points, k, j);
        }
        

    }
    
    for (int k = 0; k < 3; k++)
		mean[k] /= num_ground_samples;
    
    for (int i = 0; i < num_ground_samples; i++){
        for (int k = 0; k < 3; k++){
            gsl_matrix_set(sample_points, i, k, gsl_matrix_get(sample_points, i, k) - mean[k]);
        }
    }
    
    printf("about to run SVD\n");
    fflush(stdout);
    
    gsl_vector *S = gsl_vector_calloc(3);
    gsl_matrix *V = gsl_matrix_calloc(3,3);
    gsl_vector *work = gsl_vector_calloc(3);

    // run SVD!!! singular value decomposition! 
    int res = gsl_linalg_SV_decomp(sample_points, V, S, work);
    
    // now that we have SVD, let's get the plane normal and do some plane math
    double a = gsl_matrix_get(V, 0, 2);
    double b = gsl_matrix_get(V, 1, 2);
    double c = gsl_matrix_get(V, 2, 2);
    
    printf("plane normal: %f %f %f\n", a,b,c);
    double d = -1 * (a * mean[0] + b * mean[1] + c * mean[2]);
    fflush(stdout);
    
    m_ground_plane.a = a;
    m_ground_plane.b = b;
    m_ground_plane.c = c;
    m_ground_plane.d = d;
    
    GeometricComponent* geom = new GeometricComponent();
    geom->m_type = QUAD;
    prim_quad* q = new prim_quad;
    geom->m_quad = q;
    
    q->a = a;
    q->b = b;
    q->c = c;
    q->d = d;
    
    int x = 0;
    q->corners[x][0] = min_x;
    q->corners[x][2] = min_z;
    q->corners[x][1] = -1 * (a * q->corners[x][0] + c * q->corners[x][2] + d) / b;
    
    x = 1;
    q->corners[x][0] = min_x;
    q->corners[x][2] = max_z;
    q->corners[x][1] = -1 * (a * q->corners[x][0] + c * q->corners[x][2] + d) / b;
    
    x = 2;
    q->corners[x][0] = max_x;
    q->corners[x][2] = max_z;
    q->corners[x][1] = -1 * (a * q->corners[x][0] + c * q->corners[x][2] + d) / b;
    
    x = 3;
    q->corners[x][0] = max_x;
    q->corners[x][2] = min_z;
    q->corners[x][1] = -1 * (a * q->corners[x][0] + c * q->corners[x][2] + d) / b;
    
    geom->BuildTriangles();
    
    //m_geometry.push_back(geom);
}

void PointCloud::GroundPlaneOfDrawablePoints(){
    printf("[PointCloud::GroundPlaneOfDrawablePoints]\n");
    fflush(stdout);
    
    double min_x = DBL_MAX;
    double max_x = -1 * DBL_MAX;
    double min_z = DBL_MAX;
    double max_z = -1 * DBL_MAX;
    
    m_marked_points.clear();

    for (int i = 0; i < m_num_points; i++)
        if (m_draw[i] == 1)
            m_marked_points.push_back(i);
            
    int num_points = m_marked_points.size();
    
    float *heights = (float*)malloc(sizeof(float)*num_points*2);
     
    for (int h = 0; h < num_points; h++){     
        int i = m_marked_points[h];       
        heights[h*2 + 0] = float(i);
        heights[h*2 + 1] = gsl_matrix_get(m_gsl_points, 1, i);
        
        double local_x = gsl_matrix_get(m_gsl_points, 0, i);
        double local_z = gsl_matrix_get(m_gsl_points, 2, i);
        if (local_x > max_x)
            max_x = local_x;
        if (local_x < min_x)
            min_x = local_x;
        if (local_z > max_z)
            max_z = local_z;
        if (local_z < min_z)
            min_z = local_z;
        
    }
    
    qsort(heights, num_points, sizeof(float)*2, compare_double_floats);
    
    printf("sorting takes a while...\n");
    fflush(stdout);
    
    int num_ground_samples = min(6000, num_points);
    int skip = 500;
    if (num_ground_samples < skip*5){
        skip = 0;
    }
    
    double mean[3];
    for (int k = 0; k < 3; k++)
		mean[k] = 0;
    
    
    gsl_matrix *sample_points = gsl_matrix_calloc(num_ground_samples, 3);
    for (int h = skip; h < num_ground_samples+skip; h++){
        int i = h - skip;
        int j = int(heights[h*2 + 0]);
        
        for (int k = 0; k < 3; k++){
            gsl_matrix_set(sample_points, i, k, gsl_matrix_get(m_gsl_points, k, j));
            mean[k] += gsl_matrix_get(m_gsl_points, k, j);
        }
        

    }
    
    for (int k = 0; k < 3; k++)
		mean[k] /= num_ground_samples;
    
    for (int i = 0; i < num_ground_samples; i++){
        for (int k = 0; k < 3; k++){
            gsl_matrix_set(sample_points, i, k, gsl_matrix_get(sample_points, i, k) - mean[k]);
        }
    }
    
    printf("about to run SVD\n");
    fflush(stdout);
    
    gsl_vector *S = gsl_vector_calloc(3);
    gsl_matrix *V = gsl_matrix_calloc(3,3);
    gsl_vector *work = gsl_vector_calloc(3);

    // run SVD!!! singular value decomposition! 
    int res = gsl_linalg_SV_decomp(sample_points, V, S, work);
    
    // now that we have SVD, let's get the plane normal and do some plane math
    double a = gsl_matrix_get(V, 0, 2);
    double b = gsl_matrix_get(V, 1, 2);
    double c = gsl_matrix_get(V, 2, 2);
    
    printf("plane normal: %f %f %f\n", a,b,c);
    double d = -1 * (a * mean[0] + b * mean[1] + c * mean[2]);
    fflush(stdout);
    
    m_ground_plane.a = a;
    m_ground_plane.b = b;
    m_ground_plane.c = c;
    m_ground_plane.d = d;
    
    GeometricComponent* geom = new GeometricComponent();
    geom->m_type = QUAD;
    prim_quad* q = new prim_quad;
    geom->m_quad = q;
    
    q->a = a;
    q->b = b;
    q->c = c;
    q->d = d;
    
    int x = 0;
    q->corners[x][0] = min_x;
    q->corners[x][2] = min_z;
    q->corners[x][1] = -1 * (a * q->corners[x][0] + c * q->corners[x][2] + d) / b;
    
    x = 1;
    q->corners[x][0] = min_x;
    q->corners[x][2] = max_z;
    q->corners[x][1] = -1 * (a * q->corners[x][0] + c * q->corners[x][2] + d) / b;
    
    x = 2;
    q->corners[x][0] = max_x;
    q->corners[x][2] = max_z;
    q->corners[x][1] = -1 * (a * q->corners[x][0] + c * q->corners[x][2] + d) / b;
    
    x = 3;
    q->corners[x][0] = max_x;
    q->corners[x][2] = min_z;
    q->corners[x][1] = -1 * (a * q->corners[x][0] + c * q->corners[x][2] + d) / b;
    
    geom->BuildTriangles();
    
    m_geometry.push_back(geom);
}


void PointCloud::MarkConnectedMarkedPoints(){
    printf("[PointCloud::MarkConnectedMarkedPoints]\n");
    
    int num_marked_points = m_marked_points.size();
	printf("Number of points originally marked: %d\n", num_marked_points);
    
    std::vector<point> selected_points;
    std::vector<int> connected_marked_points;
    point pt;
    int dim = 3;
	
    // make KD tree of ALL points in scene (maybe excluding the deleted ones)
    ANNpointArray ann_points = annAllocPts(num_marked_points, dim); // or maybe i want to do it with 6 things?? 
	
    for (int h = 0; h < m_marked_points.size(); h++){
        int i = m_marked_points[h];
        if (!m_delete[i]){
            for (int k = 0; k < 3; k++){
                ann_points[h][k] = gsl_matrix_get(m_gsl_points, k, i);
            }
            if (dim == 6){
                for (int k = 0; k < 3; k++){
                    ann_points[h][3 + k] = gsl_matrix_get(m_gsl_colors, k, i);
                }
            }
		}
	}
	
	for (int h = 0; h < m_user_marked_points.size(); h++){
        int i = m_user_marked_points[h];
		pt.x = gsl_matrix_get(m_gsl_points, 0, i);
		pt.y = gsl_matrix_get(m_gsl_points, 1, i);
		pt.z = gsl_matrix_get(m_gsl_points, 2, i);
		pt.r = gsl_matrix_get(m_gsl_colors, 0, i);
		pt.g = gsl_matrix_get(m_gsl_colors, 1, i);
		pt.b = gsl_matrix_get(m_gsl_colors, 2, i);
		selected_points.insert(selected_points.begin(),pt);
    }
	
	bool *selected = (bool*)calloc(num_marked_points, sizeof(bool));
	
    ANNkd_tree* kd_3d = new ANNkd_tree(ann_points, num_marked_points, dim);
	
    // search through this space of points and find more near-by points to add     
    int n = 20;
    double radius = 0.00000000000000001;
    ANNpoint queryPt = annAllocPt(dim);
    ANNidxArray nnIdx = new ANNidx[n];
    ANNdistArray dists = new ANNdist[n];
    
    for (int i = 0; i < selected_points.size(); i++){
        queryPt[0] = selected_points[i].x;
        queryPt[1] = selected_points[i].y;
        queryPt[2] = selected_points[i].z;
        if (dim == 6){
            queryPt[3] = selected_points[i].r;
            queryPt[4] = selected_points[i].g;
            queryPt[5] = selected_points[i].b;
        }
        int double_count = 0;
        while (kd_3d->annkFRSearch(queryPt, radius, 0) < 2 && double_count < 2){
            radius *= 1.2;
            double_count ++;
            //printf("    doubling radius %.20f\n", radius);
        }
    }
    
    radius *= 2;
    
	//printf("number of selected points: %d\n", selected_points.size());
	
    while (!selected_points.empty() && selected_points.size() < 80000 && selected_points.size() > 20){
        pt = selected_points.back();
        selected_points.pop_back();
		
        queryPt[0] = pt.x;
        queryPt[1] = pt.y;
        queryPt[2] = pt.z;
        if (dim == 6){
            queryPt[3] = pt.r;
            queryPt[4] = pt.g;
            queryPt[5] = pt.b;
        }
        
        int r = pt.r;
        int b = pt.b;
        int g = pt.g;
        
        int neighbors = kd_3d->annkFRSearch(queryPt, radius, n, nnIdx, dists, 0);
        //printf("neighbors: %d, size of selected pooints: %d\n", neighbors, selected_points.size());
        int num_new_neighbors = 0;
        for (int i = 0; i < min(n, neighbors); i++){
            int idx = m_marked_points[nnIdx[i]];
            if (!selected[nnIdx[i]]){
                num_new_neighbors++;
            }
        }
        
        //printf("number of new neighbors: %d\n", num_new_neighbors);
        
        if (num_new_neighbors > n/5){
            for (int i = 0; i < min(n, neighbors); i++){
                int idx = m_marked_points[nnIdx[i]];
                if (!selected[nnIdx[i]]){
                    pt.x = gsl_matrix_get(m_gsl_points, 0, idx);
                    pt.y = gsl_matrix_get(m_gsl_points, 1, idx);
                    pt.z = gsl_matrix_get(m_gsl_points, 2, idx);
                    pt.r = gsl_matrix_get(m_gsl_colors, 0, idx);
                    pt.g = gsl_matrix_get(m_gsl_colors, 1, idx);
                    pt.b = gsl_matrix_get(m_gsl_colors, 2, idx);
                    selected_points.insert(selected_points.begin(),pt);
                    float color_diff = UtilColorDiff(pt.r, pt.b, pt.g, r, g, b);
                    if (color_diff < 1.0){
                        //printf("        adding new point\n");
                        selected[nnIdx[i]] = true;
                        pt.r = 255;
                        pt.g = pt.b = 0;
                        connected_marked_points.push_back(idx);  
                    }
                }   
            }
        }
    }
	
	m_marked_points = connected_marked_points;
}

void PointCloud::PlaneFloodFill(){
    // allocate some matrices, find mean of marked-user-points
    gsl_matrix *A = gsl_matrix_calloc(m_user_marked_points.size(), 3);
	double marked_points_mean[3];
	UtilCopyUserMarkedPoints(A, marked_points_mean);
	
    gsl_vector *S = gsl_vector_calloc(3);
    gsl_matrix *V = gsl_matrix_calloc(3,3);
    gsl_vector *work = gsl_vector_calloc(3);

    // run SVD!!! singular value decomposition! 
    int res = gsl_linalg_SV_decomp(A, V, S, work);

    // now that we have SVD, let's get the plane normal and do some plane math
    double a = gsl_matrix_get(V, 0, 2);
    double b = gsl_matrix_get(V, 1, 2);
    double c = gsl_matrix_get(V, 2, 2);
    
    printf("plane normal: %f %f %f\n", a,b,c);
    
    // hessian normal form for doing pt distance stuff 
    double x = marked_points_mean[0];
    double y = marked_points_mean[1];
    double z = marked_points_mean[2];
    
    printf("plane centroid: %f %f %f\n", x,y,z);
    
    double d = -1 * (a * x + b * y + c * z);
    SetMarkedPointPlane(a, b, c, d);
	
    double denom = 1 / sqrt(a*a + b*b + c*c);
    
    double hess[3] = {a * denom, b * denom, c * denom};
    double p = d * denom;
    
    double dist;
    for (int i = 0; i < m_num_points; i++){
        if (!m_delete[i]){
            dist = 0;
            for (int k = 0; k < 3; k++){
                 dist += (hess[k] * gsl_matrix_get(m_gsl_points, k, i));
            }
            dist += p;
            if (abs(dist) < 0.003){
                m_marked_points.push_back(i);
            }
        }
    }
	
	int num_points_in_plane = m_marked_points.size();
	printf("Number of points in plane %d\n", num_points_in_plane);

}

void PointCloud::PlaneFloodFillConnected(bool boundedByStroke){
    // allocate some matrices, find mean of marked-user-points
    printf("[PointCloud::PlaneFloodFillConnected] number of points in m_user_marked_points %d\n", m_user_marked_points.size());
    
    // find bounds of stroke approximately 
    double min_x = DBL_MAX;
    double min_y = DBL_MAX;
    double max_x = -min_x;
    double max_y = -min_y;
    for (int h = 0; h < m_user_marked_points.size(); h++){
        int i = m_user_marked_points[h];
        double x = gsl_matrix_get(m_gsl_points, 0, i);
        double y = gsl_matrix_get(m_gsl_points, 1, i);
        if (x < min_x)
            min_x = x;
        if (x > max_x)
            max_x = x;
        
        if (y < min_y)
            min_y = y;
        if (y > max_y)
            max_y = y;
    }
    
    gsl_matrix *A = gsl_matrix_calloc(m_user_marked_points.size(), 3);
	double marked_points_mean[3];
	UtilCopyUserMarkedPoints(A, marked_points_mean);
	
    gsl_vector *S = gsl_vector_calloc(3);
    gsl_matrix *V = gsl_matrix_calloc(3,3);
    gsl_vector *work = gsl_vector_calloc(3);
	
    // run SVD!!! singular value decomposition! 
    int res = gsl_linalg_SV_decomp(A, V, S, work);
	
    // now that we have SVD, let's get the plane normal and do some plane math
    double a = gsl_matrix_get(V, 0, 2);
    double b = gsl_matrix_get(V, 1, 2);
    double c = gsl_matrix_get(V, 2, 2);
    
    printf("plane normal: %f %f %f\n", a,b,c);
    fflush(stdout);
    
    // hessian normal form for doing pt distance stuff 
    double x = marked_points_mean[0];
    double y = marked_points_mean[1];
    double z = marked_points_mean[2];
    
    printf("plane centroid: %f %f %f\n", x,y,z);
    
    double d = -1 * (a * x + b * y + c * z);
    SetMarkedPointPlane(a, b, c, d);
	
    double denom = 1 / sqrt(a*a + b*b + c*c);
    
    double hess[3] = {a * denom, b * denom, c * denom};
    double p = d * denom;
    
    double dist;
    int kd_budget = 10000;
    for (int h = 0; h < m_num_points; h++){
        int i = h;
        //printf("int i: %d\n", i);
        //fflush(stdout);
        if (m_cluster_tree)
            i = m_cluster_tree[h].pt_idx;
        if (!m_delete[i]){
            dist = 0;
            for (int k = 0; k < 3; k++){
				dist += (hess[k] * gsl_matrix_get(m_gsl_points, k, i));
            }
            dist += p;
            bool pointInRange = true;
            if (boundedByStroke){
                if (gsl_matrix_get(m_gsl_points, 0, i) < min_x)
                    pointInRange = false;
                if (gsl_matrix_get(m_gsl_points, 0, i) > max_x)
                    pointInRange = false;
                if (gsl_matrix_get(m_gsl_points, 1, i) < min_y)
                    pointInRange = false;
                if (gsl_matrix_get(m_gsl_points, 1, i) > max_y)
                    pointInRange = false;
            }
            if (abs(dist) < 0.001 && pointInRange){
                m_marked_points.push_back(i);
            }
        }
        if (m_marked_points.size() == kd_budget)
            break;
    }
	
	int num_points_in_plane = m_marked_points.size();
	printf("[PointCloud::PlaneFloodFillConnected] number of points in entire, not necessarily connected plane: %d\n", num_points_in_plane);
    fflush(stdout);
	
    // **** all the points in the plane have been found *** now just get the connected ones //
    
    std::vector<point> selected_points;
    std::vector<int> connected_marked_points;
    point pt;
    int dim = 3;
	
    // make KD tree of ALL points in scene (maybe excluding the deleted ones)
    ANNpointArray ann_points = annAllocPts(num_points_in_plane, dim); // or maybe i want to do it with 6 things?? 
	
    for (int h = 0; h < m_marked_points.size(); h++){
        int i = m_marked_points[h];
        if (!m_delete[i]){
            for (int k = 0; k < 3; k++){
                ann_points[h][k] = gsl_matrix_get(m_gsl_points, k, i);
            }
            if (dim == 6){
                for (int k = 0; k < 3; k++){
                    ann_points[h][3 + k] = gsl_matrix_get(m_gsl_colors, k, i);
                }
            }
		}
	}
	
	for (int h = 0; h < m_user_marked_points.size(); h++){
        int i = m_user_marked_points[h];
		pt.x = gsl_matrix_get(m_gsl_points, 0, i);
		pt.y = gsl_matrix_get(m_gsl_points, 1, i);
		pt.z = gsl_matrix_get(m_gsl_points, 2, i);
		pt.r = gsl_matrix_get(m_gsl_colors, 0, i);
		pt.g = gsl_matrix_get(m_gsl_colors, 1, i);
		pt.b = gsl_matrix_get(m_gsl_colors, 2, i);
		selected_points.insert(selected_points.begin(),pt);
    }
	
	bool *selected = (bool*)calloc(num_points_in_plane, sizeof(bool));
	
	printf("making kd tree\n");
	fflush(stdout);
    ANNkd_tree* kd_3d = new ANNkd_tree(ann_points, num_points_in_plane, dim);
	printf("done making kd tree\n");
	fflush(stdout);
	
    // search through this space of points and find more near-by points to add     
    int n = 20;
    double radius = 0.00000000000000001;
    ANNpoint queryPt = annAllocPt(dim);
    ANNidxArray nnIdx = new ANNidx[n];
    ANNdistArray dists = new ANNdist[n];
    
    for (int i = 0; i < selected_points.size(); i++){
        queryPt[0] = selected_points[i].x;
        queryPt[1] = selected_points[i].y;
        queryPt[2] = selected_points[i].z;
        if (dim == 6){
            queryPt[3] = selected_points[i].r;
            queryPt[4] = selected_points[i].g;
            queryPt[5] = selected_points[i].b;
        }
        int double_count = 0;
        while (kd_3d->annkFRSearch(queryPt, radius, 0) < 2 && double_count < 2){
            radius *= 1.2;
            double_count ++;
            //printf("    doubling radius %.20f\n", radius);
        }
    }
    
    radius *= 2;
    
	printf("[PointCloud::PlaneFloodFillConnected] number of selected points: %d\n", selected_points.size());
	fflush(stdout);
	
    while (!selected_points.empty() && selected_points.size() < 80000 && selected_points.size() > 20){
        pt = selected_points.back();
        selected_points.pop_back();
		
        queryPt[0] = pt.x;
        queryPt[1] = pt.y;
        queryPt[2] = pt.z;
        if (dim == 6){
            queryPt[3] = pt.r;
            queryPt[4] = pt.g;
            queryPt[5] = pt.b;
        }
        
        int r = pt.r;
        int b = pt.b;
        int g = pt.g;
        
        int neighbors = kd_3d->annkFRSearch(queryPt, radius, n, nnIdx, dists, 0);
        //printf("neighbors: %d, size of selected pooints: %d\n", neighbors, selected_points.size());
        fflush(stdout);
        
        int num_new_neighbors = 0;
        for (int i = 0; i < min(n, neighbors); i++){
            int idx = m_marked_points[nnIdx[i]];
            if (!selected[nnIdx[i]]){
                num_new_neighbors++;
            }
        }
        
        //printf("selected points: %d, number of new neighbors: %d\n", selected_points.size(), num_new_neighbors);
        
        if (num_new_neighbors > n/5){
            for (int i = 0; i < min(n, neighbors); i++){
                int idx = m_marked_points[nnIdx[i]];
                if (!selected[nnIdx[i]]){
                    pt.x = gsl_matrix_get(m_gsl_points, 0, idx);
                    pt.y = gsl_matrix_get(m_gsl_points, 1, idx);
                    pt.z = gsl_matrix_get(m_gsl_points, 2, idx);
                    pt.r = gsl_matrix_get(m_gsl_colors, 0, idx);
                    pt.g = gsl_matrix_get(m_gsl_colors, 1, idx);
                    pt.b = gsl_matrix_get(m_gsl_colors, 2, idx);
                    float color_diff = UtilColorDiff(pt.r, pt.b, pt.g, r, g, b);
                    if (color_diff < 1.0){
                        //printf("        adding new point\n");
                        fflush(stdout);
                        
                        selected_points.insert(selected_points.begin(),pt);
                        selected[nnIdx[i]] = true;
                        pt.r = 255;
                        pt.g = pt.b = 0;
                        connected_marked_points.push_back(idx);    
                    }
                }
            }
        }
    }
	
	m_marked_points = connected_marked_points;
	
	printf("[PointCloud::PlaneFloodFillConnected] number of points connected in plane: %d\n", int(m_marked_points.size()));
	
}

void PointCloud::PlaneFloodFillWindows(){
    // allocate some matrices, find mean of marked-user-points
    gsl_matrix *A = gsl_matrix_calloc(m_marked_points.size(), 3);
	double marked_points_mean[3];
	UtilCopyMarkedPoints(A, marked_points_mean);
	
    gsl_vector *S = gsl_vector_calloc(3);
    gsl_matrix *V = gsl_matrix_calloc(3,3);
    gsl_vector *work = gsl_vector_calloc(3);

    // run SVD!!! singular value decomposition! 
    int res = gsl_linalg_SV_decomp(A, V, S, work);

    // now that we have SVD, let's get the plane normal and do some plane math
    double a = gsl_matrix_get(V, 0, 2);
    double b = gsl_matrix_get(V, 1, 2);
    double c = gsl_matrix_get(V, 2, 2);
    
    printf("plane normal: %f %f %f\n", a,b,c);
    
    // hessian normal form for doing pt distance stuff 
    double x = marked_points_mean[0];
    double y = marked_points_mean[1];
    double z = marked_points_mean[2];
    
    printf("plane centroid: %f %f %f\n", x,y,z);
    
    double d = -1 * (a * x + b * y + c * z);
    SetMarkedPointPlane(a, b, c, d);
	
    double denom = 1 / sqrt(a*a + b*b + c*c);
    
    double hess[3] = {a * denom, b * denom, c * denom};
    double p = d * denom;
    
    std::vector<int> window_points;
    std::vector<int> wall_points;
    
    double dist;
    for (int h = 0; h < m_marked_points.size(); h++){
        int i = m_marked_points[h];
        if (!m_delete[i]){
            dist = 0;
            for (int k = 0; k < 3; k++){
                 dist += (hess[k] * gsl_matrix_get(m_gsl_points, k, i));
            }
            dist += p;
            if (abs(dist) < 0.002){
                if (abs(dist) > 0.0003)
                    window_points.push_back(i);
                else
                    wall_points.push_back(i);
            }
        }
    }
    
    double avg_color[3];
    for (int h = 0; h < wall_points.size(); h++){
        int i = wall_points[h];
        for (int k = 0; k < 3; k++){
            avg_color[k] += gsl_matrix_get(m_gsl_colors, k, i);
        }
    }
    for (int k = 0; k < 3; k++)
        avg_color[k] /= wall_points.size();
        
    double avg_diff;
    for (int h = 0; h < wall_points.size(); h++){
        int i = wall_points[h];
        for (int k = 0; k < 3; k++){
            avg_diff += pow(gsl_matrix_get(m_gsl_colors, k, i)-avg_color[k], 2);
        }
    }
    avg_diff /= wall_points.size();
    int count = 0;
    double new_avg_color[3];
    for (int h = 0; h < wall_points.size(); h++){
        int i = wall_points[h];
        double diff = 0;
        for (int k = 0; k < 3; k++)
            diff += pow(gsl_matrix_get(m_gsl_colors, k, i)-avg_color[k], 2);
        if (diff < avg_diff/2.0){
            count++;
            for (int k = 0; k < 3; k++){
                new_avg_color[k] += gsl_matrix_get(m_gsl_colors, k, i);
            }
        }
	}
	for (int k = 0; k < 3; k++)
        new_avg_color[k] /= count;
        
    printf("color: %f %f %f\n", avg_color[0], avg_color[1],avg_color[2]);
    printf("new avg color: %f %f %f\n", new_avg_color[0], new_avg_color[1],new_avg_color[2]);
        
	//m_quads[0].r = new_avg_color[0];
	//m_quads[0].g = new_avg_color[1];
	//m_quads[0].b = new_avg_color[2];
	
    std::vector<int> new_windows;
    
    for (int h = 0; h < window_points.size(); h++){
        int i = window_points[h];
        double color[3];
        for (int k = 0; k < 3; k++)
            color[k] = gsl_matrix_get(m_gsl_colors, k, i);
        double color_diff = UtilColorDiff(new_avg_color[0], new_avg_color[1], new_avg_color[2], color[0], color[1], color[2]);
        if (color_diff < 0.2)
            wall_points.push_back(i);
        else
            new_windows.push_back(i);
    }
    window_points = new_windows;
    
  /*  
    std::vector<int> new_walls;
    for (int h = 0; h < wall_points.size(); h++){
        int i = wall_points[h];
        double color[3];
        for (int k = 0; k < 3; k++)
            color[k] = gsl_matrix_get(m_gsl_colors, k, i);
        double color_diff = UtilColorDiff(avg_color[0], avg_color[1], avg_color[2], color[0], color[1], color[2]);
        if (color_diff < 1.0)
            new_walls.push_back(i);
        else
            window_points.push_back(i);
    }
//    window_points = new_windows;
    wall_points = new_walls;
*/
	m_marked_points = window_points;
	m_user_marked_points = wall_points;
	
	int num_points_in_plane = m_marked_points.size();
	printf("Number of points in plane %d\n", num_points_in_plane);

}

void PointCloud::MoreWindowProcessing(){
    printf("[PointCloud::MoreWindowProcessing]\n");
    plane p = m_marked_point_plane;
    
    int num_points = m_marked_points.size() + m_user_marked_points.size();
    
    gsl_vector *x_pos = gsl_vector_calloc(num_points);    
    for (int h = 0; h < m_marked_points.size(); h++){
        int i = m_marked_points[h];
        gsl_vector_set(x_pos, h, gsl_matrix_get(m_gsl_points, 0, i));
    }
    for (int h = 0; h < m_user_marked_points.size(); h++){
        int i = m_user_marked_points[h];
        gsl_vector_set(x_pos, h + m_marked_points.size(), gsl_matrix_get(m_gsl_points, 0, i));
    }
    
    gsl_permutation * perm = gsl_permutation_alloc (num_points);
    gsl_permutation_init (perm);
    
    gsl_sort_vector_index(perm, x_pos);
    
    double min_x = gsl_vector_get(x_pos, gsl_permutation_get(perm, 0));
    double max_x = gsl_vector_get(x_pos, gsl_permutation_get(perm, num_points-1));
    
    int n = 100;
    double *window_buckets = (double*)calloc(n, sizeof(double));
    double *wall_buckets = (double*)calloc(n, sizeof(double));
    for (int i = 0; i < num_points; i++){
        double x = gsl_vector_get(x_pos, gsl_permutation_get(perm, i));
        int bucket = ( (x - min_x)/(max_x-min_x) ) * n;
    
        if (gsl_permutation_get(perm, i) < m_marked_points.size()){
            // this is part of the window
            window_buckets[bucket] ++;
        }
        else {
            wall_buckets[bucket] ++;
        }
    }
    
    for (int i = 0; i < n; i++){
        printf("bucket %2d: %4.4f", i, wall_buckets[i]/window_buckets[i]);
        for (int j = 0; j < window_buckets[i]/10; j++){
            //printf("*");
        }
        printf("\n");
        /*
        for (int j = 0; j < wall_buckets[i]/10; j++){
            printf("-");
        }
        printf("\n");
        */
    }
}

void PointCloud::SegmentWindowPoints(){
    printf("[PointCloud::SegmentWindowPoints]\n");

    /*
    plane wall_plane = m_vertical_planes.back();
    printf("plane stuff: %f %f %f %f\n", wall_plane.a, wall_plane.b, wall_plane.c, wall_plane.d);
    double denom = 1 / sqrt(wall_plane.a*wall_plane.a + wall_plane.b*wall_plane.b + wall_plane.c*wall_plane.c);
    
    double hess[3] = {wall_plane.a * denom, wall_plane.b * denom, wall_plane.c * denom};
    double p = wall_plane.d * denom;
    
    printf("m marked points: %d\n", m_marked_points.size());
    for (int h = 0; h < m_marked_points.size(); h++){
        int i = m_marked_points[h];
        double dist = 0;
        for (int k = 0; k < 3; k++){
            dist += (hess[k] * gsl_matrix_get(m_gsl_points, k, i));
        }
        dist += p;
        if (abs(dist) < 0.0003)
            m_draw[i] = 1;
        else
            m_draw[i] = 2;
    }
    */

}


void PointCloud::WindowFromMarkedPoints(){
    printf("[PointCloud::WindowFromMarkedPoints]\n");
    
    // min max of selected user points
    double min_x = DBL_MAX;
    double min_y = DBL_MAX;
    double min_z = DBL_MAX;
    double max_x = -min_x;
    double max_y = -min_y;
    double max_z = -min_z;
    
    // figure out which plane the marked window pts are in 
    int plane_idx = -1;
    for (int h = 0; h < m_user_marked_points.size(); h++){
        int i = m_user_marked_points[h];
		structure_ptr s = m_point_lookup[i];
        if (s.idx >= 0)
            plane_idx = s.idx;
            
        double x = gsl_matrix_get(m_gsl_points, 0, i);
        double y = gsl_matrix_get(m_gsl_points, 1, i);
        double z = gsl_matrix_get(m_gsl_points, 2, i);
        
        if (x < min_x)
            min_x = x;
        if (x > max_x)
            max_x = x;
        
        if (y < min_y)
            min_y = y;
        if (y > max_y)
            max_y = y;
            
        if (z < min_z)
            min_z = z;
        if (z > max_z)
            max_z = z;
        
    }
    
    double epsilon = 0.001;
    if (max_x - min_x > epsilon*3.0){
        min_x += epsilon;
        max_x -= epsilon;
    }
    if (max_y - min_y > epsilon*3.0){
        min_y += epsilon;
        max_y -= epsilon;
    }
    
    printf("[PointCloud::WindowFromMarkedPoints] marked window points on plane # %d\n", plane_idx);
    plane wall_plane; //m_vertical_planes[plane_idx];
    printf("plane stuff: %f %f %f %f\n", wall_plane.a, wall_plane.b, wall_plane.c, wall_plane.d);
    double denom = 1 / sqrt(wall_plane.a*wall_plane.a + wall_plane.b*wall_plane.b + wall_plane.c*wall_plane.c);
    
    double hess[3] = {wall_plane.a * denom, wall_plane.b * denom, wall_plane.c * denom};
    double p = wall_plane.d * denom;
    
    // figure out which depth user marked points are at
    double avg_marked_point_depth = 0;
    for (int h = 0; h < m_user_marked_points.size(); h++){
        int i = m_user_marked_points[h];
        double dist = 0;
        for (int k = 0; k < 3; k++){
            dist += (hess[k] * gsl_matrix_get(m_gsl_points, k, i));
        }
        dist += p;
        avg_marked_point_depth += dist;
    }
    avg_marked_point_depth /= m_user_marked_points.size(); 
    printf("avg marked point depth: %f\n", avg_marked_point_depth);

    // find all points in this plane
    std::vector<int> plane_points;
    std::vector<int> horizontal_band;
    std::vector<int> vertical_band;
    for (int i = 0; i < m_num_points; i++){
        if (m_point_lookup[i].idx == plane_idx){
            plane_points.push_back(i);
        
            double x = gsl_matrix_get(m_gsl_points, 0, i);
            double y = gsl_matrix_get(m_gsl_points, 1, i);
            double z = gsl_matrix_get(m_gsl_points, 2, i);
            
            if (x < max_x && x > min_x){
                vertical_band.push_back(i);
                m_marked_points.push_back(i);
            }
            if (y < max_y && y > min_y){
                horizontal_band.push_back(i);
                m_marked_points.push_back(i);
            }
        }
    }
    
    printf("[PointCloud::WindowFromMarkedPoints] points in this plane: %d, horiz band: %d, vert band: %d\n", plane_points.size(), horizontal_band.size(), vertical_band.size());

    double old_min_x = min_x;
    double old_min_y = min_y;
    double old_max_x = max_x;
    double old_max_y = max_y;

    // HORIZONTAL 
    gsl_vector *x_pos = gsl_vector_calloc(horizontal_band.size());  
    double *x_dist = (double*)calloc(horizontal_band.size(), sizeof(double));  
    for (int h = 0; h < horizontal_band.size(); h++){
        int i = horizontal_band[h];
        double pt[3];
        double dist = 0;
        for (int k = 0; k < 3; k++){
            pt[k] = gsl_matrix_get(m_gsl_points, k, i);
        }
        gsl_vector_set(x_pos, h, pt[0]);
        double plane_dist = 0;
        for (int k = 0; k < 3; k++){
            dist += (hess[k] * pt[k]);
        }
        dist += p;
        x_dist[h] = dist;
    }
    
    gsl_permutation * perm = gsl_permutation_alloc (horizontal_band.size());
    gsl_permutation_init (perm);
    gsl_sort_vector_index(perm, x_pos);
    
    int n_wall = 20;
    float percent = 0.95;
    
    for (int h = 0; h < horizontal_band.size(); h++){
        int i = gsl_permutation_get(perm, h);
        double x = gsl_vector_get(x_pos, i);
        if (x > (max_x + min_x)/2){
            int c = 0;
            for (int j = h; j < min(h+n_wall, int(horizontal_band.size())); j++){
                int i2 = gsl_permutation_get(perm, j); 
                if (avg_marked_point_depth < 0){ 
                    if (abs(x_dist[i2]) < 0.0003 || x_dist[i2] > 0)
                        c++;
                }
                else {
                    if (abs(x_dist[i2]) < 0.0003 || x_dist[i2] < 0){
                        c++;
                    }
                }
            }
            if (c > n_wall * percent){
                max_x = x;
                printf("break!! \n");  
                break;
            }
        }
    }
    
    for (int h = horizontal_band.size()-1; h >= 0; h--){
        int i = gsl_permutation_get(perm, h);
        double x = gsl_vector_get(x_pos, i);
        if (x < (max_x + min_x)/2){
            int c = 0;
            for (int j = h; j > max(h-n_wall,0); j--){
                int i2 = gsl_permutation_get(perm, j);   
                if (avg_marked_point_depth < 0){ 
                    if (abs(x_dist[i2]) < 0.0003 || x_dist[i2] > 0)
                        c++;
                }
                else {
                    if (abs(x_dist[i2]) < 0.0003 || x_dist[i2] < 0){
                        c++;
                    }
                }
            }
            if (c > n_wall * percent){
                min_x = x;       
                printf("break!! h: %d\n", h);          
                break;
                
            }
        }
    }
    
    if (old_max_x == max_x)
        printf(" Max X did not change! **\n");
    if (old_min_x == min_x)
        printf(" Min X did not change! **\n");
        
    // EXPERIMENT
    /*
    int ptr1 = 0;
    int ptr2 = 0; 
    int num_walls = 0;
    int num_walls_max = 20;
    int n = horizontal_band.size();
    
    for (int h = 0; h < n; h++){
        int i = gsl_permutation_get(perm, h);
        if (abs(x_dist[i]) < 0.0003){
            ptr1 = ptr2 = h;
            break;
        }
    }
    
    while (ptr1 < n-2 && ptr2 < n-1){
        // make sure there enough wall points to look at
        while (num_walls < num_walls_max && ptr2 < n-1){
            ptr2++;
            int i2 = gsl_permutation_get(perm, ptr2);
            if ( abs(x_dist[i2]) < 0.0003 ){
                num_walls++;
            }
        }
        
        // calc stuff
        //printf(" EXPERIMENT (%d) -- calculating stuff: prt1: %d, ptr2: %d\n", n, ptr1, ptr2);
        int i1 = gsl_permutation_get(perm, ptr1);
        int i2 = gsl_permutation_get(perm, ptr2);
        double pos_difference = (gsl_vector_get(x_pos, i2) - gsl_vector_get(x_pos, i1));
        if (pos_difference < 0.0004)
            printf("     position difference (%d): %0.10f\n", ptr1, pos_difference);
        
        // pop a wall back at the end
        num_walls--;
        ptr1++;
        
        
        for (int h = ptr1; h < n-1; h++){
            //printf("n, ptr1: %d %d\n", n, h);
            int i = gsl_permutation_get(perm, h);
            if (abs(x_dist[i]) < 0.0003){
                ptr1 = h;
                break;
            }
        }
    }
    */
    

    // VERTICAL
    gsl_vector *y_pos = gsl_vector_calloc(vertical_band.size());  
    double *y_dist = (double*)calloc(vertical_band.size(), sizeof(double));  
    for (int h = 0; h < vertical_band.size(); h++){
        int i = vertical_band[h];
        double pt[3];
        double dist = 0;
        for (int k = 0; k < 3; k++){
            pt[k] = gsl_matrix_get(m_gsl_points, k, i);
        }
        gsl_vector_set(y_pos, h, pt[1]);
        double plane_dist = 0;
        for (int k = 0; k < 3; k++){
            dist += (hess[k] * pt[k]);
        }
        dist += p;
        y_dist[h] = dist;
    }
    
    perm = gsl_permutation_alloc (vertical_band.size());
    gsl_permutation_init (perm);
    gsl_sort_vector_index(perm, y_pos);
    
    for (int h = 0; h < vertical_band.size(); h++){
        int i = gsl_permutation_get(perm, h);
        
        double y = gsl_vector_get(y_pos, i);
        if (y >  (max_y + min_y)/2){
            int c = 0;
            for (int j = h; j < min(h+n_wall, int(vertical_band.size())); j++){
                int i2 = gsl_permutation_get(perm, j);   
                if (avg_marked_point_depth < 0){ 
                    if (abs(y_dist[i2]) < 0.0003 || y_dist[i2] > 0)
                        c++;
                }
                else {
                    if (abs(y_dist[i2]) < 0.0003 || y_dist[i2] < 0){
                        c++;
                    }
                }
            }
            if (c > n_wall * percent){
                max_y = y;
                printf("break!! \n");  
                break;
            }
        }
    }
    
    for (int h = vertical_band.size()-1; h >= 0; h--){
        int i = gsl_permutation_get(perm, h);
        double y = gsl_vector_get(y_pos, i);
        if (y < (max_y + min_y)/2){
            int c = 0;
            for (int j = h; j > max(h-n_wall,0); j--){
                int i2 = gsl_permutation_get(perm, j);   
                if (avg_marked_point_depth < 0){ 
                    if (abs(y_dist[i2]) < 0.0003 || y_dist[i2] > 0)
                        c++;
                }
                else {
                    if (abs(y_dist[i2]) < 0.0003 || y_dist[i2] < 0){
                        c++;
                    }
                }
            }
            if (c > n_wall * percent){
                min_y = y;   
                printf("break!! \n");            
                break;
                
            }
        }
    }

    if (old_max_y == max_y)
        printf(" Max y did not change! **\n");
    if (old_min_y == min_y)
        printf(" Min y did not change! **\n");
    
    double dist;
    dist = 0;
    dist += (hess[0] * min_x);
    dist += (hess[1] * min_y);
    dist += (hess[2] * min_z);
    dist += p;
    
    min_x = min_x - dist * wall_plane.a * denom;
    min_y = min_y - dist * wall_plane.b * denom;
    min_z = min_z - dist * wall_plane.c * denom;
    min_z -= 0.0005;
    
    dist = 0;
    dist += (hess[0] * max_x);
    dist += (hess[1] * max_y);
    dist += (hess[2] * max_z);
    dist += p;
    
    max_x = max_x - dist * wall_plane.a * denom;
    max_y = max_y - dist * wall_plane.b * denom;
    max_z = max_z - dist * wall_plane.c * denom;
    max_z -= 0.0005;
        
    GeometricComponent* geom = new GeometricComponent();
    geom->m_type = QUAD;
    prim_quad* q = new prim_quad;
    geom->m_quad = q;
    
    q->corners[0][0] = min_x;
    q->corners[0][1] = min_y;
    q->corners[0][2] = min_z;
    
    q->corners[1][0] = min_x;
    q->corners[1][1] = max_y;
    q->corners[1][2] = min_z;
    
    
    q->corners[2][0] = max_x;
    q->corners[2][1] = max_y;
    q->corners[2][2] = max_z;
    
    q->corners[3][0] = max_x;
    q->corners[3][1] = min_y;
    q->corners[3][2] = max_z;
    

	geom->m_texture.bitmap[0] = 40;
	geom->m_texture.bitmap[1] = 255;
	geom->m_texture.bitmap[2] = 255;
	geom->m_texture.bitmap[3] = 150;
    
    geom->BuildTriangles();
    
    m_geometry.push_back(geom);
}

void PointCloud::CylinderFloodFillConnected(){
    // do something interesting with the m_user_marked_points vector
    // put good points int m_marked_points 

    printf("[PointCloud::CylinderFloodFillConnected] starting...\n");
    
    double min_x = DBL_MAX;
    double min_z = DBL_MAX;
    double max_x = -1*DBL_MAX;
    double max_z = -1*DBL_MAX;
    
    for (int h = 0; h < m_user_marked_points.size(); h++){
        int i = m_user_marked_points[h];
        double x = gsl_matrix_get(m_gsl_points, 0, i);
		double y = gsl_matrix_get(m_gsl_points, 1, i);
        double z = gsl_matrix_get(m_gsl_points, 2, i);

        if (x < min_x)
            min_x = x;
        if (x > max_x)
            max_x = x;
        if (z < min_z)
            min_z = z;
        if (z > max_z)
            max_z = z;
    }
    
    double epsilon = 0.0000001;
    min_x -= epsilon;
    min_z -= epsilon;
    max_x += epsilon;
    max_z += epsilon;
    
    for (int i = 0; i < m_num_points; i++){
        double x = gsl_matrix_get(m_gsl_points, 0, i);
		double y = gsl_matrix_get(m_gsl_points, 1, i);
        double z = gsl_matrix_get(m_gsl_points, 2, i);
        
        if (x <= max_x && x >= min_x && z <= max_z && z >= min_z){
            m_marked_points.push_back(i);
        }
    }

	int num_marked_points = m_marked_points.size();
	printf("[PointCloud::CylinderFloodFillConnected] number of points in entire, not necessarily connected cylinder: %d\n", num_marked_points);

	
    // **** all the points in the cylinder have been found *** now just get the connected ones //
    MarkConnectedMarkedPoints();
    	
	printf("[PointCloud::CylinderFloodFillConnected] number of points connected in cylinder: %d\n", int(m_marked_points.size()));
}

void PointCloud::FitVerticalFeature(){
    printf("[PointCloud::FitVerticalFeature] starting... number of marked user points %d \n", m_user_marked_points.size());
    
    // allocate some matrices, find mean of marked-user-points
    gsl_matrix *A = gsl_matrix_calloc(m_user_marked_points.size(), 2);
	double marked_points_mean[2];
	UtilCopyUserMarkedPoints2D(A, marked_points_mean);
	
    gsl_vector *S = gsl_vector_calloc(2);
    gsl_matrix *V = gsl_matrix_calloc(2,2);
    gsl_vector *work = gsl_vector_calloc(2);

    // run SVD!!! singular value decomposition! 
    int res = gsl_linalg_SV_decomp(A, V, S, work);
    
    // now that we have SVD, let's get the plane normal and do some plane math
    double a = gsl_matrix_get(V, 0, 1);
    double b = gsl_matrix_get(V, 1, 1);
    
    printf("plane normal: %f %f\n", a,b);
    
    double min_y = DBL_MAX;
    double max_y = -1 * DBL_MAX;
    
    for (int h = 0; h < m_user_marked_points.size(); h++){
        int i = m_user_marked_points[h];
        double local_y = gsl_matrix_get(m_gsl_points, 1, i);
        if (local_y < min_y)
            min_y = local_y;
        if (local_y > max_y)
            max_y = local_y;
    }
    
    printf("min and max y: %f %f\n", min_y, max_y);
    
    // hessian normal form for doing pt distance stuff 
    double x = marked_points_mean[0];
    double y = (min_y + max_y) / 2.0;
    double z = marked_points_mean[1];
    
    printf("line centroid: %f %f %f\n", x,y,z);
    
    double d = -1 * (a * x + b * z);
	
    double denom = 1 / sqrt(a*a + b*b);
    
    double hess[2] = {a * denom, b * denom};
    double p = d * denom;
    
    double dist;
    for (int i = 0; i < m_num_points; i++){
        double local_y = gsl_matrix_get(m_gsl_points, 1, i);
        if (!m_delete[i] && local_y < max_y && local_y > min_y){
            dist = 0;
            dist += (hess[0] * gsl_matrix_get(m_gsl_points, 0, i));
            dist += (hess[1] * gsl_matrix_get(m_gsl_points, 2, i));
            dist += p;
            if (abs(dist) < 0.0006){
                m_marked_points.push_back(i);
            }
        }
    }
	
	// go find connected sub component of this
    MarkConnectedMarkedPoints();
    
    /*
    quad q;
    q.r = q.g = q.b = 0;
    
    double min_x = DBL_MAX;
    double max_x = -1 * DBL_MAX;
    double min_z = DBL_MAX;
    double max_z = -1 * DBL_MAX;
    
    for (int h = 0; h < m_marked_points.size(); h++){
        int i = m_marked_points[h];
        double local_x = gsl_matrix_get(m_gsl_points, 0, i);
        double local_z = gsl_matrix_get(m_gsl_points, 2, i);
        if (local_x > max_x)
            max_x = local_x;
        if (local_x < min_x)
            min_x = local_x;
        if (local_z > max_z)
            max_z = local_z;
        if (local_z < min_z)
            min_z = local_z;
        q.r += gsl_matrix_get(m_gsl_colors, 0, i);
        q.g += gsl_matrix_get(m_gsl_colors, 1, i);
        q.b += gsl_matrix_get(m_gsl_colors, 2, i);
    }
    
    printf("min and max x: %f %f\n", min_x, max_x);
    
    q.r /= m_marked_points.size();
    q.g /= m_marked_points.size();
    q.b /= m_marked_points.size();
    
    if (abs(b) > abs(a)){
        min_z = -1*(d + a*min_x)/b;
        max_z = -1*(d + a*max_x)/b;
    }
    else {
        min_x = -1*(d + b*min_z)/a;
        max_x = -1*(d + b*max_z)/a;
    }
    */
    
    /*
    q.corners[0][0] = x;
    q.corners[0][1] = min_y;
    q.corners[0][2] = z;
    
    q.corners[1][0] = x;
    q.corners[1][1] = max_y;
    q.corners[1][2] = z;
    */
    
    /*
    q.corners[0][0] = min_x;
    q.corners[0][1] = min_y;
    q.corners[0][2] = min_z;
    
    q.corners[1][0] = min_x;
    q.corners[1][1] = max_y;
    q.corners[1][2] = min_z;
    
    
    q.corners[2][0] = max_x;
    q.corners[2][1] = max_y;
    q.corners[2][2] = max_z;
    
    q.corners[3][0] = max_x;
    q.corners[3][1] = min_y;
    q.corners[3][2] = max_z;
    
    m_quads.push_back(q);
    */
}


void PointCloud::MakeCylinder(){
    // all marked points are in m_marked_points and m_user_marked_points

    // find max horizontal distance between points and keep track of the center point, too
    // find the top and bottom, too, while we're at it
    double max_diam = 0;
    double center_x, center_z; 
    double min_y = DBL_MAX;
    double max_y = -1 * DBL_MAX;
    double avg_color[3];
    for (int k = 0; k < 3; k++)
        avg_color[k] = 0;
        
    for (int h = 0; h < m_marked_points.size(); h++){
        int i = m_marked_points[h];
        double x = gsl_matrix_get(m_gsl_points, 0, i);
		double y = gsl_matrix_get(m_gsl_points, 1, i);
        double z = gsl_matrix_get(m_gsl_points, 2, i);
        if (y < min_y)
            min_y = y;
        if (y > max_y)
            max_y = y;
        
        for (int k = 0; k < 3; k++)
            avg_color[k] += gsl_matrix_get(m_gsl_colors, k, i);
        
        for (int g = 0; g < m_marked_points.size(); g++){
            int j = m_marked_points[g];
            double x1 = gsl_matrix_get(m_gsl_points, 0, j);
            double y1 = gsl_matrix_get(m_gsl_points, 1, j);
            double z1 = gsl_matrix_get(m_gsl_points, 2, j);
            double diam = sqrt( pow((x1 - x),2) + pow((z1 - z),2) );
            if (diam > max_diam){
                max_diam = diam;
                center_x = (x1 + x)/2.0;
                center_z = (z1 + z)/2.0;
            }
        }
    }
    
    for (int k = 0; k < 3; k++)
        avg_color[k] /= m_marked_points.size();
    
    printf("[PointCloud::MakeCylinder] Tall skinny cylinder found -- radius %f, center %f %f, top %f bottom %f\n", max_diam, center_x, center_z, max_y, min_y);
    
    /*
    texture tex;
	tex.w = 1;
	tex.h = 1;
	tex.bitmap = (int*)calloc(4, sizeof(int));
	for (int k = 0; k < 3; k++)
    	tex.bitmap[k] = avg_color[k];
	tex.bitmap[3] = 150;
	m_model->m_textures.push_back(tex);
	*/
    
    cylinder c;
    c.radius = max_diam/2.0;
    c.top = max_y;
    c.bottom = min_y;
    c.center[0] = center_x;
    c.center[1] = (min_y + max_y)/2.0;
    c.center[2] = center_z;
    //m_vertical_columns.push_back(c);
    
    //m_model->TurnCylinderIntoTriangles(c);
}

void PointCloud::ColorFloodFillConnected(){
    printf("[PointCloud::ColorFloodFillConnected]");
    
	bool *selected = (bool*)calloc(m_num_points, sizeof(bool));
    
    double avg_color[3];
    for (int k = 0; k < 3; k++)
        avg_color[k] = 0;
        
    std::vector<point> selected_points;
    for (int h = 0; h < m_user_marked_points.size(); h++){
        int i = m_user_marked_points[h];
        point pt;
        pt.x = gsl_matrix_get(m_gsl_points, 0, i);
        pt.y = gsl_matrix_get(m_gsl_points, 1, i);
        pt.z = gsl_matrix_get(m_gsl_points, 2, i);
        pt.r = gsl_matrix_get(m_gsl_colors, 0, i);
        pt.g = gsl_matrix_get(m_gsl_colors, 1, i);
        pt.b = gsl_matrix_get(m_gsl_colors, 2, i);
        for (int k = 0; k < 3; k++)
            avg_color[k] += gsl_matrix_get(m_gsl_colors, k, i);
        selected_points.push_back(pt);
    }
    
     for (int k = 0; k < 3; k++)
        avg_color[k] /= m_user_marked_points.size();
    
    ANNpointArray ann_points = annAllocPts(m_num_points, 3);
    
    for (int i = 0; i < m_num_points; i++){
        if (!m_delete[i]){
            for (int k = 0; k < 3; k++){
                ann_points[i][k] = gsl_matrix_get(m_gsl_points, k, i);
            }
        }
    }

	
    ANNkd_tree* kd_3d = new ANNkd_tree(ann_points, m_num_points, 3);
	
    // search through this space of points and find more near-by points to add     
    int n = 20;
    double radius = 0.00000000000000001;
    ANNpoint queryPt = annAllocPt(3);
    ANNidxArray nnIdx = new ANNidx[n];
    ANNdistArray dists = new ANNdist[n];
    
    for (int i = 0; i < selected_points.size(); i++){
        queryPt[0] = selected_points[i].x;
        queryPt[1] = selected_points[i].y;
        queryPt[2] = selected_points[i].z;
        int double_count = 0;
        while (kd_3d->annkFRSearch(queryPt, radius, 0) < 2 && double_count < 2){
            radius *= 1.2;
            double_count ++;
            printf("    doubling radius %.20f\n", radius);
        }
    }
    
    radius *= 4;
    
	printf("[PointCloud::ColorFloodFillConnected] radius: %.10f, number of selected points: %d\n", radius, selected_points.size());
	
	std::vector<int> connected_marked_points;
	
    while (!selected_points.empty() && selected_points.size() < 80000 && selected_points.size() > 20){
        point pt = selected_points.back();
        selected_points.pop_back();
		
        queryPt[0] = pt.x;
        queryPt[1] = pt.y;
        queryPt[2] = pt.z;
        
        int r = pt.r;
        int b = pt.b;
        int g = pt.g;
        
        int neighbors = kd_3d->annkFRSearch(queryPt, radius, n, nnIdx, dists, 0);
        printf("neighbors: %d, size of selected pooints: %d\n", neighbors, selected_points.size());
        
        int num_new_neighbors = 0;
        for (int i = 0; i < min(n, neighbors); i++){
            int idx = nnIdx[i];
            if (!selected[nnIdx[i]]){
                num_new_neighbors++;
            }
        }
        
        printf("selected points: %d, number of new neighbors: %d\n", selected_points.size(), num_new_neighbors);
        
        if (num_new_neighbors > n/5){
            for (int i = 0; i < min(n, neighbors); i++){
                int idx = nnIdx[i];
                if (!selected[nnIdx[i]]){
                    pt.x = gsl_matrix_get(m_gsl_points, 0, idx);
                    pt.y = gsl_matrix_get(m_gsl_points, 1, idx);
                    pt.z = gsl_matrix_get(m_gsl_points, 2, idx);
                    pt.r = gsl_matrix_get(m_gsl_colors, 0, idx);
                    pt.g = gsl_matrix_get(m_gsl_colors, 1, idx);
                    pt.b = gsl_matrix_get(m_gsl_colors, 2, idx);
                    float color_diff = UtilColorDiff(pt.r, pt.b, pt.g, r, g, b);
                    float color_diff2 = UtilColorDiff(pt.r, pt.b, pt.g, avg_color[0], avg_color[1], avg_color[2]);
                    if (color_diff < 1.0 && color_diff2 < 1.0){
                        printf("        adding new point\n");
                        selected_points.insert(selected_points.begin(),pt);
                        selected[nnIdx[i]] = true;
                        pt.r = 255;
                        pt.g = pt.b = 0;
                        connected_marked_points.push_back(idx);    
                    }
                }
            }
        }
    }
	
	m_marked_points = connected_marked_points;
	
	printf("[PointCloud::ColorFloodFillConnected] number of points connected in colored patch: %d\n", int(m_marked_points.size()));
}

void PointCloud::MakeTree(){
    printf("[PointCloud::MakeTree] Making... A TREE!\n");
    double avg_pos[3];
    for (int k = 0; k < 3; k++){
        avg_pos[k] = 0;
    }
    
    int c = 0;
    int d = 6;
    for (int h = 0; h < m_marked_points.size(); h += d){
        int i = m_marked_points[h];
        for (int k = 0; k < 3; k++)
            avg_pos[k] = gsl_matrix_get(m_gsl_points, k, i);
        c++;
    }
    
    
    GeometricComponent* geom = new GeometricComponent();
    geom->m_type = POLY;
    
    geom->m_texture.w = c;
    geom->m_texture.h = 1;
    geom->m_texture.bitmap = (int*)malloc(c*4*sizeof(int));
    
    for (int h = 0; h < c; h++){
        int i = m_marked_points[h*d];
        double pos[3];
        for (int k = 0; k < 3; k++){
            geom->m_texture.bitmap[h*4 + k] = gsl_matrix_get(m_gsl_colors, k, i);
            pos[k] = gsl_matrix_get(m_gsl_points, k, i);
        }
        geom->m_texture.bitmap[h*4 + 3] = 150;
        
        
        tex_tri_vertex vert[3];
        
        vert[0].pos[0] = pos[0];
        vert[0].pos[1] = pos[1];
        vert[0].pos[2] = pos[2];
        
        vert[0].pos[0] = pos[0] + 0.0003;
        vert[0].pos[1] = pos[1] - 0.0009;
        vert[0].pos[2] = pos[2] + 0.0003;
        
        vert[0].pos[0] = pos[0] - 0.0003;
        vert[0].pos[1] = pos[1] - 0.0009;
        vert[0].pos[2] = pos[2] + 0.0003;
        
        for (int k = 0; k < 3; k++){
            vert[k].tex[0] = h;
            vert[k].tex[0] = 1;
            for (int l = 0; l < 3; l++)
                vert[k].color[l] = geom->m_texture.bitmap[h*4 + l]/255.0;
        }
        
        for (int k = 0; k < 3; k++)
            geom->m_triangle_vertices.push_back(vert[k]);
    }
    
    m_geometry.push_back(geom);
}

void PointCloud::ClusterMarkedPoints(){
    printf("[PointCloud::ClusterMarkedPoints] starting...\n");
    
    
    // put user marked points in ANN kd tree
    ANNpointArray ann_points = annAllocPts(m_user_marked_points.size(), 3);
    
    for (int h = 0; h < m_user_marked_points.size(); h++){
        int i = m_user_marked_points[h];
        if (!m_delete[i]){
            for (int k = 0; k < 3; k++){
                ann_points[h][k] = gsl_matrix_get(m_gsl_points, k, i);
            }
        }
    }
    
    ANNkd_tree* kd_3d = new ANNkd_tree(ann_points, m_user_marked_points.size(), 3);
    
    printf("[PointCloud::ClusterMarkedPoints] %d user_marked points put in ANN kd tree\n", m_user_marked_points.size());
    
    // Find magical radius 
    int n = 20;
    double radius = 0.00000000000000001;
    ANNpoint queryPt = annAllocPt(3);
    ANNidxArray nnIdx = new ANNidx[n];
    ANNdistArray dists = new ANNdist[n];
    
    for (int h = 0; h < m_user_marked_points.size(); h++){
        int i = m_user_marked_points[h];
        for (int k = 0; k < 3; k++){
            queryPt[k] = gsl_matrix_get(m_gsl_points, k, i);
        }
        
        int double_count = 0;
        while (kd_3d->annkFRSearch(queryPt, radius, 0) < 2 && double_count < 2){
            radius *= 1.2;
            double_count ++;
            //printf("    doubling radius %.20f\n", radius);
        }
    }
    
    radius *= 2;
    
    printf("[PointCloud::ClusterMarkedPoints] Radius found: %.10ff\n", radius);
	
	int* which_cluster = (int*)calloc(m_user_marked_points.size(), sizeof(int));
	int cluster_id = 0;
	
	for (int h = 0; h < m_user_marked_points.size(); h++){
        int i = m_user_marked_points[h];
        if (which_cluster[i] == 0){
            // start new cluster
            cluster_id++;
            printf("Starting new cluster %d\n", cluster_id);
            std::vector<int> selected_points; 
            selected_points.insert(selected_points.begin(), i);
            while (!selected_points.empty() && selected_points.size() < 80000){
                int j = selected_points.back();
                selected_points.pop_back();
                for (int k = 0; k < 3; k++){
                    queryPt[k] = gsl_matrix_get(m_gsl_points, k, j);
                }
                int neighbors = kd_3d->annkFRSearch(queryPt, radius, n, nnIdx, dists, 0);
                //printf("neighbors: %d, size of selected pooints: %d\n", neighbors, selected_points.size());
                for (int m = 0; m < min(n, neighbors); m++){
                    int idx = m_user_marked_points[nnIdx[m]];
                    if (which_cluster[idx] == 0){                    
                        selected_points.insert(selected_points.begin(), idx);
                        which_cluster[idx] = cluster_id;
                        //printf("        adding new point, %d %d\n", which_cluster[idx], idx);
                    }
                }
            }
        }
	}
	
	printf("[PointCloud::ClusterMarkedPoints] number of clusters: %d, total number of points: %d\n", cluster_id, m_user_marked_points.size());
	
	int* cluster_count = (int*)calloc(cluster_id, sizeof(int));
	for (int h = 0; h < m_user_marked_points.size(); h++){
	   int i = m_user_marked_points[h];
	   cluster_count[which_cluster[i]-1] ++;
	}
	
	for (int i = 0; i < cluster_id; i++){
	   printf("cluster %d: %d items, %f\n", i, cluster_count[i], float(cluster_count[i])/m_user_marked_points.size());
	}
	
	/*
	std::vector<int> saved_user_marked_points = m_user_marked_points; 
	std::vector<int> total_marked_points;
	
	for (int i = 0; i < cluster_id; i++){
	   if (cluster_count[i] > 100){
	       printf("[PointCloud::ClusterMarkedPoints] fitting plane to cluster %d\n", i);
	       m_user_marked_points.clear();
	       m_marked_points.clear();
	       for (int h = 0; h < saved_user_marked_points.size(); h++){
	           int j = saved_user_marked_points[h];
	           if ( (which_cluster[j]-1) == i){
	               m_user_marked_points.push_back(j);
	           }
	       }
	       PlaneFloodFillConnected();
	       total_marked_points.insert(total_marked_points.end(), m_marked_points.begin(), m_marked_points.end());
	   }
	}
	
	m_marked_points = total_marked_points;
	m_user_marked_points = saved_user_marked_points;
	*/
}

void PointCloud::SetMarkedPointPlane(double a, double b, double c, double d){
	m_marked_point_plane.a = a;
	m_marked_point_plane.b = b;
	m_marked_point_plane.c = c;
	m_marked_point_plane.d = d;
	
	m_marked_point_plane.line_m = 0;
	m_marked_point_plane.line_b = 0;
	
	for (int k = 0; k < 3; k++){
		m_marked_point_plane.min[k] = 0;
		m_marked_point_plane.max[k] = 0;
	}
	
	m_marked_point_plane.w = 0;
	m_marked_point_plane.h = 0;
}

void PointCloud::MakePlane(){
	// retrieve the plane in question
	plane p = m_marked_point_plane;
	if (abs(p.b) < 0.16){
		// plane is vertical
		MakeGroundAlignedPlane(p.a, p.b, p.c, p.d);
	}
	else {
		// plane is not vertical
		printf("plane is not vertical\n");
		fflush(stdout);
	}
}

void PointCloud::MakeGroundAlignedPlane(double a, double b, double c, double d){
    printf("[PointCloud::MakeGroundAlignedPlane] \n");
    
    int num_marked_points = m_marked_points.size();
    if (num_marked_points == 0)
        return;
        
    double line_m = -1*a/c;
    double line_b = -1*d/c;
    
    // find min and max values of this new data
    double min_x = DBL_MAX;
    double min_y = DBL_MAX;
    double min_z = DBL_MAX;
    double max_x = -min_x;
    double max_y = -min_y;
    double max_z = -min_z;
    for (int h = 0; h < m_marked_points.size(); h++){
        int i = m_marked_points[h];
        double x = gsl_matrix_get(m_gsl_points, 0, i);
        double y = gsl_matrix_get(m_gsl_points, 1, i);
        double z = gsl_matrix_get(m_gsl_points, 2, i);
        if (x < min_x)
            min_x = x;
        if (x > max_x)
            max_x = x;
        
        if (y < min_y)
            min_y = y;
        if (y > max_y)
            max_y = y;
            
        if (z < min_z)
            min_z = z;
        if (z > max_z)
            max_z = z;
    }
    
    printf("max x and y values here: %f %f, %f %f, %f %f\n", min_x, max_x, min_y, max_y, min_z, max_z);
    


    double mean_x = 0;
    double mean_z = 0;
	for (int h = 0; h < m_marked_points.size(); h++){
        int i = m_marked_points[h];
        mean_x +=  gsl_matrix_get(m_gsl_points, 0, i);
        mean_z +=  gsl_matrix_get(m_gsl_points, 2, i);
    }
    
    mean_x /= m_marked_points.size();
    mean_z /= m_marked_points.size();
    min_y = -1*(m_ground_plane.a * mean_x + m_ground_plane.c * mean_z + m_ground_plane.d)/m_ground_plane.b;
    
    /*
    // UPDATING IF STUFF HITS OTHER STUFF
    std::map<int, plane> intersecting_planes;
    for (int h = 0; h < m_marked_points.size(); h++){
        int i = m_marked_points[h];
		structure_ptr s = m_point_lookup[i];
        if (s.idx >= 0 && s.idx < m_vertical_planes.size())
		  intersecting_planes[s.idx] = m_vertical_planes[s.idx];
		  
        mean_x +=  gsl_matrix_get(m_gsl_points, 0, i);
        mean_z +=  gsl_matrix_get(m_gsl_points, 2, i);
    }
        
    m_planes_changed.clear();
    printf("number of intersecting planes: %d\n", intersecting_planes.size());
    for (std::map<int, plane>::const_iterator it = intersecting_planes.begin(); it != intersecting_planes.end(); ++it) {
        plane p = it->second;
        
        double int_x, int_z;
        int_x = -1*(line_b - p.line_b)/(line_m - p.line_m);
        int_z = line_m * int_x + line_b;
        
        if ( abs(max_x - int_x) < abs(max_x - min_x) || abs(max_z - int_z) < abs(max_z - min_z) ){
            printf("--- in the right range of the new line\n");
            if ( abs(p.max[0] - int_x) < abs(p.max[0] - p.min[0]) || abs(p.max[2] - int_z) < abs(p.max[2] - p.min[2]) ){
                printf("---- also in range of other old line\n");
                
                if (max_x > min_x){
                    if (int_x > mean_x)
                        max_x = int_x;
                    else 
                        min_x = int_x;
                }
                else {
                    printf(" *** hmm, min x > max x\n");
                }
                if (max_z > min_z){
                    if (int_z > mean_z)
                        max_z = int_z;
                    else
                        min_z = int_z;
                }
                
                printf(" **** --- about to NOT call UpdateGroundAlignedPlane. Current plane id: %d, plane to change: %d\n", m_vertical_planes.size(), it->first);
                //UpdateGroundAlignedPlane(it->first, int_x, int_z);
            }
        }
        
    }
    
    printf("Number of planes updated: %d\n", m_planes_changed.size());
    	
	for (int h = 0; h < m_marked_points.size(); h++){
        int i = m_marked_points[h];
        
        m_point_lookup[i].idx = structure_idx;
        m_point_lookup[i].type = structure_type;
    }
	
	if (abs(b) < abs(a)){
	   min_z = line_m * min_x + line_b;
	   max_z = line_m * max_x + line_b;
    }
    else {
        min_x = (min_z - line_b)/line_m;
        max_x = (max_z - line_b)/line_m;
    }
     
	printf("[PointCloud::MakeGroundAlignedPlane] NEW max x and y values here: %f %f, %f %f, %f %f\n", min_x, max_x, min_y, max_y, min_z, max_z);
    */
    
    printf("number of marked points: %d\n", num_marked_points);
    
	// Sampling texture here
	
    float horiz_distance = sqrt( (max_x - min_x)*(max_x - min_x) + (max_z - min_z)*(max_z - min_z) );
    float ratio = horiz_distance / (max_y - min_y);
    int samp1 = sqrt(num_marked_points * ratio) / 1.0;
    int samp2 = sqrt(num_marked_points / ratio) / 1.0;
	
    m_marked_point_plane.a = a;
    m_marked_point_plane.b = b;
    m_marked_point_plane.c = c;
    m_marked_point_plane.d = d;
    m_marked_point_plane.samp1 = samp1;
    m_marked_point_plane.samp2 = samp2;
    m_marked_point_plane.min[0] = min_x;
    m_marked_point_plane.min[1] = min_y;
    m_marked_point_plane.min[2] = line_m * min_x + line_b;
    m_marked_point_plane.max[0] = max_x;
    m_marked_point_plane.max[1] = max_y;
    m_marked_point_plane.max[2] = line_m * max_x + line_b;
    m_marked_point_plane.line_m = line_m;
    m_marked_point_plane.line_b = line_b;
    //m_marked_point_plane.center[0] = mean_x;
    //m_marked_point_plane.center[2] = mean_z;
    
	//m_vertical_planes.push_back(m_marked_point_plane);
	
	GeometricComponent* geom = new GeometricComponent();
	geom->m_type = VERT_PLANE;
	geom->m_vertical_plane = new prim_vertical_plane;
	
	geom->m_vertical_plane->line_m = line_m;
	geom->m_vertical_plane->line_b = line_b;
	for (int k = 0; k < 3; k++){
	   geom->m_vertical_plane->min[k] = m_marked_point_plane.min[k];
	   geom->m_vertical_plane->max[k] = m_marked_point_plane.max[k];
	   geom->m_vertical_plane->center[k] = m_marked_point_plane.center[k];
    }
    
    geom->m_origin_points = m_marked_points;
	
	PlaneSampleTextureTrianglesMarkedPoints(geom, m_marked_point_plane);
}

void PointCloud::PlaneSampleTextureTriangles(GeometricComponent* geom, plane pl, int idx){
    printf("[PlaneSampleTextureTriangles] beginning of function\n");
    fflush(stdout);
    
    ANNpointArray ann_points = annAllocPts(m_num_points, 3);
        
    int samp1 = pl.samp1;
    int samp2 = pl.samp2;
    
    double a = pl.a;
    double b = pl.b;
    double c = pl.c;
    double d = pl.d;
    double line_m = -1*a/c;
    double line_b = -1*d/c;
    
    double min_x = pl.min[0];
    double min_y = pl.min[1];
    double min_z = pl.min[2];
    double max_x = pl.max[0];
    double max_y = pl.max[1];
    double max_z = pl.max[2];
    for (int i = 0; i < m_num_points; i++){
            for (int k = 0; k < 3; k++){
            ann_points[i][k] = gsl_matrix_get(m_gsl_points, k, i);
        }
    }
	
    ANNkd_tree* kd_3d = new ANNkd_tree(ann_points, m_num_points, 3);
    
    ANNpoint queryPt = annAllocPt(3);
    float radius = 0.000000091;
    int n = 50;
    ANNidxArray nnIdx = new ANNidx[n];
    ANNdistArray dists = new ANNdist[n];

	geom->m_texture.w = samp1;
	geom->m_texture.h = samp2;
	printf("[PlaneSampleTextureTriangles] texture width and height set\n");
	
	geom->m_texture.bitmap = (int*)calloc(4*samp1*samp2, sizeof(int));
	printf("[PlaneSampleTextureTriangles] texture bitmap allocated \n");
	
	double avg_color[3];
	for (int k = 0; k < 3; k++)
	   avg_color[k]=0;
	
    for (float i = 0; i < samp1; i++){
        for (float j = 0; j < samp2; j++){       
            point p;
            p.x = min_x + i*(max_x-min_x)/float(samp1);
            p.y = min_y + j*(max_y-min_y)/float(samp2);
            p.z = line_m * p.x + line_b;
            p.r = 0;
            p.g = 0;
            p.b = 255;
            
            queryPt[0] = p.x;    
            queryPt[1] = p.y;     
            queryPt[2] = p.z;   
            
            kd_3d->annkSearch(queryPt, n, nnIdx, dists, 0);
            double r = 0;
            double b = 0;
            double g = 0;
            
            double tot_weight = 0;
            
            for (int k = 0; k < n; k++){
                double weight = 1/dists[k];
                r += weight*gsl_matrix_get(m_gsl_colors, 0, nnIdx[k]);
                g += weight*gsl_matrix_get(m_gsl_colors, 1, nnIdx[k]);
                b += weight*gsl_matrix_get(m_gsl_colors, 2, nnIdx[k]);  
			
                tot_weight += weight;
            }
            r /= tot_weight;
            g /= tot_weight;
            b /= tot_weight;
            
            p.r = r;
            p.g = g;
            p.b = b;
            
            avg_color[0] += r;
            avg_color[1] += g;
            avg_color[2] += b;
			
			int b_idx = 4*(i*samp2 + j);
			geom->m_texture.bitmap[b_idx + 0] = p.r;
			geom->m_texture.bitmap[b_idx + 1] = p.g;
			geom->m_texture.bitmap[b_idx + 2] = p.b;
			geom->m_texture.bitmap[b_idx + 3] = 200;
			
			// no neihgbors, make pixel transparent
			int neighbors = kd_3d->annkFRSearch(queryPt, 0.00000051, 0);
			if (neighbors == 0)
				geom->m_texture.bitmap[b_idx + 3] = 40;
        }
    }
    
    printf("[PlaneSampleTextureTriangles] texture computed\n");
    
    for (int k = 0; k < 3; k++){
        avg_color[k] /=  (samp1*samp2);
    }

    // triangle samples
	samp1 = 2;//max(10, samp1 / 3 + 1);
	samp2 = 2;//max(10, samp2 / 3 + 1);
	
	printf("geometry sampling rate: %d %d\n", samp1, samp2);
	
	std::vector<tex_tri_vertex> ordered_vertices;
	
	for (float i = 0; i < samp1; i++){
        for (float j = 0; j < samp2; j++){       
            
            tex_tri_vertex vert;
            
            vert.pos[0] = min_x + i*(max_x-min_x)/float(samp1 - 1);
            vert.pos[1] = min_y + j*(max_y-min_y)/float(samp2 - 1);
            vert.pos[2] = line_m * vert.pos[0] + line_b;
            
            for (int k = 0; k < 3; k++)
                queryPt[k] = vert.pos[k];
                            
            kd_3d->annkSearch(queryPt, n, nnIdx, dists, 0);
            
            double x = 0;
            double y = 0;
            double z = 0;
			
			double r = 0;
            double b = 0;
            double g = 0;
            
            double tot_weight = 0;
            
            for (int k = 0; k < n; k++){
                double weight = 1/dists[k];
                x += weight*gsl_matrix_get(m_gsl_points, 0, nnIdx[k]); 
                y += weight*gsl_matrix_get(m_gsl_points, 1, nnIdx[k]); 
                z += weight*gsl_matrix_get(m_gsl_points, 2, nnIdx[k]); 
				
                r += weight*gsl_matrix_get(m_gsl_colors, 0, nnIdx[k]);
                g += weight*gsl_matrix_get(m_gsl_colors, 1, nnIdx[k]);
                b += weight*gsl_matrix_get(m_gsl_colors, 2, nnIdx[k]);  
				
                tot_weight += weight;
            }
            
            x /= tot_weight;
            y /= tot_weight;
            z /= tot_weight;
            
            //if (i == 0 || i == samp1-1 || j == 0 || j == samp2-1){
            if (1) {
                // donothing
            }
            else {
                vert.pos[0] = x;
                vert.pos[1] = y;
                vert.pos[2] = z;
			}
			
			r /= tot_weight;
            g /= tot_weight;
            b /= tot_weight;

            
            vert.color[0] = r/255.0;
            vert.color[1] = g/255.0;
            vert.color[2] = b/255.0;
            vert.color[3] = .5;
            
			
			vert.tex[0] = (vert.pos[0] - min_x)/(max_x - min_x);
			vert.tex[1] = (vert.pos[1] - min_y)/(max_y - min_y);

			ordered_vertices.push_back(vert);
        }
    }
	
	for (int i = 0; i < samp1 - 1; i++){
		for (int j = 0; j < samp2 - 1; j++){
		
			geom->m_triangle_vertices.push_back(ordered_vertices[i*samp2 + j]);
			geom->m_triangle_vertices.push_back(ordered_vertices[i*samp2 + j + 1]);
			geom->m_triangle_vertices.push_back(ordered_vertices[(i + 1)*samp2 + j + 1]);
			
			geom->m_triangle_vertices.push_back(ordered_vertices[(i + 1)*samp2 + j + 1]);
			geom->m_triangle_vertices.push_back(ordered_vertices[(i + 1)*samp2 + j]);
			geom->m_triangle_vertices.push_back(ordered_vertices[i*samp2 + j]);
		}
	}
    m_geometry.push_back(geom);
}


void PointCloud::PlaneSampleTextureTrianglesMarkedPoints(GeometricComponent* geom, plane pl, int idx){
    int num_marked_points = m_marked_points.size();
    
    int n = 50;
    if (num_marked_points <= n){
        printf("[PlaneSampleTextureTrianglesMarkedPoints] number of marked points is too low\n");
        return;    
    }
        
    ANNpointArray ann_points = annAllocPts(num_marked_points, 3);
    gsl_matrix *colors = gsl_matrix_calloc(num_marked_points, 3);
    
    int samp1 = pl.samp1;
    int samp2 = pl.samp2;
    
    double a = pl.a;
    double b = pl.b;
    double c = pl.c;
    double d = pl.d;
    double line_m = -1*a/c;
    double line_b = -1*d/c;
    
    double min_x = pl.min[0];
    double min_y = pl.min[1];
    double min_z = pl.min[2];
    double max_x = pl.max[0];
    double max_y = pl.max[1];
    double max_z = pl.max[2];
    for (int h = 0; h < m_marked_points.size(); h++){
        int i = m_marked_points[h];
        for (int k = 0; k < 3; k++){
            ann_points[h][k] = gsl_matrix_get(m_gsl_points, k, i);
            gsl_matrix_set(colors, h, k, gsl_matrix_get(m_gsl_colors, k, i));
        }
    }
	
    ANNkd_tree* kd_3d = new ANNkd_tree(ann_points, num_marked_points, 3);
    
    ANNpoint queryPt = annAllocPt(3);
    float radius = 0.000000091;
    ANNidxArray nnIdx = new ANNidx[n];
    ANNdistArray dists = new ANNdist[n];

	geom->m_texture.w = samp1;
	geom->m_texture.h = samp2;
	printf("[PlaneSampleTextureTrianglesMarkedPoints] texture width and height set\n");
	
	geom->m_texture.bitmap = (int*)calloc(4*samp1*samp2, sizeof(int));
	printf("[PlaneSampleTextureTrianglesMarkedPoints] texture bitmap allocated \n");
	
	double avg_color[3];
	for (int k = 0; k < 3; k++)
	   avg_color[k]=0;
	
    for (float i = 0; i < samp1; i++){
        for (float j = 0; j < samp2; j++){       
            point p;
            p.x = min_x + i*(max_x-min_x)/float(samp1);
            p.y = min_y + j*(max_y-min_y)/float(samp2);
            p.z = line_m * p.x + line_b;
            p.r = 0;
            p.g = 0;
            p.b = 255;
            
            queryPt[0] = p.x;    
            queryPt[1] = p.y;     
            queryPt[2] = p.z;   
            
            kd_3d->annkSearch(queryPt, n, nnIdx, dists, 0);
            double r = 0;
            double b = 0;
            double g = 0;
            
            double tot_weight = 0;
            
            for (int k = 0; k < n; k++){
                double weight = 1/dists[k];
                r += weight*gsl_matrix_get(colors, nnIdx[k], 0);
                g += weight*gsl_matrix_get(colors, nnIdx[k], 1);
                b += weight*gsl_matrix_get(colors, nnIdx[k], 2);  
			
                tot_weight += weight;
            }
            r /= tot_weight;
            g /= tot_weight;
            b /= tot_weight;
            
            p.r = r;
            p.g = g;
            p.b = b;
            
            avg_color[0] += r;
            avg_color[1] += g;
            avg_color[2] += b;
			
			int b_idx = 4*(i*samp2 + j);
			geom->m_texture.bitmap[b_idx + 0] = p.r;
			geom->m_texture.bitmap[b_idx + 1] = p.g;
			geom->m_texture.bitmap[b_idx + 2] = p.b;
			geom->m_texture.bitmap[b_idx + 3] = 200;
			
            // no neihgbors, make pixel transparent
			int neighbors = kd_3d->annkFRSearch(queryPt, 0.00000051, 0);
			if (neighbors == 0)
				geom->m_texture.bitmap[b_idx + 3] = 40;
        }
    }
    
    for (int k = 0; k < 3; k++){
        avg_color[k] /=  (samp1*samp2);
    }
	
    // triangle samples
	samp1 = 2;//max(10, samp1 / 3 + 1);
	samp2 = 2;//max(10, samp2 / 3 + 1);
	
	printf("geometry sampling rate: %d %d\n", samp1, samp2);
	
	std::vector<tex_tri_vertex> ordered_vertices;
	
	for (float i = 0; i < samp1; i++){
        for (float j = 0; j < samp2; j++){       
            
            tex_tri_vertex vert;
            
            vert.pos[0] = min_x + i*(max_x-min_x)/float(samp1 - 1);
            vert.pos[1] = min_y + j*(max_y-min_y)/float(samp2 - 1);
            vert.pos[2] = line_m * vert.pos[0] + line_b;
            
            for (int k = 0; k < 3; k++)
                queryPt[k] = vert.pos[k];
                            
            kd_3d->annkSearch(queryPt, n, nnIdx, dists, 0);
            
            double x = 0;
            double y = 0;
            double z = 0;
			
			double r = 0;
            double b = 0;
            double g = 0;
            
            double tot_weight = 0;
            
            for (int k = 0; k < n; k++){
                double weight = 1/dists[k];
                x += weight*gsl_matrix_get(m_gsl_points, 0, nnIdx[k]); 
                y += weight*gsl_matrix_get(m_gsl_points, 1, nnIdx[k]); 
                z += weight*gsl_matrix_get(m_gsl_points, 2, nnIdx[k]); 
				
                r += weight*gsl_matrix_get(m_gsl_colors, 0, nnIdx[k]);
                g += weight*gsl_matrix_get(m_gsl_colors, 1, nnIdx[k]);
                b += weight*gsl_matrix_get(m_gsl_colors, 2, nnIdx[k]);  
				
                tot_weight += weight;
            }
            
            x /= tot_weight;
            y /= tot_weight;
            z /= tot_weight;
            
            if (i == 0 || i == samp1-1 || j == 0 || j == samp2-1){
                // donothing
            }
            else {
                vert.pos[0] = x;
                vert.pos[1] = y;
                vert.pos[2] = z;
			}
			
			r /= tot_weight;
            g /= tot_weight;
            b /= tot_weight;
            
            printf("color for pt %f %f: %f %f %f\n", i, j, r, g, b);
            
            vert.color[0] = r/255.0;
            vert.color[1] = g/255.0;
            vert.color[2] = b/255.0;
            vert.color[3] = .5;
			
			vert.tex[0] = (vert.pos[0] - min_x)/(max_x - min_x);
			vert.tex[1] = (vert.pos[1] - min_y)/(max_y - min_y);

			ordered_vertices.push_back(vert);
        }
    }

    for (int i = 0; i < samp1 - 1; i++){
		for (int j = 0; j < samp2 - 1; j++){
		
			geom->m_triangle_vertices.push_back(ordered_vertices[i*samp2 + j]);
			geom->m_triangle_vertices.push_back(ordered_vertices[i*samp2 + j + 1]);
			geom->m_triangle_vertices.push_back(ordered_vertices[(i + 1)*samp2 + j + 1]);
			
			geom->m_triangle_vertices.push_back(ordered_vertices[(i + 1)*samp2 + j + 1]);
			geom->m_triangle_vertices.push_back(ordered_vertices[(i + 1)*samp2 + j]);
			geom->m_triangle_vertices.push_back(ordered_vertices[i*samp2 + j]);
		}
	}
	
    m_geometry.push_back(geom);
}

void PointCloud::SampleTextureForGeom(GeometricComponent* geom){
    int num_marked_points = m_marked_points.size();
    
    printf("[PointCloud::SampleTextureForGeom] num marked points: %d\n", num_marked_points); 
    fflush(stdout);
    
    ANNpointArray ann_points = annAllocPts(num_marked_points, 3);
        
    prim_quad* q = geom->m_quad;
    
    int samp1 = 10;
    int samp2 = 10;
    
    double min_x = q->corners[0][0];
    double min_y = q->corners[0][1];
    double min_z = q->corners[0][2];
    double max_x = q->corners[2][0];
    double max_y = q->corners[2][1];
    double max_z = q->corners[2][2];
    
    double line_m = (max_z - min_z)/(max_x - min_x);
    double line_b = max_z - line_m*max_x;
    
    for (int h = 0; h < m_marked_points.size(); h++){
        int i = m_marked_points[h];
        for (int k = 0; k < 3; k++){
            ann_points[h][k] = gsl_matrix_get(m_gsl_points, k, i);
        }
    }
	
    ANNkd_tree* kd_3d = new ANNkd_tree(ann_points, num_marked_points, 3);
    
    ANNpoint queryPt = annAllocPt(3);
    float radius = 0.000000091;
    int n = 50;
    ANNidxArray nnIdx = new ANNidx[n];
    ANNdistArray dists = new ANNdist[n];

	geom->m_texture.w = samp1;
	geom->m_texture.h = samp2;
	printf("[PlaneSampleTextureTrianglesMarkedPoints] texture width and height set\n");
	
	geom->m_texture.bitmap = (int*)calloc(4*samp1*samp2, sizeof(int));
	printf("[PlaneSampleTextureTrianglesMarkedPoints] texture bitmap allocated \n");
	
	double avg_color[3];
	for (int k = 0; k < 3; k++)
	   avg_color[k]=0;
	
    for (float i = 0; i < samp1; i++){
        for (float j = 0; j < samp2; j++){       
            point p;
            p.x = min_x + i*(max_x-min_x)/float(samp1);
            p.y = min_y + j*(max_y-min_y)/float(samp2);
            p.z = line_m * p.x + line_b;
            p.r = 0;
            p.g = 0;
            p.b = 255;
            
            queryPt[0] = p.x;    
            queryPt[1] = p.y;     
            queryPt[2] = p.z;   
            
            kd_3d->annkSearch(queryPt, n, nnIdx, dists, 0);
            double r = 0;
            double b = 0;
            double g = 0;
            
            double tot_weight = 0;
            
            for (int k = 0; k < n; k++){
                double weight = 1/dists[k];
                int idx = m_marked_points[nnIdx[k]];
                 
                r += weight*gsl_matrix_get(m_gsl_colors, 0, idx);
                g += weight*gsl_matrix_get(m_gsl_colors, 1, idx);
                b += weight*gsl_matrix_get(m_gsl_colors, 2, idx); 
			
                tot_weight += weight;
            }
            r /= tot_weight;
            g /= tot_weight;
            b /= tot_weight;
            
            p.r = r;
            p.g = g;
            p.b = b;
            
            avg_color[0] += r;
            avg_color[1] += g;
            avg_color[2] += b;
			
			int b_idx = 4*(i*samp2 + j);
			geom->m_texture.bitmap[b_idx + 0] = p.r;
			geom->m_texture.bitmap[b_idx + 1] = p.g;
			geom->m_texture.bitmap[b_idx + 2] = p.b;
			geom->m_texture.bitmap[b_idx + 3] = 200;
			
        }
    }

}

void PointCloud::MakeWallFromFloorPlan(double *buffer, int num_points){
    int num_segments = num_points - 1;
    float* segment_heights = (float*)malloc(num_segments* sizeof(float));
    for (int s = 0; s < num_segments; s++)
        segment_heights[s] = -1*FLT_MAX;
    
    int* pts_near_each_segment = (int*)calloc(num_segments, sizeof(int));
    
    GeometricComponent* geoms[num_segments];
    for (int s = 0; s < num_segments; s++)
            geoms[s] = new GeometricComponent();
            
    for (int i = 0; i < m_num_points; i++){
        // for each point, see if it's close to a line segment and get its height
        double x = gsl_matrix_get(m_gsl_points, 0, i);
        double y = gsl_matrix_get(m_gsl_points, 1, i);
        double z = gsl_matrix_get(m_gsl_points, 2, i);
        
        
        for (int s = 0; s < num_segments; s++){
            if (1 || y > segment_heights[s]){
                if ( (buffer[s*3] - x) < (buffer[s*3] - buffer[(s+1)*3]) ){
                    // x in range of this segment
                    
                    
                    
                    float dist = abs( (buffer[(s+1)*3 + 0] - buffer[s*3 + 0]) * (buffer[s*3 + 2] - z) - (buffer[(s)*3 + 0] - x) * (buffer[(s+1)*3 + 2] - buffer[s*3 + 2]) ) / sqrt( pow((buffer[(s+1)*3 + 0] - buffer[s*3 + 0]), 2) + pow((buffer[(s+1)*3 + 2] - buffer[s*3 + 2]),2) );
                    
                    if (dist < 0.006){
                        if (y > segment_heights[s])
                            segment_heights[s] = y;
                        //printf("new max height for segment %d: %.10f, pt %d\n", s, y, i);
                        geoms[s]->m_origin_points.push_back(i);
                    }
                }
            }
        }
    }
    
    float avg_height =0 ;
    for (int s = 0; s < num_segments; s++){
        printf("height of segment %d: %.20f\n", s, segment_heights[s]);
        avg_height += segment_heights[s];
    }
    avg_height /= num_segments;
    fflush(stdout);
    
    for (int s = 0; s < num_segments; s++){
        GeometricComponent* geom = geoms[s];
        geom->m_type = QUAD;
        prim_quad* q = new prim_quad;
        geom->m_quad = q;
        
        for (int k = 0; k < 3; k++){
            q->corners[0][k] = buffer[s*3 + k];
            q->corners[1][k] = buffer[s*3 + k];
            q->corners[2][k] = buffer[(s+1)*3 + k];
            q->corners[3][k] = buffer[(s+1)*3 + k];
        }
        
        q->corners[1][1] = avg_height;
        q->corners[2][1] = avg_height;
        
        geom->BuildTriangles();
        
        //SampleTextureForGeom(geom);
        
        m_geometry.push_back(geom);
    }
}

void PointCloud::UpdateGroundAlignedPlane(int idx, double x, double z){
    //plane p = m_vertical_planes[idx];
    //printf("mean of this plane: %f %f\n", p.center[0], p.center[2]);
    //m_planes_changed.push_back(idx);
}

void PointCloud::MakeRoof(){
	int num_marked_points = m_marked_points.size();
	
	double a, b, c, d;
	a = m_marked_point_plane.a;
	b = m_marked_point_plane.b;
	c = m_marked_point_plane.c;
	d = m_marked_point_plane.d;
    
    // find min and max height of points in this plane 
    double min_y = DBL_MAX;
    double max_y = -min_y;
    for (int h = 0; h < m_marked_points.size(); h++){
        int i = m_marked_points[h];
        double y = gsl_matrix_get(m_gsl_points, 1, i);
		
        if (y < min_y)
            min_y = y;
        if (y > max_y)
            max_y = y;
    }
	
	// go through again and find just the points in the top x % of the wall
	// find min/max x and z values too
	
	double min_x = DBL_MAX;
    double min_z = DBL_MAX;
    double max_x = -min_x;
    double max_z = -min_z;
	
	float height_cutoff = max_y - 0.09 * (max_y - min_y);
	std::vector<int> marked_roof_top_points;
	for (int h = 0; h < m_marked_points.size(); h++){
        int i = m_marked_points[h];
        double x = gsl_matrix_get(m_gsl_points, 0, i);
		double y = gsl_matrix_get(m_gsl_points, 1, i);
        double z = gsl_matrix_get(m_gsl_points, 2, i);

        if (y > height_cutoff){
			marked_roof_top_points.push_back(i);
			if (x < min_x)
				min_x = x;
			if (x > max_x)
				max_x = x;
			if (z < min_z)
				min_z = z;
			if (z > max_z)
				max_z = z;
		}
    }
	
	point roof_top1;
	roof_top1.x = min_x;
	roof_top1.y = max_y;
	roof_top1.z = -1*(d + b*max_y + a*min_x)/c;
	point roof_top2;
	roof_top2.x = max_x;
	roof_top2.y = max_y;
	roof_top2.z = -1*(d + b*max_y + a*max_x)/c;
	
	// looking for bottom of roof now... starting with planes that also do this thing
	std::vector<int> marked_roof_bottom_points;
	/*
	
	for (int h = 0; h < m_marked_points.size(); h++){
        int i = m_marked_points[h];
		structure_ptr s = m_point_lookup[i];
		//if (s.idx >= 0)
		//	printf("points intersect with plane %d\n", s.idx);
		double y = gsl_matrix_get(m_gsl_points, 1, i);
		if (s.idx >= 0 && y > m_vertical_planes[s.idx].max[1] * .9){
			marked_roof_bottom_points.push_back(i);
		}
	}
	*/
	
	
	//m_marked_points = marked_roof_top_points;
	//m_marked_points.insert(m_marked_points.end(), marked_roof_bottom_points.begin(), marked_roof_bottom_points.end());
	//return;
	
	double roof_min_x = DBL_MAX;
	double roof_min_y = DBL_MAX;
	double roof_min_z = DBL_MAX;
	double roof_max_x = -1*DBL_MAX;
	double roof_max_y = -1*DBL_MAX;
	double roof_max_z = -1*DBL_MAX;
	if (marked_roof_bottom_points.size() == 0){
		
		// no planes intersected with... just use bottom of selected points
		printf("number of points that intersected with an existing plane: %d\n", marked_roof_bottom_points.size());
		
		float height_cutoff = min_y + 0.09 * (max_y - min_y);
		std::vector<int> marked_roof_top_points;
		for (int h = 0; h < m_marked_points.size(); h++){
			int i = m_marked_points[h];
			double x = gsl_matrix_get(m_gsl_points, 0, i);
			double y = gsl_matrix_get(m_gsl_points, 1, i);
			double z = gsl_matrix_get(m_gsl_points, 2, i);
			
			if (y < height_cutoff){
				marked_roof_bottom_points.push_back(i);
				if (x < roof_min_x)
					roof_min_x = x;
				if (x > roof_max_x)
					roof_max_x = x;
				
				if (y < roof_min_y)
					roof_min_y = y;
				
				if (z < roof_min_z)
					roof_min_z = z;
				if (z > roof_max_z)
					roof_max_z = z;
			}
		}
	}
	else {
		for (int h = 0; h < marked_roof_bottom_points.size(); h++){
			int i = marked_roof_bottom_points[h];
			double x = gsl_matrix_get(m_gsl_points, 0, i);
			double y = gsl_matrix_get(m_gsl_points, 1, i);
			double z = gsl_matrix_get(m_gsl_points, 2, i);
			if (x < roof_min_x)
				roof_min_x = x;
			if (x > roof_max_x)
				roof_max_x = x;
			
			if (y < roof_min_y)
				roof_min_y = y;
			if (y > roof_max_y)
				roof_max_y = y;
			
			if (z < roof_min_z)
				roof_min_z = z;
			if (z > roof_max_z)
				roof_max_z = z;
		}
	}
	
	m_marked_points = marked_roof_bottom_points; 
	
	point roof_bottom1;
	roof_bottom1.x = roof_min_x;
	roof_bottom1.y = roof_min_y;
	roof_bottom1.z = -1*(d + b*roof_min_y + a*roof_min_x)/c;
	point roof_bottom2;
	roof_bottom2.x = roof_max_x;
	roof_bottom2.y = roof_min_y;
	roof_bottom2.z = -1*(d + b*roof_min_y + a*roof_max_x)/c;
	
	
	float c_r = 0;
	float c_g = 0;
	float c_b = 0;
	for (int h = 0; h < m_user_marked_points.size(); h++){
		int i = m_user_marked_points[h];
		c_r += gsl_matrix_get(m_gsl_colors, 0, i);
		c_g += gsl_matrix_get(m_gsl_colors, 1, i);
        c_b += gsl_matrix_get(m_gsl_colors, 2, i);
	}
	c_r /= m_user_marked_points.size();
	c_g /= m_user_marked_points.size();
	c_b /= m_user_marked_points.size();
	
    GeometricComponent* geom = new GeometricComponent();
    geom->m_type = POLY;
	
	geom->m_texture.w = 1;
	geom->m_texture.h = 1;
	geom->m_texture.bitmap = (int*)calloc(4, sizeof(int));
	geom->m_texture.bitmap[0] = c_r;
	geom->m_texture.bitmap[1] = c_g;
	geom->m_texture.bitmap[2] = c_b;
	geom->m_texture.bitmap[3] = 150;
	
	tex_tri_vertex vert;
	for (int k = 0; k < 4; k++)
	   vert.color[k] = geom->m_texture.bitmap[k];
	
	vert.pos[0] = roof_top1.x;
	vert.pos[1] = roof_top1.y;
	vert.pos[2] = roof_top1.z;
	vert.tex[0] = 0;
	vert.tex[0] = 0;
	geom->m_triangle_vertices.push_back(vert);
	
    vert.pos[0] = roof_top2.x;
	vert.pos[1] = roof_top2.y;
	vert.pos[2] = roof_top2.z;
	vert.tex[0] = 0;
	vert.tex[0] = 0;
	geom->m_triangle_vertices.push_back(vert);
	
    vert.pos[0] = roof_bottom2.x;
	vert.pos[1] = roof_bottom2.y;
	vert.pos[2] = roof_bottom2.z;
	vert.tex[0] = 0;
	vert.tex[0] = 0;
	geom->m_triangle_vertices.push_back(vert);
	
	
	
	vert.pos[0] = roof_top1.x;
	vert.pos[1] = roof_top1.y;
	vert.pos[2] = roof_top1.z;
	vert.tex[0] = 0;
	vert.tex[0] = 0;
	geom->m_triangle_vertices.push_back(vert);
	
    vert.pos[0] = roof_bottom1.x;
	vert.pos[1] = roof_bottom1.y;
	vert.pos[2] = roof_bottom1.z;
	vert.tex[0] = 0;
	vert.tex[0] = 0;
	geom->m_triangle_vertices.push_back(vert);
	
    vert.pos[0] = roof_bottom2.x;
	vert.pos[1] = roof_bottom2.y;
	vert.pos[2] = roof_bottom2.z;
	vert.tex[0] = 0;
	vert.tex[0] = 0;
	geom->m_triangle_vertices.push_back(vert);
	
	if (1) { // if reflect this roof plane
		ReflectRoof(geom, roof_top1, roof_top2, roof_bottom1, roof_bottom2);
	}
	
	geom->m_origin_points = m_marked_points;
	m_geometry.push_back(geom);
	
}

void PointCloud::ReflectRoof(GeometricComponent *geom, point roof_top1, point roof_top2, point roof_bottom1, point roof_bottom2){
	double roof_line_m = (roof_top1.z - roof_top2.z)/(roof_top1.x - roof_top2.x);
	double roof_line_b = roof_top1.z - roof_line_m * roof_top1.x; 
		
	point orig_roof_bottom1 = roof_bottom1;
	point orig_roof_bottom2 = roof_bottom2; 
	
	ReflectPoint(&(roof_bottom1.x), &(roof_bottom1.z), roof_line_m, roof_line_b);
	ReflectPoint(&(roof_bottom2.x), &(roof_bottom2.z), roof_line_m, roof_line_b);
	
	tex_tri_vertex vert;
	for (int k = 0; k < 4; k++)
	   vert.color[k] = geom->m_texture.bitmap[k];
	
	vert.pos[0] = roof_top1.x;
	vert.pos[1] = roof_top1.y;
	vert.pos[2] = roof_top1.z;
	vert.tex[0] = 0;
	vert.tex[0] = 0;
	geom->m_triangle_vertices.push_back(vert);
	
    vert.pos[0] = roof_top2.x;
	vert.pos[1] = roof_top2.y;
	vert.pos[2] = roof_top2.z;
	vert.tex[0] = 0;
	vert.tex[0] = 0;
	geom->m_triangle_vertices.push_back(vert);
	
    vert.pos[0] = roof_bottom2.x;
	vert.pos[1] = roof_bottom2.y;
	vert.pos[2] = roof_bottom2.z;
	vert.tex[0] = 0;
	vert.tex[0] = 0;
	geom->m_triangle_vertices.push_back(vert);
	
	
	
	vert.pos[0] = roof_top1.x;
	vert.pos[1] = roof_top1.y;
	vert.pos[2] = roof_top1.z;
	vert.tex[0] = 0;
	vert.tex[0] = 0;
	geom->m_triangle_vertices.push_back(vert);
	
    vert.pos[0] = roof_bottom1.x;
	vert.pos[1] = roof_bottom1.y;
	vert.pos[2] = roof_bottom1.z;
	vert.tex[0] = 0;
	vert.tex[0] = 0;
	geom->m_triangle_vertices.push_back(vert);
	
    vert.pos[0] = roof_bottom2.x;
	vert.pos[1] = roof_bottom2.y;
	vert.pos[2] = roof_bottom2.z;
	vert.tex[0] = 0;
	vert.tex[0] = 0;
	geom->m_triangle_vertices.push_back(vert);
	
	// end caps (resuing t1 and t2)
	
	vert.pos[0] = roof_top1.x;
	vert.pos[1] = roof_top1.y;
	vert.pos[2] = roof_top1.z;
	vert.tex[0] = 0;
	vert.tex[0] = 0;
	geom->m_triangle_vertices.push_back(vert);
	
    vert.pos[0] = roof_bottom1.x;
	vert.pos[1] = roof_bottom1.y;
	vert.pos[2] = roof_bottom1.z;
	vert.tex[0] = 0;
	vert.tex[0] = 0;
	geom->m_triangle_vertices.push_back(vert);
	
    vert.pos[0] = orig_roof_bottom1.x;
	vert.pos[1] = orig_roof_bottom1.y;
	vert.pos[2] = orig_roof_bottom1.z;
	vert.tex[0] = 0;
	vert.tex[0] = 0;
	geom->m_triangle_vertices.push_back(vert);
	


	vert.pos[0] = roof_top2.x;
	vert.pos[1] = roof_top2.y;
	vert.pos[2] = roof_top2.z;
	vert.tex[0] = 0;
	vert.tex[0] = 0;
	geom->m_triangle_vertices.push_back(vert);
	
    vert.pos[0] = roof_bottom2.x;
	vert.pos[1] = roof_bottom2.y;
	vert.pos[2] = roof_bottom2.z;
	vert.tex[0] = 0;
	vert.tex[0] = 0;
	geom->m_triangle_vertices.push_back(vert);
	
    vert.pos[0] = orig_roof_bottom2.x;
	vert.pos[1] = orig_roof_bottom2.y;
	vert.pos[2] = orig_roof_bottom2.z;
	vert.tex[0] = 0;
	vert.tex[0] = 0;
	geom->m_triangle_vertices.push_back(vert);	
}

void PointCloud::ReflectPoint(float *x, float *y, double line_m, double line_b){
	double d = (*x + (*y - line_b)*line_m) / (1 + line_m*line_m);
	*x = 2*d - *x;
	*y = 2*d*line_m - *y + 2*line_b;
}


void PointCloud::GhettoTriangulateMarkedPoints(){
    // looks at points marked as PT_MARK2
    // put things into m_triangle_points
    if (m_marked_points.size() == 0){
        return;
    }
    
    double mean_pos[3];
    int num_marked_points = 0;
    
    for (int h = 0; h < m_marked_points.size(); h++){
        int i = m_marked_points[h];
        for (int k = 0; k < 3; k++){
            mean_pos[k] += gsl_matrix_get(m_gsl_points, k, i);
        }            
        num_marked_points ++;
    }
    
    for (int k = 0; k < 3; k++){
        mean_pos[k] /= num_marked_points;
    }
    
    printf("[Ghetto marked points] mean computed\n");
    printf("sizes of things: m_marked_points.size()=%d and num_marked_points=%d\n", m_marked_points.size(), num_marked_points);
    
    
    gsl_matrix *A = gsl_matrix_calloc(num_marked_points, 3);
    gsl_vector *S = gsl_vector_calloc(3);
    gsl_matrix *V = gsl_matrix_calloc(3,3);
    gsl_vector *work = gsl_vector_calloc(3);
    
    ANNpointArray ann_points = annAllocPts(num_marked_points, 3);
    gsl_matrix *colors = gsl_matrix_calloc(num_marked_points, 3);
    
    for (int h = 0; h < num_marked_points; h++){
        int i = m_marked_points[h];
        for (int k = 0; k < 3; k++){
            gsl_matrix_set(A, h, k, gsl_matrix_get(m_gsl_points, k, i) - mean_pos[k]);
            ann_points[h][k] = gsl_matrix_get(m_gsl_points, k, i);
            gsl_matrix_set(colors, h, k, gsl_matrix_get(m_gsl_colors, k, i));
        }
    }
    
    printf("after making GSL stuff\n");
    
    ANNkd_tree* kd_3d = new ANNkd_tree(ann_points, num_marked_points, 3);
    
    // run SVD!!! singular value decomposition! 
    int res = gsl_linalg_SV_decomp(A, V, S, work);

    // my points are stored in A!!
    // find min/max x and y -- just so i can sample points from a bounding box thing 
    double min_x = DBL_MAX;
    double min_y = DBL_MAX;
    double max_x = -min_x;
    double max_y = -min_y;
    for (int i = 0; i < num_marked_points; i++){
        double x = gsl_matrix_get(A, i, 0);
        double y = gsl_matrix_get(A, i, 1);
        if (x < min_x)
            min_x = x;
        if (x > max_x)
            max_x = x;
        
        if (y < min_y)
            min_y = y;
        if (y > max_y)
            max_y = y;
    }
    
    printf("max and min: %f %f %f %f\n", min_x, max_x, min_y, max_y);
    float ratio = (max_x - min_x) / (max_y - min_y);
    int samp1 = 10; //sqrt(num_marked_points * ratio) / 2.0;
    int samp2 = 10; //sqrt(num_marked_points / ratio) / 2.0;
    printf("sample number: %d %d\n", samp1, samp2);
    gsl_matrix *svd_space = gsl_matrix_calloc(samp1*samp2, 3);
    gsl_matrix *real_space = gsl_matrix_calloc(samp1*samp2, 3);
    gsl_matrix *work_space = gsl_matrix_calloc(samp1*samp2, 3);
    gsl_matrix *new_colors = gsl_matrix_calloc(samp1*samp2, 4); 
    
    for (int i = 0; i < samp1; i++){
        for (int j = 0; j < samp2; j++){
            double x = min_x + (max_x - min_x)/samp1*i;
            double y = min_y + (max_y - min_y)/samp2*j;
            double z = 0; // set Z from height of near-by things 
            gsl_matrix_set(svd_space, i*samp2 + j, 0, x);
            gsl_matrix_set(svd_space, i*samp2 + j, 1, y);
            gsl_matrix_set(svd_space, i*samp2 + j, 2, z);
        }
    }
    printf("transforming from svd space back to real space\n");
    
    gsl_matrix *new_S = gsl_matrix_calloc(3,3);
    gsl_matrix_set(new_S, 0, 0, gsl_vector_get(S, 0));
    gsl_matrix_set(new_S, 1, 1, gsl_vector_get(S, 1));
    gsl_matrix_set(new_S, 2, 2, gsl_vector_get(S, 2));
    
    res = gsl_blas_dgemm (CblasNoTrans, CblasNoTrans,
                       1.0, svd_space, new_S,
                       0.0, work_space);
                          
    res = gsl_blas_dgemm (CblasNoTrans, CblasTrans,
                       1.0, work_space, V,
                       0.0, real_space);
                       
    // find better z's
    ANNpoint queryPt = annAllocPt(3);
    float radius = 0.000000051;
    int n = 20;
    ANNidxArray nnIdx = new ANNidx[n];
    ANNdistArray dists = new ANNdist[n];
    
    for (int i = 0; i < samp1*samp2; i++){
        for (int k = 0; k < 3; k++){
            queryPt[k] = gsl_matrix_get(real_space, i, k) + mean_pos[k];
        }
        
        kd_3d->annkSearch(queryPt, n, nnIdx, dists, 0);
        double r = 0;
        double b = 0;
        double g = 0;
        
        double x = 0;
        double y = 0;
        double z = 0;
        
        double tot_weight = 0;
        
        for (int j = 0; j < n; j++){
            double weight = 1/dists[j];
            r += weight*gsl_matrix_get(colors, nnIdx[j], 0);
            g += weight*gsl_matrix_get(colors, nnIdx[j], 1);
            b += weight*gsl_matrix_get(colors, nnIdx[j], 2);  
            
            x += weight*gsl_matrix_get(A, nnIdx[j], 0); 
            y += weight*gsl_matrix_get(A, nnIdx[j], 1); 
            z += weight*gsl_matrix_get(A, nnIdx[j], 2); 
            tot_weight += weight;
        }
        r /= tot_weight;
        g /= tot_weight;
        b /= tot_weight;
        
        x /= tot_weight;
        y /= tot_weight;
        z /= tot_weight;
        
        gsl_matrix_set(new_colors, i, 0, r);
        gsl_matrix_set(new_colors, i, 1, g);
        gsl_matrix_set(new_colors, i, 2, b);
        
        //gsl_matrix_set(svd_space, i, 0, x);
        //gsl_matrix_set(svd_space, i, 1, y);
        //gsl_matrix_set(svd_space, i, 2, z);
        
    }
    
    res = gsl_blas_dgemm (CblasNoTrans, CblasNoTrans,
                       1.0, svd_space, new_S,
                       0.0, work_space);
                          
    res = gsl_blas_dgemm (CblasNoTrans, CblasTrans,
                       1.0, work_space, V,
                       0.0, real_space);
                       
                       
    GeometricComponent* geom = new GeometricComponent();
    geom->m_type = POLY;
    geom->m_texture.w = samp1;
    geom->m_texture.h = samp2;
    geom->m_texture.bitmap = (int*)malloc(samp1*samp2*4*sizeof(int));
          
    for (int i = 0; i < samp1*samp2; i++){
        if (1 || gsl_matrix_get(new_colors, i, 3) > 0){
            tex_tri_vertex vert;
            for (int k = 0; k < 3; k++){
                vert.pos[k] = gsl_matrix_get(real_space, i, k) + mean_pos[k];
                vert.color[k] = gsl_matrix_get(new_colors, i, k)/255.0;
            }
            vert.color[3] = 0.5;
            
            int the_j = i%samp2;
            int the_i = (i - the_j)/samp2;
            //printf("ghetto geom, %f %f\n", the_i, the_j);
            vert.tex[0] = float(the_i)/samp1;
            vert.tex[1] = float(the_j)/samp2;
        
            geom->m_triangle_vertices.push_back(vert);
            
            for (int k = 0; k < 4; k++)
                geom->m_texture.bitmap[i*4 + k] = vert.color[k]*255;
        }
    }
    
    geom->BuildTriangles();
    
    geom->m_origin_points = m_marked_points;
    m_geometry.push_back(geom);

}

void PointCloud::MakeStairs(){
    printf("[PointCloud::MakeStairs] starting\n");
    
    // sorting marked points by height and get rid of top and bottom 5 percent
    gsl_vector *heights = gsl_vector_calloc(m_marked_points.size());    
    for (int h = 0; h < m_marked_points.size(); h++){
        int i = m_marked_points[h];
        gsl_vector_set(heights, h, gsl_matrix_get(m_gsl_points, 1, i));
    }
    
    gsl_permutation * p = gsl_permutation_alloc (m_marked_points.size());
    gsl_permutation_init (p);
    
    gsl_sort_vector_index(p, heights);
    
    double avg_pos[3];
    double avg_color[3];
    for (int k = 0; k < 3; k++){
        avg_pos[k] = 0;
        avg_color[k] = 0;   
    }
        
    //std::vector<int> new_marked_points;
    //for (int h = five_percent*1; h < m_marked_points.size() - five_percent; h++){
    for (int h = 0; h < m_marked_points.size(); h++) {
        int i = m_marked_points[gsl_permutation_get(p, h)];
        //new_marked_points.push_back(i);
        for (int k = 0; k < 3; k++){
            avg_pos[k] += gsl_matrix_get(m_gsl_points, k, i);
            avg_color[k] += gsl_matrix_get(m_gsl_colors, k, i);
        }
    }
    
    for (int k = 0; k < 3; k++){
        avg_pos[k] /= m_marked_points.size();
        avg_color[k] /= m_marked_points.size();
        avg_color[k] /= 255.0;
    }
        //avg_pos[k] /= new_marked_points.size();
    
    //m_marked_points = new_marked_points;
    
    printf("average point: %.20f %.20f %.20f\n", avg_pos[0], avg_pos[1], avg_pos[2]);
    
    plane pl = m_marked_point_plane;
    double parallel_stair_line = -1*pl.a/pl.c; 
    double perpendicular_stair_line = pl.c/pl.a;
    printf("marked point plane: %f %f %f %f\n", pl.a, pl.b, pl.c, pl.d);
    double a = pl.a;
    double b = pl.c;
    double c = -1 * (a * avg_pos[0] + b * avg_pos[2]);
    printf("a, b: %f %f\n", a, b);
    
    // distance along perpendicular line between min and max (heigh value) points
    double min_idx = m_marked_points[gsl_permutation_get( p, 0 )];
    double max_idx = m_marked_points[gsl_permutation_get( p, m_marked_points.size()-1 )];
    double dist1 = abs(a * gsl_matrix_get(m_gsl_points, 0, min_idx) + b * gsl_matrix_get(m_gsl_points, 2, min_idx) + c) / sqrt(a*a + b*b);
    double dist2 = abs(a * gsl_matrix_get(m_gsl_points, 0, max_idx) + b * gsl_matrix_get(m_gsl_points, 2, max_idx) + c) / sqrt(a*a + b*b);
    double leg1 = dist1 + dist2;
    double hypotenuse_sq = pow( gsl_matrix_get(m_gsl_points, 0, min_idx) - gsl_matrix_get(m_gsl_points, 0, max_idx) ,2 ) + pow( gsl_matrix_get(m_gsl_points, 2, min_idx) - gsl_matrix_get(m_gsl_points, 2, max_idx), 2);
    double leg2 = sqrt(hypotenuse_sq - pow(leg1,2)); 
    
    int samples = 200;
    //double x_step = 1.0*(gsl_matrix_get( m_gsl_points, 1,  m_marked_points[gsl_permutation_get( p, m_marked_points.size()-1 )] ) - gsl_matrix_get( m_gsl_points, 1,  m_marked_points[gsl_permutation_get( p, 0 )] ))/float(samples);
    double x_step = leg2/samples;
    printf("Stair x_step: %.20f\n", x_step);
    // per line values (sweeping the line up the stairs from the bottom point)   
    int num_samples[samples];
    double avg_heights[samples];
    double x_s[samples];
    double z_s[samples];
    double avg_x[samples];
    double max_x[samples];
    double min_x[samples];
    double max_y[samples];
    double min_y[samples];
    double avg_dist_from_center[samples];
    
    double start_x = gsl_matrix_get( m_gsl_points, 0, m_marked_points[gsl_permutation_get( p, 0 )] );
    double start_z = gsl_matrix_get( m_gsl_points, 2, m_marked_points[gsl_permutation_get( p, 0 )] );
    
    GeometricComponent* geom = new GeometricComponent();
    geom->m_type = POLY;
    geom->m_texture.w = 0;
    geom->m_texture.h = 2;
    
    for (int g = 0; g < samples; g++){
        // init the sample values for this line
        num_samples[g] = 0;
        avg_heights[g] = 0;
        avg_x[g] = 0;
        max_x[g] = -1 * DBL_MAX;
        min_x[g] = DBL_MAX;
        max_y[g] = -1 * DBL_MAX;
        min_y[g] = DBL_MAX;
        avg_dist_from_center[g] = 0;
        
        // set up hess stuff to find distance to line that is parallel going thru some x,y
        double d = -1 * (a * start_x + b * start_z);
        double denom = 1 / sqrt(a*a + b*b);
        double hess[2] = {a * denom, b * denom};
        double p = d * denom;
        
        // loop thru all points and measure distance 
        double dist;
        for (int h = 0; h < m_marked_points.size(); h++){
            int i = m_marked_points[h];
            dist = 0;
            dist += (hess[0] * gsl_matrix_get(m_gsl_points, 0, i));
            dist += (hess[1] * gsl_matrix_get(m_gsl_points, 2, i));
            dist += p;
            
            if (abs(dist) < x_step){
                num_samples[g]++;
                avg_heights[g] += gsl_matrix_get(m_gsl_points, 1, i);
                
                double x = gsl_matrix_get(m_gsl_points, 0, i);
                double y = gsl_matrix_get(m_gsl_points, 1, i);
                if (x > max_x[g])
                    max_x[g] = x;
                if (x < min_x[g])
                    min_x[g] = x;
                    
                if (y > max_y[g])
                    max_y[g] = y;
                if (y < min_y[g])
                    min_y[g] = y;
            } 
        }
        
        
        
        avg_heights[g] /= num_samples[g];
        //printf("line scan %d, found %d samples and avg height DIFF of \t%.20f\n", g, num_samples[g], max_y[g]-avg_heights[g]);
        
        x_s[g] = start_x;
        z_s[g] = start_z;
        
        start_x += x_step;
        start_z += (x_step * b/a);
    }
    
    std::vector<int> stair_samples;
    stair_samples.push_back(0);
    
    int tot_diff = 0;
    int avg_diff = 0;
    float tot_height_diff = 0;
    float avg_height_diff = 0;
    int max_sample = 0;
    for (int g = 0; g < samples; g++){
        if (num_samples[g] < 5)
            break;
        
        max_sample = g;
        
        if ( (g > 2) && (num_samples[g] < num_samples[g-1]) && (num_samples[g-1] > num_samples[g-2]) ){
            stair_samples.push_back(g);   

            if (stair_samples.size() > 1){
                int prev_g = stair_samples[stair_samples.size()-2];
                tot_diff += (g-prev_g);
                avg_diff = floor( float(tot_diff)/(stair_samples.size()-1) + 0.5);
                //printf("(g=%d ... g- prev_g: %d, avg: %d\n", g, g-prev_g, avg_diff);
                
                tot_height_diff += (avg_heights[g] - avg_heights[prev_g]);
                avg_height_diff = tot_height_diff / (stair_samples.size()-1);
            }
        }
    }    
    for (int g = 0; g < max_sample;  g += avg_diff){    
        
        double d = -1 * (a * x_s[g] + b * z_s[g]);
        tex_tri_vertex vert;
        for (int k = 0; k < 3; k++)
            vert.color[k] = avg_color[k];
        vert.color[3] = .5;
        
        vert.pos[0] = min_x[g];
        vert.pos[1] = min_y[0] + avg_height_diff * (g/avg_diff); //avg_heights[g];
        vert.pos[2] = -1 * (a * vert.pos[0] + d) / b;
        geom->m_triangle_vertices.push_back(vert);
        
        vert.pos[0] = max_x[g];
        vert.pos[1] = min_y[0] + avg_height_diff * (g/avg_diff); //avg_heights[g];
        vert.pos[2] = -1 * (a * vert.pos[0] + d) / b;
        geom->m_triangle_vertices.push_back(vert);
        
        vert.pos[0] = min_x[g];
        vert.pos[1] = min_y[0] + avg_height_diff * (g/avg_diff + 1);//avg_heights[g + avg_diff];
        vert.pos[2] = -1 * (a * vert.pos[0] + d) / b;
        geom->m_triangle_vertices.push_back(vert);
        
        vert.pos[0] = max_x[g];
        vert.pos[1] = min_y[0] + avg_height_diff * (g/avg_diff + 1);//avg_heights[g + avg_diff];
        vert.pos[2] = -1 * (a * vert.pos[0] + d) / b;
        geom->m_triangle_vertices.push_back(vert);
        
        geom->m_texture.w += 2;
    }
    
    
    geom->m_origin_points = m_marked_points;
    geom->BuildTriangles();
    m_geometry.push_back(geom);

}

/*
void PointCloud::FitCylinderToMarkedPoints(){
    double mean[3];
    double radius;
    double orientation;
    int num_marked_points = find2DMeanVarianceOfMarkedPoints(mean, &radius, &orientation);    
    
    double min[3];
    double max[3];
    FindMinMaxOfMarkedPoints(min, max);
    
    radius *= 2;
    printf("%f %f %f, %f\n", mean[0], mean[1], radius, orientation);
    
    ANNkd_tree *kd_3d;
    ANNpointArray ann_points;
    gsl_matrix *colors;
    
    ann_points = annAllocPts(num_marked_points, 3);
    colors = gsl_matrix_calloc(num_marked_points, 3);
    
    int j = 0;
    for (int i = 0; i < m_num_points; i++){
        if (m_draw[i] == PT_MARK2){
            for (int k = 0; k < 3; k++){
                ann_points[j][k] = gsl_matrix_get(m_gsl_points, k, i);
                gsl_matrix_set(colors, j, k, gsl_matrix_get(m_gsl_colors, k, i));
            }
            j++;
        }
    }
    
    kd_3d = new ANNkd_tree(ann_points, num_marked_points, 3);
    
    //MakeKdTreeOfMarkedPoints(kd_3d, ann_points, colors, num_marked_points);
    
    float ratio = 2*PI*radius/(max[1] - min[1]);
    int samp1 = sqrt(num_marked_points * ratio) / 2;
    int samp2 = sqrt(num_marked_points / ratio);
    
    printf("number of marked points: %d... sample %d, %d\n", num_marked_points, samp1, samp2);
    
    ANNpoint queryPt = annAllocPt(3);
    int n = 20;
    ANNidxArray nnIdx = new ANNidx[n];
    ANNdistArray dists = new ANNdist[n];
    
    for (int i = 0; i < samp1; i++){
        for (int j = 0; j < samp2; j++){
            int idx = i*samp2 + j;
            
            double theta = orientation + PI/2 + PI/samp1*i;
            double y = min[1] + (max[1] - min[1])/samp2*j;
            
            double x = cos(theta) * radius + mean[0];
            double z = sin(theta) * radius + mean[1];
	
            
            queryPt[0] = x;
            queryPt[1] = y;
            queryPt[2] = z;
            
            kd_3d->annkSearch(queryPt, n, nnIdx, dists, 0);
            
            point p;
            p.r = p.g = p.b = 0;
            p.x = p.y = p.z = 0;
            double tot_weight = 0;
            
            double r, g, b;
            r = g = b = 0;
            
            for (int j = 0; j < n; j++){
                double weight = 1/dists[j];
                r += weight*gsl_matrix_get(colors, nnIdx[j], 0);
                g += weight*gsl_matrix_get(colors, nnIdx[j], 1);
                b += weight*gsl_matrix_get(colors, nnIdx[j], 2);  
                
                p.x += weight*ann_points[nnIdx[j]][0]; 
                p.y += weight*ann_points[nnIdx[j]][1];
                p.z += weight*ann_points[nnIdx[j]][2]; 
                tot_weight += weight;
            }
            r /= tot_weight;
            g /= tot_weight;
            b /= tot_weight;
            p.r = r;
            p.g = g;
            p.b = b;
            
            p.x /= tot_weight;
            p.y /= tot_weight;
            p.z /= tot_weight;
            
            m_triangle_points.push_back(p);
        }
    }

    m_triangle_x = samp1;
    m_triangle_y = samp2;
}

int PointCloud::find2DMeanVarianceOfMarkedPoints(double* mean_pos, double* variance, double* orientation){
    mean_pos[0] = mean_pos[1] = 0;
    *variance = 0;
    *orientation = 0;

    // count number of marked points and find mean
    int num_marked_points = 0;
    for (int i = 0; i < m_num_points; i++){
        if (m_draw[i] == PT_MARK2){
            mean_pos[0] += gsl_matrix_get(m_gsl_points, 0, i);
            mean_pos[1] += gsl_matrix_get(m_gsl_points, 2, i);
            num_marked_points ++;
        }
    }
    
    mean_pos[0] /= num_marked_points;
    mean_pos[1] /= num_marked_points;
    
    // compute variance based on mean
    double mean_dist;
    double mean_orientation[2];

    for (int i = 0; i < m_num_points; i++){
        if (m_draw[i] == PT_MARK2){
            double dist = 0;
            
            double x = (mean_pos[0] - gsl_matrix_get(m_gsl_points, 0, i));
            double z = (mean_pos[1] - gsl_matrix_get(m_gsl_points, 2, i));
            
            dist += pow(x, 2);
            dist += pow(z, 2);
            
            
            mean_orientation[0] -= x;
            mean_orientation[1] -= z;
                        
            mean_dist += dist;
            num_marked_points ++;
        }
    }
    
    mean_orientation[0] /= mean_dist;
    mean_orientation[1] /= mean_dist;
    
    *orientation = atan2(mean_orientation[1], mean_orientation[0]);
    
    // variance is mean squared distance between each point and mean... take square root to get ~radius
    mean_dist /= num_marked_points;
    *variance = sqrt(mean_dist);

    // move mean a bit?
    mean_pos[0] += cos(*orientation) * *variance;
    mean_pos[1] += sin(*orientation) * *variance;    

    return num_marked_points;
}

void PointCloud::FindMinMaxOfMarkedPoints(double* min, double* max){
    for (int k = 0; k < 3; k++){
        min[k] = DBL_MAX;
        max[k] = -1*DBL_MAX;
    }
    
    for (int i = 0; i < m_num_points; i++){
        if (m_draw[i] == PT_MARK2){
            for (int k = 0; k < 3; k++){
                double m = gsl_matrix_get(m_gsl_points, k, i);
                if (m < min[k])
                    min[k] = m;
                if (m > max[k])
                    max[k] = m;
            }
        }
    }
    
    for (int k = 0; k < 3; k++){
        printf("min %f max %f (%d)\n", min[k], max[k], k);
    }
}

void PointCloud::MakeKdTreeOfMarkedPoints(ANNkd_tree* kd_3d, ANNpointArray ann_points, gsl_matrix * colors, int num_marked_points){
    ann_points = annAllocPts(num_marked_points, 3);
    colors = gsl_matrix_calloc(num_marked_points, 3);
    
    int j = 0;
    for (int i = 0; i < m_num_points; i++){
        if (m_draw[i] == PT_MARK2){
            for (int k = 0; k < 3; k++){
                ann_points[j][k] = gsl_matrix_get(m_gsl_points, k, i);
                gsl_matrix_set(colors, j, k, gsl_matrix_get(m_gsl_colors, k, i));
            }
            j++;
        }
    }
    
    kd_3d = new ANNkd_tree(ann_points, num_marked_points, 3);
}

 */

