import org.apache.commons.lang.math.NumberUtils;



/**
 *@author Kevin.yang
 *@since 2015年5月17日
 */
public class Test {

	public static void main(String[] args) {
		
		System.out.println(NumberUtils.createBigDecimal("0.05"));
	}
	
	
	
	
	class A {
		private String i;

		public String getI() {
			return i;
		}

		public void setI(String i) {
			this.i = i;
		}
	}
	
	class B extends A{
		private String j;

		public String getJ() {
			return j;
		}

		public void setJ(String j) {
			this.j = j;
		}
		
	}
	
	

}
