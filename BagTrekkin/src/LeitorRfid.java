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
	
	private static Set<String> malas_erradas = new TreeSet<String>();
	private static Set<String> malas_corretas = new TreeSet<String>();

	//cria um leitor
	public LeitorRfid() throws Exception {
		reader.setConnection("192.168.8.54", 23);
		reader.setUsername("alien");
		reader.setPassword("password");
	}
	
	public Set<String> getMalasCorretas(){
		return malas_corretas;
	}
	
	public Set<String> getMalasErradas(){
		return malas_erradas;
	}
	
	//l� as tags rfids (apenas uma vez)
	public void read() throws Exception {
		// Open a connection to the reader
		reader.open();
		if(reader.isOpen()){
			System.out.println("Leitor aberto");
		}
		
		// Ask the reader to read tags and print them
		Tag tagList[] = reader.getTagList();
		if (tagList == null) {
			System.out.println("No Tags Found");
			//algum aviso de sa�da?
		} else {
			Set<String> malas = vooList.getMalas();
			if(malas == null)
				System.out.println("Erro: sem malas");
			for(String s: malas){
				System.out.println(s);
			}
			
			//pega tag por tag lida pelo leitor
			for (int i = 0; i < tagList.length; i++) {
				Tag tag = tagList[i];
				Boolean achou = false;
				
				//procura mala na lista de malas do voo
				if(malas != null){
					for(String m: malas){
						if(m.equals(tag.getTagID())){
							malas_corretas.add(tag.getTagID().toString());
							//malas.remove(m);
							achou = true;
							break;
						}
					}
				}
				
				//adiciona mala errada na lista de malas erradas
				if(!achou){
					Boolean achou_mala_errada = false;
					for(String m: malas_erradas){
						if(m.equals(tag.getTagID())){
							achou_mala_errada = true;
						}
					}
					if(!achou_mala_errada){
						malas_erradas.add(tag.getTagID().toString());
					}
				}
			}
			reader.close();
			//atualiza a lista de malas do voo
			vooList.setMalas(malas);
			//con.sendMalasCorretas(malas_corretas);
			//con.sendMalasErradas(malas_erradas);
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
		Conexao_http con = new Conexao_http("http://bagtrekkin.herokuapp.com/");
        //Entrada 
		Scanner entrada = new Scanner(System.in); 
				
		// Thread do bot�o
		FlagBotao botao = new FlagBotao(false);
		ThreadBotao threadBotao = new ThreadBotao(botao);
		threadBotao.start();
		
		while(true){	
			
			System.out.println("Digite o numero do voo:");
			String voo = "";
			voo = entrada.nextLine();	//TP443
			con.flushCurrentFlight();
			if(con.setCurrentFlight(voo)){
				System.out.println("Voo setado");
			}
			
			vooList = new Voo(voo);
			Set<String> list = con.getLuggageList();
			if(list != null){
				vooList.setMalas(list);
			}
			
			for(String s: list){
				System.out.println(s);
			}
			
			int i=0;
			while(botao.get() != true){
				try {
					leitor.read();
					System.out.println("Leitura: " + i++);
				} catch (AlienReaderException e) {
					leitor.encerrar();
				}
			}
			
			System.out.println("Tags malas Corretas:");
			for(String m: leitor.getMalasCorretas()){
				System.out.println(m);
			}
			System.out.println("Tags malas erradas:");
			for(String m: leitor.getMalasErradas()){
				System.out.println(m);
			}
		}
	}

} 