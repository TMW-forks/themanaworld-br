/**
 * Define o objeto Token. Utilizado pela classe Script.
 * 
 * @data 10/05/2010
 * @author Diogo_RBG - http://diogorbg.blogspot.com/
 * 
 * Wikip�dia: Token em computa��o � um segmento de texto ou s�mbolo que pode ser manipulado por
 * um parser, que fornece um significado ao texto; em outras palavras, � um conjunto de caracteres
 * (de um alfabeto, por exemplo) com um significado coletivo.
 */

package parser;

enum TipoToken{
	CMD, // Comando
	TXT, // Texto
	VLR, // Valor
	VAR  // Vari�vel ou constante
}

public class Token {
	private TipoToken tipo;
	private String valor;

	public Token(TipoToken _tipo, String _valor) {
		tipo = _tipo;
		valor = _valor;
	}

	/**
	 * O tipo de token. Que pode ser comando, texto, valor ou vari�vel/constante.
	 * @return Um objeto do tipo TipoToken.
	 */
	public TipoToken getTipo() {
		return tipo;
	}

	/**
	 * Retorna o valor do token. Tokens do tipo TXT n�o possuem aspas duplas >> " << e nem caracteres de escape >> \ <<.
	 * @return Uma string contendo o token em si.
	 */
	public String getValor() {
		return valor;
	}

}
