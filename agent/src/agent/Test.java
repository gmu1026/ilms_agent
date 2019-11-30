package agent;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class Test {
	public static void main(String[] args) {
		SimpleDateFormat format = new SimpleDateFormat ( "yyyy-MM-dd HH:mm:ss");
		
		Calendar time = Calendar.getInstance();
		
		String now = format.format(time.getTime());
		
		System.out.println("=======================================================================");
		System.out.println("[" + now + "]" + " " + "192.168.1.14 / 305-15");
		System.out.printf("%27s\n", "cpu정보");
		System.out.println("vga정보");
		System.out.println("ram정보");
		System.out.println("storage정보");
		System.out.println("=======================================================================");
	}
}
