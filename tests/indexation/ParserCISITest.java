package indexation;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.extension.*;

import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import core.Document;

import org.mockito.*;
//import static org.mockito.Mockito.*;

import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ParserCISITest {

	@Spy
	private ParserCISI parser;

	@BeforeEach
	void setUp() {

		parser	=spy(new ParserCISI());
	}

	@Test
	public void testGetDocument() {
		try {

			// Appel de la méthode getDocument pour extraire un Document à partir de la chaîne CISI
			Document doc = parser.getDocument(str1);

			// Vérification de Id
			assertEquals("1", doc.getId());

			// Vérification de l'auteur
			assertEquals(" Comaromi, J.P.", doc.get("author"));
			// Vérification du titre
			assertEquals(" 18 Editions of the Dewey Decimal Classifications", doc.get("title"));


		} catch (InvalidFormatDocumentException e) {
			fail("L'exception InvalidFormatDocumentException ne devrait pas être lancée.");
		}
	}


	@Test
	void testGetDocumentInvalid(){
		assertThrows(InvalidFormatDocumentException.class,() ->{parser.getDocument(str2);});
	}




	@Test
	void testNextDocumentWhenParserInitialized(){
		try {
			//simulation de la methode getDocument
			doReturn(new Document("1", "Text 1")).when(parser).getDocument(anyString());

			//initialisation du parser
			parser.init("data/cisi/cisi.txt");

			//appel de la methode nextDocument
			Document doc = parser.nextDocument();

			//verif doc retourné
			assertEquals("1", doc.getId());
			assertEquals("Text 1", doc.getText());

            // nombre d'appel à la methode getDocument
			verify(parser, times(1)).getDocument(anyString());
		} catch (InvalidFormatDocumentException e) {
			throw new RuntimeException(e);
		} catch (NonInitializedParserException e) {
            throw new RuntimeException(e);
        }
    }

	@Test
	void testNextDocumentParserWhenNotInitialized(){
		try {
		//appel de nextDocument
		assertThrows(NonInitializedParserException.class, () -> parser.nextDocument());

		// verification qu'aucun appel n'a pas été effectué
			verify(parser, never()).getDocument(anyString());
		} catch (InvalidFormatDocumentException e) {
			throw new RuntimeException(e);
		}
	}

	@Test
	void testNextDocumentINvalidFormat(){

		try {
			parser.init("data/cisi/cisi.txt");
			//simulation de document invalide
			Document doc = parser.getDocument(invalidText);
		} catch (InvalidFormatDocumentException e) {
			throw new RuntimeException(e);
		}

	}




	// chaine invalid à tester
	String invalidText = ".I 1\n" +
			".T Invalid Document\n" +
			".W This document is invalid.\n" +
			".X 2\t1\t1";


	// Chaîne au format CISI à analyser


	final String str1 = ".I 1\n"
			+ ".T\n"
			+ "18 Editions of the Dewey Decimal Classifications\n"
			+ ".A\n"
			+ "Comaromi, J.P.\n"
			+ ".W\n"
			+ "The present study is a history of the DEWEY Decimal\n"
			+ "Classification.  The first edition of the DDC was published\n"
			+ "in 1876, the eighteenth edition in 1971, and future editions\n"
			+ "will continue to appear as needed.  In spite of the DDC's\n"
			+ "long and healthy life, however, its full story has never\n"
			+ "been told.  There have been biographies of Dewey\n"
			+ "that briefly describe his system, but this is the first\n"
			+ "attempt to provide a detailed history of the work that\n"
			+ "more than any other has spurred the growth of\n"
			+ "librarianship in this country and abroad.\n"
			+ ".X\n"
			+ "1	5	1\n"
			+ "92	1	1\n"
			+ "262	1	1\n"
			+ "556	1	1\n"
			+ "1004	1	1\n"
			+ "1024	1	1\n"
			+ "1024	1	1\n";


	final String str2 =".T\n"
			+ "Use Made of Technical Libraries\n"
			+ ".A\n"
			+ "Slater, M.\n"
			+ ".W\n"
			+ "This report is an analysis of 6300 acts of use\n"
			+ "in 104 technical libraries in the United Kingdom.\n"
			+
			"Library use is only one aspect of the wider pattern of\n"
			+ "information use.  Information transfer in libraries is\n"
			+ "restricted to the use of documents.  It takes no\n"
			+ "account of documents used outside the library, still\n"
			+ "less of information transferred orally from person\n"
			+ "to person.  The library acts as a channel in only a\n"
			+ "proportion of the situations in which information is\n"
			+ "transferred.\n"
			+ "Taking technical information transfer as a whole,\n"
			+ "there is no doubt that this proportion is not the\n"
			+ "major one.  There are users of technical information -\n"
			+ "particularly in technology rather than science -\n"
			+ "who visit libraries rarely if at all, relying on desk\n"
			+ "collections of handbooks, current periodicals and personal\n"
			+ "contact with their colleagues and with people in other\n"
			+ "organizations.  Even regular library users also receive\n"
			+ "information in other ways.\n"
			+ ".X\n"
			+ "2	5	2\n"
			+ "32	1	2\n"
			+ "76	1	2\n"
			+ "132	1	2\n"
			+ "137	1	2\n"
			+ "139	1	2\n"
			+ "152	2	2\n"
			+ "155	1	2\n"
			+ "158	1	2\n"
			+ "183	1	2\n"
			+ "195	1	2\n"
			+ "203	1	2\n"
			+ "204	1	2\n"
			+ "210	1	2\n"
			+ "243	1	2\n"
			+ "371	1	2\n"
			+ "475	1	2\n"
			+ "552	1	2\n"
			+ "760	1	2\n"
			+ "770	1	2\n"
			+ "771	1	2\n"
			+ "774	1	2\n"
			+ "775	1	2\n"
			+ "776	1	2\n"
			+ "788	1	2\n"
			+ "789	1	2\n"
			+ "801	1	2\n"
			+ "815	1	2\n"
			+ "839	1	2\n"
			+ "977	1	2\n"
			+ "1055	1	2\n"
			+ "1056	1	2\n"
			+ "1151	1	2\n"
			+ "1361	1	2\n"
			+ "1414	1	2\n"
			+ "1451	1	2\n"
			+ "1451	1	2\n";

}
