import java.util.concurrent.*;

public class PSOSearch implements Callable<result>{
	public result finalR;
	public int threadNum;
	public CountDownLatch countDownLatch;
	public double[][] InterData;
	public int PointNum;
	public int ValidPair;
	public int sbeta;
	public int ebeta;
	public double[] InitialSizes;
	public int ParticleNum;
	public int SearchRange;
	public int  w;
	public double c1;
	public double c2;
	public int model;
	
	public PSOSearch(int tn, CountDownLatch cdl, int m, double[][] InterD, int pNum, int vp,
			double[] IS, int sb, int eb, int ParticleN, int SR, int pw, double pc1, double pc2){
		threadNum = tn;
		countDownLatch = cdl;
		model = m;
		InterData = new double[InterD.length][InterD[0].length];
		for(int i=0; i<InterD.length; i++){
			InterData[i] = InterD[i].clone();
		}
		PointNum = pNum;
		ValidPair = vp;
		InitialSizes = new double[IS.length];
		InitialSizes = IS.clone();
		sbeta = sb;
		ebeta = eb;
		ParticleNum = ParticleN;
		SearchRange = SR;
		w = pw;
		c1 = pc1;
		c2 = pc2;
		finalR = new result(pNum, -1);
	}
	
	public result call(){
		for(int i=sbeta; i<=ebeta; i++){
			System.out.println(threadNum + ": " + i);
			result r = search(i/10.0);
			/**
			System.out.println("------------"+i/10.0+"------------");
			for(int k=0; k<10; k++){
				System.out.println(r.gBestParticle[k]);
			}
			**/
			if(r.gBestParticleScore > finalR.gBestParticleScore){
				finalR = r;
			}
		}
		countDownLatch.countDown();
    	System.out.println("thread " + threadNum + " finished!");
    	return finalR;
	}
	
	public double[] ExtractFlowData(double[][] InterDataTemp){
		double[] data = new double[ValidPair];
		int Count = 0;
		for(int i=0; i<PointNum; i++)
			for(int j=i+1; j<PointNum; j++){
				if(model == 0){
					if(InterDataTemp[i][j] > 0.0){
		                data[Count] = InterDataTemp[i][j];
		                Count += 1;
					}
				}
				else if(model == 1){
					data[Count] = InterDataTemp[i][j];
	                Count += 1;
				}
			}
		return data;
	}
	
	public void CreateFlows(double[] CitySize, double[][] InterDataTemp, double cbeta){
		for(int i=0; i<PointNum; i++)
	        for(int j=0; j<i; j++){
	        	if(model == 0){
		        	if(InterDataTemp[i][j] > 0){
		        		InterDataTemp[j][i] = CitySize[i]*CitySize[j]/Math.pow(InterDataTemp[i][j],cbeta);
		        	}
		        	else{
		        		InterDataTemp[j][i] = -1;
		        	}
	        	}
	        	else if(model == 1){
	        		InterDataTemp[j][i] = CitySize[i]*CitySize[j]/Math.pow(InterDataTemp[i][j],cbeta);
	        	}
	        }
	}
	
	public double PearsonCoefficient1D(double[] data1, double[] data2, int size){
		double mean1 = 0.0;
		double mean2 = 0.0;
		int i = 0;
		while(i<size){
			mean1 += data1[i];
			mean2 += data2[i];
			i += 1;
		}
		mean1 /= size;
		mean2 /= size;
		double cov1 = 0.0;
		double cov2 = 0.0;
		double cov12 = 0.0;
		
		i = 0;
		while(i<size){
			cov12 += (data1[i]-mean1)*(data2[i]-mean2);
		    cov1 += (data1[i]-mean1)*(data1[i]-mean1);
		    cov2 += (data2[i]-mean2)*(data2[i]-mean2);
		    i += 1;
		}
		if(Math.abs(cov1)<0.000001 || Math.abs(cov2)<0.000001){
			return 0;
		}
		return cov12/Math.sqrt(cov1)/Math.sqrt(cov2);
	}
	
	public result search(double cbeta){
		result r = new result(PointNum, cbeta);
		double[][] Particles = new double[ParticleNum][PointNum];
		for(int i=0; i<ParticleNum; i++)
			for(int j=0; j<PointNum; j++){
				if(i == 0){
					Particles[i][j] = InitialSizes[j];
				}
				else{
					Particles[i][j] = InitialSizes[j]+ (Math.random()*SearchRange);
				}
				if(Particles[i][j] > SearchRange){
					Particles[i][j] = SearchRange;
				}
			    if(Particles[i][j] < 0){
			    	Particles[i][j]= 0;
			    }
			}
		double[][] Velocity = new double[ParticleNum][PointNum];
		for(int i=0; i<ParticleNum; i++)
			for(int j=0; j<PointNum; j++){
				Velocity[i][j] = Math.random()*SearchRange/100.0;
			}
		
		double[] pBestParticleScore = new double[ParticleNum];
		double[][] pBestParticle = new double[ParticleNum][PointNum];
		double[] RealFlowData = ExtractFlowData(InterData);
		double[][] InterDataTemp = new double[InterData.length][InterData[0].length];
		for(int i=0; i<InterData.length; i++){
			InterDataTemp[i] = InterData[i].clone();
		}
		int IterCount = 0;
		while(true){
			double tBestScore = 0;
			for(int i=0; i<ParticleNum; i++){
				CreateFlows(Particles[i], InterDataTemp, cbeta);
				double[] FitData = ExtractFlowData(InterDataTemp);
				double gof = PearsonCoefficient1D(FitData, RealFlowData, ValidPair);
				if(gof - tBestScore > 0.000001){
	                tBestScore = gof;
				}
				if( gof > pBestParticleScore[i]){
	                pBestParticleScore[i] = gof;
	                for(int j = 0; j<PointNum; j++){
	                	pBestParticle[i][j] = Particles[i][j];
	                }
				}
				if( gof > r.gBestParticleScore){
	                r.gBestParticleScore = gof;
	                for (int j = 0; j<PointNum; j++){
	                    r.gBestParticle[j] = Particles[i][j];
	                }
				}        
			}
			
			double maxVelocity = 0;
			for(int i=0; i<ParticleNum; i++){
				double nc1 = c1 *Math.random();
				double nc2 = c2 *Math.random();
				for (int j = 0; j<PointNum; j++){
					double newVelocity = Velocity[i][j]*w + nc1*(pBestParticle[i][j]-Particles[i][j]) + nc2 *(r.gBestParticle[j]-Particles[i][j]);
					if(newVelocity + Particles[i][j] > SearchRange){
	                    newVelocity = SearchRange - Particles[i][j];
						}
	                if(newVelocity + Particles[i][j] < 0){
	                	newVelocity = -1* Particles[i][j];
	                	}
	                if(Math.abs(newVelocity) > maxVelocity){
	                	maxVelocity = newVelocity;   
	                	}
	                Velocity[i][j] = newVelocity;
	                Particles[i][j] += newVelocity;
	                }
				}
			IterCount += 1;
			if(model == 0){
				if(IterCount >= 1000 || maxVelocity < 5 || r.gBestParticleScore > 0.98){
		            break;
					}
			}
			else if(model == 1){
				if(IterCount >= 200){
		            break;
				}
			}
			}
		return r;
		}
			
}
