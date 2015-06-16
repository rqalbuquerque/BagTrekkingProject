import java.util.Scanner;


public class CheckBotao extends Thread{

	public flagBotao flag;
	
	public CheckBotao(flagBotao flag) {
		this.flag = flag;
	}

	public void run(){
		
		Scanner scanner = new Scanner(System.in);
	    String readString = scanner.nextLine();
	    
	    while(!flag.get()) {
	        if (scanner.hasNextLine()){
	        	readString = scanner.nextLine();
	        	if(readString.equals("c")){
		        	synchronized (flag) {
		        		flag.set(true);	
					}
		        	break;
	        	}
	        }	            
	    }
	    System.out.println("Verificacao conclu�da");
	}
}
