
public class Leitor {
	
	//public flagBotao flag;
	
	public Leitor(flagBotao flag) {
		//this.flag = flag;
	}
	
	public static void main(String[] args) {
		
		//Cria a thread que checa se o botão foi pressionado
		flagBotao flag = new flagBotao(false);
		CheckBotao threadBotao = new CheckBotao(flag);
		threadBotao.start();
		
		
	}

}
