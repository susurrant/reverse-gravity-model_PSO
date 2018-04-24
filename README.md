# ReverseGravityModel_PSO
Implemention of reverse gravity model estimation using particle swarm optimization method

Language: Java (with multithreading) and Python<br>

Reference: [http://www.tandfonline.com/doi/abs/10.1080/00330124.2012.679445](http://www.tandfonline.com/doi/abs/10.1080/00330124.2012.679445)

## 1. 程序入口：
GravityFit类main函数，构建一个GravityFit对象：</br>

	GravityFit gf = new GravityFit("W:\\Java\\GravityFit_PSO\\src\\flows.txt", "W:\\Java\\GravityFit_PSO\\src\\points.txt", 3, 1);
	
参数分别表示：流文件数据，点坐标数据，数据编码格式，线程数（需能被30整除），所用模型（0或1）</br>


## 2. 注意事项：
### 2.1 数据格式（txt文档，UTF-8无BOM格式）
- 流数据例如：</br>
    **上海 苏州 69059**</br>
一行代表一条流数据，字符串之间、字符串和数字之间都是一个空格</br>
       
- 点数据例如：</br>
    **上海 121.473704 31.230393**</br>
同样一行代表一个点数据ID及其坐标，中间都是一个空格</br>

### 2.2 模型选择
只能为0或1:</br>
- 0：表示（1）提供流数据是完整的，任意两点间均存在流（2）如果两个点间的流没有或缺失，不考虑缺失流对计算的影响，忽视缺失的流</br>
- 1：表示考虑缺失的流数据对计算的影响，如果存在缺失的流数据，并把缺失的流的值设为0</br>
