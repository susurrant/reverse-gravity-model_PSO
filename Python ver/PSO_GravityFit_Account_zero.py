# -*- coding: utf-8 -*-

import string
import random
import copy
import math
import sys
import haverSineDistance

#预处理数据函数
def preprocessData(points, flows):
    PointName = []

    for rcd in flows:
        if rcd[0] not in PointName:
            PointName.append(rcd[0])
        if rcd[1] not in PointName:
            PointName.append(rcd[1])
            
        
    PointNum = len(PointName)
        
    #build a matrix for interactions
    #RT part: interaction strength
    #LB part: distance in km

    #计算所有城市任意两个城市之间的距离   
    InterData = [[0.0 for a in xrange(PointNum)] for b in xrange(PointNum)]
    lon1 = -1
    lat1 = -1
    lon2 = -1
    lat2 = -1
    for p in points:
            if(p[0] == rcd[0]):
                lon1 = p[1]
                lat1 = p[2]
                break
    for p in points:
            if(p[0] == rcd[1]):
                lon2 = p[1]
                lat2 = p[2]
                break
    c = 0
    for i in range(1, PointNum):
        for j in range(0, i):
            for p in points:
                if(p[0] == PointName[i]):
                    lon1 = p[1]
                    lat1 = p[2]
                    c += 1
                    if(c == 2):
                        c = 0
                        break
                elif(p[0] == PointName[j]):
                    lon2 = p[1]
                    lat2 = p[2]
                    c += 1
                    if(c == 2):
                        c = 0
                        break
            InterData[i][j] = haverSineDistance.haverSineDistance(lon1, lat1, lon2, lat2)

    for rcd in flows:
        n1 = PointName.index(rcd[0])   
        n2 = PointName.index(rcd[1])
        if(n1 > n2):
            t = n1
            n1 = n2
            n2 = t
        InterData[n1][n2] = string.atof(rcd[2])
    ValidPair = PointNum*(PointNum-1)/2
    #for i in InterData:
        #print i
    for idd in InterData:
        print idd
    return InterData,PointNum,ValidPair,PointName

def CreateFlows(CitySize, PointNum, beta, InterData):
    for i in range(0,PointNum):
        for j in range(0,i):
            #if(InterData[i][j]>0):  #valid pair
            InterData[j][i] = CitySize[i]*CitySize[j]/pow(InterData[i][j],beta)
            #else:
            #    InterData[j][i] = -1

def ExtractFlowData(InterData, ValidPair, PointNum):
    Data = [0.0]*ValidPair
    Count = 0
    for i in range(0,PointNum):
        for j in range(i+1,PointNum):
            #if(InterData[i][j]>0.0):
            Data[Count] = InterData[i][j]
            Count += 1
    
    return Data


def PearsonCoefficient1D(data1,data2,size):
    #print size
    mean1 = 0.0
    mean2 = 0.0
    i = 0
        
    while i<size:
        mean1 += data1[i]
        mean2 += data2[i]
        i += 1

    mean1 /= size
    mean2 /= size

    cov1 = 0.0
    cov2 = 0.0
    cov12 = 0.0
    
    i = 0
    while i<size:
        cov12 += (data1[i]-mean1)*(data2[i]-mean2)
        cov1 += (data1[i]-mean1)*(data1[i]-mean1)
        cov2 += (data2[i]-mean2)*(data2[i]-mean2)
        i += 1
    if(abs(cov1)<0.00000001 or abs(cov2)<0.00000001): return 0 
    return cov12/math.sqrt(cov1)/math.sqrt(cov2)
    

