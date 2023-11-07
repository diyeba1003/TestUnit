package evaluation;

import models.IRModel;
import models.NonPreparedModelException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.LinkedHashMap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class APTest {
    @Mock
    private Hyp hyp; // Utilisez directement le mock

    @Mock
    private IRModel model;

    private Query query;

    @BeforeEach
    void setUp() throws NonPreparedModelException {
        MockitoAnnotations.initMocks(this);
        query = new Query("1", "Requête fictive");
        // Hyp ne doit pas être créé ici
    }

    @Test
    void testEval() {
        //liste de classement
        LinkedHashMap<String, Double> ranking = new LinkedHashMap<>();
        ranking.put("doc1", 0.8);
        ranking.put("doc2", 0.6);
        Mockito.lenient().when(hyp.getRanking()).thenReturn(ranking);
        Mockito.lenient().when(hyp.getModel()).thenReturn(model);
        Mockito.lenient().when(hyp.getQuery()).thenReturn(query);

        query.addRelevant("doc1");
        query.addRelevant("doc2");


        AP ap = new AP();
        Result result = ap.eval(hyp);
        double expectedAP = (1.0 / 1.0 + 2.0 / 2.0) / 2.0;
        assertEquals(expectedAP, result.getScore("AP"), 1e-6);
    }
}
