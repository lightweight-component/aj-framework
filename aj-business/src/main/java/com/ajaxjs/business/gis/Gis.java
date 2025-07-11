package com.ajaxjs.business.gis;

public class Gis {
    /**
     * 判断某个坐标是否在一组坐标构建的多边形范围内.
     * 根据射线法的原理，如果点在多边形内，射线会和多边形交点为奇数次，如果点在多边形外，射线会和多边形交点为偶数次（或 0次）。所以通过这种方式可以准确地判断点是否在多边形内。
     * <a href="https://blog.csdn.net/qq_35222232/article/details/137258098">...</a>
     *
     * @param point
     * @param polygon
     * @return 判断某个坐标是否在一组坐标构建的多边形范围内
     */
    public boolean isPointInsidePolygon(Gps point, Gps[] polygon) {
        int polygonLength = polygon.length;
        boolean isInside = false;

        for (int i = 0, j = polygonLength - 1; i < polygonLength; j = i++) {
            if ((polygon[i].getLat() > point.getLat()) != (polygon[j].getLat() > point.getLat()) &&
                    point.getLon() < (polygon[j].getLon() - polygon[i].getLon()) * (point.getLat() - polygon[i].getLat()) / (polygon[j].getLat() - polygon[i].getLat()) + polygon[i].getLon()) {
                isInside = !isInside;
            }
        }

        return isInside;
    }

}