def PSOSearch(InterData,PointNum,ValidPair,InitialSizes,beta,ParticleNum,SearchRange,w,c1,c2):
    Particles = [[0.0 for a in xrange(PointNum)] for b in xrange(ParticleNum)]

    for i in range(0, ParticleNum):
        for j in range(0,PointNum):
            if i==0:
                Particles[i][j] = InitialSizes[j]
            else:
                Particles[i][j] = InitialSizes[j]+ (random.random()*SearchRange/5-SearchRange/10)
            if(Particles[i][j]>SearchRange): Particles[i][j]= SearchRange
            if(Particles[i][j]<0): Particles[i][j]= 0
    
    #print Particles 
    Velocity = [[random.random()*SearchRange-SearchRange/2 for a in xrange(PointNum)] for b in xrange(ParticleNum)]
    #print Velocity
    
    
    gBestParticleScore = 0.0
    gBestParticle = [0.0]*PointNum

    pBestParticleScore = [0.0]*ParticleNum
    pBestParticle = [[0.0 for a in xrange(PointNum)] for b in xrange(ParticleNum)]

    RealFlowData = ExtractFlowData(InterData,ValidPair,PointNum)
    
    InterDataTemp = copy.deepcopy(InterData)
    IterCount = 0
    while 1:
        tBestScore = 0
        for i in range(0,ParticleNum):
            CreateFlows(Particles[i], PointNum, beta, InterDataTemp)
            FitData = ExtractFlowData(InterDataTemp,ValidPair,PointNum)

            gof = PearsonCoefficient1D(FitData,RealFlowData,ValidPair)
            if(gof > tBestScore):
                tBestScore = gof
            
            #print gof
            if( gof > pBestParticleScore[i] ):
                pBestParticleScore[i] = gof
                for j in range(0,PointNum):
                    pBestParticle[i][j] = Particles[i][j]
                
            if( gof > gBestParticleScore ):
                gBestParticleScore = gof
                for j in range(0,PointNum):
                    gBestParticle[j] = Particles[i][j]

        #update particles
        maxVelocity = 0
        for i in range(0,ParticleNum):
            nc1 = c1 *random.random()
            nc2 = c2 *random.random()
            for j in range(0,PointNum):
                newVelocity = Velocity[i][j]*w + nc1*(pBestParticle[i][j]-Particles[i][j]) + nc2 *(gBestParticle[j]-Particles[i][j])
                #print i,j,c1,c2,Velocity[i][j],newVelocity
                
                if( newVelocity + Particles[i][j] > SearchRange ):
                    newVelocity = SearchRange - Particles[i][j]
                if(  newVelocity + Particles[i][j] < 0 ):
                    newVelocity = - Particles[i][j]

                if abs(newVelocity) > maxVelocity: maxVelocity = newVelocity
                    
                Velocity[i][j] = newVelocity
                Particles[i][j] += newVelocity
        #print gBestParticleScore, tBestScore, maxVelocity
        #print 
        IterCount += 1
        if IterCount >= 200:
            break

    return gBestParticleScore, gBestParticle

def InitSize(InterData,PointNum):
    Size = [0.0]*PointNum
    for i in range(0,PointNum):
        for j in range(i+1,PointNum):
            if(InterData[i][j]>0.0):
                Size[i] += InterData[i][j]
                Size[j] += InterData[i][j]
    return Size

#主调函数
def gravityFit(points, flows):
    InterData,PointNum,ValidPair,PointName = preprocessData(points, flows)
    Sizes = InitSize(InterData,PointNum)
    maxSize = max(Sizes)
    for i in range(0, PointNum):
        Sizes[i] = Sizes[i] / maxSize * 1000

    bestScoreResult = 0
    estSizeResult = []
    bestBeta = 0.1
    for beta in range(1, 30, 1):
        bs, estSize = PSOSearch(InterData, PointNum, ValidPair, Sizes, float(beta/10.0), 1000, 1000, 1, 2.0, 2.0)
        print bs
        if bs > bestScoreResult:
            bestScoreResult = bs
            bestBeta = float(beta/10.0)
            estSizeResult = []
            estSizeResult = copy.deepcopy(estSize)
            

    result = []
    result.append(['beta', bestBeta])
    for i in range(0, PointNum):
        result.append([PointName[i], estSizeResult[i]])
    return result

points = []
k = 0
points_file = open("./points.txt", "r")   #文件格式编码为UTF-8无BOM格式（推荐），或者UTF-8格式
                                        #但是UTF-8无BOM运行效率高，UTF-8格式运行效率很低
for line in points_file.readlines():
    id, x, y = line.split()
    points.append([id, float(x), float(y)])    # 添加 (id, x, y)
    k = k + 1   
points_file.close()

flows = []
flows_file = open("./flows.txt", "r")     #文件格式编码为UTF-8无BOM格式
for line in flows_file.readlines():
    id1, id2, val = line.split()
    flows.append([id1, id2, float(val)])        # 添加 (id1, id2, value)
flows_file.close()

res = gravityFit(points, flows)
for r in res:
    print('%s   %f' % (r[0], r[1])) 
