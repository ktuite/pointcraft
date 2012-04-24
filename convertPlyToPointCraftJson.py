#!/usr/bin/python

import sys
import ply

if len(sys.argv) < 3:
    print "usage: convertPlyToPointCraftJson.py input.ply output.json"
    sys.exit(0);
    
outfile = open(sys.argv[2], "w")


p = ply.Ply()
p.load(sys.argv[1])

print p.polygons[0]
print p.points[p.polygons[0][0]]


def writePolygonLine(poly):
    poly = poly + [poly[0]] # append first one to end to make cycle
    
    vertex_list = str(poly)
    
    vertex_pellets = []
    for id in poly:
        position = map( lambda x: float(x), p.points[id])
        
        #print "POSITOn: ", position 
        #position = "[" + ", ".join(map(lambda x : "%.20f" % x, position)) + "]"
        
        pellet = {"type":"pellet", "pellet_type":"POLYGON", "world_index":id, "pos":position }
        #{"type":"pellet","pellet_type":"POLYGON","pos":[-0.7974793314933777,0.02650982141494751,2.6377696990966797],"world_index":6,"radius":0.008279986679553986, }
        
        vertex_pellets.append(pellet)
    
    vertex_pellets = str(vertex_pellets)
    
    line = '{"type":"primitive","is_polygon":true,"vertices":%s,"vertex_objs":%s,"texture_url":[null],"local_textures":[""]}' % (vertex_list, vertex_pellets)
    line = line.replace('\'','"')
    outfile.write("%s\n" % line)



outfile.write("{\"version\":4}\n")
for poly in p.polygons:
    writePolygonLine(poly)


outfile.close()
