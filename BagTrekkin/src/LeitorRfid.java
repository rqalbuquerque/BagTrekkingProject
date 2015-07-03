import java.io.IOException;
import java.util.Scanner;
import java.util.Set;
import java.util.TreeSet;

import Modelo.Voo;
import WiFi.Conexao_http;

import com.alien.enterpriseRFID.reader.AlienClass1Reader;
import com.alien.enterpriseRFID.reader.AlienReaderException;
import com.alien.enterpriseRFID.tags.Tag;

public class LeitorRfid {
	private AlienClass1Reader reader = new AlienClass1Reader();
	private static Voo vooList;
	
	private static final String URL_BagTrekkin = "http://bagtrekkin.herokuapp.com/";
	
	private Set<String> malas_erradas = new TreeSet<String>();
	private Set<String> malas_corretas = new TreeSet<String>();

	//cria um leitor
	public LeitorRfid() throws Exception {
		reader.setConnection("192.168.8.54", 23);
		reader.setUsername("alien");
		reader.setPassword("password");
	}
	public void flush_malas_corretas(){
		malas_corretas.clear();
	}
	
	public void flush_malas_erradas(){
		malas_erradas.clear();
	}
	
	
	public Set<String> getMalasCorretas(){
		return malas_corretas;
	}
	
	public Set<String> getMalasErradas(){
		return malas_erradas;
	}
	
	//lê as tags rfids (apenas uma vez)
	public void read() throws Exception {
		// Open a connection to the reader
		reader.open();
		if(!reader.isOpen()){
			System.out.println("Verifique se o leitor esta conectado. Em caso afirmativo entao chamar a assistencia tecnica.");
		} else {
			System.out.println("Leitor aberto");
	
			// Verifica se o leitor foi aberto e imprime todas as tags lidas
			Tag tagList[] = reader.getTagList();
			if (tagList == null) {
				System.out.println("No Tags Found");
				//algum aviso de saída?
			} else {
				Set<String> malas = vooList.getMalas();
				if(malas == null){
					System.out.println("Erro: sem malas");
				} else {
					System.out.println("Lista de malas que sera verificada.");
					for(String s: malas){
						System.out.println(s);
					}
				}
				if(malas != null){
					//pega tag por tag lida pelo leitor
					for (int i = 0; i < tagList.length; i++) {
						Tag tag = tagList[i];
						if(malas.contains(tag.getTagID())){
							System.out.println("mala correta entrando na lista de corretas: " + tag.getTagID());
							malas_corretas.add(tag.getTagID());
						} else {
							System.out.println("mala errada entrando na lista de errada: " + tag.getTagID());
							malas_erradas.add(tag.getTagID());
						}
					}
				}
			}
			reader.close();
		}
	}
	
	//encerra a conexao
	public void encerrar() throws IOException{
		reader.close();
	}
		
	// main para testes
	public static final void main(String args[]) throws Exception {
		//Leitor
		LeitorRfid leitor = new LeitorRfid();
		//Conexao
		Conexao_http con = new Conexao_http(URL_BagTrekkin);
        //Entrada 
		Scanner entrada = new Scanner(System.in); 
				
		while(true){
			Set<String> list=null;
			do{
				// Define o voo 
				System.out.println("Digite o numero do voo:");
				String voo = entrada.nextLine();	//TP443
				//String voo = "TP443";
				System.out.println("Voo requerido: "+voo);
				con.flushCurrentFlight();
				if(con.setCurrentFlight(voo)){
					System.out.println("Voo setado");
					// Obtem a lista de malas do voo
					vooList = new Voo(voo);
					list = con.getLuggageList();
					if(list != null){
						vooList.setMalas(list);
					} else {
						System.out.println("Nao existem malas cadastradas ou voce nao tem permissao de acesso a esse voo.");
					}
				} else {
					System.out.println("voo requerido nao existe");
				}
			}while(list == null);
			
			// Imprime a lista
			System.out.println("Lista de malas recebidas do servidor.");
			for(String s: list){
				System.out.println(s);
			}
			
			// Thread do botão
			FlagBotao botao = new FlagBotao(false);
			ThreadBotao threadBotao = new ThreadBotao(botao);
			threadBotao.start();
			// Leitura até que o botão seja apertada e
			//  a verificação acabe
			int i=0;
			leitor.flush_malas_corretas();
			leitor.flush_malas_erradas();
			while(botao.get() != true){
				try {
					leitor.read();
					System.out.println("Leitura: " + i++);
				} catch (AlienReaderException e) {
					leitor.encerrar();
					System.out.println("Excecao gerada depois da leitura: "+e);
				}
			}
			
			// Teste malas corretas e malas erradas
			System.out.println("Lista de malas corretas:");
			Set<String> lista_corretas = leitor.getMalasCorretas();
			for(String n: lista_corretas){
				System.out.println(n);
			}
			System.out.println("Lista de malas erradas:");
			Set<String> lista_erradas = leitor.getMalasErradas();
			for(String n: lista_erradas){
				System.out.println(n);
			}
			// Envia a lista de malas corretas e erradas
			if(con.setCorrectLuggageList(leitor.getMalasCorretas(),leitor.getMalasErradas())){
				System.out.println("Lista de Malas enviadas para o servidor!");
			} else {
				System.out.println("Lista de Malas nao foi enviada para o servidor!");
			}
		}
	}
} 