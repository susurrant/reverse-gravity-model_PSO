
public class result {
	public double gBestParticleScore;
	public double[] gBestParticle;
	public double beta;
	
	public result(int num, double cbeta){
		gBestParticle = new double[num];
		gBestParticleScore = 0.0;
		beta = cbeta;
	}
}
