package Modelo;

import java.util.List;
import java.util.Set;
import java.util.TreeSet;

public class Voo {
	private String numero;
	private Set<String> malas = new TreeSet<String>();
	
	public Voo(String numero){
		this.numero = numero;
	}
	
	public String getNumero() {
		return numero;
	}

	public void setNumero(String numero) {
		this.numero = numero;
	}

	public Set<String> getMalas() {
		return malas;
	}

	public void setMalas(Set<String> malas) {
		this.malas = malas;
	}

	public Voo(String numero, Set<String> malas) {
		super();
		this.numero = numero;
		this.malas = malas;
	}
	
	public String formataStringVoo(){
		String strVoo = "";
		
		strVoo += numero + "\n";
		for(String item : malas){
			strVoo += item + "\n";
		}
		
		return strVoo;
	}
}
