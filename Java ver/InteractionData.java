import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class InteractionData {
	public ArrayList<String> PID;
	public ArrayList<Double[]> PCO;
	public ArrayList<String[]> FID;
	public ArrayList<Double> FV;
	
	public InteractionData(String fF, String pF, String encoding){
		PID = new ArrayList<String>();
		PCO = new ArrayList<Double[]>();
		FID = new ArrayList<String[]>();
		FV = new ArrayList<Double>();
		try {
            File flowFile = new File(fF);
            File pointFile = new File(pF);
            if(flowFile.isFile() && pointFile.isFile() && flowFile.exists() && pointFile.exists()){ 
                InputStreamReader freader = new InputStreamReader(new FileInputStream(flowFile), encoding);
                BufferedReader fbufferedReader = new BufferedReader(freader);
                String flineTxt = null;
                while((flineTxt = fbufferedReader.readLine()) != null){
                	//System.out.println(flineTxt);
                	String[] fs = flineTxt.trim().split(" ");
                	String[] ts = {fs[0], fs[1]};
                	FID.add(ts);
                	FV.add(Double.parseDouble(fs[2]));
                }
                freader.close();
                
                InputStreamReader preader = new InputStreamReader(new FileInputStream(pointFile), encoding);
                BufferedReader pbufferedReader = new BufferedReader(preader);
                String plineTxt = null;
                while((plineTxt = pbufferedReader.readLine()) != null){
                	String[] ps = plineTxt.trim().split(" ");
                	PID.add(ps[0]);
                	Double[] tp = {Double.parseDouble(ps[1]), Double.parseDouble(ps[2])};
                	PCO.add(tp);
                }
                preader.close();
            }else{
            	System.out.println("Cannot find the file!");
            }
		} catch (Exception e) {
			System.out.println("error!");
			e.printStackTrace();
		}
	}
	
}
