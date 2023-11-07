package models;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.HashMap;
import static org.mockito.Mockito.when;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
public class VectorielTest {
    @Mock
    private Weighter weighter;
    private Vectoriel vectNormal;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
        vectNormal = new Vectoriel(weighter, true);
        vectNormal.prepared = true;  // Simuler que le modèle est préparé
    }

    @Test
    void testGetScoresNormalizedTrue() {
        // poids attendus pour weighter.getQueryWeights(query)
        HashMap<String, Integer> query = new HashMap<>();
        query.put("term1", 2);
        query.put("term2", 3);

        when(weighter.getQueryWeights(query)).thenReturn(
                new HashMap<String, Double>() {{
                    put("term1", 2.0);
                    put("term2", 3.0);
                }}
        );

        when(weighter.getDocWeightsForDoc("doc1")).thenReturn(
                new HashMap<String, Double>() {{
                    put("term1", 1.0);
                    put("term2", 2.0);
                }}
        );
        when(weighter.getDocWeightsForDoc("doc2")).thenReturn(
                new HashMap<String, Double>() {{
                    put("term1", 3.0);
                    put("term2", 4.0);
                }}
        );

        HashMap<String, Double> scores = null;
        try {
            scores = vectNormal.getScores(query);
        } catch (NonPreparedModelException e) {
            throw new RuntimeException(e);
        }

        assertEquals(2.0, scores.get("doc1"), 1e-6);
        assertEquals(3.0, scores.get("doc2"), 1e-6);
    }
}
