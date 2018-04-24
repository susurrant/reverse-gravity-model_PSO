import java.util.ArrayList;
import java.util.concurrent.*; 
import java.io.FileWriter; 
import java.io.IOException; 


public class GravityFit {
	public double[][] InterData;
	public int PointNum;
	public int ValidPair;
	public ArrayList<String> PointName;
	public double[] Sizes;
	public result fr;
	public int model;
	
	public int threadNum;  //线程数
	
	public double EARTH_RADIUS = 6371.0;
	
	public double convertDegreesToRadians(double degree){
		return degree * Math.PI / 180.0;
	}
	
	public double haverSine(double theta){
	    double v = Math.sin(theta / 2);
	    return v * v;
	}
	
	public double haverSineDistance(double lon1, double lat1, double lon2, double lat2){
		lat1 = convertDegreesToRadians(lat1);
		lon1 = convertDegreesToRadians(lon1);
		lat2 = convertDegreesToRadians(lat2);
		lon2 = convertDegreesToRadians(lon2);
		
		double vLon = Math.abs(lon1 - lon2);
	    double vLat = Math.abs(lat1 - lat2);
	    double h = haverSine(vLat) + Math.cos(lat1) * Math.cos(lat2) * haverSine(vLon);
	    double distance = 2 * EARTH_RADIUS * Math.asin(Math.sqrt(h));
		
	    return distance;
	}
	
	public double ManhattanDistance(double x1, double y1, double x2, double y2){
		return Math.abs(x1-x2)+Math.abs(y1-y2);
	}
	
	public double[] InitSize(){
		double[] sizes = new double[PointNum];
		for(int i=0; i<PointNum; i++)
	        for(int j=i+1; j<PointNum; j++){
	        	if(InterData[i][j] > 0.0){
	        		sizes[i] += InterData[i][j];
	                sizes[j] += InterData[i][j];
	        	}
	        }
		return sizes;
	}
	
	public void calcBestBeta() throws InterruptedException, ExecutionException{
		int betaChunk = 30 / threadNum;
        ArrayList<Callable<result>> threads = new ArrayList<Callable<result>>();
        ArrayList<Future<result>> futures = new ArrayList<Future<result>>();
        CountDownLatch countDownLatch = new CountDownLatch(threadNum);
        ExecutorService pool = Executors. newFixedThreadPool(threadNum);
        for(int i = 0; i < threadNum; i ++)
        {
        	threads.add(new PSOSearch(i, countDownLatch, model, InterData, PointNum, ValidPair,
        			Sizes, i*betaChunk+1, (i+1)*betaChunk, 1000, 1000, 1, 2.0, 2.0));
        }
        for(int i = 0; i < threads.size(); i++)
        {
        	futures.add( pool.submit(threads.get(i)));
        }
        countDownLatch.await();
        
        //处理线程返回结果
        for(int i = 0; i < futures .size(); i++)
        {
        	if(futures.get(i).get().gBestParticleScore > fr.gBestParticleScore){
        		fr = futures.get(i).get();
        	} 
        }
        pool.shutdown();
	}
	
