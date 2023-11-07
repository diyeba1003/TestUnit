package models;




import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.times;

import java.util.HashMap;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.extension.*;

import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import core.Document;
import indexation.Index;

import org.mockito.*;
//import static org.mockito.Mockito.*;

import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class WeighterTFTest {

	private WeighterTF weighterTF;


	@Mock
	private Index index;


	@BeforeEach
	void setUp() {
		weighterTF = new WeighterTF(index);
	}

	
	@Test
	void  getDocWeightsForStemTest() {
		//données pour simuler index
		String st = "monCat";
		String dc1 ="doc1";
		String dc2 = "doc2";
     HashMap<String, Integer> res = new HashMap<>();
	 res.put(dc1,3);
	 res.put(dc2,2);

	 //comportement attendu
		when(index.getTfsForStem(st)).thenReturn(res);

		//appel de la methode
		HashMap<String, Double> result = weighterTF.getDocWeightsForStem(st);

		// verification du resultat
		assertEquals(3.0, result.get(dc1));
		assertEquals(2.0, result.get(dc2));

		//verify
		verify(index, times(1)).getTfsForStem(st);


	}

}
