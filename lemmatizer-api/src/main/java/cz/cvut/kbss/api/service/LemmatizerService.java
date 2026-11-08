package cz.cvut.kbss.api.service;

import cz.cvut.kbss.textanalysis.lemmatizer.LemmatizerApi;
import cz.cvut.kbss.textanalysis.lemmatizer.model.SingleLemmaResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

//@ExtendWith(SpringExtension.class)
//@ContextConfiguration(initializers = ConfigDataApplicationContextInitializer.class,
//        classes = {MorphoditaConf.class, MorphoDitaServiceJNI.class, LLMLemmatizerTextConf.class, LLMLemmatizer.class, SparkConf.class, SparkLemmatizer.class})
@Service
@Slf4j
public class LemmatizerService {

    @Autowired
    private LemmatizerApi lemmatizer;

    public List<List<SingleLemmaResult>> lemmatize(String text, String lang) {
        return lemmatizer.process(text, lang).getResult();
    }
}