	public GravityFit(String fF, String pF, String encoding, int tNum, int m) throws InterruptedException, ExecutionException{
		threadNum = tNum;
		model = m; 
		InteractionData od = new InteractionData(fF, pF, encoding);
		PointName = new ArrayList<String>();
		for(int i=0; i<od.FID.size(); i++){
			if(!PointName.contains(od.FID.get(i)[0])){
				PointName.add(od.FID.get(i)[0]);
			}
			if(!PointName.contains(od.FID.get(i)[1])){
				PointName.add(od.FID.get(i)[1]);
			}
		}
		PointNum = PointName.size();
        InterData = new double[PointNum][PointNum];
        
        double lon1 = -1;
        double lat1 = -1;
        double lon2 = -1;
        double lat2 = -1;       
        
        ////////////////////////////////////////////////////////
        //不同模型下的数据初始化
        if(model == 0){
        	for(int i=0; i<PointNum; i++)
	        	for(int j=0; j<PointNum; j++){
	        		InterData[i][j] = -1.0;
	        	}
        	
	        for(int i=0; i<od.FID.size(); i++){
	        	int n1 = PointName.indexOf(od.FID.get(i)[0]);
	        	for(int j=0; j<od.PID.size(); j++){
	        		if(od.PID.get(j).equals(od.FID.get(i)[0])){
	        			lon1 = od.PCO.get(j)[0];
	        	        lat1 = od.PCO.get(j)[1];
	        	        break;
	        		}
	        	}
	        	int n2 = PointName.indexOf(od.FID.get(i)[1]);
	        	for(int j=0; j<od.PID.size(); j++){
	        		if(od.PID.get(j).equals(od.FID.get(i)[1])){
	        			lon2 = od.PCO.get(j)[0];
	        	        lat2 = od.PCO.get(j)[1];
	        	        break;
	        		}
	        	}
	        	
	        	if(n1 > n2){
	                int t = n1;
	                n1 = n2;
	                n2 = t;
	        	}
	        	InterData[n1][n2] = od.FV.get(i);
	        	double dis = ManhattanDistance(lon1, lat1, lon2, lat2);
	        	InterData[n2][n1] = dis; 
	        }
	        ValidPair = od.FID.size();
        }
        else if(model == 1){
        	for(int i=0; i<PointNum; i++)
	        	for(int j=0; j<PointNum; j++){
	        		InterData[i][j] = 0.0;
	        	}
        	int c = 0;
        	for(int i=1; i<PointNum; i++){
        		for(int j=0; j<i; j++){
        			for(int k=0; k<od.PID.size(); k++){
        				if(od.PID.get(k).equals(PointName.get(i))){
        					lon1 = od.PCO.get(k)[0];
        		            lat1 = od.PCO.get(k)[1];
        		            c += 1;
        		            if(c == 2){
        		            	c = 0;
        		            	break;
        		            }           
        				}
        				else if(od.PID.get(k).equals(PointName.get(j))){
        					lon2 = od.PCO.get(k)[0];
        		            lat2 = od.PCO.get(k)[1];
        		            c += 1;
        		            if(c == 2){
        		            	c = 0;
        		            	break;
        		            }           
        				}
        			}
        			double dis = ManhattanDistance(lon1, lat1, lon2, lat2);
    	        	InterData[i][j] = dis;
        		}
        	}
        	
        	for(int i=0; i<od.FID.size(); i++){
        		int n1 = PointName.indexOf(od.FID.get(i)[0]);
        		int n2 = PointName.indexOf(od.FID.get(i)[1]);
        		if(n1 > n2){
	                int t = n1;
	                n1 = n2;
	                n2 = t;
	        	}
        		InterData[n1][n2] = od.FV.get(i);
        	}
        	
        	ValidPair = PointNum*(PointNum-1)/2;
        }
        else{
        	System.out.println("Wrong model!");
        	return;
        }
        
        ////////////////////////////////////////////////////////////////////////
        /**
        for(int i=0; i<PointNum; i++){
        	for(int j=0; j<PointNum; j++){
        		System.out.print(InterData[i][j]+" ");
        	}
        	System.out.println(" ");
        }
        **/
        
        Sizes = InitSize();
        
        double maxSize = 1;
        for(int i=0; i<Sizes.length; i++){
        	if(Sizes[i] > maxSize){
        		maxSize = Sizes[i];
        	}
        }
        for(int i=0; i<Sizes.length; i++){
        	Sizes[i] = Sizes[i] / maxSize * 1000.0;
        }
        fr = new result(PointNum, -1);
        calcBestBeta();
	}
	
	//主调函数
	public static void main(String[] args) throws InterruptedException, ExecutionException, IOException {
		long begin_time = System.currentTimeMillis();
		//参数：流文件数据，点坐标数据，数据编码格式，线程数（需能被30整除），所用模型（0或1）
		GravityFit gf = new GravityFit("W:\\Java\\GravityFit_PSO\\src\\flows.txt", "W:\\Java\\GravityFit_PSO\\src\\points.txt", "UTF-8", 6, 0);
		long end_time = System.currentTimeMillis();
 		System.out.println("Time cost: " + (end_time - begin_time) + " milliseconds.\n"); 
 		System.out.println("beta: " + gf.fr.beta);
 		
 		String LINE_SEPARATOR = System.getProperty("line.separator");
 		FileWriter fw = new FileWriter("R:\\taxi b"+gf.fr.beta+".txt" ,false); 
 		//输出数据
 		for(int i=0; i<gf.PointName.size(); i++){
 			fw.write(gf.PointName.get(i) + "," + gf.fr.gBestParticle[i] + LINE_SEPARATOR);
 		}
 		fw.close();
	}

}
