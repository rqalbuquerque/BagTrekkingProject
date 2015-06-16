import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.Set;
import java.util.TreeSet;

import Modelo.Mala;
import Modelo.Voo;
import WiFi.Conexao_http;

import com.alien.enterpriseRFID.reader.AlienClass1Reader;
import com.alien.enterpriseRFID.reader.AlienReaderException;
import com.alien.enterpriseRFID.tags.Tag;

public class LeitorRfid {
	private AlienClass1Reader reader = new AlienClass1Reader();
	private static Voo vooList;

	private String strMalasCorretas;
	private String strMalasErradas;

	//cria um leitor
	public LeitorRfid() throws Exception {
		reader.setConnection("192.168.8.54", 23);
		reader.setUsername("alien");
		reader.setPassword("password");
	}
	
	//lê as tags rfids (apenas uma vez)
	public void read() throws Exception {
		// Open a connection to the reader
		reader.open();

		// Ask the reader to read tags and print them
		Tag tagList[] = reader.getTagList();
		if (tagList == null) {
			//System.out.println("No Tags Found");
			//algum aviso de saída?
		} else {
			String envio = "";
			envio = envio + "Tag(s) found:" + "\n";
			
			List<Mala> malas = vooList.getMalas();
			List<Mala> malas_erradas = new ArrayList<Mala>();
			List<Mala> malas_corretas = new ArrayList<Mala>();
			
			//pega tag por tag lida pelo leitor
			for (int i = 0; i < tagList.length; i++) {
				Tag tag = tagList[i];
				Boolean achou = false;
				
				//procura mala na lista de malas do voo
				if(malas != null){
					for(Mala m: malas){
						if(m.getTag().equals(tag.getTagID())){
							malas_corretas.add(new Mala(tag.getTagID(), m.getPassageiro()));
							malas.remove(m);
							achou = true;
							break;
						}
					}
				}
				
				//System.out.println("Malas Corretas");
				for(Mala m: malas_corretas){
					//System.out.println("ID: " + m.getTag());
					//System.out.println("Passageiro: " + m.getPassageiro());
				}
				
				//adiciona mala errada na lista de malas erradas
				if(!achou){
					Boolean achou_mala_errada = false;
					for(Mala m: malas_erradas){
						if(m.getTag().equals(tag.getTagID())){
							achou_mala_errada = true;
						}
					}
					if(!achou_mala_errada){
						malas_erradas.add(new Mala(tag.getTagID(), "desconhecido"));
					}
				}
				
				//System.out.println("Malas Incorretas");
				for(Mala m: malas_erradas){
					//System.out.println("ID: " + m.getTag());
					//System.out.println("Passageiro: " + m.getPassageiro());
				}
				
				envio = envio + "ID:" + tag.getTagID() + ", Discovered:"
						+ tag.getDiscoverTime() + ", Last Seen:"
						+ tag.getRenewTime() + ", Antenna:" + tag.getAntenna()
						+ ", Reads:" + tag.getRenewCount() + "\n";
				
				//System.out.println(envio);
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
		//Lista de malas para teste
        /*List<Mala> lista_malas = new ArrayList<Mala>();
        lista_malas.add(new Mala("E200 3411 B802 0115 1612 6723", "Arthur"));
        lista_malas.add(new Mala("E200 2996 9618 0246 2310 256F", "Renato"));
        lista_malas.add(new Mala("E200 6296 9619 0229 0370 EC2B", "Saulo"));
        lista_malas.add(new Mala("E200 2996 9618 0246 2230 2CD7", "David"));
        vooList = new Voo("0001", lista_malas);
		*/
		
		//Leitor
		LeitorRfid leitor = new LeitorRfid();
		//Conexao
		Conexao_http con = new Conexao_http("http://bagtrekkin.herokuapp.com/");
        
		Scanner entrada = new Scanner(System.in); 
		String voo = "";
		
		// Thread do botão
		FlagBotao botao = new FlagBotao(false);
		ThreadBotao threadBotao = new ThreadBotao(botao);
		threadBotao.start();
		
		while(true){	
			
			System.out.println("Digite o numero do voo:");
			voo = entrada.nextLine();
			con.setCurrentFlight(voo);
			
			Set<String> ListaVoo = new TreeSet<String>();
			ListaVoo = con.getLuggageList();
			
			while(botao.get() != true){
				try {
					leitor.read();
					//System.out.println(".");
				} catch (AlienReaderException e) {
					leitor.encerrar();
					//System.out.println("Error: " + e.toString());
				}
				//se leu alguma coisa, então chama a thread para checar as tags.
			}
		}
	}

} 