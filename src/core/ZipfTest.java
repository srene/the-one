//import org.apache.commons.math.distribution.ZipfDistribution;
//import src.org.apache.commons.math3.distribution.ZipfDistribution;
package core;

//	import core.Zipf;

public class ZipfTest{

	private static Zipf zipf;
	private static int users;
	private static double s;
	
	//private static double s;

	public static void main(String[] args){
		System.out.println("Yes");
		int[] count;
		
		if ((args != null) && args.length == 2){
			users = Integer.parseInt(args[0]);
			s = Double.parseDouble(args[1]);
		}
		else {
			users = 5;
			s = 1;
		}
		
		System.out.println(users+" users\n");
		zipf = new Zipf(users, false, s);
		count = new int[users];
		
		for (int i=0; i<users; i++)
			count[zipf.nextZipf()]++;

		for (int i=0; i<count.length; i++)
			System.out.println((i+1)+" : "+count[i]);

		System.out.println();

		for (int i=0; i<users; i++)
			System.out.println(zipf.getProbability(i));

		System.out.println();

		boolean[] table = zipf.fillBoolTable();
		for (int i=0; i<count.length; i++)
			System.out.println((i+1)+" : "+table[i]);

	}
}