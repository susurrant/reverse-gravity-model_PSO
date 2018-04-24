#!/usr/bin/env python2.7
# -*- coding: utf-8 -*-

' 给定两点经纬度，计算距离（km） '

__author__ = 'Xin Yao'

import math

EARTH_RADIUS = 6371.0 #地球半径平均值，单位：km

def haverSine(theta):
    v = math.sin(theta / 2)
    return v * v

'''
给定的经度1，纬度1；经度2，纬度2. 计算2个经纬度之间的距离。
lat1 经度1
lon1 纬度1
lat2 经度2
lon2 纬度2
return 距离（公里、千米）
'''
def haverSineDistance(lon1, lat1, lon2, lat2):
    #用haverSinee公式计算球面两点间的距离。
    #经纬度转换成弧度
    lat1 = convertDegreesToRadians(lat1)
    lon1 = convertDegreesToRadians(lon1)
    lat2 = convertDegreesToRadians(lat2)
    lon2 = convertDegreesToRadians(lon2)
    #差值
    vLon = abs(lon1 - lon2)
    vLat = abs(lat1 - lat2)
    #h is the great circle distance in radians, great circle就是一个球体上的切面，它的圆心即是球心的一个周长最大的圆
    h = haverSine(vLat) + math.cos(lat1) * math.cos(lat2) * haverSine(vLon)
    distance = 2 * EARTH_RADIUS * math.asin(math.sqrt(h))
    return distance

'''
将角度换算为弧度
degrees 角度
return 弧度
'''
def convertDegreesToRadians(degrees):
    return degrees * math.pi / 180

#print haverSineDistance(121.473704, 31.230393, 120.585316, 31.298886)
