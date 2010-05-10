/**
 * Classe parser gen�rica que ser� utilizada por todas as outras.
 * 
 * @data 10/05/2010
 * @author Diogo_RBG - http://diogorbg.blogspot.com/
 * 
 * Wikip�dia: Em ci�ncia da computa��o e lingu�stica, an�lise sint�tica (tamb�m conhecida pelo termo
 * em ingl�s parsing) � o processo de analisar uma sequ�ncia de entrada (lida de um arquivo de computador
 * ou do teclado, por exemplo) para determinar sua estrutura gramatical segundo uma determinada gram�tica
 * formal. Essa an�lise faz parte de um compilador, junto com a an�lise l�xica e an�lise sem�ntica.
 */

package parser;

import java.util.HashMap;

public class Parser {

	private HashMap<String,String[]> dados = new HashMap<String,String[]>();

	public Parser() {
		// constructor
	}

	public HashMap<String, String[]> getDados() {
		return dados;
	}

}
